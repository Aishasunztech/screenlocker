package com.secureSetting;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.screenlocker.secure.R;
import com.secureSetting.DisplaySettings.BrightnessDialog;
import com.secureSetting.DisplaySettings.SleepDialog;
import com.secureSetting.bluetooth.BluetoothMainActivity;
import com.secureSetting.wifisettings.WifiMainActivity;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static com.secureSetting.AppConstants.CODE_WRITE_SETTINGS_PERMISSION;
import static com.secureSetting.AppConstants.LOCATION_SETTINGS_CODE;
import static com.secureSetting.UtilityFunctions.checkLocationStatus;
import static com.secureSetting.UtilityFunctions.clearConnectedPreference;
import static com.secureSetting.UtilityFunctions.getScreenBrightness;
import static com.secureSetting.UtilityFunctions.getSleepTime;
import static com.secureSetting.UtilityFunctions.getWifiSecurity;
import static com.secureSetting.UtilityFunctions.isWifiConnected;
import static com.secureSetting.UtilityFunctions.permissionModify;
import static com.secureSetting.UtilityFunctions.pxFromDp;
import static com.secureSetting.UtilityFunctions.turnOnLocation;


public class SettingsMainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks
            , BrightnessDialog.BrightnessChangeListener
            , SleepDialog.SleepChangerListener {
    private static final int RC_PERMISSION = 123;
    private ConstraintLayout wifiContainer,brightnessContainer,
            bluetooth_container_layout,sleep_container,sim_card_container,hotspot_container;
    private TextView tvStatus,tvBrightnessLevel,bluetoothStatus,tvSleepTime;
    private Toolbar toolbar;
    private ConstraintLayout permissionNotGrantedLayout , settingsLayout;
    private PopupWindow popupWindow;
    private LinearLayout linearLayout;

    private WifiManager wifiManager;
    private BroadcastReceiver ConnectedNetworkReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
           NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
           if(networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
           {
               SharedPreferences connectedWifi = getSharedPreferences(getString(R.string.connectNetwork), MODE_PRIVATE);

               if(networkInfo.isConnected()) {

                   String savedBSSID = connectedWifi.getString("BSSID", "null");
                   WifiManager mWifiManger = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                   WifiInfo mWifiInfo = mWifiManger.getConnectionInfo();

                   if (!mWifiInfo.getBSSID().contains(savedBSSID)) {
                       SharedPreferences.Editor editor = connectedWifi.edit();
                       editor.putInt(getString(R.string.networkId), mWifiInfo.getNetworkId());
                       editor.putString(getString(R.string.savedBSSID), mWifiInfo.getBSSID());
                       editor.putString(getString(R.string.savedSSID), mWifiInfo.getSSID().substring(1,mWifiInfo.getSSID().length()-1));
                       editor.putString(getString(R.string.savedSecurity), getWifiSecurity(wifiManager, mWifiInfo.getBSSID()));
                       editor.putInt(getString(R.string.savedLinkedSpeed), mWifiInfo.getLinkSpeed());
                       editor.putInt(getString(R.string.savedFrequency), mWifiInfo.getFrequency());
                       editor.putInt(getString(R.string.savedSignalStrength), mWifiInfo.getRssi());

                       editor.apply();
                   }
               }
               else{
                   connectedWifi.edit().clear().apply();
               }
           }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.SecureAppTheme);
        setContentView(R.layout.activity_settings_main);
        tvSleepTime = findViewById(R.id.sleep_time);

        initializeViews();
        wifiManager= (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        linearLayout = findViewById(R.id.linearLayout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Secure Settings");
        String[] prems = {Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE};
        if (EasyPermissions.hasPermissions(this,prems)){

        }else{
                EasyPermissions.requestPermissions(this, getString(R.string.fine_location_and_cross_location),
                        RC_PERMISSION, prems);
        }

        clickListeners();

    }

    private void clickListeners() {
        wifiContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsMainActivity.this, WifiMainActivity.class);
                startActivity(intent);
            }
        });
        bluetooth_container_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent blIntent = new Intent(SettingsMainActivity.this, BluetoothMainActivity.class);
                startActivity(blIntent);
            }
        });

        brightnessContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean permission = permissionModify(SettingsMainActivity.this);
                if(permission)
                {
                    int width = (int) (Resources.getSystem().getDisplayMetrics().widthPixels * 0.90);
                    LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                    ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.dialog_brightness,null);

                    popupWindow = new PopupWindow(container,width,(int)pxFromDp(SettingsMainActivity.this,60),true);
                    popupWindow.showAtLocation(linearLayout,Gravity.CENTER_HORIZONTAL,0,-400);
                    SeekBar seekBar = container.findViewById(R.id.seek_bar);

                    int brightness = getScreenBrightness(SettingsMainActivity.this);
                    seekBar.setProgress(brightness);
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            setScreenBrightness(progress);
                            tvBrightnessLevel.setText((int)(((float)getScreenBrightness(SettingsMainActivity.this)/255)*100) + "%");
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });

//                    BrightnessDialog brightnessDialog = new BrightnessDialog(SettingsMainActivity.this);
//                    brightnessDialog.show();
                }

            }
        });

        sleep_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SleepDialog sleepDialog = new SleepDialog(SettingsMainActivity.this);
                sleepDialog.show();
            }
        });

        sim_card_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsMainActivity.this,SimMainActivity.class);
                startActivity(intent);
            }
        });

        hotspot_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity( intent);

            }
        });
    }

    private void initializeViews() {
        settingsLayout = findViewById(R.id.settings_layout);
        permissionNotGrantedLayout = findViewById(R.id.permission_not_granted);
        registerReceiver(ConnectedNetworkReceiver,new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        bluetooth_container_layout = findViewById(R.id.bluetooth_container_layout);
        wifiContainer = findViewById(R.id.wif_container_layout);
        brightnessContainer = findViewById(R.id.brightness_container_layout);
        tvStatus = findViewById(R.id.wifi_name);

        toolbar = findViewById(R.id.main_settings_bar);
        tvBrightnessLevel = findViewById(R.id.brightness_lavel);
        bluetoothStatus = findViewById(R.id.bluetooth_name);
        sleep_container = findViewById(R.id.sleep_cotainer);
        sim_card_container = findViewById(R.id.sim_cotainer);
        hotspot_container = findViewById(R.id.hotspot_container);

    }

    private void setWifiStatus() {

        if(isWifiConnected(this))
        {

                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String SSID = wifiInfo.getSSID().substring(1,wifiInfo.getSSID().length()-1);
                if(SSID.contains("unknown"))
                {

                    tvStatus.setText("Not Connected");
                }
                else{
//                    SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.connectNetwork),MODE_PRIVATE);
//                    String savedSSID = sharedPreferences.getString("SSID","Not Connected");
//                    tvStatus.setText(savedSSID);
                    tvStatus.setText(SSID);

                }


        } else {
            clearConnectedPreference(this);
            int wifiState = wifiManager.getWifiState();
            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                tvStatus.setText("Not Connected");
            } else {
                tvStatus.setText("Disabled");
            }
        }


    }



    @Override
    protected void onResume() {
        super.onResume();
        setWifiStatus();
        setBluetoothStatus();
        tvBrightnessLevel.setText((int)(((float)getScreenBrightness(this)/255)*100) + "%");
        tvSleepTime.setText("After " + secondsToMintues(getSleepTime(SettingsMainActivity.this)) + " of inactivity");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == CODE_WRITE_SETTINGS_PERMISSION)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(Settings.System.canWrite(SettingsMainActivity.this))
                {
                    if(!checkLocationStatus(SettingsMainActivity.this))
                    {

                        turnOnLocation(SettingsMainActivity.this);
                    }
                }
            }
        }
        else if(requestCode == LOCATION_SETTINGS_CODE)
        {
            if(!(resultCode == RESULT_OK))
            {
               turnOnLocation(SettingsMainActivity.this);
            }


        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (permissionModify(this)){
        }

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        permissionNotGrantedLayout.setVisibility(View.VISIBLE);
        settingsLayout.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void setBluetoothStatus()
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter.isEnabled())
        {
            bluetoothStatus.setText("Enabled");
        }else{
            bluetoothStatus.setText("Disabled");

        }
    }


    @Override
    public void brightnessChanged() {
        tvBrightnessLevel.setText((int)(((float)getScreenBrightness(this)/255)*100) + "%");

    }

    public void setScreenBrightness(int brightnessValue){

        if(brightnessValue >= 0 && brightnessValue <= 255){
            Settings.System.putInt(
                    getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightnessValue
            );
        }
    }


    @Override
    public void sleepTimeChanged(String time) {
        tvSleepTime.setText(time);
    }

    private String secondsToMintues(int seconds)
    {
        int minutes = seconds/60;
        if(minutes!= 0)
        {
            return minutes + " minutes";
        }
        return seconds + " seconds";
    }

}
