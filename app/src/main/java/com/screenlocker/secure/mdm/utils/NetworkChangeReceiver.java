package com.screenlocker.secure.mdm.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

import static com.screenlocker.secure.utils.CommonUtils.setAlarmManager;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("on Receiver ");
        // alarm manager for network / socket connection
        setAlarmManager(context, System.currentTimeMillis() + 1, 1);

    }
}
