package com.titanlocker.secure.mdm.utils;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import static com.titanlocker.secure.mdm.utils.LifecycleReceiver.LIFECYCLE_ACTION;

public class LifecycleReceiverService extends Service implements LifecycleReceiver.StateChangeListener {

    private boolean isRunning;
    private LifecycleReceiver lifecycleReceiver;                //<---

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        this.isRunning = false;
        lifecycleReceiver = new LifecycleReceiver();            //<---

    }

    @Override
    public void onDestroy() {
        this.isRunning = false;
        Log.e("TAGLLLL", "onDestroy: Ended" );
        unregisterReceiver(lifecycleReceiver);
        lifecycleReceiver.unsetStateChangeListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!this.isRunning) {
            this.isRunning = true;
        }

        Log.e("TAGLLLL", "onStartCommand: Started" );
        registerReceiver(lifecycleReceiver,new IntentFilter(LIFECYCLE_ACTION));
        lifecycleReceiver.setStateChangeListener(this);
        return START_STICKY;
    }

    @Override
    public void onStateChange(int state) {            //<---
        switch (state){

            case LifecycleReceiver.FOREGROUND:
                Log.e("TAGLLLL", "onStateChange: FOREGROUND" );
                break;

            case LifecycleReceiver.BACKGROUND:
                Log.e("TAGLLLL", "onStateChange: BACKGROUND" );
                break;

            default:
                Log.e("TAGLLLL", "onStateChange: SOMETHING" );
                break;
        }
    }
}
