package com.screenlocker.secure.service;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.notifications.NotificationItem;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.Utils;
import com.secureSetting.t.AppConst;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DeviceNotificationListener extends NotificationListenerService {

    private NLServiceReceiver nlservicereciver;
    public static final String TAG = NLServiceReceiver.class.getSimpleName();

    public static final String ACTION_NOTIFICATION_REFRESH = BuildConfig.APPLICATION_ID + ".notification_refresh";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Log.d(TAG, "onNotificationPosted: "+sbn.getPackageName());
        if (sbn.getPackageName().equals("com.armorsec.armor1")){
            Intent intent = new Intent(AppConstants.BROADCAST_ACTION_NOTIFICATION);
            intent.putExtra("isShow",true);
            sendBroadcast(intent);
        }
        //StatusBarNotification[] notifications = getActiveNotifications();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        Log.d(TAG, "onNotificationRemoved: "+sbn.getPackageName());
        StatusBarNotification[] notifications = getActiveNotifications();
        for (StatusBarNotification notification : notifications) {
            if (notification.getPackageName().equals("com.armorsec.armor1")){
                return;
            }
        }
        Intent intent = new Intent(AppConstants.BROADCAST_ACTION_NOTIFICATION);
        intent.putExtra("isShow",false);
        sendBroadcast(intent);

        
    }

    @Override
    public void onCreate() {
        super.onCreate();
        nlservicereciver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.clearNotificaiton.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
        registerReceiver(nlservicereciver,filter);
    }

    private void refreshList() {
        try {
            List<NotificationItem> notifications = Utils.getNotificationItems(getActiveNotifications());
            Intent intent = new Intent(ACTION_NOTIFICATION_REFRESH);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("data", new ArrayList<>(notifications));
            intent.putExtras(bundle);
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(intent);
        } catch (Exception ignored) {

        }

    }

    @Override
    public void onDestroy() {

        unregisterReceiver(nlservicereciver);
        super.onDestroy();
    }

    class NLServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ");
            if (intent.getStringExtra("command").equals("clearall")) {
                DeviceNotificationListener.this.cancelAllNotifications();
            }
        }
    }

}