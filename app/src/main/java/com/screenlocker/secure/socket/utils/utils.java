package com.screenlocker.secure.socket.utils;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.listener.OnAppsRefreshListener;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.service.CheckUpdateService;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.socket.SocketManager;
import com.screenlocker.secure.socket.interfaces.GetApplications;
import com.screenlocker.secure.socket.interfaces.GetExtensions;
import com.screenlocker.secure.socket.model.InstallModel;
import com.screenlocker.secure.socket.model.InstalledAndRemainingApps;
import com.screenlocker.secure.socket.model.Settings;
import com.screenlocker.secure.socket.receiver.DeviceStatusReceiver;
import com.screenlocker.secure.socket.service.SocketService;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static android.content.Context.DEVICE_POLICY_SERVICE;
import static android.content.Context.JOB_SCHEDULER_SERVICE;
import static com.screenlocker.secure.mdm.utils.DeviceIdUtils.isValidImei;
import static com.screenlocker.secure.utils.AppConstants.APPS_LIST;
import static com.screenlocker.secure.utils.AppConstants.APPS_SENT_STATUS;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.DEFAULT_GUEST_PASS;
import static com.screenlocker.secure.utils.AppConstants.DEFAULT_MAIN_PASS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS_CHANGE_RECEIVER;
import static com.screenlocker.secure.utils.AppConstants.EXTENSIONS_SENT_STATUS;
import static com.screenlocker.secure.utils.AppConstants.IMEI1;
import static com.screenlocker.secure.utils.AppConstants.IMEI2;
import static com.screenlocker.secure.utils.AppConstants.INSTALLED_APPS;
import static com.screenlocker.secure.utils.AppConstants.INSTALLED_PACKAGES;
import static com.screenlocker.secure.utils.AppConstants.IS_SYNCED;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_SUPPORT_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.LOCK_SCREEN_STATUS;
import static com.screenlocker.secure.utils.AppConstants.LOGIN_ATTEMPTS;
import static com.screenlocker.secure.utils.AppConstants.OFFLINE_DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.ONE_DAY_INTERVAL;
import static com.screenlocker.secure.utils.AppConstants.SEND_INSTALLED_APPS;
import static com.screenlocker.secure.utils.AppConstants.SEND_UNINSTALLED_APPS;
import static com.screenlocker.secure.utils.AppConstants.SETTINGS_SENT_STATUS;
import static com.screenlocker.secure.utils.AppConstants.TIME_REMAINING;
import static com.screenlocker.secure.utils.AppConstants.TIME_REMAINING_REBOOT;
import static com.screenlocker.secure.utils.AppConstants.TOKEN;
import static com.screenlocker.secure.utils.AppConstants.UNINSTALLED_APPS;
import static com.screenlocker.secure.utils.AppConstants.UNINSTALLED_PACKAGES;
import static com.screenlocker.secure.utils.AppConstants.UPDATESIM;
import static com.screenlocker.secure.utils.AppConstants.UPDATE_JOB;
import static com.screenlocker.secure.utils.AppConstants.VALUE_EXPIRED;
import static com.screenlocker.secure.utils.Utils.sendMessageToActivity;

public class utils {


    public static boolean passwordsOk(Context context, String key) {

        String guest = PrefUtils.getStringPref(context, AppConstants.KEY_GUEST_PASSWORD);
        String main = PrefUtils.getStringPref(context, AppConstants.KEY_MAIN_PASSWORD);
        String code = PrefUtils.getStringPref(context, AppConstants.KEY_CODE_PASSWORD);
        String duress = PrefUtils.getStringPref(context, AppConstants.KEY_DURESS_PASSWORD);

        if (guest != null) {
            if (guest.equals(key)) {
                return false;
            }
        }

        if (main != null) {
            if (main.equals(key)) {
                return false;
            }

        }

        if (code != null) {
            if (code.equals(key)) {
                return false;
            }
        }

        if (duress != null) {
            return !duress.equals(key);

        }

        return true;
    }


    public static void wipeDevice(Context context) {
        ComponentName compName = new ComponentName(context, MyAdmin.class);
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(DEVICE_POLICY_SERVICE);

        if (devicePolicyManager != null) {
            boolean adminActive = devicePolicyManager.isAdminActive(compName);
            if (adminActive) {
                devicePolicyManager.wipeData(0);


            }
        }

    }


    public static void updateAppsList(final Context context, final JSONArray apps, final GetApplications listener, boolean isPolicy) {

        new Thread() {
            @Override
            public void run() {
                super.run();

                if (isPolicy) {
//                    MyApplication.getAppDatabase(context).getDao().updateAllApps(false, false, false);
                }

                for (int i = 0; i < apps.length(); i++) {

                    try {

                        JSONObject app = apps.getJSONObject(i);
                        boolean guest = (boolean) app.get("guest");
                        String uniqueName = app.getString("uniqueName");
                        String packageName = app.getString("packageName");
                        boolean enable = (boolean) app.get("enable");
                        boolean encrypted = (boolean) app.get("encrypted");

                        MyApplication.getAppDatabase(context).getDao().updateAppStatusFromServer(guest, encrypted, enable, packageName);

                        if (i == apps.length() - 1) {
                            listener.onAppsReady();
                        }


                    } catch (Exception e) {
                        Timber.d("error : %s", e.getMessage());
                    }
                }
            }
        }.start();
    }

    // function to save installed or uninstalled apps that will be used to send to server
    public static void saveAppsList(Context context, boolean install, AppInfo info, boolean status) {

        Timber.d("<<<============= save installed or uninstalled apps ============>>>");

        if (PrefUtils.getBooleanPref(context, DEVICE_LINKED_STATUS)) {
            Timber.d("device is linked");
            Gson gson = new Gson();
            Timber.d("<<<====saved apps======>>>");

            String json;
            // flag will check is package installed or uninstalled

            if (install) {
                json = PrefUtils.getStringPref(context, INSTALLED_APPS);
            } else {
                json = PrefUtils.getStringPref(context, UNINSTALLED_APPS);
            }


            Type type = new TypeToken<ArrayList<InstallModel>>() {
            }.getType();

            ArrayList<AppInfo> list;
            if (json != null) {
                list = gson.fromJson(json, type);
            } else {
                list = new ArrayList<>();
            }


            if (!status) {
                Timber.d("unique name %s", info.getUniqueName());
                // adding package to list
                if (!list.contains(info)) {
                    list.add(info);
                }
                Timber.d("<<<======app added to list and saved======>>>");
                json = gson.toJson(list);

                if (install) {
                    PrefUtils.saveStringPref(context, INSTALLED_APPS, json);
                } else {
                    PrefUtils.saveStringPref(context, UNINSTALLED_APPS, json);
                }

            }


            Socket socket = SocketManager.getInstance().getSocket();
            String device_id = PrefUtils.getStringPref(context, DEVICE_ID);
            Timber.d("device id %s", device_id);

            if (socket != null && socket.connected()) {
                Timber.d("<<<=======Socket connected=============>>>");
                if (install) {
                    SocketManager.getInstance().getSocket().emit(SEND_INSTALLED_APPS + device_id, json);
                    PrefUtils.saveStringPref(context, INSTALLED_APPS, null);
                } else {
                    SocketManager.getInstance().getSocket().emit(SEND_UNINSTALLED_APPS + device_id, json);
                    PrefUtils.saveStringPref(context, UNINSTALLED_APPS, null);
                }
            }


        } else {
            Timber.d("device is not linked");
        }
    }


    public static void refreshApps(Context context) {

        OnAppsRefreshListener listener = (OnAppsRefreshListener) context;

        String unInstalledPackage = PrefUtils.getStringPref(context, UNINSTALLED_PACKAGES);


        if (unInstalledPackage != null) {
            String[] data = unInstalledPackage.split(",");
            PackageManager pm = context.getPackageManager();
            for (int i = 0; i < data.length; i++) {
                String packageName = data[i].split(":")[0];
                String space = data[i].split(":")[1];
                try {
                    pm.getPackageInfo(packageName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    int finalI = i;
                    new Thread(() -> {
                        MyApplication.getAppDatabase(context).getDao().deletePackage(packageName);

                        AppInfo info = new AppInfo();
                        info.setPackageName(packageName);
                        info.setUniqueName(packageName);

                        saveAppsList(context, false, info, false);

                        if (finalI == data.length - 1) {
                            listener.onAppsRefresh();
                            PrefUtils.saveStringPref(context, UNINSTALLED_PACKAGES, null);
                        }
                    }).start();
                }
            }
        }

        String installedPackages = PrefUtils.getStringPref(context, INSTALLED_PACKAGES);


        if (installedPackages != null) {
            String[] data = installedPackages.split(",");

            PackageManager pm = context.getPackageManager();
            for (int i = 0; i < data.length; i++) {
                String packageName = data[i].split(":")[0];
                String space = data[i].split(":")[1];
                try {
                    pm.getPackageInfo(packageName, 0);
                    Intent intent = new Intent(Intent.ACTION_MAIN, null);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> allApps = pm.queryIntentActivities(intent, 0);
                    for (ResolveInfo ri : allApps) {
                        if (ri.activityInfo.packageName.equals(packageName)) {
                            int finalI = i;
                            AppInfo app = new AppInfo(String.valueOf(ri.loadLabel(pm)),
                                    ri.activityInfo.packageName, CommonUtils.convertDrawableToByteArray(ri.activityInfo.loadIcon(pm)));
                            app.setUniqueName(app.getPackageName());
                            app.setExtension(false);
                            app.setDefaultApp(false);
                            app.setEncrypted(false);
                            app.setGuest(false);
                            app.setVisible(true);
                            app.setSystemApp(false);
                            switch (space) {
                                case KEY_GUEST_PASSWORD:
                                    app.setGuest(true);
                                    break;
                                case KEY_MAIN_PASSWORD:
                                    app.setEncrypted(true);
                                    break;
                            }

                            app.setEnable(true);
                            new Thread(() -> {
                                MyApplication.getAppDatabase(context).getDao().insertApps(app);
                                saveAppsList(context, true, app, false);
                                if (finalI == data.length - 1) {
                                    listener.onAppsRefresh();
                                    PrefUtils.saveStringPref(context, INSTALLED_PACKAGES, null);
                                }
                            }).start();
                        }
                    }


                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();

                }
            }
        }


    }


    public static void updateExtensionsList(final Context context, final JSONArray extensions, final GetExtensions listener, boolean isPolicy) {

        new Thread() {
            @Override
            public void run() {
                super.run();

                if (isPolicy) {
//                    MyApplication.getAppDatabase(context).getDao().updateAllExtensions(false,false);
                }

                for (int i = 0; i < extensions.length(); i++) {

                    try {

                        JSONObject app = extensions.getJSONObject(i);

                        int guest = (int) app.get("guest");

                        String uniqueExtension = app.getString("uniqueExtension");

                        Timber.d(uniqueExtension);


                        int encrypted = (int) app.get("encrypted");


                        MyApplication.getAppDatabase(context).getDao().updateExtensionStatusFromServer(guest != 0, encrypted != 0, uniqueExtension);

                        if (i == extensions.length() - 1) {
                            listener.onExtensionsReady();
                        }


                    } catch (Exception e) {
                        Timber.e("error : %s", e.getMessage());
                    }
                }
            }
        }.start();
    }

    public static boolean validateRequest(String arg1, String arg2) {
        if (arg1 == null || arg2 == null) {
            return false;
        } else if (arg1.equals("") || arg2.equals("")) {
            return false;
        } else return arg1.equals(arg2);
    }


    public static void updatePasswords(Context context, JSONObject object) {
        try {

            String guest_pass = object.getString("guest_password");
            String encrypted_pass = object.getString("encrypted_password");
            String admin_pass = object.getString("admin_password");
            String duress_password = object.getString("duress_password");

            if (checkString(guest_pass)) {
                Timber.d("guest pass : %s", guest_pass);
                PrefUtils.saveStringPref(context, AppConstants.KEY_GUEST_PASSWORD, guest_pass);
            }
            if (checkString(encrypted_pass)) {
                Timber.d("encrypted pass : %s", encrypted_pass);
                PrefUtils.saveStringPref(context, KEY_MAIN_PASSWORD, encrypted_pass);
            }
            if (checkString(admin_pass)) {
                Timber.d("admin pass : %s", admin_pass);
                PrefUtils.saveStringPref(context, AppConstants.KEY_CODE_PASSWORD, admin_pass);
            }
            if (checkString(duress_password)) {
                if (duress_password.equals("clear")) {
                    PrefUtils.saveStringPref(context, AppConstants.KEY_DURESS_PASSWORD, null);
                    PrefUtils.saveStringPref(context, AppConstants.DURESS_PATTERN, null);
                    PrefUtils.saveStringPref(context, AppConstants.DUERESS_DEFAULT_CONFIG, null);
                }
            }
        } catch (Exception e) {

            Timber.d(e);
        }

    }

    private static boolean checkString(String string) {
        return string != null && !string.equals("") && !string.equals("null");
    }


    public static void suspendedDevice(final Context context, String msg) {
        Timber.d("%s device", msg);

        String device_id = PrefUtils.getStringPref(context, DEVICE_ID);

        if (device_id == null) {
            device_id = PrefUtils.getStringPref(context, OFFLINE_DEVICE_ID);
        }

        switch (msg) {
            case "suspended":
                PrefUtils.saveStringPref(context, DEVICE_STATUS, "suspended");
                break;
            case "expired":
                PrefUtils.saveStringPref(context, DEVICE_STATUS, "expired");
                break;
            case "flagged":
                PrefUtils.saveStringPref(context, DEVICE_STATUS, "flagged");
                break;
        }
        PrefUtils.saveStringPref(context, DEVICE_ID, device_id);
        String main_password = PrefUtils.getStringPref(context, KEY_MAIN_PASSWORD);
        if (main_password == null) {
            PrefUtils.saveStringPref(context, KEY_MAIN_PASSWORD, DEFAULT_MAIN_PASS);
        }

        sendBroadcast(context, msg);

        Intent lockScreenIntent = new Intent(context, LockScreenService.class);
        lockScreenIntent.setAction(msg);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            context.startForegroundService(lockScreenIntent);
        } else {
            context.startService(lockScreenIntent);
        }


        boolean device_linked = PrefUtils.getBooleanPref(context, DEVICE_LINKED_STATUS);

        if (device_linked && SocketManager.getInstance().getSocket() == null) {
            String token = PrefUtils.getStringPref(context, TOKEN);
            startSocket(context, device_id, token);
        } else if (SocketManager.getInstance().getSocket() != null && !SocketManager.getInstance().getSocket().connected()) {
            String token = PrefUtils.getStringPref(context, TOKEN);
            startSocket(context, device_id, token);
        }


    }

    public static void newDevice(Context context, boolean status) {

        PrefUtils.saveBooleanPref(context, AppConstants.DEVICE_LINKED_STATUS, false);
        PrefUtils.saveStringPref(context, AppConstants.DEVICE_STATUS, null);
        PrefUtils.saveBooleanPref(context, AppConstants.IS_SYNCED, false);
        PrefUtils.saveBooleanPref(context, AppConstants.SETTINGS_CHANGE, false);
        PrefUtils.saveBooleanPref(context, AppConstants.LOCK_SCREEN_STATUS, false);
        PrefUtils.saveBooleanPref(context, AppConstants.APPS_SETTING_CHANGE, false);
        PrefUtils.saveStringPref(context, AppConstants.DEVICE_ID, null);
        PrefUtils.saveBooleanPref(context, APPS_SENT_STATUS, false);
        PrefUtils.saveBooleanPref(context, EXTENSIONS_SENT_STATUS, false);
        PrefUtils.saveBooleanPref(context, SETTINGS_SENT_STATUS, false);
        PrefUtils.saveStringPref(context, VALUE_EXPIRED, null);

       /* String guest_pass = PrefUtils.getStringPref(context, KEY_GUEST_PASSWORD);
        String main_pass = PrefUtils.getStringPref(context, KEY_MAIN_PASSWORD);


        if (guest_pass == null) {
            PrefUtils.saveStringPref(context, AppConstants.KEY_GUEST_PASSWORD, DEFAULT_GUEST_PASS);
        }
        if (main_pass == null) {
            PrefUtils.saveStringPref(context, AppConstants.KEY_MAIN_PASSWORD, DEFAULT_MAIN_PASS);

        }*/


        Intent socketService = new Intent(context, SocketService.class);
        context.stopService(socketService);

        Intent lockScreen = new Intent(context, LockScreenService.class);
        lockScreen.setAction("unlinked");

        if (status) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(lockScreen);
            } else {
                context.startService(lockScreen);
            }
        }


    }


    public static void unlinkDevice(Context context, boolean status) {

        PrefUtils.saveStringPref(context, AppConstants.DEVICE_STATUS, "unlinked");
        PrefUtils.saveBooleanPref(context, AppConstants.DEVICE_LINKED_STATUS, false);
        PrefUtils.saveBooleanPref(context, AppConstants.IS_SYNCED, false);
        PrefUtils.saveBooleanPref(context, AppConstants.SETTINGS_CHANGE, false);
        PrefUtils.saveBooleanPref(context, AppConstants.LOCK_SCREEN_STATUS, false);
        PrefUtils.saveBooleanPref(context, AppConstants.APPS_SETTING_CHANGE, false);
//        PrefUtils.saveStringPref(context, AppConstants.DEVICE_ID, null);
        PrefUtils.saveBooleanPref(context, APPS_SENT_STATUS, false);
        PrefUtils.saveBooleanPref(context, EXTENSIONS_SENT_STATUS, false);
        PrefUtils.saveBooleanPref(context, SETTINGS_SENT_STATUS, false);
        PrefUtils.saveStringPref(context, VALUE_EXPIRED, null);



        /*String guest_pass = PrefUtils.getStringPref(context, KEY_GUEST_PASSWORD);
        String main_pass = PrefUtils.getStringPref(context, KEY_MAIN_PASSWORD);
        if (guest_pass == null) {
            PrefUtils.saveStringPref(context, AppConstants.KEY_GUEST_PASSWORD, DEFAULT_GUEST_PASS);
        }
        if (main_pass == null) {
            PrefUtils.saveStringPref(context, AppConstants.KEY_MAIN_PASSWORD, DEFAULT_MAIN_PASS);

        }*/


        Intent socketService = new Intent(context, SocketService.class);
        context.stopService(socketService);

        Intent lockScreen = new Intent(context, LockScreenService.class);
        lockScreen.setAction("unlinked");

        if (status) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(lockScreen);
            } else {
                context.startService(lockScreen);
            }
        }
        sendBroadcast(context, "unlinked");


    }


    public static void startSocket(Context context, String device_id, String token) {

        if (device_id != null && token != null) {
            Intent intent = new Intent(context, SocketService.class);
            intent.setAction("start");
            PrefUtils.saveStringPref(context, DEVICE_ID, device_id);
            PrefUtils.saveStringPref(context, TOKEN, token);
            PrefUtils.saveBooleanPref(context, AppConstants.DEVICE_LINKED_STATUS, true);
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }

        }

    }


    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void unSuspendDevice(Context context) {

        boolean lock_screen_status = PrefUtils.getBooleanPref(context, LOCK_SCREEN_STATUS);
        if (lock_screen_status) {
            Intent intent = new Intent(context, LockScreenService.class);
            context.stopService(intent);
        }

        sendBroadcast(context, null);
        Timber.d("activeDevice");

        String token = PrefUtils.getStringPref(context, TOKEN);
        String device_id = PrefUtils.getStringPref(context, DEVICE_ID);


        boolean device_linked = PrefUtils.getBooleanPref(context, DEVICE_LINKED_STATUS);

        if (device_linked && SocketManager.getInstance().getSocket() == null) {
            startSocket(context, device_id, token);
        } else if (SocketManager.getInstance().getSocket() != null && !SocketManager.getInstance().getSocket().connected()) {
            startSocket(context, device_id, token);
        }


        PrefUtils.saveStringPref(context, DEVICE_STATUS, null);

    }


    /*
     *  This method checks either app is already installed or not and which app has to update or downgrade and returns list of custom object (InstallModel)
     * */
    public static InstalledAndRemainingApps checkInstalledApps(List<InstallModel> apps, Context context) {

        // list for remaining apps
        List<InstallModel> remainingApps = new ArrayList<>();

        List<InstallModel> installedApps = new ArrayList<>();

        PackageManager pm = context.getPackageManager();


        for (InstallModel app : apps) {
            try {
                PackageInfo info = pm.getPackageInfo(app.getPackage_name(), 0);

                // current version of app
                int currentVersion = info.versionCode;
                // available version from server
                int availableVersion = Integer.parseInt(app.getVersion());


                if (currentVersion != availableVersion) {
                    // both version are not equal
                    if (currentVersion > availableVersion) {
                        // flag shows package is already installed
                        app.setInstall(true);
                        app.setUpdate(false);
                        remainingApps.add(app);
                    } else {
                        app.setInstall(true);
                        app.setUpdate(true);
                        remainingApps.add(app);
                    }
                } else {
                    installedApps.add(app);
                }

            } catch (PackageManager.NameNotFoundException e) {
                app.setInstall(false);
                remainingApps.add(app);
            }
        }


        return new InstalledAndRemainingApps(remainingApps, installedApps);
    }


    public static String getDeviceStatus(Context context) {
        return PrefUtils.getStringPref(context, DEVICE_STATUS);
    }

    public static void registerDeviceStatusReceiver(Context context, DeviceStatusReceiver deviceStatusReceiver) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DEVICE_STATUS_CHANGE_RECEIVER);
        LocalBroadcastManager.getInstance(context).registerReceiver(deviceStatusReceiver, filter);
    }

    public static void loginAsGuest(Context context) {

        PrefUtils.saveIntegerPref(context, LOGIN_ATTEMPTS, 0);
        PrefUtils.saveLongPref(context, TIME_REMAINING, 0);
        PrefUtils.saveLongPref(context, TIME_REMAINING_REBOOT, 0);
        PrefUtils.saveStringPref(context, AppConstants.CURRENT_KEY, AppConstants.KEY_GUEST_PASSWORD);
//                    Toast.makeText(context, "loading...", Toast.LENGTH_SHORT).show();
        sendMessageToActivity(AppConstants.KEY_GUEST_PASSWORD, context);
        Intent service = new Intent(context, LockScreenService.class);
        service.setAction("unlocked");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        } else {
            context.startService(service);
        }
    }


    public static void chatLogin(Context context) {

        PrefUtils.saveStringPref(context, AppConstants.CURRENT_KEY, AppConstants.KEY_SUPPORT_PASSWORD);
//                    Toast.makeText(context, "loading...", Toast.LENGTH_SHORT).show();
        sendMessageToActivity(AppConstants.KEY_SUPPORT_PASSWORD, context);
        Intent service = new Intent(context, LockScreenService.class);
        service.setAction("unlocked");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        } else {
            context.startService(service);
        }
    }

    public static void loginAsEncrypted(Context context) {

        PrefUtils.saveIntegerPref(context, LOGIN_ATTEMPTS, 0);
        PrefUtils.saveLongPref(context, TIME_REMAINING, 0);
        PrefUtils.saveLongPref(context, TIME_REMAINING_REBOOT, 0);
        PrefUtils.saveStringPref(context, AppConstants.CURRENT_KEY, AppConstants.KEY_MAIN_PASSWORD);
//                    Toast.makeText(context, "loading...", Toast.LENGTH_SHORT).show();
        sendMessageToActivity(AppConstants.KEY_MAIN_PASSWORD, context);

        Intent service = new Intent(context, LockScreenService.class);
        service.setAction("unlocked");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        } else {
            context.startService(service);
        }
        //in case we need to stop service in future
//        boolean lock_screen = PrefUtils.getBooleanPref(context, LOCK_SCREEN_STATUS);
//        if (lock_screen) {
//            Intent intent = new Intent(context, LockScreenService.class);
//            context.stopService(intent);
//            PrefUtils.saveBooleanPref(context, LOCK_SCREEN_STATUS, false);
//        }

    }


    public static String getUserType(String enteredPin, Context context) {

        String duressPin = PrefUtils.getStringPref(context, AppConstants.KEY_DURESS_PASSWORD);


        if (enteredPin.equals(PrefUtils.getStringPref(context, AppConstants.KEY_GUEST_PASSWORD))) {
            return "guest";
        } else if (enteredPin.equals(PrefUtils.getStringPref(context, AppConstants.KEY_MAIN_PASSWORD))) {
            return "encrypted";
        } else if (duressPin != null) {
            if (enteredPin.equals(duressPin)) {
                return "duress";
            }
        }
        return "none";
    }

    public static void sendBroadcast(Context context, String device_status) {
        Intent intent = new Intent(DEVICE_STATUS_CHANGE_RECEIVER);
        intent.putExtra("device_status", device_status);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    public static void syncDevice(Context context, boolean isSync, boolean apps, boolean extensions, boolean settings) {

        PrefUtils.saveBooleanPref(context, IS_SYNCED, isSync);
        PrefUtils.saveBooleanPref(context, APPS_SENT_STATUS, apps);
        PrefUtils.saveBooleanPref(context, EXTENSIONS_SENT_STATUS, extensions);
        PrefUtils.saveBooleanPref(context, SETTINGS_SENT_STATUS, settings);

    }


    public static void sendIntent(int slot, String imei, Context context) {

        if (isValidImei(imei)) {

//            Intent intent = new Intent("com.sysadmin.action.APPLY_SETTING");
//            intent.putExtra("setting", "write.imei");
//            intent.putExtra("simSlotId", String.valueOf(slot));
//            intent.putExtra("imei", imei);
//            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//            intent.setComponent(new ComponentName("com.omegamoon.sysadmin", "com.omegamoon.sysadmin.SettingsReceiver"));
//            context.sendBroadcast(intent);

            Intent intent = new Intent("com.sysadmin.action.APPLY_SETTING");
            intent.putExtra("setting", "write.imei");
            intent.putExtra("simSlotId", String.valueOf(slot));
            intent.putExtra("imei", imei);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.setComponent(new ComponentName("com.omegamoon.sysadmin", "com.omegamoon.sysadmin.SettingsReceiver"));
            context.sendBroadcast(intent);


        }
    }


    public static boolean checkIMei(Context context) {

        boolean status = false;

        List<String> imeis = DeviceIdUtils.getIMEI(context);

        String imei1 = PrefUtils.getStringPref(context, IMEI1);
        String imei2 = PrefUtils.getStringPref(context, IMEI2);

        if (imeis != null && imeis.size() >= 1) {

            if (imei1 != null && !imeis.get(0).equals(imei1)) {
                PrefUtils.saveStringPref(context, IMEI1, imeis.get(0));
                status = true;
            }

        }

        if (imeis != null && imeis.size() >= 2) {
            if (imei2 != null && !imeis.get(1).equals(imei2)) {
                PrefUtils.saveStringPref(context, IMEI2, imeis.get(1));
                status = true;
            }
        }


        return status;
    }


    public static String twoDatesBetweenTime(String oldtime) {

        int day = 0;
        int hh = 0;
        int mm = 0;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date oldDate = dateFormat.parse(oldtime);
            Date cDate = new Date();
            Long timeDiff = cDate.getTime() - oldDate.getTime();
            day = (int) TimeUnit.MILLISECONDS.toDays(timeDiff);
            hh = (int) (TimeUnit.MILLISECONDS.toHours(timeDiff) - TimeUnit.DAYS.toHours(day));
            mm = (int) (TimeUnit.MILLISECONDS.toMinutes(timeDiff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeDiff)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (day == 0) {
            return hh + " hour " + mm + " min";
        } else if (hh == 0) {
            return mm + " min";
        } else {
            return day + " days " + hh + " hour " + mm + " min";
        }
    }


    public static String getDate(long milliSeconds) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static boolean isJobServiceOn(Context context, int JOB_ID) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        boolean hasBeenScheduled = false;
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == JOB_ID) {
                hasBeenScheduled = true;
                break;
            }
        }
        return hasBeenScheduled;
    }

    public static void cancelJob(Context context, int JOB_ID) {
        JobScheduler scheduler1 = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler1.cancel(JOB_ID);
    }

    public static void scheduleUpdateJob(Context context) {

        if (!context.getResources().getString(R.string.apktype).equals("BYOD")) {
            ComponentName componentName = new ComponentName(context, CheckUpdateService.class);
            JobInfo jobInfo;
            if (PrefUtils.getIntegerPref(context, UPDATESIM) != 1) {
                jobInfo = new JobInfo.Builder(UPDATE_JOB, componentName)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setPeriodic(ONE_DAY_INTERVAL)
                        .build();
            } else {
                jobInfo = new JobInfo.Builder(UPDATE_JOB, componentName)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                        .setPeriodic(ONE_DAY_INTERVAL)
                        .build();
            }
            JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
            int resultCode = scheduler.schedule(jobInfo);
            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                Timber.d("Job Scheduled");
            } else {
                Timber.d("Job Scheduled Failed");
            }
        }


    }


    public static void saveArrayList(ArrayList<InstallModel> list, Context context) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        PrefUtils.saveStringPref(context, APPS_LIST, null);
        PrefUtils.saveStringPref(context, APPS_LIST, json);
    }

    public static ArrayList<InstallModel> getArrayList(Context context) {
        Gson gson = new Gson();
        String json = PrefUtils.getStringPref(context, APPS_LIST);
        Type type = new TypeToken<ArrayList<InstallModel>>() {
        }.getType();
        return gson.fromJson(json, type);
    }
}
