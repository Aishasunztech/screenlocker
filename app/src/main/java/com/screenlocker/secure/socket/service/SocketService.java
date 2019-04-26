package com.screenlocker.secure.socket.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.socket.interfaces.SocketEvents;
import com.screenlocker.secure.socket.model.Settings;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.socket.OnSocketConnectionListener;
import com.screenlocker.secure.socket.SocketManager;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import timber.log.Timber;

import static com.screenlocker.secure.socket.utils.utils.changeSettings;
import static com.screenlocker.secure.socket.utils.utils.getCurrentSettings;
import static com.screenlocker.secure.socket.utils.utils.suspendedDevice;
import static com.screenlocker.secure.socket.utils.utils.syncDevice;
import static com.screenlocker.secure.socket.utils.utils.unSuspendDevice;
import static com.screenlocker.secure.socket.utils.utils.unlinkDevice;
import static com.screenlocker.secure.socket.utils.utils.updateAppsList;
import static com.screenlocker.secure.socket.utils.utils.updateExtensionsList;
import static com.screenlocker.secure.socket.utils.utils.updatePasswords;
import static com.screenlocker.secure.socket.utils.utils.validateRequest;
import static com.screenlocker.secure.socket.utils.utils.wipeDevice;
import static com.screenlocker.secure.utils.AppConstants.APPS_SETTING_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.BROADCAST_APPS_ACTION;
import static com.screenlocker.secure.utils.AppConstants.BROADCAST_DATABASE;
import static com.screenlocker.secure.utils.AppConstants.DB_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.GET_APPLIED_SETTINGS;
import static com.screenlocker.secure.utils.AppConstants.GET_SYNC_STATUS;
import static com.screenlocker.secure.utils.AppConstants.IS_SYNCED;
import static com.screenlocker.secure.utils.AppConstants.KEY_DATABASE_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.SECURE_SETTINGS_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.SEND_APPS;
import static com.screenlocker.secure.utils.AppConstants.SEND_EXTENSIONS;
import static com.screenlocker.secure.utils.AppConstants.SEND_SETTINGS;
import static com.screenlocker.secure.utils.AppConstants.SETTINGS_APPLIED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.SETTINGS_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.TOKEN;
import static com.screenlocker.secure.utils.Utils.getNotification;

public class SocketService extends Service implements OnSocketConnectionListener, SocketEvents {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startService();
        SocketManager.getInstance().setSocketConnectionListener(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(appsBroadcast, new IntentFilter(BROADCAST_APPS_ACTION));
        LocalBroadcastManager.getInstance(this).registerReceiver(databaseBroadcast, new IntentFilter(BROADCAST_DATABASE));

    }

    private void startService() {
        final NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (mNM != null) {
            Notification notification = getNotification(this, R.drawable.sync);
            startForeground(4577, notification);
        }
    }

    BroadcastReceiver databaseBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                if (PrefUtils.getBooleanPref(SocketService.this, DEVICE_LINKED_STATUS)) {
                    if (socket != null) {
                        if (socket.connected()) {
                            if (!PrefUtils.getBooleanPref(SocketService.this, AppConstants.IS_SYNCED)) {
                                if (!PrefUtils.getBooleanPref(SocketService.this, AppConstants.APPS_SENT_STATUS)) {
                                    sendApps();
                                } else if (!PrefUtils.getBooleanPref(SocketService.this, AppConstants.EXTENSIONS_SENT_STATUS)) {
                                    sendExtensions();
                                } else if (!PrefUtils.getBooleanPref(SocketService.this, AppConstants.SETTINGS_SENT_STATUS)) {
                                    sendSettings();
                                }
                            }
                        }
                    }
                }

            } catch (Exception ignored) {
            }

        }
    };

    Socket socket;
    String device_id;


    private BroadcastReceiver appsBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null)
                if (intent.getAction().equals(BROADCAST_APPS_ACTION)) {

                    String action = intent.getStringExtra(KEY_DATABASE_CHANGE);


                    if (action != null) {
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

                            try {

                                if (socket != null && socket.connected()) {
                                    socket.emit(SETTINGS_APPLIED_STATUS + device_id, new JSONObject().put("device_id", device_id));
                                }

                            } catch (Exception e) {
                                Timber.d(e);
                            }


                        }
                    }


                }
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            String token = PrefUtils.getStringPref(SocketService.this, TOKEN);
            device_id = PrefUtils.getStringPref(SocketService.this, DEVICE_ID);

            if (token != null && device_id != null && action != null) {
                switch (action) {
                    case "start":
                        // connecting to socket
                        SocketManager.getInstance().connectSocket(token, device_id, AppConstants.SOCKET_SERVER_URL);
                        break;
                }
            } else {
                stopSelf();
            }


        }

        return START_STICKY;
    }


    @Override
    public void onSocketEventFailed() {
        Timber.d("Socket event failed");
    }

    @Override
    public void onSocketConnectionStateChange(int socketState) {
        switch (socketState) {
            case 1:
                Timber.d("Socket is connecting");
                break;
            case 2:
                Timber.d("Socket is connected");
                socket = SocketManager.getInstance().getSocket();
                getSyncStatus();
                getDeviceStatus();
                sendAppliedStatus();

                break;
            case 3:
                Timber.d("Socket is disconnected");
                try {
                    socket.off(GET_APPLIED_SETTINGS + device_id);
                    socket.off(GET_SYNC_STATUS + device_id);
                    socket.off(DEVICE_STATUS + device_id);
                } catch (Exception e) {
                    Timber.d(e.getMessage());
                }
                break;
        }
    }

    @Override
    public void onInternetConnectionStateChange(int socketState) {
        switch (socketState) {
            case 1:
                Timber.d("Socket is connecting");
                break;
            case 2:
                Timber.d("Socket is connected");

                break;
            case 3:
                Timber.d("Socket is disconnected");
                break;
        }
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

                            if (PrefUtils.getBooleanPref(SocketService.this, DB_STATUS)) {

                                if (!PrefUtils.getBooleanPref(SocketService.this, AppConstants.IS_SYNCED)) {
                                    if (!PrefUtils.getBooleanPref(SocketService.this, AppConstants.APPS_SENT_STATUS)) {
                                        sendApps();
                                    } else if (!PrefUtils.getBooleanPref(SocketService.this, AppConstants.EXTENSIONS_SENT_STATUS)) {
                                        sendExtensions();
                                    } else if (!PrefUtils.getBooleanPref(SocketService.this, AppConstants.SETTINGS_SENT_STATUS)) {
                                        sendSettings();
                                    }
                                }

                            }

                            syncDevice(SocketService.this, is_synced, apps, extensions, settings);

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
    public void getAppliedSettings() {

    }

    @Override
    public void sendApps() {
        Timber.d("<<< sending apps >>>");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket.connected()) {
                        List<AppInfo> apps = MyApplication.getAppDatabase(SocketService.this).getDao().getApps();
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
    public void onDestroy() {
        super.onDestroy();


        LocalBroadcastManager.getInstance(this).unregisterReceiver(appsBroadcast);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(databaseBroadcast);


        if (socket != null) {
            socket.off(GET_APPLIED_SETTINGS + device_id);
            socket.off(GET_SYNC_STATUS + device_id);
            socket.off(DEVICE_STATUS + device_id);
            SocketManager.getInstance().destroy();
        }

    }


    @Override
    public void sendExtensions() {
        Timber.d("<<< Sending Extensions >>>");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket.connected()) {

                        List<SubExtension> extensions = MyApplication.getAppDatabase(SocketService.this).getDao().getAllSubExtensions();

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
    public void getDeviceStatus() {
        try {
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
                                        suspendedDevice(SocketService.this, device_id, "suspended");
                                        utils.sendBroadcast(SocketService.this, "suspended");
                                        Timber.d("<<< device suspended >>>");
                                        break;
                                    case "active":
                                        unSuspendDevice(SocketService.this);
                                        utils.sendBroadcast(SocketService.this, null);
                                        Timber.d("<<< device activated >>>");
                                        break;
                                    case "expired":
                                        suspendedDevice(SocketService.this, device_id, "expired");
                                        utils.sendBroadcast(SocketService.this, "expired");
                                        Timber.d("<<< device expired >>>");
                                        break;
                                    case "unlinked":
                                        Timber.d("<<< device unlinked >>>");
                                        unlinkDevice(SocketService.this);
                                        break;
                                    case "wiped":
                                        Timber.d("<<< device wiped >>>");
                                        wipeDevice(SocketService.this);
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
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @Override
    public void sendSettings() {
        Timber.d("<<< Sending settings >>>");

        try {
            if (socket.connected()) {
                socket.emit(SEND_SETTINGS + device_id, new Gson().toJson(getCurrentSettings(SocketService.this)));
                PrefUtils.saveBooleanPref(SocketService.this, SETTINGS_CHANGE, false);
            } else {
                Timber.d("Socket not connected");
            }
        } catch (Exception e) {
            Timber.d(e);
        }

    }

    @Override
    public void sendAppliedStatus() {
        try {
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
                                    updateAppsList(SocketService.this, new JSONArray(appsList), () -> {
                                        Timber.d(" apps updated ");
                                    });
                                }
                                String passwords = obj.getString("passwords");
                                if (!passwords.equals("{}")) {
                                    updatePasswords(SocketService.this, new JSONObject(passwords));
                                    Timber.d(" passwords updated ");
                                }
                                String settings = obj.getString("settings");

                                if (!settings.equals("{}")) {
                                    changeSettings(SocketService.this, new Gson().fromJson(settings, Settings.class));
                                    Timber.d(" settings applied ");
                                }

                                String extensionList = obj.getString("extension_list");


                                if (!extensionList.equals("[]")) {

                                    JSONArray jsonArray = new JSONArray(extensionList);

                                    updateExtensionsList(SocketService.this, jsonArray, () -> {
                                        Timber.d(" extensions updated ");
                                    });


                                }

                                socket.emit(SETTINGS_APPLIED_STATUS + device_id, new JSONObject().put("device_id", device_id));


                                Timber.d(" settings applied status sent ");

                                Intent intent = new Intent(SocketService.this, LockScreenService.class);

                                intent.setAction("locked");

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    startForegroundService(intent);
                                } else {
                                    startService(intent);
                                }


                            } else {
                                Timber.d(" no settings available in history ");

                                boolean appsSettingStatus = PrefUtils.getBooleanPref(SocketService.this, APPS_SETTING_CHANGE);
                                Timber.d(" apps settings status in local : %S", appsSettingStatus);

                                if (appsSettingStatus) {
                                    sendAppsWithoutIcons();
                                }

                                boolean settingsStatus = PrefUtils.getBooleanPref(SocketService.this, SETTINGS_CHANGE);
                                Timber.d(" settings status in local : %S", settingsStatus);
                                if (settingsStatus) {
                                    sendApps();
                                }

                                boolean extensionsStatus = PrefUtils.getBooleanPref(SocketService.this, SECURE_SETTINGS_CHANGE);
                                Timber.d(" extensions status in local : %S", extensionsStatus);
                                if (extensionsStatus) {
                                    sendExtensionsWithoutIcons();
                                }


                            }

                        } else {
                            Timber.e(" invalid request ");
                        }

                    } catch (Exception error) {
                        Timber.e(" error : %s", error.getMessage());
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
    public void sendAppsWithoutIcons() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    if (socket.connected()) {

                        socket.emit(SEND_APPS + device_id, new Gson().toJson(MyApplication.getAppDatabase(SocketService.this).getDao().getAppsWithoutIcons()));
                        PrefUtils.saveBooleanPref(SocketService.this, APPS_SETTING_CHANGE, false);

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
                        socket.emit(SEND_EXTENSIONS + device_id, new Gson().toJson(MyApplication.getAppDatabase(SocketService.this).getDao().getExtensionsWithoutIcons()));
                        PrefUtils.saveBooleanPref(SocketService.this, SECURE_SETTINGS_CHANGE, false);

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
}
