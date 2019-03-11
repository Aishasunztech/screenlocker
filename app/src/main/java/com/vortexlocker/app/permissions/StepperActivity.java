package com.vortexlocker.app.permissions;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.vortexlocker.app.MyAdmin;
import com.vortexlocker.app.R;
import com.vortexlocker.app.mdm.MainActivity;
import com.vortexlocker.app.mdm.ui.LinkDeviceActivity;
import com.vortexlocker.app.settings.SettingsActivity;
import com.vortexlocker.app.utils.AppConstants;
import com.vortexlocker.app.utils.PermissionUtils;
import com.vortexlocker.app.utils.PrefUtils;

import java.util.ArrayList;

import me.drozdzynski.library.steppers.SteppersItem;
import me.drozdzynski.library.steppers.SteppersView;
import timber.log.Timber;

import static com.vortexlocker.app.utils.AppConstants.CODE_WRITE_SETTINGS_PERMISSION;
import static com.vortexlocker.app.utils.AppConstants.CURRENT_STEP;
import static com.vortexlocker.app.utils.AppConstants.RESULT_ENABLE;
import static com.vortexlocker.app.utils.AppConstants.TOUR_STATUS;
import static com.vortexlocker.app.utils.PermissionUtils.isPermissionGranted;
import static com.vortexlocker.app.utils.PermissionUtils.permissionAdmin;
import static com.vortexlocker.app.utils.PermissionUtils.permissionModify;
import static com.vortexlocker.app.utils.PermissionUtils.requestOverlayPermission;

public class StepperActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PASSWORD = 883;
    private SteppersView steppersView;
    boolean permission;
    boolean clickStatus = false;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stepper_layout);
        steppersView = findViewById(R.id.steppersView);
        SteppersView.Config steppersViewConfig = new SteppersView.Config();

        SettingsActivity settingsActivity = new SettingsActivity();

        steppersViewConfig.setOnFinishAction(() -> {
            PrefUtils.saveBooleanPref(StepperActivity.this, TOUR_STATUS, true);
            Intent intent = new Intent(StepperActivity.this, MainActivity.class);
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

            if (checkPermssions()) {
                int current_step = PrefUtils.getIntegerPref(StepperActivity.this, CURRENT_STEP);
                PrefUtils.saveIntegerPref(StepperActivity.this, CURRENT_STEP, current_step + 1);
                steppersView.nextStep();
            }

        });

        SteppersItem linking = new SteppersItem();
        linking.setLabel("Link Device");
        linking.setPositiveButtonEnable(true);
        linking.setSkippable(false);
        linking.setOnClickContinue(() -> {
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
        duressPassword.setSkippable(true);
        duressPassword.setPositiveButtonEnable(true);
        duressPassword.setOnClickContinue(() -> {
            settingsActivity.handleSetDuressPassword(StepperActivity.this, steppersView);
        });


        //Add Steps
        steps.add(permissions);
        steps.add(guestPassword);
        steps.add(encryptedPassword);
        steps.add(duressPassword);
        steps.add(linking);


        int current_step = PrefUtils.getIntegerPref(StepperActivity.this, CURRENT_STEP);
        steppersView.setConfig(steppersViewConfig);
        steppersView.setItems(steps);
        steppersView.build();
        steppersView.setActiveItem(current_step);
    }

    private boolean checkPermssions() {
        init();
        boolean permission;
        boolean adminActive = devicePolicyManager.isAdminActive(compName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(StepperActivity.this);
        } else {
            permission = ContextCompat.checkSelfPermission(StepperActivity.this, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        }
        if (permission && adminActive) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                return Settings.canDrawOverlays(StepperActivity.this);
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
        super.onResume();
        if (clickStatus) {
            init();
            permissionModify(StepperActivity.this);
            permissionAdmin(StepperActivity.this, devicePolicyManager, compName);
            isPermissionGranted(StepperActivity.this);
            if (!PermissionUtils.canControlNotification(StepperActivity.this)) {
                PermissionUtils.requestNotificationAccessibilityPermission(StepperActivity.this);
            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        switch (requestCode) {
            case CODE_WRITE_SETTINGS_PERMISSION:
                Log.d("permissiond", "CODE_WRITE_SETTINGS_PERMISSION");
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("permissionAllowed", "ok");

                } else {
                    Log.d("permissionAllowed", "k");

                }
                break;
        }
    }

}