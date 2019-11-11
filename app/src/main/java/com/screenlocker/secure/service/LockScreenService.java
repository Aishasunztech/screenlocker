package com.screenlocker.secure.service;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.manual_load.DownloadCompleteListener;
import com.screenlocker.secure.manual_load.DownloadPushedApps;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.mdm.utils.NetworkChangeReceiver;
import com.screenlocker.secure.notifications.NotificationItem;
import com.screenlocker.secure.permissions.SteppersActivity;
import com.screenlocker.secure.room.SimEntry;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.service.apps.ServiceConnectedListener;
import com.screenlocker.secure.service.apps.WindowChangeDetectingService;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.socket.SocketManager;
import com.screenlocker.secure.socket.TransparentActivity;
import com.screenlocker.secure.socket.interfaces.OnSocketConnectionListener;
import com.screenlocker.secure.socket.interfaces.SocketEvents;
import com.screenlocker.secure.socket.model.ImeiModel;
import com.screenlocker.secure.socket.model.InstallModel;
import com.screenlocker.secure.socket.model.Settings;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.updateDB.BlurWorker;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Utils;
import com.screenlocker.secure.views.PrepareLockScreen;
import com.screenlocker.secure.views.patternlock.PatternLockView;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import timber.log.Timber;

import static com.screenlocker.secure.app.MyApplication.getAppContext;
import static com.screenlocker.secure.mdm.utils.DeviceIdUtils.isValidImei;
import static com.screenlocker.secure.socket.utils.utils.changeSettings;
import static com.screenlocker.secure.socket.utils.utils.checkIMei;
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
import static com.screenlocker.secure.utils.AppConstants.ACTION_DEVICE_TYPE_VERSION;
import static com.screenlocker.secure.utils.AppConstants.ACTION_PULL_APPS;
import static com.screenlocker.secure.utils.AppConstants.ACTION_PUSH_APPS;
import static com.screenlocker.secure.utils.AppConstants.ALLOW_ENCRYPTED_ALL;
import static com.screenlocker.secure.utils.AppConstants.ALLOW_GUEST_ALL;
import static com.screenlocker.secure.utils.AppConstants.APPS_HASH_MAP;
import static com.screenlocker.secure.utils.AppConstants.APPS_SETTING_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.BROADCAST_APPS_ACTION;
import static com.screenlocker.secure.utils.AppConstants.CLIENT_SOCKET_URL;
import static com.screenlocker.secure.utils.AppConstants.CONNECTED;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_NETWORK_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEFAULT_MAIN_PASS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.EMERGENCY_FLAG;
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
import static com.screenlocker.secure.utils.AppConstants.KEY_SUPPORT_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.LIMITED;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.LOADING_POLICY;
import static com.screenlocker.secure.utils.AppConstants.LOAD_POLICY;
import static com.screenlocker.secure.utils.AppConstants.PENDING_FINISH_DIALOG;
import static com.screenlocker.secure.utils.AppConstants.PERMISSION_GRANTING;
import static com.screenlocker.secure.utils.AppConstants.PERVIOUS_VERSION;
import static com.screenlocker.secure.utils.AppConstants.PULL_APPS;
import static com.screenlocker.secure.utils.AppConstants.PUSH_APPS;
import static com.screenlocker.secure.utils.AppConstants.REBOOT_RESTRICTION_DELAY;
import static com.screenlocker.secure.utils.AppConstants.RESTRICTION_DELAY;
import static com.screenlocker.secure.utils.AppConstants.SECURE_SETTINGS_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.SEND_APPS;
import static com.screenlocker.secure.utils.AppConstants.SEND_EXTENSIONS;
import static com.screenlocker.secure.utils.AppConstants.SEND_PULLED_APPS_STATUS;
import static com.screenlocker.secure.utils.AppConstants.SEND_PUSHED_APPS_STATUS;
import static com.screenlocker.secure.utils.AppConstants.SEND_SETTINGS;
import static com.screenlocker.secure.utils.AppConstants.SEND_SIM_ACK;
import static com.screenlocker.secure.utils.AppConstants.SETTINGS_APPLIED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.SETTINGS_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.SIM_0_ICCID;
import static com.screenlocker.secure.utils.AppConstants.SIM_1_ICCID;
import static com.screenlocker.secure.utils.AppConstants.SOCKET_STATUS;
import static com.screenlocker.secure.utils.AppConstants.START_SOCKET;
import static com.screenlocker.secure.utils.AppConstants.STOP_SOCKET;
import static com.screenlocker.secure.utils.AppConstants.SYSTEM_EVENT_BUS;
import static com.screenlocker.secure.utils.AppConstants.TOKEN;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
import static com.screenlocker.secure.utils.AppConstants.UNINSTALLED_APPS;
import static com.screenlocker.secure.utils.AppConstants.WRITE_IMEI;
import static com.screenlocker.secure.utils.CommonUtils.setAlarmManager;
import static com.screenlocker.secure.utils.PrefUtils.PREF_FILE;
import static com.screenlocker.secure.utils.Utils.isAccessServiceEnabled;
import static com.screenlocker.secure.utils.Utils.scheduleExpiryCheck;
import static com.screenlocker.secure.utils.Utils.scheduleUpdateCheck;
import static com.screenlocker.secure.views.PrepareLockScreen.setDeviceId;
import static com.secureSetting.UtilityFunctions.setScreenBrightness;

/**
 * this service is the startForeground service to kepp the lock screen going when user lock the phone
 * (must enable service by enabling service from settings screens{@link SettingsActivity#onClick(View)})
 */


public class LockScreenService extends Service implements ServiceConnectedListener, OnSocketConnectionListener, SocketEvents {
    private SharedPreferences sharedPref;
    private KeyguardManager myKM;
    private RelativeLayout mLayout = null;
    private ScreenOffReceiver screenOffReceiver;
    private List<NotificationItem> notificationItems;
    private WindowManager windowManager;
    private FrameLayout frameLayout;
    private WindowManager.LayoutParams localLayoutParams;
    private FrameLayout mView;
    private final IBinder binder = new LocalBinder();
    private boolean isLayoutAdded = false;
    private boolean isLocked = false;
    private WindowManager.LayoutParams params;
    private Fetch fetch;
    private int downloadId = 0;
    private boolean viewAdded = false;
    private View view;
    private SocketManager socketManager;


    private HashSet<String> tempAllowed = new HashSet<>();
    private HashSet<String> blacklist = new HashSet<>();


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
            try {
                switch (extras.getString(EXTRA_REQUEST, EXTRA_INSTALL_APP)) {
                    case EXTRA_INSTALL_APP:
                        if (installAppListener != null) {

                            installAppListener.downloadComplete(path, packageName,space);
                        } else {
                            Uri uri = Uri.fromFile(new File(path));
//                            Utils.installSielentInstall(LockScreenService.this, Objects.requireNonNull(getContentResolver().openInputStream(uri)), packageName);
                        }
                        break;
                    case EXTRA_MARKET_FRAGMENT:
                        if (marketDoaLoadLister != null)
                            marketDoaLoadLister.downloadComplete(path, packageName,space);
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
            //get file path of download
            String path = extras.getString(EXTRA_FILE_PATH, "null");
            String space = extras.getString(EXTRA_SPACE,"null");
            String request_id = extras.getString(EXTRA_REQUEST_ID_SAVED,"null");

            switch (extras.getString(EXTRA_REQUEST, EXTRA_INSTALL_APP)) {
                case EXTRA_INSTALL_APP:
                    if (installAppListener != null)
                        installAppListener.onDownLoadProgress(packageName, download.getProgress(), l1,request_id,space);
                    break;
                case EXTRA_MARKET_FRAGMENT:
                    if (marketDoaLoadLister != null)
                        marketDoaLoadLister.onDownLoadProgress(packageName, download.getProgress(), l1,request_id,space);
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
            String packageName = extras.getString(EXTRA_PACKAGE_NAME,"null");
            if(packageName != null && !packageName.equals("null")) {
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


    private DownloadServiceCallBacks downloadListener;
    private String url = "";
    private String filePath = "";
    private String packageName = "";

    private static boolean isServiceConnected = false;

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

    @Override
    public void serviceConnected(boolean status) {
        isServiceConnected = status;
//        Timber.d("access service running %s ", isServiceConnected);
    }

//    @Override
//    public void isConnected(boolean state) {
//        if (!state) {
//            destroyClientChatSocket();
//        } else {
//            connectClientChatSocket();
//        }
//
//    }

    public void destroyClientChatSocket() {
        socketManager.destroyClientChatSocket();
    }

    public void connectClientChatSocket() {
        String deviceId = PrefUtils.getStringPref(this, DEVICE_ID);

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


    /* public boolean validateAppSignature(Context context, String packageName) throws PackageManager.NameNotFoundException, NoSuchAlgorithmException {

         PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                 packageName, PackageManager.GET_SIGNATURES);
         //note sample just checks the first signature
         for (Signature signature : packageInfo.signatures) {
             // SHA1 the signature
             String sha1 = getSHA1(signature.toByteArray());
             Timber.e("SHA1:" + sha1);
             // check is matches hardcoded value
             return APP_SIGNATURE.equals(sha1);
         }

         return false;
     }

     public static boolean validateAppSignatureFile(String sha1) {

         return APP_SIGNATURE.equals(sha1);

     }


     //computed the sha1 hash of the signature
     public static String getSHA1(byte[] sig) throws NoSuchAlgorithmException {
         MessageDigest digest = MessageDigest.getInstance("SHA1");
         digest.update(sig);
         byte[] hashtext = digest.digest();
         return bytesToHex(hashtext);
     }

     //util method to convert byte array to hex string
     public static String bytesToHex(byte[] bytes) {
         final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                 '9', 'A', 'B', 'C', 'D', 'E', 'F'};
         char[] hexChars = new char[bytes.length * 2];
         int v;
         for (int j = 0; j < bytes.length; j++) {
             v = bytes[j] & 0xFF;
             hexChars[j * 2] = hexArray[v >>> 4];
             hexChars[j * 2 + 1] = hexArray[v & 0x0F];
         }
         return new String(hexChars);
     }


     public static String APP_SIGNATURE = "AD46E51439B7C0B3DBD5FD6A39E4BB73427B4F49";
  */

    @Override
    public void onCreate() {

        registerNetworkPref();

        setAlarmManager(this, System.currentTimeMillis() + 15000);

        WindowChangeDetectingService.serviceConnectedListener = this;
        socketManager = SocketManager.getInstance();
        socketManager.setSocketConnectionListener(this);

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

        FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(this)
                .setDownloadConcurrentLimit(1)
                .setProgressReportingInterval(100)
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
            scheduleUpdateCheck(this);
        }

        mLayout = new RelativeLayout(LockScreenService.this);
        notificationItems = new ArrayList<>();
        params = PrepareLockScreen.getParams(LockScreenService.this, mLayout);
        appExecutor = AppExecutor.getInstance();
        frameLayout = new FrameLayout(this);
        //smalliew
        localLayoutParams = new WindowManager.LayoutParams();
        createLayoutParamsForSmallView();
        mView = new FrameLayout(this);
        ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mView.setLayoutParams(params);
        powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        screenOffReceiver = new ScreenOffReceiver(() -> startLockScreen(true));

        if (PrefUtils.getBooleanPref(this, AppConstants.KEY_DISABLE_SCREENSHOT)) {
            disableScreenShots();
        } else {
            allowScreenShoots();
        }
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


        networkChangeReceiver = new NetworkChangeReceiver();
//        networkChangeReceiver.setNetworkChangeListener(this);

        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));


        startForeground(R.string.app_name, notification);

    }


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (PrefUtils.getBooleanPref(LockScreenService.this, TOUR_STATUS)) {
//                sheduleScreenOffMonitor();
                startRecentAppsKillThread();
            }
        }
    };
    BroadcastReceiver viewAddRemoveReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("add")) {
                //addView(android.R.color.transparent);
            } else if (intent.hasExtra("screenCapture")) {
                boolean screenCaptureStatus = intent.getBooleanExtra("screenCapture", false);
                if (screenCaptureStatus) {
                    PrefUtils.saveBooleanPref(context, AppConstants.KEY_DISABLE_SCREENSHOT, true);
                    allowScreenShoots();
                } else {
                    PrefUtils.saveBooleanPref(context, AppConstants.KEY_DISABLE_SCREENSHOT, false);
                    disableScreenShots();
                }
            } else {
                //removeView();
            }


        }

    };

    PowerManager powerManager;

    AppExecutor appExecutor;

    public void startDownload(String url, String filePath, String packageName, String type,String space) {

        Timber.i("URL %s: ", url);

        Request request = new Request(url, filePath);
        request.setPriority(Priority.HIGH);
        request.setNetworkType(NetworkType.ALL);
        request.addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG");
        Map<String, String> map = new HashMap<>();
        map.put(EXTRA_PACKAGE_NAME, packageName);
        map.put(EXTRA_FILE_PATH, filePath);
        map.put(EXTRA_REQUEST, type);
        map.put(EXTRA_SPACE,space);
        map.put(EXTRA_REQUEST_ID_SAVED,String.valueOf(request.getId()));
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


    public void setDownloadListener(DownloadServiceCallBacks downloadListener) {
        if (downloadListener != null) {
            this.downloadListener = downloadListener;
        }
    }

    @Override
    public void onDestroy() {

        try {
            Timber.d("screen locker distorting.");
            unregisterReceiver(screenOffReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
            LocalBroadcastManager.getInstance(this).unregisterReceiver(viewAddRemoveReceiver);
            PrefUtils.saveToPref(this, false);
            unRegisterNetworkPref();
            PrefUtils.saveStringPref(this, AppConstants.CURRENT_NETWORK_STATUS, AppConstants.LIMITED);
            Intent intent = new Intent(LockScreenService.this, LockScreenService.class);
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

    private boolean running = false;

    private boolean permissionStatus = false;

    private int count = 0;

    private void startRecentAppsKillThread() {

        if (appExecutor.getExecutorForSedulingRecentAppKill().isShutdown()) {
            appExecutor.readyNewExecutor();
        }

        appExecutor.getExecutorForSedulingRecentAppKill().execute(() -> {

            while (!Thread.currentThread().isInterrupted()) {

                if (!powerManager.isInteractive()) {
                    appExecutor.getMainThread().execute(() -> startLockScreen(true));
                    running = false;
                    count = 0;
                    return;
                }
                running = true;

                String package_name = getCurrentApp();

//                Timber.d("current Package %s", package_name);

                if (!PrefUtils.getBooleanPref(this, EMERGENCY_FLAG)) {

                    if (AppConstants.TEMP_SETTINGS_ALLOWED) {
                        Timber.d("Settings are temporary on");
                        if (!tempAllowed.contains(package_name)) {
                            checkAppStatus(package_name);
                        }

                    } else {

                        if (blacklist.contains(package_name)) {
//                            clearRecentApp(this, false);
                            return;
                        }
                        checkAppStatus(package_name);
                    }
                }


                if (WindowChangeDetectingService.serviceConnectedListener == null) {
                    WindowChangeDetectingService.serviceConnectedListener = this;
                }

                AccessibilityManager manager = (AccessibilityManager) this.getSystemService(Context.ACCESSIBILITY_SERVICE);
                if (manager.isEnabled()) {
                    AccessibilityEvent event = AccessibilityEvent.obtain();
                    event.setEventType(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
                    event.setPackageName(getPackageName());
                    event.setClassName(this.getClass().toString());
                    event.setAction(1452);
                    manager.sendAccessibilityEvent(event);
                }

                try {
                    Thread.sleep(600);
                    if (isAccessServiceEnabled(this, WindowChangeDetectingService.class)) {

//                        Timber.d("access service condition %s", isServiceConnected);

                        if (!isServiceConnected) {
                            if (!PrefUtils.getBooleanPref(MyApplication.getAppContext(), EMERGENCY_FLAG)) {
                                count++;
                                if (count >= 4) {
                                    clearRecentApp(this, true);
                                    count = 0;
                                }
                            }

                        } else {
//                            Timber.d("Service connected");
                            isServiceConnected = false;
//                            Timber.d("access service status in thread %s", isServiceConnected);
//                            clearRecentApp(this, true);
                        }
                    } else {
                        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
                        String component = BuildConfig.APPLICATION_ID + "/com.screenlocker.secure.permissions.SteppersActivity";
                        if (!component.equals(cn.flattenToShortString())) {
                            if (!permissionStatus) {
                                launchPermissions();
                                permissionStatus = true;
                                Timber.d("launching permissions");
                            }
                            Timber.d("launched permissions");

                        } else {
                            Timber.d("permission denied");
                        }
                    }


                } catch (InterruptedException e) {
                    Timber.e(e);
                }

            }
        });
    }


    private void checkAppStatus(String packageName) {


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
    }

    private Handler handler;

    private void launchPermissions() {
        Intent a = new Intent(this, SteppersActivity.class);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        a.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PrefUtils.saveBooleanPref(MyApplication.getAppContext(), PERMISSION_GRANTING, true);
        if (PrefUtils.getBooleanPref(this, TOUR_STATUS)) {
            a.putExtra("emergency", true);
        }
        startActivity(a);
    }

    private void clearRecentApp(Context context, boolean reboot) {

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        handler = new Handler(Looper.getMainLooper());

        if (reboot) {
            ActivityCompat.startForegroundService(this, new Intent(this, LockScreenService.class).setAction("addreboot"));
            handler.postDelayed(() -> ActivityCompat.startForegroundService(LockScreenService.this, new Intent(LockScreenService.this, LockScreenService.class).setAction("remove")), REBOOT_RESTRICTION_DELAY);
        } else {
            ActivityCompat.startForegroundService(this, new Intent(this, LockScreenService.class).setAction("add"));
            handler.postDelayed(() -> ActivityCompat.startForegroundService(LockScreenService.this, new Intent(LockScreenService.this, LockScreenService.class).setAction("remove")), RESTRICTION_DELAY);

        }

        Intent i = new Intent(context, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);


//        handler.postDelayed(this::removeView, 200);


//        handler.postDelayed(() -> ActivityCompat.startForegroundService(LockScreenService.this, new Intent(LockScreenService.this, LockScreenService.class).setAction("remove")), 2000);


    }


    private String current_package = "temp";
    boolean package_status = false;
    private String current_space = "temp";


    private boolean isAllowed(Context context, String packageName) {

        if (packageName.equals("android")) {
            return true;
        }

        if (packageName.equals(BuildConfig.APPLICATION_ID)) {
            return true;
        }
        String space = PrefUtils.getStringPref(context, CURRENT_KEY);
        String currentSpace = (space == null) ? "" : space;

        if (!currentSpace.equals(current_space) || !current_package.equals(packageName)) {
            current_space = currentSpace;
            current_package = packageName;
            Timber.d("<<< QUERYING DATA >>>");
            AppInfo info = MyApplication.getAppDatabase(context).getDao().getParticularApp(packageName);
            boolean status = false;
            if (info != null) {
                if (currentSpace.equals(KEY_MAIN_PASSWORD) && (info.isEnable() && info.isEncrypted())) {
                    status = true;
                } else if (currentSpace.equals(KEY_GUEST_PASSWORD) && (info.isEnable() && info.isGuest())) {
                    status = true;
                } else if (currentSpace.equals(KEY_SUPPORT_PASSWORD) && (packageName.equals(context.getPackageName()))) {
                    status = true;
                }
            }
            package_status = status;
            return status;

        } else {
            return package_status;
        }

    }


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

        if (PrefUtils.getBooleanPref(this, TOUR_STATUS)) {
            try {
//            setTimeRemaining(getAppContext());
                if (refresh)
                    refreshKeyboard();
                notificationItems.clear();
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
                    PrefUtils.saveStringPref(this, AppConstants.CURRENT_KEY, KEY_SUPPORT_PASSWORD);
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
//        if (!PrefUtils.getStringPref(this, CURRENT_KEY).equals(AppConstants.KEY_SUPPORT_PASSWORD)){
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
            fetch.cancel(Integer.parseInt(request_id));
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

    public String getCurrentApp() {
        String dum = null;


        try {
            String currentApp = null;
            UsageStatsManager usm = null;
            usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> applist = null;

            if (usm != null) {
                applist = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, time - 86400000, time);
            }
            if (applist != null && applist.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : applist) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (!mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
            return currentApp;
        } catch (Exception e) {
            Timber.d("getCurrentApp: %s", e.getMessage());

            return dum;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("screen locker starting.");

        if (WindowChangeDetectingService.serviceConnectedListener == null) {
            WindowChangeDetectingService.serviceConnectedListener = this;
        }


        if (intent != null) {
            String action = intent.getAction();
            String socketStatus = intent.getStringExtra(SOCKET_STATUS);

            if(socketStatus!=null){
                if(socketStatus.equals(START_SOCKET)){
                    startSocket();
                }else if(socketStatus.equals(STOP_SOCKET)){
                    stopSocket();
                }
            }

            Timber.d("locker screen action :%s", action);

            if (action == null) {
                String main_password = PrefUtils.getStringPref(this, KEY_MAIN_PASSWORD);
                if (main_password == null) {
                    PrefUtils.saveStringPref(this, KEY_MAIN_PASSWORD, DEFAULT_MAIN_PASS);
                }
                if(socketStatus==null)
                startLockScreen(false);
            } else {
                switch (action) {
                    case "suspended":
                        startLockScreen(true);
                        break;
                    case "expired":
                        startLockScreen(true);
                        break;
                    case "reboot":
                        startLockScreen(false);
                        break;
                    case "unlinked":
                        startLockScreen(true);
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
                    case "transfered":
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
                setDeviceId(this, warningText, PrefUtils.getStringPref(this, DEVICE_ID), null, utils.getDeviceStatus(this));
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) mLayout.getLayoutParams();
                windowManager.updateViewLayout(mLayout, params);
            }
        } catch (Exception e) {
            Timber.d(e);
        }
    }

    private void startSocket() {

        Timber.d("startSocket");
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

    @Override
    public void onSocketEventFailed() {
        Timber.d("Socket event failed");
        new ApiUtils(LockScreenService.this, DeviceIdUtils.generateUniqueDeviceId(this), DeviceIdUtils.getSerialNumber());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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
            getSimUpdates();
            sendSystemEvents();
            getSystemEvents();


            String installedApps = PrefUtils.getStringPref(this, INSTALLED_APPS);
            String uninstalledApps = PrefUtils.getStringPref(this, UNINSTALLED_APPS);

            if (installedApps != null) {
                saveAppsList(this, true, null, true);
            }
            if (uninstalledApps != null) {
                saveAppsList(this, false, null, true);
            }

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
                socketManager.getSocket().off(GET_SIM_UPDATES + device_id);
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

                            syncDevice(LockScreenService.this, is_synced, apps, extensions, settings);

                            if (!PrefUtils.getBooleanPref(LockScreenService.this, AppConstants.IS_SYNCED)) {

                                if (!PrefUtils.getBooleanPref(LockScreenService.this, AppConstants.APPS_SENT_STATUS)) {
                                    sendApps();
                                } else if (!PrefUtils.getBooleanPref(LockScreenService.this, AppConstants.EXTENSIONS_SENT_STATUS)) {
                                    sendExtensions();
                                } else if (!PrefUtils.getBooleanPref(LockScreenService.this, AppConstants.SETTINGS_SENT_STATUS)) {
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
    public void sendSimSettings() {

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

                            updateExtensions(obj, false);

                            updateApps(obj, false);

                            sendAppliedStatus();

                            updateSettings(obj, false);

                            setScreenLock();

                            Timber.d(" settings applied status sent ");

                        } else {
                            Timber.d(" no settings available in history ");

                            boolean appsSettingStatus = PrefUtils.getBooleanPref(LockScreenService.this, APPS_SETTING_CHANGE);
                            Timber.d(" apps settings status in local : %S", appsSettingStatus);

                            if (appsSettingStatus) {
                                sendAppsWithoutIcons();
                            }

                            boolean settingsStatus = PrefUtils.getBooleanPref(LockScreenService.this, SETTINGS_CHANGE);
                            Timber.d(" settings status in local : %S", settingsStatus);
                            if (settingsStatus) {
                                sendSettings();
                            }

                            boolean extensionsStatus = PrefUtils.getBooleanPref(LockScreenService.this, SECURE_SETTINGS_CHANGE);
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
        try {
            if (!settings.equals("[]")) {
                changeSettings(LockScreenService.this, settings);
                Timber.d(" settings applied ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (isPolicy) {
            finishPolicySettings();
        }

    }

    private void updatePassword(JSONObject obj) {
        String passwords = null;
        try {
            passwords = obj.getString("passwords");
            if (!passwords.equals("{}")) {
                updatePasswords(LockScreenService.this, new JSONObject(passwords));
                Timber.d(" passwords updated ");
                setScreenLock();
            }
        } catch (JSONException e) {
            Timber.e("Error while updating passwords : " + e);
        }

    }

    @Override
    public void sendApps() {
        Timber.d("<<< sending apps >>>");
        new Thread(() -> {
            try {
                if (socketManager.getSocket().connected()) {
                    List<AppInfo> apps = MyApplication.getAppDatabase(LockScreenService.this).getDao().getApps();
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

                    List<SubExtension> extensions = MyApplication.getAppDatabase(LockScreenService.this).getDao().getAllSubExtensions();

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
                    List<Settings> settings = MyApplication.getAppDatabase(LockScreenService.this).getDao().getSettings();
                    socketManager.getSocket().emit(SEND_SETTINGS + device_id, new Gson().toJson(settings));
                    PrefUtils.saveBooleanPref(LockScreenService.this, SETTINGS_CHANGE, false);
                });


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
                Timber.e("error: %S", e.getMessage());
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
                    pushPullPolicyApps(object, PUSH_APPS, "com.secure.systemcontrol.INSTALL_PACKAGES", "com.secure.systemcontrol.receivers.PackagesInstallReceiver", false);
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

    private void pushPullPolicyApps(JSONObject object, String type, String action, String componentName, boolean isPolicy) {
        try {
            if (validateRequest(device_id, object.getString("device_id"))) {
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


                            if (finalPushedApps != null && finalPushedApps.size() > 0) {

                                if (task != null) {
                                    task.cancel(true);
                                }


                                task = new DownloadPushedApps(downloadedApps -> {
                                    Timber.d("<<< Downloading Compelte>>>");
                                    if (downloadCompleteListener != null) {
                                        Timber.d("<<< CallBack to MainActivity>>>");
                                        downloadCompleteListener.onDownloadCompleted(downloadedApps);
                                    }


                                }, this, (ArrayList<InstallModel>) finalPushedApps);

                                task.execute();

                            } else {
                                if (isPolicy) {
                                    finishPolicyPushApps();
                                } else {
                                    finishPushedApps();
                                }
                            }

                        } else {
                            if (isPolicy) {
                                finishPolicyPushApps();
                            } else {
                                finishPushedApps();
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

                            finishPulledApps();

                        } else {
                            finishPulledApps();
                        }

                    }

                } else {
                    if (isPolicy) {
                        finishPolicyPushApps();
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

        ApplicationInfo info = null;
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
                    pushPullPolicyApps(object, PULL_APPS, "com.secure.systemcontrol.DELETE_PACKAGES", "com.secure.systemcontrol.receivers.PackageUninstallReceiver", false);
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
//                    setScreenLock();
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

                            pushPullPolicyApps(object, PUSH_APPS, "com.secure.systemcontrol.INSTALL_PACKAGES", "com.secure.systemcontrol.receivers.PackagesInstallReceiver", true);

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
                if (PrefUtils.getIntegerPref(this, AppConstants.PERVIOUS_VERSION) < getPackageManager().getPackageInfo(getPackageName(), 0).versionCode) {
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
            Timber.d(e);
        }

    }

    private void updateExtensions(JSONObject object, boolean isPolicy) throws JSONException {

        Timber.d("<<<Update Extensions>>>");
        String extensionList = object.getString("extension_list");

        if (!extensionList.equals("[]")) {

            JSONArray jsonArray = new JSONArray(extensionList);

            updateExtensionsList(LockScreenService.this, jsonArray, () -> {
                Timber.d(" extensions updated ");
            }, isPolicy);
        }

        if (isPolicy) {
            finishPolicyExtensions();
        }

    }

    private void updateApps(JSONObject object, boolean isPolicy) throws JSONException {

        Timber.d("<<<Update Apps>>>");

        String appsList = object.getString("app_list");

        if (!appsList.equals("[]")) {
            updateAppsList(LockScreenService.this, new JSONArray(appsList), () -> {
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


    public interface PolicyResponse {
        void onResponse(boolean status);
    }

    private PolicyResponse policyResponse;

    public void setListener(PolicyResponse policyResponse) {
        this.policyResponse = policyResponse;
    }
}
