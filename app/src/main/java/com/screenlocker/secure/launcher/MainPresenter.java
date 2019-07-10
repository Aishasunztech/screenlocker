package com.screenlocker.secure.launcher;

import android.content.Intent;
import android.content.pm.PackageManager;

import java.util.List;

public class MainPresenter implements MainContract.MainMvpPresenter
{
    private MainContract.MainMvpView mvpView;
    private MainContract.MainMvpModel mvpModel;

    public MainPresenter(MainContract.MainMvpView mvpView, MainContract.MainMvpModel mvpModel) {
        this.mvpView = mvpView;

        this.mvpModel = mvpModel;
    }

    @Override
    public boolean isServiceRunning() {
        return mvpModel.isServiceRunning();
    }

    @Override
    public void startLockService(Intent lockScreenIntent) {
         mvpModel.startLockService(lockScreenIntent);
    }

    @Override
    public Intent getSendingIntent() {
        return mvpModel.getSendingIntent();
    }

    @Override
    public void addDataToList(List<AppInfo> allDbApps, String message, RAdapter adapter) {
        mvpModel.addDataToList( allDbApps, message,  adapter);
    }
}
