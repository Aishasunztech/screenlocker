package com.secureSetting.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.screenlocker.secure.R;
import com.secureSetting.bluetooth.BluetoothAdapters.PairedDeviceAdapter;
import com.secureSetting.bluetooth.BluetoothDialogs.BluetoothNameDialog;
import com.secureSetting.bluetooth.BluetoothDialogs.ForgetBluetoothDialog;

import java.util.ArrayList;

public class BluetoothMainActivity extends AppCompatActivity implements BluetoothNameDialog.ChangeBNameListener
        , ForgetBluetoothDialog.UnpairDeviceListener {

    private Toolbar toolbar;
    private LinearLayout switchContainer,pair_new_container,device_name_container,contentContainer;
    private TextView tvBluetooth,tv_deviceName,blueToothAddressTitle,tvVisibleName,btnReceivedFiles;
    private SwitchCompat switchBluetooth;
    private RecyclerView rc;
    BluetoothAdapter bluetoothAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.SecureAppTheme);
        setContentView(R.layout.activity_bluetooth_main);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        initializeViews();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Bluetooth");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (bluetoothAdapter.isEnabled())
        {
            tvBluetooth.setText("On");
            switchBluetooth.setChecked(true);
        }
        else{
            tvBluetooth.setText("Off");
            switchBluetooth.setChecked(false);
        }

        switchBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    contentContainer.setVisibility(View.VISIBLE);
                    bluetoothAdapter.enable();
                }
                else{
                    contentContainer.setVisibility(View.GONE);
                    bluetoothAdapter.disable();
                }
            }
        });

        updateName();
        blueToothAddressTitle.setText("Phone Bluetooth Address: " + bluetoothAdapter.getAddress());
        clickListeners(bluetoothAdapter);
        getPairedDevices();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.blutoothBar);
        switchContainer = findViewById(R.id.bluetooth_switch_container);
        tvBluetooth = findViewById(R.id.bluetooth_settings_status);
        blueToothAddressTitle = findViewById(R.id.bluetooth_address);
        switchBluetooth = findViewById(R.id.bluetooth_on_off_switch);
        pair_new_container = findViewById(R.id.pair_new_container);
        device_name_container = findViewById(R.id.device_name_container);
        tv_deviceName = findViewById(R.id.bluetooth_device_name);
        rc = findViewById(R.id.paired_list);
        tvVisibleName = findViewById(R.id.visivble_name_label);
        contentContainer = findViewById(R.id.contentContainer);
        btnReceivedFiles = findViewById(R.id.btnReceivedFiles);
    }

    private void clickListeners(final BluetoothAdapter mBluetoothAdapter) {
        switchContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isEnabled()) {
                    contentContainer.setVisibility(View.GONE);
                    mBluetoothAdapter.disable();
                    tvBluetooth.setText("Off");
                    switchBluetooth.setChecked(false);
                }
                else{
                    contentContainer.setVisibility(View.VISIBLE);

                    mBluetoothAdapter.enable();
                    tvBluetooth.setText("On");
                    switchBluetooth.setChecked(true);
                }
            }
        });
        pair_new_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BluetoothMainActivity.this, BluetoothActivity.class);
                startActivity(intent);
            }
        });

        device_name_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
        BluetoothNameDialog blutoothNameDialog = new BluetoothNameDialog(
        BluetoothMainActivity.this,BluetoothMainActivity.this);
        blutoothNameDialog.show();
            }
        });

        btnReceivedFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BluetoothMainActivity.this,BTReceivedFilesActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public void nameChanged() {
        updateName();

    }

    private void updateName() {
        tv_deviceName.setText(bluetoothAdapter.getName());
        tvVisibleName.setText("Visible as " + bluetoothAdapter.getName() + " to other devices");
    }

    private void getPairedDevices()
    {
        ArrayList<BluetoothDevice> pairedList = new ArrayList<>();
//        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        pairedList.addAll(bluetoothAdapter.getBondedDevices());
        if (pairedList.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            rc.setAdapter(new PairedDeviceAdapter(this,pairedList,this));
            rc.setLayoutManager(new LinearLayoutManager(this));
//            for (BluetoothDevice device : pairedDevices) {
//                String deviceName = device.getName();
//                Log.d("Bluetoothdevicename",deviceName);
//                String deviceHardwareAddress = device.getAddress(); // MAC address
//            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPairedDevices();
    }

    @Override
    public void unpairDevice() {
        this.recreate();
    }
}
