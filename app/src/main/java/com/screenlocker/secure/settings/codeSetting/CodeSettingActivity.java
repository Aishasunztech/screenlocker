package com.screenlocker.secure.settings.codeSetting;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.appSelection.AppSelectionActivity;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.settings.codeSetting.Sim.SimActivity;
import com.screenlocker.secure.settings.codeSetting.installApps.InstallAppsActivity;
import com.screenlocker.secure.settings.codeSetting.policy.PolicyActivity;
import com.screenlocker.secure.settings.codeSetting.secureSettings.SecureSettingsActivity;
import com.screenlocker.secure.settings.codeSetting.systemControls.SystemPermissionActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.LifecycleReceiver;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import timber.log.Timber;

import static com.screenlocker.secure.socket.utils.utils.passwordsOk;
import static com.screenlocker.secure.utils.CommonUtils.hideKeyboard;
import static com.screenlocker.secure.utils.LifecycleReceiver.BACKGROUND;
import static com.screenlocker.secure.utils.LifecycleReceiver.FOREGROUND;
import static com.screenlocker.secure.utils.LifecycleReceiver.LIFECYCLE_ACTION;
import static com.screenlocker.secure.utils.LifecycleReceiver.STATE;


public class CodeSettingActivity extends BaseActivity implements View.OnClickListener
        , CodeSettingContract.CodeSettingMvpView {
    private ConstraintLayout rootLayout;
    private InputMethodManager imm;
    private Toolbar mToolbar;
    private CodeSettingPresenter mPresenter;
    AlertDialog adminPasswordDialog;
    public static AppCompatActivity codeSettingsInstance;

    private boolean goToAppSelection;
    private boolean gotoSystemControl;
    private boolean goToInstallApps;
    private boolean goToPolicyMenu;
    private boolean goToIMEIMenu;
    private boolean goToSettingsAppPermission, goToSimActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_setting);
        codeSettingsInstance = this;
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mPresenter = new CodeSettingPresenter(new CodeSettingModel(this), this);
        setIds();

        setToolbar();

//to add the setting in db with default values
        new Thread() {
            @Override
            public void run() {
                Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                String settingPackageName = null;
                if (resolveInfos != null || resolveInfos.size() != 0) {
                    settingPackageName = resolveInfos.get(0).activityInfo.packageName;
                }
                if (settingPackageName != null) {
                    AppInfo particularApp = MyApplication.getAppDatabase(CodeSettingActivity.this).getDao().getParticularApp(settingPackageName);
                    if (particularApp == null) {
                        AppInfo appInfo = new AppInfo();
                        appInfo.setEncrypted(false);
                        appInfo.setEnable(false);
                        appInfo.setGuest(false);
                        appInfo.setPackageName(resolveInfos.get(0).activityInfo.packageName);
                        appInfo.setLabel(String.valueOf(resolveInfos.get(0).loadLabel(getPackageManager())));
                        appInfo.setUniqueName(settingPackageName);
                        MyApplication.getAppDatabase(CodeSettingActivity.this).getDao().insertApps(appInfo);
                    }

                }

            }
        }.start();

        createAdminPasswordDialog();
    }

    private void createAdminPasswordDialog() {
        final EditText input = new EditText(this);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.please_enter_code_admin_password));
        alertDialog.setCancelable(false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(10, 10, 10, 10);

        input.setGravity(Gravity.CENTER);


        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        //input.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.mipmap.ic_launcher);
        input.setFocusable(true);
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        alertDialog.setPositiveButton(R.string.ok,
                (dialog, which) -> {
                    try {
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (TextUtils.isEmpty(input.getText().toString().trim())) {
                        Snackbar.make(rootLayout, R.string.please_enter_your_current_password, Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    if (input.getText().toString().equalsIgnoreCase(PrefUtils.getStringPref(CodeSettingActivity.this, AppConstants.KEY_CODE_PASSWORD))) {
                        dialog.cancel();
                    } else {
                        Snackbar.make(rootLayout, R.string.wrong_password_entered, Snackbar.LENGTH_SHORT).show();
                    }

                });

        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        dialog.cancel();
                    }
                });

        adminPasswordDialog = alertDialog.create();
    }


    private void setToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.admin_panel_title));
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setIds() {
        findViewById(R.id.tvSetAppsPermission).setOnClickListener(this);
        findViewById(R.id.tvResetPassword).setOnClickListener(this);
        findViewById(R.id.tvSettingsMenu).setOnClickListener(this);
        findViewById(R.id.tvSettingsControl).setOnClickListener(this);
        findViewById(R.id.tvChangeAdminPassword).setOnClickListener(this);
        findViewById(R.id.tvInstallApps).setOnClickListener(this);
        findViewById(R.id.tvSim).setOnClickListener(this);
        findViewById(R.id.tvPolicyMenu).setOnClickListener(this);
        findViewById(R.id.tvIMEIMenu).setOnClickListener(this);
        rootLayout = findViewById(R.id.rootLayout);
        mToolbar = findViewById(R.id.toolbar);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvSetAppsPermission:
                mPresenter.handleSetAppsPermission();
                break;
            case R.id.tvResetPassword:
                mPresenter.handleResetPassword();
                break;
            case R.id.tvSettingsMenu:
                handleSettingsMenu();
                break;
            case R.id.tvChangeAdminPassword:
                handleChangeAdminPassword();
                break;
            case R.id.tvInstallApps:
                handleInstallApps();
                break;
            case R.id.tvSim:
                handleSim();
                break;
            case R.id.tvPolicyMenu:
                handlePolicyMenu();
                break;
            case R.id.tvSettingsControl:
                handleSettingsApp();
                break;
            case R.id.tvIMEIMenu:
                goToIMEIMenu = true;
                startActivity(new Intent(CodeSettingActivity.this, IMEIActivity.class));

        }
    }

    private void handleSim() {
        goToSimActivity = true;
        startActivity(new Intent(this, SimActivity.class));
    }

    private void handleInstallApps() {
        goToInstallApps = true;
        startActivity(new Intent(CodeSettingActivity.this, InstallAppsActivity.class));
    }

    // method to handle policy menu
    private void handlePolicyMenu() {
        goToPolicyMenu = true;
        startActivity(new Intent(CodeSettingActivity.this, PolicyActivity.class));
    }

    @Override
    public void onStateChange(int state) {
        switch (state) {

            case LifecycleReceiver.FOREGROUND:
                Timber.e("onStateChange: FOREGROUND");
                break;

            case LifecycleReceiver.BACKGROUND:
                Timber.tag("TAGLLLL").e("onStateChange: BACKGROUND");
                this.finish();
                break;

            default:
                Timber.e("onStateChange: SOMETHING");
                break;
        }
    }


    private void handleChangeAdminPassword() {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_change_admin_password);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(params);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        final EditText etOldText;
        final EditText etNewPassword;
        final EditText confirmPassword;
        Button btOk;
        etOldText = dialog.findViewById(R.id.etOldPassword);
        etOldText.setFocusable(true);
        etNewPassword = dialog.findViewById(R.id.etNewPassword);
        confirmPassword = dialog.findViewById(R.id.etNewConfirmPassword);
        btOk = dialog.findViewById(R.id.btOk);
        dialog.findViewById(R.id.btCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                if (dialog.isShowing())
                    dialog.cancel();

            }
        });

        //validate password
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validatePassword(etOldText, etNewPassword, confirmPassword)) {
//                    boolean keyOk = passwordsOk(this, reEnteredPassword);
                    PrefUtils.saveStringPref(CodeSettingActivity.this,
                            AppConstants.KEY_CODE_PASSWORD, etNewPassword.getText().toString().trim());
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                    if (dialog != null && dialog.isShowing()) {
                        dialog.cancel();
                    }
                    Snackbar.make(rootLayout, getResources().getString(R.string.admin_password_changed), Snackbar.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CodeSettingActivity.this, getResources().getString(R.string.password_taken), Toast.LENGTH_SHORT).show();

                }
            }
        });
        dialog.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        goToAppSelection = false;
        gotoSystemControl = false;
        goToInstallApps = false;
        goToPolicyMenu = false;
        goToSettingsAppPermission = false;
        goToIMEIMenu = false;
        goToSimActivity = false;
        Intent intent = new Intent(LIFECYCLE_ACTION);
        intent.putExtra(STATE, FOREGROUND);
        sendBroadcast(intent);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (!goToAppSelection && !gotoSystemControl && !goToInstallApps && !goToPolicyMenu && !goToSettingsAppPermission && !goToIMEIMenu && !goToSimActivity) {
            Intent intent = new Intent(LIFECYCLE_ACTION);
            intent.putExtra(STATE, BACKGROUND);
            sendBroadcast(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!goToAppSelection && !gotoSystemControl && !goToInstallApps && !goToPolicyMenu && !goToSettingsAppPermission && !goToIMEIMenu && !goToSimActivity) {
            hideKeyboard(CodeSettingActivity.this);
            finish();
        }

    }

    private boolean validatePassword(EditText etOldText, EditText etNewPassword, EditText etConfirmPassword) {
        if (TextUtils.isEmpty(etOldText.getText().toString())) {
            etOldText.requestFocus();
            Toast.makeText(this, getResources().getString(R.string.enter_old_password), Toast.LENGTH_SHORT).show();
            return false;
        } else if (!etOldText.getText().toString().equals(PrefUtils.getStringPref(CodeSettingActivity.this, AppConstants.KEY_CODE_PASSWORD))) {

            etOldText.requestFocus();
            Toast.makeText(this, getResources().getString(R.string.enter_correct_password), Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(etNewPassword.getText().toString())) {
            etNewPassword.requestFocus();
            Toast.makeText(this, getResources().getString(R.string.enter_new_password), Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(etConfirmPassword.getText().toString())) {
            etConfirmPassword.requestFocus();
            Toast.makeText(this, getResources().getString(R.string.re_enter_password), Toast.LENGTH_SHORT).show();
            return false;
        } else if (!etNewPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
            etConfirmPassword.requestFocus();
            Toast.makeText(this, getResources().getString(R.string.password_not_matched), Toast.LENGTH_SHORT).show();
            return false;
        } else if (!passwordsOk(this, etConfirmPassword.getText().toString())) {
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void handleSetAppsPermission() {
        goToAppSelection = true;


        startActivity(new Intent(this, AppSelectionActivity.class));
//
//        Intent i = new Intent(Intent.ACTION_MAIN, null);
//        i.addCategory(Intent.CATEGORY_LAUNCHER);
//
//        List<ResolveInfo> appsFromSystem = getPackageManager().queryIntentActivities(i, 0);
//        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
//        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//        String settingPackageName = null;
//        if (resolveInfos != null || resolveInfos.size() != 0) {
//            settingPackageName = resolveInfos.get(0).activityInfo.packageName;
//        }
//
//
//        for (ResolveInfo ri : appsFromSystem) {
//
//            AppInfo appInfo = new AppInfo();
//            appInfo.setLabel(String.valueOf(ri.loadLabel(packageManager)));
//            appInfo.setPackageName(ri.activityInfo.packageName);
//            appInfo.setIcon(ri.activityInfo.loadIcon(packageManager));
//
//            for (int i = 0; i < apps.size(); i++) {
//                if ((appInfo.getPackageName() + appInfo.getLabel()).equals(apps.get(i).getPackageName())) {
//                    appInfo.setHide(true);
//                    break;
//                }
//
//            }   for (int i = 0; i < disabledApps.size(); i++) {
//                if ((appInfo.getPackageName() + appInfo.getLabel()).equals(disabledApps.get(i).getPackageName())) {
//                    appInfo.setEnable(true);
//                    break;
//                }
//
//            }
//
//            if (settingPackageName == null || !appInfo.getPackageName().equals(settingPackageName)) {
//                appsList.add(appInfo);
//            }
//
//        }
    }


    @Override
    public void resetPassword() {
        PrefUtils.saveStringPref(CodeSettingActivity.this, AppConstants.KEY_GUEST_PASSWORD, AppConstants.DEFAULT_GUEST_PASS);
        PrefUtils.saveStringPref(CodeSettingActivity.this, AppConstants.KEY_MAIN_PASSWORD, AppConstants.DEFAULT_MAIN_PASS);

//        if (isServiceRunning()) {
//            final Intent lockScreenIntent = new Intent(CodeSettingActivity.this, LockScreenService.class);
//            stopService(lockScreenIntent);
//        }
        Snackbar.make(rootLayout, R.string.password_is_changed_to_default, Snackbar.LENGTH_SHORT);

    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LockScreenService.class.getName().equals(service.service.getClassName())) {

                return true;
            }
        }
        return false;
    }


    private void handleSettingsApp() {
        goToSettingsAppPermission = true;
        startActivity(new Intent(CodeSettingActivity.this, SecureSettingsActivity.class));
    }

    private void handleSettingsMenu() {
        gotoSystemControl = true;
        startActivity(new Intent(CodeSettingActivity.this, SystemPermissionActivity.class));

    /*    if (PrefUtils.getStringPref(this, AppConstants.KEY_MAIN_PASSWORD) == null) {
            Snackbar.make(rootLayout, R.string.please_add_encrypted_password, Snackbar.LENGTH_SHORT).show();
//            Toast.makeText(this, R.string.please_add_encrypted_password, Toast.LENGTH_LONG).show();
        } else {
            final EditText input = new EditText(CodeSettingActivity.this);
            showAlertDialog(input, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (TextUtils.isEmpty(input.getText().toString().trim())) {
                        Snackbar.make(rootLayout, R.string.please_enter_your_current_password, Snackbar.LENGTH_SHORT).show();
//                        Toast.makeText(CodeSettingActivity.this, R.string.please_enter_your_current_password, Toast.LENGTH_SHORT).show();
                        //   imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        return;
                    }
                    if (input.getText().toString().equalsIgnoreCase(PrefUtils.getStringPref(CodeSettingActivity.this, AppConstants.KEY_MAIN_PASSWORD))) {
                        startActivity(new Intent(CodeSettingActivity.this, SystemPermissionActivity.class));
                    } else {
                        Snackbar.make(rootLayout, R.string.wrong_password_entered, Snackbar.LENGTH_SHORT).show();
//                        Toast.makeText(CodeSettingActivity.this, R.string.wrong_password_entered, Toast.LENGTH_SHORT).show();
                    }
                }
            }, null, getString(R.string.please_enter_encrypted_password));

        }*/


    }

    public void showAlertDialog(final EditText input, final DialogInterface.OnClickListener onClickListener, final DialogInterface.OnClickListener onNegativeClick, String title) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(10, 10, 10, 10);

        input.setGravity(Gravity.CENTER);

        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        //input.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.mipmap.ic_launcher);
        input.setFocusable(true);
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }

        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onClickListener.onClick(dialog, which);
                        try {
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if (onNegativeClick != null)
                                onNegativeClick.onClick(dialog, which);
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dialog.cancel();
                    }
                });

        alertDialog.show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
