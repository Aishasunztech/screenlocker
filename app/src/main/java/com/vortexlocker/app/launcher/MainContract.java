package com.vortexlocker.app.launcher;

import android.content.Intent;
import android.content.pm.PackageManager;

public interface MainContract {
    interface MainMvpView {
    }

    interface MainMvpPresenter {
        boolean isServiceRunning();

        void startLockService(Intent lockScreenIntent);

        Intent getSendingIntent();


        void addDataToList(PackageManager pm, String message, RAdapter adapter);
    }

    interface MainMvpModel {
        boolean isServiceRunning();

        void startLockService(Intent lockScreenIntent);

        Intent getSendingIntent();

        void addDataToList(PackageManager pm, String message, RAdapter adapter);
    }

}
