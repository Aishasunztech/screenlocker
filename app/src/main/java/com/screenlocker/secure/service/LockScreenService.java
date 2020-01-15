package com.screenlocker.secure.service;

import android.Manifest;
import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.network.NetworkChangeReceiver;
import com.screenlocker.secure.notifications.NotificationItem;
import com.screenlocker.secure.room.SimEntry;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.settings.managepassword.NCodeView;
import com.screenlocker.secure.socket.SocketManager;
import com.screenlocker.secure.socket.TransparentActivity;
import com.screenlocker.secure.socket.interfaces.OnSocketConnectionListener;
import com.screenlocker.secure.socket.interfaces.SocketEvents;
import com.screenlocker.secure.socket.model.BooleanTypeAdapter;
import com.screenlocker.secure.socket.model.DeviceMessagesModel;
import com.screenlocker.secure.socket.model.ImeiModel;
import com.screenlocker.secure.socket.model.InstallModel;
import com.screenlocker.secure.socket.model.Settings;
import com.screenlocker.secure.socket.model.UnRegisterModel;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.updateDB.BlurWorker;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Utils;
import com.screenlocker.secure.views.PrepareLockScreen;
import com.screenlocker.secure.views.patternlock.PatternLockView;
import com.secure.launcher.R;
import com.secureMarket.DownloadStatusCls;
import com.secureMarket.SMActivity;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2core.DownloadBlock;
import com.tonyodev.fetch2core.Extras;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import timber.log.Timber;

import static android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES;
import static com.screenlocker.secure.app.MyApplication.getAppContext;
import static com.screenlocker.secure.mdm.utils.DeviceIdUtils.isValidImei;
import static com.screenlocker.secure.socket.utils.utils.changeSettings;
import static com.screenlocker.secure.socket.utils.utils.checkIMei;
import static com.screenlocker.secure.socket.utils.utils.saveAppsList;
import static com.screenlocker.secure.socket.utils.utils.scheduleUpdateJob;
import static com.screenlocker.secure.socket.utils.utils.suspendedDevice;
import static com.screenlocker.secure.socket.utils.utils.syncDevice;
import static com.screenlocker.secure.socket.utils.utils.unSuspendDevice;
import static com.screenlocker.secure.socket.utils.utils.unlinkDeviceWithMsg;
import static com.screenlocker.secure.socket.utils.utils.updateAppsList;
import static com.screenlocker.secure.socket.utils.utils.updateExtensionsList;
import static com.screenlocker.secure.socket.utils.utils.updatePasswords;
import static com.screenlocker.secure.socket.utils.utils.validateRequest;
import static com.screenlocker.secure.socket.utils.utils.verifySettings;
import static com.screenlocker.secure.socket.utils.utils.wipeDevice;
import static com.screenlocker.secure.utils.AppConstants.ACTION_DEVICE_TYPE_VERSION;
import static com.screenlocker.secure.utils.AppConstants.ACTION_PULL_APPS;
import static com.screenlocker.secure.utils.AppConstants.ACTION_PUSH_APPS;
import static com.screenlocker.secure.utils.AppConstants.ACTION_WIPE;
import static com.screenlocker.secure.utils.AppConstants.ALLOW_ENCRYPTED_ALL;
import static com.screenlocker.secure.utils.AppConstants.ALLOW_GUEST_ALL;
import static com.screenlocker.secure.utils.AppConstants.APPS_HASH_MAP;
import static com.screenlocker.secure.utils.AppConstants.APPS_SETTING_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.BROADCAST_APPS_ACTION;
import static com.screenlocker.secure.utils.AppConstants.CONNECTED;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_NETWORK_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEFAULT_MAIN_PASS;
import static com.screenlocker.secure.utils.AppConstants.DELETED_ICCIDS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DOWNLAOD_HASH_MAP;
import static com.screenlocker.secure.utils.AppConstants.DUPLICATE_MAC;
import static com.screenlocker.secure.utils.AppConstants.DUPLICATE_MAC_AND_SERIAL;
import static com.screenlocker.secure.utils.AppConstants.DUPLICATE_SERIAL;
import static com.screenlocker.secure.utils.AppConstants.EXTRA_FILE_PATH;
import static com.screenlocker.secure.utils.AppConstants.EXTRA_INSTALL_APP;
import static com.screenlocker.secure.utils.AppConstants.EXTRA_MARKET_FRAGMENT;
import static com.screenlocker.secure.utils.AppConstants.EXTRA_PACKAGE_NAME;
import static com.screenlocker.secure.utils.AppConstants.EXTRA_REQUEST;
import static com.screenlocker.secure.utils.AppConstants.EXTRA_REQUEST_ID_SAVED;
import static com.screenlocker.secure.utils.AppConstants.EXTRA_SPACE;
import static com.screenlocker.secure.utils.AppConstants.FINISHED_PULLED_APPS;
import static com.screenlocker.secure.utils.AppConstants.FINISHED_PUSHED_APPS;
import static com.screenlocker.secure.utils.AppConstants.FINISH_POLICY;
import static com.screenlocker.secure.utils.AppConstants.FINISH_POLICY_APPS;
import static com.screenlocker.secure.utils.AppConstants.FINISH_POLICY_EXTENSIONS;
import static com.screenlocker.secure.utils.AppConstants.FINISH_POLICY_PUSH_APPS;
import static com.screenlocker.secure.utils.AppConstants.FINISH_POLICY_SETTINGS;
import static com.screenlocker.secure.utils.AppConstants.FORCE_UPDATE_CHECK;
import static com.screenlocker.secure.utils.AppConstants.GET_APPLIED_SETTINGS;
import static com.screenlocker.secure.utils.AppConstants.GET_DEVICE_MSG;
import static com.screenlocker.secure.utils.AppConstants.GET_POLICY;
import static com.screenlocker.secure.utils.AppConstants.GET_PULLED_APPS;
import static com.screenlocker.secure.utils.AppConstants.GET_PUSHED_APPS;
import static com.screenlocker.secure.utils.AppConstants.GET_SIM_UPDATES;
import static com.screenlocker.secure.utils.AppConstants.GET_SYNC_STATUS;
import static com.screenlocker.secure.utils.AppConstants.IMEI1;
import static com.screenlocker.secure.utils.AppConstants.IMEI2;
import static com.screenlocker.secure.utils.AppConstants.IMEI_APPLIED;
import static com.screenlocker.secure.utils.AppConstants.IMEI_HISTORY;
import static com.screenlocker.secure.utils.AppConstants.INSTALLED_APPS;
import static com.screenlocker.secure.utils.AppConstants.IS_SYNCED;
import static com.screenlocker.secure.utils.AppConstants.KEY_DATABASE_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.KEY_DEF_BRIGHTNESS;
import static com.screenlocker.secure.utils.AppConstants.KEY_DEVICE_LINKED;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_LOCK_IMAGE;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.LIMITED;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.LOADING_POLICY;
import static com.screenlocker.secure.utils.AppConstants.LOAD_POLICY;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.OLD_DEVICE;
import static com.screenlocker.secure.utils.AppConstants.PENDING_FINISH_DIALOG;
import static com.screenlocker.secure.utils.AppConstants.PERVIOUS_VERSION;
import static com.screenlocker.secure.utils.AppConstants.POLICY_NAME;
import static com.screenlocker.secure.utils.AppConstants.SECURE_SETTINGS_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.SEND_APPS;
import static com.screenlocker.secure.utils.AppConstants.SEND_DEVICE_MSG;
import static com.screenlocker.secure.utils.AppConstants.SEND_EXTENSIONS;
import static com.screenlocker.secure.utils.AppConstants.SEND_PULLED_APPS_STATUS;
import static com.screenlocker.secure.utils.AppConstants.SEND_PUSHED_APPS_STATUS;
import static com.screenlocker.secure.utils.AppConstants.SEND_SETTINGS;
import static com.screenlocker.secure.utils.AppConstants.SEND_SIM;
import static com.screenlocker.secure.utils.AppConstants.SEND_SIM_ACK;
import static com.screenlocker.secure.utils.AppConstants.SETTINGS_APPLIED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.SETTINGS_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.SIM_0_ICCID;
import static com.screenlocker.secure.utils.AppConstants.SIM_1_ICCID;
import static com.screenlocker.secure.utils.AppConstants.SIM_ACTION_DELETED;
import static com.screenlocker.secure.utils.AppConstants.SIM_ACTION_NEW_DEVICE;
import static com.screenlocker.secure.utils.AppConstants.SIM_ACTION_UNREGISTER;
import static com.screenlocker.secure.utils.AppConstants.SIM_ACTION_UPDATE;
import static com.screenlocker.secure.utils.AppConstants.SIM_GET_INSERTD_SIMS;
import static com.screenlocker.secure.utils.AppConstants.SIM_UNREGISTER_FLAG;
import static com.screenlocker.secure.utils.AppConstants.SOCKET_STATUS;
import static com.screenlocker.secure.utils.AppConstants.START_SOCKET;
import static com.screenlocker.secure.utils.AppConstants.STOP_SOCKET;
import static com.screenlocker.secure.utils.AppConstants.SUSPENDED_PACKAGES;
import static com.screenlocker.secure.utils.AppConstants.SYSTEM_EVENT_BUS;
import static com.screenlocker.secure.utils.AppConstants.TOKEN;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
import static com.screenlocker.secure.utils.AppConstants.UNINSTALLED_APPS;
import static com.screenlocker.secure.utils.AppConstants.UNSYNC_ICCIDS;
import static com.screenlocker.secure.utils.AppConstants.WRITE_IMEI;
import static com.screenlocker.secure.utils.CommonUtils.setAlarmManager;
import static com.screenlocker.secure.utils.PrefUtils.PREF_FILE;
import static com.screenlocker.secure.utils.Utils.scheduleExpiryCheck;
import static com.screenlocker.secure.views.PrepareLockScreen.setDeviceId;
import static com.secureSetting.UtilityFunctions.setScreenBrightness;

/**
 * this service is the startForeground service to kepp the lock screen going when user lock the phone
 * (must enable service by enabling service from settings screens{@link SettingsActivity#onClick(View)})
 */


public class LockScreenService extends Service implements OnSocketConnectionListener, SocketEvents {


    private SharedPreferences sharedPref;
    private KeyguardManager myKM;
    private RelativeLayout mLayout = null;
    private ScreenOffReceiver screenOffReceiver;
    private List<NotificationItem> notificationItems;
    private WindowManager windowManager;
    private WindowManager.LayoutParams localLayoutParams;
    private FrameLayout mView;
    private final IBinder binder = new LocalBinder();
    private boolean isLocked = false;
    private WindowManager.LayoutParams params;
    private Fetch fetch;
    private int downloadId = 0;
    private boolean viewAdded = false;
    private View view;
    private ComponentName compName;
    private DevicePolicyManager mDPM;


    private NetworkSocketAlarm networkSocketAlarm;

    private NetworkChangeReceiver networkChangeReceiver;

    private void registerNetworkPref() {
        sharedPref = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(networkChange);
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void unRegisterNetworkPref() {
        if (sharedPref != null)
            sharedPref.unregisterOnSharedPreferenceChangeListener(networkChange);
        if (networkChangeReceiver != null)
            unregisterReceiver(networkChangeReceiver);
    }

    SharedPreferences.OnSharedPreferenceChangeListener networkChange = (sharedPreferences, key) -> {

        if (key.equals(CURRENT_NETWORK_STATUS)) {
            String networkStatus = sharedPreferences.getString(CURRENT_NETWORK_STATUS, LIMITED);
            boolean isConnected = networkStatus.equals(CONNECTED);
            if (!isConnected) {
                destroyClientChatSocket();
            } else {
                connectClientChatSocket();
            }
        }
    };


    private SocketManager socketManager;
    /* Downloader used for SM app to download applications in background*/
    private FetchListener fetchListener = new FetchListener() {
        @Override
        public void onAdded(@NotNull Download download) {
        }

        @Override
        public void onQueued(@NotNull Download download, boolean b) {

        }

        @Override
        public void onWaitingNetwork(@NotNull Download download) {

        }

        @Override
        public void onCompleted(@NotNull Download download) {
            Extras extras = download.getExtras();
            //getPackage Name Of download
            String packageName = extras.getString(EXTRA_PACKAGE_NAME, "null");
            //get file path of download
            String path = extras.getString(EXTRA_FILE_PATH, "null");
            String space = extras.getString(EXTRA_SPACE, "null");
            if (PrefUtils.getStringPref(LockScreenService.this, DOWNLAOD_HASH_MAP) != null) {
                Type typetoken = new TypeToken<HashMap<String, DownloadStatusCls>>() {
                }.getType();
                String hashmap = PrefUtils.getStringPref(LockScreenService.this, DOWNLAOD_HASH_MAP);
                Map<String, DownloadStatusCls> map1 = new Gson().fromJson(hashmap, typetoken);
                DownloadStatusCls status = map1.get(packageName);
                if (status != null) {
                    status.setStatus(SMActivity.INSTALLING);
                }
                map1.put(packageName, status);
                PrefUtils.saveStringPref(LockScreenService.this, DOWNLAOD_HASH_MAP, new Gson().toJson(map1));
            }
            try {
                switch (extras.getString(EXTRA_REQUEST, EXTRA_INSTALL_APP)) {
                    case EXTRA_INSTALL_APP:
                        if (installAppListener != null) {

                            installAppListener.downloadComplete(path, packageName, space);
                        } else {
                            Uri uri = Uri.fromFile(new File(path));
                            Utils.installSielentInstall(LockScreenService.this, Objects.requireNonNull(getContentResolver().openInputStream(uri)), packageName, space);
                        }
                        break;
                    case EXTRA_MARKET_FRAGMENT:
                        if (marketDoaLoadLister != null)
                            marketDoaLoadLister.downloadComplete(path, packageName, space);
                        else {
                            Uri uri = Uri.fromFile(new File(path));
                            Utils.installSielentInstall(LockScreenService.this, Objects.requireNonNull(getContentResolver().openInputStream(uri)), packageName, space);

                        }
                        break;

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        @Override
        public void onError(@NotNull Download download, @NotNull Error error, @org.jetbrains.annotations.Nullable Throwable throwable) {
            Extras extras = download.getExtras();
            //getPackage Name Of download
            String packageName = extras.getString(EXTRA_PACKAGE_NAME, "null");
            //get file path of download
            String path = extras.getString(EXTRA_FILE_PATH, "null");
            if (PrefUtils.getStringPref(LockScreenService.this, DOWNLAOD_HASH_MAP) != null) {
                Type typetoken = new TypeToken<HashMap<String, DownloadStatusCls>>() {
                }.getType();
                String hashmap = PrefUtils.getStringPref(LockScreenService.this, DOWNLAOD_HASH_MAP);
                Map<String, DownloadStatusCls> map1 = new Gson().fromJson(hashmap, typetoken);
                map1.remove(packageName);
                PrefUtils.saveStringPref(LockScreenService.this, DOWNLAOD_HASH_MAP, new Gson().toJson(map1));
            }

            switch (extras.getString(EXTRA_REQUEST, EXTRA_INSTALL_APP)) {
                case EXTRA_INSTALL_APP:
                    if (installAppListener != null)
                        installAppListener.downloadError(packageName);
                    break;
                case EXTRA_MARKET_FRAGMENT:
                    if (marketDoaLoadLister != null)
                        marketDoaLoadLister.downloadError(packageName);
                    break;

            }
            File file = new File(path);
            file.delete();


        }


        @Override
        public void onDownloadBlockUpdated(@NotNull Download download, @NotNull DownloadBlock downloadBlock, int i) {

        }

        @Override
        public void onStarted(@NotNull Download download, java.util.@NotNull List<? extends DownloadBlock> list, int i) {

            Extras extras = download.getExtras();
            //getPackage Name Of download
            String packageName = extras.getString(EXTRA_PACKAGE_NAME, "null");
            //get file path of download
            String path = extras.getString(EXTRA_FILE_PATH, "null");

            switch (extras.getString(EXTRA_REQUEST, EXTRA_INSTALL_APP)) {
                case EXTRA_INSTALL_APP:
                    if (installAppListener != null)
                        installAppListener.onDownloadStarted(packageName);
                    break;
                case EXTRA_MARKET_FRAGMENT:
                    if (marketDoaLoadLister != null)
                        marketDoaLoadLister.onDownloadStarted(packageName);
                    break;
            }

            if (PrefUtils.getStringPref(LockScreenService.this, DOWNLAOD_HASH_MAP) != null) {
                Type typetoken = new TypeToken<HashMap<String, DownloadStatusCls>>() {
                }.getType();
                String hashmap = PrefUtils.getStringPref(LockScreenService.this, DOWNLAOD_HASH_MAP);
                Map<String, DownloadStatusCls> map1 = new Gson().fromJson(hashmap, typetoken);
                DownloadStatusCls status = map1.get(packageName);
                if (status != null) {
                    status.setStatus(SMActivity.INSTALLING);
                }
                map1.put(packageName, status);
                PrefUtils.saveStringPref(LockScreenService.this, DOWNLAOD_HASH_MAP, new Gson().toJson(map1));
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onProgress(@NotNull Download download, long l, long l1) {

            Extras extras = download.getExtras();
            //getPackage Name Of download
            String packageName = extras.getString(EXTRA_PACKAGE_NAME, "null");
            //get file path of download
            String path = extras.getString(EXTRA_FILE_PATH, "null");
            String space = extras.getString(EXTRA_SPACE, "null");
            String request_id = extras.getString(EXTRA_REQUEST_ID_SAVED, "null");


            switch (extras.getString(EXTRA_REQUEST, EXTRA_INSTALL_APP)) {
                case EXTRA_INSTALL_APP:
                    if (installAppListener != null)
                        installAppListener.onDownLoadProgress(packageName, download.getProgress(), l1, request_id, space);
                    break;
                case EXTRA_MARKET_FRAGMENT:
                    if (marketDoaLoadLister != null)
                        marketDoaLoadLister.onDownLoadProgress(packageName, download.getProgress(), l1, request_id, space);
                    break;
            }
        }

        @Override
        public void onPaused(@NotNull Download download) {
        }

        @Override
        public void onResumed(@NotNull Download download) {

        }

        @Override
        public void onCancelled(@NotNull Download download) {
            Extras extras = download.getExtras();
            //getPackage Name Of download
            String packageName = extras.getString(EXTRA_PACKAGE_NAME, "null");
            //get file path of download
            String path = extras.getString(EXTRA_FILE_PATH, "null");
            if (PrefUtils.getStringPref(LockScreenService.this, DOWNLAOD_HASH_MAP) != null) {
                Type typetoken = new TypeToken<HashMap<String, DownloadStatusCls>>() {
                }.getType();
                String hashmap = PrefUtils.getStringPref(LockScreenService.this, DOWNLAOD_HASH_MAP);
                Map<String, DownloadStatusCls> map1 = new Gson().fromJson(hashmap, typetoken);
                map1.remove(packageName);
                PrefUtils.saveStringPref(LockScreenService.this, DOWNLAOD_HASH_MAP, new Gson().toJson(map1));
            }
            File file = new File(download.getFile());
            file.delete();

            if (!packageName.equals("null")) {
                switch (extras.getString(EXTRA_REQUEST, EXTRA_INSTALL_APP)) {
                    case EXTRA_INSTALL_APP:
                        if (installAppListener != null)
                            installAppListener.onDownloadCancelled(packageName);

                        break;
                    case EXTRA_MARKET_FRAGMENT:
                        if (marketDoaLoadLister != null)
                            marketDoaLoadLister.onDownloadCancelled(packageName);

                        break;
                }
                Toast.makeText(LockScreenService.this, "Download cancelled", Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        public void onRemoved(@NotNull Download download) {

        }

        @Override
        public void onDeleted(@NotNull Download download) {
            File file = new File(download.getFile());
            file.delete();
        }
    };
    private DownloadServiceCallBacks installAppListener, marketDoaLoadLister;


    public void destroyClientChatSocket() {
        if (socketManager != null)
            socketManager.destroyClientChatSocket();
    }

    public void connectClientChatSocket() {
        String deviceId = PrefUtils.getStringPref(this, DEVICE_ID);

        if (deviceId == null) {
            String serialNumber = DeviceIdUtils.getSerialNumber();
            Log.d("serialslkdj", serialNumber);
            deviceId = DeviceIdUtils.getSerialNumber();
        }
        Log.d("lkashdf", deviceId);
        if (socketManager != null)
            socketManager.connectClientChatSocket(deviceId, AppConstants.CLIENT_SOCKET_URL);
    }


    public class LocalBinder extends Binder {
        public LockScreenService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LockScreenService.this;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onCreate() {


        registerNetworkPref();

        // alarm manager for offline expiry
        setAlarmManager(this, System.currentTimeMillis() + 15000, 0);

//        // alarm manager for network / socket connection
//        setAlarmManager(this, System.currentTimeMillis() + 1, 1);

        broadCastIntentForActivatingAdmin();

        boolean old_device_status = PrefUtils.getBooleanPref(this, AppConstants.OLD_DEVICE_STATUS);

        if (!old_device_status) {
            if (PrefUtils.getBooleanPref(this, TOUR_STATUS)) {
                final ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                final NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected()) {
                    String macAddress = DeviceIdUtils.generateUniqueDeviceId(this);
                    String serialNo = DeviceIdUtils.getSerialNumber();
                    new ApiUtils(this, macAddress, serialNo).connectToSocket();
                    PrefUtils.saveBooleanPref(this, AppConstants.OLD_DEVICE_STATUS, true);
                }

            }
        }


        sharedPref = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
        myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        PackageManager packageManager = getPackageManager();

        socketManager = SocketManager.getInstance();


        FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(this)
                .setDownloadConcurrentLimit(1)
                .setProgressReportingInterval(500)
                .build();
        fetch = Fetch.Impl.getInstance(fetchConfiguration);
        fetch.addListener(fetchListener);


        Timber.d("status : %s", packageManager.checkSignatures("com.secure.launcher", "com.secure.systemcontrol"));


        OneTimeWorkRequest insertionWork =
                new OneTimeWorkRequest.Builder(BlurWorker.class)
                        .build();
        WorkManager.getInstance().enqueue(insertionWork);


        if (!PrefUtils.getBooleanPref(this, DEVICE_LINKED_STATUS)) {
            scheduleExpiryCheck(this);
        }

        if (!getResources().getString(R.string.apktype).equals("BYOD")) {
            scheduleUpdateJob(this);
        }

        mLayout = new RelativeLayout(LockScreenService.this);
        notificationItems = new ArrayList<>();
        params = PrepareLockScreen.getParams(LockScreenService.this, mLayout);
        appExecutor = AppExecutor.getInstance();
        //smalliew
        localLayoutParams = new WindowManager.LayoutParams();
        createLayoutParamsForSmallView();
        mView = new FrameLayout(this);
        ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mView.setLayoutParams(params);
        powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        screenOffReceiver = new ScreenOffReceiver(() -> startLockScreen(true));

        //default brightness only once
        if (!PrefUtils.getBooleanPref(this, KEY_DEF_BRIGHTNESS)) {
            //40% brightness by default
            setScreenBrightness(this, 102);
            PrefUtils.saveBooleanPref(this, KEY_DEF_BRIGHTNESS, true);
        }

        //local
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver, new IntentFilter(AppConstants.BROADCAST_ACTION));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                viewAddRemoveReceiver, new IntentFilter(AppConstants.BROADCAST_VIEW_ADD_REMOVE));
        registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        PrefUtils.saveToPref(this, true);
        Notification notification = Utils.getNotification(this, R.drawable.ic_lock_black_24dp, getString(R.string.service_notification_text));
        // Whitelist two apps.


// ...


        mDPM = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);
        if (mDPM.isDeviceOwnerApp(getPackageName())) {
            try {
                if (!mDPM.isUninstallBlocked(compName, getPackageName()))
                    mDPM.setUninstallBlocked(compName, getPackageName(), true);
                //mDPM.setStatusBarDisabled(compName, true);
                Bundle bundle = mDPM.getUserRestrictions(compName);
                if (!bundle.getBoolean(DISALLOW_INSTALL_UNKNOWN_SOURCES))
                    mDPM.addUserRestriction(compName, DISALLOW_INSTALL_UNKNOWN_SOURCES);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        verifySettings(this);
        startForeground(R.string.app_name, notification);

    }


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (PrefUtils.getBooleanPref(LockScreenService.this, TOUR_STATUS)) {
                sheduleScreenOffMonitor();
            }
        }
    };
    BroadcastReceiver viewAddRemoveReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("add")) {
                //addView(android.R.color.transparent);
            } else {
                //removeView();
            }
        }
    };

    PowerManager powerManager;

    AppExecutor appExecutor;

    private void sheduleScreenOffMonitor() {
        if (appExecutor.getExecutorForSedulingRecentAppKill().isShutdown()) {
            appExecutor.readyNewExecutor();
        }
        appExecutor.getExecutorForSedulingRecentAppKill().execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (!powerManager.isInteractive()) {
                    appExecutor.getMainThread().execute(() -> startLockScreen(true));
                    return;
                } else {
                    if (myKM.inKeyguardRestrictedInputMode()) {
                        //it is locked
                        appExecutor.getMainThread().execute(() -> startLockScreen(true));
                        return;
                    }
                }
            }
        });
    }

    public void startDownload(String url, String filePath, String packageName, String type, String space) {
        Request request = new Request(url, filePath);
        request.setPriority(Priority.HIGH);
        request.setNetworkType(NetworkType.ALL);
        request.addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG");
        Map<String, String> map = new HashMap<>();
        map.put(EXTRA_PACKAGE_NAME, packageName);
        map.put(EXTRA_FILE_PATH, filePath);
        map.put(EXTRA_REQUEST, type);
        map.put(EXTRA_SPACE, space);
        map.put(EXTRA_SPACE, space);
        map.put(EXTRA_REQUEST_ID_SAVED, String.valueOf(request.getId()));

        Extras extras = new Extras(map);
        request.setExtras(extras);


        fetch.enqueue(request, updatedRequest -> {

            //Request was successfully enqueued for download.
            int id = updatedRequest.getId();
            DownloadStatusCls status = new DownloadStatusCls(id, SMActivity.PENDING);
            if (PrefUtils.getStringPref(this, DOWNLAOD_HASH_MAP) != null) {
                Type typetoken = new TypeToken<HashMap<String, DownloadStatusCls>>() {
                }.getType();
                String hashmap = PrefUtils.getStringPref(this, DOWNLAOD_HASH_MAP);
                Map<String, DownloadStatusCls> map1 = new Gson().fromJson(hashmap, typetoken);
                map1.put(packageName, status);
                PrefUtils.saveStringPref(LockScreenService.this, DOWNLAOD_HASH_MAP, new Gson().toJson(map1));
            } else {
                Map<String, DownloadStatusCls> map1 = new HashMap<>();
                map1.put(packageName, status);
                PrefUtils.saveStringPref(LockScreenService.this, DOWNLAOD_HASH_MAP, new Gson().toJson(map1));

            }


        }, error -> {
            Toast.makeText(getAppContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            //An error occurred enqueuing the request.
        });
    }


    public void cancelDownload(String request_id) {
        if (request_id != null && !request_id.equals("null")) {
            fetch.cancel(Integer.parseInt(request_id));
        } else {
            Log.d("lkadnf", "service");

        }
    }


    public void setInstallAppDownloadListener(DownloadServiceCallBacks downloadListener) {

        this.installAppListener = downloadListener;

    }

    public void setMarketDownloadListener(DownloadServiceCallBacks downloadListener) {
        this.marketDoaLoadLister = downloadListener;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("screen locker starting.");


        if (intent != null) {
            String action = intent.getAction();

            String socketStatus = intent.getStringExtra(SOCKET_STATUS);

            if (socketStatus != null) {
                if (socketStatus.equals(START_SOCKET)) {
                    startSocket();
                } else if (socketStatus.equals(STOP_SOCKET)) {
                    stopSocket();
                }
            }


            Timber.d("locker screen action :%s", action);
            if (action == null) {
                String main_password = PrefUtils.getStringPref(this, KEY_MAIN_PASSWORD);
                if (main_password == null) {
                    PrefUtils.saveStringPref(this, KEY_MAIN_PASSWORD, DEFAULT_MAIN_PASS);
                }
                if (socketStatus == null)
                    startLockScreen(false);

            } else {
                switch (action) {
                    case "suspended":
                    case "expired":
                    case "unlinked":
                    case "locked":
                    case DUPLICATE_MAC:
                    case DUPLICATE_SERIAL:
                    case DUPLICATE_MAC_AND_SERIAL:
                    case "flagged":
                    case "transfered":
                        startLockScreen(true);
                        break;
                    case "reboot":
                        startLockScreen(false);
                        break;
                    case "unlocked":
                        removeLockScreenView();
                        suspendPackages();
                        simPermissionsCheck();
                        break;
                    case "lockedFromsim":
                        startLockScreen(false);
                    case "add":
                        addView(this);
                        break;
                    case "remove":
                        removeView();
                        break;
                }
            }
        }

//        disableScreenShots();


        Timber.i("Received start id " + startId + ": " + intent);
        return START_STICKY;
    }


    private void startLockScreen(boolean refresh) {

        if (PrefUtils.getBooleanPref(this, TOUR_STATUS)) {
            try {
                if (refresh)
                    refreshKeyboard();
                notificationItems.clear();

                if (mLayout.getWindowToken() == null) {
                    removeView();
                    if (params == null) {
                        params = PrepareLockScreen.getParams(this, mLayout);
                    }
                    windowManager.addView(mLayout, params);

                    try {
                        //clear home with our app to front
                        Instrumentation m_Instrumentation = new Instrumentation();
                        AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
                            m_Instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_HOME);

                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    PrefUtils.saveStringPref(this, AppConstants.CURRENT_KEY, AppConstants.KEY_SUPPORT_PASSWORD);
                }


            } catch (Exception e) {
                Timber.e(e);
            }
        }


    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void removeLockScreenView() {
        try {
            if (mLayout != null && mLayout.getWindowToken() != null) {
                windowManager.removeView(mLayout);
            }
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    private void simPermissionsCheck() {
        String iccid0 = PrefUtils.getStringPref(this, SIM_0_ICCID);
        String iccid1 = PrefUtils.getStringPref(this, SIM_1_ICCID);
        String space = PrefUtils.getStringPref(this, CURRENT_KEY);
        AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
            List<SimEntry> simEntries = MyApplication.getAppDatabase(this).getDao().getAllSimInService();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                assert simEntries != null;
                Optional<SimEntry> op = simEntries.stream()
                        .filter(a -> a.getIccid().equals(iccid0))
                        .findAny();
                if (op.isPresent()) {
                    SimEntry entry1 = op.get();
                    spaceWiseEnableDisable(space, entry1, 0);
                } else {
                    byDefaultBehaviour(space, 0);
                }
                Optional<SimEntry> op1 = simEntries.stream()
                        .filter(a -> a.getIccid().equals(iccid1))
                        .findAny();
                if (op1.isPresent()) {
                    SimEntry entry1 = op1.get();
                    spaceWiseEnableDisable(space, entry1, 1);
                } else {
                    byDefaultBehaviour(space, 1);
                }
            }
        });

    }

    private void byDefaultBehaviour(String space, int slot) {
        switch (space) {
            case KEY_GUEST_PASSWORD:
                if (PrefUtils.getBooleanPrefWithDefTrue(this, ALLOW_GUEST_ALL)) {
                    broadCastIntent(true, slot);
                } else {
                    broadCastIntent(false, slot);
                }
                break;
            case KEY_MAIN_PASSWORD:
                if (PrefUtils.getBooleanPrefWithDefTrue(this, ALLOW_ENCRYPTED_ALL)) {
                    broadCastIntent(true, slot);
                } else {
                    broadCastIntent(false, slot);
                }
                break;
        }
    }

    private void spaceWiseEnableDisable(String space, SimEntry entry1, int slot) {
        switch (space) {
            case KEY_GUEST_PASSWORD:

                if (entry1.isGuest()) {
                    //enable sim slot 1 for this user
                    broadCastIntent(true, slot);
                } else {
                    broadCastIntent(false, slot);
                    //disable sim slote for this user

                }
                break;
            case KEY_MAIN_PASSWORD:

                if (entry1.isEncrypted()) {
                    //enable sim slot 1 for this user
                    broadCastIntent(true, slot);
                } else {
                    //disable sim slote for this user
                    broadCastIntent(false, slot);

                }

                break;

        }
    }


    public void refreshKeyboard() {
        try {
            if (mLayout != null) {
                PatternLockView pl = mLayout.findViewById(R.id.patternLock);
                pl.setNumberInputAllow(true);
                pl.setUpRandomizedArray();
                pl.invalidate();
                NCodeView codeView = mLayout.findViewById(R.id.codeView);
                codeView.clearCode();
                TextView clear = mLayout.findViewById(R.id.t9_key_clear);
                TextView support = mLayout.findViewById(R.id.supporttext);
                TextView warningText = mLayout.findViewById(R.id.txtWarning);
                clear.setText(getResources().getString(R.string.btn_backspace));
                support.setText(getResources().getString(R.string.support));
                EditText pin = mLayout.findViewById(R.id.password_field);
                pin.setText(null);
                pin.setHint(getResources().getString(R.string.enter_pin_or_draw_pattern_to_unlock));
                setDeviceId(this, warningText, PrefUtils.getStringPref(this, DEVICE_ID), null, utils.getDeviceStatus(this));
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) mLayout.getLayoutParams();
                windowManager.updateViewLayout(mLayout, params);
            }
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    void broadCastIntent(boolean enabled, int slot) {
        Intent intent = new Intent("com.secure.systemcontrol.SYSTEM_SETTINGS");
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.putExtra("isEnabled", enabled);
        intent.putExtra("slot", slot);
        intent.setComponent(new ComponentName("com.secure.systemcontrol", "com.secure.systemcontrol.receivers.SettingsReceiver"));
        sendBroadcast(intent);
    }

    SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> {
        if (key.equals(KEY_LOCK_IMAGE)) {
            mLayout = null;
            mLayout = new RelativeLayout(LockScreenService.this);
            params = null;
            params = PrepareLockScreen.getParams(LockScreenService.this, mLayout);
            //windowManager.removeViewImmediate(mLayout);
        } else if (key.equals(DEVICE_ID)) {
            destroyClientChatSocket();
            connectClientChatSocket();
        } else if (key.equals(SUSPENDED_PACKAGES)) {
            suspendPackages();
        }
    };

    protected void addView(Context context) {
        Timber.d("addView: ");
        try {
            if (!isLocked && mView.getWindowToken() == null && !viewAdded) {

                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.action_restricted_layout, null);

                mView.addView(view);


                windowManager.addView(mView, localLayoutParams);
                viewAdded = true;
            }
        } catch (Exception e) {
            Timber.e(e);
            viewAdded = false;
        }

        Intent i = new Intent(context, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);


    }

    protected void removeView() {
        Timber.d("removeView: ");
        try {
            if (mView != null && mView.getWindowToken() != null) {
                if (windowManager != null) {
//                    mView.removeView(imageView);
//                    mView.removeView(textView);
                    mView.removeView(view);
                    windowManager.removeViewImmediate(mView);
                    viewAdded = false;
                }
            }
        } catch (Exception e) {
            Timber.e(e);
            viewAdded = false;
        }


    }

    private void createLayoutParamsForSmallView() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

        } else {

            localLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        localLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
// this is to enable the notification to recieve touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
// Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
//        localLayoutParams.y = WindowManager.LayoutParams.MATCH_PARENT;

        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        localLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        localLayoutParams.format = PixelFormat.TRANSLUCENT;


    }

    void broadCastIntentForActivatingAdmin() {
        Intent intent = new Intent("com.secure.systemcontrol.AADMIN");
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setComponent(new ComponentName("com.secure.systemcontrol", "com.secure.systemcontrol.receivers.SettingsReceiver"));
        sendBroadcast(intent);
    }

    /**
     * This function is called after every screen unlock
     * This function suspends the packages that are not allowed in a perticular space
     */
    private void suspendPackages() {
        if (!mDPM.isDeviceOwnerApp(getPackageName())) return;
        AppExecutor.getInstance().getSingleThreadExecutor().submit(() -> {
            List<AppInfo> appInfos = MyApplication.getAppDatabase(this).getDao().getAppsWithoutIcons();
            if (PrefUtils.getStringPref(this, CURRENT_KEY).equals(KEY_GUEST_PASSWORD)) {

                for (AppInfo appInfo : appInfos) {
                    if (!appInfo.isGuest() || !appInfo.isEnable()) {
                        if (!appInfo.getPackageName().equals(getPackageName()) && !appInfo.getPackageName().equals("com.android.settings"))
                            mDPM.setPackagesSuspended(compName, new String[]{appInfo.getPackageName()}, true);

                    } else {
                        mDPM.setPackagesSuspended(compName, new String[]{appInfo.getPackageName()}, false);
                    }
                }
            } else if (PrefUtils.getStringPref(this, CURRENT_KEY).equals(AppConstants.KEY_MAIN_PASSWORD)) {
                for (AppInfo appInfo : appInfos) {
                    if (!appInfo.isEncrypted() || !appInfo.isEnable()) {
                        if (!appInfo.getPackageName().equals(getPackageName()) && !appInfo.getPackageName().equals("com.android.settings"))
                            mDPM.setPackagesSuspended(compName, new String[]{appInfo.getPackageName()}, true);


                    } else {
                        mDPM.setPackagesSuspended(compName, new String[]{appInfo.getPackageName()}, false);
                    }
                }
            }
//
        });
    }

    private String device_id;

    private void startSocket() {

        Timber.d("startSocket");

        socketManager = SocketManager.getInstance();
        socketManager.setSocketConnectionListener(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(appsBroadcast, new IntentFilter(BROADCAST_APPS_ACTION));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PUSH_APPS);
        intentFilter.addAction(ACTION_PULL_APPS);
        LocalBroadcastManager.getInstance(this).registerReceiver(pushPullBroadcast, intentFilter);

        String token = PrefUtils.getStringPref(LockScreenService.this, TOKEN);
        device_id = PrefUtils.getStringPref(LockScreenService.this, DEVICE_ID);

        if (token != null && device_id != null) {
            // connecting to socket
            String live_url = PrefUtils.getStringPref(LockScreenService.this, LIVE_URL);
            socketManager.destroy();
            socketManager.connectSocket(token, device_id, live_url);
            Timber.d("connecting to socket....");
        }

    }

    private void stopSocket() {

        LocalBroadcastManager.getInstance(this).unregisterReceiver(appsBroadcast);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushPullBroadcast);

        Log.d("LockScreenService", "service destroy");

        socketManager.destroy();

        socketManager.removeSocketConnectionListener(this);
        socketManager.removeAllSocketConnectionListener();
    }

    public interface PolicyResponse {
        void onResponse(boolean status);
    }

    private LockScreenService.PolicyResponse policyResponse;

    public void setListener(LockScreenService.PolicyResponse policyResponse) {
        this.policyResponse = policyResponse;
    }

    BroadcastReceiver pushPullBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null)
                if (intent.getAction() != null && intent.getAction().equals(ACTION_PUSH_APPS)) {
                    boolean finishStatus = intent.getBooleanExtra("finish_status", false);
                    String packageName = intent.getStringExtra("PackageName");
                    boolean status = intent.getBooleanExtra("Status", false);
                    boolean isPolicy = intent.getBooleanExtra("isPolicy", false);
                    String setting_id = intent.getStringExtra("setting_id");
                    Map<String, Boolean> map = new HashMap<>();
                    map.put(packageName, status);
                    if (isPolicy && finishStatus)
                        finishPolicyPushApps(setting_id);
                    if (!isPolicy && finishStatus) finishPushedApps(setting_id);
                    if (!isPolicy)
                        sendPushedAppsStatus(map);
                } else if (intent.getAction() != null && intent.getAction().equals(ACTION_PULL_APPS)) {
                    boolean finishStatus = intent.getBooleanExtra("finish_status", false);
                    String packageName = intent.getStringExtra("PackageName");
                    boolean status = intent.getBooleanExtra("Status", false);
                    Map<String, Boolean> map = new HashMap<>();
                    map.put(packageName, status);
                    sendPulledAPpsStatus(map);
                    if (finishStatus)
                        finishPulledApps(intent.getStringExtra("setting_id"));
                }
        }
    };
    private BroadcastReceiver appsBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(BROADCAST_APPS_ACTION)) {
                String action = intent.getStringExtra(KEY_DATABASE_CHANGE);
                if (action != null && PrefUtils.getBooleanPref(context, IS_SYNCED)) {
                    if (action.equals("apps"))
                        sendAppsWithoutIcons();
                    if (action.equals("extensions")) sendExtensionsWithoutIcons();
                    if (action.equals("settings"))
                        sendSettings();
                    if (action.equals("simSettings")) sendSimSettings(null);
                    try {
                        if (socketManager.getSocket() != null && socketManager.getSocket().connected())
                            socketManager.getSocket().emit(SETTINGS_APPLIED_STATUS + device_id, new JSONObject().put("device_id", device_id));
                    } catch (Exception e) {
                        Timber.d(e);
                    }
                }
            }
        }
    };

    @Override
    public void onSocketEventFailed() {
        Timber.d("Socket event failed");
        new ApiUtils(LockScreenService.this, DeviceIdUtils.generateUniqueDeviceId(this), DeviceIdUtils.getSerialNumber());
    }

    @Override
    public void onSocketConnectionStateChange(int socketState) {
        if (socketState == 1)
            Timber.d("Socket is connecting");
        else if (socketState == 2) {
            Timber.d("Socket is connected");
            String installedApps = PrefUtils.getStringPref(this, INSTALLED_APPS);
            String uninstalledApps = PrefUtils.getStringPref(this, UNINSTALLED_APPS);
            if (installedApps != null)
                saveAppsList(this, true, null, true);
            if (uninstalledApps != null) saveAppsList(this, false, null, true);
            getSyncStatus();
            getPolicy();
            getDeviceStatus();
            getAppliedSettings();
            getPushedApps();
            getPulledApps();
            writeImei();
            imeiHistory();
            forceUpdateCheck();
            getSimUpdates();
            sendSystemEvents();
            getSystemEvents();
            getDeviceMessages();
            if (PrefUtils.getStringPref(this, APPS_HASH_MAP) != null) {
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
                socketManager.getSocket().off(GET_SIM_UPDATES + device_id);
                socketManager.getSocket().off(SYSTEM_EVENT_BUS + device_id);
                socketManager.getSocket().off(GET_DEVICE_MSG + device_id);
            }
        }
    }

    @Override
    public void onInternetConnectionStateChange(int socketState) {
        switch (socketState) {
            case 1:
                Timber.d("Socket is connecting");
                break;
            case 2:/*                Timber.d("Socket is connected");*/
                break;
            case 3:
                Timber.d("Socket is disconnected");
                break;
        }
    }

    @Override
    public void getSyncStatus() {
        try {
            if (socketManager.getSocket().connected())
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
                            syncDevice(LockScreenService.this, is_synced, apps, extensions, settings);
                            if (!PrefUtils.getBooleanPref(LockScreenService.this, AppConstants.IS_SYNCED))
                                if (!PrefUtils.getBooleanPref(LockScreenService.this, AppConstants.APPS_SENT_STATUS))
                                    sendApps();
                                else if (!PrefUtils.getBooleanPref(LockScreenService.this, AppConstants.EXTENSIONS_SENT_STATUS))
                                    sendExtensions();
                                else if (!PrefUtils.getBooleanPref(LockScreenService.this, AppConstants.SETTINGS_SENT_STATUS))
                                    sendSettings();
                        } else Timber.e(" invalid request ");
                    } catch (Exception error) {
                        Timber.e(error);
                    }
                });
            else
                Timber.d("Socket not connected");
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void getAppliedSettings() {
        if (socketManager.getSocket().connected())
            socketManager.getSocket().on(GET_APPLIED_SETTINGS + device_id, args -> {
                Timber.d("<<< GETTING APPLIED SETTINGS >>>");
                JSONObject obj = (JSONObject) args[0];
                String setting_id = null;
                try {
                    setting_id = obj.getString("setting_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                            sendAppliedStatus(setting_id);
                            setScreenLock();
                            Timber.d(" settings applied status sent ");
                        } else {
                            Timber.d(" no settings available in history ");
                            boolean appsSettingStatus = PrefUtils.getBooleanPref(LockScreenService.this, APPS_SETTING_CHANGE);
                            Timber.d(" apps settings status in local : %S", appsSettingStatus);
                            if (appsSettingStatus)
                                sendAppsWithoutIcons();
                            boolean settingsStatus = PrefUtils.getBooleanPref(LockScreenService.this, SETTINGS_CHANGE);
                            Timber.d(" settings status in local : %S", settingsStatus);
                            if (settingsStatus)
                                sendSettings();
                            boolean extensionsStatus = PrefUtils.getBooleanPref(LockScreenService.this, SECURE_SETTINGS_CHANGE);
                            Timber.d(" extensions status in local : %S", extensionsStatus);
                            if (extensionsStatus)
                                sendExtensionsWithoutIcons();
                        }
                    } else Timber.e(" invalid request ");
                } catch (Exception error) {
                    Timber.e(error, " error");
                }
            });
    }

    private void setScreenLock() {
        Intent intent = new Intent(LockScreenService.this, LockScreenService.class).setAction("locked");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(intent);
        else startService(intent);
    }

    private void updateSettings(JSONObject obj, boolean isPolicy) throws JSONException {
        String settings = obj.getString("settings");
        String id = null;
        try {
            if (isPolicy) id = obj.getString("setting_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            if (!settings.equals("[]")) {
                changeSettings(LockScreenService.this, settings);
                Timber.d(" settings applied ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isPolicy)
            finishPolicySettings(id);
    }

    private void updatePassword(JSONObject obj) throws JSONException {
        String passwords = obj.getString("passwords");
        if (!passwords.equals("{}")) {
            updatePasswords(LockScreenService.this, new JSONObject(passwords), device_id);
            Timber.d(" passwords updated ");
            setScreenLock();
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
                        List<AppInfo> apps = MyApplication.getAppDatabase(LockScreenService.this).getDao().getApps();
                        socketManager.getSocket().emit(SEND_APPS + device_id, new Gson().toJson(apps));
                        Timber.d(" apps sent %s", apps.size());
                    } else
                        Timber.d("Socket not connected");
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
        Timber.d("service destroy");
        socketManager.destroy();
        socketManager.removeSocketConnectionListener(this);
        socketManager.removeAllSocketConnectionListener();
        super.onDestroy();
    }

    @Override
    public void sendExtensions() {
        Timber.d("<<< Sending Extensions >>>");
        new Thread(() -> {
            try {
                if (socketManager.getSocket().connected()) {
                    List<SubExtension> extensions = MyApplication.getAppDatabase(LockScreenService.this).getDao().getAllSubExtensions();
                    socketManager.getSocket().emit(SEND_EXTENSIONS + device_id, new Gson().toJson(extensions));
                    Timber.d("extensions sent%s", extensions.size());
                } else
                    Timber.d("Socket not connected");
            } catch (Exception e) {
                Timber.d(e);
            }
        }).start();
    }

    @Override
    public void getDeviceStatus() {
        try {
            if (socketManager.getSocket().connected())
                socketManager.getSocket().on(DEVICE_STATUS + device_id, args -> {
                    Timber.d("<<< GETTING DEVICE STATUS >>>");
                    JSONObject object = (JSONObject) args[0];
                    try {
                        if (validateRequest(device_id, object.getString("device_id"))) {
                            Timber.d("<<< valid request >>>");
                            String msg = object.getString("msg");
                            Timber.e("<<< device status =>>> %S", msg);
                            if (msg != null) {
                                Timber.d("status from Socket :%s", msg);
                                switch (msg) {
                                    case "suspended":
                                        suspendedDevice(LockScreenService.this, "suspended");
                                        Timber.d("<<< device suspended >>>");
                                        break;
                                    case "active":
                                        unSuspendDevice(LockScreenService.this);
                                        Timber.d("<<< device activated >>>");
                                        break;
                                    case "expired":
                                        suspendedDevice(LockScreenService.this, "expired");
                                        Timber.d("<<< device expired >>>");
                                        break;
                                    case "unlinked":
                                        Timber.d("<<< device unlinked >>>");
                                        unlinkDeviceWithMsg(LockScreenService.this, true, "unlinked");
                                        break;
                                    case "wiped":
                                        Timber.d("<<< device wiped >>>");
                                        JSONObject json = new JSONObject().put("action", ACTION_WIPE);
                                        socketManager.getSocket().emit(SYSTEM_EVENT_BUS + device_id, json);
                                        wipeDevice(LockScreenService.this);
                                        break;
                                    case "flagged":
                                        suspendedDevice(LockScreenService.this, "flagged");
                                        break;
                                    case "transfered":
                                        suspendedDevice(LockScreenService.this, "transfered");
                                        break;
                                }
                            }
                        } else
                            Timber.d("<<< invalid request >>>");
                    } catch (Exception error) {
                        Timber.e(error);
                    }
                });
            else
                Timber.d("Socket connected");
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @Override
    public void sendSettings() {
        Timber.d("<<< Sending settings >>>");
        try {
            if (socketManager.getSocket().connected())
                AppExecutor.getInstance().getSingleThreadExecutor().submit(() -> {
                    List<Settings> settings = MyApplication.getAppDatabase(LockScreenService.this).getDao().getSettings();
                    socketManager.getSocket().emit(SEND_SETTINGS + device_id, new Gson().toJson(settings));
                    PrefUtils.saveBooleanPref(LockScreenService.this, SETTINGS_CHANGE, false);
                });
            else
                Timber.d("Socket not connected");
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @Override
    public void sendSimSettings(ArrayList<SimEntry> simEntries) {
        Timber.d("<<< Sending  Sim Settings >>>");
        try {
            if (socketManager.getSocket().connected()) {
                Set<String> set = PrefUtils.getStringSet(this, DELETED_ICCIDS);
                if (simEntries != null) {
                    for (SimEntry simEntry : simEntries) {
                        set.remove(simEntry.getIccid());
                    }
                }
                if (set != null && set.size() > 0)
                    AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
                        JSONObject json = new JSONObject();
                        try {
                            json.put("action", SIM_ACTION_DELETED).put("entries", new Gson().toJson(set));
                            socketManager.getSocket().emit(SEND_SIM + device_id, json);
                            PrefUtils.saveStringSetPref(this, DELETED_ICCIDS, null);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    });

                if (!PrefUtils.getBooleanPref(this, OLD_DEVICE)) {
                    AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
                        List<SimEntry> entries = MyApplication.getAppDatabase(this).getDao().getAllSimInService();
                        JSONObject json = new JSONObject();
                        try {
                            json.put("action", SIM_ACTION_NEW_DEVICE);
                            json.put("entries", new Gson().toJson(entries));
                            socketManager.getSocket().emit(SEND_SIM + device_id, json);
                            PrefUtils.saveBooleanPref(this, OLD_DEVICE, true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    });


                }

                Set<String> set1 = PrefUtils.getStringSet(this, UNSYNC_ICCIDS);
                if (set1 != null && set1.size() > 0) {
                    AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
                        List<SimEntry> entries = MyApplication.getAppDatabase(this).getDao().getSims(set1);
                        JSONObject json = new JSONObject();
                        try {
                            json.put("action", SIM_ACTION_UPDATE);
                            json.put("entries", new Gson().toJson(entries));
                            socketManager.getSocket().emit(SEND_SIM + device_id, json);
                            PrefUtils.saveStringSetPref(this, UNSYNC_ICCIDS, null);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });

                }
                if (PrefUtils.getBooleanPref(this, SIM_UNREGISTER_FLAG)) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("action", SIM_ACTION_UNREGISTER);
                        json.put("entries", new JSONObject().put("unrGuest", PrefUtils.getBooleanPref(this, ALLOW_GUEST_ALL)).put("unrEncrypt", PrefUtils.getBooleanPref(this, ALLOW_ENCRYPTED_ALL)).toString());
                        socketManager.getSocket().emit(SEND_SIM + device_id, json);
                        PrefUtils.saveBooleanPref(this, SIM_UNREGISTER_FLAG, false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


            } else {
                Timber.d("Socket not connected");
            }
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    @Override
    public void sendAppliedStatus(String setting_id) {
        try {
            if (socketManager.getSocket().connected()) {
                socketManager.getSocket().emit(SETTINGS_APPLIED_STATUS + device_id, new JSONObject().put("device_id", device_id).put("setting_id", setting_id));
            } else {
                Timber.d("Socket not connected");
            }
        } catch (Exception e) {
            Timber.d(e);
        }
    }


    @Override
    public void sendAppsWithoutIcons() {
        new Thread(() -> {
            try {

                if (socketManager.getSocket().connected()) {

                    socketManager.getSocket().emit(SEND_APPS + device_id, new Gson().toJson(MyApplication.getAppDatabase(LockScreenService.this).getDao().getAppsWithoutIcons()));
                    PrefUtils.saveBooleanPref(LockScreenService.this, APPS_SETTING_CHANGE, false);

                    Timber.d("Apps sent");
                } else {
                    Timber.d("Socket not connected");
                }

            } catch (Exception e) {
                Timber.e(e);
            }
        }).start();
    }

    @Override
    public void sendExtensionsWithoutIcons() {
        new Thread(() -> {
            try {

                if (socketManager.getSocket().connected()) {
                    socketManager.getSocket().emit(SEND_EXTENSIONS + device_id, new Gson().toJson(MyApplication.getAppDatabase(LockScreenService.this).getDao().getExtensionsWithoutIcons()));
                    PrefUtils.saveBooleanPref(LockScreenService.this, SECURE_SETTINGS_CHANGE, false);

                    Timber.d("Extensions sent");
                } else {
                    Timber.d("Socket not connected");
                }

            } catch (Exception e) {
                Timber.e(e);
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
            String setting_id = null;
            try {
                setting_id = object.getString("setting_id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                        item.setToken(PrefUtils.getStringPref(this, PrefUtils.getStringPref(LockScreenService.this, TOKEN)));
                        list.set(i, item);
                    }

                    String apps = new Gson().toJson(list);

                    final Intent intent = new Intent();
                    intent.setAction(s);
                    intent.putExtra("json", apps);
                    intent.putExtra("isPolicy", isPolicy);
                    intent.putExtra("setting_id", setting_id);
                    Timber.d("isPolicy %s", isPolicy);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    intent.setComponent(new ComponentName("com.secure.systemcontrol", s2));
                    sendBroadcast(intent);

                } else {
                    if (isPolicy) {
                        finishPolicyPushApps(setting_id);
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
    public void finishPushedApps(String setting_id) {
        Timber.d("<<<Finish pushed apps>>>");
        if (socketManager.getSocket() != null) {
            if (socketManager.getSocket().connected()) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("status", true);
                    jsonObject.put("setting_id", setting_id);
                    socketManager.getSocket().emit(FINISHED_PUSHED_APPS + device_id, jsonObject);
                    setScreenLock();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void finishPulledApps(String setting_id) {
        Timber.d("<<<Finish pulled apps>>>");
        if (socketManager.getSocket() != null) {
            if (socketManager.getSocket().connected()) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("status", true);
                    jsonObject.put("setting_id", setting_id);
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


                                Intent intent = new Intent(LockScreenService.this, TransparentActivity.class);
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

                String link_code = PrefUtils.getStringPref(LockScreenService.this, KEY_DEVICE_LINKED);

                Timber.d("%s", link_code);
                object.put("device_id", device_id);
                object.put("link_code", link_code);
                object.put("policy_name", policyName);

                if (policyName.equals("default_policy")) {
                    object.put("is_default", true);
                } else {
                    object.put("is_default", false);
                }
                PrefUtils.saveStringPref(LockScreenService.this, POLICY_NAME, policyName);
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
                Timber.d(args[0].toString());

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
    public void finishPolicyPushApps(String setting_id) {
        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
            Timber.d("<<< FINISH POLICY PUSH APPS>>>");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("device_id", device_id);
                jsonObject.put("status", true);
                jsonObject.put("setting_id", setting_id);
                socketManager.getSocket().emit(FINISH_POLICY_PUSH_APPS + device_id, jsonObject);
                finishPolicy(setting_id);
            } catch (JSONException e) {
                Timber.d(e);
            }
        }
    }

    @Override
    public void finishPolicyApps(String hId) {
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
    public void finishPolicySettings(String hId) {
        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
            Timber.d("<<< FINISH POLICY SETTINGS >>>");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("device_id", device_id);
                jsonObject.put("status", true);
                jsonObject.put("setting_id", hId);
                socketManager.getSocket().emit(FINISH_POLICY_SETTINGS + device_id, jsonObject);
            } catch (JSONException e) {
                Timber.d(e);
            }
        }
    }

    @Override
    public void finishPolicyExtensions(String hId) {
        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
            Timber.d("<<< FINISH POLICY EXTENSIONS >>>");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("device_id", device_id);
                jsonObject.put("status", true);
                jsonObject.put("setting_id", hId);

                socketManager.getSocket().emit(FINISH_POLICY_EXTENSIONS + device_id, jsonObject);
            } catch (JSONException e) {
                Timber.d(e);
            }
        }
    }

    @Override
    public void finishPolicy(String setting_id) {
        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
            Timber.d("<<< FINISH POLICY >>>");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("device_id", device_id);
                jsonObject.put("status", true);
                jsonObject.put("setting_id", setting_id);
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

    @Override
    public void getSimUpdates() {
        try {

            if (socketManager.getSocket().connected()) {

                socketManager.getSocket().on(GET_SIM_UPDATES + device_id, args -> {
                    Timber.d("<<< GETTING SIM UPDATES >>>");
                    JSONObject obj = (JSONObject) args[0];

                    try {
                        if (validateRequest(device_id, obj.getString("device_id"))) {
                            Timber.e(" valid request ");
                            Timber.d(obj.toString());
                            String action = obj.getString("action");
                            GsonBuilder builder = new GsonBuilder();
                            builder.registerTypeAdapter(boolean.class, new BooleanTypeAdapter());
                            Gson gson = builder.create();
                            switch (action) {
                                case SIM_ACTION_DELETED:
                                    Set<String> set = new Gson().fromJson(obj.getString("entries"), new TypeToken<Set<String>>() {
                                    }.getType());
                                    AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
                                        MyApplication.getAppDatabase(this).getDao().deleteSims(set);
                                        try {
                                            socketManager.getSocket().emit(SEND_SIM_ACK + device_id, new JSONObject().put("device_id", device_id));
                                        } catch (JSONException e) {
                                            Timber.e(e);
                                        }
                                    });
                                    break;
                                case SIM_ACTION_UPDATE:

                                    ArrayList<SimEntry> simEntries = gson.fromJson(obj.getString("entries"), new TypeToken<ArrayList<SimEntry>>() {
                                    }.getType());


                                    try {
                                        UnRegisterModel sims = gson.fromJson(obj.getString("unregSettings"), UnRegisterModel.class);
                                        PrefUtils.saveBooleanPref(this, ALLOW_GUEST_ALL, sims.isUnrGuest());
                                        PrefUtils.saveBooleanPref(this, ALLOW_ENCRYPTED_ALL, sims.isUnrEncrypt());
                                        socketManager.getSocket().emit(SEND_SIM_ACK + device_id, new JSONObject().put("device_id", device_id));
                                    } catch (JSONException e) {
                                        Timber.e(e);
                                    }
                                    AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
                                        for (SimEntry simEntry : simEntries) {
                                            simEntry.setStatus(getResources().getString(R.string.status_not_inserted));
                                            int no = MyApplication.getAppDatabase(this).getDao().updateSim(simEntry);
                                            if (no < 1) {
                                                MyApplication.getAppDatabase(this).getDao().insertSim(simEntry);
                                            }
                                        }
                                        try {
                                            socketManager.getSocket().emit(SEND_SIM_ACK + device_id, new JSONObject().put("device_id", device_id));
                                        } catch (JSONException e) {
                                            Timber.e(e);
                                        }
                                    });
                                    sendSimSettings(simEntries);
                                    break;
                                case SIM_ACTION_UNREGISTER:

                                    UnRegisterModel sim = gson.fromJson(obj.getString("entries"), UnRegisterModel.class);

                                    PrefUtils.saveBooleanPref(this, ALLOW_GUEST_ALL, sim.isUnrGuest());
                                    PrefUtils.saveBooleanPref(this, ALLOW_ENCRYPTED_ALL, sim.isUnrEncrypt());
                                    try {
                                        socketManager.getSocket().emit(SEND_SIM_ACK + device_id, new JSONObject().put("device_id", device_id));
                                    } catch (JSONException e) {
                                        Timber.e(e);
                                    }
                                    break;
                                case SIM_GET_INSERTD_SIMS:
                                    Timber.d("SIM_GET_INSERTD_SIMS: ");
                                    sendInsertedUnregisteredSims();
                                    break;
                            }

                        } else {
                            Timber.e(" invalid request ");
                        }
                    } catch (Exception error) {
                        Timber.e(error);
                    }
                });
            } else {
                Timber.d("Socket not connected");
            }

        } catch (Exception e) {
            Timber.d(e);
        }
    }

    private void sendInsertedUnregisteredSims() {
        try {
            SubscriptionManager manager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
            SubscriptionInfo infoSim1 = manager.getActiveSubscriptionInfoForSimSlotIndex(0);
            SubscriptionInfo infoSim2 = manager.getActiveSubscriptionInfoForSimSlotIndex(1);

            AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
                JSONObject json = new JSONObject();
                try {
                    json.put("action", SIM_GET_INSERTD_SIMS);

                    SimEntry first = null, second = null;
                    List<SimEntry> entries = new ArrayList<>();
                    if (infoSim1 != null) {
                        first = MyApplication.getAppDatabase(this).getDao().getSimById(infoSim1.getIccId());
                    }
                    if (infoSim2 != null) {
                        second = MyApplication.getAppDatabase(this).getDao().getSimById(infoSim2.getIccId());
                    }
                    if (first == null && infoSim1 != null) {
                        first = new SimEntry(infoSim1.getIccId(), infoSim1.getDisplayName().toString(), "", false, false);
                        entries.add(first);
                    }
                    if (second == null && infoSim2 != null) {
                        second = new SimEntry(infoSim2.getIccId(), infoSim2.getDisplayName().toString(), "", false, false);
                        entries.add(second);
                    }
                    json.put("entries", new Gson().toJson(entries));
                    socketManager.getSocket().emit(SEND_SIM + device_id, json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception ignored) {

        }
    }

    @Override
    public void sendSystemEvents() {
        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
            Timber.d("<<< FINISH SEND SYS INFO >>>");

            JSONObject jsonObject = new JSONObject();
            try {
                if (PrefUtils.getIntegerPref(this, AppConstants.PERVIOUS_VERSION) < getPackageManager().getPackageInfo(getPackageName(), 0).versionCode) {
                    JSONObject object = new JSONObject();
                    object.put("type", getResources().getString(R.string.apktype));
                    object.put("firmware_info", Build.DISPLAY);
                    object.put("version", getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                    jsonObject.put("action", ACTION_DEVICE_TYPE_VERSION);
                    jsonObject.put("object", object);
                    Timber.d(object.toString());
                    socketManager.getSocket().emit(SYSTEM_EVENT_BUS + device_id, jsonObject);
                }

            } catch (JSONException e) {
                Timber.d(e);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void getSystemEvents() {
        try {

            if (socketManager.getSocket().connected()) {

                socketManager.getSocket().on(SYSTEM_EVENT_BUS + device_id, args -> {
                    Timber.d("<<< GETTING SYSTEM_EVENTS UPDATES >>>");
                    JSONObject obj = (JSONObject) args[0];


                    try {
                        if (validateRequest(device_id, obj.getString("device_id"))) {
                            String action = obj.getString("action");
                            if (ACTION_DEVICE_TYPE_VERSION.equals(action)) {
                                Timber.d("Saved");
                                PrefUtils.saveIntegerPref(this, PERVIOUS_VERSION, getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
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
            Timber.e(e);
        }
    }

    @Override
    public void getDeviceMessages() {
        try {

            if (socketManager.getSocket().connected()) {

                socketManager.getSocket().on(GET_DEVICE_MSG + device_id, args -> {
                    Timber.d("<<< GETTING MSGs >>>");
                    JSONObject obj = (JSONObject) args[0];


                    try {
                        if (validateRequest(device_id, obj.getString("device_id"))) {

                            DeviceMessagesModel model = new Gson().fromJson(obj.toString(), DeviceMessagesModel.class);
                            model.setDate(new Date().getTime());
                            MyApplication.getAppDatabase(this).getDao().insertDeviceMessage(model);
                            Notification notification = Utils.getDeviceNotification(this, R.drawable.ic_lock_black_24dp, model.getMsg());
                            NotificationManager manager = (NotificationManager) LockScreenService.this.getSystemService(NOTIFICATION_SERVICE);
                            manager.notify(0, notification);
                            JSONObject object = new JSONObject();
                            object.put("job_id", model.getJob_id());
                            socketManager.getSocket().emit(SEND_DEVICE_MSG + device_id, object);

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
            Timber.e(e);
        }
    }

    private void updateExtensions(JSONObject object, boolean isPolicy) throws JSONException {
        String extensionList = object.getString("extension_list");
        String id = null;
        try {
            if (isPolicy) id = object.getString("setting_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!extensionList.equals("[]")) {

            JSONArray jsonArray = new JSONArray(extensionList);

            updateExtensionsList(LockScreenService.this, jsonArray, () -> {
                Timber.d(" extensions updated ");
            }, isPolicy);
        }

        if (isPolicy) {
            finishPolicyExtensions(id);
        }

    }

    private void updateApps(JSONObject object, boolean isPolicy) throws JSONException {

        String appsList = object.getString("app_list");
        String id = null;
        try {
            if (isPolicy) id = object.getString("setting_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!appsList.equals("[]")) {
            updateAppsList(LockScreenService.this, new JSONArray(appsList), () -> {
                Timber.d(" apps updated ");
            }, isPolicy);
        }

        if (isPolicy) {
            finishPolicyApps(id);
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
