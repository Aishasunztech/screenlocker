package com.titanlocker.secure.socket.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MdmEventReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String device_status = intent.getStringExtra("device_status");

        if (device_status != null) {
            switch (device_status) {
                case "connected":


                    break;
            }
        }

    }
}
