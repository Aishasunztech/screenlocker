package com.screenlocker.secure.service.apps;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.utils.AppConstants;
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

    private HashSet<String> callingApps = new HashSet<>();

    public static ServiceConnectedListener serviceConnectedListener;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

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
        ssPermissions.add("com.android.settings/.password.ChooseLockGeneric$InternalActivity");
        ssPermissions.add("com.android.settings/.password.ConfirmLockPassword$InternalActivity");
        ssPermissions.add("com.android.settings/.notification.RedactionInterstitial");
        ssPermissions.add("com.android.settings/.fingerprint.FingerprintEnrollFindSensor");
        ssPermissions.add("com.android.settings/.fingerprint.FingerprintEnrollEnrolling");
        ssPermissions.add("com.android.settings/.password.ChooseLockPassword");
        ssPermissions.add("com.android.settings/.fingerprint.FingerprintEnrollFinish");
        ssPermissions.add("com.android.phone/.MobileNetworkSettings");
        ssPermissions.add("com.android.settings/.Settings$TetherSettingsActivity");
        ssPermissions.add("com.android.settings/.Settings$TetherWifiSettingsActivity");
        ssPermissions.add("com.android.settings/.sim.SimPreferenceDialog");
        ssPermissions.add("com.android.phone/android.app.ProgressDialog");
        ssPermissions.add("com.android.providers.telephony/com.android.settings.Settings$ApnSettingsActivity");
        ssPermissions.add("com.android.settings/.Settings$ApnEditorActivity");


        ssPermissions.add("com.google.android.packageinstaller/.permission.ui.GrantPermissionsActivity");
        ssPermissions.add("com.android.packageinstaller/.permission.ui.GrantPermissionsActivity");
        ssPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.permission.ui.GrantPermissionsActivity");

        ssPermissions.add("com.google.android.packageinstaller/.permission.ui.Manage.PermissionsActivity");
        ssPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.permission.ui.ManagePermissionsActivity");
        ssPermissions.add("com.android.packageinstaller/.permission.ui.ManagePermissionsActivity");
        ssPermissions.add("com.android.bluetooth/.opp.BluetoothOppTransferHistory");
        ssPermissions.add("com.android.bluetooth/android.widget.FrameLayout");
        ssPermissions.add("com.android.phone/.NetworkSetting");
        ssPermissions.add("com.android.settings/android.app.AlertDialog");
        ssPermissions.add("com.android.settings/.sim.SimDialogActivity");
        ssPermissions.add("com.android.settings/.Settings$WifiApSettingsActivity");
        ssPermissions.add(getPackageName() + "/android.widget.FrameLayout");
        ssPermissions.add("com.android.settings/.password.ConfirmLockPattern$InternalActivity");

        // samsung
        ssPermissions.add("com.android.settings/.Settings$ConnectionsSettingsActivity");


        // huwei
        ssPermissions.add("com.android.phone/.MSimMobileNetworkSettings");
        ssPermissions.add("com.android.phone/android.app.AlertDialog");

        //SS notification related
        ssPermissions.add("com.android.settings/.Settings$AppNotificationSettingsActivity");
        ssPermissions.add("com.android.settings/.Settings$ConfigureNotificationSettingsActivity");
        ssPermissions.add("android/com.android.internal.app.ResolverActivity");
        ssPermissions.add("com.android.documentsui/.picker.PickActivity");


        ssPermissions.add(getPackageName() + "/android.widget.FrameLayout");


        // samsung
        ssPermissions.add("com.samsung.networkui/android.widget.FrameLayout");
        ssPermissions.add("com.samsung.networkui/.MobileNetworkSettings");
        ssPermissions.add("com.samsung.crane/com.android.settings.Settings$ApnSettingsActivity");
        ssPermissions.add("com.samsung.networkui/.MobileNetworkSettingsTab");

        ssPermissions.add("com.samsung.android.app.telephonyui/.netsettings.ui.NetSettingsActivity");
        ssPermissions.add("com.android.settings/com.samsung.android.settings.face.FaceLockSettings");
        ssPermissions.add("com.android.settings/com.samsung.android.settings.biometrics.BiometricsDisclaimerActivity");


        // huwei
        ssPermissions.add("com.huawei.systemmanager/com.huawei.netassistant.ui.NetAssistantMainActivity");
        ssPermissions.add("com.huawei.systemmanager/android.view.ViewGroup");
        ssPermissions.add("com.huawei.systemmanager/.netassistant.ui.setting.TrafficSettingFragment$TrafficSettingActivity");
        ssPermissions.add("com.huawei.systemmanager/.netassistant.ui.setting.OverLimitNotifyFragment$OverLimitNotifyActivity");
        ssPermissions.add("com.huawei.systemmanager/.netassistant.traffic.trafficranking.TrafficRankingListActivity");
        ssPermissions.add("com.hisi.mapcon/com.android.settings.Settings$ApnSettingsActivity");


        // vivo
        ssPermissions.add("com.android.wifisettings/.Settings$WifiSettingsActivity");
        ssPermissions.add("com.android.wifisettings/android.widget.FrameLayout");
        ssPermissions.add("com.android.wifisettings/.SubSettings");
        ssPermissions.add("com.android.phone/com.android.settings.Settings$ApnSettingsActivity");
        ssPermissions.add("com.android.settings/.ApnEditor");
        ssPermissions.add("com.android.settings/com.vivo.settings.password.ConfirmVivoPin$InternalActivity");

        // Mi
        ssPermissions.add("com.android.settings/.MiuiSecurityChooseUnlock");
        ssPermissions.add("com.miui.securitycenter/com.miui.powercenter.legacypowerrank.PowerConsumeRank");
        ssPermissions.add("com.android.phone/.settings.MobileNetworkSettings");


        ssPermissions.add(getPackageName() + "/com.secureSetting.SecureSettingsMain");
        ssPermissions.add(getPackageName() + "/com.secureMarket.SecureMarketActivity");
        ssPermissions.add(getPackageName() + "/com.screenlocker.secure.launcher.MainActivity");
        ssPermissions.add(getPackageName() + "/com.screenlocker.secure.manual_load.ManualPullPush");

        // SM permission

        smPermissions.add(getPackageName() + "/com.screenlocker.secure.manual_load.ManualPullPush");
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


        smPermissions.add("com.android.packageinstaller/.InstallSuccess");
        smPermissions.add("com.google.android.packageinstaller/.InstallSuccess");
        smPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.InstallSuccess");


//        wm.addView(mView, localLayoutParams);

        stepperPermissions.add("com.google.android.packageinstaller/.permission.ui.GrantPermissionsActivity");
        stepperPermissions.add("com.android.packageinstaller/.permission.ui.GrantPermissionsActivity");
        stepperPermissions.add("com.google.android.packageinstaller/.permission.ui.ManagePermissionsActivity");
        stepperPermissions.add("com.android.settings/.SubSettings");
        stepperPermissions.add("com.android.settings/.Settings$AccessibilitySettingsActivity");
        stepperPermissions.add(getPackageName() + "/com.screenlocker.secure.manual_load.ManualPullPush");


        // some global actions
        globalActions.add("com.android.systemui/.globalactions.GlobalActionsDialog$ExActionsDialog");
        globalActions.add("com.android.settings/android.app.Dialog");
        globalActions.add("com.android.settings/.bluetooth.RequestPermissionActivity");

        globalActions.add("com.android.packageinstaller/.permission.ui.GrantPermissionsActivity");
        globalActions.add("com.google.android.packageinstaller/.permission.ui.GrantPermissionsActivity");
        globalActions.add("com.google.android.packageinstaller/com.android.packageinstaller.permission.ui.GrantPermissionsActivity");

        globalActions.add("com.android.packageinstaller/.permission.ui.ManagePermissionsActivity");
        globalActions.add("com.google.android.packageinstaller/.permission.ui.ManagePermissionsActivity");
        globalActions.add("com.google.android.packageinstaller/com.android.packageinstaller.permission.ui.ManagePermissionsActivity");


        globalActions.add("com.android.settings/.Settings$ManageAppExternalSourcesActivity");
        globalActions.add("com.android.settings/.DeviceAdminAdd");
        globalActions.add("com.android.settings/.applications.InstalledAppDetailsTop");
        globalActions.add("com.android.vpndialogs/android.app.Dialog");
        globalActions.add("com.android.settings/.Settings$AccessibilitySettingsActivity");


        //calling app package name samsung
        callingApps.add("com.samsung.android.incallui");
        callingApps.add("com.android.incallui/.InCallActivity");


    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        Toast.makeText(this, "Service Connected ", Toast.LENGTH_SHORT).show();

        //Configure these here for compatibility with API 13 and below.
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);


    }


//    @Override
//    protected boolean onKeyEvent(KeyEvent event) {
//        if (event.getKeyCode() == KeyEvent.KEYCODE_HOME)
//            return true;
//        else
//            return super.onKeyEvent(event);
//    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            if (event.getAction() == 1452) {
//                Timber.d("Custom Event");
                if (serviceConnectedListener != null) {
                    serviceConnectedListener.serviceConnected(true);
//                    Timber.d("call back fire");
                }
                return;
            }


//            performGlobalAction(GLOBAL_ACTION_RECENTS);


            if (event.getPackageName() != null && event.getClassName() != null) {
                ComponentName componentName = new ComponentName(event.getPackageName().toString(), event.getClassName().toString());
                ActivityInfo activityInfo = tryGetActivity(componentName);
                boolean isActivity = activityInfo != null;
                Timber.d("Activity: %s", componentName.flattenToShortString());

                if (!PrefUtils.getBooleanPref(this, EMERGENCY_FLAG)) {

                    if (PrefUtils.getBooleanPref(this, TOUR_STATUS)) {
                        Timber.d("Tour Completed");
                        if (isActivity) {
                            if (PrefUtils.getBooleanPref(this, IS_SETTINGS_ALLOW)) {
                                Timber.d("settings allowed");
                                if (ssPermissions.contains(componentName.flattenToShortString())) {
                                    Timber.d("activity allowed");
                                    AppConstants.TEMP_SETTINGS_ALLOWED = true;
                                } else {
                                    checkAppStatus(componentName);
                                }
                            } else if (PrefUtils.getBooleanPref(this, UNINSTALL_ALLOWED)) {
                                Timber.d("uninstall allowed");
                                if (smPermissions.contains(componentName.flattenToShortString())) {
                                    Timber.d("activity allowed");
                                    AppConstants.TEMP_SETTINGS_ALLOWED = true;
                                } else {
                                    Timber.d("activity not allowed");
                                    checkAppStatus(componentName);
                                }
                            } else if (PrefUtils.getBooleanPref(this, PERMISSION_GRANTING)) {
                                Timber.d("permission granting");
                                AppConstants.TEMP_SETTINGS_ALLOWED = true;
                            } else {
                                Timber.d("checking app permission");
                                checkAppStatus(componentName);
                            }
                        }
                    }
                }
            }
        }
    }


    private void checkAppStatus(ComponentName componentName) {


        if (componentName.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
            return;
        }

        if (globalActions.contains(componentName.flattenToShortString())) {
            AppConstants.TEMP_SETTINGS_ALLOWED = true;
            return;
        }

        if (callingApps.contains(componentName.getPackageName())) {
            AppConstants.TEMP_SETTINGS_ALLOWED = true;
            return;
        }

        Future<Boolean> futureObject = AppExecutor.getInstance().getSingleThreadExecutor()
                .submit(() -> isAllowed(WindowChangeDetectingService.this, componentName.getPackageName()));
        try {
            boolean status = futureObject.get();
            if (!status) {
                AppConstants.TEMP_SETTINGS_ALLOWED = false;
                clearRecentApp(this);
            } else {
                AppConstants.TEMP_SETTINGS_ALLOWED = true;
            }
        } catch (Exception e) {
            clearRecentApp(this);
        }
    }

    @Override
    public void onInterrupt() {

    }


    private Handler handler;


    private void clearRecentApp(Context context) {


        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        handler = new Handler();

        ActivityCompat.startForegroundService(this, new Intent(this, LockScreenService.class).setAction("add"));

        handler.postDelayed(() -> ActivityCompat.startForegroundService(WindowChangeDetectingService.this, new Intent(WindowChangeDetectingService.this, LockScreenService.class).setAction("remove")), 2000);


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