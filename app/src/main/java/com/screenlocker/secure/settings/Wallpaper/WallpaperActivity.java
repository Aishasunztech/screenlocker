package com.screenlocker.secure.settings.Wallpaper;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.screenlocker.secure.R;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.settings.SettingsPresenter;
import com.screenlocker.secure.settings.managepassword.ManagePasswords;
import com.screenlocker.secure.settings.managepassword.PasswordOptionsAcitivity;
import com.screenlocker.secure.settings.managepassword.VerifyPatternActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static com.screenlocker.secure.utils.AppConstants.KEY_CODE;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;

public class WallpaperActivity extends BaseActivity implements View.OnClickListener {

    private boolean isBackPressed = false;
    private boolean goToGuest,goToEncrypt,goToLockScreen;
    private static final int RESULTGUEST = 100, RESULTENCRYPTED = 101, RESULTCODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_wallpaper);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.change_wallpaper_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.btnGuestWallpaper).setOnClickListener(this);
        findViewById(R.id.btnEncryptedWallpaper).setOnClickListener(this);
        findViewById(R.id.btnLockScreenWallpaer).setOnClickListener(this);

        goToGuest = false;
        goToEncrypt = false;
        goToLockScreen = false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGuestWallpaper:
                handleSetGuestPassword();
                break;
            case R.id.btnEncryptedWallpaper:
                handleSetMainPassword();
                break;
            case R.id.btnLockScreenWallpaer:
                handleSetMainPasswordForLS();
                break;
        }
    }

    public void showAlertDialog(String title, final String type) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(10, 10, 10, 10);
        final EditText input = new EditText(WallpaperActivity.this);
        input.setGravity(Gravity.CENTER);

        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        //input.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.mipmap.ic_launcher);
        input.setFocusable(true);
        input.setFocusableInTouchMode(true);
        input.clearFocus();
        input.requestFocus();
        input.postDelayed(() -> {
                    InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.showSoftInput(input, 0);
                }
                , 0);

        alertDialog.setPositiveButton(R.string.ok,
                (dialog, which) -> {
                    String password = input.getText().toString().trim();
                    if (password.equals("")){
                        Snackbar.make(findViewById(R.id.rootLayout),getResources().getString(R.string.please_enter_password),Snackbar.LENGTH_LONG).show();
                    }else
                        handlePassword(password , type);
                });

        alertDialog.setNegativeButton(R.string.cancel,
                (dialog, which) -> {
                    dialog.cancel();
                });

        alertDialog.show();

    }

    private void handlePassword(String password, String type) {
        switch (type) {
            case KEY_GUEST:
                if (password.equals(PrefUtils.getStringPref(WallpaperActivity.this, AppConstants.KEY_GUEST_PASSWORD))) {
                    //

                    goToGuest = true;
                    Intent intent = new Intent(this, ChangeWallpaper.class);
                    intent.putExtra("TYPE", KEY_GUEST);
                    startActivity(intent);
                }else
                    showInvalidPasswordDialog(this);
                break;
            case KEY_MAIN:
                if (password.equals(PrefUtils.getStringPref(WallpaperActivity.this, AppConstants.KEY_MAIN_PASSWORD))){

                    goToEncrypt = true;

                    Intent intent = new Intent(this, ChangeWallpaper.class);
                    intent.putExtra("TYPE", KEY_MAIN);
                    startActivity(intent);
                }
                else
                    showInvalidPasswordDialog(this);

                break;
            case KEY_CODE:
                if (password.equals(PrefUtils.getStringPref(WallpaperActivity.this, AppConstants.KEY_MAIN_PASSWORD))){

                    goToLockScreen = true;

                    Intent intent = new Intent(this, ChangeWallpaper.class);
                    intent.putExtra("TYPE", KEY_CODE);
                    startActivity(intent);
                }
                else
                    showInvalidPasswordDialog(this);

                break;
        }
    }

    private void showInvalidPasswordDialog(AppCompatActivity activity) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(getResources().getString(R.string.invalid_password_title));
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialog.setMessage(getResources().getString(R.string.invalid_password_message));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,  getResources().getString(R.string.ok_text),
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();

        isBackPressed = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!isBackPressed)
        {
            if(!goToGuest && !goToEncrypt && !goToLockScreen)
            {
                //this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        switch (requestCode) {

            case RESULTENCRYPTED:
                if (resultCode == RESULT_OK) {
                    goToGuest = true;
                    Intent intent = new Intent(this, ChangeWallpaper.class);
                    intent.putExtra("TYPE", KEY_MAIN);
                    startActivity(intent);
                }
                break;
            case RESULTGUEST:
                if (resultCode == RESULT_OK) {
                    goToGuest = true;
                    Intent intent = new Intent(this, ChangeWallpaper.class);
                    intent.putExtra("TYPE", KEY_GUEST);
                    startActivity(intent);
                }
                break;
            case RESULTCODE:
                if (resultCode == RESULT_OK) {
                    goToLockScreen = true;

                    Intent intent = new Intent(this, ChangeWallpaper.class);
                    intent.putExtra("TYPE", KEY_CODE);
                    startActivity(intent);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
    public void handleSetGuestPassword() {
        String passConfig = PrefUtils.getStringPref(this, AppConstants.GUEST_DEFAULT_CONFIG);
        if (passConfig == null) {
            if (PrefUtils.getStringPref(this, KEY_GUEST_PASSWORD) != null)
                showAlertDialog(getResources().getString(R.string.guest_password_dialog_title),KEY_GUEST);
            return;

        }
        switch (passConfig) {
            case AppConstants.PATTERN_PASSWORD:
                verifyCurrentPattern(AppConstants.KEY_GUEST);
                break;
            case AppConstants.PIN_PASSWORD:
                showAlertDialog(getResources().getString(R.string.guest_password_dialog_title),KEY_GUEST);
                break;
        }


    }
    public void handleSetMainPassword() {
        String passConfig = PrefUtils.getStringPref(this, AppConstants.ENCRYPT_DEFAULT_CONFIG);
        if (passConfig == null) {
            if (PrefUtils.getStringPref(this, KEY_MAIN_PASSWORD) != null)
                showAlertDialog(getResources().getString(R.string.encrypted_password_dialog_title),KEY_MAIN);
            return;

        }
        switch (passConfig) {
            case AppConstants.PATTERN_PASSWORD:
                verifyCurrentPattern(AppConstants.KEY_MAIN);
                break;
            case AppConstants.PIN_PASSWORD:
                showAlertDialog(getResources().getString(R.string.encrypted_password_dialog_title),KEY_MAIN);
                break;
        }

    }
    public void handleSetMainPasswordForLS() {
        String passConfig = PrefUtils.getStringPref(this, AppConstants.ENCRYPT_DEFAULT_CONFIG);
        if (passConfig == null) {
            if (PrefUtils.getStringPref(this, KEY_MAIN_PASSWORD) != null)
                showAlertDialog(getResources().getString(R.string.encrypted_password_dialog_title),KEY_CODE);
            return;

        }
        switch (passConfig) {
            case AppConstants.PATTERN_PASSWORD:
                verifyCurrentPattern(KEY_CODE);
                break;
            case AppConstants.PIN_PASSWORD:
                showAlertDialog(getResources().getString(R.string.encrypted_password_dialog_title),KEY_CODE);
                break;
        }

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
            case KEY_CODE:
                Intent intent3 = new Intent(this, VerifyPatternActivity.class);
                intent3.putExtra(Intent.EXTRA_TEXT, KEY_MAIN);
                startActivityForResult(intent3, RESULTCODE);
                break;
        }
    }
}
