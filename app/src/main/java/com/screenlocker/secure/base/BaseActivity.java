package com.screenlocker.secure.base;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.screenlocker.secure.BlockStatusBar;
import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.listener.OnAppsRefreshListener;
import com.screenlocker.secure.permissions.SteppersActivity;
import com.screenlocker.secure.service.apps.WindowChangeDetectingService;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PermissionUtils;
import com.screenlocker.secure.utils.PrefUtils;
import com.secure.launcher.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.EMERGENCY_FLAG;
import static com.screenlocker.secure.utils.AppConstants.FINISH_POLICY;
import static com.screenlocker.secure.utils.AppConstants.IS_SETTINGS_ALLOW;
import static com.screenlocker.secure.utils.AppConstants.LOADING_POLICY;
import static com.screenlocker.secure.utils.AppConstants.PENDING_FINISH_DIALOG;
import static com.screenlocker.secure.utils.AppConstants.PERMISSION_GRANTING;
import static com.screenlocker.secure.utils.AppConstants.POLICY_NAME;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
import static com.screenlocker.secure.utils.PermissionUtils.isAccessGranted;
import static com.screenlocker.secure.utils.PermissionUtils.isMyLauncherDefault;
import static com.screenlocker.secure.utils.PermissionUtils.isNotificationAccess;
import static com.screenlocker.secure.utils.Utils.isAccessServiceEnabled;


public abstract class BaseActivity extends AppCompatActivity implements OnAppsRefreshListener {
    //    customViewGroup view;

    private boolean overlayIsAllowed;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;
    private boolean statusViewAdded;
    private Object statusBarService;
    private Handler collapseNotificationHandler;
    protected PrefUtils prefUtils;

    public boolean isOverLayAllowed() {
        return overlayIsAllowed;
    }

    AlertDialog alertDialog;


    public AlertDialog gerOverlayDialog() {
        if (alertDialog == null) {
            createAlertDialog();
        }
        return alertDialog;
    }


    BroadcastReceiver loadingPolicyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(FINISH_POLICY)) {
                if (getPolicyDialog().isShowing()) {
                    getPolicyDialog().dismiss();
                }
                showpolicyConfirmstion();
            }
        }
    };


    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefUtils = PrefUtils.getInstance(this);
        statusBarService = getSystemService("statusbar");
        if (prefUtils.getBooleanPref( AppConstants.KEY_THEME)) {
            switch (AppCompatDelegate.getDefaultNightMode()) {
                case AppCompatDelegate.MODE_NIGHT_UNSPECIFIED:
                case AppCompatDelegate.MODE_NIGHT_NO:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    getDelegate().applyDayNight();
                    recreate();
                    break;
                case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                    break;
                case AppCompatDelegate.MODE_NIGHT_YES:
                    break;
            }
        }
        createAlertDialog();
        compName = new ComponentName(this, MyAdmin.class);
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);


        if (PermissionUtils.canDrawOver(this)) {

            statusViewAdded = true;
        } else {
            if (alertDialog == null) {
                createAlertDialog();
            } else {
                if (!alertDialog.isShowing() && prefUtils.getBooleanPref( DEVICE_LINKED_STATUS))
                    alertDialog.show();
            }

        }


//


    }


    private void createAlertDialog() {
        if (alertDialog == null) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            this.alertDialog = alertDialog
                    .setCancelable(false)
                    .setTitle("This app requires permission to draw overlay.")
                    .setMessage("Please allow this permission")
                    .setPositiveButton("allow", (dialogInterface, i) -> PermissionUtils.requestOverlayPermission(BaseActivity.this)).create();
        }
    }


    private ProgressDialog policyDialog;

    public ProgressDialog getPolicyDialog() {
        if (policyDialog == null) {
            policyDialog();
        }
        return policyDialog;
    }

    private void policyDialog() {
        policyDialog = new ProgressDialog(this);
        policyDialog.setTitle("Loading policy");
        policyDialog.setMessage("Please wait, Loading Policy to your Device. This may take a few minutes , Do not turn OFF device.");
        policyDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
            policyDialog.dismiss();
            prefUtils.saveBooleanPref( LOADING_POLICY, false);
        });
        policyDialog.setCancelable(false);
        policyDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(loadingPolicyReceiver);

    }

    @Override
    protected void onStart() {
        super.onStart();
        showpolicyConfirmstion();

        LocalBroadcastManager.getInstance(this).registerReceiver(loadingPolicyReceiver, new IntentFilter(FINISH_POLICY));

        boolean status = prefUtils.getBooleanPref( LOADING_POLICY);

        if (status) {
            if (!getPolicyDialog().isShowing()) {
                getPolicyDialog().show();
            }
        } else {
            if (getPolicyDialog().isShowing()) {
                getPolicyDialog().dismiss();
            }
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);


        if (PermissionUtils.canDrawOver(this)) {
            overlayIsAllowed = true;
            if (!statusViewAdded) {
//                addStatusOverlay();
                statusViewAdded = true;
            }

        } else {
            if (statusViewAdded) {
//                removeStatusOverlay();
            }
            overlayIsAllowed = false;
            statusViewAdded = false;

            if (alertDialog == null) {
                createAlertDialog();
            } else {
                if (!alertDialog.isShowing() && prefUtils.getBooleanPref( DEVICE_LINKED_STATUS))
                    alertDialog.show();
            }
        }


    }

    private AlertDialog policyConfirmation;


    public AlertDialog getPolicyConfirmation() {

        String policyName = prefUtils.getStringPref( POLICY_NAME);

        if (policyName == null) {
            policyName = "#default_policy";
        }
        policyConfirmation = new AlertDialog.Builder(this).create();
        policyConfirmation.setTitle("Policy Loaded!");
        policyConfirmation.setIcon(R.drawable.ic_done_white_18dp);
        policyConfirmation.setCancelable(false);
        policyConfirmation.setMessage("Policy \"" + policyName + "\" successfully loaded to device");
        policyConfirmation.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {
            dialog.dismiss();
            prefUtils.saveBooleanPref( PENDING_FINISH_DIALOG, false);
            prefUtils.saveStringPref( POLICY_NAME, null);
        });

        return policyConfirmation;
    }

    private void showpolicyConfirmstion() {

        if (prefUtils.getBooleanPref( PENDING_FINISH_DIALOG)) {
            if (!getPolicyConfirmation().isShowing()) {
                getPolicyConfirmation().show();
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        Intent intent1 = new Intent(AppConstants.BROADCAST_VIEW_ADD_REMOVE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);
        AppConstants.TEMP_SETTINGS_ALLOWED = false;

//        String language_key = prefUtils.getStringPref( AppConstants.LANGUAGE_PREF);
//        if (language_key != null && !language_key.equals("")) {
//            CommonUtils.setAppLocale(language_key, this);
//        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (!devicePolicyManager.isAdminActive(compName)) {
            launchPermissions();
        } else if (!Settings.canDrawOverlays(this)) {
            launchPermissions();
        } else if (!Settings.System.canWrite(this)) {
            launchPermissions();
        } else if (!isAccessGranted(this)) {
            launchPermissions();
        } else if (
                checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||

                        checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||

                        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||

                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            launchPermissions();
        } else if (!isAccessServiceEnabled(this, WindowChangeDetectingService.class)) {
            launchPermissions();
//            ActivityCompat.startForegroundService(this, new Intent(this, LockScreenService.class).setAction("startThread"));
        } else if (!isMyLauncherDefault(MyApplication.getAppContext())) {
            prefUtils.saveBooleanPref( PERMISSION_GRANTING, true);
            prefUtils.saveBooleanPref( IS_SETTINGS_ALLOW, false);
            Intent a = new Intent(this, SteppersActivity.class);
            if (prefUtils.getBooleanPref( TOUR_STATUS)) {
                a.putExtra("emergencyLauncher", true);
            }
            startActivity(a);
        } else if (!isNotificationAccess(this)) {
            launchPermissions();
        } else if (!pm.isIgnoringBatteryOptimizations(MyApplication.getAppContext().

                getPackageName())) {
            launchPermissions();
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !

                getPackageManager().

                        canRequestPackageInstalls()) {
            launchPermissions();
        }
        else {
            prefUtils.saveBooleanPref( PERMISSION_GRANTING, false);
//            ActivityCompat.startForegroundService(this, new Intent(this, LockScreenService.class).setAction("stopThread"));
        }
    }

    private void launchPermissions() {
        prefUtils.saveBooleanPref( PERMISSION_GRANTING, true);
        prefUtils.saveBooleanPref( IS_SETTINGS_ALLOW, false);
        Intent a = new Intent(this, SteppersActivity.class);
        if (prefUtils.getBooleanPref( TOUR_STATUS)) {
            a.putExtra("emergency", true);
        }
        startActivity(a);
    }


    @Override
    public void onAppsRefresh() {

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!hasFocus) {



                if (!prefUtils.getBooleanPref( EMERGENCY_FLAG)) {
                    // Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

                    // sendBroadcast(closeDialog);
                    collapseNow();
                }

            }
        }

    }

    public void collapseNow() {

        try {
            // Initialize 'collapseNotificationHandler'
            if (collapseNotificationHandler == null) {
                collapseNotificationHandler = new Handler();
            }

            // Post a Runnable with some delay - currently set to 300 ms
            collapseNotificationHandler.postDelayed(new Runnable() {

                @Override
                public void run() {

                    // Use reflection to trigger a method from 'StatusBarManager'

                    Class<?> statusBarManager = null;

                    try {
                        statusBarManager = Class.forName("android.app.StatusBarManager");
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    Method collapseStatusBar = null;
                    try {
                        // Prior to API 17, the method to call is 'collapse()'
                        // API 17 onwards, the method to call is `collapsePanels()`
                        if (Build.VERSION.SDK_INT > 16) {
                            collapseStatusBar = statusBarManager.getMethod("collapsePanels");
                        } else {
                            collapseStatusBar = statusBarManager.getMethod("collapse");
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                    collapseStatusBar.setAccessible(true);

                    try {
                        collapseStatusBar.invoke(statusBarService);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    // Currently, the delay is 10 ms. You can change this
                    // value to suit your needs.
                    collapseNotificationHandler.postDelayed(this, 10L);
                }
            }, 10L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
