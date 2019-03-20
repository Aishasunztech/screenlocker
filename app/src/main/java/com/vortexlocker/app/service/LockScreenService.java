package com.vortexlocker.app.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.vortexlocker.app.R;
import com.vortexlocker.app.launcher.AppInfo;
import com.vortexlocker.app.notifications.NotificationItem;
import com.vortexlocker.app.settings.SettingsActivity;
import com.vortexlocker.app.utils.PrefUtils;
import com.vortexlocker.app.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.vortexlocker.app.utils.AppConstants.DEFAULT_MAIN_PASS;
import static com.vortexlocker.app.utils.AppConstants.KEY_MAIN_PASSWORD;

/**
 * this service is the startForeground service to kepp the lock screen going when user lock the phone
 * (must enable service by enabling service from settings screens{@link SettingsActivity#onClick(View)})
 */
public class LockScreenService extends Service {
    @SuppressLint("StaticFieldLeak")
    private static FrameLayout mLayout = null;
    private ScreenOffReceiver screenOffReceiver;
    private BroadcastReceiver notificationRefreshedListener;
    private static List<NotificationItem> notificationItems;

    @Override
    public void onCreate() {

        notificationItems = new ArrayList<>();
        final NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        screenOffReceiver = new ScreenOffReceiver(new ScreenOffReceiver.OnScreenOffListener() {
            @Override
            public void onScreenOff() {
                startLockScreen();
            }
        });

        notificationRefreshedListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<NotificationItem> items = intent.getParcelableArrayListExtra("data");

                if (items != null) {
                    notificationItems.clear();
                    notificationItems.addAll(items);
                    if (mLayout != null) {
//                        ((RecyclerView) mLayout.findViewById(R.id.notification_list)).getAdapter().notifyDataSetChanged(); // <--
                    }
                }
            }
        };


        registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(notificationRefreshedListener,
                        new IntentFilter(DeviceNotificationListener.ACTION_NOTIFICATION_REFRESH));
        PrefUtils.saveToPref(this, true);
        Notification notification = Utils.getNotification(this, R.drawable.ic_lock_black_24dp);

        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        if (mNM != null) {
            mNM.notify(R.string.app_name, notification);
        }

        startForeground(R.string.app_name, notification);


    }

    @Override
    public void onDestroy() {
        Timber.d("screen locker distorting.");
        unregisterReceiver(screenOffReceiver);
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(notificationRefreshedListener);
        PrefUtils.saveToPref(this, false);


        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("screen locker starting.");
        sendBroadcast(new Intent().setAction("com.mediatek.ppl.NOTIFY_LOCK"));
        if (intent != null) {
            String action = intent.getAction();
            Timber.d("locker screen action :%s", action);
            if (action == null) {
                String main_password = PrefUtils.getStringPref(this, KEY_MAIN_PASSWORD);
                if (main_password == null) {
                    PrefUtils.saveStringPref(this, KEY_MAIN_PASSWORD, DEFAULT_MAIN_PASS);
                }
                startLockScreen();
            }
            if (action != null) {
                switch (action) {
                    case "suspended":
                        startLockScreen();
                        break;
                    case "expired":
                        startLockScreen();
                        break;
                    case "reboot":
                        startLockScreen();
                        break;
                    case "unlinked":
                        stopLockScreen();
                        break;
                }
            }

        }
        Timber.i("Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

    private void stopLockScreen() {
        try {
            WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            removeLockScreenView(windowManager);
            if (mLayout != null) {
                mLayout = null;
            }
            if (notificationItems != null) {
                notificationItems.clear();
            }
            stopSelf();
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    private void startLockScreen() {

        try {
            final NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            removeLockScreenView(windowManager);
            if (mLayout == null) {
                mLayout = new FrameLayout(LockScreenService.this);
            }

            notificationItems.clear();


            Log.d("sadklakldfa", "startLockScreen: ");


            if (mNM != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    notificationItems.addAll(Utils.getNotificationItems(mNM.getActiveNotifications()));
                }
            }
            if (windowManager != null) {
                WindowManager.LayoutParams params = Utils.prepareLockScreenView(mLayout,
                        notificationItems, LockScreenService.this);
                windowManager.addView(mLayout, params);
            }
        } catch (Exception e) {
            Timber.d(e);
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void removeLockScreenView(WindowManager manager) {

        try {
            manager.removeView(mLayout);
            mLayout = null;
        } catch (Exception ignored) {

        }
//        if (mLayout != null) {
//            if (manager != null && mLayout.getWindowToken() != null) {
//                try {
//                    manager.removeView(mLayout);
//                    mLayout = null;
//                } catch (IllegalArgumentException ignore) {
//                }
//            }
////            mLayout = null;
//        }
    }




}
