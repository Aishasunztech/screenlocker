package com.screenlocker.secure.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.screenlocker.secure.BlockStatusBar;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.LifecycleReceiver;
import com.screenlocker.secure.utils.PermissionUtils;
import com.screenlocker.secure.utils.PrefUtils;

import timber.log.Timber;

import static com.screenlocker.secure.utils.LifecycleReceiver.LIFECYCLE_ACTION;

@SuppressLint("Registered")
public abstract class BaseActivity extends AppCompatActivity implements LifecycleReceiver.StateChangeListener {
//    customViewGroup view;
    WindowManager.LayoutParams localLayoutParams;
    private boolean overlayIsAllowed;
    private static WindowManager manager, mWindowManager;

    private Window mWindow;
    private WindowManager.LayoutParams screenShotParams;

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
        manager = ((WindowManager) BaseActivity.this.getSystemService(Context.WINDOW_SERVICE));
        mWindowManager = getWindowManager();
        mWindow = getWindow();
        localLayoutParams = new WindowManager.LayoutParams();
//        disablePullNotificationTouch();
        createAlertDialog();

        if (PermissionUtils.canDrawOver(this)) {
//            addStatusOverlay();
            statusViewAdded = true;
        } else {
            if (alertDialog == null) {
                createAlertDialog();
            } else {
                if (!alertDialog.isShowing())
                    alertDialog.show();
            }

        }
        lifecycleReceiver = new LifecycleReceiver();            //<---
//
        registerReceiver(lifecycleReceiver, new IntentFilter(LIFECYCLE_ACTION));
        lifecycleReceiver.setStateChangeListener(this);


    }


    public WindowManager.LayoutParams getOverLayLayoutParams() {
        if (localLayoutParams == null) {
            localLayoutParams = new WindowManager.LayoutParams();
            createLayoutParams();
        }
        return localLayoutParams;
    }

    void removeStatusOverlay() {
//        manager.removeView(getOverLayView());
    }

    void addStatusOverlay() {
//        manager.addView(getOverLayView(), getOverLayLayoutParams());
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

    protected void allowScreenShot(boolean isAllowed) {
        if (isAllowed) {
            mWindow.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            mWindow.clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

//
//    public void disablePullNotificationTouch() {
//        createLayoutParams();
//        view = new customViewGroup(this);
//
//    }

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

//    public customViewGroup getOverLayView() {
//        if (view == null) {
//            view = new customViewGroup(this);
//        }
//        return view;
//    }


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


//    //Add this class in your project
//    public class customViewGroup extends ViewGroup {
//
//        public customViewGroup(Context context) {
//            super(context);
//        }
//
//
//        @Override
//        protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        }
//
//        @Override
//        public boolean onInterceptTouchEvent(MotionEvent ev) {
//
//            Timber.v("**********Intercepted");
//            return true;
//        }
//
//    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
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
                if (!alertDialog.isShowing())
                    alertDialog.show();
            }
        }
    }


//    public void enableScreenShotBlocker(boolean isChecked) {
//
//        if (mWindowManager == null)
//            mWindowManager = getWindowManager();
//
//        if (isChecked) {
////            mWindowManager.addView(getScreenShotView(), getLayoutParams());
//            PrefUtils.saveStringPref(this, AppConstants.KEY_ENABLE_SCREENSHOT, AppConstants.VALUE_SCREENSHOT_ENABLE);
//        } else {
//            PrefUtils.saveStringPref(this, AppConstants.KEY_ENABLE_SCREENSHOT, AppConstants.VALUE_SCREENSHOT_ENABLE);
//            Toast.makeText(BaseActivity.this, "already screenshot enabled", Toast.LENGTH_SHORT).show();
//        }
//
//
//    }

//    public void disableScreenShotBlocker(boolean isChecked) {
//        if (mWindowManager == null)
//            mWindowManager = getWindowManager();
//        if (isChecked) {
//            try {
////                mWindowManager.removeView(getScreenShotView());
//                PrefUtils.saveStringPref(this, AppConstants.KEY_ENABLE_SCREENSHOT, AppConstants.VALUE_SCREENSHOT_DISABLE);
//            } catch (Exception ignored) {
//            }
//        } else {
//            Toast.makeText(this, "already screenshot disabled", Toast.LENGTH_SHORT).show();
//            String name = "\uD83E\uDD23";
//        }
//
//    }


//    @NonNull
//    private LinearLayout getScreenShotView() {
////
////        if (mScreenShotView == null)
////            mScreenShotView = createScreenShotView();
////        return mScreenShotView;
//        return MyApplication.getScreenShotView(getApplicationContext());
//    }
/*

    @NonNull
    private WindowManager.LayoutParams getLayoutParams() {
        if (screenShotParams == null) {
            screenShotParams = createScreenShotParams();
        }
        return screenShotParams;
    }*/

//    private WindowManager.LayoutParams createScreenShotParams() {
//
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
//        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
//        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//        else
//            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
//        params.flags = WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_SECURE;
//        params.format = PixelFormat.TRANSPARENT;
//        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
//        params.gravity = Gravity.CENTER;
//
//        return params;
//    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            if (!hasFocus) {
//                Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//                sendBroadcast(closeDialog);
//                Method that handles loss of window focus
//                new BlockStatusBar(this, false).collapseNow();
            }
        }
    }



}
