package com.screenlocker.secure.updateDB;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import timber.log.Timber;

import static com.screenlocker.secure.utils.CommonUtils.setSecureSettingsMenu;

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
            List<AppInfo> dbApps = MyApplication.getAppDatabase(applicationContext).getDao().getAppsForBlurWorker(false);
            Intent i = new Intent(Intent.ACTION_MAIN, null);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> allApps = pm.queryIntentActivities(i, 0);

            Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
            List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            String settingPackageName = resolveInfos.get(0).activityInfo.packageName + resolveInfos.get(0).loadLabel(pm);

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
                        app.setExtension(false);
                    } else {

                        app.setEncrypted(false);
                        app.setGuest(true);
                        app.setEnable(false);
                        app.setExtension(false);

                        if (app.getUniqueName().equals(settingPackageName)) {
                            app.setGuest(false);
                        }

                    }
                    MyApplication.getAppDatabase(applicationContext).getDao().insertApps(app);
                }

            }

            AppInfo appInfo = MyApplication.getAppDatabase(applicationContext).getDao().getParticularApp(AppConstants.SECURE_SETTINGS_UNIQUE);

            if (appInfo == null) {
                //Secure settings Extension
                Drawable wifi_drawable = applicationContext.getResources().getDrawable(R.drawable.settings_icon);
                byte[] secure_settings_icon = CommonUtils.convertDrawableToByteArray(wifi_drawable);
                AppInfo wifiExtension = new AppInfo("Secure Settings", AppConstants.SECURE_SETTINGS_PACKAGE, secure_settings_icon);
                wifiExtension.setUniqueName(wifiExtension.getPackageName() + wifiExtension.getLabel());
                wifiExtension.setExtension(true);
                wifiExtension.setGuest(true);
                wifiExtension.setEncrypted(true);
                wifiExtension.setEnable(true);
                MyApplication.getAppDatabase(applicationContext).getDao().insertApps(wifiExtension);
            }


            List<SubExtension> dbExtensions = MyApplication.getAppDatabase(applicationContext).getDao().getSubExtensions(AppConstants.SECURE_SETTINGS_UNIQUE);

            if (dbExtensions == null || dbExtensions.size() == 0) {
                //Secure settings Menu
                setSecureSettingsMenu(applicationContext);
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