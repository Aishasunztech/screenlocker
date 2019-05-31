package com.screenlocker.secure.app;

import android.app.Application;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.view.View;
import android.widget.LinearLayout;

import androidx.room.Room;

import com.crashlytics.android.Crashlytics;
import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.interfaces.AsyncResponse;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.mdm.utils.NetworkChangeReceiver;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.room.MyAppDatabase;
import com.screenlocker.secure.socket.receiver.AppsStatusReceiver;
import com.screenlocker.secure.socket.service.SocketService;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;

/**
 * application class to get the database instance
 */
public class MyApplication extends Application implements NetworkChangeReceiver.NetworkChangeListener , AsyncResponse {


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

    private LinearLayout createScreenShotView() {
        LinearLayout linearLayout = new LinearLayout(this);
        View btn = new View(this);
        linearLayout.addView(btn);
        return linearLayout;
    }

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
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction("com.secure.systemcontroll.PackageAdded");
        filter.addAction("com.secure.systemcontroll.PackageDeleted");
        filter.addAction("com.secure.systemcontrol.PACKAGE_ADDED_SECURE_MARKET");
        registerReceiver(appsStatusReceiver, filter);


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

        super.onTerminate();
    }


    @Override
    public void isConnected(boolean state) {


        if(state){
            new AsyncCalls(this, this).execute();
        }else {
            if(utils.isMyServiceRunning(SocketService.class,appContext)){
                Intent intent = new Intent(this, SocketService.class);
                stopService(intent);
            }
        }
    }

    @Override
    public void processFinish(String output) {

        if(output!=null){
            PrefUtils.saveStringPref(appContext,LIVE_URL,output);
            boolean linkStatus = PrefUtils.getBooleanPref(this, AppConstants.DEVICE_LINKED_STATUS);
            if(linkStatus){
                String macAddress = CommonUtils.getMacAddress();
                String serialNo = DeviceIdUtils.getSerialNumber();

                if (serialNo != null) {
                    new ApiUtils(MyApplication.this, macAddress, serialNo);
                }
            }
        }
    }
}
