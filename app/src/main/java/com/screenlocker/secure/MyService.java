package com.screenlocker.secure;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.screenlocker.secure.service.LockScreenService;

public class MyService extends JobIntentService {

    public static final int JOB_ID = 0x01;


    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, MyService.class, JOB_ID, work);


    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        //

        Intent lockScreen = new Intent(MyService.this, LockScreenService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(lockScreen);
        } else {
            startService(lockScreen);
        }
    }
}
