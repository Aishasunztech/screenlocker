package com.screenlocker.secure.appSelection;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.ProgressBar;

import com.screenlocker.secure.launcher.AppInfo;

import java.util.ArrayList;
import java.util.List;

interface SelectionContract {
    interface SelectionMvpModel {
        List<ResolveInfo> getTheAppListFromSystem(PackageManager packageManager);

        void showProgress(ProgressBar progress);

        void hideProgress(ProgressBar progress);

        void addAppsToList(PackageManager packageManager, ArrayList<AppInfo> appsList,List<AppInfo> apps);

        AppInfo isAppPresentInDB(String primaryKey);

        void insertAppsInDB(AppInfo model);

        void deleteAppFromDB(String primaryKey);






        void updateAppInDB(AppInfo appInfo);


    }

    interface SelectionMvpPresenter {
        List<ResolveInfo> getTheAppListFromSystem(PackageManager packageManager);

        void showProgress(ProgressBar progress);

        void hideProgress(ProgressBar progress);

        void addAppsToList(PackageManager packageManager, ArrayList<AppInfo> appsList,List<AppInfo> apps);

        AppInfo isAppPresentInDB(String primaryKey);


        void insertAppsInDB(AppInfo model);


        void deleteAppFromDB(String primaryKey);


        void updateAppInDB(AppInfo appInfo);


    }

    interface SelectionMvpView {

    }
}
