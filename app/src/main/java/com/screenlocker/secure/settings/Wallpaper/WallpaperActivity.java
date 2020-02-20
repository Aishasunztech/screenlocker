package com.screenlocker.secure.settings.Wallpaper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.screenlocker.secure.launcher.subsettings.SSettingsViewModel;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.settings.AdvanceSettings;
import com.screenlocker.secure.utils.SecuredSharedPref;
import com.secure.launcher.R;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.settings.SettingsPresenter;
import com.screenlocker.secure.settings.managepassword.ManagePasswords;
import com.screenlocker.secure.settings.managepassword.PasswordOptionsAcitivity;
import com.screenlocker.secure.settings.managepassword.VerifyComboPassword;
import com.screenlocker.secure.settings.managepassword.VerifyPatternActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.secureSetting.SecureSettingsMain;
import com.secureSetting.SleepDialog;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.IS_SETTINGS_ALLOW;
import static com.screenlocker.secure.utils.AppConstants.KEY_CODE;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.PrefUtils.PREF_FILE;
import static com.secureSetting.UtilityFunctions.getBlueToothStatus;
import static com.secureSetting.UtilityFunctions.getScreenBrightness;
import static com.secureSetting.UtilityFunctions.getWifiStatus;
import static com.secureSetting.UtilityFunctions.permissionModify;
import static com.secureSetting.UtilityFunctions.pxFromDp;
import static com.secureSetting.UtilityFunctions.setScreenBrightness;

public class WallpaperActivity extends BaseActivity implements View.OnClickListener , SleepDialog.SleepChangerListener {

    private boolean goToGuest,goToEncrypt,goToLockScreen;
    private static final int RESULTGUEST = 100, RESULTENCRYPTED = 101, RESULTCODE = 102;
    private SharedPreferences sharedPref;
    private PopupWindow popupWindow;
    private LinearLayout brightnessContainer;
    private TextView brightnessLevel;
    private SecuredSharedPref securedSharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_wallpaper);
        securedSharedPref = SecuredSharedPref.getInstance(this);
        sharedPref = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(mPreferencesListener);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.change_wallpaper_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.btnGuestWallpaper).setOnClickListener(this);
        findViewById(R.id.btnEncryptedWallpaper).setOnClickListener(this);
        findViewById(R.id.btnLockScreenWallpaer).setOnClickListener(this);
        findViewById(R.id.tvTheme).setOnClickListener(this);
        findViewById(R.id.sleep_cotainer).setOnClickListener(this);
        findViewById(R.id.tv_set_column).setOnClickListener(this);
        brightnessContainer = findViewById(R.id.brightness_container_layout);
        brightnessLevel = findViewById(R.id.brightness_lavel);

        goToGuest = false;
        goToEncrypt = false;
        goToLockScreen = false;
        brightnessContainer.setOnClickListener(v -> {
            boolean permission = permissionModify(WallpaperActivity.this);
            if (permission) {
                int width = (int) (Resources.getSystem().getDisplayMetrics().widthPixels * 0.90);
                LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.dialog_brightness, null);

                popupWindow = new PopupWindow(container, width, (int) pxFromDp(WallpaperActivity.this, 60), true);
                popupWindow.showAtLocation(findViewById(R.id.rootLayout), Gravity.CENTER_HORIZONTAL, 0, -400);
                SeekBar seekBar = container.findViewById(R.id.seek_bar);

                int brightness = getScreenBrightness(WallpaperActivity.this);
                seekBar.setProgress(brightness);
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        setScreenBrightness(WallpaperActivity.this, progress);
                        brightnessLevel.setText((int) (((float) getScreenBrightness(WallpaperActivity.this) / 255) * 100) + "%");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

            }


        });
        SSettingsViewModel settingsViewModel = new ViewModelProvider(this).get(SSettingsViewModel.class);
        String userType = prefUtils.getStringPref( CURRENT_KEY);
        settingsViewModel.getSubExtensions().observe(this, subExtensions -> {
            if (userType.equals(AppConstants.KEY_MAIN_PASSWORD)) {
                setUpPermissionSettingsEncrypted(subExtensions);
            } else if (userType.equals(AppConstants.KEY_GUEST_PASSWORD)) {
                setUpPermissionSettingsGuest(subExtensions);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
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
            case R.id.tvTheme:
                themeDialogue();
                break;
            case R.id.sleep_cotainer:
                SleepDialog sleepDialog = new SleepDialog(WallpaperActivity.this);
                sleepDialog.show();
                break;
            case R.id.tv_set_column:
                setColumnSizes();
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
        alertDialog.setIcon(R.drawable.ic_secure_settings);
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
                (dialog, which) -> dialog.cancel());

        alertDialog.show();

    }

    private void handlePassword(String password, String type) {
        switch (type) {
            case KEY_GUEST:
                if (password.equals(securedSharedPref.getStringPref( AppConstants.KEY_GUEST_PASSWORD))) {
                    //

                    goToGuest = true;
                    Intent intent = new Intent(this, ChangeWallpaper.class);
                    intent.putExtra("TYPE", KEY_GUEST);
                    startActivity(intent);
                }else
                    showInvalidPasswordDialog(this);
                break;
            case KEY_MAIN:
                if (password.equals(securedSharedPref.getStringPref( AppConstants.KEY_MAIN_PASSWORD))){

                    goToEncrypt = true;

                    Intent intent = new Intent(this, ChangeWallpaper.class);
                    intent.putExtra("TYPE", KEY_MAIN);
                    startActivity(intent);
                }
                else
                    showInvalidPasswordDialog(this);

                break;
            case KEY_CODE:
                if (password.equals(securedSharedPref.getStringPref( AppConstants.KEY_MAIN_PASSWORD))){

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
        TextView textView = findViewById(R.id.coloumn_numbers);
        AppConstants.TEMP_SETTINGS_ALLOWED = true;
        brightnessLevel.setText((int) (((float) getScreenBrightness(this) / 255) * 100) + "%");
        int item = prefUtils.getIntegerPref( AppConstants.KEY_COLUMN_SIZE);
        if (item != 0) {
            if (item == 3) {
                textView.setText(R.string._4_columns);
            } else {
                textView.setText(R.string._5_columns);
            }
        } else {
            textView.setText(R.string._5_columns);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
                }else if (resultCode == RESULT_CANCELED){
                    Snackbar.make(findViewById(R.id.rootLayout), "Incorrect Password", Snackbar.LENGTH_LONG).show();
                }
                break;
            case RESULTGUEST:
                if (resultCode == RESULT_OK) {
                    goToGuest = true;
                    Intent intent = new Intent(this, ChangeWallpaper.class);
                    intent.putExtra("TYPE", KEY_GUEST);
                    startActivity(intent);
                }
                else if (resultCode == RESULT_CANCELED){
                    Snackbar.make(findViewById(R.id.rootLayout), "Incorrect Password", Snackbar.LENGTH_LONG).show();
                }
                break;
            case RESULTCODE:
                if (resultCode == RESULT_OK) {
                    goToLockScreen = true;

                    Intent intent = new Intent(this, ChangeWallpaper.class);
                    intent.putExtra("TYPE", KEY_CODE);
                    startActivity(intent);
                }
                else if (resultCode == RESULT_CANCELED){
                    Snackbar.make(findViewById(R.id.rootLayout), "Incorrect Password", Snackbar.LENGTH_LONG).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
    public void handleSetGuestPassword() {
        String passConfig = securedSharedPref.getStringPref( AppConstants.GUEST_DEFAULT_CONFIG);
        if (passConfig == null) {
            if (securedSharedPref.getStringPref( KEY_GUEST_PASSWORD) != null)
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
            case AppConstants.COMBO_PASSWORD:
                verifyCurrentCombo(AppConstants.KEY_GUEST);
                break;
        }


    }
    public void handleSetMainPassword() {
        String passConfig = securedSharedPref.getStringPref( AppConstants.ENCRYPT_DEFAULT_CONFIG);
        if (passConfig == null) {
            if (securedSharedPref.getStringPref( KEY_MAIN_PASSWORD) != null)
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
            case AppConstants.COMBO_PASSWORD:
                verifyCurrentCombo(AppConstants.KEY_MAIN);
                break;
        }

    }
    public void handleSetMainPasswordForLS() {
        String passConfig = securedSharedPref.getStringPref( AppConstants.ENCRYPT_DEFAULT_CONFIG);
        if (passConfig == null) {
            if (securedSharedPref.getStringPref( KEY_MAIN_PASSWORD) != null)
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
            case AppConstants.COMBO_PASSWORD:
                verifyCurrentCombo(AppConstants.KEY_CODE);
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
                intent3.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_MAIN);
                startActivityForResult(intent3, RESULTCODE);
                break;
        }
    }

    SharedPreferences.OnSharedPreferenceChangeListener mPreferencesListener = (sharedPreferences, key) -> {

        if (key.equals(AppConstants.KEY_THEME)) {
            if (prefUtils.getBooleanPref( AppConstants.KEY_THEME)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            getDelegate().applyDayNight();
            restartActivity();

        }
    };
    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        prefUtils.saveBooleanPref( IS_SETTINGS_ALLOW, false);
        AppConstants.TEMP_SETTINGS_ALLOWED = false;
        sharedPref.unregisterOnSharedPreferenceChangeListener(mPreferencesListener);
    }


    private void themeDialogue() {
        int item;
        AtomicInteger selected = new AtomicInteger();
        if (prefUtils.getBooleanPref( AppConstants.KEY_THEME)) {
            item = 0;
            selected.set(0);
        } else {
            item = 1;
            selected.set(1);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick A Theme");
        builder.setSingleChoiceItems(R.array.themes, item, (dialog, which) -> {
            selected.set(which);
        });
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            if (selected.get() == 1) {
                prefUtils.saveBooleanPref( AppConstants.KEY_THEME, false);
            } else if (selected.get() == 0) {
                prefUtils.saveBooleanPref( AppConstants.KEY_THEME, true);
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    @Override
    public void sleepTimeChanged(String time) {

    }
    private void setColumnSizes() {
        int item = prefUtils.getIntegerPref( AppConstants.KEY_COLUMN_SIZE);
        AtomicInteger selected = new AtomicInteger();
        if (item != 0) {
            if (item == 3) {
                selected.set(0);

            } else {
                selected.set(1);
            }
        } else {
            selected.set(1);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Column Span");
        builder.setSingleChoiceItems(R.array.column_sizes, selected.get(), (dialog, which) -> {
            selected.set(which);
        });
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            if (selected.get() == 1) {
                prefUtils.saveIntegerPref( AppConstants.KEY_COLUMN_SIZE, 4);
            } else if (selected.get() == 0) {
                prefUtils.saveIntegerPref( AppConstants.KEY_COLUMN_SIZE, 3);
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }


    void setUpPermissionSettingsEncrypted(List<SubExtension> settings) {
        for (SubExtension setting : settings) {
            Timber.d("setUpPermissionSettingsEncrypted: %s", setting.getUniqueExtension());
            switch (setting.getUniqueExtension()) {
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_Brightness:
                    if (setting.isEncrypted()) {
                        brightnessContainer.setVisibility(View.VISIBLE);
                    } else brightnessContainer.setVisibility(View.GONE);
                    break;
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_Sleep:
                    if (setting.isEncrypted()) {
                        findViewById(R.id.sleep_cotainer).setVisibility(View.VISIBLE);
                    } else findViewById(R.id.sleep_cotainer).setVisibility(View.GONE);
                    break;

            }
        }
    }

    void setUpPermissionSettingsGuest(List<SubExtension> settings) {
        for (SubExtension setting : settings) {
            Timber.d("setUpPermissionSettingsGuest: %s", setting.getUniqueExtension());
            switch (setting.getUniqueExtension()) {
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_Brightness:
                    if (setting.isGuest()) {
                        brightnessContainer.setVisibility(View.VISIBLE);
                    } else brightnessContainer.setVisibility(View.GONE);
                    break;
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_Sleep:
                    if (setting.isGuest()) {
                        findViewById(R.id.sleep_cotainer).setVisibility(View.VISIBLE);
                    } else findViewById(R.id.sleep_cotainer).setVisibility(View.GONE);
                    break;
            }
        }
    }



}
