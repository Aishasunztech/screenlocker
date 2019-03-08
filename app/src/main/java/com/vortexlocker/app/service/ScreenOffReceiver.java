package com.vortexlocker.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import timber.log.Timber;

import static com.vortexlocker.app.ShutDownReceiver.TAG;


public class ScreenOffReceiver extends BroadcastReceiver {

    private final OnScreenOffListener listener;

    public ScreenOffReceiver(OnScreenOffListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        listener.onScreenOff();

        Timber.d("screen on / off");
    }


    public interface OnScreenOffListener {
        void onScreenOff();
    }
}
