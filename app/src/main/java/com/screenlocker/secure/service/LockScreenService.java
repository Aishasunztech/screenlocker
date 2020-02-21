package com.screenlocker.secure.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.manual_load.DownloadCompleteListener;
import com.screenlocker.secure.manual_load.DownloadPushedApps;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.network.NetworkChangeReceiver;
import com.screenlocker.secure.permissions.SteppersActivity;
import com.screenlocker.secure.room.MyAppDatabase;
import com.screenlocker.secure.room.SimEntry;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.service.apps.ServiceConnectedListener;
import com.screenlocker.secure.service.apps.WindowChangeDetectingService;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.settings.managepassword.NCodeView;
import com.screenlocker.secure.socket.SocketManager;
import com.screenlocker.secure.socket.TransparentActivity;
import com.screenlocker.secure.socket.interfaces.OnSocketConnectionListener;
import com.screenlocker.secure.socket.interfaces.SocketEvents;
import com.screenlocker.secure.socket.model.DeviceMessagesModel;
import com.screenlocker.secure.socket.model.ImeiModel;
import com.screenlocker.secure.socket.model.InstallModel;
import com.screenlocker.secure.socket.model.Settings;
import com.screenlocker.secure.socket.receiver.DeviceStatusReceiver;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.updateDB.BlurWorker;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.SecuredSharedPref;
import com.screenlocker.secure.utils.Utils;
import com.screenlocker.secure.views.HiddenPassTransformationMethod;
import com.screenlocker.secure.views.PrepareLockScreen;
import com.screenlocker.secure.views.patternlock.PatternLockView;
import com.screenlocker.secure.views.patternlock.listener.PatternLockViewListener;
import com.screenlocker.secure.views.patternlock.utils.PatternLockUtils;
import com.secure.launcher.BuildConfig;
import com.secure.launcher.R;
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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import timber.log.Timber;

import static android.text.Html.FROM_HTML_MODE_LEGACY;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.screenlocker.secure.app.MyApplication.getAppContext;
import static com.screenlocker.secure.mdm.utils.DeviceIdUtils.isValidImei;
import static com.screenlocker.secure.socket.SocketManager.STATE_CONNECTED;
import static com.screenlocker.secure.socket.SocketManager.STATE_CONNECTING;
import static com.screenlocker.secure.socket.SocketManager.STATE_DISCONNECTED;
import static com.screenlocker.secure.socket.utils.utils.changeSettings;
import static com.screenlocker.secure.socket.utils.utils.chatLogin;
import static com.screenlocker.secure.socket.utils.utils.checkIMei;
import static com.screenlocker.secure.socket.utils.utils.getUserType;
import static com.screenlocker.secure.socket.utils.utils.loginAsEncrypted;
import static com.screenlocker.secure.socket.utils.utils.loginAsGuest;
import static com.screenlocker.secure.socket.utils.utils.registerDeviceStatusReceiver;
import static com.screenlocker.secure.socket.utils.utils.saveAppsList;
import static com.screenlocker.secure.socket.utils.utils.suspendedDevice;
import static com.screenlocker.secure.socket.utils.utils.syncDevice;
import static com.screenlocker.secure.socket.utils.utils.unSuspendDevice;
import static com.screenlocker.secure.socket.utils.utils.unlinkDeviceWithMsg;
import static com.screenlocker.secure.socket.utils.utils.updateAppsList;
import static com.screenlocker.secure.socket.utils.utils.updateExtensionsList;
import static com.screenlocker.secure.socket.utils.utils.updatePasswords;
import static com.screenlocker.secure.socket.utils.utils.validateRequest;
import static com.screenlocker.secure.socket.utils.utils.wipeDevice;
import static com.screenlocker.secure.utils.AppConstants.*;
import static com.screenlocker.secure.utils.CommonUtils.getTimeRemaining;
import static com.screenlocker.secure.utils.CommonUtils.getTimeString;
import static com.screenlocker.secure.utils.CommonUtils.setAlarmManager;
import static com.screenlocker.secure.utils.CommonUtils.setTimeRemaining;
import static com.screenlocker.secure.utils.PrefUtils.PREF_FILE;
import static com.screenlocker.secure.utils.Utils.isAccessServiceEnabled;
import static com.screenlocker.secure.utils.Utils.scheduleExpiryCheck;
import static com.screenlocker.secure.utils.Utils.scheduleUpdateCheck;
import static com.secureSetting.UtilityFunctions.setScreenBrightness;

/**
 * this service is the startForeground service to kepp the lock screen going when user lock the phone
 * (must enable service by enabling service from settings screens{@link SettingsActivity#onClick(View)})
 */


public class LockScreenService extends Service implements ServiceConnectedListener, OnSocketConnectionListener, SocketEvents {
    private SharedPreferences sharedPref;
    private PrefUtils prefUtils;
    private RelativeLayout mLayout = null;
    private ScreenOffReceiver screenOffReceiver;
    private WindowManager windowManager;
    private FrameLayout frameLayout;
    private WindowManager.LayoutParams localLayoutParams;
    private FrameLayout mView;
    private final IBinder binder = new LocalBinder();
    private boolean isLocked = false;
    private WindowManager.LayoutParams params;
    private Fetch fetch;
    private boolean viewAdded = false;
    private View view;
    private SocketManager socketManager;
    private AccessibilityManager manager;
    private UsageStatsManager usm;
    private SecuredSharedPref securedSharedPref;
    private boolean isClockTicking = false;
    public String incomingComboRequest = null;


    private HashSet<String> tempAllowed = new HashSet<>();
    private HashSet<String> blacklist = new HashSet<>();
    HashSet<String> tepperPermissions = new HashSet<>();

    @Override
    public void onCreate() {
        securedSharedPref = SecuredSharedPref.getInstance(this);
        registerNetworkPref();

        setAlarmManager(this, System.currentTimeMillis() + 15000, 0);

        socketManager = SocketManager.getInstance();
        blacklist.add("com.android.systemui");
        blacklist.add("com.vivo.upslide");
        blacklist.add("com.sec.android.app.launcher");
        blacklist.add("com.huawei.android.launcher");

        tempAllowed.add("com.android.settings");
        tempAllowed.add("com.android.phone");
        tempAllowed.add("com.android.providers.telephony");
        tempAllowed.add("com.google.android.packageinstaller");
        tempAllowed.add("com.android.packageinstaller");
        tempAllowed.add("com.android.bluetooth");
        tempAllowed.add("com.samsung.networkui");
        tempAllowed.add("com.samsung.crane");
        tempAllowed.add("com.huawei.systemmanager");
        tempAllowed.add("com.samsung.android.app.telephonyui");
        tempAllowed.add("com.hisi.mapcon");
        tempAllowed.add("com.android.wifisettings");
        tempAllowed.add("com.miui.securitycenter");
        tempAllowed.add("com.samsung.android.incallui");
        tempAllowed.add("com.android.vpndialogs");
        tepperPermissions.add("com.google.android.packageinstaller/.permission.ui.GrantPermissionsActivity");
        tepperPermissions.add("com.samsung.accessibility/.SettingsActivity");
        tepperPermissions.add("com.android.settings/.Settings$AccessibilityInstalledServiceActivity");
        tepperPermissions.add("com.android.packageinstaller/.permission.ui.GrantPermissionsActivity");
        tepperPermissions.add("com.google.android.packageinstaller/.permission.ui.ManagePermissionsActivity");
        tepperPermissions.add("com.android.settings/.SubSettings");
        tepperPermissions.add("com.android.settings/.Settings$AccessibilitySettingsActivity");
        tepperPermissions.add(BuildConfig.APPLICATION_ID + "/com.screenlocker.secure.permissions.SteppersActivity");

        //intialize pref utility
        prefUtils = PrefUtils.getInstance(this);

        boolean old_device_status = prefUtils.getBooleanPref(AppConstants.OLD_DEVICE_STATUS);

        if (!old_device_status) {
            if (prefUtils.getBooleanPref(TOUR_STATUS)) {
                final ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                final NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnected()) {
                    String macAddress = DeviceIdUtils.generateUniqueDeviceId(this);
                    String serialNo = DeviceIdUtils.getSerialNumber();
                    new ApiUtils(this, macAddress, serialNo);
                    prefUtils.saveBooleanPref(AppConstants.OLD_DEVICE_STATUS, true);
                }

            }
        }
        //Update app data base
        OneTimeWorkRequest insertionWork =
                new OneTimeWorkRequest.Builder(BlurWorker.class)
                        .build();
        WorkManager.getInstance().enqueue(insertionWork);
        //initialize downloader
        FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(this)
                .setDownloadConcurrentLimit(2)
                .setProgressReportingInterval(100)
                .build();
        fetch = Fetch.Impl.getInstance(fetchConfiguration);
        fetch.addListener(fetchListener);


        if (!prefUtils.getBooleanPref(DEVICE_LINKED_STATUS)) {
            scheduleExpiryCheck(this);
        }

        if (!getResources().getString(R.string.apktype).equals("BYOD")) {
            scheduleUpdateCheck(this);
        }

        mLayout = new RelativeLayout(LockScreenService.this);
        params = getParams( mLayout);
        appExecutor = AppExecutor.getInstance();
        frameLayout = new FrameLayout(this);
        //smalliew
        localLayoutParams = new WindowManager.LayoutParams();
        manager = (AccessibilityManager) this.getSystemService(Context.ACCESSIBILITY_SERVICE);
        usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        createLayoutParamsForSmallView();
        mView = new FrameLayout(this);
        ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mView.setLayoutParams(params);
        powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        screenOffReceiver = new ScreenOffReceiver(() -> startLockScreen(true));

        if (prefUtils.getBooleanPref(AppConstants.KEY_DISABLE_SCREENSHOT)) {
            disableScreenShots();
        } else {
            allowScreenShoots();
        }
        //default brightness only once
        if (!prefUtils.getBooleanPref(KEY_DEF_BRIGHTNESS)) {
            //40% brightness by default
            setScreenBrightness(this, 102);
            prefUtils.saveBooleanPref(KEY_DEF_BRIGHTNESS, true);
        }

        //local
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver, new IntentFilter(AppConstants.BROADCAST_ACTION));
        LocalBroadcastManager.getInstance(this).registerReceiver(
                viewAddRemoveReceiver, new IntentFilter(AppConstants.BROADCAST_VIEW_ADD_REMOVE));
        LocalBroadcastManager.getInstance(this).registerReceiver(appsBroadcast, new IntentFilter(BROADCAST_APPS_ACTION));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_PUSH_APPS);
        intentFilter.addAction(ACTION_PULL_APPS);
        LocalBroadcastManager.getInstance(this).registerReceiver(pushPullBroadcast, intentFilter);
        registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        prefUtils.saveToPref(true);
        Notification notification = Utils.getNotification(this, R.drawable.ic_lock_black_24dp, getString(R.string.service_notification_text));


        networkChangeReceiver = new NetworkChangeReceiver();
//        networkChangeReceiver.setNetworkChangeListener(this);

        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));


        startForeground(R.string.app_name, notification);

    }

    /* Downloader used for SM app to download applications in background*/
    private FetchListener fetchListener = new FetchListener() {
        @Override
        public void onAdded(@NotNull Download download) {
            Timber.tag("TEST!@").d("onAdded: ");
        }

        @Override
        public void onQueued(@NotNull Download download, boolean b) {
            Timber.d("onQueued: ");

        }

        @Override
        public void onWaitingNetwork(@NotNull Download download) {
            Timber.d("onWaitingNetwork: ");
        }

        @Override
        public void onCompleted(@NotNull Download download) {
            Extras extras = download.getExtras();
            //getPackage Name Of download
            String packageName = extras.getString(EXTRA_PACKAGE_NAME, "null");
            //get file path of download
            String path = extras.getString(EXTRA_FILE_PATH, "null");
            String space = extras.getString(EXTRA_SPACE, "null");
            try {
                switch (extras.getString(EXTRA_REQUEST, EXTRA_INSTALL_APP)) {
                    case EXTRA_INSTALL_APP:
                        if (installAppListener != null) {

                            installAppListener.downloadComplete(path, packageName, space);
                        } else {
                            Uri uri = Uri.fromFile(new File(path));
//                            Utils.installSielentInstall(LockScreenService.this, Objects.requireNonNull(getContentResolver().openInputStream(uri)), packageName);
                        }
                        break;
                    case EXTRA_MARKET_FRAGMENT:
                        if (marketDoaLoadLister != null)
                            marketDoaLoadLister.downloadComplete(path, packageName, space);
                        else {
                            Uri uri = Uri.fromFile(new File(path));
//                            Utils.installSielentInstall(LockScreenService.this, Objects.requireNonNull(getContentResolver().openInputStream(uri)), packageName);

                        }
                        break;

                }
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
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onProgress(@NotNull Download download, long l, long l1) {

            Extras extras = download.getExtras();
            //getPackage Name Of download
            String packageName = extras.getString(EXTRA_PACKAGE_NAME, "null");
            Timber.d("onProgress: %s", packageName);
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
            File file = new File(download.getFile());
            file.delete();

            Extras extras = download.getExtras();
            String packageName = extras.getString(EXTRA_PACKAGE_NAME, "null");
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


    private static boolean isServiceConnected = false;

    private NetworkChangeReceiver networkChangeReceiver;

    private void registerNetworkPref() {
        HandlerThread receiverHandlerThread = new HandlerThread("threadName");
        receiverHandlerThread.start();
        Looper looper = receiverHandlerThread.getLooper();
        Handler handler = new Handler(looper);
        sharedPref = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(networkChange);
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION), null, handler);
    }

    private void unRegisterNetworkPref() {
        if (sharedPref != null)
            sharedPref.unregisterOnSharedPreferenceChangeListener(networkChange);
        if (networkChangeReceiver != null)
            unregisterReceiver(networkChangeReceiver);
    }

    @SuppressLint("ResourceType")
    SharedPreferences.OnSharedPreferenceChangeListener networkChange = (sharedPreferences, key) -> {

        if (key.equals(CURRENT_NETWORK_CHANGED)) {
            String networkStatus = sharedPreferences.getString(CURRENT_NETWORK_STATUS, LIMITED);
            boolean isConnected = networkStatus.equals(CONNECTED);
            if (!isConnected) {
                destroyClientChatSocket();
            } else {
                connectClientChatSocket();
            }
        } else if (key.equals(KEY_LOCK_IMAGE)) {
            ConstraintLayout rootView = mLayout.findViewById(R.id.background);
            String bg = prefUtils.getStringPref( AppConstants.KEY_LOCK_IMAGE);
            if (bg == null || bg.equals("")) {
                rootView.setBackgroundResource(R.raw._12316);

            } else {
                try {
                    rootView.setBackgroundResource(Integer.parseInt(bg));
                } catch (RuntimeException e) {
                    rootView.setBackgroundResource(R.raw._12316);
                }
            }
            //windowManager.removeViewImmediate(mLayout);
        } else if (key.equals(DEVICE_ID)) {
            destroyClientChatSocket();
            connectClientChatSocket();
        }
    };

    @Override
    public void serviceConnected(boolean status) {
        isServiceConnected = status;
    }


    public void destroyClientChatSocket() {
        socketManager.destroyClientChatSocket();
    }

    public void connectClientChatSocket() {
        String deviceId = prefUtils.getStringPref(DEVICE_ID);

        if (deviceId == null) {
            String serialNumber = DeviceIdUtils.getSerialNumber();
            deviceId = DeviceIdUtils.getSerialNumber();
        }
        socketManager.connectClientChatSocket(deviceId, CLIENT_SOCKET_URL);
    }


    public class LocalBinder extends Binder {
        public LockScreenService getService() {
            // Return this instance of LocalService so clients can call public methods
            return LockScreenService.this;
        }
    }


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (prefUtils.getBooleanPref(TOUR_STATUS)) {
//                sheduleScreenOffMonitor();
                startRecentAppsKillThread();
            }
        }
    };
    BroadcastReceiver viewAddRemoveReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra("screenCapture")) {
                boolean screenCaptureStatus = intent.getBooleanExtra("screenCapture", false);
                if (screenCaptureStatus) {
                    prefUtils.saveBooleanPref(AppConstants.KEY_DISABLE_SCREENSHOT, true);
                    allowScreenShoots();
                } else {
                    prefUtils.saveBooleanPref(AppConstants.KEY_DISABLE_SCREENSHOT, false);
                    disableScreenShots();
                }
            }

        }

    };

    PowerManager powerManager;

    AppExecutor appExecutor;

    public void startDownload(String url, String filePath, String packageName, String type, String space) {

        Timber.i("URL %s: ", url);

        Request request = new Request(url, filePath);
        request.setPriority(Priority.HIGH);
        request.setNetworkType(NetworkType.ALL);
        request.setTag(packageName);
        request.addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG");
        Map<String, String> map = new HashMap<>();
        map.put(EXTRA_PACKAGE_NAME, packageName);
        map.put(EXTRA_FILE_PATH, filePath);
        map.put(EXTRA_REQUEST, type);
        map.put(EXTRA_SPACE, space);
        map.put(EXTRA_REQUEST_ID_SAVED, String.valueOf(request.getId()));
        Extras extras = new Extras(map);
        request.setExtras(extras);


        fetch.enqueue(request, updatedRequest -> {
            //Request was successfully enqueued for download.
        }, error -> {
            Toast.makeText(getAppContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            //An error occurred enqueuing the request.
        });
    }


    private String device_id;

    private DownloadServiceCallBacks installAppListener, marketDoaLoadLister;

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
            LocalBroadcastManager.getInstance(this).unregisterReceiver(appsBroadcast);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(pushPullBroadcast);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(viewAddRemoveReceiver);
            prefUtils.saveToPref(false);
            unRegisterNetworkPref();
            prefUtils.saveStringPref(AppConstants.CURRENT_NETWORK_STATUS, AppConstants.LIMITED);
            Intent intent = new Intent(LockScreenService.this, LockScreenService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        } catch (Exception e) {
            Timber.d(e);
        }


        super.onDestroy();
    }

    private int count = 0;

    private void startRecentAppsKillThread() {

        if (appExecutor.getExecutorForSedulingRecentAppKill().isShutdown()) {
            appExecutor.readyNewExecutor();
        }

        appExecutor.getExecutorForSedulingRecentAppKill().execute(() -> {

            while (!Thread.currentThread().isInterrupted()) {

                if (!powerManager.isInteractive()) {
                    appExecutor.getMainThread().execute(() -> startLockScreen(true));
                    count = 0;
                    if (!appExecutor.getSingleScheduleThreadExecutor().isShutdown())
                        appExecutor.getSingleScheduleThreadExecutor().shutdownNow();
                    return;
                }

//                Timber.d("current Package %s", package_name);

//                if (PrefUtils.getBooleanPref(this, EMERGENCY_FLAG)) {
//
//                    if (AppConstants.TEMP_SETTINGS_ALLOWED) {
//                        Timber.d("Settings are temporary on");
//                        if (!tempAllowed.contains(package_name)) {
//                            checkAppStatus(package_name);
//                        }
//
//                    } else {
//
//                        if (blacklist.contains(package_name)) {
////                            clearRecentApp(this, false);
//                            return;
//                        }
//                        checkAppStatus(package_name);
//                    }
//                }


                if (WindowChangeDetectingService.serviceConnectedListener == null) {
                    WindowChangeDetectingService.serviceConnectedListener = this;
                }


                if (manager.isEnabled()) {
                    AccessibilityEvent event = AccessibilityEvent.obtain();
                    event.setEventType(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
                    event.setPackageName(getPackageName());
                    event.setClassName(this.getClass().toString());
                    event.setAction(1452);
                    manager.sendAccessibilityEvent(event);
                }

                if (isAccessServiceEnabled(this, WindowChangeDetectingService.class)) {

//                        Timber.d("access service condition %s", isServiceConnected);

                    if (!isServiceConnected) {
                        if (!prefUtils.getBooleanPref(EMERGENCY_FLAG)) {
                            count++;
                            if (count >= 8) {
                                clearRecentApp(this);
                                count = 0;
                            }
                        }

                    }
                } else {
                    ComponentName cn = m108a();

                    Timber.d("Nadeem:%s", cn.flattenToShortString());

                    if (!tepperPermissions.contains(cn.flattenToShortString())) {
                        launchPermissions();
                    } else {
                        Timber.d("permission denied");
                    }
                }


            }
        });


    }


    /*private void checkAppStatus(String packageName) {


//        Timber.d("package in checkApps status %s ", packageName);

        if (packageName.equals(BuildConfig.APPLICATION_ID)) {
            return;
        }

        Future<Boolean> futureObject = AppExecutor.getInstance().getSingleThreadExecutor()
                .submit(() -> isAllowed(LockScreenService.this, packageName));
        try {
            boolean status = futureObject.get();
            if (!status) {
                clearRecentApp(this, false);
            }
        } catch (Exception e) {
            clearRecentApp(this, false);
        }
    }*/

    private Handler handler;

    private void launchPermissions() {
        Intent a = new Intent(this, SteppersActivity.class);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        a.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        prefUtils.saveBooleanPref(PERMISSION_GRANTING, true);
        if (prefUtils.getBooleanPref(TOUR_STATUS)) {
            a.putExtra("emergency", true);
        }
        startActivity(a);
    }

    private void clearRecentApp(Context context) {

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        handler = new Handler(Looper.getMainLooper());

        ActivityCompat.startForegroundService(this, new Intent(this, LockScreenService.class).setAction("addreboot"));
        handler.postDelayed(() -> ActivityCompat.startForegroundService(LockScreenService.this, new Intent(LockScreenService.this, LockScreenService.class).setAction("remove")), REBOOT_RESTRICTION_DELAY);

        Intent i = new Intent(context, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);


//        handler.postDelayed(this::removeView, 200);


//        handler.postDelayed(() -> ActivityCompat.startForegroundService(LockScreenService.this, new Intent(LockScreenService.this, LockScreenService.class).setAction("remove")), 2000);


    }


    boolean package_status = false;



    public void disableScreenShots() {

        int windowType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            windowType = WindowManager.LayoutParams.TYPE_TOAST |
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        }


        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                windowType, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_SECURE
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,


                PixelFormat.TRANSLUCENT);

//        | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,

        if (frameLayout != null && frameLayout.getWindowToken() == null) {

            windowManager.addView(frameLayout, params);
        }
    }

    public void allowScreenShoots() {
        if (frameLayout != null && frameLayout.getWindowToken() != null) {
            windowManager.removeViewImmediate(frameLayout);
        }
    }


    private void startLockScreen(boolean refresh) {

        if (prefUtils.getBooleanPref(TOUR_STATUS)) {
            try {
//            setTimeRemaining(getAppContext());
                if (refresh)
                    refreshKeyboard();

                if (!isLocked) {
                    isLocked = true;
                    removeView();
                    windowManager.addView(mLayout, params);
                    //clear home with our app to front
                    Intent i = new Intent(LockScreenService.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(i);
                    prefUtils.saveStringPref(AppConstants.CURRENT_KEY, KEY_SUPPORT_PASSWORD);
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
//        if (!prefUtils.getStringPref( CURRENT_KEY).equals(AppConstants.KEY_SUPPORT_PASSWORD)){
//            //            setTimeRemaining(getAppContext());
//        }

        try {
            if (mLayout != null) {
//                final Animation in = AnimationUtils.loadAnimation(this, R.anim.in_from_rigth);
//
//                in.setDuration(5000);
//
//                mLayout.setVisibility(View.GONE);
//                mLayout.startAnimation(in);
                windowManager.removeView(mLayout);
            }
            isLocked = false;
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    private void simPermissionsCheck() {
        String iccid0 = prefUtils.getStringPref(SIM_0_ICCID);
        String iccid1 = prefUtils.getStringPref(SIM_1_ICCID);
        String space = prefUtils.getStringPref(CURRENT_KEY);
        AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
            List<SimEntry> simEntries = MyAppDatabase.getInstance(this).getDao().getAllSimInService();
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
                if (prefUtils.getBooleanPrefWithDefTrue(ALLOW_GUEST_ALL)) {
                    broadCastIntent(true, slot);
                } else {
                    broadCastIntent(false, slot);
                }
                break;
            case KEY_MAIN_PASSWORD:
                if (prefUtils.getBooleanPrefWithDefTrue(ALLOW_ENCRYPTED_ALL)) {
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
                if (entry1.isEnable()) {
                    if (entry1.isGuest()) {
                        //enable sim slot 1 for this user
                        broadCastIntent(true, slot);
                    } else {
                        broadCastIntent(false, slot);
                        //disable sim slote for this user
                    }
                } else {
                    //disable in any case
                    broadCastIntent(false, slot);
                }
                break;
            case KEY_MAIN_PASSWORD:
                if (entry1.isEnable()) {
                    if (entry1.isEncrypted()) {
                        //enable sim slot 1 for this user
                        broadCastIntent(true, slot);
                    } else {
                        //disable sim slote for this user
                        broadCastIntent(false, slot);

                    }
                } else {
                    //disable in any case
                    broadCastIntent(false, slot);

                }
                break;

        }
    }

    public void cancelDownload(String request_id) {
        if (request_id != null && !request_id.equals("null")) {
            fetch.getDownloadsByTag(request_id, result -> {
                for (Download download : result) {
                    if (download.getTag().equals(request_id)){
                        fetch.cancel(download.getId());
                    }
                }
            });
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


    protected void addView(Context context, boolean reboot) {
        Timber.d("addView: ");
        try {

            if (!isLocked && mView.getWindowToken() == null && !viewAdded) {

                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (reboot) {
                    view = inflater.inflate(R.layout.reboot_layout, null);
                } else {
                    view = inflater.inflate(R.layout.action_restricted_layout, null);
                }

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

        localLayoutParams.gravity = Gravity.TOP | Gravity.END;
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


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("screen locker starting.");

//        if (WindowChangeDetectingService.serviceConnectedListener == null) {
//            WindowChangeDetectingService.serviceConnectedListener = this;
//        }


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
                String main_password = prefUtils.getStringPref(KEY_MAIN_PASSWORD);
                if (main_password == null) {
                    prefUtils.saveStringPref(KEY_MAIN_PASSWORD, DEFAULT_MAIN_PASS);
                }
                if (socketStatus == null)
                    startLockScreen(false);
            } else {
                switch (action) {
                    case "suspended":
                    case "transfered":
                    case "unlinked":
                    case "expired":
                        startLockScreen(true);
                        break;
                    case "reboot":
                        startLockScreen(false);
                        break;
                    case "unlocked":
                        removeLockScreenView();
                        simPermissionsCheck();
                        break;
                    case "locked":
                        startLockScreen(true);
                    case "flagged":
                        startLockScreen(true);
                        break;

                    case "lockedFromsim":
                        startLockScreen(false);
                    case "add":
                        addView(this, false);
                        break;
                    case "remove":
                        removeView();
                        break;
                    case "addreboot":
                        addView(this, true);
                        break;
                }
            }
        }

//        disableScreenShots();


        Timber.i("Received start id " + startId + ": " + intent);
        return START_STICKY;
    }

    public void refreshKeyboard() {
        try {
            if (mLayout != null) {
                PatternLockView pl = mLayout.findViewById(R.id.patternLock);
                pl.setUpRandomizedArray();
                pl.invalidate();
                TextView clear = mLayout.findViewById(R.id.t9_key_clear);
                TextView support = mLayout.findViewById(R.id.supporttext);
                TextView warningText = mLayout.findViewById(R.id.txtWarning);
                clear.setText(getResources().getString(R.string.btn_backspace));
                support.setText(getResources().getString(R.string.support));
                EditText pin = mLayout.findViewById(R.id.password_field);
                pin.setText(null);
                pin.setHint(getResources().getString(R.string.enter_pin_or_draw_pattern_to_unlock));
                setDeviceId( warningText, prefUtils.getStringPref(DEVICE_ID), null, prefUtils.getStringPref( DEVICE_STATUS));
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) mLayout.getLayoutParams();
                windowManager.updateViewLayout(mLayout, params);
            }
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    private void startSocket() {

        Timber.d("startSocket");

        socketManager = SocketManager.getInstance();
        socketManager.setSocketConnectionListener(this);


        String token = prefUtils.getStringPref(TOKEN);
        device_id = prefUtils.getStringPref(DEVICE_ID);

        if (token != null && device_id != null) {
            // connecting to socket
            String live_url = prefUtils.getStringPref(LIVE_URL);
            socketManager.destroy();
            socketManager.connectSocket(token, device_id, live_url);
            Timber.d("connecting to socket....");
        }

    }

    private void stopSocket() {


        Timber.d("service destroy");

        socketManager.destroy();

        socketManager.removeSocketConnectionListener(this);
        socketManager.removeAllSocketConnectionListener();
    }

    private BroadcastReceiver appsBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null)
                if (intent.getAction().equals(BROADCAST_APPS_ACTION)) {

                    String action = intent.getStringExtra(KEY_DATABASE_CHANGE);
                    Timber.d("djgdsgsggjiodig");

                    if (action != null) {
                        if (prefUtils.getBooleanPref(IS_SYNCED)) {

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
    BroadcastReceiver pushPullBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null) {

                if (intent.getAction() != null && intent.getAction().equals(ACTION_PUSH_APPS)) {
                    boolean finishStatus = intent.getBooleanExtra("finish_status", false);
                    String packageName = intent.getStringExtra("PackageName");
                    boolean status = intent.getBooleanExtra("Status", false);
                    boolean isPolicy = intent.getBooleanExtra("isPolicy", false);
                    String setting_id = intent.getStringExtra("setting_id");
                    Map<String, Boolean> map = new HashMap<>();
                    map.put(packageName, status);

                    if (isPolicy && finishStatus) {
                        finishPolicyPushApps(setting_id);
                    }

                    if (!isPolicy && finishStatus) {
                        finishPushedApps(setting_id);
                    }

                    if (!isPolicy) {
                        sendPushedAppsStatus(map);
                    }

                } else if (intent.getAction() != null && intent.getAction().equals(ACTION_PULL_APPS)) {

                    boolean finishStatus = intent.getBooleanExtra("finish_status", false);
                    String packageName = intent.getStringExtra("PackageName");
                    boolean status = intent.getBooleanExtra("Status", false);
                    String setting_id = intent.getStringExtra("setting_id");
                    Map<String, Boolean> map = new HashMap<>();
                    map.put(packageName, status);
                    sendPulledAPpsStatus(map);

                    if (finishStatus) {
                        finishPulledApps(setting_id);
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onSocketConnectionStateChange(int socketState) {


        if (socketState == STATE_CONNECTING) {
            Timber.d("Socket is connecting");

        } else if (socketState == STATE_CONNECTED) {
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
            getSimUpdates();
            sendSystemEvents();
            getSystemEvents();
            getDeviceMessages();
            getDeviceInfoUpdate();

            String installedApps = prefUtils.getStringPref(INSTALLED_APPS);
            String uninstalledApps = prefUtils.getStringPref(UNINSTALLED_APPS);

            if (installedApps != null) {
                saveAppsList(this, true, null, true);
            }
            if (uninstalledApps != null) {
                saveAppsList(this, false, null, true);
            }

            if (prefUtils.getStringPref(APPS_HASH_MAP)
                    != null) {
                Type type = new TypeToken<HashMap<String, Boolean>>() {
                }.getType();
                String hashmap = prefUtils.getStringPref(APPS_HASH_MAP);
                HashMap<String, Boolean> map = new Gson().fromJson(hashmap, type);
                sendPushedAppsStatus(map);
                prefUtils.saveStringPref(APPS_HASH_MAP, null);
            }

        } else if (socketState == STATE_DISCONNECTED) {
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
                socketManager.getSocket().off(GET_DEVICE_MSG + device_id);
                socketManager.getSocket().off(GET_DEVICE_INFO + device_id);
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
                Timber.d("Socket is connected");

                break;
            case 3:
                Timber.d("Socket is disconnected");
                break;
        }
    }


    @Override
    public void getSyncStatus() {


        Timber.d("<<< GET SYnc>>>");

        Timber.d("<<<STATUS >>> %s", SocketManager.getInstance().getSocket().connected());
//        Timber.d("<<<STATUS >>> %s", socketManager.getSocket().connected());

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

                            syncDevice(prefUtils, is_synced, apps, extensions, settings);

                            if (!prefUtils.getBooleanPref(AppConstants.IS_SYNCED)) {

                                if (!prefUtils.getBooleanPref(AppConstants.APPS_SENT_STATUS)) {
                                    sendApps();
                                } else if (!prefUtils.getBooleanPref(AppConstants.EXTENSIONS_SENT_STATUS)) {
                                    sendExtensions();
                                } else if (!prefUtils.getBooleanPref(AppConstants.SETTINGS_SENT_STATUS)) {
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

                            updateExtensions(obj, false);

                            updateApps(obj, false);

                            sendAppliedStatus(setting_id);

                            updateSettings(obj, false);

                            setScreenLock();

                            Timber.d(" settings applied status sent ");

                        } else {
                            Timber.d(" no settings available in history ");

                            boolean appsSettingStatus = prefUtils.getBooleanPref(APPS_SETTING_CHANGE);
                            Timber.d(" apps settings status in local : %S", appsSettingStatus);

                            if (appsSettingStatus) {
                                sendAppsWithoutIcons();
                            }

                            boolean settingsStatus = prefUtils.getBooleanPref(SETTINGS_CHANGE);
                            Timber.d(" settings status in local : %S", settingsStatus);
                            if (settingsStatus) {
                                sendSettings();
                            }

                            boolean extensionsStatus = prefUtils.getBooleanPref(SECURE_SETTINGS_CHANGE);
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
        Intent intent = new Intent(LockScreenService.this, LockScreenService.class);

        intent.setAction("locked");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void updateSettings(JSONObject obj, boolean isPolicy) throws JSONException {
        Timber.d("<<< Update Settings >>>");
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


        if (isPolicy) {
            finishPolicySettings(id);
        }

    }

    private void updatePassword(JSONObject obj) {
        String passwords;
        try {
            passwords = obj.getString("passwords");
            if (!passwords.equals("{}")) {
                updatePasswords(LockScreenService.this, new JSONObject(passwords), device_id);
                Timber.d(" passwords updated ");
                setScreenLock();
            }
        } catch (JSONException e) {
            Timber.e(e, "Error while updating passwords :");
        }

    }

    @Override
    public void sendApps() {
        Timber.d("<<< sending apps >>>");
        new Thread(() -> {
            try {
                if (socketManager.getSocket().connected()) {
                    List<AppInfo> apps = MyAppDatabase.getInstance(LockScreenService.this).getDao().getApps();
                    socketManager.getSocket().emit(SEND_APPS + device_id, new Gson().toJson(apps));
                    Timber.d(" apps sent %s", apps.size());
                } else {
                    Timber.d("Socket not connected");
                }

            } catch (Exception e) {
                Timber.d(e);
            }
        }).start();
    }

    @Override
    public void sendExtensions() {
        Timber.d("<<< Sending Extensions >>>");

        new Thread(() -> {
            try {
                if (socketManager.getSocket().connected()) {

                    List<SubExtension> extensions = MyAppDatabase.getInstance(LockScreenService.this).getDao().getAllSubExtensions();

                    socketManager.getSocket().emit(SEND_EXTENSIONS + device_id, new Gson().toJson(extensions));

                    Timber.d("extensions sent%s", extensions.size());
                } else {
                    Timber.d("Socket not connected");
                }
            } catch (Exception e) {
                Timber.d(e);
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
                AppExecutor.getInstance().getSingleThreadExecutor().submit(() -> {
                    List<Settings> settings = MyAppDatabase.getInstance(LockScreenService.this).getDao().getSettings();
                    socketManager.getSocket().emit(SEND_SETTINGS + device_id, new Gson().toJson(settings));
                    prefUtils.saveBooleanPref(SETTINGS_CHANGE, false);
                });


            } else {
                Timber.d("Socket not connected");
            }
        } catch (Exception e) {
            Timber.d(e);
        }


    }

    @Override
    public void sendSimSettings(ArrayList<SimEntry> simEntries) {

    }

    @Override
    public void sendAppliedStatus(String setting_id) {
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
        new Thread(() -> {
            try {

                if (socketManager.getSocket().connected()) {

                    socketManager.getSocket().emit(SEND_APPS + device_id, new Gson().toJson(MyAppDatabase.getInstance(LockScreenService.this).getDao().getAppsWithoutIcons()));
                    prefUtils.saveBooleanPref(APPS_SETTING_CHANGE, false);

                    Timber.d("Apps sent");
                } else {
                    Timber.d("Socket not connected");
                }

            } catch (Exception e) {
                Timber.e("error: %S", e.getMessage());
            }
        }).start();
    }

    @Override
    public void sendExtensionsWithoutIcons() {
        new Thread(() -> {
            try {

                if (socketManager.getSocket().connected()) {
                    socketManager.getSocket().emit(SEND_EXTENSIONS + device_id, new Gson().toJson(MyAppDatabase.getInstance(LockScreenService.this).getDao().getExtensionsWithoutIcons()));
                    prefUtils.saveBooleanPref(SECURE_SETTINGS_CHANGE, false);

                    Timber.d("Extensions sent");
                } else {
                    Timber.d("Socket not connected");
                }

            } catch (Exception e) {
                Timber.e("error: %S", e.getMessage());
            }
        }).start();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void getPushedApps() {

        try {
            if (socketManager.getSocket().connected()) {
                socketManager.getSocket().on(GET_PUSHED_APPS + device_id, args -> {
                    Timber.d("<<< GETTING PUSHED APPS>>>");
                    JSONObject object = (JSONObject) args[0];
                    pushPullPolicyApps(object, PUSH_APPS, false);
                });
            } else {
                Timber.d("Socket not connected");
            }


        } catch (Exception e) {
            Timber.d(e);
        }
    }


    private DownloadPushedApps task;


    public static DownloadCompleteListener downloadCompleteListener;

    private void pushPullPolicyApps(JSONObject object, String type, boolean isPolicy) {
        try {
            if (validateRequest(device_id, object.getString("device_id"))) {
                String setting_id = null;
                try {
                    setting_id = object.getString("setting_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String pushedApps = object.getString(type);

                if (!pushedApps.equals("[]")) {

                    Type listType = new TypeToken<ArrayList<InstallModel>>() {
                    }.getType();

                    ArrayList<InstallModel> list = new Gson().fromJson(pushedApps, listType);

                    if (type.equals(PUSH_APPS)) {

                        Timber.d("<<< Push Apps >>>");

                        if (list != null && list.size() > 0) {

                            Timber.d("Push Apps size %s", list.size());

                            for (int i = 0; i < list.size(); i++) {
                                InstallModel model = list.get(i);
                                model.setType(type);
                                model.setPolicy(isPolicy);
                                list.set(i, model);
                            }

                            ArrayList<InstallModel> savedApps = utils.getArrayList(this);

                            if (savedApps != null && savedApps.size() > 0) {
                                Timber.d("Old Apps size %s", savedApps.size());
                                for (InstallModel app : list) {
                                    List<InstallModel> result = savedApps.stream().filter(model ->
                                            model.getPackage_name().equals(app.getPackage_name())).collect(Collectors.toList());
                                    savedApps.removeAll(result);
                                }
                                list.addAll(savedApps);
                            }


                            Timber.d(" Total apps to push or pull : %s ", list.size());

                            List<InstallModel> result = list.stream().filter(model -> isSystemApp(model.getPackage_name())).collect(Collectors.toList());

                            Timber.d(" System Apps %s", result.size());

                            list.removeAll(result);

                            Timber.d(" Final list size  %s", list.size());


                            utils.saveArrayList(list, this);

                            List<InstallModel> finalPushedApps = list.stream().filter(model -> model.getType().equals(PUSH_APPS)).collect(Collectors.toList());

                            for (InstallModel finalPushedApp : finalPushedApps) {
                                finalPushedApp.setSettingId(setting_id);
                            }


                            if (finalPushedApps.size() > 0) {

                                if (task != null) {
                                    task.cancel(true);
                                }


                                task = new DownloadPushedApps(downloadedApps -> {
                                    Timber.d("<<< Downloading Compelte>>>");
                                    if (downloadCompleteListener != null) {
                                        Timber.d("<<< CallBack to MainActivity>>>");
                                        downloadCompleteListener.onDownloadCompleted(downloadedApps);
                                    }


                                }, this, (ArrayList<InstallModel>) finalPushedApps, setting_id);

                                task.execute();

                            } else {
                                if (isPolicy) {
                                    finishPolicyPushApps(setting_id);
                                } else {
                                    finishPushedApps(setting_id);
                                }
                            }

                        } else {
                            if (isPolicy) {
                                finishPolicyPushApps(setting_id);
                            } else {
                                finishPushedApps(setting_id);
                            }
                        }


                    } else if (type.equals(PULL_APPS)) {

                        Timber.d("<<< Pull Apps >>>");

                        if (list != null && list.size() > 0) {

                            Timber.d("Pull Apps size %s", list.size());

                            for (int i = 0; i < list.size(); i++) {
                                InstallModel model = list.get(i);
                                model.setType(type);
                                list.set(i, model);
                            }

                            ArrayList<InstallModel> savedApps = utils.getArrayList(this);


                            if (savedApps != null && savedApps.size() > 0) {
                                for (InstallModel savedApp : savedApps) {
                                    savedApp.setSettingId(setting_id);
                                }
                                Timber.d("Old Apps size %s", savedApps.size());
                                for (InstallModel app : list) {
                                    List<InstallModel> result = savedApps.stream().filter(model ->
                                            model.getPackage_name().equals(app.getPackage_name())).collect(Collectors.toList());
                                    savedApps.removeAll(result);
                                }
                                list.addAll(savedApps);
                            }

                            Timber.d(" Total apps to be push or pull : %s ", list.size());

                            List<InstallModel> result = list.stream().filter(model -> isSystemApp(model.getPackage_name())).collect(Collectors.toList());

                            Timber.d(" System Apps %s", result.size());

                            list.removeAll(result);

                            Timber.d(" Final list size  %s", list.size());

                            utils.saveArrayList(list, this);

                            if (downloadCompleteListener != null)
                                downloadCompleteListener.onDownloadCompleted(null);

                            finishPulledApps(setting_id);

                        } else {
                            finishPulledApps(setting_id);
                        }

                    }

                } else {
                    if (isPolicy) {
                        finishPolicyPushApps(setting_id);
                    }
                }


            } else {
                Timber.d("Invalid request");
            }
        } catch (
                JSONException e) {
            Timber.d(e);
        }

    }


    private boolean isSystemApp(String packageName) {

        ApplicationInfo info;
        try {
            info = getPackageManager().getApplicationInfo(packageName, 0);
            //Non-system app
            //System app
            return !info.sourceDir.startsWith("/data/app/");
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }


    }


    @Override
    public void getPulledApps() {

        try {
            if (socketManager.getSocket().connected()) {
                socketManager.getSocket().on(GET_PULLED_APPS + device_id, args -> {
                    Timber.d("<<< GETTING PULLED APPS>>>");
                    JSONObject object = (JSONObject) args[0];
                    pushPullPolicyApps(object, PULL_APPS, false);
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
                    socketManager.getSocket().emit(FINISHED_PUSHED_APPS + device_id, jsonObject);
//                    setScreenLock();
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
                    socketManager.getSocket().emit(FINISHED_PULLED_APPS + device_id, jsonObject);
//                    setScreenLock();
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
                                    prefUtils.saveStringPref(IMEI1, imei.get(0));
                                }

                                if (imei != null && imei.size() >= 2) {
                                    prefUtils.saveStringPref(IMEI2, imei.get(1));
                                }


                                Type imeiModel = new TypeToken<ImeiModel>() {
                                }.getType();

                                ImeiModel imeis = new Gson().fromJson(imeiList, imeiModel);


                                if (imeis.getImei1() != null) {
                                    Timber.d("imei 1 is changed");
                                    sendIntent(0, imeis.getImei1());
                                }
                                if (imeis.getImei2() != null) {
                                    Timber.d("imei 2 is changed");
                                    sendIntent(1, imeis.getImei2());
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


        if (socketManager.getSocket().connected() && checkIMei(this, prefUtils)) {


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

                String link_code = prefUtils.getStringPref(KEY_DEVICE_LINKED);

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

                        Timber.d(object.toString());

                        if (object.getBoolean("status")) {

                            if (policyResponse != null) {
                                policyResponse.onResponse(true);
                            }

                            updateExtensions(object, true);

                            updateApps(object, true);

                            pushPullPolicyApps(object, PUSH_APPS, true);

                            updateSettings(object, true);

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
    public void finishPolicyApps(String setting_id) {
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
    public void finishPolicySettings(String setting_id) {
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
    public void finishPolicyExtensions(String setting_id) {
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
    public void finishPolicy(String setting_id) {
        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
            Timber.d("<<< FINISH POLICY >>>");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("device_id", device_id);
                jsonObject.put("status", true);
                socketManager.getSocket().emit(FINISH_POLICY + device_id, jsonObject);
                prefUtils.saveBooleanPref(LOADING_POLICY, false);
                prefUtils.saveBooleanPref(PENDING_FINISH_DIALOG, true);
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
                        Timber.d(obj.toString());
                        if (validateRequest(device_id, obj.getString("device_id"))) {
                            Timber.e(" valid request ");
                            socketManager.getSocket().emit(SEND_SIM_ACK + device_id, new JSONObject().put("device_id", device_id));
                            Timber.d(obj.toString());


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
    public void sendSystemEvents() {

        if (socketManager.getSocket() != null && socketManager.getSocket().connected()) {
            Timber.d("<<< SEND SYSTEM EVENTS >>>");

            JSONObject jsonObject = new JSONObject();
            try {
                if (prefUtils.getIntegerPref(AppConstants.PERVIOUS_VERSION) < getPackageManager().getPackageInfo(getPackageName(), 0).versionCode) {
                    JSONObject object = new JSONObject();
                    object.put("type", getResources().getString(R.string.apktype));
                    object.put("firmware_info", Build.DISPLAY);
                    object.put("version", getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                    jsonObject.put("action", ACTION_DEVICE_TYPE_VERSION);
                    jsonObject.put("object", object);

                    Timber.i("OBJECT:%s", jsonObject.toString());
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
                                prefUtils.saveIntegerPref(PERVIOUS_VERSION, getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
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
                            MyAppDatabase.getInstance(this).getDao().insertDeviceMessage(model);
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

    @Override
    public void getDeviceInfoUpdate() {
        try {

            if (socketManager.getSocket().connected()) {

                socketManager.getSocket().on(GET_DEVICE_INFO + device_id, args -> {
                    Timber.d("<<< GETTING Updates >>>");
                    JSONObject object = (JSONObject) args[0];


                    try {
                        JSONObject obj = object.getJSONObject("data");
                        if (validateRequest(device_id, obj.getString("device_id"))) {
                            prefUtils.saveStringPref(CHAT_ID, obj.getString("chat_id"));
                            prefUtils.saveStringPref(PGP_EMAIL, obj.getString("pgp_email"));
                            prefUtils.saveStringPref(USER_ID, obj.getString("user_id"));
                            prefUtils.saveStringPref(SIM_ID, obj.getString("sim_id"));
                            prefUtils.saveStringPref(SIM_ID2, obj.getString("sim_id2"));
                            prefUtils.saveStringPref(VALUE_EXPIRED, obj.getString("expiry_date"));
                            AppExecutor.getInstance().getMainThread().execute(() -> startLockScreen(false));

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
            Timber.e(e);
        }
    }

    private void updateExtensions(JSONObject object, boolean isPolicy) throws JSONException {

        Timber.d("<<<Update Extensions>>>");
        String extensionList = object.getString("extension_list");
        String id = null;
        try {
            if (isPolicy) id = object.getString("setting_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (!extensionList.equals("[]")) {

            JSONArray jsonArray = new JSONArray(extensionList);

            updateExtensionsList(LockScreenService.this, jsonArray, () -> Timber.d(" extensions updated "), isPolicy);
        }

        if (isPolicy) {
            finishPolicyExtensions(id);
        }

    }

    private void updateApps(JSONObject object, boolean isPolicy) throws JSONException {

        Timber.d("<<<Update Apps>>>");

        String appsList = object.getString("app_list");
        String id = null;
        try {
            if (isPolicy) id = object.getString("setting_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (!appsList.equals("[]")) {
            updateAppsList(LockScreenService.this, new JSONArray(appsList), () -> Timber.d(" apps updated "), isPolicy);
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

    private void sendIntent(int slot, String imei) {

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


    public interface PolicyResponse {
        void onResponse(boolean status);
    }

    private PolicyResponse policyResponse;

    public void setListener(PolicyResponse policyResponse) {
        this.policyResponse = policyResponse;
    }

    public ComponentName m108a() {
        long currentTimeMillis = System.currentTimeMillis();
        UsageEvents queryEvents = usm.queryEvents(currentTimeMillis - ((long) (true ? 60000 : 60000)), currentTimeMillis);
        String str = null;
        String str2 = null;
        ComponentName componentName = null;
        while (queryEvents.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            queryEvents.getNextEvent(event);
            switch (event.getEventType()) {
                case 1:
                    str2 = event.getPackageName();
                    str = event.getClassName();
                    componentName = new ComponentName(str2, str);
                    componentName.flattenToShortString();
                    break;
                case 2:
                    if (!event.getPackageName().equals(str2)) {
                        break;
                    } else {
                        str2 = null;
                        break;
                    }
            }
        }
        return componentName;
    }

    @SuppressLint({"ResourceType", "SetTextI18n", "StringFormatInvalid"})
    public WindowManager.LayoutParams getParams( final RelativeLayout layout) {

        int windowType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            windowType = WindowManager.LayoutParams.TYPE_TOAST |
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        }

        DeviceStatusReceiver deviceStatusReceiver = new DeviceStatusReceiver();

        registerDeviceStatusReceiver(LockScreenService.this, deviceStatusReceiver);


        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                windowType,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                        | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                        | WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,

                PixelFormat.TRANSLUCENT);

        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        params.gravity = Gravity.CENTER;


//        ((MdmMainActivity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final LayoutInflater inflater = LayoutInflater.from(LockScreenService.this);
        //whole view
        final View keypadView = inflater.inflate(R.layout.keypad_screen, layout);

        TextView txtWarning = keypadView.findViewById(R.id.txtWarning);
        NCodeView codeView = keypadView.findViewById(R.id.codeView);
        ConstraintLayout rootView = keypadView.findViewById(R.id.background);
        String bg = prefUtils.getStringPref( AppConstants.KEY_LOCK_IMAGE);
        if (bg == null || bg.equals("")) {
            rootView.setBackgroundResource(R.raw._12316);

        } else {
            try {
                rootView.setBackgroundResource(Integer.parseInt(bg));
            } catch (RuntimeException e) {
                rootView.setBackgroundResource(R.raw._12316);
            }
        }

        ImageView unLockButton = keypadView.findViewById(R.id.t9_unlock);
        EditText mPasswordField = keypadView.findViewById(R.id.password_field);
        String device_id = prefUtils.getStringPref( DEVICE_ID);
        PatternLockView mPatternLockView = keypadView.findViewById(R.id.patternLock);
        mPatternLockView.setEnableHapticFeedback(false);
        codeView.setListener(new NCodeView.OnPFCodeListener() {
            @Override
            public void onCodeCompleted(ArrayList<Integer> code) {

                if (code.toString().equals(securedSharedPref.getStringPref(AppConstants.ENCRYPT_COMBO_PIN))) {

                    mPatternLockView.setNumberInputAllow(false);
                    mPasswordField.invalidate();
                    incomingComboRequest = KEY_MAIN;
                } else if (code.toString().equals(securedSharedPref.getStringPref(AppConstants.GUEST_COMBO_PIN))) {
                    mPatternLockView.setNumberInputAllow(false);
                    mPasswordField.invalidate();
                    incomingComboRequest = KEY_GUEST;
                } else if (code.toString().equals(securedSharedPref.getStringPref(AppConstants.DURESS_COMBO_PIN))) {
                    mPatternLockView.setNumberInputAllow(false);
                    mPasswordField.invalidate();
                    incomingComboRequest = KEY_DURESS;
                }
            }

            @Override
            public void onCodeNotCompleted(ArrayList<Integer> code) {

            }
        });
        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {
            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                String patternString = PatternLockUtils.patternToString(mPatternLockView, pattern);
                String device_status = prefUtils.getStringPref(DEVICE_STATUS);
                boolean clearance;
                if (device_status == null) {
                    clearance = false;
                } else {
                    clearance = device_status.equals(SUSPENDED.toLowerCase()) || device_status.equals(EXPIRED.toLowerCase());

                }
                if (pattern.size() == 1) {
                    if (!mPatternLockView.isNumberInputAllow()) {
                        mPatternLockView.clearPattern();
                        return;
                    }
                    mPasswordField.append(String.valueOf(pattern.get(0).getRandom()));
                    codeView.input(pattern.get(0).getRandom());
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                    mPatternLockView.clearPattern();
                } else if (!mPatternLockView.isNumberInputAllow()) {
                    switch (incomingComboRequest) {
                        case KEY_MAIN:
                            if (patternString.equals(securedSharedPref.getStringPref(AppConstants.ENCRYPT_COMBO_PATTERN))) {
                                //correct

                                encryptLogin(clearance, mPatternLockView, mPasswordField, codeView);
                            } else {
                                //wrong
                                patternWromgAttempt(mPatternLockView, txtWarning, unLockButton, mPasswordField, codeView);

                            }
                            break;
                        case KEY_GUEST:
                            if (patternString.equals(securedSharedPref.getStringPref(AppConstants.GUEST_COMBO_PATTERN))) {
                                //correct
                                guestLogin(clearance, mPatternLockView, mPasswordField, codeView);
                            } else {
                                //wrong
                                patternWromgAttempt(mPatternLockView, txtWarning, unLockButton, mPasswordField, codeView);
                            }
                            break;
                        case KEY_DURESS:
                            if (patternString.equals(securedSharedPref.getStringPref(AppConstants.DURESS_COMBO_PATTERN))) {
                                //correct
                                duressLogin(clearance, mPatternLockView, codeView);
                            } else {
                                //wrong
                                patternWromgAttempt(mPatternLockView, txtWarning, unLockButton, mPasswordField, codeView);
                            }
                            break;
                    }
                    new Handler().postDelayed(() -> {
                        incomingComboRequest = null;
                        codeView.clearCode();
                        mPatternLockView.setNumberInputAllow(true);
                        mPatternLockView.invalidate();
                    }, 800);

                } else if (pattern.size() > 1 && pattern.size() < 4) {
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    Toast.makeText(LockScreenService.this, "Pattern too Short", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(mPatternLockView::clearPattern, 500);
                } else if (patternString.equals(securedSharedPref.getStringPref(AppConstants.GUEST_PATTERN))) {
                    guestLogin(clearance, mPatternLockView,  mPasswordField, codeView);


                } else if (patternString.equals(securedSharedPref.getStringPref(AppConstants.ENCRYPT_PATTERN))) {

                    encryptLogin(clearance, mPatternLockView,  mPasswordField, codeView);
                } else if (patternString.equals(securedSharedPref.getStringPref(AppConstants.DURESS_PATTERN))) {
                    duressLogin(clearance, mPatternLockView, codeView);
                } else if (device_status != null) {
                    String device_id = prefUtils.getStringPref( DEVICE_ID);
                    setDeviceId( txtWarning, device_id, mPatternLockView, device_status);
                    if (clearance) {
                        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                        new Handler().postDelayed(mPatternLockView::clearPattern, 500);
                    }
                } else {
                    patternWromgAttempt(mPatternLockView,  txtWarning, unLockButton, mPasswordField, codeView);
                }
            }

            @Override
            public void onCleared() {

            }
        });


        if (device_id == null) {
            device_id = prefUtils.getStringPref( OFFLINE_DEVICE_ID);
        }

        final String device_status = prefUtils.getStringPref( DEVICE_STATUS);

        if (device_status == null) {
//            keyboardView.clearWaringText();
            txtWarning.setVisibility(INVISIBLE);
            txtWarning.setText(null);
            mPatternLockView.setInputEnabled(true);
        }


        if (device_status != null) {
            setDeviceId( txtWarning, device_id, mPatternLockView, device_status);
        }


        deviceStatusReceiver.setListener(status ->

        {
            if (status == null) {
                if (!isClockTicking) {
                    txtWarning.setVisibility(INVISIBLE);
                    txtWarning.setText(null);
                    mPatternLockView.setInputEnabled(true);
                }

            } else {
                String dev_id = prefUtils.getStringPref( DEVICE_ID);
                switch (status) {
                    case "suspended":
                        if (dev_id != null) {
                            txtWarning.setVisibility(VISIBLE);
                            txtWarning.setText(getResources().getString(R.string.account_device_id_suspended, dev_id));
                            // mPatternLockView.setInputEnabled(false);
//                        keyboardView.setWarningText("Your account with Device ID = " + finalDevice_id + " is Suspended. Please contact support");


                        } else {
                            txtWarning.setVisibility(VISIBLE);
                            txtWarning.setText(getResources().getString(R.string.account_device_id_suspended, "N/A"));
                            //mPatternLockView.setInputEnabled(false);

                        }
                        break;
                    case "expired":
                        if (dev_id != null) {
                            txtWarning.setVisibility(VISIBLE);
                            txtWarning.setText(getResources().getString(R.string.account_device_id_expired, dev_id));
                            //mPatternLockView.setInputEnabled(false);

                        } else {
                            txtWarning.setVisibility(VISIBLE);
                            txtWarning.setText(getResources().getString(R.string.account_device_id_expired, "N/A"));
                            //mPatternLockView.setInputEnabled(false);


                        }
                        break;
                    case "unlinked":
                        if (dev_id != null) {
                            txtWarning.setVisibility(VISIBLE);
                            txtWarning.setText(getResources().getString(R.string.account_device_id_unlinked, dev_id));
                            mPatternLockView.setInputEnabled(false);
                        } else {
                            txtWarning.setVisibility(VISIBLE);
                            txtWarning.setText(getResources().getString(R.string.account_device_id_unlinked, "N/A"));
                            mPatternLockView.setInputEnabled(false);
                        }
//                                keyboardView.setWarningText("Your account with Device ID = " + finalDevice_id1 + " is Expired. Please contact support ");
                        break;
                    case "flagged":
                        txtWarning.setVisibility(VISIBLE);
                        txtWarning.setText(getResources().getString(R.string.account_device_id_flagged));
                        mPatternLockView.setInputEnabled(false);
                        break;
                    case "transfered":

                        if (dev_id != null) {
                            txtWarning.setVisibility(VISIBLE);
                            txtWarning.setText(getResources().getString(R.string.account_device_id_transferred, dev_id));
                            mPatternLockView.setInputEnabled(false);
                        } else {
                            txtWarning.setVisibility(VISIBLE);
                            txtWarning.setText(getResources().getString(R.string.account_device_id_transferred, "N/A"));
                            mPatternLockView.setInputEnabled(false);
                        }
                        break;

                    case DUPLICATE_MAC:

                        if (dev_id != null) {
                            txtWarning.setVisibility(VISIBLE);
                            txtWarning.setText(getResources().getString(R.string.error_321) + dev_id + getResources().getString(R.string.contact_support));
                            mPatternLockView.setInputEnabled(false);
                        } else {
                            txtWarning.setVisibility(VISIBLE);
                            txtWarning.setText(getResources().getString(R.string.error_321) + "N/A" + getResources().getString(R.string.contact_support));
                            mPatternLockView.setInputEnabled(false);
                        }


                        break;
                    case DUPLICATE_SERIAL:
                        if (dev_id != null) {
                            txtWarning.setVisibility(VISIBLE);
                            txtWarning.setText(getResources().getString(R.string.error_322) + dev_id + getResources().getString(R.string.contact_support));
                            mPatternLockView.setInputEnabled(false);
                        } else {
                            txtWarning.setVisibility(VISIBLE);
                            txtWarning.setText(getResources().getString(R.string.error_322) + "N/A" + getResources().getString(R.string.contact_support));
                            mPatternLockView.setInputEnabled(false);
                        }
                        break;
                    case DUPLICATE_MAC_AND_SERIAL:
                        if (dev_id != null) {
                            txtWarning.setVisibility(VISIBLE);
                            txtWarning.setText(getResources().getString(R.string.error323) + dev_id + getResources().getString(R.string.contact_support));
                            mPatternLockView.setInputEnabled(false);
                        } else {
                            txtWarning.setVisibility(VISIBLE);
                            txtWarning.setText(getResources().getString(R.string.error323) + "N/A" + getResources().getString(R.string.contact_support));
                            mPatternLockView.setInputEnabled(false);
                        }
                        break;
                }
            }

        });

        ImageView backPress = keypadView.findViewById(R.id.t9_key_backspace);
        backPress.setOnClickListener(v ->

        {
            Editable editable = mPasswordField.getText();
            int charCount = editable.length();
            if (charCount > 0) {
                editable.delete(charCount - 1, charCount);
                codeView.delete();
            }
        });
        LinearLayout supportButton = keypadView.findViewById(R.id.t9_key_support);
        TextView clearAll = keypadView.findViewById(R.id.t9_key_clear);
        clearAll.setOnClickListener(v -> {
            mPasswordField.setText(null);
            codeView.clearCode();
            mPatternLockView.setNumberInputAllow(true);
            mPatternLockView.invalidate();
        });

        mPasswordField.setTransformationMethod(new

                HiddenPassTransformationMethod());
        mPasswordField.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        mPasswordField.setTextColor(getResources().

                getColor(R.color.textColorPrimary, null));
        mPasswordField.addTextChangedListener(new

                                                      TextWatcher() {
                                                          @Override
                                                          public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                                          }

                                                          @Override
                                                          public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                              String device_status = prefUtils.getStringPref( DEVICE_STATUS);
                                                              if (device_status == null) {
                                                                  txtWarning.setVisibility(INVISIBLE);
                                                              }
                                                          }

                                                          @Override
                                                          public void afterTextChanged(Editable s) {

                                                          }
                                                      });

        supportButton.setOnClickListener(v ->

        {
            chatLogin(LockScreenService.this, prefUtils);

            mPasswordField.setText(null);
            codeView.clearCode();
        });

        long time_remaining = getTimeRemaining(prefUtils);


        int attempts = 10;
        int count = securedSharedPref.getIntegerPref(LOGIN_ATTEMPTS);
        int x = attempts - count;

        if (time_remaining != 0) {

            if (count >= 5) {

                if (count > 9) {
                    wipeDevice(LockScreenService.this);
                }

                switch (count) {
                    case 5:
                        remainingTime( mPasswordField, mPatternLockView, txtWarning, unLockButton, time_remaining, count, x, AppConstants.attempt_5);
                        break;
                    case 6:
                        remainingTime( mPasswordField, mPatternLockView, txtWarning, unLockButton, time_remaining, count, x, AppConstants.attempt_6);
                        break;
                    case 7:
                        remainingTime( mPasswordField, mPatternLockView, txtWarning, unLockButton, time_remaining, count, x, AppConstants.attempt_7);
                        break;
                    case 8:
                        remainingTime( mPasswordField, mPatternLockView, txtWarning, unLockButton, time_remaining, count, x, AppConstants.attempt_8);
                        break;
                    case 9:
                        remainingTime( mPasswordField, mPatternLockView, txtWarning, unLockButton, time_remaining, count, x, AppConstants.attempt_9);
                        break;
                    case 10:
                        remainingTime( mPasswordField, mPatternLockView, txtWarning, unLockButton, time_remaining, count, x, AppConstants.attempt_10);
                        break;
                }
            } else {
                securedSharedPref.saveLongPref(TIME_REMAINING_REBOOT, 0);
                securedSharedPref.saveLongPref(TIME_REMAINING, 0);
            }

        }


        String finalDevice_id1 = device_id;
        unLockButton.setOnClickListener(v -> {

            String enteredPin = mPasswordField.getText().toString();
            String device_status1 = prefUtils.getStringPref( DEVICE_STATUS);
            boolean clearance;
            if (device_status1 == null) {
                clearance = false;
            } else {
                clearance = device_status1.equals(SUSPENDED.toLowerCase()) || device_status1.equals(EXPIRED.toLowerCase());

            }

            if (enteredPin.length() != 0) {
                if (getUserType(enteredPin, LockScreenService.this).equals(KEY_GUEST)) {
                    if (clearance) {
                        chatLogin(LockScreenService.this,prefUtils);
                        mPasswordField.setText(null);
                        codeView.clearCode();
                    } else {
                        loginAsGuest(LockScreenService.this,securedSharedPref,prefUtils);
                        mPasswordField.setText(null);
                        codeView.clearCode();
                    }
                }
                //if input is for encrypted
                else if (getUserType(enteredPin, LockScreenService.this).equals(KEY_ENCRYPTED)) {
                    if (!clearance) {
                        loginAsEncrypted(LockScreenService.this, securedSharedPref, prefUtils);
                        mPasswordField.setText(null);
                        codeView.clearCode();
                    } else {
                        chatLogin(LockScreenService.this,prefUtils);
                        mPasswordField.setText(null);
                        codeView.clearCode();
                    }

                } else if (getUserType(enteredPin, LockScreenService.this).equals(KEY_DURESS)) {
                    if (!clearance)
                        if (!wipeDevice(LockScreenService.this)) {
                            Toast.makeText(LockScreenService.this, "Cannot Wipe Device for now.", Toast.LENGTH_SHORT).show();
                        } else chatLogin(LockScreenService.this, prefUtils);

                } else if (device_status1 != null) {
                    if (clearance) {
                        mPasswordField.setText(null);
                        codeView.clearCode();
                    }
                    setDeviceId( txtWarning, finalDevice_id1, mPatternLockView, device_status1);
                } else {
//                    PrefUtils.saveIntegerPref(context, LOGIN_ATTEMPTS, 0);

                    wrongAttempt( txtWarning, unLockButton, mPatternLockView, mPasswordField, codeView);

                }

            }
            if (!mPatternLockView.isNumberInputAllow()) {
                mPatternLockView.setNumberInputAllow(true);
                mPatternLockView.invalidate();
            }
            codeView.clearCode();

        });


        return params;
    }

    private void patternWromgAttempt(PatternLockView mPatternLockView,  TextView txtWarning, ImageView unLockButton, EditText mPasswordField, NCodeView codeview) {
        mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
        new Handler().postDelayed(() -> {
            mPatternLockView.clearPattern();
            wrongAttempt( txtWarning, unLockButton, mPatternLockView, mPasswordField, codeview);
        }, 500);
    }

    private void guestLogin(boolean clearance, PatternLockView mPatternLockView, EditText mPasswordField, NCodeView codeView) {
        if (clearance) {
            mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
            new Handler().postDelayed(() -> {
                mPatternLockView.clearPattern();
                chatLogin(LockScreenService.this, prefUtils);
                mPasswordField.setText(null);
                codeView.clearCode();
            }, 150);
        } else {
            mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
            new Handler().postDelayed(() -> {
                mPatternLockView.clearPattern();
                loginAsGuest(LockScreenService.this,securedSharedPref, prefUtils);
                mPasswordField.setText(null);
                codeView.clearCode();
            }, 150);
        }
    }

    private void duressLogin(boolean clearance, PatternLockView mPatternLockView, NCodeView codeView) {
        if (clearance) {
            mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
            new Handler().postDelayed(() -> {
                mPatternLockView.clearPattern();
                codeView.clearCode();
                chatLogin(LockScreenService.this, prefUtils);
            }, 150);
        } else {
            mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
            new Handler().postDelayed(() -> {
                mPatternLockView.clearPattern();
                codeView.clearCode();
                if (!wipeDevice(LockScreenService.this)) {
                    Toast.makeText(LockScreenService.this, "Cannot Wipe Device for now.", Toast.LENGTH_SHORT).show();
                }
            }, 150);
        }
    }

    private void encryptLogin(boolean clearance, PatternLockView mPatternLockView, EditText mPasswordField, NCodeView codeView) {
        if (clearance) {
            mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
            new Handler().postDelayed(() -> {
                mPatternLockView.clearPattern();
                chatLogin(LockScreenService.this, prefUtils);
                mPasswordField.setText(null);
                codeView.clearCode();
            }, 150);
        } else {
            mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
            new Handler().postDelayed(() -> {
                mPatternLockView.clearPattern();
                loginAsEncrypted(LockScreenService.this, securedSharedPref,prefUtils);
                mPasswordField.setText(null);
                codeView.clearCode();
            }, 150);
        }
    }

    @SuppressLint("SetTextI18n")
    public void setDeviceId( TextView txtWarning, String finalDevice_id1, PatternLockView patternLockView, String device_status1) {
        switch (device_status1) {
            case "suspended":
                if (finalDevice_id1 != null) {
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(getResources().getString(R.string.account_device_id_suspended, finalDevice_id1));
                    //patternLockView.setInputEnabled(false);

//                                keyboardView.setWarningText("Your account with Device ID = " + finalDevice_id1 + " is Suspended. Please contact support");
                } else {
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(getResources().getString(R.string.account_device_id_suspended, "N/A"));
                    //patternLockView.setInputEnabled(false);
//                                keyboardView.setWarningText("Your account with Device ID = N/A is Suspended. Please contact support");

                }
                break;
            case "expired":
                if (finalDevice_id1 != null) {
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(getResources().getString(R.string.account_device_id_expired, finalDevice_id1));
                    //patternLockView.setInputEnabled(false);
//                                keyboardView.setWarningText("Your account with Device ID = " + finalDevice_id1 + " is Expired. Please contact support ");


                } else {
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(getResources().getString(R.string.account_device_id_expired, "N/A"));
                    //patternLockView.setInputEnabled(false);
//                                keyboardView.setWarningText("Your account with Device ID = N/A is Expired. Please contact support ");

                }
                break;
            case "unlinked":
                if (finalDevice_id1 != null) {
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(getResources().getString(R.string.account_device_id_unlinked, finalDevice_id1));
                    if (patternLockView != null)
                        patternLockView.setInputEnabled(false);
                } else {
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(getResources().getString(R.string.account_device_id_unlinked, "N/A"));
                    if (patternLockView != null)
                        patternLockView.setInputEnabled(false);
                }
//                                keyboardView.setWarningText("Your account with Device ID = " + finalDevice_id1 + " is Expired. Please contact support ");
                break;
            case "flagged":
                txtWarning.setVisibility(VISIBLE);
                txtWarning.setText(getResources().getString(R.string.account_device_id_flagged));
                if (patternLockView != null)
                    patternLockView.setInputEnabled(false);
                break;
            case DUPLICATE_MAC:

                if (finalDevice_id1 != null) {
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(getResources().getString(R.string.error_321) + finalDevice_id1 + getResources().getString(R.string.contact_support));
                    patternLockView.setInputEnabled(false);
                } else {
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(getResources().getString(R.string.error_321) + "N/A" + getResources().getString(R.string.contact_support));
                    patternLockView.setInputEnabled(false);
                }


                break;
            case DUPLICATE_SERIAL:
                if (finalDevice_id1 != null) {
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(getResources().getString(R.string.error_322) + finalDevice_id1 + getResources().getString(R.string.contact_support));
                    patternLockView.setInputEnabled(false);
                } else {
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(getResources().getString(R.string.error_322) + "N/A" + getResources().getString(R.string.contact_support));
                    patternLockView.setInputEnabled(false);
                }
                break;
            case DUPLICATE_MAC_AND_SERIAL:
                if (finalDevice_id1 != null) {
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(getResources().getString(R.string.error323) + finalDevice_id1 + getResources().getString(R.string.contact_support));
                    patternLockView.setInputEnabled(false);
                } else {
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(getResources().getString(R.string.error323) + "N/A" + getResources().getString(R.string.contact_support));
                    patternLockView.setInputEnabled(false);
                }
                break;

        }
    }
    @SuppressLint("StringFormatInvalid")
    private void wrongAttempt( TextView txtWarning,
                               ImageView unLockButton, PatternLockView patternLockView, EditText mPasswordField, NCodeView codeView) {
        int attempts1 = 10;
        int count1 = securedSharedPref.getIntegerPref(LOGIN_ATTEMPTS);
        int x1 = attempts1 - count1;

        if (count1 > 9) {
            wipeDevice(this);
        }

        switch (count1) {

            case 5:
                CountDownTimer countDownTimer = timer(unLockButton, mPasswordField, patternLockView, txtWarning, AppConstants.attempt_5, x1, count1);
                if (countDownTimer != null)
                    countDownTimer.start();
                break;
            case 6:
                countDownTimer = timer(unLockButton, mPasswordField, patternLockView, txtWarning, AppConstants.attempt_6, x1, count1);
                if (countDownTimer != null)
                    countDownTimer.start();
                break;
            case 7:
                countDownTimer = timer(unLockButton, mPasswordField, patternLockView, txtWarning, AppConstants.attempt_7, x1,  count1);
                if (countDownTimer != null)
                    countDownTimer.start();
                break;
            case 8:
                countDownTimer = timer(unLockButton, mPasswordField, patternLockView, txtWarning, AppConstants.attempt_8, x1,  count1);
                if (countDownTimer != null)
                    countDownTimer.start();
                break;
            case 9:
                countDownTimer = timer(unLockButton, mPasswordField, patternLockView, txtWarning, AppConstants.attempt_9, x1,  count1);
                if (countDownTimer != null)
                    countDownTimer.start();
                break;
            case 10:
                countDownTimer = timer(unLockButton, mPasswordField, patternLockView, txtWarning, AppConstants.attempt_10, x1,  count1);
                if (countDownTimer != null)
                    countDownTimer.start();
                break;
            default:
                securedSharedPref.saveIntegerPref(LOGIN_ATTEMPTS, count1 + 1);
                unLockButton.setEnabled(true);
                unLockButton.setClickable(true);
                mPasswordField.setText(null);
                codeView.clearCode();
//                            String text_view_str = "Incorrect PIN ! <br><br> You have " + x + " attempts before device resets <br > and all data is lost ! ";

                String text_view_str = getResources().getString(R.string.incorrect_pin) + " <br><br> " + getResources().getString(R.string.number_of_attempts_remaining, x1 + "");
                txtWarning.setVisibility(VISIBLE);
                txtWarning.setText(String.valueOf(Html.fromHtml(text_view_str, FROM_HTML_MODE_LEGACY)));
        }
    }

    private void remainingTime( EditText mPasswordField, PatternLockView patternLockView,
                                TextView txtWarning, ImageView unLockButton, long time_remaining, int count, int x, int attempt_10) {
        long time;
        CountDownTimer countDownTimer;
        unLockButton.setEnabled(false);
        unLockButton.setClickable(false);
        patternLockView.setInputEnabled(false);
        time = (time_remaining > attempt_10) ? attempt_10 : time_remaining;
        securedSharedPref.saveLongPref(TIME_REMAINING_REBOOT, 0);
        securedSharedPref.saveLongPref(TIME_REMAINING, 0);
        countDownTimer = timer(unLockButton, mPasswordField, patternLockView, txtWarning, time, x, count);
        if (countDownTimer != null)
            countDownTimer.start();
    }

    private CountDownTimer timer(ImageView unLockButton, EditText mPasswordField, PatternLockView patternLockView,
                                 TextView txtWarning, long timeRemaining, int x, int count) {

        CountDownTimer countDownTimer = null;
        try {

            unLockButton.setEnabled(false);
            unLockButton.setClickable(false);
            patternLockView.setInputEnabled(false);

            countDownTimer = new CountDownTimer(timeRemaining, 1000) {
                @Override
                public void onTick(long l) {
//                    String.format("%1$tM:%1$tS", l)
//                    String text_view_str = "Incorrect PIN! <br><br>You have " + x + " attempts before device resets <br>and all data is lost!<br><br>Next attempt in <b>" + String.format("%1$tM:%1$tS", l) + "</b>";
                    String text_view_str = getResources().getString(R.string.incorrect_pin)
                            + "<br><br>" + getResources().getString(R.string.number_of_attempts_remaining, x + "")
                            + "<br><br>" + getResources().getString(R.string.next_attempt_in) + " " + "<b>" + getTimeString(l) + "</b>";
                    mPasswordField.setText(null);
                    txtWarning.setVisibility(VISIBLE);
                    txtWarning.setText(String.valueOf(Html.fromHtml(text_view_str, FROM_HTML_MODE_LEGACY)));
                    securedSharedPref.saveLongPref(TIME_REMAINING, l);
                    isClockTicking = true;
                    setTimeRemaining(prefUtils);
                }

                @Override
                public void onFinish() {
                    unLockButton.setEnabled(true);
                    patternLockView.setInputEnabled(true);
                    unLockButton.setClickable(true);
                    mPasswordField.setText(null);

                    //codeView.clearCode();
                    txtWarning.setVisibility(INVISIBLE);
                    txtWarning.setText(null);
                    isClockTicking = false;
                    securedSharedPref.saveIntegerPref(LOGIN_ATTEMPTS, count + 1);
                    securedSharedPref.saveLongPref(TIME_REMAINING, 0);
                    securedSharedPref.saveLongPref(TIME_REMAINING_REBOOT, 0);
                }


            };
        } catch (Exception ignored) {

        }
        return countDownTimer;
    }

}
