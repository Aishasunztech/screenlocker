package com.screenlocker.secure.mdm.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.screenlocker.secure.utils.CommonUtils;

import timber.log.Timber;

/**
 * to check the network state
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    public static final String TAG = "NetworkChangeReceiver";

    private NetworkChangeListener listener;

    public void setNetworkChangeListener(NetworkChangeListener listener) {
        this.listener = listener;
    }

    public void unsetNetworkChangeListener() {
        this.listener = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (CommonUtils.isNetworkAvailable(context)) {
            if (listener != null) {
                listener.isConnected(true);
                Timber.d("You are Online !");
            }
        } else {
            if (listener != null) {
                listener.isConnected(false);
                Timber.d("You are Offline ! ");
            }
        }

    }

    public interface NetworkChangeListener {

        void isConnected(boolean state);
    }


}
