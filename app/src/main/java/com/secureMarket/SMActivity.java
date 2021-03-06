package com.secureMarket;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.secure.launcher.R;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.listener.OnAppsRefreshListener;
import com.screenlocker.secure.network.NetworkChangeReceiver;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.service.DownloadServiceCallBacks;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.settings.codeSetting.installApps.InstallAppModel;
import com.screenlocker.secure.settings.codeSetting.installApps.ServerAppInfo;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Utils;
import com.secureMarket.ui.home.InstalledAppsFragment;
import com.secureMarket.ui.home.MarketFragment;
import com.secureMarket.ui.home.Msgs;
import com.secureMarket.ui.home.SharedViwModel;
import com.secureMarket.ui.home.UpdateAppsFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.socket.utils.utils.refreshApps;
import static com.screenlocker.secure.utils.AppConstants.CONNECTED;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_NETWORK_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DOWNLAOD_HASH_MAP;
import static com.screenlocker.secure.utils.AppConstants.EXTRA_IS_PACKAGE_INSTALLED;
import static com.screenlocker.secure.utils.AppConstants.GET_APK_ENDPOINT;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.LIMITED;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.SECUREMARKETSIM;
import static com.screenlocker.secure.utils.AppConstants.SECUREMARKETWIFI;
import static com.screenlocker.secure.utils.AppConstants.UNINSTALL_ALLOWED;
import static com.screenlocker.secure.utils.AppConstants.URL_1;
import static com.screenlocker.secure.utils.AppConstants.URL_2;
import static com.screenlocker.secure.utils.CommonUtils.isNetworkConneted;
import static com.screenlocker.secure.utils.PrefUtils.PREF_FILE;

public class SMActivity extends BaseActivity implements DownloadServiceCallBacks, AppInstallUpdateListener, OnAppsRefreshListener {

    private static final String TAG = SMActivity.class.getSimpleName();
    private LockScreenService mService = null;
    private AsyncCalls asyncCalls;
    private List<ServerAppInfo> appInfos = new ArrayList<>();
    private List<ServerAppInfo> newApps = new ArrayList<>();
    private List<ServerAppInfo> updatesInfo = new ArrayList<>();
    private List<ServerAppInfo> installedInfo = new ArrayList<>();
    private SharedViwModel sharedViwModel;
    private MainMarketPagerAdapter sectionsPagerAdapter;
    private String currentSpace;

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            finish();
        }

    };

    //

    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Bind this activity to download service
        Intent intent = new Intent(this, LockScreenService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        //Register App installed receiver
        IntentFilter filter = new IntentFilter(AppConstants.PACKAGE_INSTALLED);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        //set content view
        setContentView(R.layout.activity_sm);
        //setup tool bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // setup view model

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(AppConstants.BROADCAST_ACTION));


        sectionsPagerAdapter = new MainMarketPagerAdapter(this, getSupportFragmentManager());

        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        sharedViwModel =new ViewModelProvider(this).get(SharedViwModel.class);


        registerNetworkPref();
        if (isNetworkConneted(prefUtils)) {
            Timber.d("onCreate: NetworkConneted");
            loadApps();
        } else {
            sharedViwModel.setMutableMsgs(Msgs.ERROR);
        }


    }

    private void loadApps() {
        String dealerId = prefUtils.getStringPref( AppConstants.KEY_DEVICE_LINKED);
        //Log.d("ConnectedDealer",dealerId);
        if (dealerId == null || dealerId.equals("")) {
            //   getAdminApps();
            Timber.d("loadApps: ");
            getServerApps(null);
        } else {
            getServerApps(dealerId);
            // getAllApps(dealerId);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sm, menu);
        MenuItem search = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) search.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if (sectionsPagerAdapter != null) {
                    MarketFragment fragment1 = sectionsPagerAdapter.getMarketFragment();
                    InstalledAppsFragment fragment2 = sectionsPagerAdapter.getInstalledAppsFragment();
                    UpdateAppsFragment fragment3 = sectionsPagerAdapter.getUpdateAppsFragment();
                    if (fragment1 != null) {
                        fragment1.searchApps(query);
                    }
                    if (fragment2 != null) {
                        fragment2.searchApps(query);
                    }
                    if (fragment3 != null) {
                        fragment3.searchApps(query);
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (sectionsPagerAdapter != null) {
                    MarketFragment fragment1 = sectionsPagerAdapter.getMarketFragment();
                    InstalledAppsFragment fragment2 = sectionsPagerAdapter.getInstalledAppsFragment();
                    UpdateAppsFragment fragment3 = sectionsPagerAdapter.getUpdateAppsFragment();
                    if (fragment1 != null) {
                        fragment1.searchApps(newText);
                    }
                    if (fragment2 != null) {
                        fragment2.searchApps(newText);
                    }
                    if (fragment3 != null) {
                        fragment3.searchApps(newText);
                    }
                    return true;
                }
                return false;
            }
        });
        return true;
    }


    //App install receiver
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String pn = intent.getStringExtra(AppConstants.EXTRA_PACKAGE_NAME);
                boolean isInstalled = intent.getBooleanExtra(EXTRA_IS_PACKAGE_INSTALLED, false);
                int index = IntStream.range(0, newApps.size())
                        .filter(i -> Objects.nonNull(newApps.get(i)))
                        .filter(i -> pn.equals(newApps.get(i).getPackageName()))
                        .findFirst()
                        .orElse(-1);
                if (index != -1) {
                    ServerAppInfo info = newApps.get(index);
                    if (sectionsPagerAdapter != null) {
                        MarketFragment fragment = sectionsPagerAdapter.getMarketFragment();
                        UpdateAppsFragment fragment1 = sectionsPagerAdapter.getUpdateAppsFragment();

                        InstalledAppsFragment fragment2 = sectionsPagerAdapter.getInstalledAppsFragment();
                        if (fragment != null) {
                            fragment.onInstallationComplete(info.getPackageName(), isInstalled);
                        }
                        if (fragment1 != null) {
                            fragment1.onInstallationComplete(info.getPackageName(), isInstalled);
                        }
                        if (fragment2 != null && isInstalled) {
                            info.setInstalled(true);
                            info.setUpdate(false);
                            info.setType(ServerAppInfo.PROG_TYPE.GONE);
                            fragment2.addPackageToList(info);
                        }
                    }
                }
            }

        }
    };

    @Override
    public void onDownLoadProgress(String pn, int progress, long speed, String requestId, String space) {

        if (sectionsPagerAdapter != null) {
            MarketFragment fragment = sectionsPagerAdapter.getMarketFragment();
            UpdateAppsFragment fragment1 = sectionsPagerAdapter.getUpdateAppsFragment();

            if (fragment != null) {
                fragment.onDownLoadProgress(pn, progress, requestId, speed);
            }
            if (fragment1 != null) {
                fragment1.onDownLoadProgress(pn, progress, requestId, speed);
            }

        }

//        int index = IntStream.range(0, unInstalledApps.size())
//                .filter(i -> Objects.nonNull(unInstalledApps.get(i)))
//                .filter(i -> pn.equals(unInstalledApps.get(i).getPackageName()))
//                .findFirst()
//                .orElse(-1);
//        if (index != -1) {
//            ServerAppInfo info = unInstalledApps.get(index);
//            info.setProgres(progress);
//            info.setType(ServerAppInfo.PROG_TYPE.VISIBLE);
//            info.setSpeed(speed);
//            uninstalledAdapter.updateProgressOfItem(info, index);
//        }

    }

    @Override
    public void downloadComplete(String filePath, String pn, String space) {

        if (sectionsPagerAdapter != null) {
            MarketFragment fragment = sectionsPagerAdapter.getMarketFragment();
            if (fragment != null) {
                fragment.downloadComplete(filePath, pn);
            }
            UpdateAppsFragment fragment1 = sectionsPagerAdapter.getUpdateAppsFragment();
            if (fragment1 != null) {
                fragment1.downloadComplete(filePath, pn);
            }
        }
        if (!filePath.equals("") && !pn.equals("")) {
            showInstallDialog(new File(filePath), pn, space);
        }

    }

    @Override
    public void downloadError(String pn) {
        if (sectionsPagerAdapter != null) {
            MarketFragment fragment = sectionsPagerAdapter.getMarketFragment();
            if (fragment != null) {
                fragment.downloadError(pn);
            }
            UpdateAppsFragment fragment1 = sectionsPagerAdapter.getUpdateAppsFragment();
            if (fragment1 != null) {
                fragment1.downloadError(pn);
            }
        }

    }

    @Override
    public void onDownloadStarted(String pn) {
        if (sectionsPagerAdapter != null) {
            MarketFragment fragment = sectionsPagerAdapter.getMarketFragment();
            if (fragment != null) {
                fragment.onDownloadStarted(pn);
            }
            UpdateAppsFragment fragment1 = sectionsPagerAdapter.getUpdateAppsFragment();
            if (fragment1 != null) {
                fragment1.onDownloadStarted(pn);
            }
        }
    }

    @Override
    public void onDownloadCancelled(String packageName) {
        if (sectionsPagerAdapter != null) {
            MarketFragment fragment = sectionsPagerAdapter.getMarketFragment();
            if (fragment != null) {
                fragment.onDownloadCancelled(packageName);
            }
            UpdateAppsFragment fragment1 = sectionsPagerAdapter.getUpdateAppsFragment();
            if (fragment1 != null) {
                fragment1.onDownloadCancelled(packageName);
            }
        }
    }

    //connection to download service
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LockScreenService.LocalBinder binder = (LockScreenService.LocalBinder) service;
            mService = binder.getService();
            mService.setMarketDownloadListener(SMActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };


    private void getServerApps(String dealerId) {
        sharedViwModel.setMutableMsgs(Msgs.LOADING);
        if (MyApplication.oneCaller == null) {
            if (asyncCalls != null) {
                asyncCalls.cancel(true);
            }
            String[] urls = {URL_1, URL_2};
            asyncCalls = new AsyncCalls(output -> {
                if (output == null) {
                    Toast.makeText(this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                } else {
                    prefUtils.saveStringPref( LIVE_URL, output);
                    String live_url = prefUtils.getStringPref( LIVE_URL);
                    Timber.d("live_url %s", live_url);
                    MyApplication.oneCaller = RetrofitClientInstance.getRetrofitInstance(live_url + MOBILE_END_POINT).create(ApiOneCaller.class);

//                    if (dealerId == null) {
//                        getAdminApps();
//                    } else {
                    getAllApps(dealerId);
//                    }
                }
            }, this, urls);

        } else {
            Timber.d("instant is not null");

//            if (dealerId == null) {
//                getAdminApps();
//            } else {
            getAllApps(dealerId);
//            }
        }
    }

    private void getAllApps(String dealerId) {

//        progressBar.setVisibility(View.GONE);
        MyApplication.oneCaller
                .getAllApps(new DeviceAndSpace(dealerId, currentSpace()))
                .enqueue(new Callback<InstallAppModel>() {
                    @Override
                    public void onResponse(@NonNull Call<InstallAppModel> call, @NonNull Response<InstallAppModel> response) {
                        if (response.isSuccessful()) {
                            if (response.body().isSuccess()) {
                                setupApps(response);


                            } else {
                                ////TODO: handle token auth fail
                                Timber.d("onResponse: token auth fail");
                                sharedViwModel.setMutableMsgs(Msgs.ERROR);
                            }

                        } else {
                            sharedViwModel.setMutableMsgs(Msgs.SERVER_ERROR);
                        }
                        //swipeRefreshLayout.setRefreshing(false);

                    }

                    @Override
                    public void onFailure(@NonNull Call<InstallAppModel> call, @NonNull Throwable t) {
                        Timber.d("onFailure: ");
                        //TODO when network failure or error while creating request or building response
                        if (t instanceof IOException) {
                            sharedViwModel.setMutableMsgs(Msgs.ERROR);
                        } else {
                            //TODO: handle your internal mapping permission
                        }


                    }
                });

    }

    private String currentSpace() {
        String space = prefUtils.getStringPref( CURRENT_KEY);
        if (KEY_MAIN_PASSWORD.equals(space)) {
            return "encrypted";
        }
        return "guest";
    }


    private void setupApps(@NonNull Response<InstallAppModel> response) {
        Map<String, DownloadStatusCls> map = null;
        if (prefUtils.getStringPref( DOWNLAOD_HASH_MAP) != null) {
            Type typetoken = new TypeToken<HashMap<String, DownloadStatusCls>>() {
            }.getType();
            String hashmap = prefUtils.getStringPref( DOWNLAOD_HASH_MAP);
            map = new Gson().fromJson(hashmap, typetoken);

        }
        appInfos.clear();
        appInfos.addAll(response.body().getServerAppInfo());
        newApps.clear();
        installedInfo.clear();
        updatesInfo.clear();

        for (int i = 0; i < appInfos.size(); i++) {
            ServerAppInfo appInfo = appInfos.get(i);
            if (map != null) {
                DownloadStatusCls status = map.get(appInfo.getPackageName());
                if (status != null) {
                    switch (status.getStatus()) {
                        case PENDING:
                            appInfo.setType(ServerAppInfo.PROG_TYPE.LOADING);
                            break;
                        case LOADING:
                            appInfo.setType(ServerAppInfo.PROG_TYPE.VISIBLE);
                            break;
                        case INSTALLING:
                            appInfo.setType(ServerAppInfo.PROG_TYPE.INSTALLING);
                            break;
                    }
                }
            }
            if (appInstalledOrNot(appInfo.getPackageName())) {
                appInfo.setInstalled(true);
                if (isUpdateAvailable(appInfo.getPackageName(), Integer.parseInt(appInfo.getVersion_code()))) {
                    appInfo.setUpdate(true);
                    updatesInfo.add(appInfo);
                } else {
                    appInfo.setType(ServerAppInfo.PROG_TYPE.GONE);
                    installedInfo.add(appInfo);
                }
            } else {
                newApps.add(appInfo);
            }

        }
        sharedViwModel.setMutableMsgs(Msgs.SUCCESS);
        sharedViwModel.setAllApps(newApps);
        sharedViwModel.setInstalledApps(installedInfo);
        sharedViwModel.setUpdates(updatesInfo);

    }

    private void getAdminApps() {
//        progressBar.setVisibility(View.VISIBLE);
        MyApplication.oneCaller
                .getAdminApps(currentSpace())
                .enqueue(new Callback<InstallAppModel>() {
                    @Override
                    public void onResponse(@NonNull Call<InstallAppModel> call, @NonNull Response<InstallAppModel> response) {
                        if (response.isSuccessful()) {
                            if (response.body().isSuccess()) {
                                setupApps(response);
                            } else {
                                //TODO: handle token auth fail
                            }

                        } else {
                            //TODO: server responded with other then 200 response code
                            try {
                                JSONObject jObjError = new JSONObject(response.errorBody().string());
                                Toast.makeText(SMActivity.this, jObjError.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                            } catch (JSONException | IOException e) {
                                Toast.makeText(SMActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }


                    }

                    @Override
                    public void onFailure(@NonNull Call<InstallAppModel> call, @NonNull Throwable t) {

                        Timber.d("onFailure: ");

                        if (t instanceof IOException) {
                            sharedViwModel.setMutableMsgs(Msgs.ERROR);
                        } else {
                            //TODO: internal error
                        }

                    }

                });


    }

    private boolean appInstalledOrNot(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean isUpdateAvailable(String packageName, int version) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            if (version > info.versionCode) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            mService.setMarketDownloadListener(null);
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        unbindService(connection);
        unRegisterNetworkPref();
    }

    @Override
    public void onInstallClick(ServerAppInfo app, int position, boolean isUpdate) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final Network n = cm.getActiveNetwork();

        if (n != null) {
            final NetworkCapabilities nc = cm.getNetworkCapabilities(n);

            if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                if (prefUtils.getIntegerPref( SECUREMARKETSIM) != 1) {
                    AppExecutor.getInstance().getMainThread().execute(() -> {
                        new AlertDialog.Builder(this)
                                .setTitle("Mobile Data")
                                .setMessage("Please allow Secure Market to use mobile data for downloading Application.")
                                .setPositiveButton("Allow", (dialog1, which) -> {
                                    //
                                    downloadAndInstallApp(app, position, isUpdate, currentSpace);
                                })
                                .setNegativeButton(R.string.cancel, (dialog1, which) -> dialog1.dismiss())
                                .show();
                    });

                } else {
                    downloadAndInstallApp(app, position, isUpdate, currentSpace);
                }
            } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                if (prefUtils.getIntegerPref( SECUREMARKETWIFI) != 1) {
                    AppExecutor.getInstance().getMainThread().execute(() -> {
                        new AlertDialog.Builder(this)
                                .setTitle("WiFi")
                                .setMessage("Please allow Secure Market to use WiFi for downloading Application.")
                                .setPositiveButton("Allow", (dialog1, which) -> {
                                    //
                                    downloadAndInstallApp(app, position, isUpdate, currentSpace);
                                })
                                .setNegativeButton(R.string.cancel, (dialog1, which) -> dialog1.dismiss())
                                .show();
                    });

                } else {
                    downloadAndInstallApp(app, position, isUpdate, currentSpace);
                }
            } else
                downloadAndInstallApp(app, position, isUpdate, currentSpace);
        }
    }

    @Override
    public void onUnInstallClick(ServerAppInfo app, boolean status) {
        if (!status) {
            Toast.makeText(this, getResources().getString(R.string.uninstall_permission_denied), Toast.LENGTH_LONG).show();
        } else {

            Set<String> packages = new HashSet<>();
            packages.add(MyApplication.getAppContext().getPackageName());
            packages.add("com.secure.systemcontrol");
            String userSpace = prefUtils.getStringPref( CURRENT_KEY);

            if (!packages.contains(app.getPackageName())) {
                try {
                    ApplicationInfo info = getPackageManager().getApplicationInfo(app.getPackageName(), 0);
                    String label = getPackageManager().getApplicationLabel(info).toString();
                    Drawable drawable = getPackageManager().getApplicationIcon(info);
                    new AlertDialog.Builder(this)
                            .setTitle(label)
                            .setIcon(drawable)
                            .setMessage("Do you want to uninstall this app?")
                            .setPositiveButton(R.string.ok, (dialog, which) -> {

                                Utils.silentPullApp(SMActivity.this, app.getPackageName(), label);

                                if (sectionsPagerAdapter != null) {
                                    InstalledAppsFragment fragment = sectionsPagerAdapter.getInstalledAppsFragment();
                                    MarketFragment fragment1 = sectionsPagerAdapter.getMarketFragment();
                                    if (fragment != null) {
                                        fragment.onInstallationComplete(app.getPackageName());
                                    }
                                    if (fragment1 != null) {
                                        app.setInstalled(false);
                                        fragment1.addPackageToList(app);
                                    }
                                }

                            }).setNegativeButton(R.string.cancel, (dialog, which) -> {
                        dialog.dismiss();
                    }).show();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(this, getResources().getString(R.string.uninstall_permission_denied), Toast.LENGTH_LONG).show();
            }


        }

    }

    @Override
    public void onAppsRefreshRequest() {
        if (isNetworkConneted(prefUtils)) {
            Timber.d("connected");
            loadApps();
        } else {
            Timber.d("not connected");
            sharedViwModel.setMutableMsgs(Msgs.ERROR);
        }
    }

    @Override
    public void onCancelClick(String request_id) {
        mService.cancelDownload(request_id);
    }


    private void downloadAndInstallApp(ServerAppInfo app, int position, boolean isUpdate, String space) {
        AppExecutor.getInstance().getMainThread().execute(() -> {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getResources().getString(R.string.download_title));
            alertDialog.setIcon(android.R.drawable.stat_sys_download);

            alertDialog.setMessage(getResources().getString(R.string.install_message));

            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.ok_capital), (dialog, which) -> {


                String live_url = prefUtils.getStringPref( LIVE_URL);
                AppConstants.INSTALLING_APP_NAME = app.getApkName();
                AppConstants.INSTALLING_APP_PACKAGE = app.getPackageName();

                File apksPath = new File(getFilesDir(), "apk");
                File file = new File(apksPath, app.getApk());
//                File file = new File(Environment.getExternalStorageDirectory() + "/" + appName);
                if (!apksPath.exists()) {
                    apksPath.mkdir();
                }
                String url = live_url + MOBILE_END_POINT + GET_APK_ENDPOINT +
                        CommonUtils.splitName(app.getApk());
                String fileName = file.getAbsolutePath();
                if (!file.exists()) {

                    if (mService != null) {
                        app.setType(ServerAppInfo.PROG_TYPE.LOADING);
                        if (isUpdate) {
                            try {
                                sectionsPagerAdapter.getUpdateAppsFragment().getInstalledAdapter().updateProgressOfItem(app, position);
                            } catch (NullPointerException ignored) {
                            }
                        } else {
                            try {
                                sectionsPagerAdapter.getMarketFragment().getInstalledAdapter().updateProgressOfItem(app, position);
                            } catch (NullPointerException ignored) {
                            }
                        }
                        mService.startDownload(url, fileName, app.getPackageName(), AppConstants.EXTRA_MARKET_FRAGMENT, space);

                    }

                } else {
                    int file_size = Integer.parseInt(String.valueOf(file.length() / 1024));
                    if (file_size >= (101 * 1024)) {
                        showInstallDialog(new File(fileName), app.getPackageName(), space);
                    } else {
                        if (mService != null) {
                            File file1 = new File(file.getAbsolutePath());
                            file.delete();
                            app.setType(ServerAppInfo.PROG_TYPE.LOADING);
                            if (isUpdate) {
                                try {
                                    sectionsPagerAdapter.getUpdateAppsFragment().getInstalledAdapter().updateProgressOfItem(app, position);
                                } catch (NullPointerException ignored) {
                                }
                            } else {
                                try {
                                    sectionsPagerAdapter.getMarketFragment().getInstalledAdapter().updateProgressOfItem(app, position);
                                } catch (NullPointerException ignored) {
                                }
                            }
                            mService.startDownload(url, file1.getAbsolutePath(), app.getPackageName(), AppConstants.EXTRA_MARKET_FRAGMENT, currentSpace);

                        }
                    }
                }

            });

            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.cancel_capital),
                    (dialog, which) -> dialog.dismiss());
            alertDialog.show();
        });
    }

    private void showInstallDialog(File file, String packageName, String space) {

        // we need to install app sielently
        try {
            Uri uri = Uri.fromFile(file);
            Utils.installSielentInstall(this, Objects.requireNonNull(getContentResolver().openInputStream(uri)), packageName, space);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefUtils.saveBooleanPref( UNINSTALL_ALLOWED, true);
        refreshApps(this);
        AppConstants.TEMP_SETTINGS_ALLOWED = true;
        currentSpace = prefUtils.getStringPref( CURRENT_KEY);
    }

    @Override
    public void onAppsRefresh() {

    }


    @Retention(RetentionPolicy.CLASS)
    @IntDef(value = {LOADING, INSTALLING, PENDING})
    public @interface DownlaodState {
    }

    public static final int LOADING = 1;
    public static final int INSTALLING = 2;
    public static final int PENDING = 3;

    private NetworkChangeReceiver networkChangeReceiver;
    private SharedPreferences sharedPref;

    private void registerNetworkPref() {
        sharedPref = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(networkChange);
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void unRegisterNetworkPref() {
        if (sharedPref != null)
            sharedPref.unregisterOnSharedPreferenceChangeListener(networkChange);
        if (networkChangeReceiver != null)
            unregisterReceiver(networkChangeReceiver);
    }

    SharedPreferences.OnSharedPreferenceChangeListener networkChange = (sharedPreferences, key) -> {

        if (key.equals(CURRENT_NETWORK_STATUS)) {

            String networkStatus = sharedPreferences.getString(CURRENT_NETWORK_STATUS, LIMITED);

            boolean isConnected = networkStatus.equals(CONNECTED);

            Timber.d("ksdklfgsmksls : " + isConnected);

            if (isConnected) {
                loadApps();
            } else {
                sharedViwModel.setMutableMsgs(Msgs.ERROR);
            }
        }
    };

}
