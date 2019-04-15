package com.securesettings;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.SeekBar;

import com.securesettings.databinding.ActivityMainBinding;

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

public class MainActivity extends AppCompatActivity implements  BrightnessDialog.BrightnessChangeListener
        , SleepDialog.SleepChangerListener {

    private PopupWindow popupWindow;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);

        setSupportActionBar(binding.mainSettingsBar);
        if(getSupportActionBar() != null)
        {
            getSupportActionBar().setTitle("Secure Settings");
        }

        checkPermissions(this);
        permissionModify(MainActivity.this);
        if(!checkLocationStatus(this))
        {
            turnOnLocation(this);
        }
        clickListeners();

    }



    private void clickListeners() {
        binding.wifContainerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//                Intent intent = new Intent(SettingsMainActivity.this,WifiMainActivity.class);
                startActivity(intent);
            }
        });
        binding.bluetoothContainerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ComponentName cn = new ComponentName("com.android.settings",
                        "com.android.settings.bluetooth.BluetoothSettings");

                Intent intent = new Intent(Intent.ACTION_MAIN, null);
                intent.setComponent(cn);
                startActivity( intent);
            }
        });

        binding.brightnessContainerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean permission = permissionModify(MainActivity.this);
                if(permission)
                {
                    int width = (int) (Resources.getSystem().getDisplayMetrics().widthPixels * 0.90);
                    LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                    ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.dialog_brightness,null);

                    popupWindow = new PopupWindow(container,width,(int)pxFromDp(MainActivity.this,60),true);
                    popupWindow.showAtLocation(binding.linearLayout, Gravity.CENTER_HORIZONTAL,0,-400);
                    SeekBar seekBar = container.findViewById(R.id.seek_bar);

                    int brightness = getScreenBrightness(MainActivity.this);
                    seekBar.setProgress(brightness);
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            setScreenBrightness(MainActivity.this,progress);
                            binding.brightnessLavel.setText((int)(((float)getScreenBrightness(MainActivity.this)/255)*100) + "%");
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

        binding.sleepCotainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SleepDialog sleepDialog = new SleepDialog(MainActivity.this);
                sleepDialog.show();
            }
        });

        binding.simCotainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
//                startActivity(intent);

                Intent intent = new Intent("com.android.settings.sim.SIM_SUB_INFO_SETTINGS");
                startActivity(intent);
            }
        });

        binding.hotspotContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_MAIN, null);
//                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
                intent.setComponent(cn);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity( intent);


            }
        });

        binding.dataRoamingCotainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Settings.Global.putInt(getContentResolver(), Settings.Global.DATA_ROAMING, 1);
            }
        });
        binding.screenLockContainer.setOnClickListener(new View.OnClickListener() {
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
        binding.bluetoothName.setText(getBlueToothStatus());
        binding.brightnessLavel.setText((int)(((float)getScreenBrightness(this)/255)*100) + "%");
        binding.sleepTime.setText("After " + secondsToMintues(getSleepTime(MainActivity.this)) + " of inactivity");
        binding.wifiName.setText(getWifiStatus(this));
    }

    @Override
    public void brightnessChanged() {
        binding.brightnessLavel.setText((int)(((float)getScreenBrightness(this)/255)*100) + "%");
    }

    @Override
    public void sleepTimeChanged(String time) {
        binding.sleepTime.setText(time);
    }
}
