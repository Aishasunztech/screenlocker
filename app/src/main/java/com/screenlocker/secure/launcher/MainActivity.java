package com.screenlocker.secure.launcher;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.screenlocker.secure.ShutDownReceiver;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.manual_load.DownloadCompleteListener;
import com.screenlocker.secure.manual_load.ManualPullPush;
import com.screenlocker.secure.permissions.SteppersActivity;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.socket.model.InstallModel;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.secure.launcher.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.IntStream;

import timber.log.Timber;

import static android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS;
import static com.screenlocker.secure.socket.utils.utils.refreshApps;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_IMAGE;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_IMAGE;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_SUPPORT_IMAGE;
import static com.screenlocker.secure.utils.AppConstants.KEY_SUPPORT_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.NUMBER_OF_NOTIFICATIONS;
import static com.screenlocker.secure.utils.AppConstants.SHOW_MANUAL_ACTIVITY;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
import static com.screenlocker.secure.utils.AppConstants.UNINSTALL_ALLOWED;
import static com.screenlocker.secure.utils.PrefUtils.PREF_FILE;

/**
 * this activity is the custom launcher for the app
 */
public class MainActivity extends BaseActivity implements
        RAdapter.ClearCacheListener,
        DownloadCompleteListener {
    private final String TAG = MainActivity.class.getSimpleName();
    /**
     * adapter for recyclerView to show the apps of system
     */
    private RAdapter adapter;
    /**
     * this is used to get the details of apps of the system
     */
    private SharedPreferences sharedPref;
    //    public static Activity context = null;
    List<AppInfo> allDbApps;

    PowerManager powerManager;

    AppExecutor appExecutor;

    private MainViewModel viewModel;
    private RecyclerView rvApps;
    private AppCompatImageView background;
    public static final int RESULT_ENABLE = 11;
    private ShutDownReceiver mShutDownReceiver;


    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_up, R.anim.slide_up);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!prefUtils.getBooleanPref( TOUR_STATUS)) {
            Intent intent = new Intent(this, SteppersActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        LockScreenService.downloadCompleteListener = this;
        sharedPref = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
        allDbApps = new ArrayList<>();
        setRecyclerView();
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getAllApps().observe(this, appInfos -> {
            allDbApps = appInfos;
            int size = adapter.appsList.size();
            adapter.appsList.clear();
            adapter.notifyItemRangeRemoved(0, --size);
            final String message = prefUtils.getStringPref( CURRENT_KEY);
            setBackground(message);
            viewModel.addDataToList(allDbApps, message, adapter);
            runLayoutAnimation();
            if (viewModel.getmUnReadCount().getValue() != null)
                adapter.updateNotificationBadgeOfSL(viewModel.getmUnReadCount().getValue());
        });
        viewModel.getmUnReadCount().observe(this, integer -> adapter.updateNotificationBadgeOfSL(integer));


        powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

        appExecutor = AppExecutor.getInstance();

        background = findViewById(R.id.background);

        Intent lockScreenIntent = new Intent(this, LockScreenService.class);


        //local
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(AppConstants.BROADCAST_ACTION));
        LocalBroadcastManager.getInstance(this).sendBroadcast(viewModel.getSendingIntent());


        try {

            IntentFilter filter = new IntentFilter(Intent.ACTION_SHUTDOWN);
            mShutDownReceiver = new ShutDownReceiver();
            registerReceiver(mShutDownReceiver, filter);

        } catch (Exception ignored) {
        }


        if (prefUtils.getStringPref( AppConstants.KEY_SHUT_DOWN) != null
                && prefUtils.getStringPref( AppConstants.KEY_SHUT_DOWN).equals(AppConstants.VALUE_SHUT_DOWN_TRUE) && prefUtils.getBooleanPref( TOUR_STATUS)) {

            if (!viewModel.isServiceRunning(this)) {
                viewModel.startLockService(this,lockScreenIntent);
            }
        }
    }


    private void setRecyclerView() {
        rvApps = findViewById(R.id.rvApps);
        int resId = R.anim.layout_animation;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(this, resId);
        rvApps.setLayoutAnimation(animation);

        adapter = new RAdapter(this,prefUtils);
        adapter.appsList = new ArrayList<>();
        int column_span = prefUtils.getIntegerPref( AppConstants.KEY_COLUMN_SIZE);
        if (column_span == 0) {
            column_span = AppConstants.LAUNCHER_GRID_SPAN;
        }
        ((SimpleItemAnimator) rvApps.getItemAnimator()).setSupportsChangeAnimations(false);
        rvApps.setLayoutManager(new GridLayoutManager(this, column_span));
        rvApps.setAdapter(adapter);
        rvApps.setItemViewCacheSize(30);


    }

    public void clearRecentApp() {
        AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(i);

        });


    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {

            adapter.appsList.clear();
            adapter.notifyDataSetChanged();
            clearRecentApp();
            final String message = intent.getStringExtra(AppConstants.BROADCAST_KEY);
            setBackground(message);
            viewModel.addDataToList(allDbApps, message, adapter);
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
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
        super.onResume();
        prefUtils.saveBooleanPref( UNINSTALL_ALLOWED, false);

//        Log.d(TAG, "DISPLAY: "+Build.DISPLAY);
        String languageKey = prefUtils.getStringPref( AppConstants.LANGUAGE_PREF);

        if (languageKey != null && languageKey.equals("ar")) {
//            layoutManager.setReverseLayout(true);
            rvApps.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        } else {
            rvApps.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        }

        if (!viewModel.isServiceRunning(this) && prefUtils.getBooleanPref( TOUR_STATUS)) {
            Intent lockScreenIntent = new Intent(this, LockScreenService.class);
            viewModel.startLockService(this, lockScreenIntent);
        }

        String msg = prefUtils.getStringPref( AppConstants.CURRENT_KEY);
        if (prefUtils.getBooleanPref( SHOW_MANUAL_ACTIVITY) && msg != null && msg.equals(KEY_MAIN_PASSWORD)) {
            Timber.d("<<< Policy Remaining >>>");
            Intent intent = new Intent(MainActivity.this, ManualPullPush.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        boolean pendingDialog = prefUtils.getBooleanPref( AppConstants.PENDING_ALARM_DIALOG);
        if (pendingDialog) {
            String dialogMessage = prefUtils.getStringPref( AppConstants.PENDING_DIALOG_MESSAGE);
            if (!dialogMessage.equals("")) {
                new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.expiry_alert_online_title))
                        .setMessage(dialogMessage)

                        .setPositiveButton(android.R.string.yes, (dialog, which) -> dialog.dismiss())

                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                prefUtils.saveBooleanPref( AppConstants.PENDING_ALARM_DIALOG, false);
                prefUtils.saveStringPref( AppConstants.PENDING_DIALOG_MESSAGE, "");
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
                    bg = prefUtils.getStringPref( KEY_MAIN_IMAGE);
                    if (bg == null || bg.equals("")) {
                        background.setImageResource(R.raw._1239);

                    } else {
                        background.setImageResource(Integer.parseInt(bg));
                    }
                } else if (message.equals(KEY_SUPPORT_PASSWORD)) {
                    // for the guest type user
                    bg = prefUtils.getStringPref( KEY_SUPPORT_IMAGE);
                    if (bg == null || bg.equals("")) {
                        background.setImageResource(R.raw.texture);

                    } else {
                        background.setImageResource(Integer.parseInt(bg));
                    }

                } else {
                    bg = prefUtils.getStringPref( AppConstants.KEY_GUEST_IMAGE);
                    if (bg == null || bg.equals("")) {
                        background.setImageResource(R.raw._12318);

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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
            unregisterReceiver(mShutDownReceiver);
            sharedPref.unregisterOnSharedPreferenceChangeListener(listener);

        } catch (Exception ignored) {
            //
        }

    }

    SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> {
        if (key.equals(KEY_GUEST_IMAGE) || key.equals(KEY_MAIN_IMAGE)) {
            String msg = prefUtils.getStringPref( AppConstants.CURRENT_KEY);
            if (msg != null && !msg.equals("")) {
                setBackground(msg);
            }
        } else if (key.equals(AppConstants.KEY_COLUMN_SIZE)) {
            int column_span = prefUtils.getIntegerPref( AppConstants.KEY_COLUMN_SIZE);
            if (column_span == 0) {
                column_span = AppConstants.LAUNCHER_GRID_SPAN;
            }
            rvApps.setLayoutManager(new GridLayoutManager(this, column_span));
        } else if (key.equals(NUMBER_OF_NOTIFICATIONS)) {
            String name = "Live Chat Support";
            int index = IntStream.range(0, adapter.appsList.size())
                    .filter(i -> name.equals(adapter.appsList.get(i).getLabel()))
                    .findFirst()
                    .orElse(-1);
            if (index != -1) {
                AppInfo app = adapter.appsList.get(index);
                app.setNumberOfnotifications(prefUtils.getIntegerPref( NUMBER_OF_NOTIFICATIONS));
                adapter.appsList.set(index, app);
                adapter.notifyItemChanged(index);

            }
        }

    };

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
                clearNotify();
                appCache();
                runOnUiThread(() -> {
                    hud.dismiss();
                    clearCacheSuccess();
                });
            }
        }, 2000);
        AccessibilityManager manager = (AccessibilityManager) this.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain();
            event.setEventType(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
            event.setAction(GLOBAL_ACTION_RECENTS);
            manager.sendAccessibilityEvent(event);
        }
    }


    private void clearRecenttasks() {
        // Force stop packages
        Intent intent = new Intent("com.secure.systemcontrol.POWERMODE");
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setComponent(new ComponentName("com.secure.systemcontrol", "com.secure.systemcontrol.receivers.SettingsReceiver"));
        sendBroadcast(intent);
    }

    private void clearNotify() {
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
    public void onDownloadCompleted(ArrayList<InstallModel> downloadedApps) {
        Timber.d("<<< Downloading Completed >>>");
        prefUtils.saveBooleanPref( SHOW_MANUAL_ACTIVITY, true);
        Intent intent = new Intent(MainActivity.this, ManualPullPush.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}



