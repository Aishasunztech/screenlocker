package com.secureSetting.bluetooth.BluetoothDialogs;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.screenlocker.secure.R;

import java.lang.reflect.Method;

public class ForgetBluetoothDialog extends Dialog {
    private LinearLayout container;
    private TextView btnForget,btnCancel;
    private EditText etName;
    private BluetoothDevice device;
    private UnpairDeviceListener listener;
    public ForgetBluetoothDialog(Context context,BluetoothDevice device,UnpairDeviceListener listener) {
        super(context);
        this.device = device;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_forget_bluetooth);

        initializeViews();

        etName.setText(device.getName());
        etName.setEnabled(false);

        btnForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unpairDevice(device);
                ForgetBluetoothDialog.this.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ForgetBluetoothDialog.this.dismiss();
                    }
                });
            }
        });


    }

    private void initializeViews() {
        container = findViewById(R.id.forgetBContainer);
        btnForget = findViewById(R.id.btnBForget);
        btnCancel = findViewById(R.id.btnBOk);
        etName = findViewById(R.id.et_rename_paired);
    }

    @Override
    protected void onStart() {
        super.onStart();

        container.setMinimumWidth(Resources.getSystem().getDisplayMetrics().widthPixels);
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
            listener.unpairDevice();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface UnpairDeviceListener{
        void unpairDevice();
    }
}
