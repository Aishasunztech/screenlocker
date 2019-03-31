package com.titanlocker.secure.service;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;

import com.titanlocker.secure.BuildConfig;
import com.titanlocker.secure.notifications.NotificationItem;
import com.titanlocker.secure.utils.Utils;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class DeviceNotificationListener extends NotificationListenerService {

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
}