package com.screenlocker.secure.settings.codeSetting.installApps;

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

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.settings.codeSetting.CodeSettingActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.LifecycleReceiver;

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
        getAllApps();

    }

    private void setToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Installed Apps");
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
            ((MyApplication) getApplicationContext())
                    .getApiOneCaller()
                    .getApps()
                    .enqueue(new Callback<InstallAppModel>() {
                        @Override
                        public void onResponse(Call<InstallAppModel> call, Response<InstallAppModel> response) {

                            if (response.body() != null && response.body().isSuccess()) {


                                appModelList.addAll(response.body().getList());
                                if (appModelList.size() == 0) {

                                }
                                mAdapter.notifyDataSetChanged();
                                checkAppInstalledOrNot(appModelList);
                            }

                        }

                        @Override
                        public void onFailure(Call<InstallAppModel> call, Throwable t) {
                            Toast.makeText(InstallAppsActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, getString(R.string.please_check_network_connection), Toast.LENGTH_SHORT).show();
        }

    }


    private void checkAppInstalledOrNot(List<com.screenlocker.secure.settings.codeSetting.installApps.List> list) {
        if (list != null && list.size() > 0) {
            for (com.screenlocker.secure.settings.codeSetting.installApps.List app :
                    list) {
                File file = getFileStreamPath(app.getApk());
                if (file.exists()) {
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
                ((MyApplication) getApplicationContext())
                        .getApiOneCaller()
                        .getApps()
                        .enqueue(new Callback<InstallAppModel>() {
                            @Override
                            public void onResponse(Call<InstallAppModel> call, Response<InstallAppModel> response) {
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
                            public void onFailure(Call<InstallAppModel> call, Throwable t) {

                            }
                        });
            } else {
                Toast.makeText(this, getString(R.string.please_check_network_connection), Toast.LENGTH_SHORT).show();
            }

        }
    }
    private static class DownLoadAndInstallUpdate extends AsyncTask<Void, Integer, Boolean> {
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
            dialog.setTitle("Downloading Update, Please Wait");
            dialog.setCancelable(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return downloadApp();
        }


        private Boolean downloadApp() {
            FileOutputStream fileOutputStream = null;
            InputStream input = null;
            try {
                appName = appName.substring(0,(appName.length() -4));
                File file = contextWeakReference.get().getFileStreamPath(appName);
//                File file = new File(Environment.getExternalStorageDirectory() + "/" + appName);

                if (file.exists())
                    return true;
                try {
                    fileOutputStream = contextWeakReference.get().openFileOutput(appName, MODE_PRIVATE);
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

                    return true;

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
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
            return false;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            dialog.setProgress(values[0]);
//            tvProgressText.setText(String.valueOf(values[0]));
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (dialog != null)
                dialog.dismiss();
            if (aBoolean) {
                showInstallDialog(appName);
            }

        }

        private void showInstallDialog(String appName) {
            File f = contextWeakReference.get().getFileStreamPath(appName);
            /*try {
                installPackage(appName);
            } catch (IOException e) {
                Log.d("dddddgffdgg", "showInstallDialog: "+e.getMessage());;
            }*/
            Uri apkUri = FileProvider.getUriForFile(contextWeakReference.get(), BuildConfig.APPLICATION_ID, f);


            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri,
                    "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


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

        String url = AppConstants.STAGING_BASE_URL + "/getApk/" +
                CommonUtils.splitName(app.getApk());
        Log.d("DownloadUrl",url);
        DownLoadAndInstallUpdate downLoadAndInstallUpdate =
                new DownLoadAndInstallUpdate(this,AppConstants.STAGING_BASE_URL + "/getApk/" +
                        CommonUtils.splitName(app.getApk()),app.getApk());
        downLoadAndInstallUpdate.execute();


    }

    @Override
    public void onUnInstallClick(View v, com.screenlocker.secure.settings.codeSetting.installApps.List app, int position) {
        File fileApk = getFileStreamPath(app.getApk());
        if (fileApk.exists()) {
            Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
            intent.setData(Uri.parse("package:" + getAppLabel(mPackageManager, fileApk.getAbsolutePath())));

            startActivity(intent);
        }
    }

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
