package com.screenlocker.secure.settings.codeSetting.systemControls;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.settings.codeSetting.CodeSettingActivity;
import com.screenlocker.secure.settings.codeSetting.secureSettings.SecureSettingsActivity;
import com.screenlocker.secure.socket.interfaces.ChangeSettings;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.LifecycleReceiver;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Utils;
import com.screenlocker.secure.utils.WifiApControl;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import timber.log.Timber;

import static android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_DEVICE;
import static android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME;
import static com.screenlocker.secure.utils.AppConstants.BROADCAST_APPS_ACTION;
import static com.screenlocker.secure.utils.AppConstants.CODE_WRITE_SETTINGS_PERMISSION;
import static com.screenlocker.secure.utils.AppConstants.KEY_ALLOW_SCREENSHOT;
import static com.screenlocker.secure.utils.AppConstants.KEY_DATABASE_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.RESULT_ENABLE;
import static com.screenlocker.secure.utils.AppConstants.SECURE_SETTINGS_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.SETTINGS_CHANGE;
import static com.screenlocker.secure.utils.LifecycleReceiver.BACKGROUND;
import static com.screenlocker.secure.utils.LifecycleReceiver.FOREGROUND;
import static com.screenlocker.secure.utils.LifecycleReceiver.LIFECYCLE_ACTION;
import static com.screenlocker.secure.utils.LifecycleReceiver.STATE;
import static com.screenlocker.secure.utils.Utils.micOff;
import static com.screenlocker.secure.utils.Utils.startNfcSettingsActivity;


public class SystemPermissionActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    //  WifiAPReceiver mReciever;
    private static final int REQUEST_CODE_LOCATION_GPS = 7;
    private LocationManager locationManager;
    private Switch switchDisable, switchCamera, switchMic, switchSpeaker, switchFileSharing;
    private Switch switchGuest, switchWifi,
            switchBluetooth, switchHotSpot, switchLocation,
            switchEncrypt, switchScreenShot, switchNFC;
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
    private ConstraintLayout containerLayout;

    DevicePolicyManager mDPM;
    private ComponentName compName;
    private ImageView appImage;
    private LockScreenService mService;
    private boolean mBound = false;
    static final int REQUEST_PROVISION_MANAGED_PROFILE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_controls);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.system_controls));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ResolveInfo settingResolveInfo = querySettingPkgName();
        PackageManager packageManager = getPackageManager();
        mDPM = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);

        switchDisable = findViewById(R.id.switchDisable);
        switchCamera = findViewById(R.id.switchCamera);
        if (mDPM.getCameraDisabled(compName)) {
            switchCamera.setChecked(true);
        } else
            switchCamera.setChecked(false);
        switchSpeaker = findViewById(R.id.switchSpeaker);
        switchMic = findViewById(R.id.switchMic);
        switchFileSharing = findViewById(R.id.switchFileSharing);
        switchGuest = findViewById(R.id.switchGuest);
        switchEncrypt = findViewById(R.id.switchEncrypt);
        switchWifi = findViewById(R.id.switchWifi);
        switchBluetooth = findViewById(R.id.switchBluetooth);
        switchHotSpot = findViewById(R.id.switchHotSpot);
        switchLocation = findViewById(R.id.switchLocation);
        switchScreenShot = findViewById(R.id.switchScreenShot);
        switchBlockCall = findViewById(R.id.switchBlockCall);
        switchNFC = findViewById(R.id.switchNFC);
        containerLayout = findViewById(R.id.container_layout);
        appImage = findViewById(R.id.appImage);
        switchWifi.setOnCheckedChangeListener(this);
        switchBluetooth.setOnCheckedChangeListener(this);
        switchHotSpot.setOnCheckedChangeListener(this);
        switchBlockCall.setOnCheckedChangeListener(this);
        switchCamera.setOnCheckedChangeListener(this);
        switchSpeaker.setOnCheckedChangeListener(this);
        switchFileSharing.setOnCheckedChangeListener(this);
        switchLocation.setOnClickListener(this);
        switchScreenShot.setOnClickListener(this);
        switchNFC.setOnClickListener(this);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (settingResolveInfo != null) {
            settingPrimaryKey = settingResolveInfo.activityInfo.packageName;
        }


        setDefaultState();
    }

    public void youDesirePermissionCode(AppCompatActivity context, boolean isChecked) {
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
        } else if (requestCode == REQUEST_PROVISION_MANAGED_PROFILE) {
            Log.d("", "onActivityResult: " + requestCode);
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


        boolean isChecked = PrefUtils.getBooleanPref(this, AppConstants.KEY_ENABLE_SCREENSHOT);

        //for screenshot
        if (isChecked) {
            switchScreenShot.setChecked(true);
        } else {
            switchScreenShot.setChecked(false);
        }

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

                appInfo = MyApplication.getAppDatabase(SystemPermissionActivity.this)
                        .getDao().getParticularApp(settingPrimaryKey);
                if (appInfo == null) {
                    MyApplication.getAppDatabase(SystemPermissionActivity.this).getDao().insertApps(appInfo);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switchGuest.setChecked(appInfo.isGuest());
                            switchEncrypt.setChecked(appInfo.isEncrypted());
                            switchDisable.setChecked(appInfo.isEnable());
                            Glide.with(SystemPermissionActivity.this).load(appInfo.getIcon()).into(appImage);
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

    @RequiresApi(api = Build.VERSION_CODES.M)
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
                        appInfo = MyApplication.getAppDatabase(SystemPermissionActivity.this)
                                .getDao().getParticularApp(settingPrimaryKey);
                        appInfo.setGuest(switchGuest.isChecked());
                        appInfo.setEncrypted(switchEncrypt.isChecked());
                        appInfo.setEnable(switchDisable.isChecked());
                    } else {
                        appInfo.setGuest(switchGuest.isChecked());
                        appInfo.setEncrypted(switchEncrypt.isChecked());
                        appInfo.setEnable(switchDisable.isChecked());
                    }
//                    MyApplication.getAppDatabase(SystemPermissionActivity.this).getDao().updateApps(appInfo);
                    Intent intent = new Intent(BROADCAST_APPS_ACTION);
                    intent.putExtra(KEY_DATABASE_CHANGE, "settings");
                    LocalBroadcastManager.getInstance(SystemPermissionActivity.this).sendBroadcast(intent);
                    PrefUtils.saveBooleanPref(SystemPermissionActivity.this, SETTINGS_CHANGE, true);


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

        unbindService(connection);
        mBound = false;

    }

    private ResolveInfo querySettingPkgName() {
        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfos == null || resolveInfos.size() == 0) {
            return null;
        }

        return resolveInfos.get(0);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
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
            case R.id.switchCamera:
                try {

                    mDPM.setCameraDisabled(compName, isChecked);
                } catch (SecurityException e) {
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why we need this permission");
                    startActivityForResult(intent, RESULT_ENABLE);
                }
                break;
            case R.id.switchSpeaker:
                //mDPM.setMasterVolumeMuted(compName, isChecked);
                if (isChecked) {
                    Utils.speaker(this);
                } else {
                    Utils.speakerOff(this);
                }
                break;
            case R.id.switchFileSharing:
                //mDPM.setScreenCaptureDisabled(compName, isChecked);
                break;
            case R.id.switchMic:
                if (mDPM.isDeviceOwnerApp(getPackageName())) {
                    Timber.d("device owner: ");
                    mDPM.setMasterVolumeMuted(compName, isChecked);
                }
                //micOff(this);
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
//                String isChecked = PrefUtils.getStringPref(this, AppConstants.KEY_ENABLE_SCREENSHOT);
//                if (isChecked.equals(AppConstants.VALUE_SCREENSHOT_ENABLE)) {
//                    switchScreenShot.setChecked(true);
////                    disableScreenShotBlocker(true);
//                } else if (isChecked.equals(AppConstants.VALUE_SCREENSHOT_DISABLE)) {
//                    switchScreenShot.setChecked(false);
////                    enableScreenShotBlocker(true);
//                }

                if (switchScreenShot.isChecked()) {
                    if (mService != null) {
                        mService.startCapture();
                        PrefUtils.saveBooleanPref(this, AppConstants.KEY_ENABLE_SCREENSHOT, true);

                    }
                } else {
                    if (mService != null) {
                        mService.stopCapture();
                        PrefUtils.saveBooleanPref(this, AppConstants.KEY_ENABLE_SCREENSHOT, false);

                    }


                }

                break;
            case R.id.switchNFC:
                startNfcSettingsActivity(this);
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
            containerLayout.setVisibility(View.INVISIBLE);
            try {
                this.finish();
                if (CodeSettingActivity.codeSettingsInstance != null) {

                    //  finish previous activity and this activity
                    CodeSettingActivity.codeSettingsInstance.finish();
                }
            } catch (Exception ignored) {
            }
        }


        PrefUtils.saveBooleanPref(SystemPermissionActivity.this, SETTINGS_CHANGE, true);


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

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, LockScreenService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }


    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LockScreenService.LocalBinder binder = (LockScreenService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


}
