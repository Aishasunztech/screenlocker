package com.screenlocker.secure.base;

import android.Manifest;
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
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.screenlocker.secure.MyAdmin;
import com.secure.launcher.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.listener.OnAppsRefreshListener;
import com.screenlocker.secure.permissions.SteppersActivity;
import com.screenlocker.secure.settings.AdvanceSettings;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PermissionUtils;
import com.screenlocker.secure.utils.PrefUtils;

import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.FINISH_POLICY;
import static com.screenlocker.secure.utils.AppConstants.LOADING_POLICY;
import static com.screenlocker.secure.utils.AppConstants.PENDING_FINISH_DIALOG;
import static com.screenlocker.secure.utils.AppConstants.PERMISSION_GRANTING;
import static com.screenlocker.secure.utils.AppConstants.POLICY_NAME;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
import static com.screenlocker.secure.utils.PermissionUtils.isAccessGranted;
import static com.screenlocker.secure.utils.PermissionUtils.isNotificationAccess;


public abstract class BaseActivity extends AppCompatActivity implements OnAppsRefreshListener {
    //    customViewGroup view;

    private boolean overlayIsAllowed;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;
    private boolean statusViewAdded;

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
        if (PrefUtils.getBooleanPref(BaseActivity.this, AppConstants.KEY_THEME)) {
            switch (AppCompatDelegate.getDefaultNightMode()) {
                case AppCompatDelegate.MODE_NIGHT_UNSPECIFIED:
                case AppCompatDelegate.MODE_NIGHT_NO:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    getDelegate().applyDayNight();
                    recreate();
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
                if (!alertDialog.isShowing() && PrefUtils.getBooleanPref(BaseActivity.this, DEVICE_LINKED_STATUS))
                    alertDialog.show();
            }

        }


//


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

        boolean status = PrefUtils.getBooleanPref(BaseActivity.this, LOADING_POLICY);

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
        Intent intent1 = new Intent(AppConstants.BROADCAST_VIEW_ADD_REMOVE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent1);
        AppConstants.TEMP_SETTINGS_ALLOWED = false;

        String language_key = PrefUtils.getStringPref(this, AppConstants.LANGUAGE_PREF);
        if (language_key != null && !language_key.equals("")) {
            CommonUtils.setAppLocale(language_key, this);
        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (!devicePolicyManager.isAdminActive(compName)) {
            launchPermissions();
        } else if (!Settings.canDrawOverlays(this)) {
            launchPermissions();
        } else if (!Settings.System.canWrite(this)) {
            launchPermissions();
        } else if (!

                isAccessGranted(this)) {
            launchPermissions();
        } else if (
                checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||

                        checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||

                        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||

                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            launchPermissions();
        } else if (!

                isNotificationAccess(this)) {
            launchPermissions();
        } else if (!pm.isIgnoringBatteryOptimizations(MyApplication.getAppContext().

                getPackageName())) {
            launchPermissions();
        }
//        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !
//
//                getPackageManager().
//
//                        canRequestPackageInstalls()) {
//            launchPermissions();
//        }
        else {
            PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PERMISSION_GRANTING, false);
//            ActivityCompat.startForegroundService(this, new Intent(this, LockScreenService.class).setAction("stopThread"));
        }
    }

    private void launchPermissions() {
        Intent a = new Intent(this, SteppersActivity.class);
        if (PrefUtils.getBooleanPref(this, TOUR_STATUS)) {
            a.putExtra("emergency", true);
        }
        startActivity(a);
    }


    @Override
    public void onAppsRefresh() {

    }

}
