package com.screenlocker.secure.offline;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyAlarmBroadcastReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {

        Toast.makeText(context, "Alarm....", Toast.LENGTH_LONG).show();

        //new LockScreenService().stopSelf();
    }

}
