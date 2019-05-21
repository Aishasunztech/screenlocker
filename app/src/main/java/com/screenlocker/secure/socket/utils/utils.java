package com.screenlocker.secure.socket.utils;

import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.socket.interfaces.GetApplications;
import com.screenlocker.secure.socket.interfaces.GetExtensions;
import com.screenlocker.secure.socket.model.Settings;
import com.screenlocker.secure.socket.receiver.DeviceStatusReceiver;
import com.screenlocker.secure.socket.service.SocketService;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import timber.log.Timber;

import static android.content.Context.DEVICE_POLICY_SERVICE;
import static com.screenlocker.secure.mdm.utils.DeviceIdUtils.isValidImei;
import static com.screenlocker.secure.utils.AppConstants.APPS_SENT_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEFAULT_GUEST_PASS;
import static com.screenlocker.secure.utils.AppConstants.DEFAULT_MAIN_PASS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS_CHANGE_RECEIVER;
import static com.screenlocker.secure.utils.AppConstants.EXTENSIONS_SENT_STATUS;
import static com.screenlocker.secure.utils.AppConstants.IMEI1;
import static com.screenlocker.secure.utils.AppConstants.IMEI2;
import static com.screenlocker.secure.utils.AppConstants.IS_SYNCED;
import static com.screenlocker.secure.utils.AppConstants.KEY_DEVICE_LINKED;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.LOCK_SCREEN_STATUS;
import static com.screenlocker.secure.utils.AppConstants.LOGIN_ATTEMPTS;
import static com.screenlocker.secure.utils.AppConstants.SETTINGS_SENT_STATUS;
import static com.screenlocker.secure.utils.AppConstants.TIME_REMAINING;
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
            if (duress.equals(key)) {
                return false;
            }

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
                        boolean enable = (boolean) app.get("enable");
                        boolean encrypted = (boolean) app.get("encrypted");

                        MyApplication.getAppDatabase(context).getDao().updateAppStatusFromServer(guest, encrypted, enable, uniqueName);

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


    public static void updateExtensionsList(final Context context, final JSONArray extensions, final GetExtensions listener,boolean isPolicy) {

        new Thread() {
            @Override
            public void run() {
                super.run();

                if(isPolicy){
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
        } catch (Exception e) {

            Timber.d(e);
        }

    }

    private static boolean checkString(String string) {
        return string != null && !string.equals("") && !string.equals("null");
    }

    public static Settings getCurrentSettings(Context context) {

        //Calls setting
        Settings settings = new Settings();
        boolean callStatus = PrefUtils.getBooleanPref(context, AppConstants.KEY_DISABLE_CALLS);
        settings.setCall_status(callStatus);
        //Bluetooth setting
        try {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            boolean bluetoothStatus = mBluetoothAdapter.isEnabled();
            settings.setBluetooth_status(bluetoothStatus);
        } catch (Exception e) {
            settings.setBluetooth_status(false);
        }

        //Wifi setting
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                boolean wifiStatus = wifiManager.isWifiEnabled();
                settings.setWifi_status(wifiStatus);
            } else {
                settings.setWifi_status(true);
            }
        } catch (Exception e) {
            settings.setWifi_status(true);
        }

        //Screenshot setting
        settings.setScreenshot_status(false);
        //Hotspot setting
        settings.setHotspot_status(false);

        return settings;
    }

    public static void changeSettings(Context context, Settings settings) {

//        Timber.d("changeSettings: ");
//        //Calls setting
//        boolean callStatus = settings.isCall_status();
//        Timber.d("callStatus: %s", callStatus);
//        PrefUtils.saveBooleanPref(context, AppConstants.KEY_DISABLE_CALLS, callStatus);
//        //Bluetooth setting
//        boolean bluetoothStatus = settings.isBluetooth_status();
//        try {
//            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//            if (bluetoothStatus) {
//                mBluetoothAdapter.enable();
//            } else {
//                mBluetoothAdapter.disable();
//            }
//        } catch (Exception ignored) {
//
//        }
//
//        Timber.d("bluetoothStatus: %s", bluetoothStatus);
//
//        //wifi setting
//        boolean wifiStatus = settings.isWifi_status();
//        try {
//            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//            if (wifiManager != null) {
//                if (wifiManager.isWifiEnabled())
//                    wifiManager.setWifiEnabled(wifiStatus);
//            }
//        } catch (Exception ignored) {
//        }
//
//        Timber.d("wifiStatus: %s", wifiStatus);
//
//
//        //screenshot setting
//        boolean screenShotStatus = settings.isScreenshot_status();
//
////        if (screenShotStatus) {
////            enableScreenShotBlocker(true);
////        } else {
////            disableScreenShotBlocker(true);
////        }
//
//        //HotSpotSetting
//        boolean hotSpotStatus = settings.isHotspot_status();
//        try {
//            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//            WifiApControl.turnOnOffHotspot(false, wifiManager);
//            Timber.d("hotSpotStatus: %s", hotSpotStatus);
//
//        } catch (Exception ignored) {
//        }
    }

    public static void suspendedDevice(final Context context, String device_id, String msg) {
        Timber.d("%s device", msg);


        switch (msg) {
            case "suspended":
                PrefUtils.saveStringPref(context, DEVICE_STATUS, "suspended");
                break;
            case "expired":
                PrefUtils.saveStringPref(context, DEVICE_STATUS, "expired");
                break;
        }
        PrefUtils.saveStringPref(context, DEVICE_ID, device_id);
        String main_password = PrefUtils.getStringPref(context, KEY_MAIN_PASSWORD);
        if (main_password == null) {
            PrefUtils.saveStringPref(context, KEY_MAIN_PASSWORD, DEFAULT_MAIN_PASS);
        }

        Intent lockScreenIntent = new Intent(context, LockScreenService.class);
        lockScreenIntent.setAction(msg);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            context.startForegroundService(lockScreenIntent);
        } else {
            context.startService(lockScreenIntent);
        }
    }

    public static void unlinkDevice(Context context) {

        PrefUtils.saveBooleanPref(context, DEVICE_LINKED_STATUS, false);
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

        String guest_pass = PrefUtils.getStringPref(context, KEY_GUEST_PASSWORD);
        String main_pass = PrefUtils.getStringPref(context, KEY_MAIN_PASSWORD);
        PrefUtils.saveStringPref(context, VALUE_EXPIRED, null);


        if (guest_pass == null) {
            PrefUtils.saveStringPref(context, AppConstants.KEY_GUEST_PASSWORD, DEFAULT_GUEST_PASS);
        }
        if (main_pass == null) {
            PrefUtils.saveStringPref(context, AppConstants.KEY_MAIN_PASSWORD, DEFAULT_MAIN_PASS);

        }


        Intent socketService = new Intent(context, SocketService.class);
        context.stopService(socketService);

        Intent lockScreen = new Intent(context, LockScreenService.class);
        lockScreen.setAction("unlinked");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(lockScreen);
        } else {
            context.startService(lockScreen);
        }


    }

    public static void unSuspendDevice(Context context) {

        boolean lock_screen_status = PrefUtils.getBooleanPref(context, LOCK_SCREEN_STATUS);
        if (lock_screen_status) {
            Intent intent = new Intent(context, LockScreenService.class);
            context.stopService(intent);
        }
        Timber.d("activeDevice");
        PrefUtils.saveStringPref(context, DEVICE_STATUS, null);
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
        Intent service = new Intent(context, LockScreenService.class);
        service.setAction("unlocked");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        } else
            context.startService(service);
        PrefUtils.saveStringPref(context, AppConstants.CURRENT_KEY, AppConstants.KEY_GUEST_PASSWORD);
//                    Toast.makeText(context, "loading...", Toast.LENGTH_SHORT).show();
        sendMessageToActivity(AppConstants.KEY_GUEST_PASSWORD, context);
    }

    public static void loginAsEncrypted(Context context) {

        PrefUtils.saveIntegerPref(context, LOGIN_ATTEMPTS, 0);
        PrefUtils.saveLongPref(context, TIME_REMAINING, 0);


        Intent service = new Intent(context, LockScreenService.class);
        service.setAction("unlocked");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        } else
            context.startService(service);

        PrefUtils.saveStringPref(context, AppConstants.CURRENT_KEY, AppConstants.KEY_MAIN_PASSWORD);
//                    Toast.makeText(context, "loading...", Toast.LENGTH_SHORT).show();
        sendMessageToActivity(AppConstants.KEY_MAIN_PASSWORD, context);
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
                PrefUtils.saveStringPref(context, IMEI1, imeis.get(1));
                status = true;
            }
        }


        return status;
    }


}
