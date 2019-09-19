package com.screenlocker.secure.mdm.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

public class BluetoothHotSpotChangeReceiver extends BroadcastReceiver {

    private BluetoothHotSpotStateListener listener;

    public void setBluetoothListener(BluetoothHotSpotStateListener listener)
    {
        this.listener = listener;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                   listener.isBlueToothEnable(false);
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:

                    break;
                case BluetoothAdapter.STATE_ON:
                    listener.isBlueToothEnable(true);

                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    break;
            }
        }
        if(intent.getAction().equals("android.net.wifi.WIFI_AP_STATE_CHANGED"))
        {
            int apState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            if (apState == 13) {

                listener.isHotspotEnable(true);
            } else {
                listener.isHotspotEnable(false);
            }
        }
    }

    public interface BluetoothHotSpotStateListener {
        void isBlueToothEnable(boolean enable);
        void isHotspotEnable(boolean enable);
    }
}
