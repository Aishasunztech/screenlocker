package com.screenlocker.secure.service;

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
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.notifications.NotificationItem;
import com.screenlocker.secure.room.SimEntry;
import com.screenlocker.secure.settings.SettingsActivity;
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

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Future;

import timber.log.Timber;

import static com.screenlocker.secure.app.MyApplication.getAppContext;
import static com.screenlocker.secure.utils.AppConstants.ALLOW_ENCRYPTED_ALL;
import static com.screenlocker.secure.utils.AppConstants.ALLOW_GUEST_ALL;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.DEFAULT_MAIN_PASS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.EMERGENCY_FLAG;
import static com.screenlocker.secure.utils.AppConstants.KEY_DEF_BRIGHTNESS;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_LOCK_IMAGE;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_SUPPORT_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.SIM_0_ICCID;
import static com.screenlocker.secure.utils.AppConstants.SIM_1_ICCID;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
import static com.screenlocker.secure.utils.CommonUtils.setAlarmManager;
import static com.screenlocker.secure.utils.PrefUtils.PREF_FILE;
import static com.screenlocker.secure.utils.Utils.refreshKeypad;
import static com.screenlocker.secure.utils.Utils.scheduleExpiryCheck;
import static com.screenlocker.secure.utils.Utils.scheduleUpdateCheck;
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

            downloadListener.downloadComplete(filePath, packageName);


        }

        @Override
        public void onError(@NotNull Download download, @NotNull Error error, @org.jetbrains.annotations.Nullable Throwable throwable) {
            Toast.makeText(LockScreenService.this, "Downloading error", Toast.LENGTH_SHORT).show();
            File file = new File(filePath);
            file.delete();


        }

        @Override
        public void onDownloadBlockUpdated(@NotNull Download download, @NotNull DownloadBlock downloadBlock, int i) {

        }

        @Override
        public void onStarted(@NotNull Download download, java.util.@NotNull List<? extends DownloadBlock> list, int i) {

            downloadId = download.getId();
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onProgress(@NotNull Download download, long l, long l1) {

            if (downloadListener != null) {
                downloadListener.showProgressDialog(download.getProgress());


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
            File file = new File(filePath);
            file.delete();

            Toast.makeText(LockScreenService.this, "Download cancelled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRemoved(@NotNull Download download) {

        }

        @Override
        public void onDeleted(@NotNull Download download) {
            File file = new File(filePath);
            file.delete();
        }
    };
    private DownloadServiceCallBacks downloadListener;
    private String url = "";
    private String filePath = "";
    private String packageName = "";


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

        setAlarmManager(this, System.currentTimeMillis() + 15000);

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


        sharedPref = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
        myKM = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        PackageManager packageManager = getPackageManager();

        FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(this)
                .setDownloadConcurrentLimit(3)
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
        Notification notification = Utils.getNotification(this, R.drawable.ic_lock_black_24dp);


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
            } else {
                //removeView();
            }
        }
    };

    PowerManager powerManager;

    AppExecutor appExecutor;

//    private void sheduleScreenOffMonitor() {
//        if (appExecutor.getExecutorForSedulingRecentAppKill().isShutdown()) {
//            appExecutor.readyNewExecutor();
//        }
//        appExecutor.getExecutorForSedulingRecentAppKill().execute(() -> {
//            while (!Thread.currentThread().isInterrupted()) {
//                if (!powerManager.isInteractive()) {
//                    appExecutor.getMainThread().execute(() -> startLockScreen(true));
//                    return;
//                } else {
//                    if (myKM.inKeyguardRestrictedInputMode()) {
//                        //it is locked
//                        appExecutor.getMainThread().execute(() -> startLockScreen(true));
//                        return;
//                    }
//                }
//            }
//        });
//    }

    public void startDownload(String url, String filePath, String packageName) {
        this.url = url;
        this.filePath = filePath;
        this.packageName = packageName;
        Request request = new Request(url, filePath);
        request.setPriority(Priority.HIGH);
        request.setNetworkType(NetworkType.ALL);
        request.addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG");

        fetch.enqueue(request, updatedRequest -> {
            Toast.makeText(getAppContext(), "Download Pending", Toast.LENGTH_LONG).show();

            //Request was successfully enqueued for download.
        }, error -> {
            Toast.makeText(getAppContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            //An error occurred enqueuing the request.
        });
    }


    public void cancelDownload() {
        if (downloadId != 0)
            fetch.cancel(downloadId);
    }

    public interface DownloadServiceCallBacks {
        void showProgressDialog(int progress);

        void downloadComplete(String filePath, String packagename);

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

    private boolean running = false;

    private void startRecentAppsKillThread() {

        if (appExecutor.getExecutorForSedulingRecentAppKill().isShutdown()) {
            appExecutor.readyNewExecutor();
        }

        appExecutor.getExecutorForSedulingRecentAppKill().execute(() -> {

            while (!Thread.currentThread().isInterrupted()) {

                if (!powerManager.isInteractive()) {
                    appExecutor.getMainThread().execute(() -> startLockScreen(true));
                    running = false;
                    return;
                }
                running = true;

                String package_name = getCurrentApp();

                Timber.d("current Package %s", package_name);

                if (!PrefUtils.getBooleanPref(this, EMERGENCY_FLAG)) {

                    if (AppConstants.TEMP_SETTINGS_ALLOWED) {
                        Timber.d("Settings are temporary on");
                        if (!tempAllowed.contains(package_name)) {
                            checkAppStatus(package_name);
                        }

                    } else {
                        if (blacklist.contains(package_name)) {
                            clearRecentApp(this);
                        }
                        checkAppStatus(package_name);
                    }
                }

                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    Timber.e(e);
                }

            }
        });
    }


    private void checkAppStatus(String packageName) {


        Timber.d("package in checkApps status %s ", packageName);

        if (packageName.equals(BuildConfig.APPLICATION_ID)) {
            return;
        }

        Future<Boolean> futureObject = AppExecutor.getInstance().getSingleThreadExecutor()
                .submit(() -> isAllowed(LockScreenService.this, packageName));
        try {
            boolean status = futureObject.get();
            if (!status) {
                clearRecentApp(this);
            }
        } catch (Exception e) {
            clearRecentApp(this);
        }
    }

    private Handler handler;

    private void clearRecentApp(Context context) {

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        handler = new Handler(Looper.getMainLooper());

        ActivityCompat.startForegroundService(this, new Intent(this, LockScreenService.class).setAction("add"));

        Intent i = new Intent(context, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(i);


        handler.postDelayed(() -> ActivityCompat.startForegroundService(LockScreenService.this, new Intent(LockScreenService.this, LockScreenService.class).setAction("remove")), 2000);


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
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            windowType = WindowManager.LayoutParams.TYPE_TOAST |
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        } else {
            windowType = WindowManager.LayoutParams.TYPE_PHONE;
        }


        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                windowType,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                        | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_SECURE
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,

                PixelFormat.TRANSLUCENT);

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
                //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                PrefUtils.saveStringPref(this, AppConstants.CURRENT_KEY, KEY_SUPPORT_PASSWORD);
            }


        } catch (Exception e) {
            Timber.e(e);
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


    public void refreshKeyboard() {
        try {
            if (mLayout != null) {
                PatternLockView pl = mLayout.findViewById(R.id.patternLock);
                pl.setUpRandomizedArray();
                pl.invalidate();
                TextView clear = mLayout.findViewById(R.id.t9_key_clear);
                clear.setText(getResources().getString(R.string.btn_backspace));
                EditText pin = mLayout.findViewById(R.id.password_field);
                pin.setText(null);
                pin.setHint(getResources().getString(R.string.enter_pin_or_draw_pattern_to_unlock));
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
        }
    };


    //    protected void addView(int colorId) {
//        mView.setBackgroundColor(getResources().getColor(colorId));
//        windowManager.addView(mView, localLayoutParams);
//    }
//
//    protected void removeView() {
//        Timber.d("removeView: ");
//        if (mView != null && mView.getWindowToken() != null) {
//            if (windowManager != null) {
//                windowManager.removeViewImmediate(mView);
//            }
//        }
//    }
    protected void addView(Context context) {
        Timber.d("addView: ");
        try {
//            mView.setBackground(drawable);
            if (!isLocked && mView.getWindowToken() == null && !viewAdded) {

                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.action_restricted_layout, null);

//                mView.getWindow().getDecorView().setSystemUiVisibility(
//                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


                mView.addView(view);


//                imageView = new ImageView(this);
//                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(250, 250);
//                imageView.setLayoutParams(params);
//                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
//
//                Glide.with(this).load(R.mipmap.ic_launcher).into(imageView);
//
//                textView = new TextView(this);
//                textView.setGravity(Gravity.CENTER_HORIZONTAL);
//                textView.setText("Action not allowed !");
//                textView.setTextSize(18f);
//                textView.setTextColor(getResources().getColor(R.color.white));
//
//
//                mView.addView(imageView);
//                mView.addView(textView);

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

}
