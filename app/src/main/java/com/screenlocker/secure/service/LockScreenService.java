package com.screenlocker.secure.service;

import android.app.Activity;
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
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.notifications.NotificationItem;
import com.screenlocker.secure.offline.CheckExpiryFromSuperAdmin;
import com.screenlocker.secure.permissions.WelcomeScreenActivity;
import com.screenlocker.secure.room.SimEntry;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.updateDB.BlurWorker;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.AppInstallReciever;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import timber.log.Timber;

import static com.screenlocker.secure.app.MyApplication.getAppContext;
import static com.screenlocker.secure.utils.AppConstants.ALLOW_ENCRYPTED_ALL;
import static com.screenlocker.secure.utils.AppConstants.ALLOW_GUEST_ALL;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.DEFAULT_MAIN_PASS;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.ONE_DAY_INTERVAL;
import static com.screenlocker.secure.utils.AppConstants.SIM_0_ICCID;
import static com.screenlocker.secure.utils.AppConstants.SIM_1_ICCID;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
import static com.screenlocker.secure.utils.CommonUtils.setTimeRemaining;
import static com.screenlocker.secure.utils.Utils.refreshKeypad;
import static com.screenlocker.secure.utils.Utils.scheduleExpiryCheck;

/**
 * this service is the startForeground service to kepp the lock screen going when user lock the phone
 * (must enable service by enabling service from settings screens{@link SettingsActivity#onClick(View)})
 */
public class LockScreenService extends Service {
    private RelativeLayout mLayout = null;
    private ScreenOffReceiver screenOffReceiver;
    private List<NotificationItem> notificationItems;
    private WindowManager windowManager;
    private FrameLayout frameLayout;
    private final IBinder binder = new LocalBinder();
    private boolean isLayoutAdded = false;


    public class LocalBinder extends Binder {
        public LockScreenService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LockScreenService.this;
        }
    }


    private void updateDb(Activity context) {

        OneTimeWorkRequest insertionWork =
                new OneTimeWorkRequest.Builder(BlurWorker.class)
                        .build();
        WorkManager.getInstance().enqueue(insertionWork);
//
//        WorkManager.getInstance().getWorkInfoByIdLiveData(insertionWork.getId())
//                .observe(context, workInfo -> {
//                    // Do something with the status
//                    if (workInfo != null && workInfo.getState().isFinished()) {
//
//                    }
//                });
    }

    @Override
    public void onCreate() {

        OneTimeWorkRequest insertionWork =
                new OneTimeWorkRequest.Builder(BlurWorker.class)
                        .build();
        WorkManager.getInstance().enqueue(insertionWork);


        ComponentName componentName1 = new ComponentName(this, CheckExpiryFromSuperAdmin.class);

        JobInfo jobInfo1 = new JobInfo.Builder(1345, componentName1)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(ONE_DAY_INTERVAL)
                .build();

        JobScheduler scheduler1 = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode1 = scheduler1.schedule(jobInfo1);
        if (resultCode1 == JobScheduler.RESULT_SUCCESS) {
            Timber.d("Job Scheduled");
        } else {
            Timber.d("Job Scheduled Failed");
        }

        ComponentName componentName = new ComponentName(this, CheckUpdateService.class);

        JobInfo jobInfo = new JobInfo.Builder(1234, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(ONE_DAY_INTERVAL)
                .build();

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);

        int resultCode = scheduler.schedule(jobInfo);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Timber.d("Job Scheduled");
        } else {
            Timber.d("Job Scheduled Failed");
        }


        appExecutor = AppExecutor.getInstance();
        frameLayout = new FrameLayout(this);

        powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

        notificationItems = new ArrayList<>();
        final NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        scheduleExpiryCheck(this);
        screenOffReceiver = new ScreenOffReceiver(this::startLockScreen);

        //local
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver, new IntentFilter(AppConstants.BROADCAST_ACTION));
        registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        PrefUtils.saveToPref(this, true);
        Notification notification = Utils.getNotification(this, R.drawable.ic_lock_black_24dp);


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
                        simPermissionsCheck();
                        break;
                    case "locked":
                        startLockScreen();
                        break;
                }
            }
        }

//        stopCapture();


        Timber.i("Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

    public void stopCapture() {
        int windowType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            windowType = WindowManager.LayoutParams.TYPE_TOAST |
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        } else {
            windowType = WindowManager.LayoutParams.TYPE_PHONE;
        }


        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                windowType,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_SECURE
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,

                PixelFormat.TRANSLUCENT);

        if (!isLayoutAdded) {

            windowManager.addView(frameLayout, params);
            isLayoutAdded = true;
        }
    }

    public void startCapture() {
        if (isLayoutAdded) {
            windowManager.removeViewImmediate(frameLayout);
            isLayoutAdded = false;
        }
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
                    try {
                        Intent i = new Intent(LockScreenService.this, MainActivity.class);
//i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(i);
                    } catch (Exception ignored) {


                    }
                }
            }

        } catch (Exception e) {
            Timber.e(e);
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void removeLockScreenView() {
        if (!PrefUtils.getStringPref(this, CURRENT_KEY).equals(AppConstants.KEY_SUPPORT_PASSWORD))
            setTimeRemaining(getAppContext());
        try {
            if (mLayout != null)
                windowManager.removeView(mLayout);
            mLayout = null;
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    private void simPermissionsCheck() {
        String iccid0 = PrefUtils.getStringPref(this, SIM_0_ICCID);
        String iccid1 = PrefUtils.getStringPref(this, SIM_1_ICCID);
        String space = PrefUtils.getStringPref(this, CURRENT_KEY);
        AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
            List<SimEntry> simEntries = MyApplication.getAppDatabase(this).getDao().getAllSimInService();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                assert simEntries != null;
                Optional<SimEntry> op = simEntries.stream()
                        .filter(a -> a.getIccid().equals(iccid0))
                        .findAny();
                if (op.isPresent()) {
                    SimEntry entry1 = op.get();
                    spaceWiseEnableDisable(space, entry1, 0);
                } else {
                    byDefaultBehaviour(space, 0);
                }
                Optional<SimEntry> op1 = simEntries.stream()
                        .filter(a -> a.getIccid().equals(iccid1))
                        .findAny();
                if (op1.isPresent()) {
                    SimEntry entry1 = op1.get();
                    spaceWiseEnableDisable(space, entry1, 1);
                } else {
                    byDefaultBehaviour(space, 1);
                }
            }
        });

    }

    private void byDefaultBehaviour(String space, int slot) {
        switch (space) {
            case KEY_GUEST_PASSWORD:
                if (PrefUtils.getBooleanPrefWithDefTrue(this, ALLOW_GUEST_ALL)) {
                    broadCastIntent(true, slot);
                } else {
                    broadCastIntent(false, slot);
                }
                break;
            case KEY_MAIN_PASSWORD:
                if (PrefUtils.getBooleanPrefWithDefTrue(this, ALLOW_ENCRYPTED_ALL)) {
                    broadCastIntent(true, slot);
                } else {
                    broadCastIntent(false, slot);
                }
                break;
        }
    }

    private void spaceWiseEnableDisable(String space, SimEntry entry1, int slot) {
        switch (space) {
            case KEY_GUEST_PASSWORD:
                if (entry1.isEnable()) {
                    if (entry1.isGuest()) {
                        //enable sim slot 1 for this user
                        broadCastIntent(true, slot);
                    } else {
                        broadCastIntent(false, slot);
                        //disable sim slote for this user
                    }
                } else {
                    //disable in any case
                    broadCastIntent(false, slot);
                }
                break;
            case KEY_MAIN_PASSWORD:
                if (entry1.isEnable()) {
                    if (entry1.isEncrypted()) {
                        //enable sim slot 1 for this user
                        broadCastIntent(true, slot);
                    } else {
                        //disable sim slote for this user
                        broadCastIntent(false, slot);

                    }
                } else {
                    //disable in any case
                    broadCastIntent(false, slot);

                }
                break;

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

    void broadCastIntent(boolean enabled, int slot) {
        Intent intent = new Intent("com.secure.systemcontrol.SYSTEM_SETTINGS");
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.putExtra("isEnabled", enabled);
        intent.putExtra("slot", slot);
        intent.setComponent(new ComponentName("com.secure.systemcontrol", "com.secure.systemcontrol.receivers.SettingsReceiver"));
        sendBroadcast(intent);
    }


}
