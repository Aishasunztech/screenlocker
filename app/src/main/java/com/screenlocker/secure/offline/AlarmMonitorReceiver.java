package com.screenlocker.secure.offline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmMonitorReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            String time_milli = "";
            AlarmTimeSet.at_(context, time_milli);
        }

    }
}