package com.screenlocker.secure.service;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.Nullable;

import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.utils.PrefUtils;
import com.secure.launcher.IPCWithSL;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.CHAT_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.PGP_EMAIL;
import static com.screenlocker.secure.utils.AppConstants.URL_1;

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
//            Timber.d("getChatId: %s", PrefUtils.getStringPref(IPCService.this, CHAT_ID));
            return PrefUtils.getInstance(MyApplication.getAppContext()).getStringPref( CHAT_ID);
        }

        @Override
        public String getDeviceId() throws RemoteException {
//            Timber.d("getDeviceId: %s", PrefUtils.getStringPref(IPCService.this, DEVICE_ID));
            return PrefUtils.getInstance(MyApplication.getAppContext()).getStringPref( DEVICE_ID);
        }

        @Override
        public String getPGPEmail() throws RemoteException {
            return PrefUtils.getInstance(MyApplication.getAppContext()).getStringPref( PGP_EMAIL);
        }

        @Override
        public boolean isPackageSuspended(String packageName) throws RemoteException {
            DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
            ComponentName compName = new ComponentName(MyApplication.getAppContext(), MyAdmin.class);
            if (mDPM.isDeviceOwnerApp(getPackageName())) {
                try {
                    if (mDPM.isPackageSuspended(compName, getPackageName()))
                        return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }

            }
            return false;
        }

        @Override
        public int getWhiteLabelType() throws RemoteException {
            Timber.d("getwhitelabletype");
            int value = URL_1.equals("https://api.lockmesh.com") ? 0 : 1;
            Timber.d( "getWhiteLabelType: %s", String.valueOf(value));
            return value;
        }
    };
}
