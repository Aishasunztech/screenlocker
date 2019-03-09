package com.vortexlocker.app.settings.codeSetting;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
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

import com.vortexlocker.app.R;
import com.vortexlocker.app.app.MyApplication;
import com.vortexlocker.app.appSelection.AppSelectionActivity;
import com.vortexlocker.app.base.BaseActivity;
import com.vortexlocker.app.launcher.AppInfo;
import com.vortexlocker.app.service.LockScreenService;
import com.vortexlocker.app.settings.SettingsActivity;
import com.vortexlocker.app.settings.codeSetting.Sim.SimActivity;
import com.vortexlocker.app.settings.codeSetting.installApps.InstallAppsActivity;
import com.vortexlocker.app.settingsMenu.SettingsMenuActivity;
import com.vortexlocker.app.utils.AppConstants;
import com.vortexlocker.app.utils.LifecycleReceiver;
import com.vortexlocker.app.utils.PrefUtils;

import java.util.List;

import timber.log.Timber;

import static com.vortexlocker.app.utils.LifecycleReceiver.BACKGROUND;
import static com.vortexlocker.app.utils.LifecycleReceiver.FOREGROUND;
import static com.vortexlocker.app.utils.LifecycleReceiver.LIFECYCLE_ACTION;
import static com.vortexlocker.app.utils.LifecycleReceiver.STATE;
import static com.vortexlocker.app.utils.Utils.collapseNow;

public class CodeSettingActivity extends BaseActivity implements View.OnClickListener
        , CodeSettingContract.CodeSettingMvpView {
    private ConstraintLayout rootLayout;
    private InputMethodManager imm;
    private Toolbar mToolbar;
    private CodeSettingPresenter mPresenter;
    AlertDialog adminPasswordDialog;
    public static Activity codeSettingsInstance;
    private boolean goToAppSelection;
    private boolean goToAppSettingMenu;
    private boolean goToInstallApps;

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

                    settingPackageName = resolveInfos.get(0).activityInfo.packageName + String.valueOf(resolveInfos.get(0).loadLabel(getPackageManager()));
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
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
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
        getSupportActionBar().setTitle("Admin Panel");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setIds() {
        findViewById(R.id.tvSetAppsPermission).setOnClickListener(this);
        findViewById(R.id.tvResetPassword).setOnClickListener(this);
        findViewById(R.id.tvSettingsMenu).setOnClickListener(this);
        findViewById(R.id.tvChangeAdminPassword).setOnClickListener(this);
        findViewById(R.id.tvInstallApps).setOnClickListener(this);
        findViewById(R.id.tvSim).setOnClickListener(this);

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
        }
    }

    private void handleSim() {
        startActivity(new Intent(this, SimActivity.class));
    }

    private void handleInstallApps() {
        goToInstallApps = true;
        startActivity(new Intent(CodeSettingActivity.this, InstallAppsActivity.class));
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

    @Override
    protected void freezeStatusbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            collapseNow(this);
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
                    PrefUtils.saveStringPref(CodeSettingActivity.this,
                            AppConstants.KEY_CODE_PASSWORD, etNewPassword.getText().toString().trim());
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                    if (dialog != null && dialog.isShowing()) {
                        dialog.cancel();
                    }
                    Snackbar.make(rootLayout, "Admin Password Changed.", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        goToAppSelection = false;
        goToAppSettingMenu = false;
        goToInstallApps = false;
        Intent intent = new Intent(LIFECYCLE_ACTION);
        intent.putExtra(STATE, FOREGROUND);
        sendBroadcast(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!goToAppSelection && !goToAppSettingMenu && !goToInstallApps) {
            Intent intent = new Intent(LIFECYCLE_ACTION);
            intent.putExtra(STATE, BACKGROUND);
            sendBroadcast(intent);
        }
    }

    private boolean validatePassword(EditText etOldText, EditText etNewPassword, EditText etConfirmPassword) {
        if (TextUtils.isEmpty(etOldText.getText().toString())) {
            etOldText.requestFocus();
            Toast.makeText(this, "Please enter your old password.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!etOldText.getText().toString().equals(PrefUtils.getStringPref(CodeSettingActivity.this, AppConstants.KEY_CODE_PASSWORD))) {

            etOldText.requestFocus();
            Toast.makeText(this, "Please enter correct password.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(etNewPassword.getText().toString())) {
            etNewPassword.requestFocus();
            Toast.makeText(this, "Please enter new password.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(etConfirmPassword.getText().toString())) {
            etConfirmPassword.requestFocus();
            Toast.makeText(this, "Please renter your password.", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!etNewPassword.getText().toString().equals(etConfirmPassword.getText().toString())) {
            etConfirmPassword.requestFocus();
            Toast.makeText(this, "Password did not match.", Toast.LENGTH_SHORT).show();
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
        if (isServiceRunning()) {
            final Intent lockScreenIntent = new Intent(CodeSettingActivity.this, LockScreenService.class);
            stopService(lockScreenIntent);
        }
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

    private void handleSettingsMenu() {
        goToAppSettingMenu = true;
        startActivity(new Intent(CodeSettingActivity.this, SettingsMenuActivity.class));
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
                        startActivity(new Intent(CodeSettingActivity.this, SettingsMenuActivity.class));
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
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

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
