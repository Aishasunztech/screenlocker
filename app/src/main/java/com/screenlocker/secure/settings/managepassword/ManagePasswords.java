package com.screenlocker.secure.settings.managepassword;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
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
import com.screenlocker.secure.settings.SettingContract;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.settings.SettingsModel;
import com.screenlocker.secure.settings.SettingsPresenter;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import static com.screenlocker.secure.utils.AppConstants.KEY_DURESS_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;

public class ManagePasswords extends BaseActivity implements View.OnClickListener {

    private static final int RESULTGUEST = 100, RESULTENCRYPTED = 101, RESULTDURES = 102;

    private ConstraintLayout rootLayout;
    private SettingsActivity settingsActivity;
    private Toolbar mToolbar;
    private Chip duressStatus;
    private boolean isBackPressed = false, goToGuest, goToEncrypt, goToDuress;
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
            duressStatus.setText(getResources().getString(R.string.activated_code));
        } else {
            if (PrefUtils.getStringPref(this, AppConstants.DUERESS_DEFAULT_CONFIG) != null) {
                duressStatus.setText(getResources().getString(R.string.activated_code));
            }else {
                duressStatus.setText(getResources().getString(R.string.not_set));
            }
        }
        setListeners();
        settingsActivity = new SettingsActivity();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        switch (requestCode) {
            case REQUEST_CODE_PASSWORD:
                if (resultCode == RESULT_OK) {
                    showAlertDialog(ManagePasswords.this, getResources().getString(R.string.password_changed_title), getResources().getString(R.string.password_changed_message), R.drawable.ic_checked);
                }
                break;
            case RESULTENCRYPTED:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(this, PasswordOptionsAcitivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_MAIN);
                    startActivityForResult(intent, REQUEST_CODE_PASSWORD);
                }else if (resultCode == RESULT_CANCELED){
                    Snackbar.make(findViewById(R.id.rootLayout), "Incorrect Password", Snackbar.LENGTH_LONG).show();
                }
                break;
            case RESULTGUEST:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(this, PasswordOptionsAcitivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_GUEST);
                    startActivityForResult(intent, REQUEST_CODE_PASSWORD);
                }
                else if (resultCode == RESULT_CANCELED){
                    Snackbar.make(findViewById(R.id.rootLayout), "Incorrect Password", Snackbar.LENGTH_LONG).show();
                }
                break;
            case RESULTDURES:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(this, PasswordOptionsAcitivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_DURESS);
                    startActivityForResult(intent, REQUEST_CODE_PASSWORD);
                }
                else if (resultCode == RESULT_CANCELED){
                    Snackbar.make(findViewById(R.id.rootLayout), "Incorrect Password", Snackbar.LENGTH_LONG).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);

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
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.ok_text),
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
            getSupportActionBar().setTitle(getResources().getString(R.string.manage_password_title));
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
                handleSetGuestPassword(ManagePasswords.this, null, rootLayout);
                break;

            case R.id.tvSetMainPassword:    // handle the set main password click event
                /**
                 * start the {@link SetUpLockActivity} to get the password
                 */
//                settingsActivity.handleSetMainPassword(ManagePasswords.this, rootLayout);
                handleSetMainPassword(ManagePasswords.this, null, rootLayout);
                break;

            case R.id.tvSetDuressPassword:    // handle the set duress password click event
                /**
                 * start the {@link SetUpLockActivity} to get the password
                 */

//                settingsActivity.handleSetDuressPassword(ManagePasswords.this, rootLayout);
                handleSetDuressPassword(ManagePasswords.this, null, rootLayout);
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        isBackPressed = false;
        if (PrefUtils.getStringPref(this, AppConstants.KEY_DURESS_PASSWORD) != null) {
            duressStatus.setText(getResources().getString(R.string.activated_code));
        } else {
            if (PrefUtils.getStringPref(this, AppConstants.DUERESS_DEFAULT_CONFIG) != null) {
                duressStatus.setText(getResources().getString(R.string.activated_code));
            }else {
                duressStatus.setText(getResources().getString(R.string.not_set));
            }
        }
        setListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isBackPressed) {
            if (!goToGuest && !goToEncrypt && !goToDuress) {
                // this.finish();
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
    }

    public void handleSetGuestPassword(AppCompatActivity activity, SettingsPresenter settingsPresenter, View rootLayout) {
        String passConfig = PrefUtils.getStringPref(this, AppConstants.GUEST_DEFAULT_CONFIG);
        if (passConfig == null) {
            if (PrefUtils.getStringPref(this, KEY_GUEST_PASSWORD) != null)
                showGuestPin(activity, settingsPresenter);
            return;

        }
        switch (passConfig) {
            case AppConstants.PATTERN_PASSWORD:
                verifyCurrentPattern(AppConstants.KEY_GUEST);
                break;
            case AppConstants.PIN_PASSWORD:
                showGuestPin(activity, settingsPresenter);
                break;
            case AppConstants.COMBO_PASSWORD:
                verifyCurrentCombo(AppConstants.KEY_GUEST);
                break;
        }


    }

    private void showGuestPin(AppCompatActivity activity, SettingsPresenter settingsPresenter) {
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
                showAlertDialog(activity, getResources().getString(R.string.invalid_password_title), getResources().getString(R.string.invalid_password_message), android.R.drawable.stat_sys_warning);
                return;
            }

            if (input.getText().toString().
                    equalsIgnoreCase(PrefUtils.getStringPref(activity,
                            KEY_GUEST_PASSWORD))) {
                // if password is right then allow user to change it

                goToGuest = true;
                Intent intent = new Intent(activity, PasswordOptionsAcitivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_GUEST);
                activity.startActivityForResult(intent, REQUEST_CODE_PASSWORD);

            } else {
                showAlertDialog(activity, getResources().getString(R.string.invalid_password_title), getResources().getString(R.string.invalid_password_message), android.R.drawable.ic_dialog_alert);
            }
        }, null, activity.getResources().getString(R.string.please_enter_current_guest_password));
    }

    public void handleSetMainPassword(AppCompatActivity activity, SettingsPresenter settingsPresenter, View rootLayout) {
        String passConfig = PrefUtils.getStringPref(this, AppConstants.ENCRYPT_DEFAULT_CONFIG);
        if (passConfig == null) {
            if (PrefUtils.getStringPref(this, KEY_MAIN_PASSWORD) != null)
                showEncryptedPin(activity, settingsPresenter);
            return;

        }
        switch (passConfig) {
            case AppConstants.PATTERN_PASSWORD:
                verifyCurrentPattern(AppConstants.KEY_MAIN);
                break;
            case AppConstants.PIN_PASSWORD:
                showEncryptedPin(activity, settingsPresenter);
                break;
            case AppConstants.COMBO_PASSWORD:
                verifyCurrentCombo(AppConstants.KEY_MAIN);
                break;
        }

    }

    private void showEncryptedPin(AppCompatActivity activity, SettingsPresenter settingsPresenter) {
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
                showAlertDialog(activity, getResources().getString(R.string.invalid_password_title), getResources().getString(R.string.invalid_password_message), android.R.drawable.stat_sys_warning);
                return;
            }

            if (input.getText().toString().
                    equalsIgnoreCase(PrefUtils.getStringPref(activity,
                            KEY_MAIN_PASSWORD))) {
                // if password is right then allow user to change it
                goToEncrypt = true;
                Intent setUpLockActivityIntent = new Intent(activity, PasswordOptionsAcitivity.class);
                setUpLockActivityIntent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_MAIN);
                activity.startActivityForResult(setUpLockActivityIntent, REQUEST_CODE_PASSWORD);

            } else {
                showAlertDialog(activity, getResources().getString(R.string.invalid_password_title), getResources().getString(R.string.invalid_password_message), android.R.drawable.ic_dialog_alert);
//                        Toast.makeText(StateSettingsActivity.this, R.string.wrong_password_entered, Toast.LENGTH_SHORT).show();
            }
        }, null, activity.getString(R.string.please_enter_current_encrypted_password));
    }

    public void handleSetDuressPassword(AppCompatActivity activity, SettingsPresenter settingsPresenter, View rootLayout) {
        String passConfig = PrefUtils.getStringPref(this, AppConstants.DUERESS_DEFAULT_CONFIG);
        if (passConfig == null) {
            if (PrefUtils.getStringPref(this, KEY_DURESS_PASSWORD) != null)
                showDuressPin(activity, settingsPresenter);
            else {
                new AlertDialog.Builder(activity).
                        setTitle(getResources().getString(R.string.duress_password_warning))
                        .setMessage(getResources().getString(R.string.duress_password_message)).setPositiveButton(getResources().getString(R.string.ok_text), (dialogInterface, i) -> {
                    goToDuress = true;
                    Intent intent = new Intent(activity, PasswordOptionsAcitivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_DURESS);
                    activity.startActivityForResult(intent, REQUEST_CODE_PASSWORD);
                })
                        .setNegativeButton(getResources().getString(R.string.cancel_text), (dialogInterface, i) -> dialogInterface.cancel())
                        .show();
            }
            return;

        }
        switch (passConfig) {
            case AppConstants.PATTERN_PASSWORD:
                verifyCurrentPattern(AppConstants.KEY_DURESS);
                break;
            case AppConstants.PIN_PASSWORD:
                showDuressPin(activity, settingsPresenter);
                break;
            case AppConstants.COMBO_PASSWORD:
                verifyCurrentCombo(AppConstants.KEY_DURESS);
                break;


        }

    }

    private void showDuressPin(AppCompatActivity activity, SettingsPresenter settingsPresenter) {
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
                showAlertDialog(activity, getResources().getString(R.string.invalid_password_title), getResources().getString(R.string.invalid_password_message), android.R.drawable.stat_sys_warning);
                return;
            }

            if (input.getText().toString().
                    equalsIgnoreCase(PrefUtils.getStringPref(activity,
                            AppConstants.KEY_DURESS_PASSWORD))) {
                // if password is right then allow user to change it
                goToDuress = true;
                Intent setUpLockActivityIntent = new Intent(activity, PasswordOptionsAcitivity.class);
                setUpLockActivityIntent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_DURESS);
                activity.startActivityForResult(setUpLockActivityIntent, REQUEST_CODE_PASSWORD);

            } else {
                showAlertDialog(activity, getResources().getString(R.string.invalid_password_title), getResources().getString(R.string.invalid_password_message), android.R.drawable.ic_dialog_alert);
//                        Toast.makeText(StateSettingsActivity.this, R.string.wrong_password_entered, Toast.LENGTH_SHORT).show();
            }
        }, null, activity.getString(R.string.please_enter_current_duress_password));
    }

    private void verifyCurrentPattern(String userType) {
        switch (userType) {
            case AppConstants.KEY_MAIN:
                Intent intent = new Intent(this, VerifyPatternActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_MAIN);
                startActivityForResult(intent, RESULTENCRYPTED);
                break;
            case AppConstants.KEY_GUEST:
                Intent intent2 = new Intent(this, VerifyPatternActivity.class);
                intent2.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_GUEST);
                startActivityForResult(intent2, RESULTGUEST);
                break;
            case AppConstants.KEY_DURESS:
                Intent intent3 = new Intent(this, VerifyPatternActivity.class);
                intent3.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_DURESS);
                startActivityForResult(intent3, RESULTDURES);
                break;
        }
    }


    private void verifyCurrentCombo(String userType) {
        switch (userType) {
            case AppConstants.KEY_MAIN:
                Intent intent = new Intent(this, VerifyComboPassword.class);
                intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_MAIN);
                startActivityForResult(intent, RESULTENCRYPTED);
                break;
            case AppConstants.KEY_GUEST:
                Intent intent2 = new Intent(this, VerifyComboPassword.class);
                intent2.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_GUEST);
                startActivityForResult(intent2, RESULTGUEST);
                break;
            case AppConstants.KEY_DURESS:
                Intent intent3 = new Intent(this, VerifyComboPassword.class);
                intent3.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_DURESS);
                startActivityForResult(intent3, RESULTDURES);
                break;
        }
    }



}
