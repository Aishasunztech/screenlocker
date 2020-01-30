package com.screenlocker.secure.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.utils.PrefUtils;
import com.secure.launcher.IPCWithSL;

import static com.screenlocker.secure.utils.AppConstants.CHAT_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.PGP_EMAIL;

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
            return PrefUtils.getStringPref(MyApplication.getAppContext(), CHAT_ID);
        }

        @Override
        public String getDeviceId() throws RemoteException {
            return PrefUtils.getStringPref(MyApplication.getAppContext(), DEVICE_ID);
        }

        @Override
        public String getPGPEmail() throws RemoteException {
            return PrefUtils.getStringPref(MyApplication.getAppContext(), PGP_EMAIL);
        }

        @Override
        public boolean isPackageSuspended(String packageName) throws RemoteException {
            return false;
        }
    };
}
