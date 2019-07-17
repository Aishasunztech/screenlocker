package com.screenlocker.secure.launcher;

import android.content.Intent;
import android.content.pm.PackageManager;

import java.util.List;

public interface MainContract {
    interface MainMvpView {
    }

    interface MainMvpPresenter {
        boolean isServiceRunning();

        void startLockService(Intent lockScreenIntent);

        Intent getSendingIntent();


        void addDataToList(List<AppInfo> allDbApps, String message, RAdapter adapter);
    }

    interface MainMvpModel {
        boolean isServiceRunning();

        void startLockService(Intent lockScreenIntent);

        Intent getSendingIntent();

        void addDataToList(List<AppInfo> allDbApps, String message, RAdapter adapter);
    }

}
