package com.secureSetting;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.secure.launcher.R;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.List;
import java.util.WeakHashMap;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.IS_SETTINGS_ALLOW;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.secureSetting.UtilityFunctions.getBlueToothStatus;
import static com.secureSetting.UtilityFunctions.getScreenBrightness;
import static com.secureSetting.UtilityFunctions.getSleepTime;
import static com.secureSetting.UtilityFunctions.getWifiStatus;
import static com.secureSetting.UtilityFunctions.permissionModify;
import static com.secureSetting.UtilityFunctions.pxFromDp;
import static com.secureSetting.UtilityFunctions.secondsToMintues;
import static com.secureSetting.UtilityFunctions.setScreenBrightness;
@Deprecated
public class SecureSettingsMain extends BaseActivity implements BrightnessDialog.BrightnessChangeListener, CompoundButton.OnCheckedChangeListener
        , SleepDialog.SleepChangerListener {

    private PopupWindow popupWindow;

    private TextView bluetoothName, brightnessLevel, sleepTime,
            wifiName, battery_status;


    private LinearLayout wifiContainer, bluetoothContainer, simCardContainer,
            hotspotContainer, screenLockContainer, brightnessContainer,
            sleepContainer, battery_container, sound_container,
            language_container, dateTimeContainer, mobile_container, dataRoamingContainer, notifications_container, no_settings_layout;

    private ConstraintLayout settingsLayout;

    WeakHashMap<String, LinearLayout> extensions;

    private Switch switch_airplane;

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            finish();
        }

    };

    FrameLayout mView;

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            String batteryStatus = "";
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                batteryStatus = "Charging";
            }
            battery_status.setText(String.valueOf(level) + "% " + batteryStatus);
        }
    };

    // showing allowed menu for each user type
    private void showMenus() {
        String userType = PrefUtils.getInstance(this).getStringPref( CURRENT_KEY);
//        if (userType != null) {
//            switch (userType) {
//                // encrypted user
//                case KEY_MAIN_PASSWORD:
//                    new Thread(() -> {
//                        List<SubExtension> subExtensions = MyAppDatabase.getInstance(SecureSettingsMain.this).getDao().getEncryptedExtensions(AppConstants.SECURE_SETTINGS_UNIQUE, true);
//                        if (subExtensions == null || subExtensions.size() == 0) {
//                            runOnUiThread(() -> {
//                                settingsLayout.setVisibility(View.GONE);
//                                no_settings_layout.setVisibility(View.VISIBLE);
//                            });
//                        } else {
//                            runOnUiThread(() -> no_settings_layout.setVisibility(View.GONE));
//                            for (SubExtension subExtension : subExtensions) {
//                                String extensionName = subExtension.getUniqueExtension();
//                                if (extensions.containsKey(extensionName)) {
//                                    LinearLayout extension = extensions.get(extensionName);
//                                    if (extension != null) {
//                                        runOnUiThread(() -> extension.setVisibility(View.VISIBLE));
//                                    }
//                                }
//                            }
//                        }
//
//                    }).start();
//
//                    break;
//
//                //guest user
//                case KEY_GUEST_PASSWORD:
//                    new Thread(() -> {
//
//                        List<SubExtension> subExtensions = MyAppDatabase.getInstance(SecureSettingsMain.this).getDao().getGuestExtensions(AppConstants.SECURE_SETTINGS_UNIQUE, true);
//
//                        if (subExtensions == null || subExtensions.size() == 0) {
//                            runOnUiThread(() -> {
//                                settingsLayout.setVisibility(View.GONE);
//                                no_settings_layout.setVisibility(View.VISIBLE);
//                            });
//                        } else {
//                            runOnUiThread(() -> no_settings_layout.setVisibility(View.GONE));
//                            for (SubExtension subExtension : subExtensions) {
//                                String extensionName = subExtension.getUniqueExtension();
//                                if (extensions.containsKey(extensionName)) {
//                                    LinearLayout extension = extensions.get(extensionName);
//                                    if (extension != null) {
//                                        runOnUiThread(() -> extension.setVisibility(View.VISIBLE));
//                                    }
//                                }
//                            }
//                        }
//                    }).start();
//
//                    break;
//            }
//        }

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

        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));


//        permissionModify(SecureSettingsMain.this)

//        if (!checkLocationStatus(this)) {
//            turnOnLocation(this);
//        }

        extensions = new WeakHashMap<>();

        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "wi-fi", wifiContainer);
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "Bluetooth", bluetoothContainer);
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "SIM Cards", simCardContainer);
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "Hotspot", hotspotContainer);
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "Finger Print + Lock", screenLockContainer);
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "Brightness", brightnessContainer);
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "Sleep", sleepContainer);
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "Battery", battery_container);
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "Sound", sound_container);
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "Date & Time", dateTimeContainer);
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "Data Roaming", dataRoamingContainer);
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "Mobile Data", mobile_container);
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "Languages & Input", language_container);
        extensions.put(AppConstants.SECURE_SETTINGS_UNIQUE + "Notifications", notifications_container);

        clickListeners();

    }


    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.secure_settings_activity_title));
        }

    }

    private Switch switch_mobile_data;

    private void setSetIds() {
        bluetoothName = findViewById(R.id.bluetooth_name);
        brightnessLevel = findViewById(R.id.brightness_lavel);
        sleepTime = findViewById(R.id.sleep_time);
        wifiName = findViewById(R.id.wifi_name);
        no_settings_layout = findViewById(R.id.no_settings_layout);
        battery_status = findViewById(R.id.battery_status);
        wifiContainer = findViewById(R.id.wif_container_layout);
        bluetoothContainer = findViewById(R.id.bluetooth_container_layout);
        simCardContainer = findViewById(R.id.sim_cotainer);
        hotspotContainer = findViewById(R.id.hotspot_container);
        screenLockContainer = findViewById(R.id.screen_lock_container);
        brightnessContainer = findViewById(R.id.brightness_container_layout);
        sleepContainer = findViewById(R.id.sleep_cotainer);
        battery_container = findViewById(R.id.battery_cotainer);
        sound_container = findViewById(R.id.sound_container);
        language_container = findViewById(R.id.language_container);
        dateTimeContainer = findViewById(R.id.dateTime_container);
        mobile_container = findViewById(R.id.mobile_data_cotainer);
        dataRoamingContainer = findViewById(R.id.data_roaming_cotainer);
        settingsLayout = findViewById(R.id.settings_layout);
        switch_mobile_data = findViewById(R.id.switch_mobile_data);
        notifications_container = findViewById(R.id.notification_container);
        no_settings_layout = findViewById(R.id.no_settings_layout);
        switch_mobile_data.setOnCheckedChangeListener(this);
        mView = new FrameLayout(this);
//        switch_airplane = findViewById(R.id.switch_air);
//        switch_airplane.setOnCheckedChangeListener(this);
    }

    WindowManager wm;


    private void clickListeners() {

        findViewById(R.id.wif_container_layout)
                .setOnClickListener(v -> {
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                Intent intent = new Intent(SettingsMainActivity.this,WifiMainActivity.class);
                    startActivity(intent);
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

        findViewById(R.id.brightness_container_layout).setOnClickListener(v -> {
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


        });

        findViewById(R.id.sleep_cotainer).setOnClickListener(v -> {
            SleepDialog sleepDialog = new SleepDialog(SecureSettingsMain.this);
            sleepDialog.show();
        });

        findViewById(R.id.sim_cotainer).setOnClickListener(v -> {

            Intent intent = new Intent("com.android.settings.sim.SIM_SUB_INFO_SETTINGS");
            startActivity(intent);
        });

        findViewById(R.id.hotspot_container).setOnClickListener(v -> {
            final Intent intent = new Intent(Intent.ACTION_MAIN, null);
            ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
            intent.setComponent(cn);
            startActivity(intent);


        });

        dataRoamingContainer.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
            startActivity(intent);
        });
        findViewById(R.id.screen_lock_container).setOnClickListener(v -> {
            Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
            startActivity(intent);
        });
        battery_container.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
            startActivityForResult(intent, 3);

            getOverLayLayoutParams();
//            createLayoutParams();


        });


        sound_container.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_SOUND_SETTINGS);
            startActivity(intent);
        });
        language_container.setOnClickListener(v -> {
            final Intent intent = new Intent(Intent.ACTION_MAIN, null);
            ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.Settings$LanguageAndInputSettingsActivity");
            intent.setComponent(cn);
            startActivity(intent);

        });
        dateTimeContainer.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_DATE_SETTINGS);
            startActivity(intent);
        });

        mobile_container.setOnClickListener(v -> {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.MODIFY_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                Timber.e("Permission granted");
            } else {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                startActivity(intent);
            }


        });

        notifications_container.setOnClickListener(v -> startActivity(new Intent(SecureSettingsMain.this, AllNotificationActivity.class)));


    }

    private void getOverLayLayoutParams() {

        if (localLayoutParams == null) {
            localLayoutParams = new WindowManager.LayoutParams();
        }
        createLayoutParams();
    }

    private void createLayoutParams() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

        } else {

            localLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        localLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
// this is to enable the notification to recieve touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
// Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        localLayoutParams.y = (int) (24 * getResources().getDisplayMetrics().scaledDensity);

        localLayoutParams.width = (int) (56 * getResources().getDisplayMetrics().scaledDensity);
        localLayoutParams.height = (int) (56 * getResources().getDisplayMetrics().scaledDensity);

        localLayoutParams.format = PixelFormat.TRANSLUCENT;

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        mView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        wm.addView(mView, localLayoutParams);


    }


    public void setMobileDataState(boolean mobileDataEnabled) {
        TelephonyManager cm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cm.setDataEnabled(mobileDataEnabled);
        }
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mBatInfoReceiver);
        removeview();
        super.onDestroy();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        PrefUtils.getInstance(this).saveBooleanPref( IS_SETTINGS_ALLOW, true);
        AppConstants.TEMP_SETTINGS_ALLOWED = true;

        bluetoothName.setText(getBlueToothStatus(this));
        brightnessLevel.setText((int) (((float) getScreenBrightness(this) / 255) * 100) + "%");
//        sleepTime.setText("After " + secondsToMintues(getSleepTime(SecureSettingsMain.this),SecureSettingsMain.this) + " of inactivity");
        //sleepTime.setText(getResources().getString(R.string.inactivity_message, secondsToMintues(getSleepTime(SecureSettingsMain.this), SecureSettingsMain.this)));
        wifiName.setText(getWifiStatus(this));

        Intent intent = getIntent();
        if (intent != null) {
            String msg = intent.getStringExtra("show_default");
            if (msg != null && msg.equals("show_default")) {
                simCardContainer.setVisibility(View.VISIBLE);
                wifiContainer.setVisibility(View.VISIBLE);
                mobile_container.setVisibility(View.VISIBLE);
                dataRoamingContainer.setVisibility(View.VISIBLE);
                language_container.setVisibility(View.VISIBLE);
                no_settings_layout.setVisibility(View.GONE);
            } else {
                showMenus();
            }
        } else {
            showMenus();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.MODIFY_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {

            switch_mobile_data.setVisibility(View.VISIBLE);
            TelephonyManager cm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                TelephonyManager telMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

                int simStateMain = telMgr.getSimState(0);
                int simStateSecond = telMgr.getSimState(1);

                if (simStateMain == 5 || simStateSecond == 5) {
                    switch_mobile_data.setChecked(cm.isDataEnabled());
                } else {
                    // Toast.makeText(this, getResources().getString(R.string.list_is_empty), Toast.LENGTH_SHORT).show();
                    switch_mobile_data.setEnabled(false);
                }


            }

        } else {
            switch_mobile_data.setVisibility(View.GONE);
        }


//        battery_status.setText(getBatteryLevel(this) + " % " + getBatteryStatus());
        removeview();

    }

    private void removeview() {
        if (mView != null && mView.getWindowToken() != null) {
            if (wm != null) {
                wm.removeViewImmediate(mView);
            }
        }
    }


    @Override
    public void brightnessChanged() {
        brightnessLevel.setText((int) (((float) getScreenBrightness(this) / 255) * 100) + "%");
    }

    @Override
    public void sleepTimeChanged(String time) {
        sleepTime.setText(time);
    }


    private WindowManager.LayoutParams localLayoutParams;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 3) {
            if (wm != null && mView != null)
                wm.removeViewImmediate(mView);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switch_mobile_data:
                setMobileDataState(isChecked);
                break;

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
