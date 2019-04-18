package com.screenlocker.secure.permissions;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.DEVICE_POLICY_SERVICE;
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
import static com.screenlocker.secure.utils.PermissionUtils.isPermissionGranted1;
import static com.screenlocker.secure.utils.PermissionUtils.permissionAdmin;
import static com.screenlocker.secure.utils.PermissionUtils.permissionModify1;
import static com.screenlocker.secure.utils.PermissionUtils.requestNotificationAccessibilityPermission;
import static com.screenlocker.secure.utils.PermissionUtils.requestNotificationAccessibilityPermission1;
import static com.screenlocker.secure.utils.PermissionUtils.requestUsageStatePermission1;

public class PermissionStep extends AbstractStep implements CompoundButton.OnCheckedChangeListener {


    private static final int DRAW_OVERLAY = 111;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;

    @Override
    public void onNext() {
        PrefUtils.saveIntegerPref(MyApplication.getAppContext(),DEF_PAGE_NO,1);
    }

    @Override
    public boolean nextIf() {
        return PrefUtils.getIntegerPref(MyApplication.getAppContext(),PERMISSIONS_NUMBER) ==8;

    }

    @Override
    public boolean setSkipable() {
        return false;
    }

    @Override
    public String name() {
        return "Permissions";
    }
    // step optional title (default: "Optional")


    @Override
    public String error() {
        return "Please Grant all permissions";
    }

    TextView textView;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.permmision_layout, container, false);
        ButterKnife.bind(this, v);
        init();
        if (PrefUtils.getBooleanPref(getContext(), PER_ADMIN)) {
            activeAdmin.setChecked(true);
            activeAdmin.setClickable(false);
        }
        activeAdmin.setOnCheckedChangeListener(this);
        if (PrefUtils.getBooleanPref(getContext(), PER_OVERLAY)) {
            drawoverlay.setChecked(true);
            drawoverlay.setClickable(false);
        }
        drawoverlay.setOnCheckedChangeListener(this);
        if (PrefUtils.getBooleanPref(getContext(), PER_MODIFIY)) {
            modifiSystemState.setChecked(true);
            modifiSystemState.setClickable(false);
        }
        modifiSystemState.setOnCheckedChangeListener(this);
        if (PrefUtils.getBooleanPref(getContext(), PER_USAGE)) {
            usageAccess.setChecked(true);
            usageAccess.setClickable(false);
        }
        usageAccess.setOnCheckedChangeListener(this);
        if (PrefUtils.getBooleanPref(getContext(), PER_RUNTIME)) {
            runtimePermissions.setChecked(true);
            runtimePermissions.setClickable(false);
        }
        runtimePermissions.setOnCheckedChangeListener(this);
        if (PrefUtils.getBooleanPref(getContext(), PER_UNKNOWN)) {
            unknownResources.setChecked(true);
            unknownResources.setClickable(false);
        }
        unknownResources.setOnCheckedChangeListener(this);
        if (PrefUtils.getBooleanPref(getContext(), PER_NOTIFICATION)) {
            notificationAccess.setChecked(true);
            notificationAccess.setClickable(false);

        }
        notificationAccess.setOnCheckedChangeListener(this);
        if (PrefUtils.getBooleanPref(getContext(), PER_BATTERY)) {
            batteryOptimization.setChecked(true);
            batteryOptimization.setClickable(false);

        }
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
            case RESULT_ENABLE:
                if (resultCode == RESULT_OK) {
                    activeAdmin.setChecked(true);
                    activeAdmin.setClickable(false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_ADMIN, true);
                    PrefUtils.saveIntegerPref(MyApplication.getAppContext(), PERMISSIONS_NUMBER, (PrefUtils.getIntegerPref(MyApplication.getAppContext(), PERMISSIONS_NUMBER) + 1));
                } else activeAdmin.setChecked(false);
                break;
            case DRAW_OVERLAY:
                if (Settings.canDrawOverlays(getContext())) {
                    drawoverlay.setChecked(true);
                    drawoverlay.setClickable(false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_OVERLAY, true);
                    PrefUtils.saveIntegerPref(MyApplication.getAppContext(), PERMISSIONS_NUMBER, (PrefUtils.getIntegerPref(MyApplication.getAppContext(), PERMISSIONS_NUMBER) + 1));
                } else drawoverlay.setChecked(false);
                break;
            case CODE_MODIFY_SYSTEMS_STATE:
                if (android.provider.Settings.System.canWrite(getActivity())) {
                    modifiSystemState.setChecked(true);
                    modifiSystemState.setClickable(false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_MODIFIY, true);
                    PrefUtils.saveIntegerPref(MyApplication.getAppContext(), PERMISSIONS_NUMBER, (PrefUtils.getIntegerPref(MyApplication.getAppContext(), PERMISSIONS_NUMBER) + 1));
                } else modifiSystemState.setChecked(false);
                break;
            case CODE_USAGE_ACCESS:
                if (isAccessGranted(getActivity())) {
                    usageAccess.setChecked(true);
                    usageAccess.setClickable(false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_USAGE, true);
                    PrefUtils.saveIntegerPref(MyApplication.getAppContext(), PERMISSIONS_NUMBER, (PrefUtils.getIntegerPref(MyApplication.getAppContext(), PERMISSIONS_NUMBER) + 1));
                } else usageAccess.setChecked(false);
                break;
            case CODE_UNKNOWN_RESOURCES:
                if (resultCode == RESULT_OK) {

                    unknownResources.setChecked(true);
                    unknownResources.setClickable(false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_UNKNOWN, true);
                    PrefUtils.saveIntegerPref(MyApplication.getAppContext(), PERMISSIONS_NUMBER, (PrefUtils.getIntegerPref(MyApplication.getAppContext(), PERMISSIONS_NUMBER) + 1));
                } else unknownResources.setChecked(false);
                break;
            case NOFICATION_REQUEST:
                Set<String> abc = NotificationManagerCompat
                        .getEnabledListenerPackages(MyApplication.getAppContext());
                if (abc.contains(MyApplication.getAppContext().getPackageName())) {
                    notificationAccess.setChecked(true);
                    notificationAccess.setClickable(false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_NOTIFICATION, true);
                    PrefUtils.saveIntegerPref(MyApplication.getAppContext(), PERMISSIONS_NUMBER, (PrefUtils.getIntegerPref(MyApplication.getAppContext(), PERMISSIONS_NUMBER) + 1));
                } else notificationAccess.setChecked(false);
                break;
            case CODE_BATERY_OPTIMIZATION:
                if (resultCode == RESULT_OK){
                    batteryOptimization.setChecked(true);
                    batteryOptimization.setClickable(false);
                    PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_BATTERY, true);
                    PrefUtils.saveIntegerPref(MyApplication.getAppContext(), PERMISSIONS_NUMBER, (PrefUtils.getIntegerPref(MyApplication.getAppContext(), PERMISSIONS_NUMBER) + 1));
                }else batteryOptimization.setChecked(false);

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_PHONE_STATE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                runtimePermissions.setChecked(true);
                runtimePermissions.setClickable(false);
                PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PER_RUNTIME, true);
                PrefUtils.saveIntegerPref(MyApplication.getAppContext(), PERMISSIONS_NUMBER, (PrefUtils.getIntegerPref(MyApplication.getAppContext(), PERMISSIONS_NUMBER) + 1));
            } else {
                runtimePermissions.setChecked(false);

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
