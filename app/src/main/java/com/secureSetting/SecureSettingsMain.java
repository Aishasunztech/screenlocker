package com.secureSetting;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.secureSetting.BrightnessDialog;
import com.secureSetting.SleepDialog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.secureSetting.UtilityFunctions.checkLocationStatus;
import static com.secureSetting.UtilityFunctions.checkPermissions;
import static com.secureSetting.UtilityFunctions.getBlueToothStatus;
import static com.secureSetting.UtilityFunctions.getScreenBrightness;
import static com.secureSetting.UtilityFunctions.getSleepTime;
import static com.secureSetting.UtilityFunctions.getWifiStatus;
import static com.secureSetting.UtilityFunctions.permissionModify;
import static com.secureSetting.UtilityFunctions.pxFromDp;
import static com.secureSetting.UtilityFunctions.secondsToMintues;
import static com.secureSetting.UtilityFunctions.setScreenBrightness;
import static com.secureSetting.UtilityFunctions.turnOnLocation;

public class SecureSettingsMain extends AppCompatActivity implements BrightnessDialog.BrightnessChangeListener
        , SleepDialog.SleepChangerListener {

    private PopupWindow popupWindow;

    private TextView bluetoothName, brightnessLevel, sleepTime, wifiName;

    private LinearLayout wifiContainer, bluetoothContainer, simCardContainer, hotspotContainer, screenLockContainer, brightnessContainer, sleepContainer;

    private ConstraintLayout settingsLayout;

    HashMap<String, LinearLayout> extensions;


    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            finish();
        }

    };


    // showing allowed menu for each user type
    private void showMenus() {
        String userType = PrefUtils.getStringPref(this, CURRENT_KEY);

        switch (userType) {
            // encrypted user
            case KEY_MAIN_PASSWORD:

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<SubExtension> subExtensions = MyApplication.getAppDatabase(SecureSettingsMain.this).getDao().getEncryptedExtensions(AppConstants.SECURE_SETTINGS_UNIQUE, true);
                        if (subExtensions == null || subExtensions.size() == 0) {
                            settingsLayout.setVisibility(View.GONE);
                        } else {
                            for (SubExtension subExtension : subExtensions) {
                                String extensionName = subExtension.getUniqueExtension();
                                if (extensions.containsKey(extensionName)) {
                                    LinearLayout extension = extensions.get(extensionName);
                                    if (extension != null) {
                                        extension.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        }

                    }
                }).start();

                break;

            //guest user
            case KEY_GUEST_PASSWORD:
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        List<SubExtension> subExtensions = MyApplication.getAppDatabase(SecureSettingsMain.this).getDao().getGuestExtensions(AppConstants.SECURE_SETTINGS_UNIQUE, true);

                        if (subExtensions == null || subExtensions.size() == 0) {
                            settingsLayout.setVisibility(View.GONE);
                        } else {
                            for (SubExtension subExtension : subExtensions) {
                                String extensionName = subExtension.getUniqueExtension();
                                if (extensions.containsKey(extensionName)) {
                                    LinearLayout extension = extensions.get(extensionName);
                                    if (extension != null) {
                                        extension.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        }
                    }
                }).start();

                break;
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_main);
        setToolbar();
        setSetIds();
//        checkPermissions(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(AppConstants.BROADCAST_ACTION));


//        permissionModify(SecureSettingsMain.this);


//        if (!checkLocationStatus(this)) {
//            turnOnLocation(this);
//        }

        clickListeners();
        showMenus();

        extensions = new HashMap<>();
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "wi-fi", wifiContainer);
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "Bluetooth", bluetoothContainer);
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "SIM Cards", simCardContainer);
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "Hotspot", hotspotContainer);
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "Screen Lock", screenLockContainer);
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "Brightness", brightnessContainer);
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "Sleep", sleepContainer);


    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Secure Settings");
        }

    }

    private void setSetIds() {
        bluetoothName = findViewById(R.id.bluetooth_name);
        brightnessLevel = findViewById(R.id.brightness_lavel);
        sleepTime = findViewById(R.id.sleep_time);
        wifiName = findViewById(R.id.wifi_name);

        wifiContainer = findViewById(R.id.wif_container_layout);
        bluetoothContainer = findViewById(R.id.bluetooth_container_layout);
        simCardContainer = findViewById(R.id.sim_cotainer);
        hotspotContainer = findViewById(R.id.hotspot_container);
        screenLockContainer = findViewById(R.id.screen_lock_container);
        brightnessContainer = findViewById(R.id.brightness_container_layout);
        sleepContainer = findViewById(R.id.sleep_cotainer);
        settingsLayout = findViewById(R.id.settings_layout);

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
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();
        bluetoothName.setText(getBlueToothStatus());
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
