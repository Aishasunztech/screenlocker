package com.screenlocker.secure.app;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;

import com.crashlytics.android.Crashlytics;
import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.R;
import com.screenlocker.secure.crash.CustomErrorActivity;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.mdm.ui.LinkDeviceActivity;
import com.screenlocker.secure.mdm.utils.BluetoothHotSpotChangeReceiver;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.mdm.utils.NetworkChangeReceiver;
import com.screenlocker.secure.networkResponseModels.LoginModel;
import com.screenlocker.secure.networkResponseModels.LoginResponse;
import com.screenlocker.secure.offline.MyAlarmBroadcastReceiver;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.room.MyAppDatabase;
import com.screenlocker.secure.room.migrations.Migration_11_13;
import com.screenlocker.secure.room.migrations.Migration_13_14;
import com.screenlocker.secure.room.migrations.Migration_14_15;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.socket.receiver.AppsStatusReceiver;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;
import com.secureSetting.t.data.AppItem;
import com.secureSetting.t.data.DataManager;
import com.secureSetting.t.db.DbHistoryExecutor;
import com.secureSetting.t.db.DbIgnoreExecutor;
import com.secureSetting.t.service.AppService;
import com.secureSetting.t.util.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.socket.utils.utils.saveLiveUrl;
import static com.screenlocker.secure.utils.AppConstants.ALARM_TIME_COMPLETED;
import static com.screenlocker.secure.utils.AppConstants.CONNECTED;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_NETWORK_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.KEY_BLUETOOTH_ENABLE;
import static com.screenlocker.secure.utils.AppConstants.KEY_HOTSPOT_ENABLE;
import static com.screenlocker.secure.utils.AppConstants.KEY_WIFI_ENABLE;
import static com.screenlocker.secure.utils.AppConstants.LIMITED;
import static com.screenlocker.secure.utils.AppConstants.NEW_DEVICE_STATUS_CHECK;
import static com.screenlocker.secure.utils.AppConstants.PENDING_ACTIVATION;
import static com.screenlocker.secure.utils.AppConstants.SOCKET_STATUS;
import static com.screenlocker.secure.utils.AppConstants.STOP_SOCKET;
import static com.screenlocker.secure.utils.AppConstants.SYSTEM_LOGIN_TOKEN;
import static com.screenlocker.secure.utils.CommonUtils.isSocketConnected;
import static com.screenlocker.secure.utils.PrefUtils.PREF_FILE;

/**
 * application class to get the database instance
 */
public class MyApplication extends Application implements BluetoothHotSpotChangeReceiver.BluetoothHotSpotStateListener, LinkDeviceActivity.OnScheduleTimerListener {

    public static final String CHANNEL_1_ID = "channel_1_id";
    public static boolean recent = false;
    private MyAppDatabase myAppDatabase;
    private ComponentName compName;
    ApiOneCaller apiOneCaller;
    PrefManager preferenceManager;
    AppsStatusReceiver appsStatusReceiver;
    private WifiManager wifimanager;
    private BluetoothAdapter bluetoothAdapter;
    private WifiManager.LocalOnlyHotspotReservation mReservation;


    private static Context appContext;

    private NetworkChangeReceiver networkChangeReceiver;
    private BluetoothHotSpotChangeReceiver bluetoothChangeReceiver;

    private MyAlarmBroadcastReceiver myAlarmBroadcastReceiver;

    public MyApplication() {
    }


    public static Context getAppContext() {
        return appContext;
    }


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

            Timber.d("<<< Network Status Changed >>>");

            if (isConnected) {

                boolean isWifiEnable = PrefUtils.getBooleanPrefWithDefTrue(this, KEY_WIFI_ENABLE);
                if (!isWifiEnable) {
                    wifimanager.setWifiEnabled(false);
                    Toast.makeText(appContext, "Wifi is disabled by admin. ", Toast.LENGTH_SHORT).show();
                }
                Timber.i("------------> Network Connected");

                boolean newDeviceSatatusCheck = PrefUtils.getBooleanPref(this, NEW_DEVICE_STATUS_CHECK);
                boolean linkDeviceStatus = PrefUtils.getBooleanPref(this, DEVICE_LINKED_STATUS);
                boolean isPendingActivation = PrefUtils.getBooleanPref(this, PENDING_ACTIVATION);
                if (!newDeviceSatatusCheck || linkDeviceStatus || isPendingActivation) {

                    if (!isPendingActivation)
                        Timber.i(newDeviceSatatusCheck ? "---------> Device is using first time. " : "----------> Device is already linked. ");
                    else
                        Timber.i("-------------------> Device is in pending Activation state.");

                    if (!newDeviceSatatusCheck) {
                        PrefUtils.saveBooleanPref(this, NEW_DEVICE_STATUS_CHECK, true);
                    }

                    checkDeviceStatus();
                }
            } else {
                Timber.i("----------> Network Disconnected");

                if (isSocketConnected()) {

                    Timber.i("-----------> Socket service is stopping. ");
                    Intent intent = new Intent(this, LockScreenService.class);
                    intent.putExtra(SOCKET_STATUS,STOP_SOCKET);
                    ActivityCompat.startForegroundService(this,intent);

                } else {
                    Timber.i("--------------> Socket Service is already stopped. ");
                }
            }
        }
    };



    @Override
    public void onCreate() {
        super.onCreate();

        appContext = getApplicationContext();
        LinkDeviceActivity.mListener = this;
        wifimanager = (WifiManager) getSystemService(WIFI_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        PrefUtils.saveStringPref(this, AppConstants.CURRENT_NETWORK_STATUS, AppConstants.LIMITED);
        registerNetworkPref();

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
//        networkChangeReceiver.setNetworkChangeListener(this);

        bluetoothChangeReceiver = new BluetoothHotSpotChangeReceiver();
        bluetoothChangeReceiver.setBluetoothListener(this);


        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        registerReceiver(bluetoothChangeReceiver, intentFilter);

        registerReceiver(myAlarmBroadcastReceiver, new IntentFilter(ALARM_TIME_COMPLETED));


        try {
            Fabric.with(this, new Crashlytics());
        } catch (Exception e) {
            e.printStackTrace();
        }

        compName = new ComponentName(this, MyAdmin.class);

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


        Thread thread = new Thread() {
            @Override
            public void run() {
                myAppDatabase = Room.databaseBuilder(getApplicationContext(), MyAppDatabase.class, AppConstants.DATABASE_NAME)
                        .addMigrations(new Migration_11_13(11, 13), new Migration_13_14(13, 14), new Migration_14_15(14, 15))
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

    public static ComponentName getComponent(Context context) {
        return ((MyApplication) context.getApplicationContext()).compName;
    }

    @Override
    public void onTerminate() {
        unregisterReceiver(appsStatusReceiver);
        unregisterReceiver(networkChangeReceiver);
//        networkChangeReceiver.unsetNetworkChangeListener();
        unRegisterNetworkPref();
        unregisterReceiver(myAlarmBroadcastReceiver);
        unregisterReceiver(bluetoothChangeReceiver);
        super.onTerminate();
    }




    private void checkDeviceStatus() {
        String macAddress = DeviceIdUtils.generateUniqueDeviceId(this);

        String serialNo = DeviceIdUtils.getSerialNumber();

        Timber.i("---------> mac address of device : " + macAddress);

        Timber.i("---------> serial number of device : " + serialNo);

        new ApiUtils(this, macAddress, serialNo);
    }


    public static boolean isFailSafe = false;

    // Method to save token
    public static void saveToken(ApiOneCaller apiOneCaller) {

        Timber.d("<<< saving system login token >>>");

        apiOneCaller
                .login(new LoginModel(DeviceIdUtils.getSerialNumber(), DeviceIdUtils.generateUniqueDeviceId(getAppContext()), DeviceIdUtils.getIPAddress(true))).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {

                boolean responseStatus = response.isSuccessful();

                Timber.i("-------------> login api response status : %s", responseStatus);

                if (responseStatus) {
                    if (response.body() != null) {

                        LoginResponse loginResponse = response.body();

                        boolean tokenStatus = loginResponse.isStatus();

                        Timber.i("-----------> login token status :%s ", tokenStatus);

                        saveLiveUrl(isFailSafe);

                        if (tokenStatus) {
                            Timber.i("---------------> Saving login token .");
                            PrefUtils.saveStringPref(appContext, SYSTEM_LOGIN_TOKEN, response.body().getToken());
                        } else {
                            Timber.i("---------------> oops login token not provided by server . :(");
                        }
                    } else {
                        Timber.i("-------------> oops response body is null .");
                    }
                } else {
                    Timber.i("---------------> wrong response code :( .");
                }


            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NotNull Throwable t) {
                Timber.d("onFailure : %s", t.getMessage());

                if (t instanceof UnknownHostException) {
                    Timber.e("-----------> something very dangerous happen with domain : %s", t.getMessage());
                    if (isFailSafe) {
                        Timber.e("------------> FailSafe domain is also not working. ");
                    } else {
                        Timber.i("<<< New Api call with failsafe domain >>>");
                        saveToken(RetrofitClientInstance.getFailSafeInstanceForWhiteLabel());
                        isFailSafe = true;
                    }

                } else if (t instanceof IOException) {
                    Timber.e(" ----> IO Exception :%s", t.getMessage());
                }
            }
        });


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

    private Timer t;

    private void scheduleTimer() {

        if (t != null) {
            t.cancel();
            t = null;
        }

        t = new Timer();

        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Timber.d("zklvnsdfvnsdklfsdfg : " + "checking device status ");
                if (PrefUtils.getBooleanPref(MyApplication.getAppContext(), AppConstants.PENDING_ACTIVATION)) {
                    checkDeviceStatus();
                } else {
                    if (t != null) {
                        t.cancel();
                        t = null;
                    }
                }


            }

        }, 5000, 10 * 60 * 1000);
    }

    private void stopTimer() {
        if (t != null) {
            t.cancel();
            t = null;
        }
        Timber.d("zklvnsdfvnsdklfsdfg : " + "stop Timer");
    }


    @Override
    public void onScheduleTimer(boolean state) {
        Timber.d("zklvnsdfvnsdklfsdfg : " + "state :" + state);
        if (state) {
            scheduleTimer();
        } else {
            stopTimer();
        }

    }

    @Override
    public void isBlueToothEnable(boolean enable) {
        if (enable) {
            boolean isBluetoothEnable = PrefUtils.getBooleanPrefWithDefTrue(this, KEY_BLUETOOTH_ENABLE);
            if (isBluetoothEnable)
                bluetoothAdapter.enable();
            else {
                bluetoothAdapter.disable();
                Toast.makeText(appContext, "Bluetooth is disabled by admin. ", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void isHotspotEnable(boolean enable) {
        Log.d("laskjdf", "checkTest" + enable);
        if (enable) {
            boolean isHotSpotEnable = PrefUtils.getBooleanPref(this, KEY_HOTSPOT_ENABLE);
            if (isHotSpotEnable) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    wifimanager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

                        @Override
                        public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                            super.onStarted(reservation);
                            mReservation = reservation;
                        }

                        @Override
                        public void onStopped() {
                            super.onStopped();
                        }

                        @Override
                        public void onFailed(int reason) {
                            super.onFailed(reason);
                        }
                    }, new Handler());
                }
            } else {
                if (mReservation != null) {
                    mReservation.close();
                }
            }
        }
    }

}




