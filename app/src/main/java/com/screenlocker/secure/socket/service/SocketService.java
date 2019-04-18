package com.screenlocker.secure.socket.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.screenlocker.secure.R;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.settings.SettingContract;
import com.screenlocker.secure.socket.SocketSingleton;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.socket.utils.SocketUtils;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.BROADCAST_APPS_ACTION;
import static com.screenlocker.secure.utils.Utils.getNotification;

public class SocketService extends Service implements SettingContract.SettingsMvpView {


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Timber.d("<<< socket service created >>>");
        LocalBroadcastManager.getInstance(this).registerReceiver(appsBroadcast, new IntentFilter(BROADCAST_APPS_ACTION));
        startService();

    }


    private ApiUtils apiUtils = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
//                if (action.equals("refresh")) {
//                    if (!isSocketConnected()) {
//                        String macAddress = CommonUtils.getMacAddress();
//                        String serialNo = DeviceIdUtils.getSerialNumber();
//                        apiUtils = new ApiUtils(SocketService.this, macAddress, serialNo);
//                    }
//
//                }
                if (action.equals("restart") || action.equals("refresh")) {
                    String device_id = PrefUtils.getStringPref(this, AppConstants.DEVICE_ID);
                    SocketSingleton.closeSocket(device_id);
                    String macAddress = CommonUtils.getMacAddress();
                    String serialNo = DeviceIdUtils.getSerialNumber();
                    apiUtils = new ApiUtils(SocketService.this, macAddress, serialNo);
                }
            }
        }
        Timber.d("<<< socket service started >>>");

        return START_STICKY;
    }

    private void startService() {
        final NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (mNM != null) {
            Notification notification = getNotification(this, R.drawable.sync);
            startForeground(4577, notification);
        }
    }

    @Override
    public void onDestroy() {
        Timber.d("<<< socket service destroyed >>>");
        String device_id = PrefUtils.getStringPref(this, AppConstants.DEVICE_ID);
        SocketSingleton.closeSocket(device_id);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(appsBroadcast);
        super.onDestroy();
    }


    private BroadcastReceiver appsBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null)
                if (intent.getAction().equals(BROADCAST_APPS_ACTION)) {
//                    List<AppInfo> appInfos = intent.getParcelableArrayListExtra("apps_list");
                    if (apiUtils != null) {
                        SocketUtils su = apiUtils.getSocketUtils();
                        if (su != null) {
                            su.setApps();
                        }
                    }
                }
        }
    };

}