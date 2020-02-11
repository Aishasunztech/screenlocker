package com.screenlocker.secure.launcher.subsettings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.room.MyAppDatabase;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.settings.AdvanceSettings;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.secure.launcher.R;

import java.util.List;

import static android.os.UserManager.DISALLOW_CONFIG_TETHERING;
import static android.os.UserManager.DISALLOW_UNMUTE_MICROPHONE;
import static com.screenlocker.secure.utils.AppConstants.APPS_SETTING_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.BROADCAST_APPS_ACTION;
import static com.screenlocker.secure.utils.AppConstants.KEY_DATABASE_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.SECURE_SETTINGS_CHANGE;
import static com.secureSetting.UtilityFunctions.setScreenBrightness;

public class BackuoAndRestoreActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backuo_and_restore);
        setToolbar();
        findViewById(R.id.tvRestore).setOnClickListener(this);

    }

    private void setToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.backup_and_restore));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.tvRestore){

                restoreLocker();
        }
    }
    private void restoreLocker() {
        new AlertDialog.Builder(this)
                .setTitle("Restore " + getResources().getString(R.string.app_name))
                .setMessage("Warning this will restore all Settings back to factory settings, you will not lose data but your device appearance such as wallpapers or apps showing and settings may change. Would you like to continue?")
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
        dialog.setTitle(getResources().getString(R.string.restore_settings));
        dialog.setMessage(getResources().getString(R.string.rest_msg));
        dialog.setCancelable(false);
        dialog.show();

        //  default Brightness
        setScreenBrightness(this, 102);

        //default wallpapers

        PrefUtils.saveStringPref(this, AppConstants.KEY_GUEST_IMAGE, String.valueOf(R.raw._12318));
        PrefUtils.saveStringPref(this, AppConstants.KEY_SUPPORT_IMAGE, String.valueOf(R.raw.texture));
        PrefUtils.saveStringPref(this, AppConstants.KEY_MAIN_IMAGE, String.valueOf(R.raw._1239));
        PrefUtils.saveStringPref(this, AppConstants.KEY_LOCK_IMAGE, String.valueOf(R.raw._12316));

        //enable wifi by default

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        //bluetooth disable
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null)
            mBluetoothAdapter.disable();

        //Reset System Setting
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName compName = new ComponentName(this, MyAdmin.class);

        //enable dada toggle
        broadCastIntent();

        // allow camera
        dpm.setCameraDisabled(compName, false);

        if (dpm.isDeviceOwnerApp(getPackageName())) {
            dpm.setScreenCaptureDisabled(compName, false);
            dpm.setBluetoothContactSharingDisabled(compName, false);
            dpm.clearUserRestriction(compName, DISALLOW_UNMUTE_MICROPHONE);
            dpm.setMasterVolumeMuted(compName, false);
            dpm.clearUserRestriction(compName, DISALLOW_CONFIG_TETHERING);
        }
        //block calls
        PrefUtils.saveBooleanPref(this, AppConstants.KEY_DISABLE_CALLS, false);
        //SS app permissions to default
        AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
            List<SubExtension> subExtensions = MyAppDatabase.getInstance(this).getDao().getAllSubExtensions();
            for (SubExtension subExtension : subExtensions) {
                if (subExtension.getLabel().equals("Bluetooth") || subExtension.getLabel().equals("Hotspot")) {
                    subExtension.setEncrypted(false);
                    subExtension.setGuest(false);
                } else {
                    subExtension.setEncrypted(true);
                    subExtension.setGuest(false);
                }
                MyAppDatabase.getInstance(this).getDao().updateSubExtention(subExtension);
            }
        });

        //change Grid Size to default
        PrefUtils.saveIntegerPref(this, AppConstants.KEY_COLUMN_SIZE, AppConstants.LAUNCHER_GRID_SPAN);

        //Application permissions
        AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
            List<AppInfo> allapps = MyAppDatabase.getInstance(this).getDao().getApps();
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
                    app.setEncrypted(false);
                    app.setGuest(false);
                    app.setEnable(false);
                } else if (app.getPackageName().equals("com.armorSec.android")) {
                    app.setEnable(true);
                    app.setGuest(false);
                    app.setEncrypted(true);
                } else if (app.getPackageName().equals("ca.unlimitedwireless.mailpgp")) {
                    app.setEnable(true);
                    app.setGuest(false);
                    app.setEncrypted(true);
                } else if (app.getPackageName().equals("com.rim.mobilefusion.client")) {
                    app.setEnable(true);
                    app.setGuest(false);
                    app.setEncrypted(true);
                } else if (app.getPackageName().equals("com.secure.vpn")) {
                    app.setEnable(true);
                    app.setGuest(false);
                    app.setEncrypted(true);
                } else {
                    app.setEnable(false);
                    app.setGuest(true);
                    app.setEncrypted(false);
                }
                MyAppDatabase.getInstance(this).getDao().updateApps(app);
            }
            AppExecutor.getInstance().getMainThread().execute(() -> {

                PrefUtils.saveBooleanPref(this, SECURE_SETTINGS_CHANGE, true);

                PrefUtils.saveBooleanPref(this, APPS_SETTING_CHANGE, true);

                Intent intent = new Intent(BROADCAST_APPS_ACTION);
                intent.putExtra(KEY_DATABASE_CHANGE, "extensions");
                LocalBroadcastManager.getInstance(BackuoAndRestoreActivity.this).sendBroadcast(intent);

                PrefUtils.saveBooleanPref(BackuoAndRestoreActivity.this, AppConstants.KEY_THEME, false);

                Intent intent1 = new Intent(BROADCAST_APPS_ACTION);
                intent1.putExtra(KEY_DATABASE_CHANGE, "apps");
                LocalBroadcastManager.getInstance(BackuoAndRestoreActivity.this).sendBroadcast(intent1);
                if (dialog != null && dialog.isShowing()) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home){
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
