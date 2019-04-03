package com.screenlocker.secure.socket.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.screenlocker.secure.socket.interfaces.NetworkListener;

public class NetworkReceiver extends BroadcastReceiver {

    private final NetworkListener listener;

    public NetworkReceiver(NetworkListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            if (noConnectivity) {
                if (listener != null)
                    listener.onNetworkChange(false);
            } else {
                if (listener != null)
                    listener.onNetworkChange(true);
            }
        }
    }


}
