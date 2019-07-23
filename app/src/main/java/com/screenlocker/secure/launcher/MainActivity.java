package com.screenlocker.secure.launcher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStats;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ShareCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.screenlocker.secure.R;
import com.screenlocker.secure.ShutDownReceiver;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.listener.OnAppsRefreshListener;
import com.screenlocker.secure.manual_load.ManualPullPush;
import com.screenlocker.secure.manual_load.ManualPushPullAdapter;
import com.screenlocker.secure.permissions.SteppersActivity;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.service.ScreenOffReceiver;
import com.screenlocker.secure.settings.ManagePasswords;
import com.screenlocker.secure.settings.SettingContract;
import com.screenlocker.secure.settings.codeSetting.installApps.DownLoadAndInstallUpdate;
import com.screenlocker.secure.socket.SocketManager;
import com.screenlocker.secure.socket.model.InstallModel;
import com.screenlocker.secure.socket.service.SocketService;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.updateDB.BlurWorker;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.AppInstallReceiver;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;
import com.secureMarket.MarketFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

import static com.screenlocker.secure.socket.utils.utils.refreshApps;
import static com.screenlocker.secure.utils.AppConstants.BROADCAST_APPS_ACTION;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.FINISHED_PULLED_APPS;
import static com.screenlocker.secure.utils.AppConstants.FINISHED_PUSHED_APPS;
import static com.screenlocker.secure.utils.AppConstants.FINISH_POLICY;
import static com.screenlocker.secure.utils.AppConstants.FINISH_POLICY_APPS;
import static com.screenlocker.secure.utils.AppConstants.FINISH_POLICY_PUSH_APPS;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_IMAGE;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_SUPPORT_IMAGE;
import static com.screenlocker.secure.utils.AppConstants.KEY_SUPPORT_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.LOADING_POLICY;
import static com.screenlocker.secure.utils.AppConstants.PENDING_FINISH_DIALOG;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;

/**
 * this activity is the custom launcher for the app
 */
public class MainActivity extends
        BaseActivity implements
        MainContract.MainMvpView,
        SettingContract.SettingsMvpView,
        RAdapter.ClearCacheListener,
        OnAppsRefreshListener,
        LockScreenService.ServiceCallbacks,
        DownLoadAndInstallUpdate.OnAppAvailable {
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
    public static PolicyRefreshListener policyRefreshListener;


    private ScreenOffReceiver screenOffReceiver;

    public interface PolicyRefreshListener {
        void refreshPolicy();
    }

    public static Activity context;

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LockScreenService.mCallBacks = MainActivity.this;
        DownLoadAndInstallUpdate.onAppAvailable = MainActivity.this;

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


        registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

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


        closeBar();
    }


    private void setRecyclerView() {
        rvApps = findViewById(R.id.rvApps);
        int resId = R.anim.layout_animation;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this, resId);
        rvApps.setLayoutAnimation(animation);

        adapter = new RAdapter(this);
        adapter.appsList = new ArrayList<>();
        rvApps.setLayoutManager(new GridLayoutManager(this, AppConstants.LAUNCHER_GRID_SPAN));
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

            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager != null) {
                int taskId = getTaskId();
                Log.i("sfs", "clearRecentApp: task id is : " + taskId);
                activityManager.moveTaskToFront(taskId, 0);
            }
        } catch (Exception e) {
            Timber.e(e);
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

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        if (!mainPresenter.isServiceRunning() && PrefUtils.getBooleanPref(MainActivity.this, TOUR_STATUS)) {
            Intent lockScreenIntent = new Intent(this, LockScreenService.class);
            mainPresenter.startLockService(lockScreenIntent);
        }

        String msg = PrefUtils.getStringPref(MainActivity.this, AppConstants.CURRENT_KEY);
        if (msg != null && !msg.equals("")) {
            setBackground(msg);
        }

        refreshApps(this);


        Log.i("checkpolicy", "onResume: onreusme called ");

        ComponentName cn;
        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            cn = am.getAppTasks().get(0).getTaskInfo().topActivity;
        } else {
            //noinspection deprecation
            cn = am.getRunningTasks(1).get(0).topActivity;
        }


        super.onResume();
//        allowScreenShot(PrefUtils.getBooleanPref(this, AppConstants.KEY_ALLOW_SCREENSHOT));

        ArrayList<InstallModel> appsList = utils.getArrayList(MainActivity.this);


        if (!PrefUtils.getStringPref(this, CURRENT_KEY).equals(KEY_SUPPORT_PASSWORD)) {

            if (appsList != null) {
                Log.i("checkpolicy", "onResume:  in MainActivity ....... app list size : " + appsList.size());
                if (appsList.size() > 0) {
                    if (!"com.screenlocker.secure.manual_load.ManualPullPush".equals(cn.getClassName())) {


                        for (InstallModel model : appsList) {
                            Log.i("checkpolicy", "onResume: in MainActivity ....... apps call for " + model.getPackage_name());
                        }


                        Intent intent = new Intent(MainActivity.this, ManualPullPush.class);
                        startActivity(intent);
                    }

                }
            } else {
                Log.i("checkpolicy", "onResume: app list null : ");
            }


            Log.i("checkpolicy", "onResume:  component name is : " + cn.getClassName());
        }

        Log.i("checkpolicy", "onResume:  component name is : " + cn.getClassName());


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
                        background.setImageResource(R.raw.audiblack);

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
                        background.setImageResource(R.raw.tower);
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.i("checkfoucsch", "onWindowFocusChanged: " + hasFocus);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(appsBroadcast);
            unregisterReceiver(mShutDownReceiver);
            unregisterReceiver(screenOffReceiver);

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

    @Override
    public void onAppsRefresh() {
        // AppExecutor.getInstance().getMainThread().execute(this::refreshAppsList);
    }

    @Override
    public void onRecentAppKill() {
        clearRecentApp();
    }

    @Override
    public void showPolicyApps(boolean isPolicy, boolean isPulled) {

        Log.i("checkpolicy", "showPolicyApps: in main activity : " + isPolicy + isPulled);

        SocketManager socketMSanager = SocketManager.getInstance();
        String device_id = PrefUtils.getStringPref(MainActivity.this, DEVICE_ID);


        ArrayList<InstallModel> appsList = utils.getArrayList(MainActivity.this);


        ComponentName cn;
        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            cn = am.getAppTasks().get(0).getTaskInfo().topActivity;
        } else {
            //noinspection deprecation
            cn = am.getRunningTasks(1).get(0).topActivity;
        }

        if (!"com.screenlocker.secure.manual_load.ManualPullPush".equals(cn.getClassName())) {

            if (appsList != null && appsList.size() > 0) {
                Intent intent = new Intent(MainActivity.this, ManualPullPush.class);
                startActivity(intent);
            }


        } else if (policyRefreshListener != null) {
            if (appsList != null && appsList.size() > 0) {
                runOnUiThread(() -> policyRefreshListener.refreshPolicy());

            }

        }

        Log.d("socket", "showPolicyApps: acknolgment tags are :    is Pulled... " + isPulled + " ...  is Policy .. " + isPolicy);

        if (isPulled) {

            finishPulledApps(socketMSanager, device_id);

        } else {

            if (isPolicy) {

                if (socketMSanager.getSocket() != null && socketMSanager.getSocket().connected()) {
                    Timber.d("<<< FINISH POLICY PUSH APPS>>>");
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("device_id", device_id);
                        jsonObject.put("status", true);
                        socketMSanager.getSocket().emit(FINISH_POLICY_APPS + device_id, jsonObject);
                        socketMSanager.getSocket().emit(FINISH_POLICY + device_id, jsonObject);
                        finishPolicy(socketMSanager, device_id);

                    } catch (JSONException e) {
                        Timber.d(e);
                    }
                }

            } else {

                finishPushedApps(socketMSanager, device_id);


            }
        }


    }


    public void finishPolicy(SocketManager socketManager, String device_id) {
        Log.d("socket", "finishPolicy: called ... ");
        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
            Timber.d("<<< FINISH POLICY >>>");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("device_id", device_id);
                jsonObject.put("status", true);
                socketManager.getSocket().emit(FINISH_POLICY + device_id, jsonObject);
                PrefUtils.saveBooleanPref(this, LOADING_POLICY, false);
                PrefUtils.saveBooleanPref(this, PENDING_FINISH_DIALOG, true);
                Intent intent = new Intent(FINISH_POLICY);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            } catch (JSONException e) {
                Timber.d(e);
            }
        }
    }


    public void finishPushedApps(SocketManager socketManager, String device_id) {
        Timber.d("<<<Finish pushed apps>>>");
        if (socketManager.getSocket() != null) {
            if (socketManager.getSocket().connected()) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("status", true);
                    socketManager.getSocket().emit(FINISHED_PUSHED_APPS + device_id, jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void finishPulledApps(SocketManager socketManager, String device_id) {
        Timber.d("<<<Finish pulled apps>>>");
        if (socketManager.getSocket() != null) {
            if (socketManager.getSocket().connected()) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("status", true);
                    socketManager.getSocket().emit(FINISHED_PULLED_APPS + device_id, jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void closeBar() {
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
            WindowManager manager = ((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE));
            WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
            localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            localLayoutParams.gravity = Gravity.TOP;
            localLayoutParams.flags =
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |

                            // this is to enable the notification to receive touch events
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |

                            // Draws over status bar
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

            localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            localLayoutParams.height = (int) (40 * getResources().getDisplayMetrics().scaledDensity);
            localLayoutParams.format = PixelFormat.TRANSPARENT;

            RelativeLayout relativeLayout = new RelativeLayout(MainActivity.this);
            manager.addView(relativeLayout, localLayoutParams);
        }
    }


    @Override
    public void onAppDownloadedAndAvailabe(String appName, String uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            if (getPackageManager().canRequestPackageInstalls()) {

                Intent intent = ShareCompat.IntentBuilder.from(MainActivity.this)
                        .setStream(Uri.parse(uri))
                        .setText("text/html")
                        .getIntent()
                        .setAction(Intent.ACTION_VIEW)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .setDataAndType(Uri.parse(uri), "application/vnd.android.package-archive");

//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.parse(PrefUtils.getStringPref(getContext(),APk_URI)), "application/vnd.android.package-archive");
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
                startActivity(intent);

            } else {
                Toast.makeText(MainActivity.this, "Allowed apps to install from unsource", Toast.LENGTH_SHORT).show();

                // getActivity().startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));

                startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName())));

            }
        }
    }
}



