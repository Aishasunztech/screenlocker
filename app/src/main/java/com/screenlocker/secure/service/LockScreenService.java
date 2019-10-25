package com.screenlocker.secure.service;

import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.app.Notification;
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
import com.google.gson.reflect.TypeToken;
import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.network.NetworkChangeReceiver;
import com.screenlocker.secure.notifications.NotificationItem;
import com.screenlocker.secure.room.SimEntry;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.settings.managepassword.NCodeView;
import com.screenlocker.secure.socket.SocketManager;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.updateDB.BlurWorker;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Utils;
import com.screenlocker.secure.views.PrepareLockScreen;
import com.screenlocker.secure.views.patternlock.PatternLockView;
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import timber.log.Timber;

import static android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES;
import static com.screenlocker.secure.app.MyApplication.getAppContext;
import static com.screenlocker.secure.socket.utils.utils.getDeviceStatus;
import static com.screenlocker.secure.socket.utils.utils.scheduleUpdateJob;
import static com.screenlocker.secure.socket.utils.utils.verifySettings;
import static com.screenlocker.secure.utils.AppConstants.ALLOW_ENCRYPTED_ALL;
import static com.screenlocker.secure.utils.AppConstants.ALLOW_GUEST_ALL;
import static com.screenlocker.secure.utils.AppConstants.CONNECTED;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_NETWORK_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEFAULT_MAIN_PASS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DOWNLAOD_HASH_MAP;
import static com.screenlocker.secure.utils.AppConstants.DUPLICATE_MAC;
import static com.screenlocker.secure.utils.AppConstants.DUPLICATE_MAC_AND_SERIAL;
import static com.screenlocker.secure.utils.AppConstants.DUPLICATE_SERIAL;
import static com.screenlocker.secure.utils.AppConstants.EXTRA_FILE_PATH;
import static com.screenlocker.secure.utils.AppConstants.EXTRA_INSTALL_APP;
import static com.screenlocker.secure.utils.AppConstants.EXTRA_MARKET_FRAGMENT;
import static com.screenlocker.secure.utils.AppConstants.EXTRA_PACKAGE_NAME;
import static com.screenlocker.secure.utils.AppConstants.EXTRA_REQUEST;
import static com.screenlocker.secure.utils.AppConstants.EXTRA_SPACE;
import static com.screenlocker.secure.utils.AppConstants.KEY_DEF_BRIGHTNESS;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_LOCK_IMAGE;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.LIMITED;
import static com.screenlocker.secure.utils.AppConstants.SIM_0_ICCID;
import static com.screenlocker.secure.utils.AppConstants.SIM_1_ICCID;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
import static com.screenlocker.secure.utils.CommonUtils.setAlarmManager;
import static com.screenlocker.secure.utils.PrefUtils.PREF_FILE;
import static com.screenlocker.secure.utils.Utils.scheduleExpiryCheck;
import static com.screenlocker.secure.views.PrepareLockScreen.setDeviceId;
import static com.secureSetting.UtilityFunctions.setScreenBrightness;

/**
 * this service is the startForeground service to kepp the lock screen going when user lock the phone
 * (must enable service by enabling service from settings screens{@link SettingsActivity#onClick(View)})
 */


public class LockScreenService extends Service {


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
            String space = extras.getString(EXTRA_SPACE,"null");
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

                            installAppListener.downloadComplete(path, packageName,space);
                        } else {
                            Uri uri = Uri.fromFile(new File(path));
                            Utils.installSielentInstall(LockScreenService.this, Objects.requireNonNull(getContentResolver().openInputStream(uri)), packageName,space);
                        }
                        break;
                    case EXTRA_MARKET_FRAGMENT:
                        if (marketDoaLoadLister != null)
                            marketDoaLoadLister.downloadComplete(path, packageName,space);
                        else {
                            Uri uri = Uri.fromFile(new File(path));
                            Utils.installSielentInstall(LockScreenService.this, Objects.requireNonNull(getContentResolver().openInputStream(uri)), packageName,space);

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
            String space = extras.getString(EXTRA_SPACE,"null");
            switch (extras.getString(EXTRA_REQUEST, EXTRA_INSTALL_APP)) {
                case EXTRA_INSTALL_APP:
                    if (installAppListener != null)
                        installAppListener.onDownLoadProgress(packageName, download.getProgress(), l1,space);
                    break;
                case EXTRA_MARKET_FRAGMENT:
                    if (marketDoaLoadLister != null)
                        marketDoaLoadLister.onDownLoadProgress(packageName, download.getProgress(), l1,space);
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

            Toast.makeText(LockScreenService.this, "Download cancelled", Toast.LENGTH_SHORT).show();
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
                    new ApiUtils(this, macAddress, serialNo);
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


        DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        ComponentName compName = new ComponentName(this, MyAdmin.class);
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

    public void startDownload(String url, String filePath, String packageName, String type,String space)  {
        Request request = new Request(url, filePath);
        request.setPriority(Priority.HIGH);
        request.setNetworkType(NetworkType.ALL);
        request.addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG");
        Map<String, String> map = new HashMap<>();
        map.put(EXTRA_PACKAGE_NAME, packageName);
        map.put(EXTRA_FILE_PATH, filePath);
        map.put(EXTRA_REQUEST, type);
        map.put(EXTRA_SPACE,space);
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


    public void cancelDownload() {
        if (downloadId != 0)
            fetch.cancel(downloadId);
    }


    public void setInstallAppDownloadListener(DownloadServiceCallBacks downloadListener) {

        this.installAppListener = downloadListener;

    }

    public void setMarketDownloadListener(DownloadServiceCallBacks downloadListener) {
        this.marketDoaLoadLister = downloadListener;
    }

    @Override
    public void onDestroy() {

        try {
            Timber.d("screen locker distorting.");
            unregisterReceiver(screenOffReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(viewAddRemoveReceiver);
            PrefUtils.saveToPref(this, false);
            Intent intent = new Intent(LockScreenService.this, LockScreenService.class);
            unRegisterNetworkPref();
            PrefUtils.saveStringPref(this, AppConstants.CURRENT_NETWORK_STATUS, AppConstants.LIMITED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
            sharedPref.unregisterOnSharedPreferenceChangeListener(listener);
        } catch (Exception e) {
            Timber.d(e);
        }


        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("screen locker starting.");


        if (intent != null) {
            String action = intent.getAction();

            Timber.d("locker screen action :%s", action);
            if (action == null) {
                String main_password = PrefUtils.getStringPref(this, KEY_MAIN_PASSWORD);
                if (main_password == null) {
                    PrefUtils.saveStringPref(this, KEY_MAIN_PASSWORD, DEFAULT_MAIN_PASS);
                }
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
                        startLockScreen(true);
                        break;
                    case "transfered":
                        startLockScreen(true);
                        break;
                    case "reboot":
                        startLockScreen(false);
                        break;
                    case "unlocked":
                        removeLockScreenView();
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
                    } catch (Exception ignored) {
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
                setDeviceId(this, warningText, PrefUtils.getStringPref(this, DEVICE_ID), null, getDeviceStatus(this));
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

}
