package com.screenlocker.secure.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.appSelection.AppSelectionActivity;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.notifications.NotificationItem;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.socket.interfaces.GetApplications;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import timber.log.Timber;

import static com.screenlocker.secure.app.MyApplication.getAppContext;
import static com.screenlocker.secure.launcher.MainActivity.clearRecentApp;
import static com.screenlocker.secure.launcher.MainActivity.drawOverLay;
import static com.screenlocker.secure.launcher.MainActivity.getCurrentApp;
import static com.screenlocker.secure.utils.AppConstants.APPS_SETTING_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.DEFAULT_MAIN_PASS;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.CommonUtils.setTimeRemaining;
import static com.screenlocker.secure.utils.LifecycleReceiver.BACKGROUND;
import static com.screenlocker.secure.utils.LifecycleReceiver.LIFECYCLE_ACTION;
import static com.screenlocker.secure.utils.LifecycleReceiver.STATE;

/**
 * this service is the startForeground service to kepp the lock screen going when user lock the phone
 * (must enable service by enabling service from settings screens{@link SettingsActivity#onClick(View)})
 */
public class LockScreenService extends Service implements GetApplications {
    @SuppressLint("StaticFieldLeak")
    private static RelativeLayout mLayout = null;
    private ScreenOffReceiver screenOffReceiver;
    private ScreenOnReceiver screenOnReceiver;
    private BroadcastReceiver notificationRefreshedListener;
    private static List<NotificationItem> notificationItems;


    private static HashSet<String> blackPackages = new HashSet<>();


    PowerManager powerManager;

    AppExecutor appExecutor;

    @Override
    public void onCreate() {

        blackPackages.add("com.rim.mobilefusion.client");
        blackPackages.add("");


//        blackPackages.add("com.android.settings");


        notificationItems = new ArrayList<>();
        final NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

        appExecutor = AppExecutor.getInstance();

        sheduleOwerLayWindow();
        sheduleRecentAppsKill();

        screenOnReceiver = new ScreenOnReceiver(new ScreenOnReceiver.OnScreenOnListener() {
            @Override
            public void onScreenOn() {
                sheduleOwerLayWindow();
                sheduleRecentAppsKill();
            }
        });


        screenOffReceiver = new ScreenOffReceiver(new ScreenOffReceiver.OnScreenOffListener() {
            @Override
            public void onScreenOff() {
                startLockScreen();
                clearRecentApp();
//                try {
//                    // cancel black apps thread
//                    if (timer != null) {
//                        timer.cancel();
//                    }
//                } catch (Exception ignored) {
//                }
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
        registerReceiver(screenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(notificationRefreshedListener,
                        new IntentFilter(DeviceNotificationListener.ACTION_NOTIFICATION_REFRESH));
        PrefUtils.saveToPref(this, true);
        Notification notification = Utils.getNotification(this, R.drawable.ic_lock_black_24dp);

//        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
//        if (mNM != null) {
//            mNM.notify(R.string.app_name, notification);
//        }

//
        startForeground(R.string.app_name, notification);

    }

    private void sheduleOwerLayWindow() {
        appExecutor.getSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (!powerManager.isScreenOn()) {
                        appExecutor.getMainThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                drawOverLay();
                            }
                        });
                        return;
                    }
                }
            }
        });
    }

    private void sheduleRecentAppsKill() {
        appExecutor.getSecondSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (powerManager.isScreenOn()) {
                        if (blackPackages.contains(getCurrentApp())) {
                            clearRecentApp();
                        }
                    } else {
                        return;
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {

        Timber.d("screen locker distorting.");
        unregisterReceiver(screenOffReceiver);
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(notificationRefreshedListener);
        PrefUtils.saveToPref(this, false);
        unregisterReceiver(screenOnReceiver);

        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("screen locker starting.");

        try {
            sendBroadcast(new Intent().setAction("com.mediatek.ppl.NOTIFY_LOCK"));
        } catch (Exception ignored) {
        }

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
                mLayout = new RelativeLayout(LockScreenService.this);
            }
            notificationItems.clear();
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
        setTimeRemaining(getAppContext());

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


    private void getApps() {

//        appExecutor.getSecondSingleThreadExecutor().shutdown();

        new Thread() {
            @Override
            public void run() {
                MyApplication.getAppDatabase(LockScreenService.this).getDao().getApps();
            }
        }.start();
    }

    @Override
    public void onAppsReady(List<AppInfo> appList) {

    }
}
