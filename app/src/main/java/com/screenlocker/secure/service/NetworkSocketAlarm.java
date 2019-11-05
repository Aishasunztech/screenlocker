package com.screenlocker.secure.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.screenlocker.secure.network.CheckInternetTask;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import static com.screenlocker.secure.utils.AppConstants.CONNECTED;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DISCONNECTED;
import static com.screenlocker.secure.utils.CommonUtils.isSocketConnected;
import static com.screenlocker.secure.utils.CommonUtils.setAlarmManager;

public class NetworkSocketAlarm extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (isNetworkConnected(context)) {
            if (isSocketConnected()) {
                PrefUtils.saveStringPref(context, AppConstants.CURRENT_NETWORK_STATUS, CONNECTED);
            } else {
                new CheckInternetTask(data -> PrefUtils.saveStringPref(context, AppConstants.CURRENT_NETWORK_STATUS, data ? CONNECTED : DISCONNECTED)).execute();
            }

//            if (PrefUtils.getBooleanPref(context, DEVICE_LINKED_STATUS)) {
                setAlarmManager(context, System.currentTimeMillis() + 1500L, 1);
//            }

        } else {
            PrefUtils.saveStringPref(context, AppConstants.CURRENT_NETWORK_STATUS, DISCONNECTED);
        }

    }

    boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();

        //should check null because in airplane mode it will be null
        return netInfo != null && netInfo.isAvailable() && netInfo.isConnected();

    }
}
