package com.screenlocker.secure.permissions;

import android.Manifest;
import android.annotation.TargetApi;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.DEVICE_POLICY_SERVICE;
import static android.view.View.GONE;
import static com.screenlocker.secure.utils.AppConstants.CODE_BATERY_OPTIMIZATION;
import static com.screenlocker.secure.utils.AppConstants.CODE_MODIFY_SYSTEMS_STATE;
import static com.screenlocker.secure.utils.AppConstants.CODE_UNKNOWN_RESOURCES;
import static com.screenlocker.secure.utils.AppConstants.CODE_USAGE_ACCESS;
import static com.screenlocker.secure.utils.AppConstants.DEF_PAGE_NO;
import static com.screenlocker.secure.utils.AppConstants.NOFICATION_REQUEST;
import static com.screenlocker.secure.utils.AppConstants.PERMISSIONS_NUMBER;
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
import static com.screenlocker.secure.utils.PermissionUtils.requestNotificationAccessibilityPermission;
import static com.screenlocker.secure.utils.PermissionUtils.requestNotificationAccessibilityPermission1;
import static com.screenlocker.secure.utils.PermissionUtils.requestUsageStatePermission1;

public class PermissionStepFragment extends AbstractStep implements CompoundButton.OnCheckedChangeListener {


    private static final int DRAW_OVERLAY = 111;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;


    /**
    * This method is called when user clicks the next button.
     *
     *  User is only allowed to move to next step if he/she has granted all the permissions
     *
    */
    @Override
    public boolean nextIf() {
        if (PrefUtils.getBooleanPref(MyApplication.getAppContext(), PER_ADMIN) &&
                PrefUtils.getBooleanPref(MyApplication.getAppContext(), PER_OVERLAY) &&
                PrefUtils.getBooleanPref(MyApplication.getAppContext(), PER_MODIFIY) &&
                PrefUtils.getBooleanPref(MyApplication.getAppContext(), PER_USAGE) &&
                PrefUtils.getBooleanPref(MyApplication.getAppContext(), PER_RUNTIME) &&
                PrefUtils.getBooleanPref(MyApplication.getAppContext(), PER_UNKNOWN) &&
                PrefUtils.getBooleanPref(MyApplication.getAppContext(), PER_NOTIFICATION) &&
                PrefUtils.getBooleanPref(MyApplication.getAppContext(), PER_BATTERY)) {
            PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 1);
            //all the permissions are granted, can move t0o next
            return true;
        }
        return false;

    }

    // user can,'t skip this
    @Override
    public boolean setSkipable() {
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
        return getResources().getString(R.string.please_grant_all_permissions);
    }

    /**
    * Bind all views
    */
    @BindView(R.id.active_admin) Switch activeAdmin;
    @BindView(R.id.active_drawoverlay) Switch drawoverlay;
    @BindView(R.id.active_modify) Switch modifiSystemState;
    @BindView(R.id.active_usage) Switch usageAccess;
    @BindView(R.id.active_runtime) Switch runtimePermissions;
    @BindView(R.id.active_unknown) Switch unknownResources;
    @BindView(R.id.active_notification) Switch notificationAccess;
    @BindView(R.id.active_battery_optimization) Switch batteryOptimization;
    /**
    * Layout references to hide permission which are already granted
    */
    @BindView(R.id.layout_allow_overlay) LinearLayout layoutOverLay;
    @BindView(R.id.layout_allow_usage) LinearLayout layoutUsage;
    @BindView(R.id.layout_allow_admin) LinearLayout layoutAdmin;
    @BindView(R.id.layout_allow_ignorebattery) LinearLayout layoutIgnore;
    @BindView(R.id.layout_allow_modify_sytem) LinearLayout layoutModifySystem;
    @BindView(R.id.layout_allow_installPackages) LinearLayout layoutInstall;
    @BindView(R.id.layout_allow_runtime) LinearLayout layoutRuntime;
    @BindView(R.id.layout_allow_notification_acces) LinearLayout layoutNotification;

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.permmision_layout, container, false);
        ButterKnife.bind(this, v);
        init();
        //check if user already granted the permission
        if (devicePolicyManager.isAdminActive(compName)) {
            activeAdmin.setChecked(true);
            activeAdmin.setClickable(false);
            PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_ADMIN, true);
            layoutAdmin.setVisibility(GONE);
        } else PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_ADMIN, false);
        activeAdmin.setOnCheckedChangeListener(this);
        if (Settings.canDrawOverlays(getContext())) {
            layoutOverLay.setVisibility(GONE);
            drawoverlay.setChecked(true);
            drawoverlay.setClickable(false);
            PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_OVERLAY, true);
        } else PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_OVERLAY, false);
        drawoverlay.setOnCheckedChangeListener(this);
        if (Settings.System.canWrite(getActivity())) {
            layoutModifySystem.setVisibility(GONE);
            modifiSystemState.setChecked(true);
            modifiSystemState.setClickable(false);
            PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_MODIFIY, true);
        } else PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_MODIFIY, false);
        modifiSystemState.setOnCheckedChangeListener(this);
        if (isAccessGranted(getContext())) {
            layoutUsage.setVisibility(GONE);
            usageAccess.setChecked(true);
            usageAccess.setClickable(false);
            PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_USAGE, true);
        } else PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_USAGE, false);
        usageAccess.setOnCheckedChangeListener(this);
        if (getActivity().checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
                getActivity().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            runtimePermissions.setChecked(true);
            layoutRuntime.setVisibility(GONE);
            runtimePermissions.setClickable(false);
            PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_RUNTIME, true);
        } else PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_RUNTIME, false);
        runtimePermissions.setOnCheckedChangeListener(this);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            if (MyApplication.getAppContext().getPackageManager().canRequestPackageInstalls()) {
                layoutInstall.setVisibility(GONE);
                unknownResources.setChecked(true);
                unknownResources.setClickable(false);
                PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_UNKNOWN, true);
            } else PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_UNKNOWN, false);
        }else{
            layoutInstall.setVisibility(GONE);
            unknownResources.setChecked(true);
            unknownResources.setClickable(false);
            PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_UNKNOWN, true);
        }

        unknownResources.setOnCheckedChangeListener(this);
        if (isNotificationAccess(getContext())) {
            layoutNotification.setVisibility(GONE);
            notificationAccess.setChecked(true);
            notificationAccess.setClickable(false);
            PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_NOTIFICATION, true);
        } else PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_NOTIFICATION, false);
        notificationAccess.setOnCheckedChangeListener(this);
        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        if (pm.isIgnoringBatteryOptimizations(MyApplication.getAppContext().getPackageName())) {
            layoutIgnore.setVisibility(GONE);
            batteryOptimization.setChecked(true);
            batteryOptimization.setClickable(false);
            PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_BATTERY, true);
        } else PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_BATTERY, false);
        batteryOptimization.setOnCheckedChangeListener(this);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
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

            }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            //handle results from permission settings
            case RESULT_ENABLE:
                if (resultCode == RESULT_OK) {
                    activeAdmin.setChecked(true);
                    activeAdmin.setClickable(false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_ADMIN, true);
                } else activeAdmin.setChecked(false);
                break;
            case DRAW_OVERLAY:
                if (Settings.canDrawOverlays(getContext())) {
                    drawoverlay.setChecked(true);
                    drawoverlay.setClickable(false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_OVERLAY, true);
                } else drawoverlay.setChecked(false);
                break;
            case CODE_MODIFY_SYSTEMS_STATE:
                if (android.provider.Settings.System.canWrite(getActivity())) {
                    modifiSystemState.setChecked(true);
                    modifiSystemState.setClickable(false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_MODIFIY, true);
                } else modifiSystemState.setChecked(false);
                break;
            case CODE_USAGE_ACCESS:
                if (isAccessGranted(getActivity())) {
                    usageAccess.setChecked(true);
                    usageAccess.setClickable(false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_USAGE, true);
                } else usageAccess.setChecked(false);
                break;
            case CODE_UNKNOWN_RESOURCES:
                if (resultCode == RESULT_OK) {
                    unknownResources.setChecked(true);
                    unknownResources.setClickable(false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_UNKNOWN, true);
                } else unknownResources.setChecked(false);
                break;
            case NOFICATION_REQUEST:
                Set<String> abc = NotificationManagerCompat
                        .getEnabledListenerPackages(MyApplication.getAppContext());
                if (abc.contains(MyApplication.getAppContext().getPackageName())) {
                    notificationAccess.setChecked(true);
                    notificationAccess.setClickable(false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_NOTIFICATION, true);
                } else notificationAccess.setChecked(false);
                break;
            case CODE_BATERY_OPTIMIZATION:
                if (resultCode == RESULT_OK) {
                    batteryOptimization.setChecked(true);
                    batteryOptimization.setClickable(false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_BATTERY, true);
                } else batteryOptimization.setChecked(false);

                break;
            case 1245:
                if (getActivity().checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED &&
                        getActivity().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                        getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    runtimePermissions.setChecked(true);
                    runtimePermissions.setClickable(false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_RUNTIME, true);
                } else PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_RUNTIME, false);
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
                runtimePermissions.setChecked(true);
                runtimePermissions.setClickable(false);
                PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_RUNTIME, true);
            } else {
                runtimePermissions.setChecked(false);

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
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void requestUnknownResouirces() {
        boolean isNonPlayAppAllowed = MyApplication.getAppContext().getPackageManager().canRequestPackageInstalls();
        if (!isNonPlayAppAllowed) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            intent.setData(Uri.parse("package:" + MyApplication.getAppContext().getPackageName()));
            startActivityForResult(intent, CODE_UNKNOWN_RESOURCES);
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
}
