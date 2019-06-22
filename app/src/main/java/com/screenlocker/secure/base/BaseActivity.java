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
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.permissions.SteppersActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.LifecycleReceiver;
import com.screenlocker.secure.utils.PermissionUtils;
import com.screenlocker.secure.utils.PrefUtils;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.FINISH_POLICY;
import static com.screenlocker.secure.utils.AppConstants.LOADING_POLICY;
import static com.screenlocker.secure.utils.AppConstants.PENDING_FINISH_DIALOG;
import static com.screenlocker.secure.utils.AppConstants.POLICY_NAME;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
import static com.screenlocker.secure.utils.LifecycleReceiver.LIFECYCLE_ACTION;
import static com.screenlocker.secure.utils.PermissionUtils.isAccessGranted;
import static com.screenlocker.secure.utils.PermissionUtils.isNotificationAccess;

@SuppressLint("Registered")
public abstract class BaseActivity extends AppCompatActivity implements LifecycleReceiver.StateChangeListener {
    //    customViewGroup view;
    WindowManager.LayoutParams localLayoutParams;
    private boolean overlayIsAllowed;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;
//    private static WindowManager manager, mWindowManager;


    private boolean statusViewAdded;
    private LifecycleReceiver lifecycleReceiver;

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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        localLayoutParams = new WindowManager.LayoutParams();

        createAlertDialog();
        compName = new ComponentName(this, MyAdmin.class);
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);


        if (PermissionUtils.canDrawOver(this)) {

            statusViewAdded = true;
        } else {
            if (alertDialog == null) {
                createAlertDialog();
            } else {
                if (!alertDialog.isShowing() && PrefUtils.getBooleanPref(BaseActivity.this, DEVICE_LINKED_STATUS))
                    alertDialog.show();
            }

        }


        lifecycleReceiver = new LifecycleReceiver();            //<---
//
        registerReceiver(lifecycleReceiver, new IntentFilter(LIFECYCLE_ACTION));
        lifecycleReceiver.setStateChangeListener(this);


    }



    private void createAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        this.alertDialog = alertDialog
                .setCancelable(false)
                .setTitle("This app requires permission to draw overlay.")
                .setMessage("Please allow this permission")
                .setPositiveButton("allow", (dialogInterface, i) -> PermissionUtils.requestOverlayPermission(BaseActivity.this)).create();
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
            PrefUtils.saveBooleanPref(this, LOADING_POLICY, false);
        });
        policyDialog.setCancelable(false);
        policyDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }


    private void createLayoutParams() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

        } else {

            localLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        localLayoutParams.gravity = Gravity.TOP;
        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
// this is to enable the notification to recieve touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
// Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        localLayoutParams.height = (int) (25 * getResources().getDisplayMetrics().scaledDensity);
        localLayoutParams.format = PixelFormat.TRANSPARENT;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(lifecycleReceiver);
        lifecycleReceiver.unsetStateChangeListener();

        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    @Override
    public void onStateChange(int state) {
        switch (state) {

            case LifecycleReceiver.FOREGROUND:
                Timber.e("onStateChange: FOREGROUND");
                break;

            case LifecycleReceiver.BACKGROUND:
                Timber.e("onStateChange: BACKGROUND");
                break;

            default:
                Timber.e("onStateChange: SOMETHING");
                break;
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

        boolean status = PrefUtils.getBooleanPref(BaseActivity.this, LOADING_POLICY);
//       PrefUtils.saveBooleanPref(BaseActivity.this, LOADING_POLICY,false);

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
                if (!alertDialog.isShowing() && PrefUtils.getBooleanPref(BaseActivity.this, DEVICE_LINKED_STATUS))
                    alertDialog.show();
            }
        }


    }

    private AlertDialog policyConfirmation;


    public AlertDialog getPolicyConfirmation() {

        if (policyConfirmation == null) {

            String policyName = PrefUtils.getStringPref(BaseActivity.this, POLICY_NAME);

            if (policyName == null) {
                policyName = "#default_policy";
            }
            policyConfirmation = new AlertDialog.Builder(this).create();
            policyConfirmation.setTitle("Policy Loaded!");
            policyConfirmation.setIcon(R.drawable.ic_done_white_18dp);
            policyConfirmation.setCancelable(false);
            policyConfirmation.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
                dialog.dismiss();

            });
            policyConfirmation.setMessage("Policy \"" + policyName + "\" successfully loaded to device");
            policyConfirmation.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {
                dialog.dismiss();
                PrefUtils.saveBooleanPref(this, PENDING_FINISH_DIALOG, false);
                PrefUtils.saveStringPref(this, POLICY_NAME, null);
            });
        }

        return policyConfirmation;
    }

    private void showpolicyConfirmstion() {

        if (PrefUtils.getBooleanPref(this, PENDING_FINISH_DIALOG)) {
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
        String language_key = PrefUtils.getStringPref(this, AppConstants.LANGUAGE_PREF);
        if(language_key != null && !language_key.equals(""))
        {
            CommonUtils.setAppLocale(language_key,this);
        }
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (!devicePolicyManager.isAdminActive(compName)) {
            launchPermissions();
        } else if (!Settings.canDrawOverlays(this)) {
            launchPermissions();
        } else if (!Settings.System.canWrite(this)) {
            launchPermissions();
        } else if (!isAccessGranted(this)) {
            launchPermissions();
        } else if (checkSelfPermission(android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            launchPermissions();
        } else if (!getPackageManager().canRequestPackageInstalls()) {
            launchPermissions();
        } else if (!isNotificationAccess(this)) {
            launchPermissions();
        } else if (!pm.isIgnoringBatteryOptimizations(MyApplication.getAppContext().getPackageName())) {
            launchPermissions();
        }
    }

    private void launchPermissions() {
        Intent a = new Intent(this, SteppersActivity.class);
        if (PrefUtils.getBooleanPref(this, TOUR_STATUS)) {
            a.putExtra("emergency", true);
        }
        startActivity(a);
    }


}
