package com.secureMarket;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.listener.OnAppsRefreshListener;
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
import com.secureMarket.ui.home.InstalledAppsFragment;
import com.secureMarket.ui.home.MarketFragment;
import com.secureMarket.ui.home.Msgs;
import com.secureMarket.ui.home.SharedViwModel;
import com.secureMarket.ui.home.UpdateAppsFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static com.screenlocker.secure.socket.utils.utils.refreshApps;
import static com.screenlocker.secure.socket.utils.utils.saveLiveUrl;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.INSTALLED_PACKAGES;
import static com.screenlocker.secure.utils.AppConstants.IS_SETTINGS_ALLOW;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.SECUREMARKETSIM;
import static com.screenlocker.secure.utils.AppConstants.SECUREMARKETWIFI;
import static com.screenlocker.secure.utils.AppConstants.UNINSTALLED_PACKAGES;
import static com.screenlocker.secure.utils.AppConstants.UNINSTALL_ALLOWED;
import static com.secureMarket.MarketUtils.savePackages;

public class SMActivity extends AppCompatActivity implements DownloadServiceCallBacks, AppInstallUpdateListener, OnAppsRefreshListener {

    private LockScreenService mService = null;
    private List<ServerAppInfo> appInfos = new ArrayList<>();
    private List<ServerAppInfo> newApps = new ArrayList<>();
    private List<ServerAppInfo> updatesInfo = new ArrayList<>();
    private List<ServerAppInfo> installedInfo = new ArrayList<>();
    private SharedViwModel sharedViwModel;
    private MainMarketPagerAdapter sectionsPagerAdapter;

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

        sectionsPagerAdapter = new MainMarketPagerAdapter(this, getSupportFragmentManager());

        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        sharedViwModel = ViewModelProviders.of(this).get(SharedViwModel.class);

        String dealerId = PrefUtils.getStringPref(this, AppConstants.KEY_DEVICE_LINKED);
        //Log.d("ConnectedDealer",dealerId);
        if (dealerId == null || dealerId.equals("")) {
            //   getAdminApps();
            getServerApps(null, RetrofitClientInstance.getWhiteLabelInstance());
        } else {
            getServerApps(dealerId, RetrofitClientInstance.getWhiteLabelInstance());
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
                            fragment.onInstallationComplete(info.getPackageName());
                        }
                        if (fragment1 != null) {
                            fragment1.onInstallationComplete(info.getPackageName());
                        }
                        if (fragment2 != null) {
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
    public void onDownLoadProgress(String pn, int progress, long speed) {

        if (sectionsPagerAdapter != null) {
            MarketFragment fragment = sectionsPagerAdapter.getMarketFragment();
            UpdateAppsFragment fragment1 = sectionsPagerAdapter.getUpdateAppsFragment();
            if (fragment != null) {
                fragment.onDownLoadProgress(pn, progress, speed);
            } if (fragment1!=null){
                fragment1.onDownLoadProgress(pn, progress, speed);
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
    public void downloadComplete(String filePath, String pn) {

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
            showInstallDialog(new File(filePath), pn);
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
                fragment1.onDownloadStarted( pn);
            }
        }
//        int index = IntStream.range(0, unInstalledApps.size())
//                .filter(i -> Objects.nonNull(unInstalledApps.get(i)))
//                .filter(i -> pn.equals(unInstalledApps.get(i).getPackageName()))
//                .findFirst()
//                .orElse(-1);
//        if (index != -1) {
//            ServerAppInfo info = unInstalledApps.get(index);
//            info.setProgres(0);
//            info.setType(ServerAppInfo.PROG_TYPE.VISIBLE);
//            uninstalledAdapter.updateProgressOfItem(info, index);
//        }
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


    private void getServerApps(String dealerId, ApiOneCaller apiOneCaller) {

        Timber.d("<<< Getting Server Apps >>>");

        if (dealerId == null) {
            Timber.i("-----------> getting admin apps ");
            getAdminApps(apiOneCaller);
        } else {
            Timber.i("---------> getting dealer apps and dealer id is :%s", dealerId);
            getAllApps(dealerId, apiOneCaller);
        }
    }

    private void getAllApps(String dealerId, ApiOneCaller apiOneCaller) {

//        progressBar.setVisibility(View.GONE);
        apiOneCaller
                .getAllApps("marketApplist/" + dealerId)
                .enqueue(new Callback<InstallAppModel>() {
                    @Override
                    public void onResponse(@NonNull Call<InstallAppModel> call, @NonNull Response<InstallAppModel> response) {
                        boolean responseStatus = response.isSuccessful();

                        Timber.i("---------> get admin apps response status :%s", responseStatus);


                        if (responseStatus) {

                            if (response.body() != null) {

                                boolean tokenStatus = response.body().isSuccess();
                                if (tokenStatus) {
                                    saveLiveUrl(isFailSafe);
                                    setupApps(response);
                                } else {
                                    //TODO: handle token auth fail
                                    Timber.e("--------------> token verification failed from server :( ");
                                }

                            } else {
                                Timber.e("response body is null :(");
                            }

                        } else {
                            //TODO: server responded with other then 200 response code
                            try {
                                JSONObject jObjError = new JSONObject(response.errorBody().string());
                                Toast.makeText(SMActivity.this, jObjError.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(SMActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                            Timber.i("-----------> response code is not valid :( ");
                        }


                    }

                    @Override
                    public void onFailure(@NonNull Call<InstallAppModel> call, @NonNull Throwable t) {
                        Timber.d("onFailure: ");
                        //TODO when network failure or error while creating request or building response
                        if (t instanceof IOException){
                            sharedViwModel.setMutableMsgs(Msgs.ERROR);
                        }else {
                            //TODO: handle your internal mapping permission
                        }

                        Timber.d("onFailure : %s", t.getMessage());

                        if (t instanceof UnknownHostException) {
                            Timber.e("-----------> something very dangerous happen with domain : %s", t.getMessage());
                            if (isFailSafe) {
                                Timber.e("------------> FailSafe domain is also not working. ");
                            } else {
                                Timber.i("<<< New Api call with failsafe domain >>>");
                                getAllApps(dealerId, RetrofitClientInstance.getFailSafeInstanceForWhiteLabel());
                                isFailSafe = true;
                            }

                        } else if (t instanceof IOException) {
                            Timber.e(" ----> IO Exception :%s", t.getMessage());
                        }

                    }
                });

    }

    private void setupApps(@NonNull Response<InstallAppModel> response) {

        appInfos.clear();
        if (response.body() != null) {
            appInfos.addAll(response.body().getServerAppInfo());
        }
        refreshAppList();

    }

    private void refreshAppList() {
        newApps.clear();
        installedInfo.clear();
        updatesInfo.clear();

        for (int i = 0; i < appInfos.size(); i++) {
            ServerAppInfo appInfo = appInfos.get(i);
            appInfo.setProgres(0);
            appInfo.setType(ServerAppInfo.PROG_TYPE.GONE);
            if (appInstalledOrNot(appInfo.getPackageName())) {
                appInfo.setInstalled(true);
                if (isUpdateAvailable(appInfo.getPackageName(), Integer.parseInt(appInfo.getVersion_code()))) {
                    appInfo.setUpdate(true);
                    updatesInfo.add(appInfo);
                } else {
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

    private boolean isFailSafe = false;

    private void getAdminApps(ApiOneCaller apiOneCaller) {

        apiOneCaller
                .getAdminApps()
                .enqueue(new Callback<InstallAppModel>() {
                    @Override
                    public void onResponse(@NonNull Call<InstallAppModel> call, @NonNull Response<InstallAppModel> response) {

                        boolean responseStatus = response.isSuccessful();

                        Timber.i("---------> get admin apps response status :%s", responseStatus);


                        if (responseStatus) {

                            if (response.body() != null) {


                                boolean tokenStatus = response.body().isSuccess();
                                if (tokenStatus) {
                                    saveLiveUrl(isFailSafe);
                                    setupApps(response);
                                } else {
                                    //TODO: handle token auth fail
                                    Timber.e("--------------> token verification failed from server :( ");
                                }

                            } else {
                                Timber.e("response body is null :(");
                            }

                        } else {
                            //TODO: server responded with other then 200 response code
                            Timber.i("-----------> response code is not valid :( ");
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

                        if (t instanceof IOException){
                           sharedViwModel.setMutableMsgs(Msgs.ERROR);
                        }else {
                            //TODO: internal error
                        }

                        Timber.d("onFailure : %s", t.getMessage());

                        if (t instanceof UnknownHostException) {
                            Timber.e("-----------> something very dangerous happen with domain : %s", t.getMessage());
                            if (isFailSafe) {
                                Timber.e("------------> FailSafe domain is also not working. ");
                            } else {
                                Timber.i("<<< New Api call with failsafe domain >>>");
                                getAdminApps(RetrofitClientInstance.getFailSafeInstanceForWhiteLabel());
                                isFailSafe = true;
                            }

                        } else if (t instanceof IOException) {
                            Timber.e(" ----> IO Exception :%s", t.getMessage());
                        }

                    }

                });


    }

    private boolean appInstalledOrNot(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
//           Timber.e(e);
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
//            Timber.e(e);
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
    }

    @Override
    public void onInstallClick(ServerAppInfo app, int position, boolean isUpdate) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final Network n = cm.getActiveNetwork();

        if (n != null) {
            final NetworkCapabilities nc = cm.getNetworkCapabilities(n);

            if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                if (PrefUtils.getIntegerPref(this, SECUREMARKETSIM) != 1) {
                    AppExecutor.getInstance().getMainThread().execute(() -> {
                        new AlertDialog.Builder(this)
                                .setTitle("Mobile Data")
                                .setMessage("Please allow Secure Market to use mobile data for downloading Application.")
                                .setPositiveButton("Allow", (dialog1, which) -> {
                                    //
                                    downloadAndInstallApp(app, position, isUpdate);
                                })
                                .setNegativeButton(R.string.cancel, (dialog1, which) -> dialog1.dismiss())
                                .show();
                    });

                } else {
                    downloadAndInstallApp(app, position, isUpdate);
                }
            } else if (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                if (PrefUtils.getIntegerPref(this, SECUREMARKETWIFI) != 1) {
                    AppExecutor.getInstance().getMainThread().execute(() -> {
                        new AlertDialog.Builder(this)
                                .setTitle("WiFi")
                                .setMessage("Please allow Secure Market to use WiFi for downloading Application.")
                                .setPositiveButton("Allow", (dialog1, which) -> {
                                    //
                                    downloadAndInstallApp(app, position, isUpdate);
                                })
                                .setNegativeButton(R.string.cancel, (dialog1, which) -> dialog1.dismiss())
                                .show();
                    });

                } else {
                    downloadAndInstallApp(app, position, isUpdate);
                }
            } else
                downloadAndInstallApp(app, position, isUpdate);
        }
    }

    @Override
    public void onUnInstallClick(ServerAppInfo app, boolean status) {
        if (!status) {
            Toast.makeText(this, getResources().getString(R.string.uninstall_permission_denied), Toast.LENGTH_LONG).show();
        } else {

            Set<String> packages = new HashSet<>();
            packages.add(MyApplication.getAppContext().getPackageName());
            packages.add("com.vortexlocker.app");
            packages.add("com.rim.mobilefusion.client");
            packages.add("com.secure.systemcontrol");
            String userSpace = PrefUtils.getStringPref(this, AppConstants.CURRENT_KEY);

            if (!packages.contains(app.getPackageName())) {
                savePackages(app.getPackageName(), UNINSTALLED_PACKAGES, PrefUtils.getStringPref(this, CURRENT_KEY), this);
                Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
                intent.setData(Uri.parse("package:" + app.getPackageName()));
                startActivity(intent);
            } else {
                Toast.makeText(this, getResources().getString(R.string.uninstall_permission_denied), Toast.LENGTH_LONG).show();
            }


        }

    }

    @Override
    public void onAppsRefreshRequest() {
        String dealerId = PrefUtils.getStringPref(this, AppConstants.KEY_DEVICE_LINKED);
        //Log.d("ConnectedDealer",dealerId);
        if (dealerId == null || dealerId.equals("")) {
            //   getAdminApps();
            getServerApps(null, RetrofitClientInstance.getWhiteLabelInstance());
        } else {
            getServerApps(dealerId, RetrofitClientInstance.getWhiteLabelInstance());
            // getAllApps(dealerId);
        }
    }


    private void downloadAndInstallApp(ServerAppInfo app, int position, boolean isUpdate) {
        AppExecutor.getInstance().getMainThread().execute(() -> {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle(getResources().getString(R.string.download_title));
            alertDialog.setIcon(android.R.drawable.stat_sys_download);

            alertDialog.setMessage(getResources().getString(R.string.install_message));

            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.ok_capital), (dialog, which) -> {


                String live_url = PrefUtils.getStringPref(this, LIVE_URL);

                AppConstants.INSTALLING_APP_NAME = app.getApkName();
                AppConstants.INSTALLING_APP_PACKAGE = app.getPackageName();

                File apksPath = new File(getFilesDir(), "apk");
                File file = new File(apksPath, app.getApk());
//                File file = new File(Environment.getExternalStorageDirectory() + "/" + appName);
                if (!apksPath.exists()) {
                    apksPath.mkdir();
                }
                String url = live_url + "getApk/" +
                        CommonUtils.splitName(app.getApk());

                Timber.i("LIVE URL :%s", url);

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
                        mService.startDownload(url, fileName, app.getPackageName(), AppConstants.EXTRA_MARKET_FRAGMENT);

                    }

                } else {
                    int file_size = Integer.parseInt(String.valueOf(file.length() / 1024));
                    if (file_size >= (101 * 1024)) {
                        showInstallDialog(new File(fileName), app.getPackageName());
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
                            mService.startDownload(url, file1.getAbsolutePath(), app.getPackageName(), AppConstants.EXTRA_MARKET_FRAGMENT);

                        }
                    }
                }

            });

            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.cancel_capital),
                    (dialog, which) -> dialog.dismiss());
            alertDialog.show();
        });
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
    protected void onResume() {
        super.onResume();
        PrefUtils.saveBooleanPref(this, UNINSTALL_ALLOWED, true);
        PrefUtils.saveBooleanPref(this, IS_SETTINGS_ALLOW, false);
        refreshApps(this);
        AppConstants.TEMP_SETTINGS_ALLOWED = true;
        refreshAppList();
    }


    @Override
    public void onAppsRefresh() {

    }
}
