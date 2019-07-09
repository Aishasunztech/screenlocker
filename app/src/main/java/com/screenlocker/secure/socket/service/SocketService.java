package com.screenlocker.secure.socket.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.socket.SocketManager;
import com.screenlocker.secure.socket.TransparentActivity;
import com.screenlocker.secure.socket.interfaces.OnSocketConnectionListener;
import com.screenlocker.secure.socket.interfaces.SocketEvents;
import com.screenlocker.secure.socket.model.ImeiModel;
import com.screenlocker.secure.socket.model.InstallModel;
import com.screenlocker.secure.socket.model.Settings;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static com.screenlocker.secure.app.MyApplication.getAppContext;
import static com.screenlocker.secure.mdm.utils.DeviceIdUtils.isValidImei;
import static com.screenlocker.secure.socket.utils.utils.changeSettings;
import static com.screenlocker.secure.socket.utils.utils.checkIMei;
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
import static com.screenlocker.secure.utils.AppConstants.ACTION_PULL_APPS;
import static com.screenlocker.secure.utils.AppConstants.ACTION_PUSH_APPS;
import static com.screenlocker.secure.utils.AppConstants.APPS_HASH_MAP;
import static com.screenlocker.secure.utils.AppConstants.APPS_SETTING_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.BROADCAST_APPS_ACTION;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.FINISHED_PULLED_APPS;
import static com.screenlocker.secure.utils.AppConstants.FINISHED_PUSHED_APPS;
import static com.screenlocker.secure.utils.AppConstants.FINISH_POLICY;
import static com.screenlocker.secure.utils.AppConstants.FINISH_POLICY_APPS;
import static com.screenlocker.secure.utils.AppConstants.FINISH_POLICY_EXTENSIONS;
import static com.screenlocker.secure.utils.AppConstants.FINISH_POLICY_PUSH_APPS;
import static com.screenlocker.secure.utils.AppConstants.FINISH_POLICY_SETTINGS;
import static com.screenlocker.secure.utils.AppConstants.FORCE_UPDATE_CHECK;
import static com.screenlocker.secure.utils.AppConstants.GET_APPLIED_SETTINGS;
import static com.screenlocker.secure.utils.AppConstants.GET_POLICY;
import static com.screenlocker.secure.utils.AppConstants.GET_PULLED_APPS;
import static com.screenlocker.secure.utils.AppConstants.GET_PUSHED_APPS;
import static com.screenlocker.secure.utils.AppConstants.GET_SYNC_STATUS;
import static com.screenlocker.secure.utils.AppConstants.IMEI1;
import static com.screenlocker.secure.utils.AppConstants.IMEI2;
import static com.screenlocker.secure.utils.AppConstants.IMEI_APPLIED;
import static com.screenlocker.secure.utils.AppConstants.IMEI_HISTORY;
import static com.screenlocker.secure.utils.AppConstants.IS_SYNCED;
import static com.screenlocker.secure.utils.AppConstants.KEY_DATABASE_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.KEY_DEVICE_LINKED;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.LOADING_POLICY;
import static com.screenlocker.secure.utils.AppConstants.LOAD_POLICY;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.PENDING_FINISH_DIALOG;
import static com.screenlocker.secure.utils.AppConstants.SECURE_SETTINGS_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.SEND_APPS;
import static com.screenlocker.secure.utils.AppConstants.SEND_EXTENSIONS;
import static com.screenlocker.secure.utils.AppConstants.SEND_PULLED_APPS_STATUS;
import static com.screenlocker.secure.utils.AppConstants.SEND_PUSHED_APPS_STATUS;
import static com.screenlocker.secure.utils.AppConstants.SEND_SETTINGS;
import static com.screenlocker.secure.utils.AppConstants.SETTINGS_APPLIED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.SETTINGS_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.TOKEN;
import static com.screenlocker.secure.utils.AppConstants.WRITE_IMEI;
import static com.screenlocker.secure.utils.Utils.getNotification;

public class SocketService extends Service implements OnSocketConnectionListener, SocketEvents {

    private SocketManager socketManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return localBinder;
    }


    public interface PolicyResponse {
        void onResponse(boolean status);
    }

    private PolicyResponse policyResponse;

    public void setListener(PolicyResponse policyResponse) {
        this.policyResponse = policyResponse;
    }

    private IBinder localBinder = new LocalBinder();


    public class LocalBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startService();

        socketManager = SocketManager.getInstance();
        socketManager.setSocketConnectionListener(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(appsBroadcast, new IntentFilter(BROADCAST_APPS_ACTION));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PUSH_APPS);
        intentFilter.addAction(ACTION_PULL_APPS);

        LocalBroadcastManager.getInstance(this).registerReceiver(pushPullBroadcast, intentFilter);


    }

    private void startService() {
        final NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (mNM != null) {
            Notification notification = getNotification(this, R.drawable.sync);
            startForeground(4577, notification);
        }
    }

    BroadcastReceiver pushPullBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null) {

                if (intent.getAction() != null && intent.getAction().equals(ACTION_PUSH_APPS)) {
                    boolean finishStatus = intent.getBooleanExtra("finish_status", false);
                    String packageName = intent.getStringExtra("PackageName");
                    boolean status = intent.getBooleanExtra("Status", false);
                    boolean isPolicy = intent.getBooleanExtra("isPolicy", false);
                    Map<String, Boolean> map = new HashMap<>();
                    map.put(packageName, status);

                    if (isPolicy && finishStatus) {
                        finishPolicyPushApps();
                    }

                    if (!isPolicy && finishStatus) {
                        finishPushedApps();
                    }

                    if (!isPolicy) {
                        sendPushedAppsStatus(map);
                    }

                } else if (intent.getAction() != null && intent.getAction().equals(ACTION_PULL_APPS)) {

                    boolean finishStatus = intent.getBooleanExtra("finish_status", false);
                    String packageName = intent.getStringExtra("PackageName");
                    boolean status = intent.getBooleanExtra("Status", false);
                    Map<String, Boolean> map = new HashMap<>();
                    map.put(packageName, status);
                    sendPulledAPpsStatus(map);

                    if (finishStatus) {
                        finishPulledApps();
                    }

                }


            }

        }
    };


    private String device_id;


    private BroadcastReceiver appsBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null)
                if (intent.getAction().equals(BROADCAST_APPS_ACTION)) {

                    String action = intent.getStringExtra(KEY_DATABASE_CHANGE);
                    Timber.d("djgdsgsggjiodig");

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

                                if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
                                    socketManager.getSocket().emit(SETTINGS_APPLIED_STATUS + device_id, new JSONObject().put("device_id", device_id));
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
                        String live_url = PrefUtils.getStringPref(SocketService.this, LIVE_URL);
                        socketManager.destroy();
                        socketManager.connectSocket(token, device_id, live_url);

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
        new ApiUtils(SocketService.this, DeviceIdUtils.generateUniqueDeviceId(this), DeviceIdUtils.getSerialNumber());
    }

    @Override
    public void onSocketConnectionStateChange(int socketState) {


        if (socketState == 1) {
            Timber.d("Socket is connecting");

        } else if (socketState == 2) {
            Timber.d("Socket is connected");
            getSyncStatus();
            getPolicy();
            getDeviceStatus();
            getAppliedSettings();
            getPushedApps();
            getPulledApps();
            writeImei();
            imeiHistory();
            forceUpdateCheck();

            if (PrefUtils.getStringPref(this, APPS_HASH_MAP)
                    != null) {
                Type type = new TypeToken<HashMap<String, Boolean>>() {
                }.getType();
                String hashmap = PrefUtils.getStringPref(this, APPS_HASH_MAP);
                HashMap<String, Boolean> map = new Gson().fromJson(hashmap, type);
                sendPushedAppsStatus(map);
                PrefUtils.saveStringPref(this, APPS_HASH_MAP, null);
            }

        } else if (socketState == 3) {
            Timber.d("Socket is disconnected");

            if (socketManager.getSocket() != null) {
                socketManager.getSocket().off(GET_SYNC_STATUS + device_id);
                socketManager.getSocket().off(GET_APPLIED_SETTINGS + device_id);
                socketManager.getSocket().off(DEVICE_STATUS + device_id);
                socketManager.getSocket().off(GET_PUSHED_APPS + device_id);
                socketManager.getSocket().off(WRITE_IMEI + device_id);
                socketManager.getSocket().off(GET_PUSHED_APPS + device_id);
                socketManager.getSocket().off(GET_PULLED_APPS + device_id);
                socketManager.getSocket().off(GET_POLICY + device_id);
                socketManager.getSocket().off(FORCE_UPDATE_CHECK + device_id);
            }


        }

    }

    @Override
    public void onInternetConnectionStateChange(int socketState) {
        switch (socketState) {
            case 1:
                Timber.d("Socket is connecting");
                break;
            case 2:
//                Timber.d("Socket is connected");

                break;
            case 3:
                Timber.d("Socket is disconnected");
                break;
        }
    }


    @Override
    public void getSyncStatus() {

        try {

            if (socketManager.getSocket().connected()) {

                socketManager.getSocket().on(GET_SYNC_STATUS + device_id, args -> {
                    Timber.d("<<< GETTING SYNC STATUS >>>");
                    JSONObject obj = (JSONObject) args[0];
                    try {
                        if (validateRequest(device_id, obj.getString("device_id"))) {
                            Timber.e(" valid request ");

                            Timber.d(obj.toString());

                            boolean is_synced = obj.getBoolean("is_sync");
                            boolean apps = obj.getBoolean("apps_status");
                            boolean extensions = obj.getBoolean("extensions_status");
                            boolean settings = obj.getBoolean("settings_status");

                            syncDevice(SocketService.this, is_synced, apps, extensions, settings);

                            if (!PrefUtils.getBooleanPref(SocketService.this, AppConstants.IS_SYNCED)) {

                                if (!PrefUtils.getBooleanPref(SocketService.this, AppConstants.APPS_SENT_STATUS)) {
                                    sendApps();
                                } else if (!PrefUtils.getBooleanPref(SocketService.this, AppConstants.EXTENSIONS_SENT_STATUS)) {
                                    sendExtensions();
                                } else if (!PrefUtils.getBooleanPref(SocketService.this, AppConstants.SETTINGS_SENT_STATUS)) {
                                    sendSettings();
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
    public void getAppliedSettings() {

        if (socketManager.getSocket().connected()) {
            socketManager.getSocket().on(GET_APPLIED_SETTINGS + device_id, args -> {
                Timber.d("<<< GETTING APPLIED SETTINGS >>>");


                JSONObject obj = (JSONObject) args[0];

                try {
                    if (validateRequest(device_id, obj.getString("device_id"))) {

                        Timber.d(" valid request ");
                        boolean status = obj.getBoolean("status");
                        Timber.d(" applied settings status : %S", status);

                        if (status) {

                            Timber.d(obj.toString());

                            updatePassword(obj);

                            updateSettings(obj, false);

                            updateExtensions(obj, false);

                            updateApps(obj, false);

                            sendAppliedStatus();

                            setScreenLock();

                            Timber.d(" settings applied status sent ");

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
                                sendSettings();
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
        }
    }

    private void setScreenLock() {
        Intent intent = new Intent(SocketService.this, LockScreenService.class);

        intent.setAction("locked");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void updateSettings(JSONObject obj, boolean isPolicy) throws JSONException {
        String settings = obj.getString("settings");

        if (!settings.equals("{}") && !isPolicy) {
            changeSettings(SocketService.this, new Gson().fromJson(settings, Settings.class));
            Timber.d(" settings applied ");
        }

        if (isPolicy) {
            finishPolicySettings();
        }
    }

    private void updatePassword(JSONObject obj) throws JSONException {
        String passwords = obj.getString("passwords");
        if (!passwords.equals("{}")) {
            updatePasswords(SocketService.this, new JSONObject(passwords));
            Timber.d(" passwords updated ");
        }
    }

    @Override
    public void sendApps() {
        Timber.d("<<< sending apps >>>");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socketManager.getSocket().connected()) {
                        List<AppInfo> apps = MyApplication.getAppDatabase(SocketService.this).getDao().getApps();
                        socketManager.getSocket().emit(SEND_APPS + device_id, new Gson().toJson(apps));
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


        LocalBroadcastManager.getInstance(this).unregisterReceiver(appsBroadcast);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushPullBroadcast);

        Log.d("SocketService", "service destroy");

        socketManager.destroy();

        socketManager.removeSocketConnectionListener(this);
        socketManager.removeAllSocketConnectionListener();

        super.onDestroy();

    }


    @Override
    public void sendExtensions() {
        Timber.d("<<< Sending Extensions >>>");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socketManager.getSocket().connected()) {

                        List<SubExtension> extensions = MyApplication.getAppDatabase(SocketService.this).getDao().getAllSubExtensions();

                        socketManager.getSocket().emit(SEND_EXTENSIONS + device_id, new Gson().toJson(extensions));

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
            if (socketManager.getSocket().connected()) {

                socketManager.getSocket().on(DEVICE_STATUS + device_id, args -> {
                    Timber.d("<<< GETTING DEVICE STATUS >>>");

                    JSONObject object = (JSONObject) args[0];
                    try {
                        if (validateRequest(device_id, object.getString("device_id"))) {
                            Timber.d("<<< valid request >>>");
                            String msg = object.getString("msg");
                            Timber.e("<<< device status =>>> %S", msg);
                            if (msg != null) {
                                switch (msg) {
                                    case "suspended":
                                        suspendedDevice(SocketService.this, "suspended");
                                        Timber.d("<<< device suspended >>>");
                                        break;
                                    case "active":
                                        unSuspendDevice(SocketService.this);
                                        Timber.d("<<< device activated >>>");
                                        break;
                                    case "expired":
                                        suspendedDevice(SocketService.this, "expired");
                                        Timber.d("<<< device expired >>>");
                                        break;
                                    case "unlinked":
                                        Timber.d("<<< device unlinked >>>");
                                        unlinkDevice(SocketService.this, true);
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
            if (socketManager.getSocket().connected()) {
                socketManager.getSocket().emit(SEND_SETTINGS + device_id, new Gson().toJson(getCurrentSettings(SocketService.this)));
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
            if (socketManager.getSocket().connected()) {
                socketManager.getSocket().emit(SETTINGS_APPLIED_STATUS + device_id, new JSONObject().put("device_id", device_id));
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

                    if (socketManager.getSocket().connected()) {

                        socketManager.getSocket().emit(SEND_APPS + device_id, new Gson().toJson(MyApplication.getAppDatabase(SocketService.this).getDao().getAppsWithoutIcons()));
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
        new Thread(() -> {
            try {

                if (socketManager.getSocket().connected()) {
                    socketManager.getSocket().emit(SEND_EXTENSIONS + device_id, new Gson().toJson(MyApplication.getAppDatabase(SocketService.this).getDao().getExtensionsWithoutIcons()));
                    PrefUtils.saveBooleanPref(SocketService.this, SECURE_SETTINGS_CHANGE, false);

                    Timber.d("Extensions sent");
                } else {
                    Timber.d("Socket not connected");
                }

            } catch (Exception e) {
                Timber.e("error: %S", e.getMessage());
            }
        }).start();
    }

    @Override
    public void getPushedApps() {

        try {
            if (socketManager.getSocket().connected()) {
                socketManager.getSocket().on(GET_PUSHED_APPS + device_id, args -> {
                    Timber.d("<<< GETTING PUSHED APPS>>>");

                    JSONObject object = (JSONObject) args[0];

                    pushedApps(object, "push_apps", "com.secure.systemcontrol.INSTALL_PACKAGES", "com.secure.systemcontrol.receivers.PackagesInstallReceiver", false);
                });
            } else {
                Timber.d("Socket not connected");
            }


        } catch (Exception e) {
            Timber.d(e);
        }
    }

    private void pushedApps(JSONObject object, String push_apps, String s, String s2, boolean isPolicy) {
        try {
            if (validateRequest(device_id, object.getString("device_id"))) {

                String pushedApps = object.getString(push_apps);
                Timber.d(pushedApps);


                if (!pushedApps.equals("[]")) {

                    Type listType = new TypeToken<ArrayList<InstallModel>>() {
                    }.getType();

                    List<InstallModel> list = new Gson().fromJson(pushedApps, listType);

                    for (int i = 0; i < list.size(); i++) {

                        InstallModel item = list.get(i);
                        String apk = item.getApk();

                        String live_url = PrefUtils.getStringPref(MyApplication.getAppContext(), LIVE_URL);
                        String url = live_url + MOBILE_END_POINT + "getApk/" + CommonUtils.splitName(apk);
                        item.setApk(url);
                        item.setToken(PrefUtils.getStringPref(this, PrefUtils.getStringPref(SocketService.this, TOKEN)));
                        list.set(i, item);
                    }

                    String apps = new Gson().toJson(list);

                    final Intent intent = new Intent();
                    intent.setAction(s);
                    intent.putExtra("json", apps);
                    intent.putExtra("isPolicy", isPolicy);
                    Timber.d("isPolicy %s", isPolicy);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    intent.setComponent(new ComponentName("com.secure.systemcontrol", s2));
                    sendBroadcast(intent);

                } else {
                    if (isPolicy) {
                        finishPolicyPushApps();
                    }
                }
            } else {
                Timber.d("Invalid request");
            }
        } catch (JSONException e) {
            Timber.d(e);
        }
    }

    @Override
    public void getPulledApps() {

        try {
            if (socketManager.getSocket().connected()) {
                socketManager.getSocket().on(GET_PULLED_APPS + device_id, args -> {
                    Timber.d("<<< GETTING PULLED APPS>>>");
                    JSONObject object = (JSONObject) args[0];
                    pushedApps(object, "pull_apps", "com.secure.systemcontrol.DELETE_PACKAGES", "com.secure.systemcontrol.receivers.PackageUninstallReceiver", false);
                });
            } else {
                Timber.d("Socket not connected");
            }


        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @Override
    public void sendPushedAppsStatus(Map<String, Boolean> hashMap) {

        Timber.d("<<< Pushed apps status sending >>>");

        if (socketManager.getSocket() != null) {
            if (socketManager.getSocket().connected()) {
                try {
                    JSONArray jsonArray = new JSONArray();

                    for (Map.Entry<String, Boolean> entry : hashMap.entrySet()) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("package_name", entry.getKey());
                        jsonObject.put("status", entry.getValue());
                        jsonArray.put(jsonObject);
                    }

                    socketManager.getSocket().emit(SEND_PUSHED_APPS_STATUS + device_id, jsonArray.toString());
                } catch (JSONException e) {
                    Timber.d(e);
                }

            }
        }
    }

    @Override
    public void sendPulledAPpsStatus(Map<String, Boolean> hashMap) {
        Timber.d("<<< Pulled apps status sending >>>");

        if (socketManager.getSocket() != null) {
            if (socketManager.getSocket().connected()) {
                try {
                    JSONArray jsonArray = new JSONArray();

                    for (Map.Entry<String, Boolean> entry : hashMap.entrySet()) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("package_name", entry.getKey());
                        jsonObject.put("status", entry.getValue());
                        jsonArray.put(jsonObject);
                    }

                    socketManager.getSocket().emit(SEND_PULLED_APPS_STATUS + device_id, jsonArray.toString());
                } catch (JSONException e) {
                    Timber.d(e);
                }

            }
        }
    }


    @Override
    public void finishPushedApps() {
        Timber.d("<<<Finish pushed apps>>>");
        if (socketManager.getSocket() != null) {
            if (socketManager.getSocket().connected()) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("status", true);
                    socketManager.getSocket().emit(FINISHED_PUSHED_APPS + device_id, jsonObject);
                    setScreenLock();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void finishPulledApps() {
        Timber.d("<<<Finish pulled apps>>>");
        if (socketManager.getSocket() != null) {
            if (socketManager.getSocket().connected()) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("status", true);
                    socketManager.getSocket().emit(FINISHED_PULLED_APPS + device_id, jsonObject);
                    setScreenLock();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void writeImei() {

        try {
            if (socketManager.getSocket().connected()) {

                socketManager.getSocket().on(WRITE_IMEI + device_id, args -> {

                    Timber.d("<<< IMEI CHANGED>>>");

                    JSONObject object = (JSONObject) args[0];

                    try {
                        if (validateRequest(device_id, object.getString("device_id"))) {

                            Timber.d(args[0].toString());
                            String imeiList = object.getString("imei");
                            Timber.d(imeiList);
                            if (!imeiList.equals("{}")) {

                                List<String> imei = DeviceIdUtils.getIMEI(getAppContext());


                                if (imei != null && imei.size() >= 1) {
                                    PrefUtils.saveStringPref(this, IMEI1, imei.get(0));
                                }

                                if (imei != null && imei.size() >= 2) {
                                    PrefUtils.saveStringPref(this, IMEI2, imei.get(1));
                                }


                                Type imeiModel = new TypeToken<ImeiModel>() {
                                }.getType();

                                ImeiModel imeis = new Gson().fromJson(imeiList, imeiModel);


                                if (imeis.getImei1() != null) {
                                    Timber.d("imei 1 is changed");
                                    sendIntent(0, imeis.getImei1(), this);
                                }
                                if (imeis.getImei2() != null) {
                                    Timber.d("imei 2 is changed");
                                    sendIntent(1, imeis.getImei2(), this);
                                }


                                Intent intent = new Intent(SocketService.this, TransparentActivity.class);
                                startActivity(intent);
                                imeiApplied();
                            }
                        } else {
                            Timber.d("Invalid request");
                        }
                    } catch (JSONException e) {
                        Timber.d(e);
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
    public void imeiApplied() {

        if (socketManager.getSocket() != null) {
            if (socketManager.getSocket().connected()) {
                Timber.d("<<<Imei applied >>>");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("status", true);
                    jsonObject.put("device_id", device_id);
                    socketManager.getSocket().emit(IMEI_APPLIED + device_id, jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void imeiHistory() {


        if (socketManager.getSocket().connected() && checkIMei(this)) {


            Timber.d("<<<IMEI HISTORY >>> ");

            List<String> imeis = DeviceIdUtils.getIMEI(this);
            String imei1 = null;
            String imei2 = null;

            if (imeis != null && imeis.size() > 0) {
                imei1 = imeis.get(0);

            }
            if (imeis != null && imeis.size() > 1) {
                imei2 = imeis.get(1);

            }


            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("device_id", device_id);
                if (imei1 != null) {
                    jsonObject.put("imei1", imei1);
                }
                if (imei2 != null) {
                    jsonObject.put("imei2", imei2);
                }

                String serial = DeviceIdUtils.getSerialNumber();
                String mac = DeviceIdUtils.generateUniqueDeviceId(this);

                jsonObject.put("serial", serial);
                jsonObject.put("mac", mac);
                socketManager.getSocket().emit(IMEI_HISTORY + device_id, jsonObject);


            } catch (JSONException e) {
                Timber.e(e);
            }
        }
    }


    @Override
    public void loadPolicy(String policyName) {

        Timber.d("<<< Load policy >>>");
        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {


            try {
                JSONObject object = new JSONObject();

                String link_code = PrefUtils.getStringPref(SocketService.this, KEY_DEVICE_LINKED);

                Timber.d("%s", link_code);
                object.put("device_id", device_id);
                object.put("link_code", link_code);
                object.put("policy_name", policyName);

                if (policyName.equals("default_policy")) {
                    object.put("is_default", true);
                } else {
                    object.put("is_default", false);
                }

                socketManager.getSocket().emit(LOAD_POLICY + device_id, object);

            } catch (JSONException e) {
                Timber.d(e);
            }

        }
    }

    @Override
    public void getPolicy() {
        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
            socketManager.getSocket().on(GET_POLICY + device_id, args -> {
                Timber.d("<<< GETTING POLICY >>>");

                JSONObject object = (JSONObject) args[0];


                try {
                    if (validateRequest(device_id, object.getString("device_id"))) {
                        if (object.getBoolean("status")) {

                            if (policyResponse != null) {
                                policyResponse.onResponse(true);
                            }

                            updateSettings(object, true);

                            updateExtensions(object, true);

                            updateApps(object, true);

                            pushedApps(object, "push_apps", "com.secure.systemcontrol.INSTALL_PACKAGES", "com.secure.systemcontrol.receivers.PackagesInstallReceiver", true);


                        } else {
                            if (policyResponse != null) {
                                policyResponse.onResponse(false);

                            }
                        }

                    }
                } catch (JSONException e) {
                    Timber.d(e);
                }
            });

        }
    }

    @Override
    public void forceUpdateCheck() {

        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
            socketManager.getSocket().on(FORCE_UPDATE_CHECK + device_id, args -> {
                Timber.d("<<< CHECKING FORCE UPDATE >>>");

                JSONObject jsonObject = (JSONObject) args[0];
                try {

                    if (validateRequest(device_id, jsonObject.getString("device_id"))) {

                        boolean status = jsonObject.getBoolean("status");
                        if (status) {
                            final Intent intent = new Intent();
                            intent.setAction("com.secure.systemcontrol.CHECK_FOR_UPDATE");
                            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            intent.putExtra("packageName", getPackageName());
                            intent.putExtra("isForce", true);
                            intent.setComponent(new ComponentName("com.secure.systemcontrol", "com.secure.systemcontrol.receivers.PackagesInstallReceiver"));
                            sendBroadcast(intent);
                        }

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public void finishPolicyPushApps() {
        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
            Timber.d("<<< FINISH POLICY PUSH APPS>>>");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("device_id", device_id);
                jsonObject.put("status", true);
                socketManager.getSocket().emit(FINISH_POLICY_PUSH_APPS + device_id, jsonObject);
                finishPolicy();
            } catch (JSONException e) {
                Timber.d(e);
            }
        }
    }

    @Override
    public void finishPolicyApps() {
        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
            Timber.d("<<< FINISH POLICY APPS>>>");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("device_id", device_id);
                jsonObject.put("status", true);
                socketManager.getSocket().emit(FINISH_POLICY_APPS + device_id, jsonObject);
            } catch (JSONException e) {
                Timber.d(e);
            }
        }
    }

    @Override
    public void finishPolicySettings() {
        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
            Timber.d("<<< FINISH POLICY SETTINGS >>>");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("device_id", device_id);
                jsonObject.put("status", true);
                socketManager.getSocket().emit(FINISH_POLICY_SETTINGS + device_id, jsonObject);
            } catch (JSONException e) {
                Timber.d(e);
            }
        }
    }

    @Override
    public void finishPolicyExtensions() {
        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
            Timber.d("<<< FINISH POLICY EXTENSIONS >>>");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("device_id", device_id);
                jsonObject.put("status", true);
                socketManager.getSocket().emit(FINISH_POLICY_EXTENSIONS + device_id, jsonObject);
            } catch (JSONException e) {
                Timber.d(e);
            }
        }
    }

    @Override
    public void finishPolicy() {
        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
            Timber.d("<<< FINISH POLICY >>>");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("device_id", device_id);
                jsonObject.put("status", true);
                socketManager.getSocket().emit(FINISH_POLICY + device_id, jsonObject);
                PrefUtils.saveBooleanPref(this, LOADING_POLICY, false);
                PrefUtils.saveBooleanPref(this, PENDING_FINISH_DIALOG, true);
                Intent intent = new Intent(FINISH_POLICY);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                setScreenLock();
            } catch (JSONException e) {
                Timber.d(e);
            }
        }
    }

    private void updateExtensions(JSONObject object, boolean isPolicy) throws JSONException {
        String extensionList = object.getString("extension_list");

        if (!extensionList.equals("[]")) {

            JSONArray jsonArray = new JSONArray(extensionList);

            updateExtensionsList(SocketService.this, jsonArray, () -> {
                Timber.d(" extensions updated ");
            }, isPolicy);
        }

        if (isPolicy) {
            finishPolicyExtensions();
        }

    }

    private void updateApps(JSONObject object, boolean isPolicy) throws JSONException {

        String appsList = object.getString("app_list");

        if (!appsList.equals("[]")) {
            updateAppsList(SocketService.this, new JSONArray(appsList), () -> {
                Timber.d(" apps updated ");
            }, isPolicy);
        }

        if (isPolicy) {
            finishPolicyApps();
        }
    }


    public void onLoadPolicy(String policyName) {
        Timber.d("<<< On load policy >>>");
        Timber.d(policyName);
        loadPolicy(policyName);
    }

    private void sendIntent(int slot, String imei, Context context) {

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
            sendBroadcast(intent);


        }
    }


}
