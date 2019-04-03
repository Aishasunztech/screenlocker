package com.screenlocker.secure.launcher;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.R;
import com.screenlocker.secure.ShutDownReceiver;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.permissions.StepperActivity;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.settings.SettingContract;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.settings.SettingsModel;
import com.screenlocker.secure.settings.SettingsPresenter;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.screenlocker.secure.utils.AppConstants.DEFAULT_GUEST_PASS;
import static com.screenlocker.secure.utils.AppConstants.DEFAULT_MAIN_PASS;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;

/**
 * this activity is the custom launcher for the app
 */
public class MainActivity extends BaseActivity implements MainContract.MainMvpView, SettingContract.SettingsMvpView {
    private static final String TAG = MainActivity.class.getSimpleName();
    /**
     * adapter for recyclerView to show the apps of system
     */
    private RAdapter adapter;
    /**
     * this is used to get the details of apps of the system
     */

    public static Activity context = null;

    private PackageManager pm;
    private MainPresenter mainPresenter;
    private AppCompatImageView background;
    public static final int RESULT_ENABLE = 11;
    public static ActivityManager activityManager;

    private SettingsPresenter settingsPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        settingsPresenter = new SettingsPresenter(this, new SettingsModel(this));


        boolean tour_status = PrefUtils.getBooleanPref(this, TOUR_STATUS);

        if (!tour_status) {
            if (settingsPresenter.isMyLauncherDefault()) {
                try {

                    String key_guest = PrefUtils.getStringPref(this, KEY_GUEST_PASSWORD);
                    if (key_guest == null)
                        PrefUtils.saveStringPref(this, KEY_GUEST_PASSWORD, DEFAULT_GUEST_PASS);
                    String key_main = PrefUtils.getStringPref(this, KEY_MAIN_PASSWORD);
                    if (key_main == null)
                        PrefUtils.saveStringPref(this, KEY_MAIN_PASSWORD, DEFAULT_MAIN_PASS);
                    PrefUtils.saveBooleanPref(this, TOUR_STATUS, true);
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                    StepperActivity.activity.finish();
                    finish();
                } catch (Exception ignored) {
                }

            }
        }


        context = MainActivity.this;


        try {
            sendBroadcast(new Intent().setAction("com.mediatek.ppl.NOTIFY_LOCK"));
        } catch (Exception ignored) {

        }

        //Remove title bar
        // this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//Remove notification bar
        // this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName compName = new ComponentName(this, MyAdmin.class);
        mainPresenter = new MainPresenter(this, new MainModel(this));
        background = findViewById(R.id.background);
        pm = getPackageManager();
        //        LockScreenService lockScreenService = new LockScreenService();
        Intent lockScreenIntent = new Intent(this, LockScreenService.class);

        // if service is  running make it run
//        if (mainPresenter.isServiceRunning() && PrefUtils.getStringPref(this, AppConstants.KEY_MAIN_PASSWORD) != null) {
//            PrefUtils.saveToPref(this, true);
//            mainPresenter.startLockService(lockScreenIntent);
//            Toast.makeText(lockScreenService, "service is running now", Toast.LENGTH_SHORT).show();
//            ((KeyguardManager) getSystemService(KEYGUARD_SERVICE)).newKeyguardLock("IN").disableKeyguard();
//        }

        setRecyclerView();

        //local
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(AppConstants.BROADCAST_ACTION));
        LocalBroadcastManager.getInstance(this).sendBroadcast(mainPresenter.getSendingIntent());

        IntentFilter filter = new IntentFilter(Intent.ACTION_SHUTDOWN);
        ShutDownReceiver mShutDownReceiver = new ShutDownReceiver();
        registerReceiver(mShutDownReceiver, filter);

        //      Toast.makeText(this, " "+PrefUtils.getStringPref(this, AppConstants.KEY_SHUT_DOWN), Toast.LENGTH_LONG).show();

        if (PrefUtils.getStringPref(this, AppConstants.KEY_SHUT_DOWN) != null
                && PrefUtils.getStringPref(this, AppConstants.KEY_SHUT_DOWN).equals(AppConstants.VALUE_SHUT_DOWN_TRUE)) {

            if (!mainPresenter.isServiceRunning()) {
                mainPresenter.startLockService(lockScreenIntent);
            }
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                MyService.enqueueWork(MainActivity.this, new Intent());
//            }
            try {
                sendBroadcast(new Intent().setAction("com.mediatek.ppl.NOTIFY_LOCK"));
            } catch (Exception ignored) {

            }
            //  boolean isActive = MyApplication.getDevicePolicyManager(this).isAdminActive(MyApplication.getComponent(this));
            if (!PrefUtils.getBooleanPref(this, AppConstants.KEY_ADMIN_ALLOWED)) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, MyApplication.getComponent(this));
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why we need this permission");
                startActivityForResult(intent, RESULT_ENABLE);

            } else {
                if (devicePolicyManager != null) {
                    devicePolicyManager.lockNow();
                }
            }


            //MyApplication.getDevicePolicyManager(this).lockNow();
        }

    }


    private void setRecyclerView() {
        RecyclerView rvApps = findViewById(R.id.rvApps);
        adapter = new RAdapter();
        adapter.appsList = new ArrayList<>();
        rvApps.setLayoutManager(new GridLayoutManager(this, AppConstants.LAUNCHER_GRID_SPAN));
        rvApps.setAdapter(adapter);
    }

    /**
     * reciever to recieve for the action {@link AppConstants#BROADCAST_ACTION}
     */

    public static void clearRecentApp() {

        try {
            ActivityManager activityManager = (ActivityManager) MyApplication.getAppContext()
                    .getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager != null) {
                activityManager.moveTaskToFront(context.getTaskId(), 0);
            }
        } catch (Exception ignored) {
        }
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {

            final String message = intent.getStringExtra(AppConstants.BROADCAST_KEY);
            setBackground(message);

            adapter.appsList.clear();
            adapter.notifyDataSetChanged();

            Thread t2 = new Thread() {
                @Override
                public void run() {
                    mainPresenter.addDataToList(pm, message, adapter);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            };
            t2.start();

            removeOverlay();

        }

    };

    private static boolean view_status = false;

    public static WindowManager windowManager = (WindowManager) MyApplication.getAppContext().getSystemService(WINDOW_SERVICE);

    public static RelativeLayout layout = new RelativeLayout(MyApplication.getAppContext());

    public static void drawOverLay() {

        try {
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
                            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT);
            params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            params.gravity = Gravity.CENTER;


            if (layout != null) {
                layout.setBackgroundColor(MyApplication.getAppContext().getResources().getColor(R.color.colorPrimary));
            }


            if (windowManager != null) {
                if (layout != null && layout.getId() != R.id.splash_id && !view_status) {
                    layout.setId(R.id.splash_id);
                    windowManager.addView(layout, params);
                    view_status = true;
                    Log.d("dogrikog", "drawOverLay: ");
                }
            }
        } catch (Exception ignored) {

        }


    }

    public static void removeOverlay() {
        try {
            if (windowManager != null) {
                if (layout != null) {
                    layout.setId(R.id.splash_id_null);
                    windowManager.removeViewImmediate(layout);
                    view_status = false;
                }
            }
        } catch (Exception ignored) {

        }

    }

    @Override
    protected void onResume() {
        super.onResume();


        Intent lockScreenIntent = new Intent(this, LockScreenService.class);
        if (!mainPresenter.isServiceRunning()) {
            mainPresenter.startLockService(lockScreenIntent);
        }

        String msg = PrefUtils.getStringPref(MainActivity.this, AppConstants.CURRENT_KEY);
        if (msg != null && !msg.equals("")) {
            setBackground(msg);
        }
        allowScreenShot(PrefUtils.getBooleanPref(this, AppConstants.KEY_ALLOW_SCREENSHOT));
    }

    private void setBackground(String message) {

        try {
            String bg = "";
            if (!message.equals("")) {
                if (message.equals(KEY_GUEST_PASSWORD)) {
                    // for the guest type user
                    bg = PrefUtils.getStringPref(MainActivity.this, AppConstants.KEY_GUEST_IMAGE);
                    if (bg == null || bg.equals("")) {

                        Glide.with(MainActivity.this).load(R.drawable.guest_space).apply(new RequestOptions().centerCrop()).into(background);

                    } else {
                        Glide.with(MainActivity.this)
                                .load(bg)
                                .apply(new RequestOptions().centerCrop())
                                .into(background);
                    }
                } else {
                    // for the encrypted user type
                    bg = PrefUtils.getStringPref(MainActivity.this, AppConstants.KEY_MAIN_IMAGE);
                    if (bg == null || bg.equals("")) {

                        Glide.with(MainActivity.this).load(R.drawable.default_background).apply(new RequestOptions().centerCrop()).into(background);
//                    background.setBackgroundColor(ContextCompat.getColor(this, R.color.encrypted_default_background_color));

                    } else {
                        Glide.with(MainActivity.this)
                                .load(bg)
                                .apply(new RequestOptions().centerCrop())
                                .into(background);
                    }
                }
            }
        } catch (Exception ignored) {
        }


    }


    /**
     * have to override this method to bypass the back button click by not calling its super method
     */
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }


//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void checkCurrentProcess() {
//        try {
//            if (isAccessGranted(MainActivity.this)) {
//                String currentProcess = retriveNewApp();
//                if (currentProcess.contains("com.android.systemui")) {
//                    clearRecentApp();
//                }
//
//            } else {
//                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
//                startActivity(intent);
//            }
//
//        } catch (Exception ignored) {
//        }
//
//    }

    public static String getCurrentApp() {
        String dum = "hello";
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                String currentApp = "test";
                UsageStatsManager usm = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                    usm = (UsageStatsManager) SettingsActivity.setting_context.getSystemService(Context.USAGE_STATS_SERVICE);
                }
                long time = System.currentTimeMillis();
                List<UsageStats> applist = null;
                if (usm != null) {
                    applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, time - 86400000, time);
                }
                if (applist != null && applist.size() > 0) {
                    SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();

                    for (UsageStats usageStats : applist) {
                        mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                    }
                    if (!mySortedMap.isEmpty()) {
                        currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                    }
                }
                return currentApp;
            } else {
                ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                String mm = "test";
                if (manager != null) {
                    mm = (manager.getRunningTasks(1).get(0)).topActivity.getPackageName();
                }
                return mm;
            }
        } catch (Exception ignored) {

            return dum;
        }
    }

}



