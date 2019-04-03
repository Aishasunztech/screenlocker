package com.screenlocker.secure.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;


public class ScreenOffReceiver extends BroadcastReceiver {

    private final OnScreenOffListener listener;

    public ScreenOffReceiver(OnScreenOffListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        listener.onScreenOff();
        Timber.d("screen off");
    }


    public interface OnScreenOffListener {
        void onScreenOff();
    }
}
