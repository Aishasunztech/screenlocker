package com.titanlocker.secure.launcher;

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
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.titanlocker.secure.MyAdmin;
import com.titanlocker.secure.R;
import com.titanlocker.secure.ShutDownReceiver;
import com.titanlocker.secure.app.MyApplication;
import com.titanlocker.secure.base.BaseActivity;
import com.titanlocker.secure.service.LockScreenService;
import com.titanlocker.secure.utils.AppConstants;
import com.titanlocker.secure.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import static com.titanlocker.secure.utils.PermissionUtils.isAccessGranted;
import static com.titanlocker.secure.utils.Utils.collapseNow;

/**
 * this activity is the custom launcher for the app
 */
public class MainActivity extends BaseActivity implements MainContract.MainMvpView {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        context = MainActivity.this;


        try {
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void run() {
                    checkCurrentProcess();
                }
            }, 0, 100);
            sendBroadcast(new Intent().setAction("com.mediatek.ppl.NOTIFY_LOCK"));

        } catch (Exception ignored) {

        }


        //Remove title bar
        // this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//Remove notification bar
        // this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
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

            mainPresenter.startLockService(lockScreenIntent);

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


            Toast.makeText(this, "oncreate", Toast.LENGTH_SHORT).show();
            //MyApplication.getDevicePolicyManager(this).lockNow();
        }

    }

    @Override
    protected void freezeStatusbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            collapseNow(this);
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
            removeOverlay();
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

        }

    };

    public static WindowManager windowManager = (WindowManager) MyApplication.getAppContext().getSystemService(WINDOW_SERVICE);

    public static RelativeLayout layout = new RelativeLayout(MyApplication.getAppContext());

    public static void drawOverLay() {

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
            if (layout != null && layout.getId() != R.id.splash_id) {
                layout.setId(R.id.splash_id);
                windowManager.addView(layout, params);
            }
        }

    }

    public static void removeOverlay() {
        try {
            if (windowManager != null) {
                if (layout != null) {
                    layout.setId(R.id.splash_id_null);
                    windowManager.removeView(layout);
                }
            }
        } catch (Exception ignored) {

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        String msg = PrefUtils.getStringPref(MainActivity.this, AppConstants.CURRENT_KEY);
        if (msg != null && !msg.equals("")) {
            setBackground(msg);
        }
        allowScreenShot(PrefUtils.getBooleanPref(this, AppConstants.KEY_ALLOW_SCREENSHOT));
    }

    private void setBackground(String message) {
        String bg = "";
        if (!message.equals("")) {
            if (message.equals(AppConstants.KEY_GUEST_PASSWORD)) {
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

    }


    /**
     * have to override this method to bypass the back button click by not calling its super method
     */
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

        }

//        ActivityManager activityManager = (ActivityManager) getApplicationContext()
//                .getSystemService(Context.ACTIVITY_SERVICE);
//        activityManager.moveTaskToFront(getTaskId(), 0);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void checkCurrentProcess() {

        try {
            if (isAccessGranted(MainActivity.this)) {
                String currentProcess = retriveNewApp();
                if (currentProcess.contains("com.android.systemui")) {
                    clearRecentApp();
                }

            } else {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivity(intent);
            }

        } catch (Exception ignored) {
        }

    }

    private String retriveNewApp() {

        if (Build.VERSION.SDK_INT >= 21) {
            String currentApp = "test";
            UsageStatsManager usm = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            }
            long time = System.currentTimeMillis();
            List<UsageStats> applist = null;
            if (usm != null) {
                applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
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

            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            String mm = null;
            if (manager != null) {
                mm = (manager.getRunningTasks(1).get(0)).topActivity.getPackageName();
            }
            Log.e("MainActivity", "Current App in foreground is: " + mm);
            return mm;
        }
    }

}



