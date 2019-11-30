package com.screenlocker.secure.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.screenlocker.secure.utils.PrefUtils;
import com.secure.launcher.IPCWithSL;

import static com.screenlocker.secure.utils.AppConstants.CHAT_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;

public class IPCService extends Service {


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    IPCWithSL.Stub binder =  new IPCWithSL.Stub() {
        @Override
        public String getChatId() throws RemoteException {

            return PrefUtils.getStringPref(IPCService.this, CHAT_ID);
        }

        @Override
        public String getDeviceId() throws RemoteException {
            return PrefUtils.getStringPref(IPCService.this, DEVICE_ID);
        }
    };
}
