package com.screenlocker.secure.manual_load;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.socket.model.InstallModel;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.INSTALLED_APP;
import static com.screenlocker.secure.utils.AppConstants.IS_SETTINGS_ALLOW;
import static com.screenlocker.secure.utils.AppConstants.KEY_SUPPORT_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.PULL_APPS;
import static com.screenlocker.secure.utils.AppConstants.PUSH_APPS;
import static com.screenlocker.secure.utils.AppConstants.SHOW_MANUAL_ACTIVITY;
import static com.screenlocker.secure.utils.AppConstants.SYSTEM_APP;
import static com.screenlocker.secure.utils.AppConstants.UNINSTALL_ALLOWED;

public class ManualPullPush extends BaseActivity implements ManualPushPullAdapter.PushPullAppsListener {

    @BindView(R.id.recyclerViewManualList)
    RecyclerView recyclerView;
    ManualPushPullAdapter manualPushPullAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_pull_push);
        ButterKnife.bind(this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        Timber.d("<<< ManualPushPull >>>");
    }

    private ArrayList<InstallModel> filterApps() {

        ArrayList<InstallModel> list = utils.getArrayList(ManualPullPush.this);

        utils.saveArrayList(null, this);

        Timber.d("<<============ ALL APPS ==============>>>");
        if (list != null && list.size() > 0) {
            for (InstallModel model : list) {
                Timber.d(model.getPackage_name());
            }
        }

        Timber.d("<<============= PUSH APPS TO INSERT IN TO DB ===================>>");

        if (list != null && list.size() > 0) {

            List<InstallModel> pushDbApps = list.stream().filter(model -> (model.getType().equals(PUSH_APPS) && model.getApk_uri() != null) && isPackageInstalled(model.getPackage_name(), getPackageManager()) && !isSystemApp(model.getPackage_name())).collect(Collectors.toList());

            if (pushDbApps != null && pushDbApps.size() > 0) {
                for (int i = 0; i < pushDbApps.size(); i++) {
                    sendBroadcast(this, true, pushDbApps.get(i), i == pushDbApps.size() - 1, true);
                    Timber.d(pushDbApps.get(i).getPackage_name());
                }
            }

        }

        Timber.d("<<============= PULLL APPS TO DELETE FROM DB ===================>>");

        if (list != null && list.size() > 0) {

            List<InstallModel> pullApps = list.stream().filter(model -> model.getType().equals(PULL_APPS) && !isPackageInstalled(model.getPackage_name(), getPackageManager()) && !isSystemApp(model.getPackage_name())).collect(Collectors.toList());

            if (pullApps != null && pullApps.size() > 0) {
                for (int i = 0; i < pullApps.size(); i++) {
                    sendDelBroadCast(this, false, pullApps.get(i), i == pullApps.size() - 1, "", pullApps.get(i).getPackage_name(), false);
                    Timber.d(pullApps.get(i).getPackage_name());
                }
            }

        }

        Timber.d("<<=============== PUSH APPS ==============>>");


        if (list != null && list.size() > 0) {
            List<InstallModel> pushApps = list.stream().filter(model -> (model.getType().equals(PUSH_APPS)) && model.getApk_uri() != null && !isPackageInstalled(model.getPackage_name(), getPackageManager()) && !isSystemApp(model.getPackage_name())).collect(Collectors.toList());
            if (pushApps != null && pushApps.size() > 0) {
                for (InstallModel model : pushApps) {
                    Timber.d(model.getPackage_name());
                }

                ArrayList<InstallModel> push = utils.getArrayList(this);

                if (push == null) {
                    push = new ArrayList<>(pushApps);
                } else {
                    push.addAll(pushApps);
                }

                utils.saveArrayList(push, this);

            }
        } else {
            Timber.d("<<===================== DATA NOT FOUND ======================>>");
        }


        Timber.d("<<=============== PULL APPS ==============>>");


        if (list != null && list.size() > 0) {
            List<InstallModel> pullApps = list.stream().filter(model -> (model.getType().equals(PULL_APPS)) && !model.getPackage_name().equals(getPackageName()) && !isSystemApp(model.getPackage_name()) && isPackageInstalled(model.getPackage_name(), getPackageManager())).collect(Collectors.toList());
            if (pullApps != null && pullApps.size() > 0) {
                for (InstallModel model : pullApps) {
                    Timber.d(model.getPackage_name());
                }

                ArrayList<InstallModel> pull = utils.getArrayList(this);

                if (pull == null) {
                    pull = new ArrayList<>(pullApps);
                } else {
                    pull.addAll(pullApps);
                }

                utils.saveArrayList(pull, this);
            }
        } else {
            Timber.d("<<===================== DATA NOT FOUND ======================>>");
        }

        return utils.getArrayList(this);
    }


    private boolean isSystemApp(String packageName) {

        ApplicationInfo info = null;
        try {
            info = getPackageManager().getApplicationInfo(packageName, 0);
            //Non-system app
            //System app
            return !info.sourceDir.startsWith("/data/app/");
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    //send Intent with success status of package push request
    private void sendBroadcast(Context context, boolean status, InstallModel model, boolean isLast, boolean insertApp) {

        Timber.d(model.getPackage_name());

        Intent appInstalled = new Intent();
        appInstalled.setComponent(new ComponentName(context.getPackageName(), "com.screenlocker.secure.socket.receiver.AppsStatusReceiver"));
        appInstalled.setAction("com.secure.systemcontroll.PackageAdded");
        appInstalled.putExtra("packageAdded", new Gson().toJson(model));
        appInstalled.putExtra("status", status);
        appInstalled.putExtra("isPolicy", model.isPolicy());
        appInstalled.putExtra("isLast", isLast);
        appInstalled.putExtra("insertApp", insertApp);
        context.sendBroadcast(appInstalled);
    }


    private void sendDelBroadCast(Context context, boolean secureMarket, InstallModel model, boolean isLast, String label, String packageName, boolean sendStatus) {
        //package already uninstall
        Intent delIntent = new Intent();
        delIntent.setComponent(new ComponentName(context.getPackageName(), "com.screenlocker.secure.socket.receiver.AppsStatusReceiver"));
        delIntent.setAction("com.secure.systemcontroll.PackageDeleted");
        delIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        //package name of deleted package
        delIntent.putExtra("package", packageName);
        delIntent.putExtra("model", new Gson().toJson(model));
        // isLast specifies if this is the last package of requested delete packages list
        // for notifying LM About operation
        delIntent.putExtra("isLast", isLast);
        delIntent.putExtra("sendStatus", sendStatus);
        //Label of deleting package (important for deleting record from local database)
        delIntent.putExtra("label", label);
        delIntent.putExtra("SecureMarket", secureMarket);
        context.sendBroadcast(delIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Timber.d("<<< ONRESUME >>>");

        ArrayList<InstallModel> appsList = filterApps();

        if (appsList != null && appsList.size() > 0) {
            Timber.d(" AppsList Size : %s", appsList.size());
            setRecyclerAdapter(appsList);
            PrefUtils.saveBooleanPref(this, UNINSTALL_ALLOWED, true);

        } else {
            Timber.d("Completed Manual Push Pull");
            PrefUtils.saveBooleanPref(ManualPullPush.this, SHOW_MANUAL_ACTIVITY, false);
            PrefUtils.saveBooleanPref(this, UNINSTALL_ALLOWED, false);
            finish();
        }
    }

    private void setRecyclerAdapter(ArrayList<InstallModel> list) {
        Timber.d("<<< Setting Resycler View >>>");
        if (recyclerView != null) {
            manualPushPullAdapter = new ManualPushPullAdapter(ManualPullPush.this, list, this);
            recyclerView.setAdapter(manualPushPullAdapter);
            recyclerView.setHasFixedSize(true);
        }
    }


    @Override
    public void appTextButtonClick(int position, InstallModel installModel) {

        Timber.d("<< appTextButtonClick >>>");

        if (installModel.getType().equals(PUSH_APPS)) {
            Timber.d(installModel.getApk_uri());
            appInstallRequest(installModel.getApk_uri());
        } else if (installModel.getType().equals(PULL_APPS)) {
            appUninstallRequest(installModel.getPackage_name());
        }


    }


    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        boolean found = true;


        try {
            packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            found = false;
        }

        return found;
    }

    private void appUninstallRequest(String app_pkg_name) {
        Timber.d("UNINSTALL PACKAGE " + app_pkg_name);

        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.setData(Uri.parse("package:" + app_pkg_name));
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        startActivity(intent);
    }

    private void appInstallRequest(String app_uri) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (getPackageManager().canRequestPackageInstalls()) {
                Intent intent = ShareCompat.IntentBuilder.from(ManualPullPush.this)
                        .setStream(Uri.parse(app_uri))
                        .setText("text/html")
                        .getIntent()
                        .putExtra(Intent.EXTRA_RETURN_RESULT, true)
                        .setAction(Intent.ACTION_VIEW)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .setDataAndType(Uri.parse(app_uri), "application/vnd.android.package-archive");
                startActivity(intent);
            } else {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName()));
                intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                startActivity(intent);

            }
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(app_uri),
                    "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {

        ArrayList<InstallModel> list = utils.getArrayList(ManualPullPush.this);
        if (list != null) {
            if (list.size() > 0) {
                Toast.makeText(this, "Please Complete Operation", Toast.LENGTH_SHORT).show();
            } else {
                finish();
            }
        } else {
            super.onBackPressed();
        }
    }
//    public void code_secret(View view) {
//        utils.saveArrayList(null, this);
//        finish();
//    }


}
