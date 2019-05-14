package com.screenlocker.secure.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Intent;

public class UpdateTriggerService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        final Intent intent = new Intent();
        intent.setAction("com.secure.systemcontrol.CHECK_FOR_UPDATE");
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.putExtra("packageName", getPackageName());
        intent.setComponent(new ComponentName("com.secure.systemcontrol", "com.secure.systemcontrol.PackagesInstallReceiver"));
        sendBroadcast(intent);

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
