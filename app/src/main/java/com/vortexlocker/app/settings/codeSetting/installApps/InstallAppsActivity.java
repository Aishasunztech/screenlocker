package com.vortexlocker.app.settings.codeSetting.installApps;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vortexlocker.app.BuildConfig;
import com.vortexlocker.app.R;
import com.vortexlocker.app.app.MyApplication;
import com.vortexlocker.app.base.BaseActivity;
import com.vortexlocker.app.settings.codeSetting.CodeSettingActivity;
import com.vortexlocker.app.utils.AppConstants;
import com.vortexlocker.app.utils.CommonUtils;
import com.vortexlocker.app.utils.LifecycleReceiver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.vortexlocker.app.utils.LifecycleReceiver.BACKGROUND;
import static com.vortexlocker.app.utils.LifecycleReceiver.LIFECYCLE_ACTION;
import static com.vortexlocker.app.utils.LifecycleReceiver.STATE;
import static com.vortexlocker.app.utils.Utils.collapseNow;

public class InstallAppsActivity extends BaseActivity implements View.OnClickListener, InstallAppsAdapter.InstallAppListener {
    private RecyclerView rvInstallApps;
    private TextView tvProgressText;
    private InstallAppsAdapter mAdapter;
    private List<com.vortexlocker.app.settings.codeSetting.installApps.List> appModelList;
    private AlertDialog progressDialog;
    private ProgressBar mProgressBar;
    public static final String TAG = InstallAppsActivity.class.getSimpleName();
    private PackageManager mPackageManager;
    private boolean isBackPressed;
    private boolean isInstallDialogOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_apps);
        setToolbar();
        mPackageManager = getPackageManager();
        setRecyclerView();
        createProgressDialog();
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

    @Override
    protected void freezeStatusbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            collapseNow(this);
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


    private void checkAppInstalledOrNot(List<com.vortexlocker.app.settings.codeSetting.installApps.List> list) {
        if (list != null && list.size() > 0) {
            for (com.vortexlocker.app.settings.codeSetting.installApps.List app :
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


    @SuppressLint("StaticFieldLeak")
    private void downloadToDisk(/*final ResponseBody body, */final String appName, final String url) {
        new AsyncTask<Void, Integer, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                return downloadApp();
            }


            private Boolean downloadApp() {
                FileOutputStream fileOutputStream = null;
                InputStream input = null;
                try {
                    File file = getFileStreamPath(appName);

                    if (file.exists())
                        return true;
                    try {
                        fileOutputStream = openFileOutput(appName, MODE_PRIVATE);
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
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }
                return false;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                mProgressBar.setProgress(values[0]);
                tvProgressText.setText(String.valueOf(values[0]));
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean) {
                    showInstallDialog(appName);
                }
                if (progressDialog != null)
                    progressDialog.dismiss();
            }

            private void showInstallDialog(String appName) {
                File f = getFileStreamPath(appName);
                Uri apkUri = FileProvider.getUriForFile(InstallAppsActivity.this,
                        /*getApplicationContext().getPackageName() + ".provider"*/ BuildConfig.APPLICATION_ID, f);
                Timber.e("showInstallDialog:  app path " + getAppLabel(getPackageManager(), f.getAbsolutePath()));

                Intent intent = ShareCompat.IntentBuilder.from(InstallAppsActivity.this)
                        .setStream(apkUri) // uri from FileProvider
                        .setType("text/html")
                        .getIntent()
                        .setAction(Intent.ACTION_VIEW) //Change if needed
                        .setDataAndType(apkUri, "application/vnd.android.package-archive")
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
                isInstallDialogOpen = true;
            }


        }.execute();
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
            String packageName = packageInfo.packageName;
            return packageName;
        } else {
            return null;
        }
    }

    @Override
    public void onInstallClick(View v, final com.vortexlocker.app.settings.codeSetting.installApps.List app, int position) {
        downloadToDisk(app.getApk(), AppConstants.STAGING_BASE_URL + "/getApk/" + CommonUtils.splitName(app.getApk()));

    }

    @Override
    public void onUnInstallClick(View v, com.vortexlocker.app.settings.codeSetting.installApps.List app, int position) {
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
}
