package com.screenlocker.secure.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.screenlocker.secure.network.CheckInternetTask;
import com.screenlocker.secure.network.InternetConnectivityListener;
import com.screenlocker.secure.network.TaskFinished;
import com.screenlocker.secure.socket.SocketManager;

import static com.screenlocker.secure.utils.CommonUtils.setAlarmManager;

public class NetworkSocketAlarm extends BroadcastReceiver {


    private InternetConnectivityListener listener;

    public void setListener(InternetConnectivityListener listener) {
        this.listener = listener;
    }

    public void unsetListener() {
        this.listener = null;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (isNetworkConnected(context)) {

            if (SocketManager.getInstance().getSocket() != null && SocketManager.getInstance().getSocket().connected()) {
                if (listener != null) {
                    listener.onInternetStateChanged(true);
                }
            } else {
                new CheckInternetTask(new TaskFinished<Boolean>() {
                    @Override
                    public void onTaskFinished(Boolean data) {
                        if (listener != null) {
                            listener.onInternetStateChanged(data);
                        }
                    }
                }).execute();
            }
            setAlarmManager(context, System.currentTimeMillis() + 500L, 1);

        } else {
            if (listener != null) {
                listener.onInternetStateChanged(false);
            }
        }


    }


    boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();

        //should check null because in airplane mode it will be null
        return netInfo != null && netInfo.isAvailable() && netInfo.isConnected();

    }

}
