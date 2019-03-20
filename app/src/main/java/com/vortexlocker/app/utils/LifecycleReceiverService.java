package com.vortexlocker.app.utils;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import timber.log.Timber;

import static com.vortexlocker.app.utils.LifecycleReceiver.LIFECYCLE_ACTION;


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
        Timber.tag("TAGLLLL").e("onDestroy: Ended");
        unregisterReceiver(lifecycleReceiver);
        lifecycleReceiver.unsetStateChangeListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!this.isRunning) {
            this.isRunning = true;
        }
        Timber.tag("TAGLLLL").e("onStartCommand: Started");
        registerReceiver(lifecycleReceiver,new IntentFilter(LIFECYCLE_ACTION));
        lifecycleReceiver.setStateChangeListener(this);
        return START_STICKY;
    }

    @Override
    public void onStateChange(int state) {            //<---
        switch (state){

            case LifecycleReceiver.FOREGROUND:
                Timber.e("onStateChange: FOREGROUND");
                break;

            case LifecycleReceiver.BACKGROUND:
                Timber.tag("TAGLLLL").e("onStateChange: BACKGROUND");

                break;

            default:
                Timber.e("onStateChange: SOMETHING");
                break;
        }
    }
}
