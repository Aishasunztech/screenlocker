package com.screenlocker.secure.socket.utils;

import android.content.Context;

import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.screenlocker.secure.appSelection.AppSelectionActivity;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.settings.SettingContract;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.settingsMenu.SettingsMenuActivity;
import com.screenlocker.secure.socket.SocketSingleton;
import com.screenlocker.secure.socket.interfaces.ChangeSettings;
import com.screenlocker.secure.socket.interfaces.DatabaseStatus;
import com.screenlocker.secure.socket.interfaces.GetApplications;
import com.screenlocker.secure.socket.interfaces.SocketEvents;
import com.screenlocker.secure.utils.PrefUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import timber.log.Timber;

import static com.screenlocker.secure.socket.SocketSingleton.getSocket;
import static com.screenlocker.secure.socket.utils.utils.getAppsList;
import static com.screenlocker.secure.socket.utils.utils.getAppsWithoutIcons;
import static com.screenlocker.secure.socket.utils.utils.settingsChangeListener;
import static com.screenlocker.secure.socket.utils.utils.suspendedDevice;
import static com.screenlocker.secure.socket.utils.utils.syncDevice;
import static com.screenlocker.secure.socket.utils.utils.unSuspendDevice;
import static com.screenlocker.secure.socket.utils.utils.unSyncDevice;
import static com.screenlocker.secure.socket.utils.utils.unlinkDevcie;
import static com.screenlocker.secure.socket.utils.utils.updateAppsList;
import static com.screenlocker.secure.socket.utils.utils.updatePasswords;
import static com.screenlocker.secure.socket.utils.utils.validateRequest;
import static com.screenlocker.secure.utils.AppConstants.APPS_SETTING_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.DB_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.GET_APPLIED_SETTINGS;
import static com.screenlocker.secure.utils.AppConstants.GET_SYNC_STATUS;
import static com.screenlocker.secure.utils.AppConstants.IS_SYNCED;
import static com.screenlocker.secure.utils.AppConstants.SEND_APPS;
import static com.screenlocker.secure.utils.AppConstants.SETTINGS_APPLIED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.SETTINGS_CHANGE;

public class SocketUtils implements SocketEvents, DatabaseStatus, GetApplications, SettingContract.SettingsMvpView, ChangeSettings {


    private Socket socket;

    private String device_id;

    private Context context;

    private String token;

    public SocketUtils() {

    }

    SocketUtils(String device_id, Context context, String token) {
        this.device_id = device_id;
        this.context = context;
        this.token = token;

        AppSelectionActivity appSelectionActivity = new AppSelectionActivity();
        appSelectionActivity.setListener(this);

        SettingsActivity settingsActivity = new SettingsActivity();
        settingsActivity.setDatabaseStatus(this);

        SettingsMenuActivity settingsMenuActivity = new SettingsMenuActivity();
        settingsMenuActivity.setListener(this);

        initSocket();
        if (socket != null) {
            connectSocket();
            disconnectSocket();
            getSyncStatus();
            getAlliedSettings();
            getDeviceStatus();
            sendSettings();
            sendAppliedStatus();
            socketEventError();
        }


    }

    private void initSocket() {
        Timber.d("<<< initializing socket >>>");
        socket = getSocket(device_id, token);

    }


    @Override
    public void getSyncStatus() {
        socket.on(GET_SYNC_STATUS + device_id, args -> {
            Timber.d("<<< getting device_sync status >>>");
            JSONObject obj = (JSONObject) args[0];
            try {
                if (validateRequest(device_id, obj.getString("device_id"))) {
                    Timber.e(" valid request ");
                    boolean is_synced = obj.getBoolean("is_sync");
                    Timber.d(" device_sync status : %s", is_synced);
                    if (is_synced) {
                        Timber.d(" device synced successfully ");
                        syncDevice(context);
                    } else {
                        Timber.d(" device is not synced yet ");
                        unSyncDevice(context);
                    }
                } else {
                    Timber.e(" invalid request ");
                }
            } catch (JSONException error) {
                Timber.e(" JSON error : %s", error.getMessage());
            }
        });


    }

    @Override
    public void getAlliedSettings() {
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
                            updateAppsList(context, new JSONArray(appsList), appList -> {
                                Timber.d(" apps updated size : %S", appList.size());
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
                        settingsChangeListener(context, this);
                        socket.emit(SETTINGS_APPLIED_STATUS + device_id, new JSONObject().put("device_id", device_id));
                        Timber.d(" settings applied status sent ");
                    } else {
                        Timber.d(" no settings available in history ");
                        boolean appsSettingStatus = PrefUtils.getBooleanPref(context, APPS_SETTING_CHANGE);
                        Timber.d(" apps settings status in local : %S", appsSettingStatus);
                        boolean settingsStatus = PrefUtils.getBooleanPref(context, SETTINGS_CHANGE);
                        Timber.d(" settings status in local : %S", settingsStatus);
                        if (appsSettingStatus) {
                            getAppsList(context, appList -> {
                                Gson gson = new Gson();
                                socket.emit(SEND_APPS + device_id, gson.toJson(appList));
                                Timber.d(" apps list sent and size : %S", appList.size());
                            });
                        }
                        if (settingsStatus) {
                            Gson gson = new Gson();
//                            Settings settings = getCurrentSettings(context);
//                            socket.emit(SEND_SETTINGS + device_id, gson.toJson(settings));
                            Timber.d(" settings sent ");
                        }

                    }

                } else {
                    Timber.e(" invalid request ");
                }

            } catch (JSONException error) {
                Timber.e(" JSON error : %s", error.getMessage());
            }

        });
    }

    @Override
    public void getDeviceStatus() {
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
                                suspendedDevice(context, this, device_id, "suspended");
                                utils.sendBroadcast(context, "suspended");
                                Timber.d("<<< device suspended >>>");
                                break;
                            case "active":
                                unSuspendDevice(context);
                                utils.sendBroadcast(context, null);
                                Timber.d("<<< device activated >>>");
                                break;
                            case "expired":
                                suspendedDevice(context, (SettingContract.SettingsMvpView) context, device_id, "expired");
                                utils.sendBroadcast(context, "expired");
                                Timber.d("<<< device expired >>>");
                                break;
                            case "unlinked":
                                Timber.d("<<< device unlinked >>>");
                                unlinkDevcie(context);
                                break;
                        }
                    }

                } else {
                    Timber.d("<<< invalid request >>>");
                }
            } catch (JSONException error) {
                Timber.e("<<< JSON error >>>%s", error.getMessage());
            }
        });
    }

    @Override
    public void sendApps() {

        Timber.d("<<< sending apps >>>");
        boolean is_synced = PrefUtils.getBooleanPref(context, IS_SYNCED);
        Timber.d(" device sync status : %S", is_synced);
        boolean db_status = PrefUtils.getBooleanPref(context, DB_STATUS);
        Timber.d(" db status : %S", db_status);

        if (!is_synced && db_status) {
            Timber.d(" device is not synced ");
            getAppsList(context, appList -> {
                socket.emit(SEND_APPS + device_id, new Gson().toJson(appList));
//                socket.emit(SEND_SETTINGS + device_id, new Gson().toJson(getCurrentSettings(context)));
                Timber.d(" apps sent and size : %S", appList.size());
                Timber.d(" settings sent ");
            });
        } else {
            Timber.d("device already synced");
        }

    }

    @Override
    public void sendSettings() {
        Timber.d("<<< sending settings >>>");
    }

    @Override
    public void sendAppliedStatus() {

    }


    @Override
    public void socketEventError() {
        socket.on(Socket.EVENT_ERROR, args -> {
            Timber.e("<<< socket event error >>>");
        });
    }

    @Override
    public void closeSocket() {
        Timber.d("<<< closing socket >>>");
        SocketSingleton.closeSocket(device_id);
    }

    @Override
    public void disconnectSocket() {
        socket.on(Socket.EVENT_DISCONNECT, args -> {
            Timber.d("<<< disconnect socket >>>");
        });
    }

    @Override
    public void connectSocket() {
        socket.on(Socket.EVENT_CONNECT, args -> {
            Timber.d("<<< socket connected >>>");
            sendApps();
        });
    }

    @Override
    public void onSettingsChanged() {
        Timber.d("<<< on settings changed >>>");
        try {
            Gson gson = new Gson();
//            Settings settings = getCurrentSettings(context);
//            socket.emit(SEND_SETTINGS + device_id, gson.toJson(settings));
            PrefUtils.saveBooleanPref(context, SETTINGS_CHANGE, false);
        } catch (Exception e) {
            Timber.e("error :%S", e.getMessage());
        }
    }

    @Override
    public void onAppsInserted() {
        Timber.d("<<< apps are ready to send >>>");
        PrefUtils.saveBooleanPref(context, DB_STATUS, true);
        sendApps();
    }

    @Override
    public void onAppsReady(List<AppInfo> infos) {
        Timber.d("<<< on apps ready size =>>>%S", infos.size());
        try {
            List<AppInfo> appList = getAppsWithoutIcons(infos);
            Gson gson = new Gson();
            socket.emit(SEND_APPS + device_id, gson.toJson(appList));
            PrefUtils.saveBooleanPref(context, APPS_SETTING_CHANGE, false);
        } catch (Exception e) {
            Timber.e("error: %S", e.getMessage());
        }
    }


}
