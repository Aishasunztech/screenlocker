package com.screenlocker.secure.mdm;

import android.app.job.JobParameters;
import android.app.job.JobService;

/**
 * @author Muhammad Nadeem
 * @Date 6/24/2019.
 */
public class LinkStatusJobScheduler extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
