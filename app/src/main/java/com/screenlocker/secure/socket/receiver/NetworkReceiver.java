package com.screenlocker.secure.socket.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.socket.SocketManager;
import com.screenlocker.secure.socket.interfaces.NetworkListener;
import com.screenlocker.secure.socket.model.InstallModel;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import java.lang.reflect.Type;
import java.util.HashMap;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.ACTION_PULL_APPS;
import static com.screenlocker.secure.utils.AppConstants.ACTION_PUSH_APPS;
import static com.screenlocker.secure.utils.AppConstants.APPS_HASH_MAP;
import static com.screenlocker.secure.utils.AppConstants.DELETE_HASH_MAP;

public class NetworkReceiver extends BroadcastReceiver {

    private final NetworkListener listener;

    public NetworkReceiver(NetworkListener listener) {
        this.listener = listener;
    }


    @Override
    public void onReceive(Context context, Intent intent) {


        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            if (noConnectivity) {
                if (listener != null)
                    listener.onNetworkChange(false);
            } else {
                if (listener != null)
                    listener.onNetworkChange(true);
            }
        } else if (intent.getAction() != null && intent.getAction().equals("com.secure.systemcontroll.PackageAdded")) {

            boolean status = intent.getBooleanExtra("status", false);
            String model = intent.getStringExtra("packageAdded");
            boolean isLast = intent.getBooleanExtra("isLast", false);
            InstallModel installModel = new Gson().fromJson(model, InstallModel.class);

            Timber.d("isLast %s", isLast);
            Timber.d("packageName %s", installModel.getPackage_name());


            if (status) {

                PackageManager pm = context.getPackageManager();

                try {
                    ApplicationInfo applicationInfo = pm.getApplicationInfo(installModel.getPackage_name(), 0);

                    Drawable ic = pm.getApplicationIcon(applicationInfo);
                    byte[] icon = CommonUtils.convertDrawableToByteArray(ic);
                    String label = pm.getApplicationLabel(applicationInfo).toString();
                    new Thread(() -> {
                        AppInfo appInfo = new AppInfo();
                        appInfo.setDefaultApp(false);
                        appInfo.setExtension(false);
                        appInfo.setEncrypted(installModel.isEncrypted());
                        appInfo.setGuest(installModel.isGuest());
                        appInfo.setEnable(installModel.isEnable());
                        appInfo.setLabel(label);
                        appInfo.setPackageName(installModel.getPackage_name());
                        appInfo.setUniqueName(installModel.getPackage_name() + label);
                        appInfo.setIcon(icon);
                        int i = MyApplication.getAppDatabase(context).getDao().updateApps(appInfo);
                        Timber.d("result :%s", i);

                        if (i == 0) {
                            MyApplication.getAppDatabase(context).getDao().insertApps(appInfo);
                        }

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

        } else if (intent.getAction() != null && intent.getAction().equals("com.secure.systemcontroll.PackageDeleted")) {

            Timber.d("packageDeleted");

            String aPackageName = intent.getStringExtra("package");
            Timber.d("package %s", aPackageName);

            String label = intent.getStringExtra("label");
            Timber.d("label %s", label);

            new Thread(() -> MyApplication.getAppDatabase(context).getDao().deleteOne(aPackageName + label)).start();
            if (intent.hasExtra("isLast")){
                Timber.d("ISLAST AVAILABLE : ");
            }

            boolean isLast = intent.getBooleanExtra("isLast", false);

            Timber.d("isLast : %s", isLast);

            if (SocketManager.getInstance().getSocket() != null && SocketManager.getInstance().getSocket().connected()) {
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
                Intent pulledIntent = intent.setAction(ACTION_PULL_APPS);
                pulledIntent.putExtra("PackageName", aPackageName);
                pulledIntent.putExtra("Status", true);

                if (isLast) {
                    pulledIntent.putExtra("finish_status", true);
                }

                localBroadcastManager.sendBroadcast(pulledIntent);
            } else {
                String hashMApGson = PrefUtils.getStringPref(context, DELETE_HASH_MAP);
                if (hashMApGson == null) {
                    HashMap<String, Boolean> h = new HashMap<>();
                    h.put(aPackageName, true);
                    h.put("isLastAvailable",isLast);
                    PrefUtils.saveStringPref(context, DELETE_HASH_MAP, new Gson().toJson(h));
                } else {
                    Type hashType = new TypeToken<HashMap<String, Boolean>>() {
                    }.getType();
                    HashMap<String, Boolean> h = new Gson().fromJson(hashMApGson, hashType);
                    h.put(aPackageName, true);
                    h.put("isLastAvailable",isLast);
                    PrefUtils.saveStringPref(context, DELETE_HASH_MAP, new Gson().toJson(h));
                }
            }


        }
    }


}
