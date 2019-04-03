package com.screenlocker.secure.appSelection;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.View;
import android.widget.ProgressBar;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;

public class SelectionModel implements SelectionContract.SelectionMvpModel {
    private Context context;

    public SelectionModel(Context context) {

        this.context = context;
    }

    @Override
    public List<ResolveInfo> getTheAppListFromSystem(PackageManager packageManager) {

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        return packageManager.queryIntentActivities(i, 0);
    }

    @Override
    public void showProgress(ProgressBar progress) {
        if (progress != null)
            progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress(ProgressBar progress) {
        if (progress != null && progress.isShown())
            progress.setVisibility(View.GONE);
    }

    @Override
    public void addAppsToList(PackageManager packageManager,
                              ArrayList<AppInfo> appsList, List<AppInfo> dbApps) {
//                    appSelected.clear();
//                for (int i = 0; i < apps.size(); i++) {
//                   adapter appSelected.add(apps.get(i).getPackageName());
//                }
        //List<ResolveInfo> appsFromSystem = getTheAppListFromSystem(packageManager);
        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String settingPackageName = null;
        if (resolveInfos != null || resolveInfos.size() != 0) {

            settingPackageName = resolveInfos.get(0).activityInfo.packageName + String.valueOf(resolveInfos.get(0).loadLabel(packageManager));
        }

        boolean allDisable = true;
        boolean allGuest = true;
        boolean allEncrypted = true;
        String projectPrimaryKey = context.getPackageName() + context.getString(R.string.app_name);


        for (int i = 0; i < dbApps.size(); i++) {
            AppInfo appInfo = dbApps.get(i);

            if (settingPackageName == null || !appInfo.getUniqueName().equals(settingPackageName)) {
                appsList.add(appInfo);
            }

            if (allDisable) {
                if (appInfo.isEnable()) {
                    allDisable = true;
                } else {
                    if ((settingPackageName != null && appInfo.getUniqueName().equals(settingPackageName))
                            || (appInfo.getUniqueName().equals(projectPrimaryKey)))
                        allDisable = true;
                    else
                        allDisable = false;
                }
            }
            if (allGuest) {
                if (appInfo.isGuest()) {
                    allGuest = true;
                } else {
                    if (settingPackageName != null && appInfo.getUniqueName().equals(settingPackageName))
                        allGuest = true;
                    else
                        allGuest = false;
                }
            }

            if (allEncrypted) {
                if (appInfo.isEncrypted()) {
                    allEncrypted = true;
                } else {
                    if ((settingPackageName != null && appInfo.getUniqueName().equals(settingPackageName))
                            || (appInfo.getUniqueName().equals(projectPrimaryKey)))
                        allEncrypted = true;
                    else
                        allEncrypted = false;
                }
            }

        }

        PrefUtils.saveBooleanPref(context, AppConstants.KEY_GUEST_ALL, allGuest);
        PrefUtils.saveBooleanPref(context, AppConstants.KEY_ENCRYPTED_ALL, allEncrypted);
        PrefUtils.saveBooleanPref(context, AppConstants.KEY_DISABLE_ALL, allDisable);


    }

    @Override
    public AppInfo isAppPresentInDB(String primaryKey) {
        return MyApplication.getAppDatabase(context).getDao().getParticularApp(primaryKey);
    }


    @Override
    public void insertAppsInDB(AppInfo model) {
        MyApplication.getAppDatabase(context).getDao().insertApps(model);
    }

    @Override
    public void updateAppInDB(AppInfo appInfo) {
        MyApplication.getAppDatabase(context).getDao().updateApps(appInfo);
    }


    @Override
    public void deleteAppFromDB(String primaryKey) {
        MyApplication.getAppDatabase(context).getDao().deleteOne(primaryKey);
    }


}
