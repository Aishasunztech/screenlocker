package com.screenlocker.secure.socket.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.screenlocker.secure.R;
import com.screenlocker.secure.settings.SettingContract;
import com.screenlocker.secure.socket.SocketSingleton;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import timber.log.Timber;

import static com.screenlocker.secure.socket.SocketSingleton.isSocketConnected;
import static com.screenlocker.secure.utils.Utils.getNotification;

public class SocketService extends Service implements SettingContract.SettingsMvpView {


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Timber.d("<<< socket service created >>>");
        startService();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getAction();
        if (action != null) {
            if (action.equals("refresh")) {
                if (!isSocketConnected()) {
                    String macAddress = CommonUtils.getMacAddress();
                    new ApiUtils(SocketService.this, macAddress);
                }

            }
            if (action.equals("restart")) {
                String device_id = PrefUtils.getStringPref(this, AppConstants.DEVICE_ID);
                SocketSingleton.closeSocket(device_id);
                String macAddress = CommonUtils.getMacAddress();
                new ApiUtils(SocketService.this, macAddress);
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
        super.onDestroy();
    }

}