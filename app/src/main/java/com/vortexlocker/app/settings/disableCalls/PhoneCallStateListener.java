package com.vortexlocker.app.settings.disableCalls;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

import timber.log.Timber;

public class PhoneCallStateListener extends PhoneStateListener {
public static final String TAG=PhoneCallStateListener.class.getSimpleName();
private Context context;
public PhoneCallStateListener(Context context){
    this.context = context;
}


@Override
public void onCallStateChanged(int state, String incomingNumber) {  
    SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(context);

    switch (state) {

        case TelephonyManager.CALL_STATE_RINGING:

              String block_number = prefs.getString("block_number", null);
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            //Turn ON the mute
            audioManager.setStreamMute(AudioManager.STREAM_RING, true);                 
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            try {
                Timber.e("onCallStateChanged: " + block_number);
                Toast.makeText(context, "in"+block_number, Toast.LENGTH_LONG).show();
                Class clazz = Class.forName(telephonyManager.getClass().getName());
                Method method = clazz.getDeclaredMethod("getITelephony");
                method.setAccessible(true);
                ITelephony telephonyService = (ITelephony) method.invoke(telephonyManager);
                //Checking incoming call number
                System.out.println("Call "+block_number);

               // if (incomingNumber.equalsIgnoreCase("+91"+block_number)) {
                    //telephonyService.silenceRinger();//Security exception problem
                     telephonyService = (ITelephony) method.invoke(telephonyManager);
                     telephonyService.silenceRinger();
                    System.out.println(" in  "+block_number);
                    telephonyService.endCall();
               // }
            } catch (Exception e) {
                Timber.e(e, "error: ");
                Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
            }
            //Turn OFF the mute     
            audioManager.setStreamMute(AudioManager.STREAM_RING, false);
            break;
        case PhoneStateListener.LISTEN_CALL_STATE:

    }
    super.onCallStateChanged(state, incomingNumber);
}}