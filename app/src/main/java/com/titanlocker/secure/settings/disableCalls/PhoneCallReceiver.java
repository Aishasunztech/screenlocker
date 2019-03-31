package com.titanlocker.secure.settings.disableCalls;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;
import com.titanlocker.secure.utils.AppConstants;
import com.titanlocker.secure.utils.PrefUtils;

import java.lang.reflect.Method;

import timber.log.Timber;

public class PhoneCallReceiver extends BroadcastReceiver {
    private ITelephony telephonyService;
public static final String TAG=PhoneCallReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
 //  TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
 // PhoneCallStateListener customPhoneListener = new PhoneCallStateListener(context);
 //  telephony.listen(customPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        TelephonyManager telephony = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
//        try {
//            Class c = Class.forName(telephony.getClass().getName());
//            Method m = c.getDeclaredMethod("getITelephony");
//            m.setAccessible(true);
//            telephonyService = (ITelephony) m.invoke(telephony);
//            //telephonyService.silenceRinger();
//            telephonyService.endCall();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        endCall(context);

//        ITelephony telephonyService;
//        try {
//            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
//            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
//
//            if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
//                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//                try {
//                    Method m = tm.getClass().getDeclaredMethod("getITelephony");
//
//                    m.setAccessible(true);
//                    telephonyService = (ITelephony) m.invoke(tm);
//
//                    if ((number != null)) {
//                        telephonyService.endCall();
//                        Toast.makeText(context, "Ending the call from: " + number, Toast.LENGTH_SHORT).show();
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//                Toast.makeText(context, "Ring " + number, Toast.LENGTH_SHORT).show();
//
//            }
//            if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
//                Toast.makeText(context, "Answered " + number, Toast.LENGTH_SHORT).show();
//            }
//            if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_IDLE)) {
//                Toast.makeText(context, "Idle " + number, Toast.LENGTH_SHORT).show();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @SuppressLint("PrivateApi")
    public static void endCall(Context context) {
        boolean isCallDisable = PrefUtils.getBooleanPref(context, AppConstants.KEY_DISABLE_CALLS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null && ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
                //telecomManager.endCall();
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
            if(isCallDisable)
            telephonyEndCall = telephonyClass.getMethod("endCall");
            else
            telephonyClass.getMethod("answerRingingCall");
            if (telephonyEndCall!=null)
            telephonyEndCall.invoke(telephonyObject);
        } catch (Exception e) {
            e.printStackTrace();
            Timber.e(e, "endCall: ");

        }
    }
}