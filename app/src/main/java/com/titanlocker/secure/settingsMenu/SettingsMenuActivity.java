package com.titanlocker.secure.settingsMenu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.titanlocker.secure.R;
import com.titanlocker.secure.app.MyApplication;
import com.titanlocker.secure.base.BaseActivity;
import com.titanlocker.secure.launcher.AppInfo;
import com.titanlocker.secure.settings.codeSetting.CodeSettingActivity;
import com.titanlocker.secure.socket.interfaces.ChangeSettings;
import com.titanlocker.secure.utils.AppConstants;
import com.titanlocker.secure.utils.LifecycleReceiver;
import com.titanlocker.secure.utils.PrefUtils;
import com.titanlocker.secure.utils.WifiApControl;

import java.util.List;

import timber.log.Timber;

import static com.titanlocker.secure.utils.AppConstants.CODE_WRITE_SETTINGS_PERMISSION;
import static com.titanlocker.secure.utils.AppConstants.SETTINGS_CHANGE;
import static com.titanlocker.secure.utils.LifecycleReceiver.BACKGROUND;
import static com.titanlocker.secure.utils.LifecycleReceiver.FOREGROUND;
import static com.titanlocker.secure.utils.LifecycleReceiver.LIFECYCLE_ACTION;
import static com.titanlocker.secure.utils.LifecycleReceiver.STATE;


public class SettingsMenuActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    //  WifiAPReceiver mReciever;
    private static final int REQUEST_CODE_LOCATION_GPS = 7;
    private LocationManager locationManager;
    private Switch switchDisable;
    private Switch switchGuest, switchWifi, switchBluetooth, switchHotSpot, switchLocation, switchEncrypt, switchScreenShot;
    private WifiManager wifiManager;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean openHotSpot;
    boolean permission;
    boolean isHotSpotEnabled;
    AppInfo appInfo;
    private String settingPrimaryKey;
    private boolean isBackPressed;
    private Switch switchBlockCall;
    private boolean isSwitchLocationClicked;


    private static ChangeSettings listener;

    public void setListener(ChangeSettings settingsChangeListener) {
        listener = settingsChangeListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_menu);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings App");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ResolveInfo settingResolveInfo = querySettingPkgName();

        PackageManager packageManager = getPackageManager();
        switchDisable = findViewById(R.id.switchDisable);
        switchGuest = findViewById(R.id.switchGuest);
        switchEncrypt = findViewById(R.id.switchEncrypt);
        switchWifi = findViewById(R.id.switchWifi);
        switchBluetooth = findViewById(R.id.switchBluetooth);
        switchHotSpot = findViewById(R.id.switchHotSpot);
        switchLocation = findViewById(R.id.switchLocation);
        switchScreenShot = findViewById(R.id.switchScreenShot);
        switchBlockCall = findViewById(R.id.switchBlockCall);
        switchWifi.setOnCheckedChangeListener(this);
        switchBluetooth.setOnCheckedChangeListener(this);
        switchHotSpot.setOnCheckedChangeListener(this);
        switchBlockCall.setOnCheckedChangeListener(this);
        switchLocation.setOnClickListener(this);
        switchScreenShot.setOnClickListener(this);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        settingPrimaryKey = settingResolveInfo.activityInfo.packageName + settingResolveInfo.loadLabel(packageManager);


        setDefaultState();
    }

    public void youDesirePermissionCode(Activity context, boolean isChecked) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(context);
        } else {
            permission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        }
        if (permission) {
            WifiApControl.turnOnOffHotspot(isChecked, wifiManager);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivityForResult(intent, CODE_WRITE_SETTINGS_PERMISSION);
            } else {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_SETTINGS}, CODE_WRITE_SETTINGS_PERMISSION);
            }
        }
    }


    @SuppressLint("NewApi")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_WRITE_SETTINGS_PERMISSION) {
            if (Settings.System.canWrite(this)) {
                Timber.tag("TAG").d("MdmMainActivity.CODE_WRITE_SETTINGS_PERMISSION success");
                //do your code
                WifiApControl.turnOnOffHotspot(openHotSpot, wifiManager);
                Toast.makeText(this, "onactivityresult", Toast.LENGTH_SHORT).show();
            } else {
                isHotSpotEnabled = WifiApControl.isWifiApEnabled(wifiManager);
                if (isHotSpotEnabled) {
                    switchHotSpot.setChecked(true);
                } else {
                    switchHotSpot.setChecked(false);
                }
            }

        } else if (requestCode == REQUEST_CODE_LOCATION_GPS) {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            ) {
                switchLocation.setChecked(true);
            } else {
                switchLocation.setChecked(false);
            }
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODE_WRITE_SETTINGS_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //do your code
            WifiApControl.turnOnOffHotspot(openHotSpot, wifiManager);
            Toast.makeText(this, "onRequestPermissionsResult", Toast.LENGTH_SHORT).show();

        }
    }

    private void setDefaultState() {
        //for wifi
        switchWifi.setChecked(wifiManager.isWifiEnabled());
        //for bluetooth
        try {
            switchBluetooth.setChecked(mBluetoothAdapter.isEnabled());
        } catch (Exception ignored) {
        }

        String isChecked = PrefUtils.getStringPref(this, AppConstants.KEY_ENABLE_SCREENSHOT);

        //for screenshot
//        if (isChecked.equals(AppConstants.VALUE_SCREENSHOT_ENABLE)) {
//            switchScreenShot.setChecked(false);
//        } else if (isChecked.equals(AppConstants.VALUE_SCREENSHOT_DISABLE)) {
//            switchScreenShot.setChecked(true);
//        }

        //for hotspot
        isHotSpotEnabled = WifiApControl.isWifiApEnabled(wifiManager);


        switchHotSpot.setChecked(isHotSpotEnabled);

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            switchLocation.setChecked(true);
        } else {
            switchLocation.setChecked(false);
        }
        switchBlockCall.setChecked(PrefUtils.getBooleanPref(this, AppConstants.KEY_DISABLE_CALLS));

        new Thread() {
            @Override
            public void run() {

                appInfo = MyApplication.getAppDatabase(SettingsMenuActivity.this)
                        .getDao().getParticularApp(settingPrimaryKey);
                if (appInfo == null) {
                    MyApplication.getAppDatabase(SettingsMenuActivity.this).getDao().insertApps(appInfo);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//TODO if it exist in db then updatte the ui

                            switchGuest.setChecked(appInfo.isGuest());
                            switchEncrypt.setChecked(appInfo.isEncrypted());
                            switchDisable.setChecked(appInfo.isEnable());

                        }
                    });
                }
            }
        }.start();


    }

    @Override
    public void onStateChange(int state) {            //<---
        switch (state) {
            case LifecycleReceiver.FOREGROUND:
                Timber.e("onStateChange: FOREGROUND");
                break;

            case LifecycleReceiver.BACKGROUND:
                Timber.tag("TAGLLLL").e("onStateChange: BACKGROUND");
                if (CodeSettingActivity.codeSettingsInstance != null) {
                    CodeSettingActivity.codeSettingsInstance.finish();
                    this.finish();
                }
                break;

            default:
                Timber.e("onStateChange: SOMETHING");
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isBackPressed = false;
        isSwitchLocationClicked = false;
        Intent intent = new Intent(LIFECYCLE_ACTION);
        intent.putExtra(STATE, FOREGROUND);
        sendBroadcast(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        new Thread() {
            @Override
            public void run() {
                try {
                    if (appInfo == null) {
                        appInfo = MyApplication.getAppDatabase(SettingsMenuActivity.this)
                                .getDao().getParticularApp(settingPrimaryKey);
                        appInfo.setGuest(switchGuest.isChecked());
                        appInfo.setEncrypted(switchEncrypt.isChecked());
                        appInfo.setEnable(switchDisable.isChecked());
                    } else {
                        appInfo.setGuest(switchGuest.isChecked());
                        appInfo.setEncrypted(switchEncrypt.isChecked());
                        appInfo.setEnable(switchDisable.isChecked());
                    }
                    MyApplication.getAppDatabase(SettingsMenuActivity.this).getDao().updateApps(appInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!isBackPressed && !isSwitchLocationClicked) {
                    Intent intent = new Intent(LIFECYCLE_ACTION);
                    intent.putExtra(STATE, BACKGROUND);
                    sendBroadcast(intent);

                }
            }
        }.start();

    }

    private ResolveInfo querySettingPkgName() {
        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfos == null || resolveInfos.size() == 0) {
            return null;
        }

        return resolveInfos.get(0);
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        switch (compoundButton.getId()) {
            case R.id.switchWifi:
                wifiManager.setWifiEnabled(isChecked);
                break;
            case R.id.switchBluetooth:
                if (isChecked) {
                    if (!mBluetoothAdapter.isEnabled()) {
                        //if bluetooth is not enabled enabled it
                        mBluetoothAdapter.enable();
                    }
                } else {
                    //disable bluetooth
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.disable();
                    }
                }

                break;
            case R.id.switchHotSpot:
                openHotSpot = isChecked;
                if (isChecked) {
                    if (!WifiApControl.isWifiApEnabled(wifiManager))
                        youDesirePermissionCode(this, true);
                } else {
                    if (WifiApControl.isWifiApEnabled(wifiManager)) {
                        youDesirePermissionCode(this, false);
                    }

                }

                break;
            case R.id.switchBlockCall:
                PrefUtils.saveBooleanPref(this, AppConstants.KEY_DISABLE_CALLS, isChecked);

                break;

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.switchLocation:
                isSwitchLocationClicked = true;
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(myIntent, REQUEST_CODE_LOCATION_GPS);
                break;
            case R.id.switchScreenShot:
                String isChecked = PrefUtils.getStringPref(this, AppConstants.KEY_ENABLE_SCREENSHOT);
                if (isChecked.equals(AppConstants.VALUE_SCREENSHOT_ENABLE)) {
                    switchScreenShot.setChecked(true);
//                    disableScreenShotBlocker(true);
                } else if (isChecked.equals(AppConstants.VALUE_SCREENSHOT_DISABLE)) {
                    switchScreenShot.setChecked(false);
//                    enableScreenShotBlocker(true);
                }

                break;
        }

    }


    public class WifiAPReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == "android.net.wifi.WIFI_AP_STATE_CHANGED") {
                int apState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                if (WifiManager.WIFI_STATE_ENABLED == apState % 10) {
                    // Hotspot AP is enabled
                    switchHotSpot.setChecked(true);
                } else {
                    switchHotSpot.setChecked(false);
                    // Hotspot AP is disabled/not ready
                }
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (!isBackPressed && !isSwitchLocationClicked) {
            try {
                if (CodeSettingActivity.codeSettingsInstance != null) {
                    this.finish();
                    //  finish previous activity and this activity
                    CodeSettingActivity.codeSettingsInstance.finish();
                }
            } catch (Exception ignored) {
            }
        }

        if (listener != null) {
            listener.onSettingsChanged();
        }

        PrefUtils.saveBooleanPref(SettingsMenuActivity.this, SETTINGS_CHANGE, true);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
    }
}
