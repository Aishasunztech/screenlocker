package com.screenlocker.secure.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LifecycleReceiver extends BroadcastReceiver {

    public static final String TAG = "AppsStatusReceiver";
    public static final int FOREGROUND = 1;
    public static final int BACKGROUND = 2;
    public static final String STATE = "STATE";
    public static final String LIFECYCLE_ACTION = "com.secureportal.barryapp.utils.LIFECYCLE_ACTION";

    private StateChangeListener listener;

    public void setStateChangeListener(StateChangeListener listener) {
        this.listener = listener;
    }

    public void unsetStateChangeListener() {
        this.listener = null;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        switch (intent.getIntExtra(STATE,0)){

            case FOREGROUND:
                listener.onStateChange(FOREGROUND);
                break;

            case BACKGROUND:
                listener.onStateChange(BACKGROUND);
                break;
        }
    }

    public interface StateChangeListener{

        void onStateChange(int state);
    }
}
