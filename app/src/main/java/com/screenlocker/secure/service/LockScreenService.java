package com.screenlocker.secure.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.screenlocker.secure.R;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.notifications.NotificationItem;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.AppInstallReciever;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.screenlocker.secure.app.MyApplication.getAppContext;
import static com.screenlocker.secure.utils.AppConstants.DEFAULT_MAIN_PASS;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.ONE_DAY_INTERVAL;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
import static com.screenlocker.secure.utils.CommonUtils.setTimeRemaining;
import static com.screenlocker.secure.utils.Utils.refreshKeypad;

/**
 * this service is the startForeground service to kepp the lock screen going when user lock the phone
 * (must enable service by enabling service from settings screens{@link SettingsActivity#onClick(View)})
 */
public class LockScreenService extends Service {
    private RelativeLayout mLayout = null;
    private ScreenOffReceiver screenOffReceiver;
    private BroadcastReceiver notificationRefreshedListener;
    private List<NotificationItem> notificationItems;
    private WindowManager windowManager;




    @Override
    public void onCreate() {

        appExecutor = AppExecutor.getInstance();
        powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

        notificationItems = new ArrayList<>();
        final NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        screenOffReceiver = new ScreenOffReceiver(this::startLockScreen);

        //local
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver, new IntentFilter(AppConstants.BROADCAST_ACTION));

        notificationRefreshedListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<NotificationItem> items = intent.getParcelableArrayListExtra("data");
                if (items != null) {
                    notificationItems.clear();
                    notificationItems.addAll(items);
                }
            }
        };


        registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(notificationRefreshedListener,
                        new IntentFilter(DeviceNotificationListener.ACTION_NOTIFICATION_REFRESH));
        PrefUtils.saveToPref(this, true);
        Notification notification = Utils.getNotification(this, R.drawable.ic_lock_black_24dp);
        ComponentName componentName = new ComponentName(this, UpdateTriggerService.class);

        JobInfo jobInfo = new JobInfo.Builder(1234, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(ONE_DAY_INTERVAL)
                .build();
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        int resultCode = scheduler.schedule(jobInfo);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Timber.d("Job scheduled");
        } else {
            Timber.d("Job scheduling failed");
        }

        startForeground(R.string.app_name, notification);

    }


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (PrefUtils.getBooleanPref(LockScreenService.this, TOUR_STATUS)) {
                sheduleScreenOffMonitor();
            }
        }
    };

    PowerManager powerManager;

    AppExecutor appExecutor;

    private void sheduleScreenOffMonitor() {
        if (appExecutor.getExecutorForSedulingRecentAppKill().isShutdown()) {
            appExecutor.readyNewExecutor();
        }
        appExecutor.getExecutorForSedulingRecentAppKill().execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (!powerManager.isScreenOn()) {
                    Timber.d("is screen off");
                    appExecutor.getMainThread().execute(this::startLockScreen);
                    return;
                }
            }
        });
    }

    @Override
    public void onDestroy() {

        try {
            Timber.d("screen locker distorting.");
            unregisterReceiver(screenOffReceiver);

            LocalBroadcastManager.getInstance(this)
                    .unregisterReceiver(notificationRefreshedListener);
            LocalBroadcastManager.getInstance(this)
                    .unregisterReceiver(broadcastReceiver);
            PrefUtils.saveToPref(this, false);

            Intent intent = new Intent(LockScreenService.this, LockScreenService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        } catch (Exception e) {
            Timber.d(e);
        }


        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("screen locker starting.");


        if (intent != null) {
            String action = intent.getAction();

            Timber.d("locker screen action :%s", action);
            if (action == null) {
                String main_password = PrefUtils.getStringPref(this, KEY_MAIN_PASSWORD);
                if (main_password == null) {
                    PrefUtils.saveStringPref(this, KEY_MAIN_PASSWORD, DEFAULT_MAIN_PASS);
                }
                startLockScreen();
            } else {
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
                        startLockScreen();
                        break;
                    case "unlocked":
                        removeLockScreenView();
                        break;
                    case "locked":
                        startLockScreen();
                        break;
                }
            }
        }


        Timber.i("Received start id " + startId + ": " + intent);
        return START_STICKY;
    }


    private void startLockScreen() {

        try {
            final NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            refreshKeyboard();

            if (mLayout == null) {
                mLayout = new RelativeLayout(LockScreenService.this);
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
            }

        } catch (Exception e) {
            Timber.d(e);
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void removeLockScreenView() {
        setTimeRemaining(getAppContext());
        try {
            if (mLayout != null)
                windowManager.removeView(mLayout);
            mLayout = null;
        } catch (Exception e) {
            Timber.d(e);
        }
    }


    public void refreshKeyboard() {

        setTimeRemaining(getAppContext());
        try {
            if (mLayout != null) {
                View view = mLayout.findViewById(R.id.keypad);
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) mLayout.getLayoutParams();
                refreshKeypad(view);
                windowManager.updateViewLayout(mLayout, params);

            }

        } catch (Exception e) {
            Timber.d(e);
        }
    }


}
