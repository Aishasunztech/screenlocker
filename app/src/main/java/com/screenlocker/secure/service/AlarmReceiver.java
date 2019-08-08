package com.screenlocker.secure.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import static com.screenlocker.secure.utils.CommonUtils.getRemainingDays;
import static com.screenlocker.secure.utils.CommonUtils.setAlarmManager;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String remainingDays = getRemainingDays(context);
        int days = Integer.valueOf(remainingDays);
        Log.d("kjshdf","called: lksjdf");
        if(days <= 7 && days >=1)
        {
            PrefUtils.saveBooleanPref(context, AppConstants.PENDING_ALARM_DIALOG,true);
            PrefUtils.saveStringPref(context, AppConstants.PENDING_DIALOG_MESSAGE,"Your device will be expired in" + remainingDays);
        }

        setAlarmManager(context,System.currentTimeMillis() + 86400000L);
    }
}
