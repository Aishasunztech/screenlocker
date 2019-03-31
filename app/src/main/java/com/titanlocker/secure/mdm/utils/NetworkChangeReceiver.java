package com.titanlocker.secure.mdm.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

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
                if (isOnline(context)) {
                    listener.isConnected(true);
                    Log.e(TAG, "You are Online !");
                } else {
                    listener.isConnected(false);
                    Log.e(TAG, "You are Offline ! ");
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
    }

    private boolean isOnline(Context context) {

        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = null;
            if (cm != null) {
                netInfo = cm.getActiveNetworkInfo();
            }
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    public interface NetworkChangeListener{

        void isConnected(boolean state);
    }
}
