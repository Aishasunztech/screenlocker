package com.screenlocker.secure.launcher.subsettings;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.secure.launcher.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.IS_SETTINGS_ALLOW;
import static com.secureSetting.UtilityFunctions.getBlueToothStatus;
import static com.secureSetting.UtilityFunctions.getWifiStatus;

public class ConnectionsSubSettings extends BaseActivity implements View.OnClickListener {


    @BindView(R.id.layout_hotspot)
    LinearLayout layout_hotspot;

    @BindView(R.id.connection_layout)
    LinearLayout connection_layout;

    @BindView(R.id.bluethooth_layout)
    LinearLayout bluethooth_layout;

    @BindView(R.id.layout_simcards)
    LinearLayout layout_simcards;

    @BindView(R.id.layout_mobiledata)
    LinearLayout layout_mobiledata;

    @BindView(R.id.layoutDataRoaming)
    LinearLayout layoutDataRoaming;

    @BindView(R.id.switch_mobile_data)
    TextView mobileDataSim;
    @BindView(R.id.tvConnectedWIF)
    TextView tvConnectedWIF;
    @BindView(R.id.tvConnectedBluetooth)
    TextView tvConnectedBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connections_sub_settings);
        ButterKnife.bind(this);

        setToolbar();
        setListeners();
        String userType = prefUtils.getStringPref( CURRENT_KEY);
        SSettingsViewModel settingsViewModel = ViewModelProviders.of(this).get(SSettingsViewModel.class);

        settingsViewModel.getSubExtensions().observe(this, subExtensions -> {
            if (userType.equals(AppConstants.KEY_MAIN_PASSWORD)) {
                setUpPermissionSettingsEncrypted(subExtensions);
            } else if (userType.equals(AppConstants.KEY_GUEST_PASSWORD)) {
                setUpPermissionSettingsGuest(subExtensions);
            }
        });

    }

    private void setToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.connections);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void setListeners() {
        layout_hotspot.setOnClickListener(this);
        connection_layout.setOnClickListener(this);
        bluethooth_layout.setOnClickListener(this);
        layout_mobiledata.setOnClickListener(this);
        layout_simcards.setOnClickListener(this);
        layoutDataRoaming.setOnClickListener(this);
//        mobileDataSim.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            setMobileDataState(isChecked);
//        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_hotspot: {
                final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
                intent.setComponent(cn);
                startActivity(intent);
                break;
            }
            case R.id.connection_layout:
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                break;
            case R.id.bluethooth_layout:
                ComponentName cn = new ComponentName("com.android.settings",
                        "com.android.settings.bluetooth.BluetoothSettings");
                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.setComponent(cn);
                startActivity(intent);
                break;
            case R.id.layout_simcards:
                startActivity(new Intent("com.android.settings.sim.SIM_SUB_INFO_SETTINGS"));
                break;
            case R.id.layout_mobiledata: {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.MODIFY_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                    Timber.e("Permission granted");
                } else {
                    Intent intent1 = new Intent();
                    intent1.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                    startActivity(intent1);
                }
                break;
            }
            case R.id.layoutDataRoaming: {
                startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                break;
            }


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppConstants.TEMP_SETTINGS_ALLOWED = true;
        tvConnectedWIF.setText(getWifiStatus(this));
        tvConnectedBluetooth.setText(getBlueToothStatus(this));
        String currentKey = prefUtils.getStringPref( CURRENT_KEY);

        if (currentKey != null && currentKey.equals(AppConstants.KEY_SUPPORT_PASSWORD)) {
//            tvManagePasswords.setVisibility(View.GONE);
            bluethooth_layout.setVisibility(View.GONE);
            layout_hotspot.setVisibility(View.GONE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.MODIFY_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {

            //mobileDataSim.setVisibility(View.VISIBLE);
            TelephonyManager cm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                TelephonyManager telMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

                int simStateMain = telMgr.getSimState(0);
                int simStateSecond = telMgr.getSimState(1);

//                if (simStateMain == 5 || simStateSecond == 5) {
//                    mobileDataSim.setChecked(cm.isDataEnabled());
//                } else {
//                    // Toast.makeText(this, getResources().getString(R.string.list_is_empty), Toast.LENGTH_SHORT).show();
//                    mobileDataSim.setEnabled(false);
//                }
            }

        } else {
//            mobileDataSim.setVisibility(View.GONE);
        }


    }

    void setUpPermissionSettingsEncrypted(List<SubExtension> settings) {
        for (SubExtension setting : settings) {
            Timber.d("setUpPermissionSettingsEncrypted: %s", setting.getUniqueExtension());
            switch (setting.getUniqueExtension()) {
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_WIFI:
                    if (setting.isEncrypted()) {
                        connection_layout.setVisibility(View.VISIBLE);
                    } else connection_layout.setVisibility(View.GONE);
                    break;
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_Hotspot:
                    if (setting.isEncrypted()) {
                        layout_hotspot.setVisibility(View.VISIBLE);
                    } else layout_hotspot.setVisibility(View.GONE);
                    break;
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_Bluetooth:
                    if (setting.isEncrypted()) {
                        bluethooth_layout.setVisibility(View.VISIBLE);
                    } else bluethooth_layout.setVisibility(View.GONE);
                    break;
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_SIM:
                    if (setting.isEncrypted()) {
                        layout_simcards.setVisibility(View.VISIBLE);
                    } else layout_simcards.setVisibility(View.GONE);
                    break;
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_MobileData:
                    if (setting.isEncrypted()) {
                        layout_mobiledata.setVisibility(View.VISIBLE);
                    } else layout_mobiledata.setVisibility(View.GONE);
                    break;
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_DataRoaming:
                    if (setting.isEncrypted()) {
                        layoutDataRoaming.setVisibility(View.VISIBLE);
                    } else layoutDataRoaming.setVisibility(View.GONE);
                    break;
            }
        }
    }

    void setUpPermissionSettingsGuest(List<SubExtension> settings) {
        for (SubExtension setting : settings) {
            Timber.d("setUpPermissionSettingsGuest: %s", setting.getUniqueExtension());
            switch (setting.getUniqueExtension()) {
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_WIFI:
                    if (setting.isGuest()) {
                        connection_layout.setVisibility(View.VISIBLE);
                    } else connection_layout.setVisibility(View.GONE);
                    break;
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_Hotspot:
                    if (setting.isGuest()) {
                        layout_hotspot.setVisibility(View.VISIBLE);
                    } else layout_hotspot.setVisibility(View.GONE);
                    break;
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_Bluetooth:
                    if (setting.isGuest()) {
                        bluethooth_layout.setVisibility(View.VISIBLE);
                    } else bluethooth_layout.setVisibility(View.GONE);
                    break;
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_SIM:
                    if (setting.isGuest()) {
                        layout_simcards.setVisibility(View.VISIBLE);
                    } else layout_simcards.setVisibility(View.GONE);
                    break;
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_MobileData:
                    if (setting.isGuest()) {
                        layout_mobiledata.setVisibility(View.VISIBLE);
                    } else layout_mobiledata.setVisibility(View.GONE);
                    break;
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_DataRoaming:
                    if (setting.isGuest()) {
                        layoutDataRoaming.setVisibility(View.VISIBLE);
                    } else layoutDataRoaming.setVisibility(View.GONE);
                    break;
            }
        }
    }
    public void setMobileDataState(boolean mobileDataEnabled) {
        TelephonyManager cm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cm.setDataEnabled(mobileDataEnabled);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        prefUtils.saveBooleanPref( IS_SETTINGS_ALLOW, false);
        AppConstants.TEMP_SETTINGS_ALLOWED = false;
    }
}
