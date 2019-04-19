package com.screenlocker.secure.socket.utils;

import android.content.Context;

import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.settings.codeSetting.systemControls.SystemPermissionActivity;
import com.screenlocker.secure.socket.SocketSingleton;
import com.screenlocker.secure.socket.interfaces.ChangeSettings;
import com.screenlocker.secure.socket.interfaces.DatabaseStatus;
import com.screenlocker.secure.socket.interfaces.GetApplications;
import com.screenlocker.secure.socket.interfaces.GetExtensions;
import com.screenlocker.secure.socket.interfaces.SocketEvents;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import timber.log.Timber;

import static com.screenlocker.secure.socket.SocketSingleton.getSocket;
import static com.screenlocker.secure.socket.utils.utils.suspendedDevice;
import static com.screenlocker.secure.socket.utils.utils.syncDevice;
import static com.screenlocker.secure.socket.utils.utils.unSuspendDevice;
import static com.screenlocker.secure.socket.utils.utils.unlinkDevice;
import static com.screenlocker.secure.socket.utils.utils.updateAppsList;
import static com.screenlocker.secure.socket.utils.utils.updateExtensionsList;
import static com.screenlocker.secure.socket.utils.utils.updatePasswords;
import static com.screenlocker.secure.socket.utils.utils.validateRequest;
import static com.screenlocker.secure.socket.utils.utils.wipeDevice;
import static com.screenlocker.secure.utils.AppConstants.APPS_SENT_STATUS;
import static com.screenlocker.secure.utils.AppConstants.APPS_SETTING_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.DB_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.EXTENSIONS_SENT_STATUS;
import static com.screenlocker.secure.utils.AppConstants.GET_APPLIED_SETTINGS;
import static com.screenlocker.secure.utils.AppConstants.GET_SYNC_STATUS;
import static com.screenlocker.secure.utils.AppConstants.IS_SYNCED;
import static com.screenlocker.secure.utils.AppConstants.SECURE_SETTINGS_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.SEND_APPS;
import static com.screenlocker.secure.utils.AppConstants.SEND_EXTENSIONS;
import static com.screenlocker.secure.utils.AppConstants.SEND_SETTINGS;
import static com.screenlocker.secure.utils.AppConstants.SETTINGS_APPLIED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.SETTINGS_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.SETTINGS_SENT_STATUS;

public class SocketUtils implements SocketEvents, DatabaseStatus {


    private Socket socket;

    private String device_id;

    private Context context;

    private String token;


    SocketUtils(String device_id, Context context, String token) {
        this.device_id = device_id;
        this.context = context;
        this.token = token;

        SettingsActivity settingsActivity = new SettingsActivity();
        settingsActivity.setDatabaseStatus(this);


        initSocket();

        if (socket != null) {
            connectSocket();
        }


    }


    private void initSocket() {
        Timber.d("<<< initializing socket >>>");
        socket = getSocket(device_id, token);

    }

    @Override
    public void connectSocket() {


        try{
            socket.on(Socket.EVENT_CONNECT, args -> {
            Timber.d("<<< socket connected >>>");
            disconnectSocket();
            socketEventError();
            getSyncStatus();
            getAlliedSettings();
            getDeviceStatus();


        });}catch (Exception e){
            Timber.d(e);
        }

    }

    @Override
    public void sendAppsWithoutIcons() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    if (socket.connected()) {
                        socket.emit(SEND_APPS + device_id, new Gson().toJson(MyApplication.getAppDatabase(context).getDao().getAppsWithoutIcons()));
                        PrefUtils.saveBooleanPref(context, APPS_SETTING_CHANGE, false);

                        Timber.d("Apps sent");
                    } else {
                        Timber.d("Socket not connected");
                    }

                } catch (Exception e) {
                    Timber.e("error: %S", e.getMessage());
                }
            }
        }).start();


    }

    @Override
    public void sendExtensionsWithoutIcons() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    if (socket.connected()) {
                        socket.emit(SEND_EXTENSIONS + device_id, new Gson().toJson(MyApplication.getAppDatabase(context).getDao().getExtensionsWithoutIcons()));
                        PrefUtils.saveBooleanPref(context, SECURE_SETTINGS_CHANGE, false);

                        Timber.d("Extensions sent");
                    } else {
                        Timber.d("Socket not connected");
                    }

                } catch (Exception e) {
                    Timber.e("error: %S", e.getMessage());
                }
            }
        }).start();

    }


    @Override
    public void getSyncStatus() {
        try {
            if (socket.connected()) {
                socket.on(GET_SYNC_STATUS + device_id, args -> {
                    Timber.d("<<< getting device_sync status >>>");
                    JSONObject obj = (JSONObject) args[0];
                    try {
                        if (validateRequest(device_id, obj.getString("device_id"))) {
                            Timber.e(" valid request ");

                            Timber.d(obj.toString());

                            boolean is_synced = obj.getBoolean("is_sync");
                            boolean apps = obj.getBoolean("apps_status");
                            boolean extensions = obj.getBoolean("extensions_status");
                            boolean settings = obj.getBoolean("settings_status");

                            syncDevice(context, is_synced, apps, extensions, settings);

                            if (PrefUtils.getBooleanPref(context, DB_STATUS)) {

                                if (!PrefUtils.getBooleanPref(context, AppConstants.IS_SYNCED)) {
                                    if (!PrefUtils.getBooleanPref(context, AppConstants.APPS_SENT_STATUS)) {
                                        sendApps();
                                    } else if (!PrefUtils.getBooleanPref(context, AppConstants.EXTENSIONS_SENT_STATUS)) {
                                        sendExtensions();
                                    } else if (!PrefUtils.getBooleanPref(context, AppConstants.SETTINGS_SENT_STATUS)) {
                                        sendSettings();
                                    }
                                }

                            }


                        } else {
                            Timber.e(" invalid request ");
                        }
                    } catch (Exception error) {
                        Timber.e(" JSON error : %s", error.getMessage());
                    }
                });
            } else {
                Timber.d("Socket not connected");
            }

        } catch (Exception e) {
            Timber.d(e);
        }
    }


    @Override
    public void sendApps() {

        Timber.d("<<< sending apps >>>");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket.connected()) {
                        List<AppInfo> apps = MyApplication.getAppDatabase(context).getDao().getApps();
                        socket.emit(SEND_APPS + device_id, new Gson().toJson(apps));
                        Timber.d(" apps sent %s", apps.size());
                    } else {
                        Timber.d("Socket not connected");
                    }

                } catch (Exception e) {
                    Timber.d(e);
                }
            }
        }).start();

    }


    @Override
    public void sendExtensions() {
        Timber.d("<<< Sending Extensions >>>");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket.connected()) {

                        List<SubExtension> extensions = MyApplication.getAppDatabase(context).getDao().getAllSubExtensions();

                        socket.emit(SEND_EXTENSIONS + device_id, new Gson().toJson(extensions));

                        Timber.d("extensions sent%s", extensions.size());
                    } else {
                        Timber.d("Socket not connected");
                    }
                } catch (Exception e) {
                    Timber.d(e);
                }
            }
        }).start();
    }

    @Override
    public void getAlliedSettings() {

        if (socket.connected()) {

            socket.on(GET_APPLIED_SETTINGS + device_id, args -> {
                Timber.d("<<< getting applied settings >>>");
                JSONObject obj = (JSONObject) args[0];
                try {
                    if (validateRequest(device_id, obj.getString("device_id"))) {
                        Timber.d(" valid request ");
                        boolean status = obj.getBoolean("status");
                        Timber.d(" applied settings status : %S", status);
                        if (status) {
                            String appsList = obj.getString("app_list");
                            if (!appsList.equals("[]")) {
                                updateAppsList(context, new JSONArray(appsList), () -> {
                                    Timber.d(" apps updated ");
                                });
                            }
                            String passwords = obj.getString("passwords");
                            if (!passwords.equals("{}")) {
                                updatePasswords(context, new JSONObject(passwords));
                                Timber.d(" passwords updated ");
                            }
                            String settings = obj.getString("settings");
                            if (!settings.equals("{}")) {
//                            changeSettings(context, new Gson().fromJson(settings, Settings.class));
                                Timber.d(" settings applied ");
                            }
                            String extensionList = obj.getString("extension_list");

                            if(!extensionList.equals("[]"))
                            {
                                updateExtensionsList(context, new JSONArray(extensionList), () -> {
                                    Timber.d(" extensions updated ");
                                });
                            }

                            socket.emit(SETTINGS_APPLIED_STATUS + device_id, new JSONObject().put("device_id", device_id));
                            Timber.d(" settings applied status sent ");
                        } else {
                            Timber.d(" no settings available in history ");

                            boolean appsSettingStatus = PrefUtils.getBooleanPref(context, APPS_SETTING_CHANGE);
                            Timber.d(" apps settings status in local : %S", appsSettingStatus);

                            if (appsSettingStatus) {
                                sendAppsWithoutIcons();
                            }

                            boolean settingsStatus = PrefUtils.getBooleanPref(context, SETTINGS_CHANGE);
                            Timber.d(" settings status in local : %S", settingsStatus);
                            if (settingsStatus) {
                                sendApps();
                            }

                            boolean extensionsStatus = PrefUtils.getBooleanPref(context, SECURE_SETTINGS_CHANGE);
                            Timber.d(" extensions status in local : %S", extensionsStatus);
                            if (extensionsStatus) {
                                sendExtensionsWithoutIcons();
                            }



                        }

                    } else {
                        Timber.e(" invalid request ");
                    }

                } catch (Exception error) {
                    Timber.e(" JSON error : %s", error.getMessage());
                }

            });
        } else {
            Timber.d("Socket not connected");
        }


    }

    @Override
    public void getDeviceStatus() {

        if (socket.connected()) {

            socket.on(DEVICE_STATUS + device_id, args -> {
                Timber.d("<<< getting device status >>>");
                JSONObject object = (JSONObject) args[0];
                try {
                    if (validateRequest(device_id, object.getString("device_id"))) {
                        Timber.d("<<< valid request >>>");
                        String msg = object.getString("msg");
                        Timber.e("<<< device status =>>> %S", msg);
                        if (msg != null) {
                            switch (msg) {
                                case "suspended":
                                    suspendedDevice(context, device_id, "suspended");
                                    utils.sendBroadcast(context, "suspended");
                                    Timber.d("<<< device suspended >>>");
                                    break;
                                case "active":
                                    unSuspendDevice(context);
                                    utils.sendBroadcast(context, null);
                                    Timber.d("<<< device activated >>>");
                                    break;
                                case "expired":
                                    suspendedDevice(context, device_id, "expired");
                                    utils.sendBroadcast(context, "expired");
                                    Timber.d("<<< device expired >>>");
                                    break;
                                case "unlinked":
                                    Timber.d("<<< device unlinked >>>");
                                    unlinkDevice(context);
                                    break;
                                case "wiped":
                                    Timber.d("<<< device wiped >>>");
                                    wipeDevice(context);
                                    break;
                            }
                        }

                    } else {
                        Timber.d("<<< invalid request >>>");
                    }
                } catch (Exception error) {
                    Timber.e("<<< JSON error >>>%s", error.getMessage());
                }
            });
        } else {
            Timber.d("Socket connected");
        }

    }


    @Override
    public void sendSettings() {
        Timber.d("<<< Sending settings >>>");

        if (socket.connected()) {
            socket.emit(SEND_SETTINGS + device_id, "settings");
            PrefUtils.saveBooleanPref(context, SETTINGS_CHANGE, false);
        }else {
            Timber.d("Socket not connected");
        }

    }

    @Override
    public void sendAppliedStatus() {

    }


    @Override
    public void socketEventError() {
        if (socket.connected()) {
            socket.on(Socket.EVENT_ERROR, args -> {
                Timber.e("<<< socket event error >>>");
            });
        } else {
            Timber.d("Socket not connected");
        }

    }

    @Override
    public void closeSocket() {
        Timber.d("<<< closing socket >>>");
        SocketSingleton.closeSocket(device_id);
    }

    @Override
    public void disconnectSocket() {
        if (socket.connected()) {

            socket.on(Socket.EVENT_DISCONNECT, args -> {
                Timber.d("<<< disconnect socket >>>");
            });
        } else {
            Timber.d("Socket not connected");
        }

    }

    @Override
    public void onDataInserted() {
        Timber.d("<<< data is ready to send >>>");

        PrefUtils.saveBooleanPref(context, DB_STATUS, true);

        if (!PrefUtils.getBooleanPref(context, IS_SYNCED)) {

            if (!PrefUtils.getBooleanPref(context, APPS_SENT_STATUS)) {
                sendApps();
            }
            if (!PrefUtils.getBooleanPref(context, EXTENSIONS_SENT_STATUS)) {
                sendExtensions();
            }
            if (!PrefUtils.getBooleanPref(context, SETTINGS_SENT_STATUS)) {
                sendSettings();
            }

        }
    }


    public void syncDatabaseChange(String action) {

        if (PrefUtils.getBooleanPref(context, IS_SYNCED)) {

            if (action.equals("apps")) {
                sendAppsWithoutIcons();
            }
            if (action.equals("extensions")) {
                sendExtensionsWithoutIcons();
            }
            if (action.equals("settings")) {
                sendSettings();
            }

        }


    }


}
