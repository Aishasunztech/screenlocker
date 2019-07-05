package com.screenlocker.secure.settings.codeSetting.installApps;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.settings.codeSetting.CodeSettingActivity;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.LifecycleReceiver;
import com.screenlocker.secure.utils.PrefUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.URL_1;
import static com.screenlocker.secure.utils.AppConstants.URL_2;
import static com.screenlocker.secure.utils.LifecycleReceiver.BACKGROUND;
import static com.screenlocker.secure.utils.LifecycleReceiver.LIFECYCLE_ACTION;
import static com.screenlocker.secure.utils.LifecycleReceiver.STATE;


public class InstallAppsActivity extends BaseActivity implements View.OnClickListener, InstallAppsAdapter.InstallAppListener {
    private RecyclerView rvInstallApps;
    private TextView tvProgressText;
    private InstallAppsAdapter mAdapter;
    private List<com.screenlocker.secure.settings.codeSetting.installApps.List> appModelList;
    private AlertDialog progressDialog;
    private ProgressBar mProgressBar;
    public static final String TAG = InstallAppsActivity.class.getSimpleName();
    private PackageManager mPackageManager;
    private boolean isBackPressed;
    private boolean isInstallDialogOpen;
    private ConstraintLayout containerLayout;

    private AsyncCalls asyncCalls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_apps);
        setToolbar();
        mPackageManager = getPackageManager();
        setRecyclerView();
        createProgressDialog();
        findViewById(R.id.fabRefresh).setBackgroundColor(getResources().getColor(R.color.seekbarColor));
        findViewById(R.id.fabRefresh).setOnClickListener(this);

        if (MyApplication.oneCaller == null) {
            String[] urls = {URL_1, URL_2};

            if (asyncCalls != null) {
                asyncCalls.cancel(true);
            }

            asyncCalls = new AsyncCalls(output -> {

                if (output != null) {
                    PrefUtils.saveStringPref(this, LIVE_URL, output);
                    String live_url = PrefUtils.getStringPref(MyApplication.getAppContext(), LIVE_URL);
                    Timber.d("live_url %s", live_url);
                    MyApplication.oneCaller = RetrofitClientInstance.getRetrofitInstance(live_url + MOBILE_END_POINT).create(ApiOneCaller.class);
                    getAllApps();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                }

            }, this, urls);
            asyncCalls.execute();

        } else {
            getAllApps();

        }

    }

    private void setToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.installed_apps_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public void onStateChange(int state) {            //<---
        switch (state) {
            case LifecycleReceiver.FOREGROUND:
                Timber.e("onStateChange: FOREGROUND");
                break;

            case LifecycleReceiver.BACKGROUND:
                Timber.e("onStateChange: BACKGROUND");
                if (CodeSettingActivity.codeSettingsInstance != null) {
                    CodeSettingActivity.codeSettingsInstance.finish();
                    this.finish();
                }
                break;

            default:
                Timber.e("onStateChange: SOMETHING");
                break;
        }
    }

    private void getAllApps() {
        if (CommonUtils.isNetworkAvailable(this)) {

            MyApplication.oneCaller
                    .getApps()
                    .enqueue(new Callback<InstallAppModel>() {
                        @Override
                        public void onResponse(Call<InstallAppModel> call, Response<InstallAppModel> response) {

                            if (response.body() != null && response.body().isSuccess()) {


                                appModelList.addAll(response.body().getList());
                                if (appModelList.size() == 0) {

                                }
                                checkAppInstalledOrNot(appModelList);
                                mAdapter.notifyDataSetChanged();
                            }

                        }

                        @Override
                        public void onFailure(Call<InstallAppModel> call, Throwable t) {
                            Toast.makeText(InstallAppsActivity.this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                        }
                    });


        } else {
            Toast.makeText(this, getString(R.string.please_check_network_connection), Toast.LENGTH_SHORT).show();
        }

    }


    private void checkAppInstalledOrNot(List<com.screenlocker.secure.settings.codeSetting.installApps.List> list) {
        Log.d("CheckAppInstalledOrNot", "called");
        if (list != null && list.size() > 0) {
            for (com.screenlocker.secure.settings.codeSetting.installApps.List app :
                    list) {
                String fileName = app.getApk();
//                File file = getActivity().getFileStreamPath(fileName);
                File apksPath = new File(getFilesDir(), "apk");
                File file = new File(apksPath, fileName);
                if (file.exists()) {
                    Log.d("FileExists", "Yes");
                    String appPackageName = getAppLabel(mPackageManager, file.getAbsolutePath());
                    if (appPackageName != null)
                        app.setInstalled(appInstalledOrNot(appPackageName));
                }
            }
        }

    }

    private boolean appInstalledOrNot(String uri) {
        try {
            mPackageManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void createProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false); // if you want user to wait for some process to finish,
        LayoutInflater factory = LayoutInflater.from(this);
        final View yourInflatedView = factory.inflate(R.layout.layout_loading_dialog, null);

        builder.setView(yourInflatedView);

        progressDialog = builder.create();

        mProgressBar = yourInflatedView.findViewById(R.id.progressBar);

        tvProgressText = yourInflatedView.findViewById(R.id.tvProgressText);


    }

    private void setRecyclerView() {
        containerLayout = findViewById(R.id.container_layout);
        appModelList = new ArrayList<>();
        rvInstallApps = findViewById(R.id.rvInstallApps);

        mAdapter = new InstallAppsAdapter(appModelList, this);

        rvInstallApps.setLayoutManager(new LinearLayoutManager(this));
        rvInstallApps.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        rvInstallApps.setAdapter(mAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.fabRefresh) {

            if (CommonUtils.isNetworkAvailable(this)) {


                if (MyApplication.oneCaller == null) {
                    String[] urls = {URL_1, URL_2};

                    if (asyncCalls != null) {
                        asyncCalls.cancel(true);
                    }

                    asyncCalls = new AsyncCalls(output -> {

                        if (output != null) {
                            PrefUtils.saveStringPref(this, LIVE_URL, output);
                            String live_url = PrefUtils.getStringPref(MyApplication.getAppContext(), LIVE_URL);
                            Timber.d("live_url %s", live_url);
                            MyApplication.oneCaller = RetrofitClientInstance.getRetrofitInstance(live_url + MOBILE_END_POINT).create(ApiOneCaller.class);
                            refreshApps();
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                        }

                    }, this, urls);
                    asyncCalls.execute();
                } else {
                    refreshApps();
                }


            } else {
                Toast.makeText(this, getString(R.string.please_check_network_connection), Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void refreshApps() {
        MyApplication.oneCaller
                .getApps()
                .enqueue(new Callback<InstallAppModel>() {
                    @Override
                    public void onResponse(@NonNull Call<InstallAppModel> call, @NonNull Response<InstallAppModel> response) {
                        if (response.body() != null) {
                            if (response.body().isSuccess()) {
                                appModelList.clear();
                                appModelList.addAll(response.body().getList());
                                mAdapter.notifyDataSetChanged();
                                checkAppInstalledOrNot(appModelList);
                            } else {
                                if (response.body().getList() == null) {
                                    appModelList.clear();
                                    mAdapter.notifyDataSetChanged();
                                }


                            }

                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<InstallAppModel> call, @NonNull Throwable t) {

                        Toast.makeText(InstallAppsActivity.this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static class DownLoadAndInstallUpdate extends AsyncTask<Void, Integer, Uri> {
        private String appName, url;
        private WeakReference<Context> contextWeakReference;
        private ProgressDialog dialog;

        DownLoadAndInstallUpdate(Context context, final String url, String appName) {
            contextWeakReference = new WeakReference<>(context);
            this.url = url;
            this.appName = appName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(contextWeakReference.get());
            dialog.setTitle(contextWeakReference.get().getResources().getString(R.string.downloading_update));
            dialog.setCancelable(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.show();
        }

        @Override
        protected Uri doInBackground(Void... voids) {
            return downloadApp();
        }


        private Uri downloadApp() {
            FileOutputStream fileOutputStream = null;
            InputStream input = null;
            try {

                File apksPath = new File(contextWeakReference.get().getFilesDir(), "apk");
                File file = new File(apksPath, appName);
//                File file = new File(Environment.getExternalStorageDirectory() + "/" + appName);
                if (!apksPath.exists()) {
                    apksPath.mkdir();
                }

                if (file.exists())
                    return FileProvider.getUriForFile(contextWeakReference.get(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
                try {
                    fileOutputStream = new FileOutputStream(file);
                    URL downloadUrl = new URL(url);
                    URLConnection connection = downloadUrl.openConnection();
                    int contentLength = connection.getContentLength();

                    // input = body.byteStream();
                    input = new BufferedInputStream(downloadUrl.openStream());
                    byte data[] = new byte[contentLength];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        publishProgress((int) ((total * 100) / contentLength));
                        fileOutputStream.write(data, 0, count);
                    }

                    return FileProvider.getUriForFile(contextWeakReference.get(), BuildConfig.APPLICATION_ID + ".fileprovider", file);

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    if (fileOutputStream != null) {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                    if (input != null)
                        input.close();
                    file.setReadable(true, false);


                }
            } catch (Exception e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            dialog.setProgress(values[0]);
//            tvProgressText.setText(String.valueOf(values[0]));
        }

        @Override
        protected void onPostExecute(Uri uri) {
            super.onPostExecute(uri);
            try {
                if (dialog != null)
                    dialog.dismiss();
                if (uri != null) {
                    showInstallDialog(uri);
                }
            } catch (Exception e) {
                Timber.d(e);
            }


        }

        private void showInstallDialog(Uri uri) {
            /*try {
                installPackage(appName);
            } catch (IOException e) {
                Log.d("dddddgffdgg", "showInstallDialog: "+e.getMessage());;
            }*/
            Intent intent = ShareCompat.IntentBuilder.from((Activity) contextWeakReference.get())
                    .setStream(uri) // uri from FileProvider
                    .setType("text/html")
                    .getIntent()
                    .setAction(Intent.ACTION_VIEW) //Change if needed
                    .setDataAndType(uri, "application/vnd.android.package-archive")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            contextWeakReference.get().startActivity(intent);


        }
    }

    public String getAppLabel(PackageManager pm, String pathToApk) {
        PackageInfo packageInfo = pm.getPackageArchiveInfo(pathToApk, 0);
        if (packageInfo != null) {

            if (Build.VERSION.SDK_INT >= 8) {
                // those two lines do the magic:
                packageInfo.applicationInfo.sourceDir = pathToApk;
                packageInfo.applicationInfo.publicSourceDir = pathToApk;
            }

            CharSequence label = pm.getApplicationLabel(packageInfo.applicationInfo);
            Timber.e("getAppLabel: package name is " + packageInfo.packageName);
            return packageInfo.packageName;

        } else {
            return null;
        }
    }

    @Override
    public void onInstallClick(View v, final com.screenlocker.secure.settings.codeSetting.installApps.List app, int position) {


        String live_url = PrefUtils.getStringPref(MyApplication.getAppContext(), LIVE_URL);

        DownLoadAndInstallUpdate downLoadAndInstallUpdate =
                new DownLoadAndInstallUpdate(InstallAppsActivity.this, live_url + MOBILE_END_POINT + "getApk/" +
                        CommonUtils.splitName(app.getApk()), app.getApk());

        downLoadAndInstallUpdate.execute();


    }

    @Override
    public void onUnInstallClick(View v, com.screenlocker.secure.settings.codeSetting.installApps.List app, int position) {
        String fileName = app.getApk();
        File dir = new File(getFilesDir(), "apk");

        File fileApk = new File(dir, fileName);
        if (fileApk.exists()) {
            Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
            intent.setData(Uri.parse("package:" + getAppLabel(mPackageManager, fileApk.getAbsolutePath())));

            startActivity(intent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        isBackPressed = false;
        isInstallDialogOpen = false;
        checkAppInstalledOrNot(appModelList);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isBackPressed && !isInstallDialogOpen) {
            try {
                containerLayout.setVisibility(View.INVISIBLE);
                this.finish();
                if (CodeSettingActivity.codeSettingsInstance != null) {

                    //  finish previous activity and this activity
                    CodeSettingActivity.codeSettingsInstance.finish();
                }
            } catch (Exception ignored) {
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!isBackPressed && !isInstallDialogOpen) {
            Intent intent = new Intent(LIFECYCLE_ACTION);
            intent.putExtra(STATE, BACKGROUND);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean installPackage(Context context, String abspath, String packageName)
            throws IOException {
        PackageInstaller packageInstaller = getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(packageName);
        // set params
        int sessionId = packageInstaller.createSession(params);
        PackageInstaller.Session session = packageInstaller.openSession(sessionId);
        InputStream in = new FileInputStream(abspath);
        OutputStream out = session.openWrite("com.nemo.vidmate", 0, -1);
        byte[] buffer = new byte[65536];
        int c;
        while ((c = in.read(buffer)) != -1) {
            out.write(buffer, 0, c);
        }
        session.fsync(out);
        in.close();
        out.close();

        session.commit(createIntentSender(context, sessionId));
        return true;
    }


    private IntentSender createIntentSender(Context context, int sessionId) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                new Intent(Intent.ACTION_MAIN),
                0);
        return pendingIntent.getIntentSender();
    }
}
