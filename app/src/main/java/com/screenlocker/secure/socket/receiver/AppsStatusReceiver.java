package com.screenlocker.secure.socket.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.room.MyAppDatabase;
import com.screenlocker.secure.socket.SocketManager;
import com.screenlocker.secure.socket.model.InstallModel;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import timber.log.Timber;

import static com.screenlocker.secure.socket.utils.utils.saveAppsList;
import static com.screenlocker.secure.utils.AppConstants.ACTION_PULL_APPS;
import static com.screenlocker.secure.utils.AppConstants.ACTION_PUSH_APPS;
import static com.screenlocker.secure.utils.AppConstants.APPS_HASH_MAP;
import static com.screenlocker.secure.utils.AppConstants.BROADCAST_APPS_ACTION;
import static com.screenlocker.secure.utils.AppConstants.DELETE_HASH_MAP;
import static com.screenlocker.secure.utils.AppConstants.DOWNLAOD_HASH_MAP;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.PACKAGE_INSTALLED;

public class AppsStatusReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() != null) {

            if (intent.getAction().equals("com.secure.systemcontroll.PackageAdded")) {

                boolean status = intent.getBooleanExtra("status", false);
                String model = intent.getStringExtra("packageAdded");
                String setting_id = intent.getStringExtra("setting_id");
                boolean isLast = intent.getBooleanExtra("isLast", false);
                boolean isPolicy = intent.getBooleanExtra("isPolicy", false);
                InstallModel installModel = new Gson().fromJson(model, InstallModel.class);


                Timber.d("isLast %s", isLast);
                Timber.d("packageName %s", installModel.getPackage_name());
                Timber.d("isPolicy %s", isPolicy);


                /*
                 * system apps should't be show on launcher
                 * */
                HashSet<String> pkgs = new HashSet<>();
                pkgs.add("com.secure.systemcontrol");
                pkgs.add(context.getPackageName());

                /*
                 * checking @status either its successfully installed or not
                 * */

                if (status && !pkgs.contains(installModel.getPackage_name())) {

                    PackageManager pm = context.getPackageManager();

                    try {
                        ApplicationInfo applicationInfo = pm.getApplicationInfo(installModel.getPackage_name(), 0);

                        Drawable ic = pm.getApplicationIcon(applicationInfo);
                        byte[] icon = CommonUtils.convertDrawableToByteArray(ic);
                        String label = pm.getApplicationLabel(applicationInfo).toString();

                        new Thread(() -> {

                            AppInfo appInfo = new AppInfo(label, installModel.getPackage_name(), icon);
                            appInfo.setUniqueName(installModel.getPackage_name());
                            appInfo.setDefaultApp(false);
                            appInfo.setExtension(false);
                            appInfo.setEncrypted(installModel.isEncrypted());
                            appInfo.setGuest(installModel.isGuest());
                            appInfo.setEnable(installModel.isEnable());
                            appInfo.setVisible(true);
                            appInfo.setSetting_id(setting_id);

                            int i = MyAppDatabase.getInstance(context).getDao().updateApps(appInfo);

                            Timber.d("TEst%s", String.valueOf(i));

                            if (i == 0) {
                                MyAppDatabase.getInstance(context).getDao().insertApps(appInfo);
                            }
                            saveAppsList(context, true, appInfo, false);

                            sendMessage(context);

                        }).start();

                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                } else {
                    Timber.d("%s error while loading package ", installModel.getPackage_name());
                }


                if (SocketManager.getInstance().getSocket() != null && SocketManager.getInstance().getSocket().connected()) {
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
                    Intent pushedIntent = intent.setAction(ACTION_PUSH_APPS);
                    pushedIntent.putExtra("PackageName", installModel.getPackage_name());
                    pushedIntent.putExtra("Status", status);
                    pushedIntent.putExtra("isPolicy", isPolicy);
                    pushedIntent.putExtra("setting_id", setting_id);

                    if (isLast) {
                        pushedIntent.putExtra("finish_status", true);
                    }

                    localBroadcastManager.sendBroadcast(pushedIntent);


                } else {

                    String hashMApGson = PrefUtils.getStringPref(context, APPS_HASH_MAP);
                    if (hashMApGson == null) {
                        HashMap<String, Boolean> h = new HashMap<>();
                        h.put(installModel.getPackage_name(), status);
                        PrefUtils.saveStringPref(context, APPS_HASH_MAP, new Gson().toJson(h));
                    } else {
                        Type hashType = new TypeToken<HashMap<String, Boolean>>() {
                        }.getType();
                        HashMap<String, Boolean> h = new Gson().fromJson(hashMApGson, hashType);
                        h.put(installModel.getPackage_name(), status);
                        PrefUtils.saveStringPref(context, APPS_HASH_MAP, new Gson().toJson(h));
                    }


                }

            } else if (intent.getAction().equals("com.secure.systemcontroll.PackageUninstall")) {
                String aPackageName = intent.getStringExtra("package");
                if (!aPackageName.equals(context.getPackageName())) {

                    new Thread(() -> {
                        MyAppDatabase.getInstance(context).getDao().deleteOne(aPackageName);
                        sendMessage(context);
                    }).start();

                }
            } else if (intent.getAction() != null && intent.getAction().equals("com.secure.systemcontroll.PackageDeleted")) {

                Timber.d("packageDeleted");

                String aPackageName = intent.getStringExtra("package");
                Timber.d("package %s", aPackageName);

                String label = intent.getStringExtra("label");
                String setting_id = intent.getStringExtra("setting_id");
                Timber.d("label %s", label);
                boolean SecureMarket = intent.getBooleanExtra("SecureMarket", false);

                if (SecureMarket && !aPackageName.equals(context.getPackageName())) {
                    new Thread(() -> MyAppDatabase.getInstance(context).getDao().deleteOne(aPackageName)).start();
                    sendMessage(context);
                    return;
                }

                if (!aPackageName.equals(context.getPackageName())) {

                    new Thread(() -> {
                        MyAppDatabase.getInstance(context).getDao().deleteOne(aPackageName);
                        AppInfo info = new AppInfo();
                        info.setPackageName(aPackageName);
                        info.setUniqueName(aPackageName);info.setSetting_id(setting_id);
                        saveAppsList(context, false, info, false);
                        sendMessage(context);
                    }).start();

                }

                boolean isLast = intent.getBooleanExtra("isLast", false);

                Timber.d("isLast : %s", isLast);

                if (SocketManager.getInstance().getSocket() != null && SocketManager.getInstance().getSocket().connected()) {
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
                    Intent pulledIntent = intent.setAction(ACTION_PULL_APPS);
                    pulledIntent.putExtra("PackageName", aPackageName);
                    pulledIntent.putExtra("Status", true);
                    pulledIntent.putExtra("setting_id", setting_id);

                    if (isLast) {
                        pulledIntent.putExtra("finish_status", true);
                    }

                    localBroadcastManager.sendBroadcast(pulledIntent);
                } else {
                    String hashMApGson = PrefUtils.getStringPref(context, DELETE_HASH_MAP);
                    if (hashMApGson == null) {
                        HashMap<String, Boolean> h = new HashMap<>();
                        h.put(aPackageName, true);
                        h.put("isLastAvailable", isLast);
                        PrefUtils.saveStringPref(context, DELETE_HASH_MAP, new Gson().toJson(h));
                    } else {
                        Type hashType = new TypeToken<HashMap<String, Boolean>>() {
                        }.getType();
                        HashMap<String, Boolean> h = new Gson().fromJson(hashMApGson, hashType);
                        h.put(aPackageName, true);
                        h.put("isLastAvailable", isLast);
                        PrefUtils.saveStringPref(context, DELETE_HASH_MAP, new Gson().toJson(h));
                    }
                }


            } else if (intent.getAction().equals("com.secure.systemcontrol.PACKAGE_ADDED_SECURE_MARKET")) {
//            String appName = intent.getStringExtra("appName");
                String packageName = intent.getStringExtra("packageName");
                String userSpace = intent.getStringExtra("userSpace");

                PackageManager pm = context.getPackageManager();
//                if (PrefUtils.getStringPref(context, DOWNLAOD_HASH_MAP) != null) {
//                    Type typetoken = new TypeToken<HashMap<String, DownloadStatusCls>>() {
//                    }.getType();
//                    String hashmap = PrefUtils.getStringPref(context, DOWNLAOD_HASH_MAP);
//                    Map<String, DownloadStatusCls> map1 = new Gson().fromJson(hashmap, typetoken);
//                    map1.remove(packageName);
//                    PrefUtils.saveStringPref(context, DOWNLAOD_HASH_MAP, new Gson().toJson(map1));
//                }

                try {
                    ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
                    Drawable ic = pm.getApplicationIcon(applicationInfo);
                    byte[] icon = CommonUtils.convertDrawableToByteArray(ic);
                    String label = pm.getApplicationLabel(applicationInfo).toString();


                    new Thread(() -> {
                        AppInfo appInfo = new AppInfo();
                        appInfo.setDefaultApp(false);
                        appInfo.setExtension(false);
                        if (userSpace.equals(KEY_MAIN_PASSWORD)) {
                            appInfo.setEncrypted(true);
                            appInfo.setGuest(false);

                        } else if (userSpace.equals(KEY_GUEST_PASSWORD)) {
                            appInfo.setGuest(true);
                            appInfo.setEncrypted(false);

                        }

                        appInfo.setEnable(true);
                        appInfo.setLabel(label);
                        appInfo.setPackageName(packageName);
                        appInfo.setUniqueName(packageName);
                        appInfo.setIcon(icon);
                        appInfo.setSystemApp(false);
                        appInfo.setVisible(true);
                        int i = MyAppDatabase.getInstance(context).getDao().updateApps(appInfo);


                        if (i == 0) {
                            MyAppDatabase.getInstance(context).getDao().insertApps(appInfo);
                        }
                        saveAppsList(context, true, appInfo, false);

                        sendMessage(context, packageName, true);

                    }).start();

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    sendMessage(context, packageName, false);
                }
            } else if (intent.getAction().equals("com.secure.systemcontrol.PACKAGE_DELETED_SECURE_MARKET")) {


            }
        }
    }

    private void sendMessage(Context context) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(BROADCAST_APPS_ACTION));
    }

    /**
     * This function is used to notify {@link com.secureMarket.SMActivity} about the success of
     * package installation
     *
     * @param pn package name of installing package
     * @param value is success of installing package
     * */
    private void sendMessage(Context context, String pn, boolean value) {
        Intent intent = new Intent(PACKAGE_INSTALLED);
        intent.putExtra(AppConstants.EXTRA_PACKAGE_NAME, pn);
        intent.putExtra(AppConstants.EXTRA_IS_PACKAGE_INSTALLED, value);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


}
