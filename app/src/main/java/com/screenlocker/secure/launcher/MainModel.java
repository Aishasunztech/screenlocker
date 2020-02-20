package com.screenlocker.secure.launcher;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.List;

import timber.log.Timber;

import static android.content.Context.ACTIVITY_SERVICE;

public class MainModel implements MainContract.MainMvpModel {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Context context;

    MainModel(Context context) {
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

        //i am sending the key from here to receiver
        //sending the broadcast to show the apps
        PrefUtils prefUtils = PrefUtils.getInstance(context);
        if (prefUtils.getStringPref( AppConstants.CURRENT_KEY) != null) {
            // from the guest part
            if (prefUtils.getStringPref( AppConstants.CURRENT_KEY).equalsIgnoreCase(AppConstants.KEY_GUEST_PASSWORD)) {
                intent.putExtra(AppConstants.BROADCAST_KEY, AppConstants.KEY_GUEST_PASSWORD);
            } else if (prefUtils.getStringPref( AppConstants.CURRENT_KEY).equalsIgnoreCase(AppConstants.KEY_MAIN_PASSWORD)) {    //  from the encrypted part
                intent.putExtra(AppConstants.BROADCAST_KEY, AppConstants.KEY_MAIN_PASSWORD);
            } else if (prefUtils.getStringPref( AppConstants.CURRENT_KEY).equalsIgnoreCase(AppConstants.KEY_SUPPORT_PASSWORD)) {
                intent.putExtra(AppConstants.BROADCAST_KEY, AppConstants.KEY_SUPPORT_PASSWORD);
            }
        } else {
            intent.putExtra(AppConstants.BROADCAST_KEY, "");
        }
        return intent;
    }

    /**
     * @param message this is the message to the broadcast catches when thrown from {@link MainActivity#(Bundle)} OR {@link com.screenlocker.secure.utils.Utils#sendMessageToActivity(String, Context)}
     * @param adapter adapter instance to populate the  list of apps we get from the package manager
     */
    @Override
    public void addDataToList(List<AppInfo> allDbApps,  String message, RAdapter adapter) {




        if (message != null && !message.equals("")) {
            if (allDbApps != null) {
                for (AppInfo model : allDbApps) {
                    switch (message) {
                        case AppConstants.KEY_GUEST_PASSWORD:
                            if (model.isGuest()) {
                                adapter.appsList.add(model);
                            }

                            break;
                        case AppConstants.KEY_SUPPORT_PASSWORD:
                            if (model.getUniqueName().equals(AppConstants.LIVE_CLIENT_CHAT_UNIQUE)) {
                                adapter.appsList.add(model);
                            } else if (model.getUniqueName().equals(AppConstants.SECURE_SETTINGS_UNIQUE)) {
                                adapter.appsList.add(model);
                            }
                            break;
                        case AppConstants.KEY_MAIN_PASSWORD:
                            // for the encrypted user typemessage
                            if (model.isEncrypted()) {
                                adapter.appsList.add(model);
                            }
                            break;
                    }
                }
            }
        }
         ((MainActivity) context).removeOverlay();
    }

}
