package com.screenlocker.secure.mdm.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.screenlocker.secure.async.InternetCheck;

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
    public void onReceive(Context context, Intent intent)
    {
        if (listener!=null)
            try
            {
                new InternetCheck(internet -> {
                    if (internet) {
                        if(listener!=null)
                        listener.isConnected(true);
                        Log.e(TAG, "You are Online !");
                    } else {
                        if(listener!=null)
                        listener.isConnected(false);
                        Log.e(TAG, "You are Offline ! ");
                    }
                });
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
    }
    public interface NetworkChangeListener{

        void isConnected(boolean state);
    }
}
