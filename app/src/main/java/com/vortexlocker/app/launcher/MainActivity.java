package com.vortexlocker.app.launcher;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.vortexlocker.app.MyAdmin;
import com.vortexlocker.app.R;
import com.vortexlocker.app.ShutDownReceiver;
import com.vortexlocker.app.app.MyApplication;
import com.vortexlocker.app.base.BaseActivity;
import com.vortexlocker.app.service.LockScreenService;
import com.vortexlocker.app.utils.AppConstants;
import com.vortexlocker.app.utils.PrefUtils;

import java.util.ArrayList;

import static com.vortexlocker.app.utils.Utils.collapseNow;

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
    private PackageManager pm;
    private MainPresenter mainPresenter;
    private AppCompatImageView background;
    public static final int RESULT_ENABLE = 11;
    private ActivityManager activityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

            sendBroadcast(new Intent().setAction("com.mediatek.ppl.NOTIFY_LOCK"));

            mainPresenter.startLockService(lockScreenIntent);

            //  boolean isActive = MyApplication.getDevicePolicyManager(this).isAdminActive(MyApplication.getComponent(this));
            if (!PrefUtils.getBooleanPref(this, AppConstants.KEY_ADMIN_ALLOWED)) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, MyApplication.getComponent(this));
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why we need this permission");
                startActivityForResult(intent, RESULT_ENABLE);

            } else {
                devicePolicyManager.lockNow();
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
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            // Get extra data included in the Intent
            final ComponentName cn = activityManager.getRunningTasks(1).get(0).topActivity;
            if (!cn.getClassName().equals(MainActivity.class.getName()))
                activityManager.moveTaskToFront(getTaskId(), 0);
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


}
