package com.screenlocker.secure;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import timber.log.Timber;

public class ShutDownReceiver extends BroadcastReceiver {
    public static final String TAG = ShutDownReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
//            if(!PrefUtils.getBooleanPref(context,AppConstants.KEY_SHUT_DOWN))
            PrefUtils.saveStringPref(context, AppConstants.KEY_SHUT_DOWN, AppConstants.VALUE_SHUT_DOWN_TRUE);

            if (PrefUtils.getBooleanPref(context, AppConstants.TOUR_STATUS)) {
                PrefUtils.saveStringPref(context, AppConstants.CURRENT_KEY, AppConstants.KEY_GUEST_PASSWORD);
            }

            Toast.makeText(context, "System shutting down", Toast.LENGTH_SHORT).show();
            Timber.e("onReceive: shutdown" + PrefUtils.getStringPref(context, AppConstants.KEY_SHUT_DOWN));

            //context.stopService(new Intent(context, BluetoothPanService.class));
        }
    }

}