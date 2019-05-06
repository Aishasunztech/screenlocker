package com.screenlocker.secure.app;

import android.app.Application;
import android.app.admin.DevicePolicyManager;

import androidx.room.Room;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.snackbar.Snackbar;
import com.screenlocker.secure.MyAdmin;

import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.room.MyAppDatabase;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.settings.codeSetting.Models.IMEIModel;
import com.screenlocker.secure.socket.interfaces.NetworkListener;
import com.screenlocker.secure.socket.receiver.MdmEventReciever;
import com.screenlocker.secure.socket.receiver.NetworkReceiver;
import com.screenlocker.secure.socket.service.SocketService;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;


import java.util.List;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * application class to get the database instance
 */
public class MyApplication extends Application implements NetworkListener {


    public static boolean recent = false;
    private MyAppDatabase myAppDatabase;
    private ComponentName compName;
    private DevicePolicyManager devicePolicyManager;
    private LinearLayout screenShotView;
    ApiOneCaller apiOneCaller;
    PrefManager preferenceManager;
    NetworkReceiver networkReceiver;

    MdmEventReciever mdmEventReciever;


    private static Context appContext;

    private LinearLayout createScreenShotView() {
        LinearLayout linearLayout = new LinearLayout(this);
        View btn = new View(this);
        linearLayout.addView(btn);
        return linearLayout;
    }

    public static Context getAppContext() {
        return appContext;
    }


    BroadcastReceiver broadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        appContext = getApplicationContext();


        try {
            Fabric.with(this, new Crashlytics());
            sendBroadcast(new Intent().setAction("com.mediatek.ppl.NOTIFY_LOCK"));
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
        networkReceiver = new NetworkReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
        mdmEventReciever = new MdmEventReciever();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.secureportal.barryapp.app_linked_success");
        registerReceiver(mdmEventReciever, intentFilter);


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
    public void onNetworkChange(boolean status) {
        boolean linkStatus = PrefUtils.getBooleanPref(this, AppConstants.DEVICE_LINKED_STATUS);


        if (linkStatus) {
            Timber.d("ikgiogjrgjrgrgjoi %s", linkStatus);
            Timber.d("ikgiogjrgjrgrgjoi %s", status);

            Intent intent = new Intent(this, SocketService.class);

            if (status) {
                setIMEIstatus();
                String macAddress = CommonUtils.getMacAddress();
                String serialNo = DeviceIdUtils.getSerialNumber();


                if (serialNo != null) {
                    new ApiUtils(MyApplication.this, macAddress, serialNo);
                }
            } else {
                stopService(intent);
            }
        }

    }

    private void setIMEIstatus() {
        Log.d("jksdfh","called");
        boolean imeiChanged = PrefUtils.getBooleanPref(getAppContext(), AppConstants.IMEI_CHANGED);
        if(imeiChanged)
        {
            boolean rebootStatus = PrefUtils.getBooleanPref(getAppContext(),AppConstants.REBOOT_STATUS);
            if(rebootStatus) {


                String deviceId = PrefUtils.getStringPref(getAppContext(), AppConstants.DEVICE_ID);
                String serial = DeviceIdUtils.getSerialNumber();
                String mac = DeviceIdUtils.getMacAddress();
                List<String> imei = DeviceIdUtils.getIMEI(getAppContext());
                IMEIModel imeiModel = new IMEIModel(deviceId, serial, mac, imei);

                getApiOneCaller().checkIMEIstatus(imeiModel).enqueue(new Callback<Boolean>() {
                    @Override
                    public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                        if (response.body()) {
                            PrefUtils.saveBooleanPref(getAppContext(), AppConstants.IMEI_CHANGED, false);
                            PrefUtils.saveBooleanPref(getAppContext(),AppConstants.REBOOT_STATUS,false);

                        } else {
                            Log.d("IMEIRESPONSE", response + "");
                        }
                        Log.d("IMEIRESPONSE", response + "");

                    }

                    @Override
                    public void onFailure(Call<Boolean> call, Throwable t) {
                        Log.d("IMEIRESPONSE", t.toString());
                    }
                });
            }
        }
    }

    @Override
    public void onTerminate() {
        unregisterReceiver(networkReceiver);
        unregisterReceiver(mdmEventReciever);

        super.onTerminate();
    }



}
