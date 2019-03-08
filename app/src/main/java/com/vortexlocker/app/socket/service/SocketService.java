package com.vortexlocker.app.socket.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.vortexlocker.app.R;
import com.vortexlocker.app.settings.SettingContract;
import com.vortexlocker.app.socket.SocketSingleton;
import com.vortexlocker.app.socket.utils.ApiUtils;
import com.vortexlocker.app.socket.utils.SocketUtils;
import com.vortexlocker.app.utils.AppConstants;
import com.vortexlocker.app.utils.CommonUtils;
import com.vortexlocker.app.utils.PrefUtils;

import timber.log.Timber;

import static com.vortexlocker.app.utils.Utils.getNotification;

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