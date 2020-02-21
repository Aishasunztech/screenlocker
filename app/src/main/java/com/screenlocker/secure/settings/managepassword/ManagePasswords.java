package com.screenlocker.secure.settings.managepassword;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.screenlocker.secure.launcher.subsettings.SSettingsViewModel;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.utils.SecuredSharedPref;
import com.secure.launcher.R;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.List;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.IS_SETTINGS_ALLOW;
import static com.screenlocker.secure.utils.AppConstants.KEY_DURESS_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;

public class ManagePasswords extends BaseActivity implements View.OnClickListener {

    private static final int RESULTGUEST = 100, RESULTENCRYPTED = 101, RESULTDURES = 102, RESULT_DURESS_ENCRYPTED = 103;
    public static final String VERIFY_DURESS_ENCRYPT = "VERIFY_DURESS";

    private LinearLayout rootLayout;
    private Toolbar mToolbar;
    private Chip duressStatus;
    private boolean isBackPressed = false, goToGuest, goToEncrypt, goToDuress;
    /**
     * request code for the set password activity
     */
    public static final int REQUEST_CODE_PASSWORD = 883;
    private SecuredSharedPref securedSharedPref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_passwords);

        securedSharedPref = SecuredSharedPref.getInstance(this);
        setIds();
        if (securedSharedPref.getStringPref( AppConstants.KEY_DURESS_PASSWORD) != null) {
            duressStatus.setText(getResources().getString(R.string.activated_code));
        } else {
            if (securedSharedPref.getStringPref( AppConstants.DUERESS_DEFAULT_CONFIG) != null) {
                duressStatus.setText(getResources().getString(R.string.activated_code));
            }else {
                duressStatus.setText(getResources().getString(R.string.not_set));
            }
        }
        setListeners();
        String userType = prefUtils.getStringPref( CURRENT_KEY);
        SSettingsViewModel settingsViewModel = ViewModelProviders.of(this).get(SSettingsViewModel.class);

        settingsViewModel.getSubExtensions().observe(this, subExtensions -> {
            if (userType.equals(AppConstants.KEY_MAIN_PASSWORD)) {
                setUpPermissionSettingsEncrypted(subExtensions);
            } else if (userType.equals(AppConstants.KEY_GUEST_PASSWORD)) {
                setUpPermissionSettingsGuest(subExtensions);
            }
        });


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
                    Snackbar.make(findViewById(R.id.rootLayout), getResources().getString(R.string.incorrect_password), Snackbar.LENGTH_LONG).show();
                }
                break;
            case RESULTGUEST:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(this, PasswordOptionsAcitivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_GUEST);
                    startActivityForResult(intent, REQUEST_CODE_PASSWORD);
                }
                else if (resultCode == RESULT_CANCELED){
                    Snackbar.make(findViewById(R.id.rootLayout), getResources().getString(R.string.incorrect_password), Snackbar.LENGTH_LONG).show();
                }
                break;
            case RESULTDURES:
            case RESULT_DURESS_ENCRYPTED:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent(this, PasswordOptionsAcitivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_DURESS);
                    startActivityForResult(intent, REQUEST_CODE_PASSWORD);
                }
                else if (resultCode == RESULT_CANCELED){
                    Snackbar.make(findViewById(R.id.rootLayout), getResources().getString(R.string.incorrect_password), Snackbar.LENGTH_LONG).show();
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
        findViewById(R.id.screen_lock_container).setOnClickListener(this);
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
            getSupportActionBar().setTitle(getResources().getString(R.string.biometrics_and_security));
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
                handleSetGuestPassword(ManagePasswords.this, rootLayout);
                break;

            case R.id.tvSetMainPassword:    // handle the set main password click event
                /**
                 * start the {@link SetUpLockActivity} to get the password
                 */
//                settingsActivity.handleSetMainPassword(ManagePasswords.this, rootLayout);
                handleSetMainPassword(ManagePasswords.this,  AppConstants.KEY_MAIN);
                break;

            case R.id.tvSetDuressPassword:    // handle the set duress password click event
                /**
                 * start the {@link SetUpLockActivity} to get the password
                 */

//                settingsActivity.handleSetDuressPassword(ManagePasswords.this, rootLayout);
                handleSetDuressPassword(ManagePasswords.this);
                break;
            case R.id.screen_lock_container:
                startActivity(new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD));
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
        if (securedSharedPref.getStringPref( AppConstants.KEY_DURESS_PASSWORD) != null) {
            duressStatus.setText(getResources().getString(R.string.activated_code));
        } else {
            if (securedSharedPref.getStringPref( AppConstants.DUERESS_DEFAULT_CONFIG) != null) {
                duressStatus.setText(getResources().getString(R.string.activated_code));
            }else {
                duressStatus.setText(getResources().getString(R.string.not_set));
            }
        }
        setListeners();
        AppConstants.TEMP_SETTINGS_ALLOWED = true;
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

    public void handleSetGuestPassword(AppCompatActivity activity,  View rootLayout) {
        String passConfig = securedSharedPref.getStringPref( AppConstants.GUEST_DEFAULT_CONFIG);
        if (passConfig == null) {
            if (securedSharedPref.getStringPref( KEY_GUEST_PASSWORD) != null)
                showGuestPin(activity);
            return;

        }
        switch (passConfig) {
            case AppConstants.PATTERN_PASSWORD:
                verifyCurrentPattern(AppConstants.KEY_GUEST);
                break;
            case AppConstants.PIN_PASSWORD:
                showGuestPin(activity);
                break;
            case AppConstants.COMBO_PASSWORD:
                verifyCurrentCombo(AppConstants.KEY_GUEST);
                break;
        }


    }

    private void showGuestPin(AppCompatActivity activity) {
        final EditText input = new EditText(activity);

       showAlertDialog(input, (dialogInterface, i) -> {

            if (TextUtils.isEmpty(input.getText().toString().trim())) {
                showAlertDialog(activity, getResources().getString(R.string.invalid_password_title), getResources().getString(R.string.invalid_password_message), android.R.drawable.stat_sys_warning);
                return;
            }

            if (input.getText().toString().
                    equalsIgnoreCase(securedSharedPref.getStringPref(
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

    public void handleSetMainPassword(AppCompatActivity activity,   String key_type) {
        String passConfig = securedSharedPref.getStringPref( AppConstants.ENCRYPT_DEFAULT_CONFIG);
        if (passConfig == null) {
            if (securedSharedPref.getStringPref( KEY_MAIN_PASSWORD) != null)
                showEncryptedPin(activity,key_type);
            return;

        }
        switch (passConfig) {
            case AppConstants.PATTERN_PASSWORD:
                verifyCurrentPattern(key_type);
                break;
            case AppConstants.PIN_PASSWORD:
                showEncryptedPin(activity, key_type);
                break;
            case AppConstants.COMBO_PASSWORD:
                verifyCurrentCombo(key_type);
                break;
        }

    }

    private void showEncryptedPin(AppCompatActivity activity, String keyType) {
        final EditText input = new EditText(activity);
        showAlertDialog(input, (dialogInterface, i) -> {

            if (TextUtils.isEmpty(input.getText().toString().trim())) {
                showAlertDialog(activity, getResources().getString(R.string.invalid_password_title), getResources().getString(R.string.invalid_password_message), android.R.drawable.stat_sys_warning);
                return;
            }

            if (input.getText().toString().
                    equalsIgnoreCase(securedSharedPref.getStringPref(
                            KEY_MAIN_PASSWORD))) {
                // if password is right then allow user to change it
                goToEncrypt = true;
                Intent setUpLockActivityIntent = new Intent(activity, PasswordOptionsAcitivity.class);
                switch (keyType){
                    case AppConstants.KEY_MAIN:
                        setUpLockActivityIntent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_MAIN);
                        activity.startActivityForResult(setUpLockActivityIntent, REQUEST_CODE_PASSWORD);
                        break;
                    case VERIFY_DURESS_ENCRYPT:
                        setUpLockActivityIntent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_DURESS);
                        activity.startActivityForResult(setUpLockActivityIntent, REQUEST_CODE_PASSWORD);
                        break;

                }


            } else {
                showAlertDialog(activity, getResources().getString(R.string.invalid_password_title), getResources().getString(R.string.invalid_password_message), android.R.drawable.ic_dialog_alert);
//                        Toast.makeText(StateSettingsActivity.this, R.string.wrong_password_entered, Toast.LENGTH_SHORT).show();
            }
        }, null, activity.getString(R.string.please_enter_current_encrypted_password));
    }

    public void handleSetDuressPassword(AppCompatActivity activity) {
        String passConfig = securedSharedPref.getStringPref( AppConstants.DUERESS_DEFAULT_CONFIG);
        if (passConfig == null) {
            if (securedSharedPref.getStringPref( KEY_DURESS_PASSWORD) != null)
                showDuressPin(activity);
            else {
                new AlertDialog.Builder(activity).
                        setTitle(getResources().getString(R.string.duress_password_warning))
                        .setMessage(getResources().getString(R.string.duress_password_message)).setPositiveButton(getResources().getString(R.string.ok_text), (dialogInterface, i) -> {
                    goToDuress = true;
                    handleSetMainPassword(ManagePasswords.this,  VERIFY_DURESS_ENCRYPT);
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
                showDuressPin(activity);
                break;
            case AppConstants.COMBO_PASSWORD:
                verifyCurrentCombo(AppConstants.KEY_DURESS);
                break;


        }

    }

    private void showDuressPin(AppCompatActivity activity) {
        final EditText input = new EditText(activity);
       showAlertDialog(input, (dialogInterface, i) -> {

            if (TextUtils.isEmpty(input.getText().toString().trim())) {
                showAlertDialog(activity, getResources().getString(R.string.invalid_password_title), getResources().getString(R.string.invalid_password_message), android.R.drawable.stat_sys_warning);
                return;
            }

            if (input.getText().toString().
                    equalsIgnoreCase(securedSharedPref.getStringPref(
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

            case VERIFY_DURESS_ENCRYPT:
                Intent intent4 = new Intent(this, VerifyPatternActivity.class);
                intent4.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_MAIN);
                startActivityForResult(intent4, RESULT_DURESS_ENCRYPTED);
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
            case VERIFY_DURESS_ENCRYPT:
                Intent intent4 = new Intent(this, VerifyPatternActivity.class);
                intent4.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_MAIN);
                startActivityForResult(intent4, RESULT_DURESS_ENCRYPTED);
        }
    }




    void setUpPermissionSettingsEncrypted(List<SubExtension> settings) {
        for (SubExtension setting : settings) {
            Timber.d("setUpPermissionSettingsEncrypted: %s", setting.getUniqueExtension());
            switch (setting.getUniqueExtension()) {
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_Finger:
                    if (setting.isEncrypted()) {
                        findViewById(R.id.screen_lock_container).setVisibility(View.VISIBLE);
                    } else findViewById(R.id.screen_lock_container).setVisibility(View.GONE);
                    break;
            }
        }
    }

    void setUpPermissionSettingsGuest(List<SubExtension> settings) {
        for (SubExtension setting : settings) {
            Timber.d("setUpPermissionSettingsGuest: %s", setting.getUniqueExtension());
            switch (setting.getUniqueExtension()) {
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_Finger:
                    if (setting.isGuest()) {
                        findViewById(R.id.screen_lock_container).setVisibility(View.VISIBLE);
                    } else findViewById(R.id.screen_lock_container).setVisibility(View.GONE);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        prefUtils.saveBooleanPref( IS_SETTINGS_ALLOW, false);
        super.onDestroy();
        AppConstants.TEMP_SETTINGS_ALLOWED = false;
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
        alertDialog.setIcon(R.drawable.ic_secure_settings);
        input.setFocusable(true);
        input.setFocusableInTouchMode(true);
        input.clearFocus();
        input.requestFocus();
        input.postDelayed(new Runnable(){
                              @Override public void run(){
                                  InputMethodManager keyboard=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                  keyboard.showSoftInput(input,0);
                              }
                          }
                ,100);
//

        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onClickListener.onClick(dialog, which);
                        try {
//                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        alertDialog.setNegativeButton(R.string.cancel,
                (dialog, which) -> {

                    try {
                        if (onNegativeClick != null)
                            onNegativeClick.onClick(dialog, which);
//                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dialog.cancel();
                });

        alertDialog.show();

    }
}
