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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.notifications.NotificationItem;
import com.screenlocker.secure.utils.Utils;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DeviceNotificationListener extends NotificationListenerService {

    private NLServiceReceiver nlservicereciver;

    public static final String ACTION_NOTIFICATION_REFRESH = BuildConfig.APPLICATION_ID + ".notification_refresh";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        refreshList();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        refreshList();
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

    class NLServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("command").equals("clearall")) {
                DeviceNotificationListener.this.cancelAllNotifications();
            }
        }
    }

}