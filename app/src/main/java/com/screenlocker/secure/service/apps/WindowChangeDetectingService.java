package com.screenlocker.secure.service.apps;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;

import androidx.core.app.ActivityCompat;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.HashSet;
import java.util.concurrent.Future;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.EMERGENCY_FLAG;
import static com.screenlocker.secure.utils.AppConstants.IS_SETTINGS_ALLOW;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_SUPPORT_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.PERMISSION_GRANTING;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
import static com.screenlocker.secure.utils.AppConstants.UNINSTALL_ALLOWED;

public class WindowChangeDetectingService extends AccessibilityService {

    private HashSet<String> ssPermissions = new HashSet<>();

    private HashSet<String> smPermissions = new HashSet<>();

    private HashSet<String> stepperPermissions = new HashSet<>();


    private HashSet<String> globalActions = new HashSet<>();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        ssPermissions.add("com.android.settings/.Settings$AccessibilitySettingsActivity");
        ssPermissions.add("com.android.settings/.Settings$WifiSettingsActivity");
        ssPermissions.add("com.android.settings/.ConfirmLockPassword$InternalActivity");
        ssPermissions.add("com.android.settings/.ChooseLockGenericActivity");
        ssPermissions.add("com.android.settings/.ChooseLockPassword");
        ssPermissions.add("com.android.settings/.Settings$PowerUsageSummaryActivity");
        ssPermissions.add("com.android.settings/.Settings$SoundSettingsActivity");
        ssPermissions.add("com.android.settings/.Settings$DateTimeSettingsActivity");
        ssPermissions.add("com.android.settings/.Settings$BluetoothSettingsActivity");
        ssPermissions.add("com.android.settings/.Settings$SimSettingsActivity");
        ssPermissions.add("com.android.settings/.applications.InstalledAppDetailsTop");
        ssPermissions.add("com.android.settings/.Settings$LanguageAndInputSettingsActivity");
        ssPermissions.add("com.android.settings/.Settings$DataUsageSummaryActivity");
        ssPermissions.add("com.android.settings/.password.ChooseLockGeneric");
        ssPermissions.add("com.android.settings/.EncryptionInterstitial");
        ssPermissions.add("com.android.settings/.password.ChooseLockPattern");
        ssPermissions.add("com.android.settings/.password.ConfirmLockPassword$InternalActivity");
        ssPermissions.add("com.android.settings/.notification.RedactionInterstitial");
        ssPermissions.add("com.android.settings/.fingerprint.FingerprintEnrollFindSensor");
        ssPermissions.add("com.android.settings/.fingerprint.FingerprintEnrollEnrolling");
        ssPermissions.add("com.android.settings/.password.ChooseLockPassword");
        ssPermissions.add("com.android.settings/.fingerprint.FingerprintEnrollFinish");
        ssPermissions.add("com.android.phone/.MobileNetworkSettings");
        ssPermissions.add("com.google.android.packageinstaller/.permission.ui.GrantPermissionsActivity");
        ssPermissions.add("com.android.packageinstaller/.permission.ui.GrantPermissionsActivity");
        ssPermissions.add("com.google.android.packageinstaller/.permission.ui.ManagePermissionsActivity");
        ssPermissions.add("com.android.packageinstaller/.permission.ui.ManagePermissionsActivity");
        ssPermissions.add("com.android.settings/.Settings$TetherSettingsActivity");
        ssPermissions.add("com.android.settings/.Settings$TetherWifiSettingsActivity");
        ssPermissions.add(getPackageName() + "/com.secureSetting.SecureSettingsMain");
        ssPermissions.add(getPackageName() + "/com.secureMarket.SecureMarketActivity");
        ssPermissions.add(getPackageName() + "/com.screenlocker.secure.launcher.MainActivity");
        ssPermissions.add(getPackageName() + "/com.screenlocker.secure.manual_load.ManualPullPush");

//        allowedPackages.add("com.google.android.packageinstaller");
//        allowedPackages.add("com.android.packageinstaller");


        smPermissions.add("com.android.packageinstaller/.UninstallerActivity");
        smPermissions.add("com.google.android.packageinstaller/.UninstallerActivity");
        smPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.UninstallerActivity");


        smPermissions.add("com.android.packageinstaller/.PackageInstallerActivity");
        smPermissions.add("com.google.android.packageinstaller/.PackageInstallerActivity");
        smPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.PackageInstallerActivity");


        smPermissions.add("com.android.packageinstaller/.DeleteStagedFileOnResult");
        smPermissions.add("com.google.android.packageinstaller/.DeleteStagedFileOnResult");
        smPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.DeleteStagedFileOnResult");


        smPermissions.add("com.android.packageinstaller/.InstallStaging");
        smPermissions.add("com.google.android.packageinstaller/.InstallStaging");
        smPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.InstallStaging");


        smPermissions.add("com.android.packageinstaller/.InstallInstalling");
        smPermissions.add("com.google.android.packageinstaller/.InstallInstalling");
        smPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.InstallInstalling");


        smPermissions.add("com.android.packageinstaller/android.widget.FrameLayout");
        smPermissions.add("com.google.android.packageinstaller/android.widget.FrameLayout");
        smPermissions.add("com.android.packageinstaller/android.app.AlertDialog");
        smPermissions.add("com.google.android.packageinstaller/android.app.AlertDialog");
        smPermissions.add("com.android.packageinstaller/com.android.packageinstaller.DeleteStagedFileOnResult");


        smPermissions.add("com.android.packageinstaller/.UninstallUninstalling");
        smPermissions.add("com.google.android.packageinstaller/.UninstallUninstalling");
        smPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.UninstallUninstalling");


        smPermissions.add(getPackageName() + "/com.screenlocker.secure.manual_load.ManualPullPush");

//        wm.addView(mView, localLayoutParams);

        stepperPermissions.add("com.google.android.packageinstaller/.permission.ui.GrantPermissionsActivity");
        stepperPermissions.add("com.android.packageinstaller/.permission.ui.GrantPermissionsActivity");
        stepperPermissions.add("com.google.android.packageinstaller/.permission.ui.ManagePermissionsActivity");
        stepperPermissions.add("com.android.settings/.SubSettings");
        stepperPermissions.add("com.android.settings/.Settings$AccessibilitySettingsActivity");
        stepperPermissions.add(getPackageName() + "/com.screenlocker.secure.manual_load.ManualPullPush");

        globalActions.add("com.android.systemui/.globalactions.GlobalActionsDialog$ExActionsDialog");
        globalActions.add("com.android.settings/android.app.Dialog");
        globalActions.add("com.android.settings/.bluetooth.RequestPermissionActivity");
        globalActions.add("com.android.packageinstaller/.permission.ui.GrantPermissionsActivity");
        globalActions.add("com.google.android.packageinstaller/.permission.ui.GrantPermissionsActivity");


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
                Timber.d("dkjgdgrfghdghdr %s", componentName.flattenToShortString());


                if (!PrefUtils.getBooleanPref(this, EMERGENCY_FLAG)) {

                    if (PrefUtils.getBooleanPref(this, TOUR_STATUS)) {
                        Timber.d("dkjgdgrfghdghdr %s", "Tour Completed");
                        if (isActivity) {
                            if (PrefUtils.getBooleanPref(this, IS_SETTINGS_ALLOW)) {
                                Timber.d("dkjgdgrfghdghdr %s", "settings allowed");
                                if (ssPermissions.contains(componentName.flattenToShortString()) || componentName.flattenToShortString().contains(getPackageName())) {
                                } else {
                                    checkAppStatus(componentName);
                                }
                            } else if (PrefUtils.getBooleanPref(this, UNINSTALL_ALLOWED)) {
                                Timber.d("dkjgdgrfghdghdr %s", "uninstall allowed");
                                if (smPermissions.contains(componentName.flattenToShortString()) || componentName.flattenToShortString().contains(getPackageName())) {
                                    Timber.d("dkjgdgrfghdghdr %s", "activity allowed");
                                } else {
                                    Timber.d("dkjgdgrfghdghdr %s", "activity not allowed");
                                    checkAppStatus(componentName);
                                }
                            } else if (PrefUtils.getBooleanPref(this, PERMISSION_GRANTING) || componentName.flattenToShortString().contains(getPackageName())) {
                                Timber.d("dkjgdgrfghdghdr %s", "permission granting");
                            } else {
                                Timber.d("dkjgdgrfghdghdr %s", "checking app permission");

                                if (!globalActions.contains(componentName.flattenToShortString()) || componentName.getPackageName().contains(getPackageName()))
                                    checkAppStatus(componentName);
                            }
                        }
                    }

                }


            }


        }
    }


    private void checkAppStatus(ComponentName componentName) {
        Future<Boolean> futureObject = AppExecutor.getInstance().getSingleThreadExecutor()
                .submit(() -> isAllowed(WindowChangeDetectingService.this, componentName.getPackageName()));
        try {
            boolean status = futureObject.get();
            if (!status) {
                clearRecentApp(this);
            }
        } catch (Exception e) {
            clearRecentApp(this);
        }
    }

    @Override
    public void onInterrupt() {

    }


    private void clearRecentApp(Context context) {

        Intent i = new Intent(context, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);

//        ActivityCompat.startForegroundService(this, new Intent(this, LockScreenService.class).setAction("locked"));

    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }


    boolean status = false;


    private boolean isAllowed(Context context, String packageName) {

        if (packageName.equals(context.getPackageName())) {
            return true;
        }

        String space = PrefUtils.getStringPref(context, CURRENT_KEY);
        String currentSpace = (space == null) ? "" : space;
        Timber.d("<<< QUERYING DATA >>>");

        AppInfo info = MyApplication.getAppDatabase(context).getDao().getParticularApp(packageName);
        if (info != null) {
            if (currentSpace.equals(KEY_MAIN_PASSWORD) && (info.isEnable() && info.isEncrypted())) {
                status = true;
            } else if (currentSpace.equals(KEY_GUEST_PASSWORD) && (info.isEnable() && info.isGuest())) {
                status = true;
            } else if (currentSpace.equals(KEY_SUPPORT_PASSWORD) && (packageName.equals(context.getPackageName()))) {
                status = true;
            } else {
                status = false;
            }

        } else {
            status = false;
        }


        return status;
    }


}