//package com.screenlocker.secure.service.apps;
//
//import android.app.Activity;
//import android.app.ActivityManager;
//import android.app.usage.UsageEvents;
//import android.app.usage.UsageStatsManager;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Handler;
//import android.os.Looper;
//
//import androidx.core.app.ActivityCompat;
//
//import com.screenlocker.secure.app.MyApplication;
//import com.screenlocker.secure.launcher.AppInfo;
//import com.screenlocker.secure.room.MyAppDatabase;
//import com.screenlocker.secure.service.AppExecutor;
//import com.screenlocker.secure.service.LockScreenService;
//import com.screenlocker.secure.utils.AppConstants;
//import com.screenlocker.secure.utils.PrefUtils;
//import com.secure.launcher.BuildConfig;
//
//import org.apache.commons.collections4.queue.CircularFifoQueue;
//
//import java.util.HashSet;
//import java.util.Queue;
//import java.util.TimerTask;
//
//import timber.log.Timber;
//
//import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
//import static com.screenlocker.secure.utils.AppConstants.EMERGENCY_FLAG;
//import static com.screenlocker.secure.utils.AppConstants.IS_SETTINGS_ALLOW;
//import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
//import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
//import static com.screenlocker.secure.utils.AppConstants.KEY_SUPPORT_PASSWORD;
//import static com.screenlocker.secure.utils.AppConstants.PERMISSION_GRANTING;
//import static com.screenlocker.secure.utils.AppConstants.RESTRICTION_DELAY;
//import static com.screenlocker.secure.utils.AppConstants.TEMP_SETTINGS_ALLOWED;
//import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
//import static com.screenlocker.secure.utils.AppConstants.UNINSTALL_ALLOWED;
//
///**
// * @author : Muhammad Nadeem
// * Created at: 1/27/2020
// */
//public class RefreshTimerTask implements Runnable {
//    private UsageStatsManager usm;
//    private boolean aff1 = true;
//    private Context context;
//    private Handler handler;
//
//    private HashSet<String> ssPermissions = new HashSet<>();
//
//    private HashSet<String> smPermissions = new HashSet<>();
//
//    private HashSet<String> stepperPermissions = new HashSet<>();
//
//    private HashSet<String> globalActions = new HashSet<>();
//
//    private HashSet<String> callingApps = new HashSet<>();
//    private HashSet<String> previousTasksFor = new HashSet<>();
//    private Queue<String> fifo = new CircularFifoQueue<String>(2);
//
//
//    public RefreshTimerTask(Context context) {
//        this.context = context;
//        usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
//        setupStrings();
//
//    }
//
//    @Override
//    public void run() {
//        if (!PrefUtils.getBooleanPref(context, EMERGENCY_FLAG)) {
//
//            if (PrefUtils.getBooleanPref(context, TOUR_STATUS)) {
//                Timber.d("Tour Completed");
//                ComponentName componentName = m108a();
//
//                if (componentName != null) {
//                    String activity = componentName.flattenToShortString();
//                    Timber.d(activity);
//                    boolean break1 = false;
//                    if (PrefUtils.getBooleanPref(context, UNINSTALL_ALLOWED)) {
//                        Timber.d("uninstall allowed");
//                        if (smPermissions.contains(activity)) {
//                            Timber.d("activity allowed");
//                            AppConstants.TEMP_SETTINGS_ALLOWED = true;
//                        } else {
//                            Timber.d("activity not allowed");
//                            checkAppStatus(componentName);
//                        }
//                    }
//                    else if (PrefUtils.getBooleanPref(context, IS_SETTINGS_ALLOW)) {
//                        Timber.d("System settings allowed");
//                        if (ssPermissions.contains(activity)) {
//                            Timber.d("activity allowed");
//                            AppConstants.TEMP_SETTINGS_ALLOWED = true;
//                            //privousTrack = activity;
//                        } else if ("com.android.settings/.SubSettings".contains(activity)) {
//                            AppConstants.TEMP_SETTINGS_ALLOWED = false;
//                            for (String s : previousTasksFor) {
//                                if (fifo.contains(s)) {
//                                    Timber.d("SubActivity allowed allowed");
//                                    AppConstants.TEMP_SETTINGS_ALLOWED = true;
//                                }
//                            }
//                            if (!TEMP_SETTINGS_ALLOWED) {
//                                checkAppStatus(componentName);
//                            }
//
//                        } else {
//                            checkAppStatus(componentName);
//                        }
//
//                    }
//                    else if (PrefUtils.getBooleanPref(context, PERMISSION_GRANTING)) {
//                        Timber.d("permission granting");
//                        AppConstants.TEMP_SETTINGS_ALLOWED = true;
//                    } else {
//                        Timber.d("checking app permission");
//                        checkAppStatus(componentName);
//                    }
//                    if (!fifo.contains(activity)) {
//                        fifo.add(activity);
//                    }
//                }
//
//            }
//        }
//    }
//
//
//    public ComponentName m108a() {
//        long currentTimeMillis = System.currentTimeMillis();
//        UsageEvents queryEvents = usm.queryEvents(currentTimeMillis - ((long) (aff1 ? 60000 : 60000)), currentTimeMillis);
//        String str = null;
//        String str2 = null;
//        ComponentName componentName = null;
//        while (queryEvents.hasNextEvent()) {
//            UsageEvents.Event event = new UsageEvents.Event();
//            queryEvents.getNextEvent(event);
//            switch (event.getEventType()) {
//                case 1:
//                    str2 = event.getPackageName();
//                    str = event.getClassName();
//                    componentName = new ComponentName(str2, str);
//                    componentName.flattenToShortString();
//                    break;
//                case 2:
//                    if (!event.getPackageName().equals(str2)) {
//                        break;
//                    } else {
//                        str2 = null;
//                        break;
//                    }
//            }
//        }
//        return componentName;
//    }
//
//    private void setupStrings() {
//        ssPermissions.add("com.android.settings/.Settings$AccessibilitySettingsActivity");
//        ssPermissions.add("com.android.settings/.Settings$WifiSettingsActivity");
//        ssPermissions.add("com.android.settings/.ConfirmLockPassword$InternalActivity");
//        ssPermissions.add("com.android.settings/.ChooseLockGenericActivity");
//        ssPermissions.add("com.android.settings/.ChooseLockPassword");
//        ssPermissions.add("com.android.settings/.Settings$PowerUsageSummaryActivity");
//        ssPermissions.add("com.android.settings/.Settings$SoundSettingsActivity");
//        ssPermissions.add("com.android.settings/.Settings$DateTimeSettingsActivity");
//        ssPermissions.add("com.android.settings/.Settings$BluetoothSettingsActivity");
//        ssPermissions.add("com.android.settings/.Settings$SimSettingsActivity");
//        ssPermissions.add("com.android.settings/.applications.InstalledAppDetailsTop");
//        ssPermissions.add("com.android.settings/.Settings$LanguageAndInputSettingsActivity");
//        ssPermissions.add("com.android.settings/.Settings$DataUsageSummaryActivity");
//        ssPermissions.add("com.android.settings/.password.ChooseLockGeneric");
//        ssPermissions.add("com.android.settings/.EncryptionInterstitial");
//        ssPermissions.add("com.android.settings/.password.ChooseLockPattern");
//        ssPermissions.add("com.android.settings/.password.ChooseLockGeneric$InternalActivity");
//        ssPermissions.add("com.android.settings/.password.ConfirmLockPassword$InternalActivity");
//        ssPermissions.add("com.android.settings/.notification.RedactionInterstitial");
//        ssPermissions.add("com.android.settings/.fingerprint.FingerprintEnrollFindSensor");
//        ssPermissions.add("com.android.settings/.fingerprint.FingerprintEnrollEnrolling");
//        ssPermissions.add("com.android.settings/.password.ChooseLockPassword");
//        ssPermissions.add("com.android.settings/.fingerprint.FingerprintEnrollFinish");
//        ssPermissions.add("com.android.phone/.MobileNetworkSettings");
//        ssPermissions.add("com.android.settings/.Settings$TetherSettingsActivity");
//        ssPermissions.add("com.android.settings/.Settings$TetherWifiSettingsActivity");
//        ssPermissions.add("com.android.settings/.sim.SimPreferenceDialog");
//        ssPermissions.add("com.android.phone/android.app.ProgressDialog");
//        ssPermissions.add("com.android.providers.telephony/com.android.settings.Settings$ApnSettingsActivity");
//        ssPermissions.add("com.android.settings/.Settings$ApnEditorActivity");
//        ssPermissions.add("com.android.settings/.Settings$ApnSettingsActivity");
//
//
////
//        ssPermissions.add("com.google.android.packageinstaller/.permission.ui.GrantPermissionsActivity");
//        ssPermissions.add("com.android.packageinstaller/.permission.ui.GrantPermissionsActivity");
//        ssPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.permission.ui.GrantPermissionsActivity");
//
//        ssPermissions.add("com.google.android.packageinstaller/.permission.ui.Manage.PermissionsActivity");
//        ssPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.permission.ui.ManagePermissionsActivity");
//        ssPermissions.add("com.android.packageinstaller/.permission.ui.ManagePermissionsActivity");
//        ssPermissions.add("com.android.bluetooth/.opp.BluetoothOppTransferHistory");
//        ssPermissions.add("com.android.bluetooth/android.widget.FrameLayout");
//        ssPermissions.add("com.android.phone/.NetworkSetting");
//        ssPermissions.add("com.android.settings/android.app.AlertDialog");
//        ssPermissions.add("com.android.settings/.sim.SimDialogActivity");
//        ssPermissions.add("com.android.settings/.Settings$WifiApSettingsActivity");
//        ssPermissions.add("com.android.settings/.password.SetNewPasswordActivity");
//        ssPermissions.add("com.android.settings/.password.ConfirmLockPattern$InternalActivity");
//        ssPermissions.add("com.android.vpndialogs/.ConfirmDialog");
//
//        // samsung
//        ssPermissions.add("com.android.settings/.Settings$ConnectionsSettingsActivity");
//        ssPermissions.add("com.android.phone/com.samsung.telephony.phone.activities.SamsungMobileNetworkSettingsActivity");
//        ssPermissions.add("com.android.settings/com.samsung.android.settings.personalvibration.SelectPatternDialog");
//
//        // huwei
//        ssPermissions.add("com.android.phone/.MSimMobileNetworkSettings");
//        ssPermissions.add("com.android.phone/android.app.AlertDialog");
//
//        //SS notification related
//        ssPermissions.add("com.android.settings/.Settings$AppNotificationSettingsActivity");
//        ssPermissions.add("com.android.settings/.Settings$ConfigureNotificationSettingsActivity");
//        ssPermissions.add("android/com.android.internal.app.ResolverActivity");
//        ssPermissions.add("com.android.documentsui/.picker.PickActivity");
//        ssPermissions.add("com.android.settings/.bluetooth.BluetoothPairingDialog");
//        ssPermissions.add("com.android.settings/.Settings$WifiNetworkConnectActivity");
//
//
////        ssPermissions.add(BuildConfig.APPLICATION_ID + "/android.widget.FrameLayout");
//
//
//        // samsung
//        ssPermissions.add("com.samsung.networkui/android.widget.FrameLayout");
//        ssPermissions.add("com.samsung.networkui/.MobileNetworkSettings");
//        ssPermissions.add("com.samsung.crane/com.android.settings.Settings$ApnSettingsActivity");
//        ssPermissions.add("com.samsung.networkui/.MobileNetworkSettingsTab");
//        ssPermissions.add("com.samsung.android.app.telephonyui/.netsettings.ui.NetSettingsActivity");
//
//        ssPermissions.add("com.android.settings/com.samsung.android.settings.face.FaceLockSettings");
//        ssPermissions.add("com.android.settings/com.samsung.android.settings.biometrics.BiometricsDisclaimerActivity");
//
//
//        // huwei
//        ssPermissions.add("com.huawei.systemmanager/com.huawei.netassistant.ui.NetAssistantMainActivity");
//        ssPermissions.add("com.huawei.systemmanager/android.view.ViewGroup");
//        ssPermissions.add("com.huawei.systemmanager/.netassistant.ui.setting.TrafficSettingFragment$TrafficSettingActivity");
//        ssPermissions.add("com.huawei.systemmanager/.netassistant.ui.setting.OverLimitNotifyFragment$OverLimitNotifyActivity");
//        ssPermissions.add("com.huawei.systemmanager/.netassistant.traffic.trafficranking.TrafficRankingListActivity");
//        ssPermissions.add("com.hisi.mapcon/com.android.settings.Settings$ApnSettingsActivity");
//
//
//        // vivo
//        ssPermissions.add("com.android.wifisettings/.Settings$WifiSettingsActivity");
//        ssPermissions.add("com.android.wifisettings/android.widget.FrameLayout");
//        ssPermissions.add("com.android.wifisettings/.SubSettings");
//        ssPermissions.add("com.android.phone/com.android.settings.Settings$ApnSettingsActivity");
//        ssPermissions.add("com.android.settings/.ApnEditor");
//        ssPermissions.add("com.android.settings/com.vivo.settings.password.ConfirmVivoPin$InternalActivity");
//
//        // Mi
//        ssPermissions.add("com.android.settings/.MiuiSecurityChooseUnlock");
//        ssPermissions.add("com.miui.securitycenter/com.miui.powercenter.legacypowerrank.PowerConsumeRank");
//        ssPermissions.add("com.android.phone/.settings.MobileNetworkSettings");
//
//
//        ssPermissions.add(BuildConfig.APPLICATION_ID + "/com.secureSetting.SecureSettingsMain");
//        ssPermissions.add(BuildConfig.APPLICATION_ID + "/com.secureMarket.SecureMarketActivity");
//        ssPermissions.add(BuildConfig.APPLICATION_ID + "/com.screenlocker.secure.launcher.MainActivity");
//        ssPermissions.add(BuildConfig.APPLICATION_ID + "/com.screenlocker.secure.manual_load.ManualPullPush");
//        ssPermissions.add(BuildConfig.APPLICATION_ID + "/android.widget.FrameLayout");
//
//        // SM permission
//
//        smPermissions.add(BuildConfig.APPLICATION_ID + "/com.screenlocker.secure.manual_load.ManualPullPush");
//        smPermissions.add("com.android.packageinstaller/.UninstallerActivity");
//        smPermissions.add("com.google.android.packageinstaller/.UninstallerActivity");
//        smPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.UninstallerActivity");
//        smPermissions.add(" com.google.android.packageinstaller/com.android.packageinstaller.UninstallerActivity");
//        ;
//        smPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.InstallStart");
//
//
//        smPermissions.add("com.android.packageinstaller/.PackageInstallerActivity");
//        smPermissions.add("com.google.android.packageinstaller/.PackageInstallerActivity");
//        smPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.PackageInstallerActivity");
//
//
//        smPermissions.add("com.android.packageinstaller/.DeleteStagedFileOnResult");
//        smPermissions.add("com.google.android.packageinstaller/.DeleteStagedFileOnResult");
//        smPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.DeleteStagedFileOnResult");
//
//
//        smPermissions.add("com.android.packageinstaller/.InstallStaging");
//        smPermissions.add("com.google.android.packageinstaller/.InstallStaging");
//        smPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.InstallStaging");
//
//
//        smPermissions.add("com.android.packageinstaller/.InstallInstalling");
//        smPermissions.add("com.google.android.packageinstaller/.InstallInstalling");
//        smPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.InstallInstalling");
//
//
//        smPermissions.add("com.android.packageinstaller/android.widget.FrameLayout");
//        smPermissions.add("com.google.android.packageinstaller/android.widget.FrameLayout");
//        smPermissions.add("com.android.packageinstaller/android.app.AlertDialog");
//        smPermissions.add("com.google.android.packageinstaller/android.app.AlertDialog");
//        smPermissions.add("com.android.packageinstaller/com.android.packageinstaller.DeleteStagedFileOnResult");
//
//
//        smPermissions.add("com.android.packageinstaller/.UninstallUninstalling");
//        smPermissions.add("com.google.android.packageinstaller/.UninstallUninstalling");
//        smPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.UninstallUninstalling");
//
//
//        smPermissions.add("com.android.packageinstaller/.InstallSuccess");
//        smPermissions.add("com.google.android.packageinstaller/.InstallSuccess");
//        smPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.InstallSuccess");
//
//        smPermissions.add("com.android.packageinstaller/.InstallFailed");
//        smPermissions.add("com.google.android.packageinstaller/.InstallFailed");
//        smPermissions.add("com.google.android.packageinstaller/com.android.packageinstaller.InstallFailed");
//
//
////        wm.addView(mView, localLayoutParams);
//
//        stepperPermissions.add("com.google.android.packageinstaller/.permission.ui.GrantPermissionsActivity");
//        stepperPermissions.add("com.android.packageinstaller/.permission.ui.GrantPermissionsActivity");
//        stepperPermissions.add("com.google.android.packageinstaller/.permission.ui.ManagePermissionsActivity");
//        stepperPermissions.add("com.android.settings/.SubSettings");
//        stepperPermissions.add("com.android.settings/.Settings$AccessibilitySettingsActivity");
//        stepperPermissions.add(BuildConfig.APPLICATION_ID + "/com.screenlocker.secure.manual_load.ManualPullPush");
//
//
//        // some global actions
//        globalActions.add("com.android.systemui/.globalactions.GlobalActionsDialog$ExActionsDialog");
//        globalActions.add("com.android.settings/android.app.Dialog");
//        globalActions.add("com.android.settings/.bluetooth.RequestPermissionActivity");
//
//        globalActions.add("com.android.packageinstaller/.permission.ui.GrantPermissionsActivity");
//        globalActions.add("com.google.android.packageinstaller/.permission.ui.GrantPermissionsActivity");
//        globalActions.add("com.google.android.packageinstaller/com.android.packageinstaller.permission.ui.GrantPermissionsActivity");
//
//        globalActions.add("com.android.packageinstaller/.permission.ui.ManagePermissionsActivity");
//        globalActions.add("com.google.android.packageinstaller/.permission.ui.ManagePermissionsActivity");
//        globalActions.add("com.google.android.packageinstaller/com.android.packageinstaller.permission.ui.ManagePermissionsActivity");
//
//
//        globalActions.add("com.android.settings/.Settings$ManageAppExternalSourcesActivity");
//        globalActions.add("com.android.settings/.DeviceAdminAdd");
//        globalActions.add("com.android.settings/.applications.InstalledAppDetailsTop");
//        globalActions.add("com.android.vpndialogs/android.app.Dialog");
//        globalActions.add("com.android.settings/.Settings$AccessibilitySettingsActivity");
//
//
//        //calling app package name samsung
//        callingApps.add("com.samsung.android.incallui");
//        callingApps.add("com.android.incallui/.InCallActivity");
//
//        //Previous allowed subsetting activities
//        previousTasksFor.add("com.secure.launcher/com.screenlocker.secure.settings.SettingsActivity");
//        previousTasksFor.add("com.android.settings/.Settings$LanguageAndInputSettingsActivity");
//        previousTasksFor.add("com.android.settings/.Settings$SoundSettingsActivity");
////        previousTasksFor.add("com.android.settings/.Settings$PowerUsageSummaryActivity");
//        previousTasksFor.add("com.android.settings/.Settings$DateTimeSettingsActivity");
//        previousTasksFor.add("Settings$ConfigureNotificationSettingsActivity");
//    }
//
//    private void checkAppStatus(ComponentName componentName) {
//
//
//        if (componentName.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
//            return;
//        }
//
//        if (globalActions.contains(componentName.getPackageName())) {
//            AppConstants.TEMP_SETTINGS_ALLOWED = true;
//            return;
//        }
//
//        if (callingApps.contains(componentName.getPackageName())) {
//            AppConstants.TEMP_SETTINGS_ALLOWED = true;
//            return;
//        }
//
//        boolean status = isAllowed(context, componentName.getPackageName());
//
//
//        Timber.d("isAllowed : %s", status);
//        if (!status) {
//            AppConstants.TEMP_SETTINGS_ALLOWED = false;
//            clearRecentApp(context, componentName.getPackageName());
//        } else {
//            AppConstants.TEMP_SETTINGS_ALLOWED = true;
//        }
//
//    }
//
//    private boolean isAllowed(Context context, String packageName) {
//
//        String space = PrefUtils.getStringPref(context, CURRENT_KEY);
//        String currentSpace = (space == null) ? "" : space;
//        Timber.d("<<< QUERYING DATA >>>");
//        boolean status = false;
//
//        AppInfo info = MyAppDatabase.getInstance(context).getDao().getParticularApp(packageName);
//        if (info != null) {
//            if (currentSpace.equals(KEY_MAIN_PASSWORD) && (info.isEnable() && info.isEncrypted())) {
//                status = true;
//            } else if (currentSpace.equals(KEY_GUEST_PASSWORD) && (info.isEnable() && info.isGuest())) {
//                status = true;
//            } else
//                status = currentSpace.equals(KEY_SUPPORT_PASSWORD) && (packageName.equals(context.getPackageName()));
//
//        }
//
//
//        return status;
//    }
//
//    private void clearRecentApp(Context context, String packageName) {
//
//        Timber.d("clear recent app is calling ");
//
//        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
//        am.killBackgroundProcesses(packageName);
//
//        if (handler != null) {
//            handler.removeCallbacksAndMessages(null);
//            handler = null;
//        }
//        handler = new Handler(Looper.getMainLooper());
//
//        ActivityCompat.startForegroundService(context, new Intent(context, LockScreenService.class).setAction("add"));
//
//        handler.postDelayed(() -> ActivityCompat.startForegroundService(context, new Intent(context, LockScreenService.class).setAction("remove")), RESTRICTION_DELAY);
//
//
//    }
//
//}
