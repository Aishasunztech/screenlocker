package com.vortexlocker.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.vortexlocker.app.alarm.AlarmReceiver;
import com.vortexlocker.app.service.LockScreenService;
import com.vortexlocker.app.utils.AppConstants;
import com.vortexlocker.app.utils.PrefUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

import timber.log.Timber;

import static com.vortexlocker.app.utils.AppConstants.DEVICE_STATUS;

public class ReBootReciever extends BroadcastReceiver {
    private static final String TAG = ReBootReciever.class.getSimpleName();


    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.tag(TAG).e("onReceive: triggered");

//        context.sendBroadcast(new Intent().setAction("com.mediatek.ppl.NOTIFY_LOCK"));

        if (Objects.requireNonNull(intent.getAction()).equals(Intent.ACTION_BOOT_COMPLETED)) {
            String device_status = PrefUtils.getStringPref(context, DEVICE_STATUS);
            Timber.d("<<< device status >>>%S", device_status);

            if (device_status != null) {
                Intent lockScreenIntent = new Intent(context, LockScreenService.class);
                lockScreenIntent.setAction("reboot");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(lockScreenIntent);
                } else {
                    context.startService(lockScreenIntent);
                }

            }

            Toast.makeText(context, "on boot completed", Toast.LENGTH_LONG).show();
            PrefUtils.saveStringPref(context, AppConstants.KEY_SHUT_DOWN, AppConstants.VALUE_SHUT_DOWN_FALSE);

            // on device boot complete, reset the alarm
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(context));

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, sharedPref.getInt("dailyNotificationHour", 7));
            calendar.set(Calendar.MINUTE, sharedPref.getInt("dailyNotificationMin", 15));
            calendar.set(Calendar.SECOND, 1);

            Calendar newC = new GregorianCalendar();
            newC.setTimeInMillis(sharedPref.getLong("nextNotifyTime", Calendar.getInstance().getTimeInMillis()));

            if (calendar.after(newC)) {
                calendar.add(Calendar.HOUR, 1);
            }

            if (manager != null) {
                manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, pendingIntent);
            }
        }


    }

}