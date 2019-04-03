package com.screenlocker.secure.appSelection;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.ProgressBar;

import com.screenlocker.secure.launcher.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class SelectionPresenter implements SelectionContract.SelectionMvpPresenter {
    private SelectionContract.SelectionMvpView mvpView;
    private SelectionContract.SelectionMvpModel mvpModel;

    public SelectionPresenter(SelectionContract.SelectionMvpView mvpView, SelectionContract.SelectionMvpModel mvpModel) {
        this.mvpView = mvpView;
        this.mvpModel = mvpModel;
    }

    @Override
    public List<ResolveInfo> getTheAppListFromSystem(PackageManager packageManager) {
        return mvpModel.getTheAppListFromSystem(packageManager);
    }

    @Override
    public void showProgress(ProgressBar progress) {
        mvpModel.showProgress(progress);
    }

    @Override
    public void hideProgress(ProgressBar progress) {
        mvpModel.hideProgress(progress);
    }

    @Override
    public void addAppsToList(PackageManager packageManager, ArrayList<AppInfo> appsList,List<AppInfo> apps ) {
        mvpModel.addAppsToList(packageManager,appsList,apps);
    }

    @Override
    public AppInfo isAppPresentInDB(String primaryKey) {
        return mvpModel.isAppPresentInDB(primaryKey);
    }



    @Override
    public void insertAppsInDB(AppInfo model) {
        mvpModel.insertAppsInDB(model);
    }

    @Override
    public void updateAppInDB(AppInfo appInfo) {
        mvpModel.updateAppInDB(appInfo);
    }





    @Override
    public void deleteAppFromDB(String primaryKey) {
        mvpModel.deleteAppFromDB(primaryKey);
    }



}
