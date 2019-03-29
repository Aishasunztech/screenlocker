package com.vortexlocker.app.permissions;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.vortexlocker.app.MyAdmin;
import com.vortexlocker.app.R;
import com.vortexlocker.app.mdm.MainActivity;
import com.vortexlocker.app.settings.SettingContract;
import com.vortexlocker.app.settings.SettingsActivity;
import com.vortexlocker.app.settings.SettingsModel;
import com.vortexlocker.app.settings.SettingsPresenter;
import com.vortexlocker.app.utils.PermissionUtils;
import com.vortexlocker.app.utils.PrefUtils;

import java.util.ArrayList;

import me.drozdzynski.library.steppers.SteppersItem;
import me.drozdzynski.library.steppers.SteppersView;
import me.drozdzynski.library.steppers.interfaces.OnSkipStepAction;

import static com.vortexlocker.app.utils.AppConstants.CURRENT_STEP;
import static com.vortexlocker.app.utils.AppConstants.TOUR_STATUS;
import static com.vortexlocker.app.utils.PermissionUtils.isAccessGranted;
import static com.vortexlocker.app.utils.PermissionUtils.isPermissionGranted;
import static com.vortexlocker.app.utils.PermissionUtils.permissionAdmin;
import static com.vortexlocker.app.utils.PermissionUtils.permissionModify;
import static com.vortexlocker.app.utils.PermissionUtils.requestOverlayPermission;
import static com.vortexlocker.app.utils.PermissionUtils.requestUsageStatePermission;

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

        steppersViewConfig.setOnFinishAction(() -> {
            PrefUtils.saveBooleanPref(StepperActivity.this, TOUR_STATUS, true);
            Intent intent = new Intent(StepperActivity.this, SettingsActivity.class);
            startActivity(intent);
            finish();
        });


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
                steppersView.nextStep();
            } else {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
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
        linking.setSkippable(true);
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


        SteppersItem finish = new SteppersItem();
        finish.setLabel("Activate");

        finish.setSubLabel("By pressing finish button you can start Screen Locker.");
        //Add Steps
        steps.add(permissions);
        steps.add(guestPassword);
        steps.add(encryptedPassword);
        steps.add(duressPassword);
        steps.add(linking);
        steps.add(defaultLauncher);
        steps.add(finish);
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
        if (permission && adminActive && PermissionUtils.canControlNotification(StepperActivity.this)) {
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
                    Toast.makeText(this, "Password is not changed! Try again", Toast.LENGTH_SHORT).show();
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
            int current_step = PrefUtils.getIntegerPref(StepperActivity.this, CURRENT_STEP);
            if (clickStatus) {
                if (checkPermssions() && current_step == 0) {
                    PrefUtils.saveIntegerPref(StepperActivity.this, CURRENT_STEP, current_step + 1);
                    if (steppersView != null) {
                        steppersView.nextStep();
                    }
                }
            } else if (launcher) {
                if (settingsPresenter.isMyLauncherDefault()) {
                    steppersView.nextStep();
                }

            } else if (steppersView != null) {
                steppersView.setActiveItem(current_step);
            }
        } catch (Exception ignored) {

        }


        super.onResume();
    }


}