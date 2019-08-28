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
import android.os.RecoverySystem;
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
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Utils;
import com.screenlocker.secure.utils.WifiApControl;

import java.io.IOException;
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

import static android.os.UserManager.DISALLOW_CONFIG_TETHERING;
import static android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES;
import static android.os.UserManager.DISALLOW_UNMUTE_MICROPHONE;
import static com.screenlocker.secure.utils.AppConstants.BROADCAST_APPS_ACTION;
import static com.screenlocker.secure.utils.AppConstants.CODE_WRITE_SETTINGS_PERMISSION;
import static com.screenlocker.secure.utils.AppConstants.KEY_DATABASE_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.RESULT_ENABLE;
import static com.screenlocker.secure.utils.AppConstants.SETTINGS_CHANGE;
import static com.screenlocker.secure.utils.LifecycleReceiver.BACKGROUND;
import static com.screenlocker.secure.utils.LifecycleReceiver.FOREGROUND;
import static com.screenlocker.secure.utils.LifecycleReceiver.LIFECYCLE_ACTION;
import static com.screenlocker.secure.utils.LifecycleReceiver.STATE;
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
    static final int REQUEST_PROVISION_MANAGED_PROFILE = 1;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_controls);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.system_controls));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ResolveInfo settingResolveInfo = querySettingPkgName();
        mDPM = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);
        switchCamera = findViewById(R.id.switchCamera);
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
        switchWifi.setOnCheckedChangeListener(this);
        switchBluetooth.setOnCheckedChangeListener(this);
        switchHotSpot.setOnCheckedChangeListener(this);
        switchBlockCall.setOnCheckedChangeListener(this);
        switchCamera.setOnCheckedChangeListener(this);
        switchSpeaker.setOnCheckedChangeListener(this);
        switchMic.setOnCheckedChangeListener(this);
        switchFileSharing.setOnCheckedChangeListener(this);
        switchLocation.setOnClickListener(this);
        switchScreenShot.setOnCheckedChangeListener(this);
        switchNFC.setOnClickListener(this);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (settingResolveInfo != null) {
            settingPrimaryKey = settingResolveInfo.activityInfo.packageName;
        }


        setDefaultState();
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
        //switch file sharing
        if (mDPM.isDeviceOwnerApp(getPackageName())) {
            switchFileSharing.setChecked(!mDPM.getBluetoothContactSharingDisabled(compName));
        } else {
            switchFileSharing.setChecked(false);
        }

        //switch mic
        Bundle bundle1 = mDPM.getUserRestrictions(compName);
        if (!bundle1.getBoolean(DISALLOW_UNMUTE_MICROPHONE)) {
            switchMic.setChecked(true);
        } else {
            switchMic.setChecked(false);
        }

        //switch speaker
        if (mDPM.isDeviceOwnerApp(getPackageName())) {

            switchSpeaker.setChecked(!mDPM.isMasterVolumeMuted(compName));

        } else {
            switchSpeaker.setChecked(false);
        }

        //for camera
        if (!mDPM.getCameraDisabled(compName)) {
            switchCamera.setChecked(true);
        } else
            switchCamera.setChecked(false);


        //for wifi
        switchWifi.setChecked(wifiManager.isWifiEnabled());
        //for bluetooth
        try {
            switchBluetooth.setChecked(mBluetoothAdapter.isEnabled());
        } catch (Exception ignored) {
        }


        //for screenshot
        if (mDPM.isDeviceOwnerApp(getPackageName())) {
            if (mDPM.getScreenCaptureDisabled(compName))
                switchScreenShot.setChecked(false);
            else
                switchScreenShot.setChecked(true);
        } else {
            switchScreenShot.setChecked(true);
        }

        //for hotspot
        Bundle bundle = mDPM.getUserRestrictions(compName);
        if (bundle.getBoolean(DISALLOW_CONFIG_TETHERING)) {
            switchHotSpot.setChecked(false);
        } else {
            switchHotSpot.setChecked(false);
        }


        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            switchLocation.setChecked(true);
        } else {
            switchLocation.setChecked(false);
        }
        switchBlockCall.setChecked(PrefUtils.getBooleanPref(this, AppConstants.KEY_DISABLE_CALLS));


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
                if (isChecked) {
                    mDPM.clearUserRestriction(compName, DISALLOW_CONFIG_TETHERING);
                } else {
                    mDPM.addUserRestriction(compName, DISALLOW_CONFIG_TETHERING);
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
                if (mDPM.isDeviceOwnerApp(getPackageName())) {
                    mDPM.setMasterVolumeMuted(compName, !isChecked);
                } else {
                    Toast.makeText(this, "Setting not available.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.switchFileSharing:
                if (mDPM.isDeviceOwnerApp(getPackageName())) {
                    mDPM.setBluetoothContactSharingDisabled(compName, !isChecked);
                } else {
                    Toast.makeText(this, "Setting not available.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.switchMic:
                if (isChecked) {
                    mDPM.clearUserRestriction(compName, DISALLOW_UNMUTE_MICROPHONE);
                } else
                    mDPM.addUserRestriction(compName, DISALLOW_UNMUTE_MICROPHONE);
                break;
            case R.id.switchScreenShot:
                if (mDPM.isDeviceOwnerApp(getPackageName())) {
                    mDPM.setScreenCaptureDisabled(compName, !isChecked);
                } else {
                    Toast.makeText(this, "Setting not available.", Toast.LENGTH_SHORT).show();
                }

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
            case R.id.switchNFC:
                startNfcSettingsActivity(this);
                try {
//                    RecoverySystem.rebootWipeCache(this);
                    RecoverySystem.rebootWipeUserData(this);
                } catch (IOException e) {
                    e.printStackTrace();
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


}
