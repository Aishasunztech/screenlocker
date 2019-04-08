package com.screenlocker.secure.launcher;

import android.app.ActivityManager;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.List;

import timber.log.Timber;

import static android.content.Context.ACTIVITY_SERVICE;
import static com.screenlocker.secure.launcher.MainActivity.removeOverlay;

public class MainModel implements MainContract.MainMvpModel {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Context context;

    public MainModel(Context context) {
        this.context = context;
    }


    /**
     * checks that the passed service object is running or not
     *
     * @return true if that passed service is running else false
     */

    @Override
    public boolean isServiceRunning() {

        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LockScreenService.class.getName().equals(service.service.getClassName())) {
                Timber.tag(TAG).i(String.valueOf(true));
                return true;
            }
        }
        Timber.i(String.valueOf(false));
        return false;
    }


    /**
     * start the service based on the passed intent
     *
     * @param lockScreenIntent
     */
    @Override
    public void startLockService(Intent lockScreenIntent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(lockScreenIntent);
        } else {
            context.startService(lockScreenIntent);
        }
    }

    /**
     * @return it returns the intent that will get broad cast and the reciever will catch it
     */
    @Override
    public Intent getSendingIntent() {
        Intent intent = new Intent(AppConstants.BROADCAST_ACTION);

        //i am sending the key from here to reciever
        //sending the broadcast to show the apps
        if (PrefUtils.getStringPref(context, AppConstants.CURRENT_KEY) != null) {
            // from the guest part
            if (PrefUtils.getStringPref(context, AppConstants.CURRENT_KEY).equalsIgnoreCase(AppConstants.KEY_GUEST_PASSWORD)) {
                intent.putExtra(AppConstants.BROADCAST_KEY, AppConstants.KEY_GUEST_PASSWORD);
            } else {    //  from the encrypted part
                intent.putExtra(AppConstants.BROADCAST_KEY, AppConstants.KEY_MAIN_PASSWORD);
            }
        } else {
            intent.putExtra(AppConstants.BROADCAST_KEY, "");
        }
        return intent;
    }

    /**
     * @param pm      package manager to get the list  of all apps and display it
     * @param message this is the message to the broadcast catches when thrown from {@link MainActivity#onCreate(Bundle)} OR {@link com.screenlocker.secure.utils.Utils#sendMessageToActivity(String, Context)}
     * @param adapter adapter instance to populate the  list of apps we get from the package manager
     */
    @Override
    public void addDataToList(PackageManager pm, String message, RAdapter adapter) {



        List<AppInfo> allDbApps = MyApplication.getAppDatabase(context).getDao().getApps();


        if (message != null && !message.equals("")) {
            if (allDbApps != null) {
                for (AppInfo model :
                        allDbApps) {
                    if (message.equals(AppConstants.KEY_GUEST_PASSWORD)) {
                        if (model.isGuest()) {
                            adapter.appsList.add(model);
                        }

                    } else {
                        // for the encrypted user type
                        if (model.isEncrypted()) {
                            adapter.appsList.add(model);
                        }
                    }
                }
            }
        } else {
            if (allDbApps != null)
                adapter.appsList.addAll(allDbApps);
        }

        AppExecutor.getInstance().getMainThread().execute(new Runnable() {
            @Override
            public void run() {
                removeOverlay();
            }
        });

     /*   Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> allApps = pm.queryIntentActivities(i, 0);
        // adding data to the model
        //getRunningApps(pm);
        for (ResolveInfo ri : allApps) {
            AppInfo app = new AppInfo(String.valueOf(ri.loadLabel(pm)), ri.activityInfo.packageName, ri.activityInfo.loadIcon(pm));
            if (message != null && !message.equals("")) {
                AppInfo dbApps = MyApplication.getAppDatabase(context).getDao().getParticularApp(app.getPackageName() + app.getLabel());
                if (message.equals(AppConstants.KEY_GUEST_PASSWORD)) {
                    // for the guest type user
                    if (dbApps != null) {
                        if (dbApps.isGuest()) {
                            adapter.appsList.add(app);
                        }
                    }

                } else {
                    // for the encrypted user type
                    if (dbApps != null) {
                        if (dbApps.isEncrypted()) {
                            adapter.appsList.add(app);
                        }
                    }
                }
            } else  // means that the user have not come from the lock screen so show all apps
                adapter.appsList.add(app);
        }*/
    }

    private void getRunningApps(PackageManager pm) {
        List<ApplicationInfo> packages;
        //get a list of installed apps.
        packages = pm.getInstalledApplications(0);
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ApplicationInfo packageInfo : packages) {
            if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) continue;
            if (packageInfo.packageName.equals(context.getPackageName())) continue;
            if (mActivityManager != null) {
                mActivityManager.killBackgroundProcesses(packageInfo.packageName);
            }
        }
    }

}
