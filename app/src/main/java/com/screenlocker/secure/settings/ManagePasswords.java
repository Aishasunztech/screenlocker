package com.screenlocker.secure.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.screenlocker.secure.R;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import static com.screenlocker.secure.settings.SettingsActivity.REQUEST_CODE_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;

public class ManagePasswords extends BaseActivity implements View.OnClickListener {
    private ConstraintLayout rootLayout;


    private SettingsActivity settingsActivity;
    private Toolbar mToolbar;
    private Chip duressStatus;
    private boolean isBackPressed = false,goToGuest,goToEncrypt,goToDuress;
    /**
     * request code for the set password activity
     */
    public static final int REQUEST_CODE_PASSWORD = 883;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_passwords);
        setIds();
        if (PrefUtils.getStringPref(this, AppConstants.KEY_DURESS_PASSWORD) != null) {
            duressStatus.setText("Activated");
        } else duressStatus.setText("Not Set");
        setListeners();
        settingsActivity = new SettingsActivity();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PASSWORD) {
            if (resultCode == RESULT_OK) {
                showAlertDialog(ManagePasswords.this, "Password Changed!", "Password Successfully Changed.", R.drawable.ic_checked);
            }
        }

    }

    /**
     * set listeners
     */
    private void setListeners() {
        findViewById(R.id.tvSetGuestPassword).setOnClickListener(this);
        findViewById(R.id.tvSetMainPassword).setOnClickListener(this);
        findViewById(R.id.tvSetDuressPassword).setOnClickListener(this);
        setToolbar(mToolbar);
    }


    private void showAlertDialog(AppCompatActivity activity, String title, String msg, int icon) {

        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        alertDialog.setIcon(icon);
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();

    }






    private void setIds() {
        rootLayout = findViewById(R.id.rootLayout);
        mToolbar = findViewById(R.id.toolbar);
        duressStatus = findViewById(R.id.chip_status);
    }

    private void setToolbar(Toolbar mToolbar) {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Passwords");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvSetGuestPassword:   // handle the set guest password click event
                /**
                 * start the {@link SetUpLockActivity} to get the password
                 */
//                settingsActivity.handleSetGuestPassword(ManagePasswords.this, rootLayout);
                handleSetGuestPassword(ManagePasswords.this,null,rootLayout);
                break;

            case R.id.tvSetMainPassword:    // handle the set main password click event
                /**
                 * start the {@link SetUpLockActivity} to get the password
                 */
//                settingsActivity.handleSetMainPassword(ManagePasswords.this, rootLayout);
                handleSetMainPassword(ManagePasswords.this,null,rootLayout);
                break;

            case R.id.tvSetDuressPassword:    // handle the set duress password click event
                /**
                 * start the {@link SetUpLockActivity} to get the password
                 */

//                settingsActivity.handleSetDuressPassword(ManagePasswords.this, rootLayout);
                handleSetDuressPassword(ManagePasswords.this,null,rootLayout);
                break;

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isBackPressed = false;
        if (PrefUtils.getStringPref(this, AppConstants.KEY_DURESS_PASSWORD) != null) {
            duressStatus.setText("Activated");
        } else duressStatus.setText("Not Set");
        setListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!isBackPressed )
        {
            if(!goToGuest && !goToEncrypt && !goToDuress)
            {
                this.finish();
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
    }

    public void handleSetGuestPassword(AppCompatActivity activity,SettingsPresenter settingsPresenter, View rootLayout) {

        if (PrefUtils.getStringPref(activity, KEY_GUEST_PASSWORD) == null) {
            goToGuest = true;
            Intent intent = new Intent(activity, SetUpLockActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_GUEST);
            activity.startActivityForResult(intent, REQUEST_CODE_PASSWORD);
        } else {
            final EditText input = new EditText(activity);

            if (settingsPresenter == null) {
                settingsPresenter = new SettingsPresenter(new SettingContract.SettingsMvpView() {
                    @Override
                    public int hashCode() {
                        return super.hashCode();
                    }
                }, new SettingsModel(activity));

            }

            settingsPresenter.showAlertDialog(input, (dialogInterface, i) -> {

                if (TextUtils.isEmpty(input.getText().toString().trim())) {
                    showAlertDialog(activity, "Invalid Password", "The password you entered is incorrect.", android.R.drawable.stat_sys_warning);
                    return;
                }

                if (input.getText().toString().
                        equalsIgnoreCase(PrefUtils.getStringPref(activity,
                                KEY_GUEST_PASSWORD))) {
                    // if password is right then allow user to change it

                    goToGuest = true;
                    Intent intent = new Intent(activity, SetUpLockActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_GUEST);
                    activity.startActivityForResult(intent, REQUEST_CODE_PASSWORD);

                } else {
                    showAlertDialog(activity, "Invalid password", "The password you entered is incorrect.", android.R.drawable.ic_dialog_alert);
                }
            }, null, activity.getResources().getString(R.string.please_enter_current_guest_password));
        }


    }

    public void handleSetMainPassword(AppCompatActivity activity,SettingsPresenter settingsPresenter, View rootLayout) {

        if (PrefUtils.getStringPref(activity, KEY_MAIN_PASSWORD) == null) {
            goToEncrypt = true;
            Intent i = new Intent(activity, SetUpLockActivity.class);
            i.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_MAIN);
            activity.startActivityForResult(i, REQUEST_CODE_PASSWORD);
        } else {
            final EditText input = new EditText(activity);
            if (settingsPresenter == null) {
                settingsPresenter = new SettingsPresenter(new SettingContract.SettingsMvpView() {
                    @Override
                    public int hashCode() {
                        return super.hashCode();
                    }
                }, new SettingsModel(activity));

            }
            settingsPresenter.showAlertDialog(input, (dialogInterface, i) -> {

                if (TextUtils.isEmpty(input.getText().toString().trim())) {
                    showAlertDialog(activity, "Invalid Password", "The password you entered is incorrect.", android.R.drawable.stat_sys_warning);
                    return;
                }

                if (input.getText().toString().
                        equalsIgnoreCase(PrefUtils.getStringPref(activity,
                                KEY_MAIN_PASSWORD))) {
                    // if password is right then allow user to change it
                    goToEncrypt = true;
                    Intent setUpLockActivityIntent = new Intent(activity, SetUpLockActivity.class);
                    setUpLockActivityIntent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_MAIN);
                    activity.startActivityForResult(setUpLockActivityIntent, REQUEST_CODE_PASSWORD);

                } else {
                    showAlertDialog(activity, "Invalid password", "The password you entered is incorrect.", android.R.drawable.ic_dialog_alert);
//                        Toast.makeText(SettingsActivity.this, R.string.wrong_password_entered, Toast.LENGTH_SHORT).show();
                }
            }, null, activity.getString(R.string.please_enter_current_encrypted_password));
        }

    }

    public void handleSetDuressPassword(AppCompatActivity activity,SettingsPresenter settingsPresenter, View rootLayout) {
        if (PrefUtils.getStringPref(activity, AppConstants.KEY_DURESS_PASSWORD) == null) {
            new AlertDialog.Builder(activity).
                    setTitle("Warning!")
                    .setMessage("Entering Duress Pin when device is locked will wipe your phone data. You cannot undo this action. All data will be deleted from target device without any confirmation. There is no way to reverse this action.").setPositiveButton("Ok", (dialogInterface, i) -> {
                        goToDuress = true;
                        Intent intent = new Intent(activity, SetUpLockActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_DURESS);
                activity.startActivityForResult(intent, REQUEST_CODE_PASSWORD);
            })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel())
                    .show();

        } else {
            final EditText input = new EditText(activity);
            if (settingsPresenter == null) {
                settingsPresenter = new SettingsPresenter(new SettingContract.SettingsMvpView() {
                    @Override
                    public int hashCode() {
                        return super.hashCode();
                    }
                }, new SettingsModel(activity));

            }
            settingsPresenter.showAlertDialog(input, (dialogInterface, i) -> {

                if (TextUtils.isEmpty(input.getText().toString().trim())) {
                    showAlertDialog(activity, "Invalid Password", "The password you entered is incorrect.", android.R.drawable.stat_sys_warning);
                    return;
                }

                if (input.getText().toString().
                        equalsIgnoreCase(PrefUtils.getStringPref(activity,
                                AppConstants.KEY_DURESS_PASSWORD))) {
                    // if password is right then allow user to change it
                    goToDuress = true;
                    Intent setUpLockActivityIntent = new Intent(activity, SetUpLockActivity.class);
                    setUpLockActivityIntent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_DURESS);
                    activity.startActivityForResult(setUpLockActivityIntent, REQUEST_CODE_PASSWORD);

                } else {
                    showAlertDialog(activity, "Invalid password!", "The password you entered is incorrect.", android.R.drawable.ic_dialog_alert);
//                        Toast.makeText(SettingsActivity.this, R.string.wrong_password_entered, Toast.LENGTH_SHORT).show();
                }
            }, null, activity.getString(R.string.please_enter_current_duress_password));
        }
    }





}
