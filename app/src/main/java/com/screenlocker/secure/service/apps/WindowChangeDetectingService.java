package com.screenlocker.secure.service.apps;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.HashSet;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.IS_SETTINGS_ALLOW;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;

public class WindowChangeDetectingService extends AccessibilityService {


    private WindowManager wm;
    private FrameLayout mView;
    private TextView textView;
    private HashSet<String> allowedSettingsActivities = new HashSet<>();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();


        mView = new FrameLayout(this);

        textView = new TextView(this);

        allowedSettingsActivities.add("com.android.settings/.Settings$AccessibilitySettingsActivity");
        allowedSettingsActivities.add("com.android.settings/.Settings$WifiSettingsActivity");
        allowedSettingsActivities.add("com.android.settings/.ConfirmLockPassword$InternalActivity");
        allowedSettingsActivities.add("com.android.settings/.ChooseLockGenericActivity");
        allowedSettingsActivities.add("com.android.settings/.ChooseLockPassword");
        allowedSettingsActivities.add("com.android.settings/.Settings$PowerUsageSummaryActivity");
        allowedSettingsActivities.add("com.android.settings/.Settings$SoundSettingsActivity");
        allowedSettingsActivities.add("com.android.settings/.Settings$DateTimeSettingsActivity");
        allowedSettingsActivities.add("com.android.settings/.Settings$BluetoothSettingsActivity");
        allowedSettingsActivities.add("com.android.settings/.Settings$SimSettingsActivity");
        allowedSettingsActivities.add("com.android.settings/.applications.InstalledAppDetailsTop");
        allowedSettingsActivities.add("com.google.android.packageinstaller/com.android.packageinstaller.permission.ui.ManagePermissionsActivity");
        allowedSettingsActivities.add(getPackageName() + "/com.secureSetting.SecureSettingsMain");
        allowedSettingsActivities.add(getPackageName() + "/com.secureMarket.SecureMarketActivity");


        textView.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        textView.setWidth(WindowManager.LayoutParams.MATCH_PARENT);

        textView.setSingleLine(false);
        textView.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);

        mView.addView(textView);

        getOverLayLayoutParams();
        createLayoutParams();

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        mView.setBackgroundColor(getResources().getColor(android.R.color.white));
//        wm.addView(mView, localLayoutParams);


        //Configure these here for compatibility with API 13 and below.
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {


        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null && event.getClassName() != null) {
                ComponentName componentName = new ComponentName(
                        event.getPackageName().toString(),
                        event.getClassName().toString()
                );


                ActivityInfo activityInfo = tryGetActivity(componentName);
                boolean isActivity = activityInfo != null;
                if (isActivity) {
                    Timber.d("dkjgdgrfghdghdr %s", componentName.flattenToShortString());
                    textView.setText(componentName.flattenToShortString());
                    if (allowedSettingsActivities.contains(componentName.flattenToShortString()) && PrefUtils.getBooleanPref(this, IS_SETTINGS_ALLOW)) {
                        textView.setText(componentName.flattenToShortString() + "This activity is allowed");
                    } else {

                        if (PrefUtils.getBooleanPref(this, IS_SETTINGS_ALLOW)) {
                            textView.setText(componentName.flattenToShortString() + "This activity is not allowed");
                            if (PrefUtils.getBooleanPref(this, TOUR_STATUS)) {
                                Intent intent = new Intent(this, LockScreenService.class);
                                intent.setAction("locked");
                                ActivityCompat.startForegroundService(this, intent);
                            }


                        } else {
                            textView.setText(componentName.flattenToShortString() + "This activity is handled by service");
                        }
                    }
                }


            }
        }
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public void onInterrupt() {
        try {
            if (wm != null && mView != null)
                wm.removeViewImmediate(mView);
        } catch (Exception ignored) {
        }

    }

    private WindowManager.LayoutParams localLayoutParams;

    private void createLayoutParams() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

        } else {

            localLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        localLayoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER;

        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
// this is to enable the notification to recieve touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
// Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;


        localLayoutParams.y = (int) (450 * getResources().getDisplayMetrics().scaledDensity);
        ;
        localLayoutParams.width = (int) (300 * getResources().getDisplayMetrics().scaledDensity);

        localLayoutParams.height = (int) (56 * getResources().getDisplayMetrics().scaledDensity);

        localLayoutParams.format = PixelFormat.TRANSLUCENT;

    }

    public void getOverLayLayoutParams() {
        if (localLayoutParams == null) {
            localLayoutParams = new WindowManager.LayoutParams();
            createLayoutParams();
        }
    }

}