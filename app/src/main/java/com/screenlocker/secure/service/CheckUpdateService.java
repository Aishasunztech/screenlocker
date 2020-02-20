package com.screenlocker.secure.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.networkResponseModels.LoginModel;
import com.screenlocker.secure.networkResponseModels.LoginResponse;
import com.screenlocker.secure.retrofit.ErrorLogRequestBody;
import com.screenlocker.secure.retrofit.ErrorResponse;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.retrofitapis.LogsAPICaller;
import com.screenlocker.secure.room.MyAppDatabase;
import com.screenlocker.secure.settings.codeSetting.installApps.UpdateModel;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;
import com.secure.launcher.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Credentials;
import retrofit2.Response;
import timber.log.Timber;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static com.screenlocker.secure.utils.AppConstants.GET_APK_ENDPOINT;
import static com.screenlocker.secure.utils.AppConstants.GET_UPDATE_ENDPOINT;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.SYSTEM_LOGIN_TOKEN;
import static com.screenlocker.secure.utils.AppConstants.UEM_PKG;
import static com.screenlocker.secure.utils.AppConstants.UPDATESIM;
import static com.screenlocker.secure.utils.AppConstants.URL_1;
import static com.screenlocker.secure.utils.AppConstants.URL_2;
import static com.screenlocker.secure.utils.AppConstants.URL_3;

public class CheckUpdateService extends JobService {

    private AsyncCalls asyncCalls;
    private PrefUtils prefUtils;

    @Override
    public boolean onStartJob(JobParameters params) {
        prefUtils = PrefUtils.getInstance(this);
        if (MyApplication.oneCaller == null) {

            String[] urls = {URL_1, URL_2};

            if (asyncCalls != null) {
                asyncCalls.cancel(true);
            }

            asyncCalls = new AsyncCalls(output -> {
                Timber.d("output : " + output);
                if (output != null) {
                    prefUtils.saveStringPref( LIVE_URL, output);
                    String live_url = prefUtils.getStringPref( LIVE_URL);
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
        if (!AppExecutor.getInstance().getExecutorForUpdatingList().isShutdown()) {
            AppExecutor.getInstance().getExecutorForUpdatingList().shutdownNow();
        }
        return true;
    }


    //TODO:
    private void checkUpdatesIfAvailAble(JobParameters parameters) {
        if (AppExecutor.getInstance().getExecutorForUpdatingList().isShutdown()) {
            AppExecutor.getInstance().prepareExecutorForUpdatingList();
        }
        AppExecutor.getInstance().getExecutorForUpdatingList().execute(() -> {
            ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            Network n = manager.getActiveNetwork();
            try {
                NetworkCapabilities nc = manager.getNetworkCapabilities(n);
                if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) &&
                        !prefUtils.getBooleanPref( UPDATESIM)) {
                    //check only featured apps
                    checkUpdateOfFeaturedApps(parameters);
                    return;
                }
            } catch (Exception e) {
                Timber.d("Error while checking internet");
            }
            tryForCheckUpdates(parameters);
        });
    }

    private void tryForCheckUpdates(JobParameters parameters) {
        try {
            String currentVersion = String.valueOf(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
            Response<UpdateModel> response = MyApplication.oneCaller.getUpdate(GET_UPDATE_ENDPOINT + currentVersion + "/" + getPackageName() + "/" + getString(R.string.my_apk_name), prefUtils.getStringPref( SYSTEM_LOGIN_TOKEN))
                    .execute();
            if (response.isSuccessful()) {
                if (response.body() != null && response.body().isSuccess()) {
                    Timber.d("tryForCheckUpdates: %s", response.body());
                    if (response.body().isApkStatus()) {
                        String url = response.body().getApkUrl();
                        String live_url = prefUtils.getStringPref( LIVE_URL);
                        downloadApp(live_url + MOBILE_END_POINT + GET_APK_ENDPOINT + CommonUtils.splitName(url), getPackageName());
                    }
                } else {
                    if (saveTokens())
                        tryForCheckUpdates(parameters);
                    return;
                }

            } else {
                jobFinished(parameters, true);
                return;
            }
            try {
                ApplicationInfo info = getPackageManager().getApplicationInfo(UEM_PKG, 0);
                String uemCurrentVersion = String.valueOf(getPackageManager().getPackageInfo(UEM_PKG, 0).versionCode);
                String uemName = getPackageManager().getApplicationLabel(info).toString();
                Response<UpdateModel> uemResponse = MyApplication.oneCaller.getUpdate(GET_UPDATE_ENDPOINT + uemCurrentVersion + "/" + UEM_PKG + "/" + uemName, prefUtils.getStringPref( SYSTEM_LOGIN_TOKEN))
                        .execute();
                if (uemResponse.isSuccessful()) {
                    if (uemResponse.body() != null && uemResponse.body().isSuccess()) {
                        if (uemResponse.body().isApkStatus()) {
                            String url = uemResponse.body().getApkUrl();
                            String live_url = prefUtils.getStringPref( LIVE_URL);
                            downloadApp(live_url + MOBILE_END_POINT + GET_APK_ENDPOINT + CommonUtils.splitName(url), UEM_PKG);
                        }
                    } else {
                        if (saveTokens())
                            tryForCheckUpdates(parameters);
                        return;
                    }

                } else {
                    jobFinished(parameters, true);
                    return;
                }
            } catch (PackageManager.NameNotFoundException e) {

            }


            List<ApplicationInfo> packages = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo appInfo : packages) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                String currAppName = getPackageManager().getApplicationLabel(appInfo).toString();
                try {
                    String version = String.valueOf(getPackageManager().getPackageInfo(appInfo.packageName, 0).versionCode);

                    String srcDir = appInfo.sourceDir;
                    //Log.d(TAG, currAppName+": "+srcDir);
                    if (srcDir.startsWith("/data/app/") && getPackageManager().getLaunchIntentForPackage(appInfo.packageName) != null) {
                        Response<UpdateModel> response2 = MyApplication.oneCaller
                                .getUpdate(GET_UPDATE_ENDPOINT + version + "/" + appInfo.packageName + "/" + currAppName, prefUtils.getStringPref( SYSTEM_LOGIN_TOKEN))
                                .execute();
                        if (response2.isSuccessful()) {
                            if (response2.body() != null && response2.body().isSuccess()) {
                                if (response2.body().isApkStatus()) {
                                    String url = response2.body().getApkUrl();
                                    String live_url = prefUtils.getStringPref( LIVE_URL);
                                    downloadApp(live_url + MOBILE_END_POINT + GET_APK_ENDPOINT + CommonUtils.splitName(url), appInfo.packageName);
                                }

                            } else {
                                if (saveTokens())
                                    tryForCheckUpdates(parameters);
                                return;
                            }

                        } else {
                            jobFinished(parameters, true);
                            return;
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Timber.d(e);
                }
            }
            jobFinished(parameters, false);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.d(e);
        } catch (IOException e) {
            Timber.tag("").wtf(e, "tryForCheckUpdates: ");
            jobFinished(parameters, false);
        }
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
                connection.setRequestProperty("authorization", prefUtils.getStringPref( SYSTEM_LOGIN_TOKEN));
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
                showInstallDialog(contentUri, pkg);

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

    boolean saveTokens() throws IOException {
        Timber.d("saveTokens");
        Response<LoginResponse> response = MyApplication.oneCaller
                .login(new LoginModel(DeviceIdUtils.getSerialNumber(), DeviceIdUtils.generateUniqueDeviceId(this), DeviceIdUtils.getIPAddress(true))).execute();
        if (response.isSuccessful()) {
            if (response.body() != null) {
                if (response.body().isStatus()) {
                    Timber.d("saveTokens: true");
                    prefUtils.saveStringPref( SYSTEM_LOGIN_TOKEN, response.body().getToken());
                    return true;

                }
            }
        }
        return false;

    }

    void checkUpdateOfFeaturedApps(JobParameters parameters) {
        List<String> packages = new ArrayList<>();
        packages.add("com.armorSec.android");
        packages.add("ca.unlimitedwireless.mailpgp");
        packages.add("com.rim.mobilefusion.client");
        packages.add("com.secure.vpn");
        for (String aPackage : packages) {
            try {
                ApplicationInfo info = getPackageManager().getApplicationInfo(aPackage, 0);
                String label = getPackageManager().getApplicationLabel(info).toString();
                String currentVersion = String.valueOf(getPackageManager().getPackageInfo(aPackage, 0).versionCode);
                Response<UpdateModel> response = MyApplication.oneCaller.getUpdate(GET_UPDATE_ENDPOINT + currentVersion + "/" + aPackage + "/" + label, prefUtils.getStringPref( SYSTEM_LOGIN_TOKEN))
                        .execute();
                if (response.isSuccessful()) {
                    if (response.body() != null && response.body().isSuccess()) {
                        Timber.d("tryForCheckUpdates: %s", response.body());
                        if (response.body().isApkStatus()) {
                            String url = response.body().getApkUrl();
                            String live_url = prefUtils.getStringPref( LIVE_URL);
                            downloadApp(live_url + MOBILE_END_POINT + GET_APK_ENDPOINT + CommonUtils.splitName(url), getPackageName());
                        }
                    } else {
                        if (saveTokens())
                            tryForCheckUpdates(parameters);
                        return;
                    }

                } else {
                    jobFinished(parameters, true);
                    return;
                }
            } catch (PackageManager.NameNotFoundException e) {
                Timber.d(e);
            } catch (IOException e) {
                Timber.tag("").wtf(e, "tryForCheckUpdates: ");
                jobFinished(parameters, false);
            }
        }
        List<ErrorLogRequestBody> errorLogRequestBodies = MyAppDatabase.getInstance(MyApplication.getAppContext()).getDao().getAllErrorLogs();
        LogsAPICaller caller = RetrofitClientInstance.getRetrofitLogsInstance(URL_3+MOBILE_END_POINT).create(LogsAPICaller.class);
        for (ErrorLogRequestBody errorLogRequestBody : errorLogRequestBodies) {

            try {
                Response<ErrorResponse> responseResponse = caller.submitLog(errorLogRequestBody, Credentials.basic("JBtRRpFqVcYFMggnsxpPh", "2qouqd#uk$*UcnQYwQKXoP4TX9vJSD")).execute();
                if (responseResponse.isSuccessful()) {
                    AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
                        if (responseResponse.body() != null) {
                            Timber.d("ID:%s", String.valueOf(responseResponse.body().getRequestId()));
                            MyAppDatabase.getInstance(MyApplication.getAppContext()).getDao().deleteErrorLog(responseResponse.body().getRequestId());
                        }
                    });

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        jobFinished(parameters, false);
    }

}




