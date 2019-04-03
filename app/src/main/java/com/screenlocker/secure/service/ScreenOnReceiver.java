package com.screenlocker.secure.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import timber.log.Timber;

public class ScreenOnReceiver extends BroadcastReceiver {


    private OnScreenOnListener listener;

    public ScreenOnReceiver(OnScreenOnListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        listener.onScreenOn();
        Timber.d("Screen on");
    }

    public interface OnScreenOnListener {
        void onScreenOn();
    }

}
