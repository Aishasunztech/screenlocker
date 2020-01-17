package com.screenlocker.secure.app;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.crashlytics.android.Crashlytics;
import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.mdm.ui.LinkDeviceActivity;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.network.NetworkChangeReceiver;
import com.screenlocker.secure.networkResponseModels.LoginModel;
import com.screenlocker.secure.networkResponseModels.LoginResponse;
import com.screenlocker.secure.offline.MyAlarmBroadcastReceiver;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.room.MyAppDatabase;
import com.screenlocker.secure.room.migrations.Migration_11_13;
import com.screenlocker.secure.room.migrations.Migration_13_14;
import com.screenlocker.secure.room.migrations.Migration_14_15;
import com.screenlocker.secure.room.migrations.Migration_15_16;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.socket.receiver.AppsStatusReceiver;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;
import com.secureSetting.UtilityFunctions;
import com.secureSetting.t.data.AppItem;
import com.secureSetting.t.data.DataManager;
import com.secureSetting.t.db.DbHistoryExecutor;
import com.secureSetting.t.db.DbIgnoreExecutor;
import com.secureSetting.t.service.AppService;
import com.secureSetting.t.util.PreferenceManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.ALARM_TIME_COMPLETED;
import static com.screenlocker.secure.utils.AppConstants.CONNECTED;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_NETWORK_CHANGED;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_NETWORK_STATUS;
import static com.screenlocker.secure.utils.AppConstants.LIMITED;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.SYSTEM_LOGIN_TOKEN;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
import static com.screenlocker.secure.utils.AppConstants.URL_1;
import static com.screenlocker.secure.utils.AppConstants.URL_2;
import static com.screenlocker.secure.utils.CommonUtils.isSocketConnected;
import static com.screenlocker.secure.utils.PrefUtils.PREF_FILE;

/**
 * application class to get the database instance
 */
public class MyApplication extends Application implements LinkDeviceActivity.OnScheduleTimerListener {


    public static final String CHANNEL_1_ID = "channel_1_id";
    public static boolean recent = false;
    private MyAppDatabase myAppDatabase;
    private ComponentName compName;
    private DevicePolicyManager devicePolicyManager;
    private LinearLayout screenShotView;
    private ApiOneCaller apiOneCaller;
    private PrefManager preferenceManager;
    private AppsStatusReceiver appsStatusReceiver;
    private ApiUtils apiUtils;
    private long mInterval = 10000; // 10 seconds by default, can be changed later
    private Handler mHandler;


    private static Context appContext;

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

        if (key.equals(CURRENT_NETWORK_CHANGED)) {
            String networkStatus = sharedPreferences.getString(CURRENT_NETWORK_STATUS, LIMITED);

            boolean isConnected = networkStatus.equals(CONNECTED);

            Timber.d("ksdklfgsmksls : " + isConnected);

            if (isConnected) {
                if (!isSocketConnected()) {
                    onlineConnection();
                }
            } else {
                utils.stopSocket(this);
                stopRepeatingTask();
            }
        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        appContext = getApplicationContext();
        PrefUtils.saveStringPref(this, AppConstants.CURRENT_NETWORK_STATUS, AppConstants.LIMITED);

        registerNetworkPref();

        if (LinkDeviceActivity.mListener == null)
            LinkDeviceActivity.mListener = this;

        registerReceiver(myAlarmBroadcastReceiver, new IntentFilter(ALARM_TIME_COMPLETED));


        try {
            Fabric.with(this, new Crashlytics());
        } catch (Exception ignored) {
        }

        compName = new ComponentName(this, MyAdmin.class);
        screenShotView = createScreenShotView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Channel Chat",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Chat channel");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }


        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        AppExecutor.getInstance().getSingleThreadExecutor().submit(() -> {
            myAppDatabase = Room.databaseBuilder(getApplicationContext(), MyAppDatabase.class, AppConstants.DATABASE_NAME)
                    .addMigrations(new Migration_11_13(11, 13),
                            new Migration_13_14(13, 14)
                            , new Migration_14_15(14, 15)
                            , new Migration_15_16(15, 16))
                    .build();
        });
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
        unregisterReceiver(myAlarmBroadcastReceiver);
        unRegisterNetworkPref();
        stopRepeatingTask();
        super.onTerminate();
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

                boolean old_device_status = PrefUtils.getBooleanPref(this, AppConstants.OLD_DEVICE_STATUS);

                Timber.d("LinkStatus :" + linkStatus);
                boolean pendingActivation = PrefUtils.getBooleanPref(this, AppConstants.PENDING_ACTIVATION);
                Timber.d("pendingActivation " + pendingActivation);

                Timber.d("LinkStatus :" + linkStatus);
                String macAddress = DeviceIdUtils.generateUniqueDeviceId(this);
                String serialNo = DeviceIdUtils.getSerialNumber();

                if (!old_device_status) {
                    if (PrefUtils.getBooleanPref(this, TOUR_STATUS)) {
                        if (apiUtils == null)
                            apiUtils = new ApiUtils(MyApplication.this, macAddress, serialNo);
                        apiUtils.connectToSocket();
                        PrefUtils.saveBooleanPref(this, AppConstants.OLD_DEVICE_STATUS, true);
                    }
                }

                if (linkStatus) {
                    if (apiUtils == null)
                        apiUtils = new ApiUtils(MyApplication.this, macAddress, serialNo);
                    apiUtils.connectToSocket();
                } else if (pendingActivation) {
                    if (!isFirst)
                        scheduleTimer();
                    if (apiUtils == null)
                        apiUtils = new ApiUtils(MyApplication.this, macAddress, serialNo);
                    apiUtils.connectToSocket();
                }
//                checkForDownload();

            }
        }, this, urls);// checking hosts

        asyncCalls.execute();
    }

    private boolean isFirst = false;

    private void scheduleTimer() {

        isFirst = true;
        startRepeatingTask();
    }


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
        new Thread(() -> {
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
        }).run();
    }

    @Override
    public void onScheduleTimer(boolean state) {
        if (state) {
            scheduleTimer();
        } else {
            stopRepeatingTask();
        }
    }

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    final Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {

            boolean schedule = true;
            try {
                if (PrefUtils.getBooleanPref(MyApplication.getAppContext(), AppConstants.PENDING_ACTIVATION)) {
                    switch (UtilityFunctions.getNetworkType(MyApplication.this)) {
                        case NetworkCapabilities.TRANSPORT_CELLULAR:
                            mInterval = mInterval + 10000;
                            break;
                        case NetworkCapabilities.TRANSPORT_WIFI:
                            mInterval = 10000;
                            break;
                    }
                    onlineConnection();

                } else {
                    //TODO remove timer from here
                    stopRepeatingTask();
                    schedule = false;
                }
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                if (schedule)
                    mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };
}




