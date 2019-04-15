package com.secureSetting.wifisettings;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.screenlocker.secure.R;
import com.secureSetting.wifisettings.Dialogs.ForgetWifiDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static com.secureSetting.AppConstants.LOCATION_PERMISSION_CODE;
import static com.secureSetting.AppConstants.LOCATION_SETTINGS_CODE;
import static com.secureSetting.UtilityFunctions.checkLocationStatus;
import static com.secureSetting.UtilityFunctions.isWifiConnected;
import static com.secureSetting.UtilityFunctions.turnOnLocation;


public class WifiMainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, ForgetWifiDialog.ConnectListener {


    private List<ScanResult> mScanResults;
    private WifiManager mWifiManager;
    private LocationManager locationManager;
    private String[] perms;

    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private TextView tvTitle;
    private SwitchCompat on_off_switch;
    private ProgressBar progressBar;
    private boolean isDialogShow = false;
    private WifiAdapter mAdapter;
    private  boolean isConnected = false;

    public static int networkId = 0;

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*mScanResults = mWifiManager.getScanResults();*/
            mScanResults.clear();
            mScanResults.addAll(mWifiManager.getScanResults());
            if(isWifiConnected(context))
            {
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                for(ScanResult scanResult : mScanResults)
                {
                    if(scanResult.BSSID.contains(wifiInfo.getBSSID()))
                    {
                        int index = mScanResults.indexOf(scanResult);
                        Collections.swap(mScanResults,index,0);
                    }
                }
            }
//            mAdapter.notifyDataSetChanged();
            mAdapter = new WifiAdapter(mScanResults, WifiMainActivity.this, mWifiManager, WifiMainActivity.this);
            recyclerView.setAdapter(mAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(WifiMainActivity.this));

            progressBar.setVisibility(View.GONE);


        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_main);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        initializeViews();
        int wifiState = mWifiManager.getWifiState();
        if(wifiState == WifiManager.WIFI_STATE_ENABLED){
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();

            if( wifiInfo.getNetworkId() == -1 ){
                // Not connected to an access point

            }
            else {
                // Connected to an access point
                isConnected = true;


            }
            progressBar.setVisibility(View.VISIBLE);
        }


        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mScanResults = new ArrayList<>();
//        mAdapter = new WifiAdapter(mScanResults,WifiMainActivity.this, mWifiManager,this);
//        recyclerView.setAdapter(mAdapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(WifiMainActivity.this));





        //Set Variables
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.wifi_bar_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        perms = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};


        if(mWifiManager.isWifiEnabled())
        {
            on_off_switch.setChecked(true);
            tvTitle.setText("On");
        }
        else{
            on_off_switch.setChecked(false);
            tvTitle.setText("Off");
        }

        //switch Listener
        on_off_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    mWifiManager.setWifiEnabled(true);
                    tvTitle.setText("On");
                    progressBar.setVisibility(View.VISIBLE);
                    getWifiList();


                }
                else{
                   mScanResults.clear();
                    tvTitle.setText("Off");
                    mWifiManager.setWifiEnabled(false);

                }
            }
        });


        if (EasyPermissions.hasPermissions(this, perms)) {
                getWifiList();

        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.location_permission_message),
                    LOCATION_PERMISSION_CODE, perms);
        }

    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.wifi_list_recycler);
        toolbar = findViewById(R.id.wifi_settings_bar);
        tvTitle = findViewById(R.id.wifi_settings_title);
        on_off_switch = findViewById(R.id.on_off_switch);
        progressBar = findViewById(R.id.progressBar);
    }

    public void getWifiList() {
        if(checkLocationStatus(WifiMainActivity.this)) {
            mWifiManager.startScan();
        }
        else{
            turnOnLocation(WifiMainActivity.this);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.hasPermissions(this, this.perms)) {
            getWifiList();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
          if(requestCode == LOCATION_SETTINGS_CODE) {
                if (resultCode == RESULT_OK) {
                    getWifiList();
                }
                else{
                    this.finish();
                }
            }

    }


    @Override
    public void disconnect() {
        getWifiList();
    }


    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        if(item.getItemId() == R.id.home)
        {
           onBackPressed();
        }
        return false;
    }
}
