package com.vortexlocker.app.socket.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vortexlocker.app.socket.interfaces.DeviceStatus;

import static com.vortexlocker.app.utils.AppConstants.DEVICE_STATUS_CHANGE_RECEIVER;

public class DeviceStatusReceiver extends BroadcastReceiver {


    private DeviceStatus listener;

    public void setListener(DeviceStatus deviceStatus) {

        listener = deviceStatus;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(DEVICE_STATUS_CHANGE_RECEIVER)) {
                String device_status = intent.getStringExtra("device_status");
                if (listener != null) {
                    listener.onStatusChanged(device_status);
                }
            }
        }

    }
}
