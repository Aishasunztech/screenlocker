package com.screenlocker.secure.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.secure.launcher.R;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.CommonUtils.getRemainingDays;
import static com.screenlocker.secure.utils.CommonUtils.setAlarmManager;

public class OfflineExpiryAlarm extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PrefUtils prefUtils = PrefUtils.getInstance(context);
        String remainingDays = getRemainingDays(context,prefUtils);
        if (remainingDays != null) {

            if (!remainingDays.equals(context.getResources().getString(R.string.expired))) {
                int days = Integer.valueOf(remainingDays);
                if (days <= 0) {
//                    utils.suspendedDevice(context, "expired");
                } else if (days <= 7) {
                    prefUtils.saveBooleanPref(AppConstants.PENDING_ALARM_DIALOG, true);
                    prefUtils.saveStringPref(AppConstants.PENDING_DIALOG_MESSAGE, context.getResources().getString(R.string.expiry_alert_online_message, remainingDays));
                    setAlarmManager(context, System.currentTimeMillis() + 86400000L, 0);
                    String currentStatus = prefUtils.getStringPref( DEVICE_STATUS);
                    if(currentStatus != null && currentStatus.equals("expired"))
                    {
//                        utils.unSuspendDevice(context);
                    }
                } else {
                    setAlarmManager(context, System.currentTimeMillis() + 86400000L, 0);
                    String currentStatus = prefUtils.getStringPref( DEVICE_STATUS);
                    if(currentStatus != null && currentStatus.equals("expired"))
                    {
//                        utils.unSuspendDevice(context);
                    }
                }
            }

        }


    }
}
