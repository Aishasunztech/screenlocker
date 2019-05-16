package com.screenlocker.secure.base;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.screenlocker.secure.BlockStatusBar;
import com.screenlocker.secure.R;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.LifecycleReceiver;
import com.screenlocker.secure.utils.PermissionUtils;
import com.screenlocker.secure.utils.PrefUtils;
import com.secureSetting.SecureSettingsMain;

import org.jsoup.Connection;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.LOADING_POLICY;
import static com.screenlocker.secure.utils.AppConstants.LOAD_POLICY;
import static com.screenlocker.secure.utils.AppConstants.PENDING_FINISH_DIALOG;
import static com.screenlocker.secure.utils.LifecycleReceiver.LIFECYCLE_ACTION;

@SuppressLint("Registered")
public abstract class BaseActivity extends AppCompatActivity implements LifecycleReceiver.StateChangeListener {
    //    customViewGroup view;
    WindowManager.LayoutParams localLayoutParams;
    private boolean overlayIsAllowed;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        localLayoutParams = new WindowManager.LayoutParams();

        createAlertDialog();

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
                .setPositiveButton("allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PermissionUtils.requestOverlayPermission(BaseActivity.this);
                    }
                }).create();
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
        policyDialog.setMessage("Please wait, Loading Policy to your Device. This may take a few minutes, do not turn of your Device.");
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
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

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

        if (PermissionUtils.canDrawOver(this)) {
            overlayIsAllowed = true;
            if (!statusViewAdded) {
//                addStatusOverlay();
                statusViewAdded = true;
            }
            if (PrefUtils.getStringPref(this, AppConstants.KEY_ENABLE_SCREENSHOT) == null) {
//                enableScreenShotBlocker(true);
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

    @Override
    protected void onResume() {
        super.onResume();

        if (PrefUtils.getBooleanPref(this, PENDING_FINISH_DIALOG)) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Policy Loaded!");
            alertDialog.setIcon(R.drawable.ic_done_white_18dp);
            alertDialog.setCancelable(false);
            alertDialog.setMessage("Policy successfully loaded.");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {
                dialog.dismiss();
            });

            alertDialog.show();
        }
    }


}
