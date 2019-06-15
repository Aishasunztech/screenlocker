package com.screenlocker.secure.settings.codeSetting.Sim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.screenlocker.secure.service.LockScreenService;

public class SimStateChangeListener extends BroadcastReceiver {
  String TAG = "sndiofhdigojfho";
    @Override
    public void onReceive(Context context, Intent intent) {
        assert intent.getAction() != null;
        if (intent.getAction().equals("android.intent.action.SIM_STATE_CHANGED")) {

            String state = intent.getStringExtra("ss");
            if (state.equals("ABSENT")){
                Intent lock = new Intent(context, LockScreenService.class);
                lock.setAction("locked");
                ActivityCompat.startForegroundService(context,lock);
            }
        }
    }
}
