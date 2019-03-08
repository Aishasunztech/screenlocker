package com.vortexlocker.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.vortexlocker.app.service.LockScreenService;
import com.vortexlocker.app.utils.AppConstants;
import com.vortexlocker.app.utils.PrefUtils;

import java.util.Objects;

import timber.log.Timber;

import static com.vortexlocker.app.utils.AppConstants.DEVICE_STATUS;

public class ReBootReciever extends BroadcastReceiver {
    private static final String TAG = ReBootReciever.class.getSimpleName();


    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.tag(TAG).e("onReceive: triggered");


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
        }
    }
}