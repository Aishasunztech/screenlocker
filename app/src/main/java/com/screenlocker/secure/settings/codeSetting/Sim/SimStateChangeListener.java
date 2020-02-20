package com.screenlocker.secure.settings.codeSetting.Sim;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.screenlocker.secure.room.MyAppDatabase;
import com.secure.launcher.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.HashSet;
import java.util.Set;

import static com.screenlocker.secure.utils.AppConstants.BROADCAST_APPS_ACTION;
import static com.screenlocker.secure.utils.AppConstants.KEY_DATABASE_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.SIM_0_ICCID;
import static com.screenlocker.secure.utils.AppConstants.SIM_1_ICCID;
import static com.screenlocker.secure.utils.AppConstants.UNSYNC_ICCIDS;

public class SimStateChangeListener extends BroadcastReceiver {
  String TAG = "sndiofhdigojfho";
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void onReceive(Context context, Intent intent) {
        assert intent.getAction() != null;
        if (intent.getAction().equals("android.intent.action.SIM_STATE_CHANGED")) {

            String state = intent.getStringExtra("ss");
            if (state.equals("READY")){

                SubscriptionManager sManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
                SubscriptionInfo infoSim1 = sManager.getActiveSubscriptionInfoForSimSlotIndex(0);
                SubscriptionInfo infoSim2 = sManager.getActiveSubscriptionInfoForSimSlotIndex(1);
                if (infoSim1 != null) {
                    AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
                        int result =  MyAppDatabase.getInstance(context).getDao().updateSimStatus(0,context.getString(R.string.status_active),infoSim1.getIccId());
                        if (result>0){
                            saveIccid(context,infoSim1.getIccId());
                        }
                    });
                }
                if (infoSim2 != null) {
                   AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
                       int result =  MyAppDatabase.getInstance(context).getDao().updateSimStatus(1,context.getString(R.string.status_active),infoSim2.getIccId());
                       if (result>0){
                           saveIccid(context,infoSim2.getIccId());
                       }
                   });
                }

            }
        }
    }
    void saveIccid(Context context,String iccid){
        Set<String> set = PrefUtils.getInstance(context).getStringSet( UNSYNC_ICCIDS);
        if (set == null)
            set = new HashSet<>();
        set.add(iccid);
        PrefUtils.getInstance(context).saveStringSetPref( UNSYNC_ICCIDS, set);
        Intent intent = new Intent(BROADCAST_APPS_ACTION);
        intent.putExtra(KEY_DATABASE_CHANGE, "simSettings");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
