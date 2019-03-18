package com.vortexlocker.app.updateDB;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import com.vortexlocker.app.R;
import com.vortexlocker.app.app.MyApplication;
import com.vortexlocker.app.launcher.AppInfo;
import com.vortexlocker.app.utils.CommonUtils;

import java.util.List;

import androidx.work.Worker;
import androidx.work.WorkerParameters;
import timber.log.Timber;

public class BlurWorker extends Worker {
    public BlurWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    private static final String TAG = BlurWorker.class.getSimpleName();

    @NonNull
    @Override
    public Worker.Result doWork() {

        Context applicationContext = getApplicationContext();

        try {
            PackageManager pm = applicationContext.getPackageManager();
            List<AppInfo> dbApps = MyApplication.getAppDatabase(applicationContext).getDao().getApps();
            Intent i = new Intent(Intent.ACTION_MAIN, null);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> allApps = pm.queryIntentActivities(i, 0);
            // adding data to the model
            //getRunningApps(pm);
            for (int j = 0; j < allApps.size(); j++) {
                ResolveInfo ri = allApps.get(j);
                AppInfo app = new AppInfo(String.valueOf(ri.loadLabel(pm)),
                        ri.activityInfo.packageName, CommonUtils.convertDrawableToByteArray(ri.activityInfo.loadIcon(pm)));
                app.setUniqueName(app.getPackageName() + app.getLabel());
//                Glide.with(applicationContext)
//                        .load(app.getIcon())
//                        .apply(new RequestOptions().centerCrop())
//                        .into(viewHolder.img);
                if (!dbApps.contains(app)) {

                    app.setGuest(false);

                    // own app && uem app
                    if (app.getUniqueName().equals(applicationContext.getPackageName() + applicationContext.getString(R.string.app_name)) || app.getPackageName().equals("com.rim.mobilefusion.client")) {
                        app.setEncrypted(true);
                        app.setEnable(true);
                    } else {
                        app.setEncrypted(false);
                        app.setGuest(true);
                        app.setEnable(false);
                    }

                    MyApplication.getAppDatabase(applicationContext).getDao().insertApps(app);
                }


            }


            return Worker.Result.success();
        } catch (Throwable throwable) {

            // Technically WorkManager will return Worker.Result.FAILURE
            // but it's best to be explicit about it.
            // Thus if there were errors, we're return FAILURE
            Timber.tag(TAG).e(throwable, "Error applying blur");
            return Worker.Result.failure();
        }
    }
}