package com.screenlocker.secure.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.async.DownLoadAndInstallUpdate;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.settings.codeSetting.installApps.UpdateModel;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static com.screenlocker.secure.app.MyApplication.saveToken;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.SYSTEM_LOGIN_TOKEN;
import static com.screenlocker.secure.utils.AppConstants.URL_1;
import static com.screenlocker.secure.utils.AppConstants.URL_2;

public class CheckUpdateService extends JobService {

    private AsyncCalls asyncCalls;

    @Override
    public boolean onStartJob(JobParameters params) {

        if (MyApplication.oneCaller == null) {

            String[] urls = {URL_1, URL_2};

            if (asyncCalls != null) {
                asyncCalls.cancel(true);
            }

            asyncCalls = new AsyncCalls(output -> {
                Timber.d("output : " + output);
                if (output != null) {
                    PrefUtils.saveStringPref(this, LIVE_URL, output);
                    String live_url = PrefUtils.getStringPref(this, LIVE_URL);
                    Timber.d("live_url %s", live_url);
                    MyApplication.oneCaller = RetrofitClientInstance.getRetrofitInstance(live_url + MOBILE_END_POINT).create(ApiOneCaller.class);
                    checkUpdatesIfAvailAble(params);
                }
            }, this, urls);// checking hosts
            asyncCalls.execute();

        } else {
            checkUpdatesIfAvailAble(params);

        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }




    //TODO:
    private void checkUpdatesIfAvailAble(JobParameters parameters) {
        AppExecutor.getInstance().getExecutorForUpdatingList().execute(() -> {
            try {
                String currentVersion = String.valueOf(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
                Response<UpdateModel> response = MyApplication.oneCaller.getUpdate("getUpdate/" + currentVersion + "/" + getPackageName() + "/" + getString(R.string.app_name), PrefUtils.getStringPref(this, SYSTEM_LOGIN_TOKEN))
                        .execute();
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().isApkStatus()) {
                        String url = response.body().getApkUrl();
                        String live_url = PrefUtils.getStringPref(MyApplication.getAppContext(), LIVE_URL);
                        downloadApp(live_url + MOBILE_END_POINT + "getApk/" + CommonUtils.splitName(url),getPackageName());
                    }

                } else {
                    saveToken();
                    checkUpdatesIfAvailAble(parameters);
                }
                List<ApplicationInfo> packages = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);

                for (ApplicationInfo appInfo : packages) {
                    String currAppName = getPackageManager().getApplicationLabel(appInfo).toString();
                    try {
                        String version = String.valueOf(getPackageManager().getPackageInfo(appInfo.packageName, 0).versionCode);

                        String srcDir = appInfo.sourceDir;
                        //Log.d(TAG, currAppName+": "+srcDir);
                        if (srcDir.startsWith("/data/app/") && getPackageManager().getLaunchIntentForPackage(appInfo.packageName) != null) {
                            Response<UpdateModel> response2 = MyApplication.oneCaller
                                    .getUpdate("getUpdate/" + version + "/" + appInfo.packageName + "/" + currAppName, PrefUtils.getStringPref(this, SYSTEM_LOGIN_TOKEN))
                                    .execute();
                            if (response2.isSuccessful()) {
                                if (response2.body() != null && response2.body().isApkStatus()) {
                                    String url = response2.body().getApkUrl();
                                    String live_url = PrefUtils.getStringPref(MyApplication.getAppContext(), LIVE_URL);
                                   downloadApp(live_url + MOBILE_END_POINT + "getApk/" + CommonUtils.splitName(url), appInfo.packageName);


                                }

                            } else {
                                saveToken();
                                checkUpdatesIfAvailAble(parameters);
                            }
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                jobFinished(parameters,false);
            } catch (PackageManager.NameNotFoundException e) {
                Timber.d(e);
            } catch (IOException e) {
                e.printStackTrace();
                jobFinished(parameters,false);
            }
        });
    }


    private void downloadApp(String url, String pkg) {
        FileOutputStream fileOutputStream = null;
        InputStream input = null;
        try {
            String appName = new Date().getTime() + ".apk";
            File apksPath = new File(getFilesDir(), "apk");
            File file = new File(apksPath, appName);
            if (!apksPath.exists()) {
                apksPath.mkdir();
            }

            try {
                fileOutputStream = new FileOutputStream(file);

                URL downloadUrl = new URL(url);
                URLConnection connection = downloadUrl.openConnection();
                connection.setRequestProperty("authorization", PrefUtils.getStringPref(this, SYSTEM_LOGIN_TOKEN));
                int contentLength = connection.getContentLength();
                Timber.d("downloadUrl: %s ", downloadUrl.toString());
                // input = body.byteStream();
                input = new BufferedInputStream(downloadUrl.openStream());
                byte data[] = new byte[contentLength];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    Timber.d("downloadApp: %s", total);
                    fileOutputStream.write(data, 0, count);
                }
                Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);

                //Uri uri =  FileProvider.getUriForFile(contextWeakReference.get(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
                Timber.d("downloadApp: %s ", contentUri.toString());


                //TODO:request installation
                showInstallDialog(contentUri,pkg);

            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                if (input != null)
                    input.close();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    private void showInstallDialog(Uri apkUri, String packageName) {


        Intent launchIntent = new Intent();
        ComponentName componentName = new ComponentName("com.secure.systemcontrol", "com.secure.systemcontrol.MainActivity");
        launchIntent.setAction(Intent.ACTION_MAIN);
        launchIntent.setComponent(componentName);
        launchIntent.setData(apkUri);

        launchIntent.putExtra("package", packageName);
        launchIntent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(launchIntent);
    }

}




