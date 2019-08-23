package com.screenlocker.secure.launcher;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStats;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.screenlocker.secure.R;
import com.screenlocker.secure.ShutDownReceiver;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.permissions.SteppersActivity;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.settings.SettingContract;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;
import com.secureSetting.t.AppConst;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

import static com.screenlocker.secure.socket.utils.utils.refreshApps;
import static com.screenlocker.secure.utils.AppConstants.BROADCAST_APPS_ACTION;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_IMAGE;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_SUPPORT_IMAGE;
import static com.screenlocker.secure.utils.AppConstants.KEY_SUPPORT_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;

/**
 * this activity is the custom launcher for the app
 */
public class MainActivity extends BaseActivity implements MainContract.MainMvpView, SettingContract.SettingsMvpView, RAdapter.ClearCacheListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    /**
     * adapter for recyclerView to show the apps of system
     */
    private RAdapter adapter;
    /**
     * this is used to get the details of apps of the system
     */

//    public static Activity context = null;
    List<AppInfo> allDbApps;

    PowerManager powerManager;

    AppExecutor appExecutor;

    private MainViewModel viewModel;
    private RecyclerView rvApps;
    private MainPresenter mainPresenter;
    private AppCompatImageView background;
    public static final int RESULT_ENABLE = 11;
    private ShutDownReceiver mShutDownReceiver;


    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        allDbApps = new ArrayList<>();
        setRecyclerView();
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getAllApps().observe(this, appInfos -> {
            allDbApps = appInfos;
            int size = adapter.appsList.size();
            adapter.appsList.clear();
            adapter.notifyItemRangeRemoved(0, --size);
            final String message = PrefUtils.getStringPref(this, CURRENT_KEY);
            setBackground(message);
            mainPresenter.addDataToList(allDbApps, message, adapter);
            runLayoutAnimation();
        });
        if (!PrefUtils.getBooleanPref(this, TOUR_STATUS)) {
            Intent intent = new Intent(this, SteppersActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

        appExecutor = AppExecutor.getInstance();

        LocalBroadcastManager.getInstance(this).registerReceiver(appsBroadcast, new IntentFilter(BROADCAST_APPS_ACTION));


        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        mainPresenter = new MainPresenter(this, new MainModel(this));
        background = findViewById(R.id.background);

        Intent lockScreenIntent = new Intent(this, LockScreenService.class);


        //local
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(AppConstants.BROADCAST_ACTION));
        LocalBroadcastManager.getInstance(this).sendBroadcast(mainPresenter.getSendingIntent());


        try {

            IntentFilter filter = new IntentFilter(Intent.ACTION_SHUTDOWN);
            mShutDownReceiver = new ShutDownReceiver();
            registerReceiver(mShutDownReceiver, filter);

        } catch (Exception ignored) {
        }


        if (PrefUtils.getStringPref(this, AppConstants.KEY_SHUT_DOWN) != null
                && PrefUtils.getStringPref(this, AppConstants.KEY_SHUT_DOWN).equals(AppConstants.VALUE_SHUT_DOWN_TRUE) && PrefUtils.getBooleanPref(this, TOUR_STATUS)) {

            if (!mainPresenter.isServiceRunning()) {
                mainPresenter.startLockService(lockScreenIntent);
            }
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
    }


    private void setRecyclerView() {
        rvApps = findViewById(R.id.rvApps);
        int resId = R.anim.layout_animation;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this, resId);
        rvApps.setLayoutAnimation(animation);

        adapter = new RAdapter(this);
        adapter.appsList = new ArrayList<>();

        rvApps.setAdapter(adapter);
        rvApps.setItemViewCacheSize(30);


    }

    /**
     * reciever to recieve for the action {@link AppConstants#BROADCAST_ACTION}
     */

    public void clearRecentApp() {
        try {
            Intent i = new Intent(MainActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);
        } catch (Exception ignored) {
        }
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {

            adapter.appsList.clear();
            adapter.notifyDataSetChanged();
            clearRecentApp();
            final String message = intent.getStringExtra(AppConstants.BROADCAST_KEY);
            setBackground(message);
            mainPresenter.addDataToList(allDbApps, message, adapter);
            runLayoutAnimation();


        }

    };

    public WindowManager windowManager = (WindowManager) MyApplication.getAppContext().getSystemService(WINDOW_SERVICE);

    public RelativeLayout layout = new RelativeLayout(MyApplication.getAppContext());


    public void removeOverlay() {
        try {
            if (windowManager != null) {
                if (layout != null) {
                    layout.setId(R.id.splash_id_null);
                    windowManager.removeViewImmediate(layout);
                }
            }
        } catch (Exception e) {

            Timber.d("removeOverlay: %s", e.getMessage());
        }

    }

    @Override
    protected void onPause() {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();

        int column_span = PrefUtils.getIntegerPref(this, AppConstants.KEY_COLUMN_SIZE);
        if(column_span == 0)
        {
            column_span = AppConstants.LAUNCHER_GRID_SPAN;
        }
        rvApps.setLayoutManager(new GridLayoutManager(this, column_span));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        if (!mainPresenter.isServiceRunning() && PrefUtils.getBooleanPref(MainActivity.this, TOUR_STATUS)) {
            Intent lockScreenIntent = new Intent(this, LockScreenService.class);
            mainPresenter.startLockService(lockScreenIntent);
        }

        String msg = PrefUtils.getStringPref(MainActivity.this, AppConstants.CURRENT_KEY);
        if (msg != null && !msg.equals("")) {
            setBackground(msg);
        }

        boolean pendingDialog = PrefUtils.getBooleanPref(this,AppConstants.PENDING_ALARM_DIALOG);
        if(pendingDialog)
        {
            String dialogMessage = PrefUtils.getStringPref(this,AppConstants.PENDING_DIALOG_MESSAGE);
            if(!dialogMessage.equals("")){
                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.expiry_alert_online_title))
                        .setMessage(dialogMessage)

                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })

                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                PrefUtils.saveBooleanPref(this,AppConstants.PENDING_ALARM_DIALOG,false);
                PrefUtils.saveStringPref(this,AppConstants.PENDING_DIALOG_MESSAGE,"");
            }
        }

//
        refreshApps(this);
//
    }

    private void runLayoutAnimation() {
        final Context context = rvApps.getContext();
        final LayoutAnimationController controller =
                AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation);

        rvApps.setLayoutAnimation(controller);
        adapter.notifyDataSetChanged();
        //rvApps.invalidate();
        rvApps.scheduleLayoutAnimation();
    }

    @SuppressLint("ResourceType")
    private void setBackground(String message) {

        try {
            String bg;
            if (!message.equals("")) {
                if (message.equals(KEY_MAIN_PASSWORD)) {
                    // for the encrypted user type
                    bg = PrefUtils.getStringPref(MainActivity.this, KEY_MAIN_IMAGE);
                    if (bg == null || bg.equals("")) {
                        //Glide.with(MainActivity.this).load(R.raw.audiblack).apply(new RequestOptions().centerCrop()).into(background);
//                    background.setBackgroundColor(ContextCompat.getColor(this, R.color.encrypted_default_background_color));
                        background.setImageResource(R.raw._12321);

                    } else {
                        //Glide.with(MainActivity.this).load(Integer.parseInt(bg)).apply(new RequestOptions().centerCrop()).into(background);
                        background.setImageResource(Integer.parseInt(bg));
                    }
                } else if (message.equals(KEY_SUPPORT_PASSWORD)) {
                    // for the guest type user
                    bg = PrefUtils.getStringPref(MainActivity.this, KEY_SUPPORT_IMAGE);
                    if (bg == null || bg.equals("")) {
                        background.setImageResource(R.raw.texture);
                        //Glide.with(MainActivity.this).load(R.raw.texture).apply(new RequestOptions().centerCrop()).into(background);

                    } else {
                        background.setImageResource(Integer.parseInt(bg));
                    }

                } else {
                    bg = PrefUtils.getStringPref(MainActivity.this, AppConstants.KEY_GUEST_IMAGE);
                    if (bg == null || bg.equals("")) {
                        background.setImageResource(R.raw._12318);
                        //Glide.with(MainActivity.this).load(R.raw.tower).apply(new RequestOptions().centerCrop()).into(background);

                    } else {
                        background.setImageResource(Integer.parseInt(bg));
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
        //do nothing here
    }


    private BroadcastReceiver appsBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null)
                if (intent.getAction().equals(BROADCAST_APPS_ACTION)) {
                    //refreshAppsList();
                }
        }
    };

   /* private void refreshAppsList() {


        String current_user = PrefUtils.getStringPref(MainActivity.this, CURRENT_KEY);
        adapter.appsList.clear();
        adapter.notifyDataSetChanged();
        Thread t2 = new Thread() {
            @Override
            public void run() {
                mainPresenter.addDataToList(current_user, adapter);
                runOnUiThread(() -> adapter.notifyDataSetChanged());
            }
        };
        t2.start();


    }*/


    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(appsBroadcast);
            unregisterReceiver(mShutDownReceiver);

        } catch (Exception ignored) {
            //
        }

    }

    @Override
    public void clearCache(Context context) {
        final KProgressHUD hud = KProgressHUD.create(MainActivity.this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(getResources().getString(R.string.clearing_cache))
                .setCancellable(false)
                .setAnimationSpeed(2)
                .setDimAmount(0.5f);
        hud.show();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                clearRecenttasks();
                clearNotif();
                appCache();
                runOnUiThread(() -> {
                    hud.dismiss();
                    clearCacheSuccess();
                });
            }
        }, 2000);
    }


    private void clearRecenttasks() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<UsageStats> list = CommonUtils.getCurrentApp(this);
        if (list != null) {
            for (UsageStats usageStats : list) {
                if (!usageStats.getPackageName().equals(getPackageName()))
                    assert activityManager != null;
                activityManager.killBackgroundProcesses(usageStats.getPackageName());
            }
        }
    }

    private void clearNotif() {
        Intent i = new Intent("com.example.clearNotificaiton.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
        i.putExtra("command", "clearall");
        sendBroadcast(i);
    }


    private void appCache() {
        try {
            Intent intent = new Intent("com.freeme.intent.action.pfw.STOPPED_PACKAGE");
            sendBroadcast(intent);
        } catch (Exception ignored) {


        }
    }

    private void clearCacheSuccess() {

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getResources().getString(R.string.clearing_cache_successfull));
        alertDialog.setIcon(R.drawable.ic_checked);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {
            alertDialog.dismiss();

        });

        alertDialog.show();
    }

//    @Override
//    public void onAppsRefresh() {
//        // AppExecutor.getInstance().getMainThread().execute(this::refreshAppsList);
//    }
}



