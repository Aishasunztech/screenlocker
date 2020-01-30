package com.screenlocker.secure.settings.disableCalls;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.android.internal.telephony.ITelephony;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.lang.reflect.Method;

import timber.log.Timber;

public class PhoneCallReceiver extends BroadcastReceiver {
    private ITelephony telephonyService;
    public static final String TAG = PhoneCallReceiver.class.getSimpleName();

    private static boolean incomingFlag = false;

    private static String incoming_number = null;


    @Override

    public void onReceive(Context context, Intent intent) {


        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {

            incomingFlag = false;

            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);

            Log.i(TAG, "call OUT:" + phoneNumber);

        } else {

            TelephonyManager tm =

                    (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);


            switch (tm.getCallState()) {

                case TelephonyManager.CALL_STATE_RINGING:

                    incomingFlag = true;

                    incoming_number = intent.getStringExtra("incoming_number");

                    Log.i(TAG, "RINGING :" + incoming_number);

                    endCall(context);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:

                    if (incomingFlag) {

                        Log.i(TAG, "incoming ACCEPT :" + incoming_number);

                    }

                    break;


                case TelephonyManager.CALL_STATE_IDLE:

                    if (incomingFlag) {
                        Log.i(TAG, "incoming IDLE");
                    }

                    break;

            }

        }


    }

    @SuppressLint("PrivateApi")
    public static void endCall(Context context) {
        boolean isCallDisable = PrefUtils.getBooleanPref(context, AppConstants.KEY_DISABLE_CALLS);
        Log.d(TAG, "endCall: d0");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            final TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null && ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
                if (isCallDisable) {
                    Log.d(TAG, "endCall: d");
                    telecomManager.endCall();
                }
                return;
            }
            return;
        }
        //use unofficial API for older Android versions, as written here: https://stackoverflow.com/a/8380418/878126
        try {
            final Class<?> telephonyClass = Class.forName("com.android.internal.telephony.ITelephony");
            final Class<?> telephonyStubClass = telephonyClass.getClasses()[0];
            final Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
            final Class<?> serviceManagerNativeClass = Class.forName("android.os.ServiceManagerNative");
            final Method getService = serviceManagerClass.getMethod("getService", String.class);
            final Method tempInterfaceMethod = serviceManagerNativeClass.getMethod("asInterface", IBinder.class);
            final Binder tmpBinder = new Binder();
            tmpBinder.attachInterface(null, "fake");
            final Object serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
            final IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, "phone");
            final Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);
            final Object telephonyObject = serviceMethod.invoke(null, retbinder);
            Method telephonyEndCall = null;
            if (isCallDisable)
                telephonyEndCall = telephonyClass.getMethod("endCall");
            else
                telephonyClass.getMethod("answerRingingCall");
            if (telephonyEndCall != null)
                telephonyEndCall.invoke(telephonyObject);
        } catch (Exception e) {
            e.printStackTrace();
            Timber.e(e, "endCall: ");

        }
    }
}