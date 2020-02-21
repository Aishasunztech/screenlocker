package com.screenlocker.secure.launcher;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.room.MyAppDatabase;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.List;

import timber.log.Timber;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * @author Muhammad Nadeem
 * @Date 7/10/2019.
 */
public class MainViewModel extends AndroidViewModel {

    private LiveData<List<AppInfo>> mAppInfos;
    private LiveData<Integer> mUnReadCount;

    public MainViewModel(@NonNull Application application) {
        super(application);
        mAppInfos = MyAppDatabase.getInstance(application).getDao().getAllApps();
        mUnReadCount = MyAppDatabase.getInstance(application).getDao().getUnSeenCount();
    }

    LiveData<List<AppInfo>> getAllApps() {
        return mAppInfos;
    }

    public LiveData<Integer> getmUnReadCount() {
        return mUnReadCount;
    }

    public Intent getSendingIntent() {
        Intent intent = new Intent(AppConstants.BROADCAST_ACTION);

        //i am sending the key from here to receiver
        //sending the broadcast to show the apps
        PrefUtils prefUtils = PrefUtils.getInstance(MyApplication.getAppContext());
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
//                            else if (model.getPackageName().equals(context.getPackageName())){
//                                adapter.appsList.add(model);
//                            }
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
    }
    public void startLockService(Context context,Intent lockScreenIntent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(lockScreenIntent);
        } else {
            context.startService(lockScreenIntent);
        }
    }

    public boolean isServiceRunning(Context context) {

        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LockScreenService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        Timber.i(String.valueOf(false));
        return false;
    }
}
