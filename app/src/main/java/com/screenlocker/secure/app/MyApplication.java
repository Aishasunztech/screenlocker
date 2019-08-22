package com.screenlocker.secure.app;

import android.app.Activity;
import android.app.Application;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.room.Room;

import com.crashlytics.android.Crashlytics;
import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.R;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.crash.CustomErrorActivity;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.mdm.utils.NetworkChangeReceiver;
import com.screenlocker.secure.networkResponseModels.LoginModel;
import com.screenlocker.secure.networkResponseModels.LoginResponse;
import com.screenlocker.secure.offline.MyAlarmBroadcastReceiver;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.room.migrations.Migration_11_13;
import com.screenlocker.secure.room.MyAppDatabase;
import com.screenlocker.secure.socket.receiver.AppsStatusReceiver;
import com.screenlocker.secure.socket.service.SocketService;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;
import com.secureSetting.t.data.AppItem;
import com.secureSetting.t.data.DataManager;
import com.secureSetting.t.db.DbHistoryExecutor;
import com.secureSetting.t.db.DbIgnoreExecutor;
import com.secureSetting.t.service.AppService;
import com.secureSetting.t.util.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.ALARM_TIME_COMPLETED;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
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

    public MyApplication() {
    }


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

        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
                .enabled(true) //default: true
                .showErrorDetails(true) //default: true
                .showRestartButton(true) //default: true
                .logErrorOnRestart(true) //default: true
                .trackActivities(false) //default: false
                .minTimeBetweenCrashesMs(2000) //default: 3000
                .errorDrawable(R.drawable.customactivityoncrash_error_image) //default: bug image
                .restartActivity(MainActivity.class) //default: null (your app's launch activity)
                .errorActivity(CustomErrorActivity.class) //default: null (default error activity)
                .eventListener(null) //default: null
                .apply();

        networkChangeReceiver = new NetworkChangeReceiver();
        networkChangeReceiver.setNetworkChangeListener(this);

        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        registerReceiver(myAlarmBroadcastReceiver, new IntentFilter(ALARM_TIME_COMPLETED));


        try {
            Fabric.with(this, new Crashlytics());
        } catch (Exception e) {
            e.printStackTrace();
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
                        .addMigrations(new Migration_11_13(11, 13))
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
        String language_key = PrefUtils.getStringPref(getAppContext(), AppConstants.LANGUAGE_PREF);
        if (language_key != null && !language_key.equals("")) {
            CommonUtils.setAppLocale(language_key, getAppContext());
        }

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                String language_key = PrefUtils.getStringPref(getAppContext(), AppConstants.LANGUAGE_PREF);
                if (language_key != null && !language_key.equals("")) {
                    CommonUtils.setAppLocale(language_key, getAppContext());
                    try {
                        ((AppCompatActivity) activity).getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

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

        PreferenceManager.init(this);
        getApplicationContext().startService(new Intent(getApplicationContext(), AppService.class));
        DbIgnoreExecutor.init(getApplicationContext());
        DbHistoryExecutor.init(getApplicationContext());
        DataManager.init();
        addDefaultIgnoreAppsToDB();


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

        Timber.d("STATUS :" + state);


        if (state) {
            onlineConnection();
        } else {
            if (utils.isMyServiceRunning(SocketService.class, appContext)) {
                Intent intent = new Intent(this, SocketService.class);
                stopService(intent);
            }
        }
    }

    private AsyncCalls asyncCalls;

    private void onlineConnection() {


        String[] urls = {URL_1, URL_2};

        if (asyncCalls != null) {
            asyncCalls.cancel(true);
        }

        asyncCalls = new AsyncCalls(output -> {

            Timber.d("output : " + output);

            if (output != null) {
                PrefUtils.saveStringPref(appContext, LIVE_URL, output);
                String live_url = PrefUtils.getStringPref(this, LIVE_URL);
                Timber.d("live_url %s", live_url);
                oneCaller = RetrofitClientInstance.getRetrofitInstance(live_url + MOBILE_END_POINT).create(ApiOneCaller.class);
                boolean linkStatus = PrefUtils.getBooleanPref(this, AppConstants.DEVICE_LINKED_STATUS);
                Timber.d("LinkStatus :" + linkStatus);

                if (linkStatus) {

                    Timber.d("LinkStatus :" + linkStatus);
                    String macAddress = DeviceIdUtils.generateUniqueDeviceId(this);
                    String serialNo = DeviceIdUtils.getSerialNumber();

                    new ApiUtils(MyApplication.this, macAddress, serialNo);

                }
            }
        }, this, urls);// checking hosts
        asyncCalls.execute();
    }


//    private void checkForDownload() {
//
//
//        String currentVersion = "1";
//        try {
//            currentVersion = String.valueOf(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
//        } catch (PackageManager.NameNotFoundException e) {
//            Timber.d(e);
//        }
//
//        MyApplication.oneCaller
//                .getUpdate("getUpdate/" + currentVersion + "/" + getPackageName() + "/" + getString(R.string.app_name), PrefUtils.getStringPref(this, SYSTEM_LOGIN_TOKEN))
//                .enqueue(new Callback<UpdateModel>() {
//                    @Override
//                    public void onResponse(@NonNull Call<UpdateModel> call, @NonNull Response<UpdateModel> response) {
//
//                        if (response.body() != null) {
//                            if (response.body().isSuccess()) {
//                                if (response.body().isApkStatus()) {
//                                    String url = response.body().getApkUrl();
//                                    String live_url = PrefUtils.getStringPref(MyApplication.getAppContext(), LIVE_URL);
//                                    DownLoadAndInstallUpdate obj = new DownLoadAndInstallUpdate(appContext, live_url + MOBILE_END_POINT + "getApk/" + CommonUtils.splitName(url), true, null);
//                                    obj.execute();
//
//                                }  //                                            Toast.makeText(appContext, getString(R.string.uptodate), Toast.LENGTH_SHORT).show();
//
//
//                            } else {
//                                saveToken();
//                                checkForDownload();
//                            }
//
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(@NonNull Call<UpdateModel> call, @NonNull Throwable t) {
//
//                    }
//                });
//
//
//    }


    public static void saveToken() {

        if (MyApplication.oneCaller == null) {

            String[] urls = {URL_1, URL_2};

            new AsyncCalls(output -> {

                if (output != null) {
                    PrefUtils.saveStringPref(appContext, LIVE_URL, output);
                    String live_url = PrefUtils.getStringPref(MyApplication.getAppContext(), LIVE_URL);
                    Timber.d("live_url %s", live_url);
                    oneCaller = RetrofitClientInstance.getRetrofitInstance(live_url + MOBILE_END_POINT).create(ApiOneCaller.class);
                    MyApplication.oneCaller
                            .login(new LoginModel(DeviceIdUtils.getSerialNumber(), DeviceIdUtils.generateUniqueDeviceId(getAppContext()), DeviceIdUtils.getIPAddress(true))).enqueue(new Callback<LoginResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                            if (response.body() != null) {
                                if (response.body().isStatus()) {
                                    PrefUtils.saveStringPref(appContext, SYSTEM_LOGIN_TOKEN, response.body().getToken());

                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<LoginResponse> call, Throwable t) {

                        }
                    });
                }
            }, MyApplication.getAppContext(), urls).execute();


        } else {
            MyApplication.oneCaller
                        .login(new LoginModel(DeviceIdUtils.getSerialNumber(), DeviceIdUtils.generateUniqueDeviceId(getAppContext()), DeviceIdUtils.getIPAddress(true))).enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                        if (response.body() != null) {
                            if (response.body().isStatus()) {
                                PrefUtils.saveStringPref(appContext, SYSTEM_LOGIN_TOKEN, response.body().getToken());

                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LoginResponse> call, Throwable t) {

                    }
                });
        }


    }

    private void addDefaultIgnoreAppsToDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<String> mDefaults = new ArrayList<>();
                mDefaults.add("com.android.settings");
                //mDefaults.add(BuildConfig.APPLICATION_ID);
//                mDefaults.add(BuildConfig.APPLICATION_ID);
                for (String packageName : mDefaults) {
                    AppItem item = new AppItem();
                    item.mPackageName = packageName;
                    item.mEventTime = System.currentTimeMillis();
                    DbIgnoreExecutor.getInstance().insertItem(item);
                }
            }
        }).run();
    }


}




