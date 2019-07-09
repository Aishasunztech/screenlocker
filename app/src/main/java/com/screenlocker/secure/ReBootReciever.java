package com.screenlocker.secure;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.screenlocker.secure.offline.CheckExpiryFromSuperAdmin;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import timber.log.Timber;

import static android.content.Context.JOB_SCHEDULER_SERVICE;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.ONE_DAY_INTERVAL;
import static com.screenlocker.secure.utils.AppConstants.SIM_0_ICCID;
import static com.screenlocker.secure.utils.AppConstants.SIM_1_ICCID;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;

public class ReBootReciever extends BroadcastReceiver {
    private static final String TAG = ReBootReciever.class.getSimpleName();


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.tag(TAG).e("onReceive: triggered");

        PrefUtils.saveBooleanPref(context, AppConstants.REBOOT_STATUS, true);


        if (intent.getAction() != null)
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

                ComponentName componentName1 = new ComponentName(context, CheckExpiryFromSuperAdmin.class);

                JobInfo jobInfo1 = new JobInfo.Builder(1345, componentName1)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setPeriodic(ONE_DAY_INTERVAL)
                        .build();

                JobScheduler scheduler1 = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
                int resultCode1 = scheduler1.schedule(jobInfo1);
                if (resultCode1 == JobScheduler.RESULT_SUCCESS) {
                    Timber.d("Job Scheduled");
                } else {
                    Timber.d("Job Scheduled Failed");
                }

                String device_status = PrefUtils.getStringPref(context, DEVICE_STATUS);
                Timber.d("<<< device status >>>%S", device_status);

                if (PrefUtils.getBooleanPref(context, TOUR_STATUS)) {
                    if (device_status != null) {
                        Intent lockScreenIntent = new Intent(context, LockScreenService.class);
                        lockScreenIntent.setAction("reboot");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(lockScreenIntent);
                        } else {
                            context.startService(lockScreenIntent);
                        }
                    }
                }
                SubscriptionManager sm = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                PrefUtils.saveStringPref(context, AppConstants.KEY_SHUT_DOWN, AppConstants.VALUE_SHUT_DOWN_FALSE);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
                SubscriptionInfo si0 = sm.getActiveSubscriptionInfoForSimSlotIndex(0);
                SubscriptionInfo si1 = sm.getActiveSubscriptionInfoForSimSlotIndex(1);
                if (si0 != null) {
                    PrefUtils.saveStringPref(context, SIM_0_ICCID, si0.getIccId());
                    Log.d("onstatuschanged", "onReceive: " + si0.getIccId());
                } else
                    PrefUtils.saveStringPref(context, SIM_0_ICCID, null);
                if (si1 != null) {
                    PrefUtils.saveStringPref(context, SIM_1_ICCID, si1.getIccId());
                    Log.d("onstatuschanged", "onReceive: " + si1.getIccId());

                } else
                    PrefUtils.saveStringPref(context, SIM_1_ICCID, null);

            }


    }

}