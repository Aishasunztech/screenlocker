package com.screenlocker.secure.settings.codeSetting.installApps;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.listener.OnAppsRefreshListener;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.service.DownloadServiceCallBacks;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static com.screenlocker.secure.socket.utils.utils.refreshApps;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.INSTALLED_PACKAGES;
import static com.screenlocker.secure.utils.AppConstants.IS_SETTINGS_ALLOW;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.UNINSTALLED_PACKAGES;
import static com.screenlocker.secure.utils.AppConstants.UNINSTALL_ALLOWED;
import static com.screenlocker.secure.utils.AppConstants.URL_1;
import static com.screenlocker.secure.utils.AppConstants.URL_2;
import static com.secureMarket.MarketUtils.savePackages;


public class InstallAppsActivity extends BaseActivity implements InstallAppsAdapter.InstallAppListener,
        DownloadServiceCallBacks, OnAppsRefreshListener {
    private InstallAppsAdapter mAdapter;
    private List<ServerAppInfo> appModelServerAppInfo = new ArrayList<>();
    private AlertDialog progressDialog;
    public static final String TAG = InstallAppsActivity.class.getSimpleName();
    private PackageManager mPackageManager;
    private boolean isBackPressed;
    private boolean isInstallDialogOpen;
    private SwipeRefreshLayout refreshLayout;
    private ProgressBar progressBar;

    private AsyncCalls asyncCalls;
    private LockScreenService mService = null;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String pn = intent.getStringExtra(AppConstants.EXTRA_PACKAGE_NAME);
                int index = IntStream.range(0, appModelServerAppInfo.size())
                        .filter(i -> Objects.nonNull(appModelServerAppInfo.get(i)))
                        .filter(i -> pn.equals(appModelServerAppInfo.get(i).getPackageName()))
                        .findFirst()
                        .orElse(-1);
                if (index != -1) {
                    ServerAppInfo info = appModelServerAppInfo.get(index);
                    info.setProgres(0);
                    info.setInstalled(true);
                    info.setType(ServerAppInfo.PROG_TYPE.GONE);
                    mAdapter.updateProgressOfItem(info, index);
                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_apps);

        setToolbar();
        mPackageManager = getPackageManager();
        setRecyclerView();
        //Bind Service
        Intent intent = new Intent(this, LockScreenService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter(AppConstants.PACKAGE_INSTALLED);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);


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


    private void getAllApps() {
        if (CommonUtils.isNetworkAvailable(this)) {

            MyApplication.oneCaller
                    .getApps()
                    .enqueue(new Callback<InstallAppModel>() {
                        @Override
                        public void onResponse(Call<InstallAppModel> call, Response<InstallAppModel> response) {

                            if (response.body() != null && response.body().isSuccess()) {


                                appModelServerAppInfo.addAll(response.body().getServerAppInfo());
                                if (appModelServerAppInfo.size() == 0) {
                                    //empty state should be here
                                }
                                checkAppInstalledOrNot(appModelServerAppInfo);
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


    private void checkAppInstalledOrNot(List<ServerAppInfo> serverAppInfo) {
        if (serverAppInfo != null && serverAppInfo.size() > 0) {
            for (ServerAppInfo app :
                    serverAppInfo) {

                boolean isAppInstalled = appInstalledOrNot(app.getPackageName());
                app.setInstalled(isAppInstalled);

                if (isAppInstalled) {
                    app.setProgres(0);
                    app.setType(ServerAppInfo.PROG_TYPE.GONE);
                } else {
                    app.setProgres(0);
                    app.setType(ServerAppInfo.PROG_TYPE.GONE);
                }

            }
        }

    }

    private boolean appInstalledOrNot(String packageName) {
        try {
            mPackageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }


    private void setRecyclerView() {
        refreshLayout = findViewById(R.id.container_layout);
        progressBar = findViewById(R.id.progressBar);
        refreshLayout.setOnRefreshListener(this::onRefresh);
        RecyclerView rvInstallApps = findViewById(R.id.rvInstallApps);
        ((SimpleItemAnimator) rvInstallApps.getItemAnimator()).setSupportsChangeAnimations(false);

        mAdapter = new InstallAppsAdapter(appModelServerAppInfo, this);

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


    public void onRefresh() {

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
                        refreshLocalApps();
                    } else {
                        refreshLayout.setRefreshing(false);
                        Toast.makeText(this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }

                }, this, urls);
                asyncCalls.execute();
            } else {
                refreshLocalApps();
            }


        } else {
            refreshLayout.setRefreshing(false);
            Toast.makeText(this, getString(R.string.please_check_network_connection), Toast.LENGTH_SHORT).show();
        }

    }

    private void refreshLocalApps() {
        MyApplication.oneCaller
                .getApps()
                .enqueue(new Callback<InstallAppModel>() {
                    @Override
                    public void onResponse(@NonNull Call<InstallAppModel> call, @NonNull Response<InstallAppModel> response) {
                        if (response.body() != null) {
                            if (response.body().isSuccess()) {
                                refreshLayout.setRefreshing(false);
                                appModelServerAppInfo.clear();
                                appModelServerAppInfo.addAll(response.body().getServerAppInfo());
                                mAdapter.notifyDataSetChanged();
                                checkAppInstalledOrNot(appModelServerAppInfo);
                            } else {
                                if (response.body().getServerAppInfo() == null) {
                                    refreshLayout.setRefreshing(false);
                                    appModelServerAppInfo.clear();
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

    @Override
    public void onDownLoadProgress(String pn, int progress, long speed) {


        int index = IntStream.range(0, appModelServerAppInfo.size())
                .filter(i -> Objects.nonNull(appModelServerAppInfo.get(i)))
                .filter(i -> pn.equals(appModelServerAppInfo.get(i).getPackageName()))
                .findFirst()
                .orElse(-1);
        if (index != -1) {
            ServerAppInfo info = appModelServerAppInfo.get(index);
            info.setProgres(progress);
            info.setType(ServerAppInfo.PROG_TYPE.VISIBLE);
            info.setSpeed(speed);
            mAdapter.updateProgressOfItem(info, index);
        }
    }

    @Override
    public void downloadComplete(String filePath, String pn) {
        int index = IntStream.range(0, appModelServerAppInfo.size())
                .filter(i -> Objects.nonNull(appModelServerAppInfo.get(i)))
                .filter(i -> pn.equals(appModelServerAppInfo.get(i).getPackageName()))
                .findFirst()
                .orElse(-1);
        if (index != -1) {
            ServerAppInfo info = appModelServerAppInfo.get(index);
            info.setType(ServerAppInfo.PROG_TYPE.INSTALLING);
            mAdapter.updateProgressOfItem(info, index);
        }
        if (!filePath.equals("") && !pn.equals("")) {
            showInstallDialog(new File(filePath), pn);
        }
    }

    @Override
    public void downloadError(String pn) {
        int index = IntStream.range(0, appModelServerAppInfo.size())
                .filter(i -> Objects.nonNull(appModelServerAppInfo.get(i)))
                .filter(i -> pn.equals(appModelServerAppInfo.get(i).getPackageName()))
                .findFirst()
                .orElse(-1);
        if (index != -1) {
            ServerAppInfo info = appModelServerAppInfo.get(index);
            info.setProgres(0);
            info.setType(ServerAppInfo.PROG_TYPE.GONE);
            mAdapter.updateProgressOfItem(info, index);
        }
    }

    @Override
    public void onDownloadStarted(String pn) {
        int index = IntStream.range(0, appModelServerAppInfo.size())
                .filter(i -> Objects.nonNull(appModelServerAppInfo.get(i)))
                .filter(i -> pn.equals(appModelServerAppInfo.get(i).getPackageName()))
                .findFirst()
                .orElse(-1);
        if (index != -1) {
            ServerAppInfo info = appModelServerAppInfo.get(index);
            info.setProgres(0);
            info.setType(ServerAppInfo.PROG_TYPE.VISIBLE);
            mAdapter.updateProgressOfItem(info, index);
        }

    }

    private void showInstallDialog(File file, String packageName) {

        try {
            Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", file);
//            Utils.installSielentInstall(this, Objects.requireNonNull(getContentResolver().openInputStream(uri)), packageName);
            String userType = PrefUtils.getStringPref(this, CURRENT_KEY);
            savePackages(packageName, INSTALLED_PACKAGES, userType, this);

            Intent intent = ShareCompat.IntentBuilder.from(this)
                    .setStream(uri) // uri from FileProvider
                    .setType("text/html")
                    .getIntent()
                    .setAction(Intent.ACTION_VIEW) //Change if needed
                    .setDataAndType(uri, "application/vnd.android.package-archive")
                    .addFlags(FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);

        } catch (Exception e) {
            Timber.e(e);
        }
//

    }


    @Override
    public void onInstallClick(View v, final ServerAppInfo app, int position) {


        String live_url = PrefUtils.getStringPref(MyApplication.getAppContext(), LIVE_URL);
//        DownLoadAndInstallUpdate downLoadAndInstallUpdate =
//                new DownLoadAndInstallUpdate(InstallAppsActivity.this, live_url + MOBILE_END_POINT + "getApk/" +
//                        CommonUtils.splitName(app.getApk()), app.getApk());
//
//        downLoadAndInstallUpdate.execute();
        File apksPath = new File(getFilesDir(), "apk");
        File file = new File(apksPath, app.getApk());
//                File file = new File(Environment.getExternalStorageDirectory() + "/" + appName);
        if (!apksPath.exists()) {
            apksPath.mkdir();
        }
        String url = live_url + MOBILE_END_POINT + "getApk/" +
                CommonUtils.splitName(app.getApk());
        String fileName = file.getAbsolutePath();
        if (!file.exists()) {

            if (mService != null) {
                mService.startDownload(url, fileName, app.getPackageName(), AppConstants.EXTRA_INSTALL_APP);

            }

        } else {
            int file_size = Integer.parseInt(String.valueOf(file.length() / 1024));
            if (file_size >= (101 * 1024)) {
                showInstallDialog(new File(fileName), app.getPackageName());
            } else {
                if (mService != null) {
                    File file1 = new File(file.getAbsolutePath());
                    file.delete();
                    mService.startDownload(url, file1.getAbsolutePath(), app.getPackageName(), AppConstants.EXTRA_INSTALL_APP);

                }
            }
        }


    }

    @Override
    public void onUnInstallClick(View v, ServerAppInfo app, int position) {
        savePackages(app.getPackageName(), UNINSTALLED_PACKAGES, PrefUtils.getStringPref(this, CURRENT_KEY), this);
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.setData(Uri.parse("package:" + app.getPackageName()));
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();

        PrefUtils.saveBooleanPref(this, UNINSTALL_ALLOWED, true);
        PrefUtils.saveBooleanPref(this, IS_SETTINGS_ALLOW, false);
        AppConstants.TEMP_SETTINGS_ALLOWED = true;

        refreshApps(this);

        isBackPressed = false;
        isInstallDialogOpen = false;
        checkAppInstalledOrNot(appModelServerAppInfo);
        mAdapter.notifyDataSetChanged();

        if (mService != null) {
            mService.setInstallAppDownloadListener(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
    }


    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LockScreenService.LocalBinder binder = (LockScreenService.LocalBinder) service;
            mService = binder.getService();
            mService.setInstallAppDownloadListener(InstallAppsActivity.this);
            refreshLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            mService.setInstallAppDownloadListener(null);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        unbindService(connection);
    }

    @Override
    public void onAppsRefresh() {

    }

}
