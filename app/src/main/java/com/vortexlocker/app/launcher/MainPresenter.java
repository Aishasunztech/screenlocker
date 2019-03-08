package com.vortexlocker.app.launcher;

import android.content.Intent;
import android.content.pm.PackageManager;

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
    public void addDataToList(PackageManager pm, String message, RAdapter adapter) {
        mvpModel.addDataToList(pm,  message,  adapter);
    }

//    public void setBackground(String message){
//        mvpView.setBackground();
//    }
}
