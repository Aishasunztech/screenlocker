package com.securesettings;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static com.securesettings.UtilityFunctions.checkLocationStatus;
import static com.securesettings.UtilityFunctions.checkPermissions;
import static com.securesettings.UtilityFunctions.getBlueToothStatus;
import static com.securesettings.UtilityFunctions.getScreenBrightness;
import static com.securesettings.UtilityFunctions.getSleepTime;
import static com.securesettings.UtilityFunctions.getWifiStatus;
import static com.securesettings.UtilityFunctions.permissionModify;
import static com.securesettings.UtilityFunctions.pxFromDp;
import static com.securesettings.UtilityFunctions.secondsToMintues;
import static com.securesettings.UtilityFunctions.setScreenBrightness;
import static com.securesettings.UtilityFunctions.turnOnLocation;

public class SecureSettingsMain extends AppCompatActivity implements BrightnessDialog.BrightnessChangeListener
        , SleepDialog.SleepChangerListener {

    private PopupWindow popupWindow;

    private TextView bluetoothLabel, brightnessLevel, sleepTime, wifiName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_main);

        setToolbar();

        setSetIds();

        checkPermissions(this);
        permissionModify(SecureSettingsMain.this);
        if (!checkLocationStatus(this)) {
            turnOnLocation(this);
        }
        clickListeners();

    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Secure Settings");
        }

    }

    private void setSetIds() {
        bluetoothLabel = findViewById(R.id.bluetooth_label);
        brightnessLevel = findViewById(R.id.brightness_lavel);
        sleepTime = findViewById(R.id.sleep_time);
        wifiName = findViewById(R.id.wifi_name);
    }

    private void clickListeners() {

        findViewById(R.id.wif_container_layout)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                Intent intent = new Intent(SettingsMainActivity.this,WifiMainActivity.class);
                        startActivity(intent);
                    }
                });
        findViewById(R.id.bluetooth_container_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComponentName cn = new ComponentName("com.android.settings",
                        "com.android.settings.bluetooth.BluetoothSettings");

                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.setComponent(cn);
                startActivity(intent);
            }
        });

        findViewById(R.id.brightness_container_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean permission = permissionModify(SecureSettingsMain.this);
                if (permission) {
                    int width = (int) (Resources.getSystem().getDisplayMetrics().widthPixels * 0.90);
                    LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                    ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.dialog_brightness, null);

                    popupWindow = new PopupWindow(container, width, (int) pxFromDp(SecureSettingsMain.this, 60), true);
                    popupWindow.showAtLocation(findViewById(R.id.linearLayout), Gravity.CENTER_HORIZONTAL, 0, -400);
                    SeekBar seekBar = container.findViewById(R.id.seek_bar);

                    int brightness = getScreenBrightness(SecureSettingsMain.this);
                    seekBar.setProgress(brightness);
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            setScreenBrightness(SecureSettingsMain.this, progress);
                            brightnessLevel.setText((int) (((float) getScreenBrightness(SecureSettingsMain.this) / 255) * 100) + "%");
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });

                }


            }
        });

        findViewById(R.id.sleep_cotainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SleepDialog sleepDialog = new SleepDialog(SecureSettingsMain.this);
                sleepDialog.show();
            }
        });

        findViewById(R.id.sim_cotainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
//                startActivity(intent);

                Intent intent = new Intent("com.android.settings.sim.SIM_SUB_INFO_SETTINGS");
                startActivity(intent);
            }
        });

        findViewById(R.id.hotspot_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_MAIN, null);
//                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
                intent.setComponent(cn);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);


            }
        });

        findViewById(R.id.data_roaming_cotainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Settings.Global.putInt(getContentResolver(), Settings.Global.DATA_ROAMING, 1);
            }
        });
        findViewById(R.id.screen_lock_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        bluetoothLabel.setText(getBlueToothStatus());
        brightnessLevel.setText((int) (((float) getScreenBrightness(this) / 255) * 100) + "%");
        sleepTime.setText("After " + secondsToMintues(getSleepTime(SecureSettingsMain.this)) + " of inactivity");
        wifiName.setText(getWifiStatus(this));
    }

    @Override
    public void brightnessChanged() {
        brightnessLevel.setText((int) (((float) getScreenBrightness(this) / 255) * 100) + "%");
    }

    @Override
    public void sleepTimeChanged(String time) {
        sleepTime.setText(time);
    }
}
