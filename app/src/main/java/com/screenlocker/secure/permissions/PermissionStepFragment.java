package com.screenlocker.secure.permissions;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.service.apps.WindowChangeDetectingService;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.secure.launcher.BuildConfig;
import com.secure.launcher.R;

import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.DEVICE_POLICY_SERVICE;
import static android.view.View.GONE;
import static com.screenlocker.secure.utils.AppConstants.CODE_ACCESSIBILITY;
import static com.screenlocker.secure.utils.AppConstants.CODE_BATERY_OPTIMIZATION;
import static com.screenlocker.secure.utils.AppConstants.CODE_MODIFY_SYSTEMS_STATE;
import static com.screenlocker.secure.utils.AppConstants.CODE_UNKNOWN_RESOURCES;
import static com.screenlocker.secure.utils.AppConstants.CODE_USAGE_ACCESS;
import static com.screenlocker.secure.utils.AppConstants.DEF_PAGE_NO;
import static com.screenlocker.secure.utils.AppConstants.NOFICATION_REQUEST;
import static com.screenlocker.secure.utils.AppConstants.PER_ACCESS;
import static com.screenlocker.secure.utils.AppConstants.PER_ADMIN;
import static com.screenlocker.secure.utils.AppConstants.PER_BATTERY;
import static com.screenlocker.secure.utils.AppConstants.PER_MODIFIY;
import static com.screenlocker.secure.utils.AppConstants.PER_NOTIFICATION;
import static com.screenlocker.secure.utils.AppConstants.PER_OVERLAY;
import static com.screenlocker.secure.utils.AppConstants.PER_RUNTIME;
import static com.screenlocker.secure.utils.AppConstants.PER_UNKNOWN;
import static com.screenlocker.secure.utils.AppConstants.PER_USAGE;
import static com.screenlocker.secure.utils.AppConstants.REQUEST_READ_PHONE_STATE;
import static com.screenlocker.secure.utils.AppConstants.RESULT_ENABLE;
import static com.screenlocker.secure.utils.PermissionUtils.isAccessGranted;
import static com.screenlocker.secure.utils.PermissionUtils.isNotificationAccess;
import static com.screenlocker.secure.utils.PermissionUtils.isPermissionGranted1;
import static com.screenlocker.secure.utils.PermissionUtils.permissionAdmin;
import static com.screenlocker.secure.utils.PermissionUtils.permissionModify1;
import static com.screenlocker.secure.utils.PermissionUtils.requestNotificationAccessibilityPermission1;
import static com.screenlocker.secure.utils.PermissionUtils.requestUsageStatePermission1;
import static com.screenlocker.secure.utils.Utils.isAccessServiceEnabled;

public class PermissionStepFragment extends AbstractStep implements CompoundButton.OnCheckedChangeListener {


    private static final int DRAW_OVERLAY = 111;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;


    /**
     * This method is called when user clicks the next button.
     * <p>
     * User is only allowed to move to next step if he/she has granted all the permissions
     */


    @Override
    public boolean nextIf() {

        if (checkPermissions(MyApplication.getAppContext())) {
            PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 1);
            //all the permissions are granted, can move to next
            return true;
        } else {

            init();
            // setting listeners
            if (activeAdmin != null)
                activeAdmin.setOnCheckedChangeListener(this);
            if (drawoverlay != null)
                drawoverlay.setOnCheckedChangeListener(this);
            if (usageAccess != null)
                usageAccess.setOnCheckedChangeListener(this);
            if (runtimePermissions != null)
                runtimePermissions.setOnCheckedChangeListener(this);
            if (unknownResources != null)
                unknownResources.setOnCheckedChangeListener(this);
            if (notificationAccess != null)
                notificationAccess.setOnCheckedChangeListener(this);
            if (batteryOptimization != null)
                batteryOptimization.setOnCheckedChangeListener(this);
            if (accessibilityService != null)
                accessibilityService.setOnCheckedChangeListener(this);
            if (modifiSystemState != null)
                modifiSystemState.setOnCheckedChangeListener(this);
            showPermissionsMenus();
        }
        return false;


    }


    @Override
    public void onResume() {
        super.onResume();

        AppConstants.TEMP_SETTINGS_ALLOWED = true;
        //check if user already granted the permission

//        if (!checkPermissions(MyApplication.getAppContext())) {
//            showPermissionsMenus();
//            Toast.makeText(mStepper, "Permissions not allowed ", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(mStepper, "Permissions are allowed ", Toast.LENGTH_SHORT).show();
//
//        }
//
//        if (checkPermissions(MyApplication.getAppContext())) {
//            Toast.makeText(mStepper, "Please click on next button.", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(mStepper, "Some permissions missing", Toast.LENGTH_SHORT).show();
//            showPermissionsMenus();
//        }
//

//        if (!PrefUtils.getBooleanPref(MyApplication.getAppContext(), TOUR_STATUS)) {
//            if (checkPermissions(MyApplication.getAppContext())) {
//                if (mListener != null) {
//                    mListener.onPageUpdate(1);
//                    PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 1);
//                }
//
//            }
//
//        }


    }

    // user can,'t skip this
    @Override
    public boolean isSkipable() {
        return false;
    }

    @Override
    public boolean isPreviousAllow() {
        return false;
    }


    //return title for activity
    @Override
    public String name() {
        return "Permissions";
    }
    // step optional title (default: "Optional")

    // error message if user try to next without granting permission
    @Override
    public String error() {
        return MyApplication.getAppContext().getResources().getString(R.string.please_grant_all_permissions);
    }

    /**
     * Bind all views
     */
    @BindView(R.id.active_admin)
    Switch activeAdmin;
    @BindView(R.id.active_drawoverlay)
    Switch drawoverlay;
    @BindView(R.id.active_modify)
    Switch modifiSystemState;
    @BindView(R.id.active_usage)
    Switch usageAccess;
    @BindView(R.id.active_runtime)
    Switch runtimePermissions;
    @BindView(R.id.active_unknown)
    Switch unknownResources;
    @BindView(R.id.active_notification)
    Switch notificationAccess;
    @BindView(R.id.active_battery_optimization)
    Switch batteryOptimization;
    @BindView(R.id.active_accessibility)
    Switch accessibilityService;


    /**
     * Layout references to hide permission which are already granted
     */
    @BindView(R.id.layout_allow_overlay)
    LinearLayout layoutOverLay;
    @BindView(R.id.layout_allow_usage)
    LinearLayout layoutUsage;
    @BindView(R.id.layout_allow_admin)
    LinearLayout layoutAdmin;
    @BindView(R.id.layout_allow_ignorebattery)
    LinearLayout layoutIgnore;
    @BindView(R.id.layout_allow_modify_sytem)
    LinearLayout layoutModifySystem;
    @BindView(R.id.layout_allow_installPackages)
    LinearLayout layoutInstall;
    @BindView(R.id.layout_allow_runtime)
    LinearLayout layoutRuntime;
    @BindView(R.id.layout_allow_notification_acces)
    LinearLayout layoutNotification;
    @BindView(R.id.layout_accessibility)
    LinearLayout layoutAccessibility;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.permmision_layout, container, false);
        ButterKnife.bind(this, v);


        init();

        // setting listeners

        activeAdmin.setOnCheckedChangeListener(this);
        drawoverlay.setOnCheckedChangeListener(this);
        usageAccess.setOnCheckedChangeListener(this);
        runtimePermissions.setOnCheckedChangeListener(this);
        unknownResources.setOnCheckedChangeListener(this);
        notificationAccess.setOnCheckedChangeListener(this);
        batteryOptimization.setOnCheckedChangeListener(this);
        accessibilityService.setOnCheckedChangeListener(this);
        modifiSystemState.setOnCheckedChangeListener(this);

        //check if user already granted the permission
        showPermissionsMenus();

        return v;
    }


    private boolean checkPermissions(Context context) {

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        compName = new ComponentName(context, MyAdmin.class);
        devicePolicyManager = (DevicePolicyManager) context.getSystemService(DEVICE_POLICY_SERVICE);


        if (!devicePolicyManager.isAdminActive(compName)) {
            return false;
        } else if (!Settings.canDrawOverlays(context)) {
            return false;
        } else if (!Settings.System.canWrite(context)) {
            return false;
        } else if (!isAccessGranted(context)) {
            return false;
        } else if (context.checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
                context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else if (!isAccessServiceEnabled(context, WindowChangeDetectingService.class)) {
            return false;
        }
//
        else if (!isNotificationAccess(context)) {
            return false;
        } else if (!pm.isIgnoringBatteryOptimizations(MyApplication.getAppContext().getPackageName())) {
            return false;
        } else
            return Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.getPackageManager().canRequestPackageInstalls();
    }

    private void showPermissionsMenus() {

        // 1
        if (devicePolicyManager.isAdminActive(compName)) {
            setCheckedAndClickAble(activeAdmin, false, true);
            layoutAdmin.setVisibility(GONE);
        } else {
            setCheckedAndClickAble(activeAdmin, true, false);
            layoutAdmin.setVisibility(View.VISIBLE);
        }
        //2
        if (Settings.canDrawOverlays(getContext())) {
            layoutOverLay.setVisibility(GONE);
            setCheckedAndClickAble(drawoverlay, false, true);

        } else {
            layoutOverLay.setVisibility(View.VISIBLE);
            setCheckedAndClickAble(drawoverlay, true, false);
        }
        //3
        if (Settings.System.canWrite(getActivity())) {
            layoutModifySystem.setVisibility(GONE);
            setCheckedAndClickAble(modifiSystemState, false, true);
        } else {
            layoutModifySystem.setVisibility(View.VISIBLE);
            setCheckedAndClickAble(modifiSystemState, true, false);
        }
        //4
        if (isAccessGranted(MyApplication.getAppContext())) {
            layoutUsage.setVisibility(GONE);
            setCheckedAndClickAble(usageAccess, false, true);
        } else {
            layoutUsage.setVisibility(View.VISIBLE);
            setCheckedAndClickAble(usageAccess, true, false);
        }
        //5
        if (getActivity() != null && getActivity().checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
                getActivity().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            layoutRuntime.setVisibility(GONE);
            setCheckedAndClickAble(runtimePermissions, false, true);
        } else {
            layoutRuntime.setVisibility(View.VISIBLE);
            setCheckedAndClickAble(runtimePermissions, true, false);
        }

        //6
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (MyApplication.getAppContext().getPackageManager().canRequestPackageInstalls()) {
                layoutInstall.setVisibility(GONE);
                setCheckedAndClickAble(unknownResources, false, true);
            } else {
                layoutInstall.setVisibility(View.VISIBLE);
                setCheckedAndClickAble(unknownResources, true, false);
            }
        } else {
            layoutInstall.setVisibility(GONE);
            setCheckedAndClickAble(unknownResources, true, false);
        }
        //7
        if (isNotificationAccess(getContext())) {
            layoutNotification.setVisibility(GONE);
            setCheckedAndClickAble(notificationAccess, false, true);
        } else {
            layoutNotification.setVisibility(View.VISIBLE);
            setCheckedAndClickAble(notificationAccess, true, false);
        }
        //8
        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        if (pm.isIgnoringBatteryOptimizations(getContext().getPackageName())) {
            layoutIgnore.setVisibility(GONE);
            setCheckedAndClickAble(batteryOptimization, false, true);
        } else {
            layoutIgnore.setVisibility(View.VISIBLE);
            setCheckedAndClickAble(batteryOptimization, true, false);
        }
        //9
        if (isAccessServiceEnabled(MyApplication.getAppContext(), WindowChangeDetectingService.class)) {
            layoutAccessibility.setVisibility(GONE);
            setCheckedAndClickAble(batteryOptimization, false, true);
        } else {
            layoutAccessibility.setVisibility(View.VISIBLE);
            setCheckedAndClickAble(batteryOptimization, true, false);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }


    private OnPageUpdateListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (OnPageUpdateListener) context;
        } catch (Exception ignored) {

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked)
            //request permission according to user clicks
            switch (buttonView.getId()) {
                case R.id.active_admin:
                    permissionAdmin(this, devicePolicyManager, compName);
                    break;
                case R.id.active_drawoverlay:
                    requestOverlayPermission();
                    break;
                case R.id.active_modify:
                    permissionModify1(getActivity(), this);
                    break;
                case R.id.active_usage:
                    requestUsageStatePermission1(getContext(), this);
                    break;
                case R.id.active_runtime:
                    isPermissionGranted1(getActivity(), this);
                    break;
                case R.id.active_unknown:
                    requestUnknownResouirces();
                    break;
                case R.id.active_notification:
                    requestNotificationAccessibilityPermission1(getContext(), this);
                    break;
                case R.id.active_battery_optimization:
                    startActivityForResult(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getContext().getPackageName())),
                            CODE_BATERY_OPTIMIZATION);
                    break;
                case R.id.active_accessibility:
                    if (!isAccessServiceEnabled(MyApplication.getAppContext(), WindowChangeDetectingService.class)) {
                        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivityForResult(intent, CODE_ACCESSIBILITY);
                    }
                    break;
            }
    }

    private void setCheckedAndClickAble(Switch item, boolean click, boolean check) {

        if (click) {
            if (!item.isClickable()) {
                item.setClickable(click);
            }
        } else {
            if (item.isClickable()) {
                item.setClickable(click);
            }
        }
        if (check) {
            if (!item.isChecked()) {
                item.setChecked(check);
            }
        } else {
            if (item.isChecked()) {
                item.setChecked(check);
            }
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            //handle results from permission settings
            case RESULT_ENABLE:
                if (resultCode == RESULT_OK) {
                    setCheckedAndClickAble(activeAdmin, false, true);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_ADMIN, true);
                } else {
                    setCheckedAndClickAble(activeAdmin, true, false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_ADMIN, false);
                }
                break;
            case DRAW_OVERLAY:
                if (Settings.canDrawOverlays(getContext())) {
                    setCheckedAndClickAble(drawoverlay, false, true);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_OVERLAY, true);
                } else {
                    setCheckedAndClickAble(drawoverlay, true, false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_OVERLAY, false);
                }

                break;
            case CODE_MODIFY_SYSTEMS_STATE:
                if (android.provider.Settings.System.canWrite(getActivity())) {
                    setCheckedAndClickAble(modifiSystemState, false, true);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_MODIFIY, true);
                } else {
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_MODIFIY, false);
                    setCheckedAndClickAble(modifiSystemState, true, false);
                }
                break;
            case CODE_USAGE_ACCESS:
                if (isAccessGranted(getActivity())) {
                    setCheckedAndClickAble(usageAccess, false, true);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_USAGE, true);
                } else {
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_USAGE, false);
                    setCheckedAndClickAble(usageAccess, true, false);
                }
                break;
            case CODE_UNKNOWN_RESOURCES:
                if (resultCode == RESULT_OK) {
                    setCheckedAndClickAble(unknownResources, false, true);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_UNKNOWN, true);
                } else {
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_UNKNOWN, false);
                    setCheckedAndClickAble(unknownResources, true, false);
                }
                break;
            case NOFICATION_REQUEST:
                Set<String> abc = NotificationManagerCompat
                        .getEnabledListenerPackages(MyApplication.getAppContext());
                if (abc.contains(MyApplication.getAppContext().getPackageName())) {
                    setCheckedAndClickAble(notificationAccess, false, true);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_NOTIFICATION, true);
                } else {
                    setCheckedAndClickAble(notificationAccess, true, false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_NOTIFICATION, false);
                }
                break;
            case CODE_BATERY_OPTIMIZATION:
                if (resultCode == RESULT_OK) {
                    setCheckedAndClickAble(batteryOptimization, false, true);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_BATTERY, true);
                } else {
                    setCheckedAndClickAble(batteryOptimization, true, false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_BATTERY, false);
                }
                break;
            case 1245:
                if (getActivity() != null && getActivity().checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
                        getActivity().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                        getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    setCheckedAndClickAble(runtimePermissions, false, true);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_RUNTIME, true);
                } else {
                    setCheckedAndClickAble(runtimePermissions, true, false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_RUNTIME, false);
                }
                break;
            case CODE_ACCESSIBILITY:

                if (isAccessServiceEnabled(MyApplication.getAppContext(), WindowChangeDetectingService.class)) {
                    setCheckedAndClickAble(accessibilityService, false, true);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_ACCESS, true);
                } else {
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_ACCESS, false);
                    setCheckedAndClickAble(accessibilityService, true, false);

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_PHONE_STATE) {
            //runtime permission result
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                setCheckedAndClickAble(runtimePermissions, false, true);
                PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_RUNTIME, true);
            } else {
                setCheckedAndClickAble(runtimePermissions, true, false);
                PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_RUNTIME, false);


                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CALL_PHONE) && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_PHONE_STATE) && ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_PHONE_STATE)) {
                    // now, user has denied permission (but not permanently!)

                } else {

                    // now, user has denied permission permanently!

                    startActivityForResult(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)), 1245);
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void requestOverlayPermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (!Settings.canDrawOverlays(getContext())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + MyApplication.getAppContext().getPackageName()));
            startActivityForResult(intent, DRAW_OVERLAY);
        } else {
            setCheckedAndClickAble(drawoverlay, false, true);
            PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_OVERLAY, true);
        }
    }


    private void requestUnknownResouirces() {
        boolean isNonPlayAppAllowed;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            isNonPlayAppAllowed = MyApplication.getAppContext().getPackageManager().canRequestPackageInstalls();
            if (!isNonPlayAppAllowed) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                intent.setData(Uri.parse("package:" + MyApplication.getAppContext().getPackageName()));
                startActivityForResult(intent, CODE_UNKNOWN_RESOURCES);
            }
        }

    }

    private void init() {
        if (devicePolicyManager == null) {
            devicePolicyManager = (DevicePolicyManager) getContext().getSystemService(DEVICE_POLICY_SERVICE);
        }
        if (compName == null) {
            compName = new ComponentName(getActivity(), MyAdmin.class);
        }

    }

    @OnClick({R.id.layout_allow_admin,R.id.layout_allow_overlay,R.id.layout_allow_modify_sytem
            ,R.id.layout_allow_usage,R.id.layout_allow_runtime,R.id.layout_allow_installPackages,R.id.layout_allow_notification_acces
            ,R.id.layout_allow_ignorebattery,R.id.layout_accessibility})
    public void layoutClicked(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.layout_allow_admin:
                boolean isAdmin = PrefUtils.getBooleanPref(getActivity(), PER_ADMIN);
                if (!isAdmin) {
                    activeAdmin.setChecked(true);
                }

                break;
            case R.id.layout_allow_overlay:
                boolean isOverlay = PrefUtils.getBooleanPref(getActivity(), PER_OVERLAY);
                if (!isOverlay) {
                    drawoverlay.setChecked(true);
                }

                break;
            case R.id.layout_allow_modify_sytem:
                boolean isAllowModify = PrefUtils.getBooleanPref(getActivity(), PER_MODIFIY);
                if (!isAllowModify) {
                    modifiSystemState.setChecked(true);
                }
                break;
            case R.id.layout_allow_usage:
                boolean isUsage = PrefUtils.getBooleanPref(getActivity(), PER_USAGE);
                if (!isUsage) {
                    usageAccess.setChecked(true);
                }
                break;
            case R.id.layout_allow_runtime:
                boolean isRunTime = PrefUtils.getBooleanPref(getContext(), PER_RUNTIME);
                if (!isRunTime) {
                    runtimePermissions.setChecked(true);
                }
                break;
            case R.id.layout_allow_installPackages:
                boolean isUnknown = PrefUtils.getBooleanPref(getActivity(), PER_UNKNOWN);
                if (!isUnknown) {
                    unknownResources.setChecked(true);
                }
                break;
            case R.id.layout_allow_notification_acces:
                boolean isNotificationAccess = PrefUtils.getBooleanPref(getActivity(), PER_NOTIFICATION);
                if (!isNotificationAccess) {
                    notificationAccess.setChecked(true);
                }
                break;
            case R.id.layout_allow_ignorebattery:
                boolean isBattery = PrefUtils.getBooleanPref(getActivity(), PER_BATTERY);
                if (!isBattery) {
                    batteryOptimization.setChecked(true);
                }
                break;
            case R.id.layout_accessibility:
                if (!isAccessServiceEnabled(MyApplication.getAppContext(), WindowChangeDetectingService.class)) {
                    accessibilityService.setChecked(true);
                }
                break;

        }
    }
}
