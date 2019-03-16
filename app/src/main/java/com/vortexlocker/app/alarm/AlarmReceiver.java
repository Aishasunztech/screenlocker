package com.vortexlocker.app.alarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.vortexlocker.app.R;
import com.vortexlocker.app.launcher.MainActivity;
import com.vortexlocker.app.utils.PrefUtils;

import java.util.Calendar;
import java.util.Objects;

import static com.vortexlocker.app.utils.AppConstants.VALUE_EXPIRED;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(context));
        SharedPreferences.Editor sharedPrefEditor = prefs.edit();

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, MainActivity.class);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingI = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "Daily Notification",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Daily Notification");
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
        NotificationCompat.Builder b = new NotificationCompat.Builder(context, "default");
        b.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
//                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setTicker("{Time to watch some cool stuff!}")
                .setContentTitle("My Cool App")
                .setContentText("Time to watch some cool stuff!")
                .setContentInfo("INFO")
                .setContentIntent(pendingI);

        if (nm != null) {

            int value_expired = PrefUtils.getIntegerPref(context, VALUE_EXPIRED);

//            nm.notify(1, b.build());

            Calendar nextNotifyTime = Calendar.getInstance();
            nextNotifyTime.add(Calendar.DATE, 1);
            sharedPrefEditor.putLong("nextNotifyTime", nextNotifyTime.getTimeInMillis());
            sharedPrefEditor.apply();
        }


    }
}