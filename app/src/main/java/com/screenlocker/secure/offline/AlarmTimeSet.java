package com.screenlocker.secure.offline;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import static android.content.Context.ALARM_SERVICE;

public class AlarmTimeSet {

    public static void at_(Context context, String time_milli) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(context, MyAlarmBroadcastReceiver.class);
        int i = Integer.parseInt(time_milli);
        long l = Long.valueOf(time_milli);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context.getApplicationContext(), 234324243, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (pendingIntent != null) {

            alarmManager.cancel(pendingIntent);
            // alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (i * 1000), pendingIntent);
            alarmManager.set(AlarmManager.RTC, l, pendingIntent);

        } else {
            //  alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (i * 1000), pendingIntent);
            alarmManager.set(AlarmManager.RTC, l, pendingIntent);
        }

    }
}
