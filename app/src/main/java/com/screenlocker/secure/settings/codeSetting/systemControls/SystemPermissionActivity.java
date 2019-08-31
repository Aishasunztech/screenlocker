package com.screenlocker.secure.settings.codeSetting.systemControls;

import android.app.admin.DeviceAdminInfo;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.socket.model.Settings;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;


import static android.os.UserManager.DISALLOW_CONFIG_TETHERING;
import static android.os.UserManager.DISALLOW_UNMUTE_MICROPHONE;
import static com.screenlocker.secure.utils.AppConstants.BROADCAST_APPS_ACTION;
import static com.screenlocker.secure.utils.AppConstants.KEY_DATABASE_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.RESULT_ENABLE;
import static com.screenlocker.secure.utils.AppConstants.SETTINGS_CHANGE;


public class SystemPermissionActivity extends BaseActivity implements PermissionSateChangeListener {

    //  WifiAPReceiver mReciever;
    private RecyclerView recyclerView;
    private boolean isSettingsChanged = false;
    private SystemPermissionAdaptor adaptor;
    private List<Settings> settings = new ArrayList<>();
    private WifiManager wifiManager;
    private BluetoothAdapter mBluetoothAdapter;

    private DevicePolicyManager mDPM;
    private ComponentName compName;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_controls);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.system_controls));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //RecyclerView
        recyclerView = findViewById(R.id.sys_perm_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(SystemPermissionActivity.this));
        adaptor = new SystemPermissionAdaptor(settings, this);
        recyclerView.setAdapter(adaptor);

        mDPM = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        AppExecutor.getInstance().getSingleThreadExecutor().submit(() -> {
            settings = MyApplication.getAppDatabase(SystemPermissionActivity.this).getDao().getSettings();
            AppExecutor.getInstance().getMainThread().execute(() -> adaptor.notifyDataSetChanged());

        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (isSettingsChanged) {
            Intent intent = new Intent(BROADCAST_APPS_ACTION);
            intent.putExtra(KEY_DATABASE_CHANGE, "settings");
            LocalBroadcastManager.getInstance(SystemPermissionActivity.this).sendBroadcast(intent);
            PrefUtils.saveBooleanPref(SystemPermissionActivity.this, SETTINGS_CHANGE, true);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);

    }


    @Override
    public void OnPermisionChangeListener(Settings setting, boolean isChecked) {
        switch (setting.getSetting_name()) {
            case AppConstants.SET_WIFI:
                wifiManager.setWifiEnabled(isChecked);
                break;
            case AppConstants.SET_BLUETOOTH:
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
            case AppConstants.SET_BLUE_FILE_SHARING:
                if (mDPM.isDeviceOwnerApp(getPackageName())) {
                    mDPM.setBluetoothContactSharingDisabled(compName, !isChecked);
                } else {
                    Toast.makeText(this, "Setting not available.", Toast.LENGTH_SHORT).show();
                }

                break;
            case AppConstants.SET_CALLS:
                PrefUtils.saveBooleanPref(this, AppConstants.KEY_DISABLE_CALLS, isChecked);
                break;
            case AppConstants.SET_CAM:
                try {
                    if (mDPM.hasGrantedPolicy(compName, DeviceAdminInfo.USES_POLICY_DISABLE_CAMERA)) {
                        mDPM.setCameraDisabled(compName, !isChecked);
                    } else {
                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "We need this permission to go in GOD mode.");
                        startActivityForResult(intent, RESULT_ENABLE);
                    }

                } catch (SecurityException e) {
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "We need this permission to go in GOD mode.");
                    startActivityForResult(intent, RESULT_ENABLE);
                }
                break;
            case AppConstants.SET_HOTSPOT:
                if (isChecked) {
                    mDPM.clearUserRestriction(compName, DISALLOW_CONFIG_TETHERING);
                } else {
                    mDPM.addUserRestriction(compName, DISALLOW_CONFIG_TETHERING);
                }

                break;
            case AppConstants.SET_MIC:
                if (isChecked) {
                    mDPM.clearUserRestriction(compName, DISALLOW_UNMUTE_MICROPHONE);
                } else
                    mDPM.addUserRestriction(compName, DISALLOW_UNMUTE_MICROPHONE);
                break;
            case AppConstants.SET_SPEAKER:
                if (mDPM.isDeviceOwnerApp(getPackageName())) {
                    mDPM.setMasterVolumeMuted(compName, !isChecked);
                } else {
                    Toast.makeText(this, "Setting not available.", Toast.LENGTH_SHORT).show();
                }
                break;
            case AppConstants.SET_SS:
                if (mDPM.isDeviceOwnerApp(getPackageName())) {
                    mDPM.setScreenCaptureDisabled(compName, !isChecked);
                } else {
                    Toast.makeText(this, "Setting not available.", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                return;


        }
        PrefUtils.saveBooleanPref(SystemPermissionActivity.this, SETTINGS_CHANGE, true);
        setting.setSetting_status(isChecked);
        isSettingsChanged = true;
        AppExecutor.getInstance().getSingleThreadExecutor().submit(() -> {
            MyApplication.getAppDatabase(SystemPermissionActivity.this).getDao().updateSetting(setting);
        });
    }
}
