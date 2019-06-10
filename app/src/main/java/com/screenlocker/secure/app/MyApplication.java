package com.screenlocker.secure.app;

import android.app.Activity;
import android.app.Application;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.crashlytics.android.Crashlytics;
import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.R;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.async.CheckInstance;
import com.screenlocker.secure.async.DownLoadAndInstallUpdate;
import com.screenlocker.secure.mdm.base.DeviceExpiryResponse;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceModel;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.mdm.utils.NetworkChangeReceiver;
import com.screenlocker.secure.networkResponseModels.LoginModel;
import com.screenlocker.secure.networkResponseModels.LoginResponse;
import com.screenlocker.secure.offline.MyAlarmBroadcastReceiver;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.room.MyAppDatabase;
import com.screenlocker.secure.settings.codeSetting.installApps.UpdateModel;
import com.screenlocker.secure.socket.receiver.AppsStatusReceiver;
import com.screenlocker.secure.socket.service.SocketService;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.ALARM_TIME_COMPLETED;
import static com.screenlocker.secure.utils.AppConstants.CHECK_OFFLINE_EXPIRY;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.SUPER_ADMIN;
import static com.screenlocker.secure.utils.AppConstants.SUPER_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.SYSTEM_LOGIN_TOKEN;
import static com.screenlocker.secure.utils.AppConstants.URL_1;
import static com.screenlocker.secure.utils.AppConstants.URL_2;

/**
 * application class to get the database instance
 */
public class MyApplication extends Application implements NetworkChangeReceiver.NetworkChangeListener {


    public static boolean recent = false;
    private MyAppDatabase myAppDatabase;
    private ComponentName compName;
    private DevicePolicyManager devicePolicyManager;
    private LinearLayout screenShotView;
    ApiOneCaller apiOneCaller;
    PrefManager preferenceManager;
    AppsStatusReceiver appsStatusReceiver;


    private static Context appContext;

    private NetworkChangeReceiver networkChangeReceiver;
    private MyAlarmBroadcastReceiver myAlarmBroadcastReceiver;

    private LinearLayout createScreenShotView() {
        LinearLayout linearLayout = new LinearLayout(this);
        View btn = new View(this);
        linearLayout.addView(btn);
        return linearLayout;
    }


    public static ApiOneCaller oneCaller = null;

    public static Context getAppContext() {
        return appContext;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        appContext = getApplicationContext();

        networkChangeReceiver = new NetworkChangeReceiver();
        networkChangeReceiver.setNetworkChangeListener(this);

        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        registerReceiver(myAlarmBroadcastReceiver, new IntentFilter(ALARM_TIME_COMPLETED));


        try {
            Fabric.with(this, new Crashlytics());
        } catch (Exception ignored) {
        }

        compName = new ComponentName(this, MyAdmin.class);
        screenShotView = createScreenShotView();


        // your oncreate code should be


//        sendBroadcast(new Intent().setAction("com.mediatek.ppl.NOTIFY_LOCK"));

//        OnEventsChangeListener onEventsChangeListener = (OnEventsChangeListener) this;
//
//        if (onEventsChangeListener != null) {
//            Log.d(TAG, "listener not null");
//            onEventsChangeListener.onAppSettings(true);
//        }

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        Thread thread = new Thread() {
            @Override
            public void run() {
                myAppDatabase = Room.databaseBuilder(getApplicationContext(), MyAppDatabase.class, AppConstants.DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build();
            }
        };


        thread.start();
        Timber.plant(new Timber.DebugTree());

        BarryAppComponent component = DaggerBarryAppComponent
                .builder()
                .contextModule(new ContextModule(this))
                .build();

        apiOneCaller = component.getApiOneCaller();


//   startService(new Intent(this,LifecycleReceiverService.class));

        IntentFilter filter = new IntentFilter();

        filter.addAction("com.secure.systemcontroll.PackageAdded");
        filter.addAction("com.secure.systemcontroll.PackageDeleted");
        filter.addAction("com.secure.systemcontrol.PACKAGE_ADDED_SECURE_MARKET");

        registerReceiver(appsStatusReceiver, filter);
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE);
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });


    }


    /**
     * to get the database object
     *
     * @param context
     * @return room database object
     */

    public static MyAppDatabase getAppDatabase(Context context) {
        return ((MyApplication) context.getApplicationContext()).myAppDatabase;
    }

    public static LinearLayout getScreenShotView(Context context) {
        return ((MyApplication) context.getApplicationContext()).screenShotView;
    }

    public static ComponentName getComponent(Context context) {
        return ((MyApplication) context.getApplicationContext()).compName;
    }

    public static DevicePolicyManager getDevicePolicyManager(Context context) {
        return ((MyApplication) context.getApplicationContext()).devicePolicyManager;
    }

    public ApiOneCaller getApiOneCaller() {
        return apiOneCaller;
    }

    public PrefManager getPreferenceManager() {
        return preferenceManager;
    }


    @Override
    public void onTerminate() {
        unregisterReceiver(appsStatusReceiver);
        unregisterReceiver(networkChangeReceiver);
        networkChangeReceiver.unsetNetworkChangeListener();
        unregisterReceiver(myAlarmBroadcastReceiver);
        super.onTerminate();
    }


    @Override
    public void isConnected(boolean state) {


        if (state) {

            onlineConnection();

            boolean offlineExpiry = PrefUtils.getBooleanPref(this, CHECK_OFFLINE_EXPIRY);
            if (!offlineExpiry) {
                checkOfflineExpiry();
            }

        } else {
            if (utils.isMyServiceRunning(SocketService.class, appContext)) {
                Intent intent = new Intent(this, SocketService.class);
                stopService(intent);
            }
        }
    }


    private void onlineConnection() {

        AppConstants.isProgress = true;
        AppConstants.result = false;

        String[] urls = {URL_1, URL_2};

        new AsyncCalls(output -> {

            if (output != null) {
                PrefUtils.saveStringPref(appContext, LIVE_URL, output);
                String live_url = PrefUtils.getStringPref(this, LIVE_URL);
                oneCaller = RetrofitClientInstance.getRetrofitInstance(live_url + MOBILE_END_POINT).create(ApiOneCaller.class);
                checkForDownload();
                boolean linkStatus = PrefUtils.getBooleanPref(this, AppConstants.DEVICE_LINKED_STATUS);
                if (linkStatus) {
                    String macAddress = CommonUtils.getMacAddress();
                    String serialNo = DeviceIdUtils.getSerialNumber();
                    if (serialNo != null) {
                        new ApiUtils(MyApplication.this, macAddress, serialNo);
                    }
                }
                AppConstants.result = true;
            }
            AppConstants.isProgress = false;
        }, this, urls).execute();// checking hosts
    }

    private void checkOfflineExpiry() {

        Timber.d("Checking offline Expiry");

        String[] urls = {SUPER_ADMIN, URL_2};

        new AsyncCalls(output -> {
            if (output != null) {
                String url = output + SUPER_END_POINT;
                ApiOneCaller service = RetrofitClientInstance.getRetrofitSecondInstance(url).create(ApiOneCaller.class);
                service.getOfflineExpiry(new DeviceModel(DeviceIdUtils.getSerialNumber(), DeviceIdUtils.getMacAddress())).enqueue(new Callback<DeviceExpiryResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DeviceExpiryResponse> call, @NonNull Response<DeviceExpiryResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {

                            DeviceExpiryResponse deviceExpiryResponse = response.body();
                            Timber.d("EspiresIn : %s", deviceExpiryResponse.getExpiresIn());
                            Timber.d("StartDate : %s", deviceExpiryResponse.getStartDate());
                            Timber.d("EndDate : %s", deviceExpiryResponse.getEndDate());

                            if (deviceExpiryResponse.isStatus()) {

                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DeviceExpiryResponse> call, @NonNull Throwable t) {
                        Timber.d(t.getMessage());
                    }
                });

            }
        }, this, urls).execute();
    }


    private void checkForDownload() {
        new CheckInstance(internet -> {
            if (internet) {

                String currentVersion = "1";
                try {
                    currentVersion = String.valueOf(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
                } catch (PackageManager.NameNotFoundException e) {
                    Timber.d(e);
                }

                MyApplication.oneCaller
                        .getUpdate("getUpdate/" + currentVersion + "/" + getPackageName() + "/" + getString(R.string.app_name), PrefUtils.getStringPref(this, SYSTEM_LOGIN_TOKEN))
                        .enqueue(new Callback<UpdateModel>() {
                            @Override
                            public void onResponse(@NonNull Call<UpdateModel> call, @NonNull Response<UpdateModel> response) {

                                if (response.body() != null) {
                                    if (response.body().isSuccess()) {
                                        if (response.body().isApkStatus()) {
                                            String url = response.body().getApkUrl();
                                            String live_url = PrefUtils.getStringPref(MyApplication.getAppContext(), LIVE_URL);
                                            DownLoadAndInstallUpdate obj = new DownLoadAndInstallUpdate(appContext, live_url + MOBILE_END_POINT + "getApk/" + CommonUtils.splitName(url), true);
                                            obj.execute();

                                        }  //                                            Toast.makeText(appContext, getString(R.string.uptodate), Toast.LENGTH_SHORT).show();


                                    } else {
                                        saveToken();
                                        checkForDownload();
                                    }

                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<UpdateModel> call, @NonNull Throwable t) {

                            }
                        });
            }
        });

    }


    public static void saveToken() {


        new CheckInstance(internet -> {
            if (internet) {
                MyApplication.oneCaller
                        .login(new LoginModel(DeviceIdUtils.getSerialNumber(), DeviceIdUtils.getMacAddress(), DeviceIdUtils.getIPAddress(true))).enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                        if (response.body() != null) {
                            if (response.body().isStatus()) {
                                PrefUtils.saveStringPref(appContext, SYSTEM_LOGIN_TOKEN, response.body().getToken());

                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {

                    }
                });
            }
        });


    }




}
