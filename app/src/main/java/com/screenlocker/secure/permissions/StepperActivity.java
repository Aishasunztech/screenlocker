package com.screenlocker.secure.permissions;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.R;
import com.screenlocker.secure.mdm.MainActivity;
import com.screenlocker.secure.settings.SettingContract;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.settings.SettingsModel;
import com.screenlocker.secure.settings.SettingsPresenter;
import com.screenlocker.secure.utils.PermissionUtils;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.ArrayList;

import me.drozdzynski.library.steppers.SteppersItem;
import me.drozdzynski.library.steppers.SteppersView;
import me.drozdzynski.library.steppers.interfaces.OnSkipStepAction;

import static com.screenlocker.secure.utils.AppConstants.CURRENT_STEP;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
import static com.screenlocker.secure.utils.PermissionUtils.isAccessGranted;
import static com.screenlocker.secure.utils.PermissionUtils.isPermissionGranted;
import static com.screenlocker.secure.utils.PermissionUtils.permissionAdmin;
import static com.screenlocker.secure.utils.PermissionUtils.permissionModify;
import static com.screenlocker.secure.utils.PermissionUtils.requestOverlayPermission;
import static com.screenlocker.secure.utils.PermissionUtils.requestUsageStatePermission;

public class StepperActivity extends AppCompatActivity implements SettingContract.SettingsMvpView {
    private static final int REQUEST_CODE_PASSWORD = 883;
    private SteppersView steppersView;
    boolean launcher = false;
    boolean clickStatus = false;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;

    private SettingsPresenter settingsPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stepper_layout);
        steppersView = findViewById(R.id.steppersView);
        SteppersView.Config steppersViewConfig = new SteppersView.Config();

        SettingsActivity settingsActivity = new SettingsActivity();

        settingsPresenter = new SettingsPresenter(this, new SettingsModel(this));

        steppersViewConfig.setOnCancelAction(this::finish);


        steppersViewConfig.setOnChangeStepAction((position, activeStep) -> {

//            Toast.makeText(StepperActivity.this,
//                    "Step changed to: " + activeStep.getLabel() + " (" + position + ")",
//                    Toast.LENGTH_SHORT).show();

        });

        steppersViewConfig.setFragmentManager(getSupportFragmentManager());
        ArrayList<SteppersItem> steps = new ArrayList<>();

        SteppersItem permissions = new SteppersItem();
        permissions.setLabel("Permissions");
        permissions.setPositiveButtonEnable(true);
        permissions.setSkippable(false);
        permissions.setOnClickContinue(() -> {
            clickStatus = true;
            init();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getPackageName())));
            }
            permissionAdmin(StepperActivity.this, devicePolicyManager, compName);
            permissionModify(StepperActivity.this);
            requestOverlayPermission(StepperActivity.this);
            isPermissionGranted(StepperActivity.this);
            requestUsageStatePermission(StepperActivity.this);

        });

        SteppersItem defaultLauncher = new SteppersItem();
        defaultLauncher.setLabel("Set Default Launcher");
        defaultLauncher.setPositiveButtonEnable(true);

//        defaultLauncher.setSkippable(true);
        defaultLauncher.setOnClickContinue(() -> {
            launcher = true;
            if (settingsPresenter.isMyLauncherDefault()) {
                Toast.makeText(StepperActivity.this, "Already set as default.", Toast.LENGTH_SHORT).show();
                PrefUtils.saveBooleanPref(StepperActivity.this, TOUR_STATUS, true);
                Intent intent = new Intent(StepperActivity.this, SettingsActivity.class);
                startActivity(intent);
                finish();
            } else {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        final Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
                        startActivity(intent);
                    } else {
                        final Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        startActivity(intent);
                    }
                } catch (Exception ignored) {
                }
            }

        });

        SteppersItem linking = new SteppersItem();
        linking.setLabel("Link Device");
        linking.setPositiveButtonEnable(true);
        linking.setSkippable(true, new OnSkipStepAction() {
            @Override
            public void onSkipStep() {
                int current_step = PrefUtils.getIntegerPref(StepperActivity.this, CURRENT_STEP);
                PrefUtils.saveIntegerPref(StepperActivity.this, CURRENT_STEP, current_step + 1);
            }
        });
        linking.setOnClickContinue(() -> {
            Intent intent = new Intent(StepperActivity.this, MainActivity.class);
            startActivity(intent);
        });


        SteppersItem guestPassword = new SteppersItem();
        guestPassword.setLabel("Set Guest Password");
        guestPassword.setSkippable(false);
        guestPassword.setPositiveButtonEnable(true);
        guestPassword.setOnClickContinue(() -> {
            settingsActivity.handleSetGuestPassword(StepperActivity.this, steppersView);
        });

        SteppersItem encryptedPassword = new SteppersItem();
        encryptedPassword.setLabel("Set Encrypted Password");
        encryptedPassword.setSkippable(false);
        encryptedPassword.setPositiveButtonEnable(true);
        encryptedPassword.setOnClickContinue(() -> {
            settingsActivity.handleSetMainPassword(StepperActivity.this, steppersView);
        });

        SteppersItem duressPassword = new SteppersItem();
        duressPassword.setLabel("Set Duress Password");
        duressPassword.setPositiveButtonEnable(true);
        duressPassword.setSkippable(true, new OnSkipStepAction() {
            @Override
            public void onSkipStep() {
                int current_step = PrefUtils.getIntegerPref(StepperActivity.this, CURRENT_STEP);
                PrefUtils.saveIntegerPref(StepperActivity.this, CURRENT_STEP, current_step + 1);
            }
        });
        duressPassword.setOnClickContinue(() -> {
            settingsActivity.handleSetDuressPassword(StepperActivity.this, steppersView);
        });


//        SteppersItem launchApplication = new SteppersItem();
//        launchApplication.setLabel("Launch App");
//        launchApplication.setSkippable(false);
//        launchApplication.setPositiveButtonEnable(true);
//        launchApplication.setOnClickContinue(() -> {
//
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//
//
//
//                }
//            }, 5000);
//
//        });


        //Add Steps
        steps.add(permissions);
        steps.add(guestPassword);
        steps.add(encryptedPassword);
        steps.add(duressPassword);
        steps.add(linking);
        steps.add(defaultLauncher);
//        steps.add(launchApplication);

        int current_step = PrefUtils.getIntegerPref(StepperActivity.this, CURRENT_STEP);

        steppersView.setConfig(steppersViewConfig);
        steppersView.setItems(steps);
        steppersView.build();
        steppersView.setActiveItem(current_step);
    }


    private boolean checkPermssions() {
        init();
        if (!PermissionUtils.canControlNotification(StepperActivity.this)) {
            PermissionUtils.requestNotificationAccessibilityPermission(StepperActivity.this);
        }
        boolean permission;

        boolean adminActive = devicePolicyManager.isAdminActive(compName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(StepperActivity.this);
        } else {
            permission = ContextCompat.checkSelfPermission(StepperActivity.this, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        }
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);

        boolean isIgnoringBatteryOptimizations = false;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (pm != null) {
                isIgnoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(getPackageName());
            }

        } else {
            isIgnoringBatteryOptimizations = true;
        }

        if (isIgnoringBatteryOptimizations && permission && adminActive && PermissionUtils.canControlNotification(StepperActivity.this)) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                if (isAccessGranted(StepperActivity.this)) {
                    return Settings.canDrawOverlays(StepperActivity.this);
                }
            }

        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_PASSWORD:
                if (resultCode == RESULT_OK) {
                    steppersView.nextStep();
                    int current_step = PrefUtils.getIntegerPref(StepperActivity.this, CURRENT_STEP);
                    PrefUtils.saveIntegerPref(StepperActivity.this, CURRENT_STEP, current_step + 1);
                } else {
//                    Toast.makeText(this, "Password is not changed! Try again", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void init() {
        if (devicePolicyManager == null) {
            devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        }
        if (compName == null) {
            compName = new ComponentName(this, MyAdmin.class);
        }

    }

    @Override
    protected void onResume() {

        try {
            if (clickStatus) {
                int current_step = PrefUtils.getIntegerPref(StepperActivity.this, CURRENT_STEP);
                if (checkPermssions() && current_step == 0) {
                    PrefUtils.saveIntegerPref(StepperActivity.this, CURRENT_STEP, current_step + 1);
                }
            }
            if (launcher) {
                int current_step = PrefUtils.getIntegerPref(StepperActivity.this, CURRENT_STEP);
                if (settingsPresenter.isMyLauncherDefault()) {
                    PrefUtils.saveBooleanPref(StepperActivity.this, TOUR_STATUS, true);
                    Intent intent = new Intent(StepperActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
            if (steppersView != null) {
                int current_step = PrefUtils.getIntegerPref(StepperActivity.this, CURRENT_STEP);
                steppersView.setActiveItem(current_step);
            }
        } catch (Exception ignored) {

        }

        super.onResume();
    }


}