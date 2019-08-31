package com.screenlocker.secure.settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;

import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.settings.codeSetting.IMEIActivity;
import com.screenlocker.secure.settings.dataConsumption.DataConsumptionActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.secureSetting.t.ui.MainActivity;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static android.os.UserManager.DISALLOW_CONFIG_TETHERING;
import static android.os.UserManager.DISALLOW_UNMUTE_MICROPHONE;
import static com.screenlocker.secure.utils.AppConstants.KEY_DEF_BRIGHTNESS;
import static com.screenlocker.secure.utils.PrefUtils.PREF_FILE;
import static com.secureSetting.UtilityFunctions.setScreenBrightness;

public class AdvanceSettings extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advance_settings);


        sharedPref = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(mPreferencesListener);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.advance));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.tvDataUSage).setOnClickListener(this);
        findViewById(R.id.tvDataCunsumption).setOnClickListener(this);
        findViewById(R.id.tv_IMEI).setOnClickListener(this);
        findViewById(R.id.tv_set_column).setOnClickListener(this);
        findViewById(R.id.tvTheme).setOnClickListener(this);
        findViewById(R.id.tvRestore).setOnClickListener(this);
        Switch powersaver = findViewById(R.id.powerMode);
        PowerManager pm  = (PowerManager) getSystemService(Context.POWER_SERVICE);
        powersaver.setChecked(pm.isPowerSaveMode());
        powersaver.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updatePower(isChecked);
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvDataCunsumption:
                startActivity(new Intent(this, DataConsumptionActivity.class));
                break;
            case R.id.tvDataUSage:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.tv_IMEI:
                startActivity(new Intent(this, IMEIActivity.class));
                break;
            case R.id.tv_set_column:
                setColumnSizes();
                break;
            case R.id.tvTheme:
                themeDialogue();
                break;
            case R.id.tvRestore:
                restoreLocker();
                break;
        }
    }

    private void setColumnSizes() {
        int item = PrefUtils.getIntegerPref(this, AppConstants.KEY_COLUMN_SIZE);
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
                PrefUtils.saveIntegerPref(this, AppConstants.KEY_COLUMN_SIZE, 4);
            } else if (selected.get() == 0) {
                PrefUtils.saveIntegerPref(this, AppConstants.KEY_COLUMN_SIZE, 3);
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    private void themeDialogue() {
        int item;
        AtomicInteger selected = new AtomicInteger();
        if (PrefUtils.getBooleanPref(this, AppConstants.KEY_THEME)) {
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
                PrefUtils.saveBooleanPref(this, AppConstants.KEY_THEME, false);
            } else if (selected.get() == 0) {
                PrefUtils.saveBooleanPref(this, AppConstants.KEY_THEME, true);
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    SharedPreferences.OnSharedPreferenceChangeListener mPreferencesListener = (sharedPreferences, key) -> {
        if (key.equals(AppConstants.KEY_THEME)) {
            if (PrefUtils.getBooleanPref(AdvanceSettings.this, AppConstants.KEY_THEME)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            getDelegate().applyDayNight();
            recreate();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPref.unregisterOnSharedPreferenceChangeListener(mPreferencesListener);
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    private void restoreLocker() {
        new AlertDialog.Builder(this)
                .setTitle("Restore " + getResources().getString(R.string.app_name))
                .setMessage("Warning! your current Preferences will be restored to defaults.")
                .setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton("Restore", (dialog, which) -> {
                    resetSettings();
                })
                .setIcon(R.drawable.ic_warning2).
                show();


    }

    private void resetSettings() {

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Restoring Settings");
        dialog.setMessage("Please wait while we are restoring your default settings. Don not turn off your mobile.");
        dialog.setCancelable(false);
        dialog.show();

        //  default Brightness
        setScreenBrightness(this, 102);

        //default wallpapers

        PrefUtils.saveStringPref(this, AppConstants.KEY_GUEST_IMAGE, String.valueOf(R.raw._12318));
        PrefUtils.saveStringPref(this, AppConstants.KEY_SUPPORT_IMAGE, String.valueOf(R.raw.texture));
        PrefUtils.saveStringPref(this, AppConstants.KEY_MAIN_IMAGE, String.valueOf(R.raw._12321));
        PrefUtils.saveStringPref(this, AppConstants.KEY_LOCK_IMAGE, String.valueOf(R.raw._1232));

        //enable wifi by default

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        //bluetooth disable
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.disable();

        //Reset System Setting
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName compName = new ComponentName(this, MyAdmin.class);

        //enable dada toggle
        broadCastIntent();

        // allow camera
        dpm.setCameraDisabled(compName, false);

        if (dpm.isDeviceOwnerApp(getPackageName())) {
            dpm.setScreenCaptureDisabled(compName,false);
            dpm.setBluetoothContactSharingDisabled(compName, false);
            dpm.clearUserRestriction(compName, DISALLOW_UNMUTE_MICROPHONE);
            dpm.setMasterVolumeMuted(compName, false);
            dpm.clearUserRestriction(compName, DISALLOW_CONFIG_TETHERING);
        }
        //block calls
        PrefUtils.saveBooleanPref(this, AppConstants.KEY_DISABLE_CALLS, false);
        //SS app permissions to default
        AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
            List<SubExtension> subExtensions = MyApplication.getAppDatabase(this).getDao().getAllSubExtensions();
            for (SubExtension subExtension : subExtensions) {
                if (subExtension.getLabel().equals("Bluetooth") || subExtension.getLabel().equals("Hotspot")) {
                    subExtension.setEncrypted(false);
                    subExtension.setGuest(false);
                } else {
                    subExtension.setEncrypted(true);
                    subExtension.setGuest(false);
                }
                MyApplication.getAppDatabase(this).getDao().updateSubExtention(subExtension);
            }
        });

        //change Grid Size to default
        PrefUtils.saveIntegerPref(this, AppConstants.KEY_COLUMN_SIZE,AppConstants.LAUNCHER_GRID_SPAN);

        //Application permissions
        AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
            List<AppInfo> allapps = MyApplication.getAppDatabase(this).getDao().getApps();
            for (AppInfo app : allapps) {
                if (app.getUniqueName().equals(AppConstants.SFM_UNIQUE) ||
                        app.getUniqueName().equals(AppConstants.SECURE_CLEAR_UNIQUE) ||
                        app.getUniqueName().equals(AppConstants.SECURE_SETTINGS_UNIQUE)) {
                    app.setEnable(true);
                    app.setEncrypted(true);
                    app.setGuest(false);
                } else if (app.getUniqueName().equals(AppConstants.SECURE_MARKET_UNIQUE)) {
                    app.setEnable(true);
                    app.setEncrypted(true);
                    app.setGuest(true);
                } else if (app.getPackageName().equals(getPackageName())) {
                    app.setEncrypted(true);
                    app.setGuest(false);
                    app.setEnable(true);
                } else if (app.isSystemApp()) {
                    app.setEnable(false);
                    app.setGuest(true);
                    app.setEncrypted(false);
                } else {
                    app.setEncrypted(true);
                    app.setGuest(false);
                    app.setEnable(true);
                }
                MyApplication.getAppDatabase(this).getDao().updateApps(app);
            }
            AppExecutor.getInstance().getMainThread().execute(() -> {
                PrefUtils.saveBooleanPref(AdvanceSettings.this, AppConstants.KEY_THEME,false);
                if (dialog != null && dialog.isShowing()){
                    dialog.dismiss();
                }
            });

        });

    }

    void broadCastIntent() {
        Intent intent = new Intent("com.secure.systemcontrol.DATA_AND_ROAMING");
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setComponent(new ComponentName("com.secure.systemcontrol", "com.secure.systemcontrol.receivers.SettingsReceiver"));
        sendBroadcast(intent);
    }
    void updatePower(boolean state) {

    }
}
