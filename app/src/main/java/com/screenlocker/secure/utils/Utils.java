package com.screenlocker.secure.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ServiceInfo;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.R;
import com.screenlocker.secure.notifications.NotificationItem;
import com.screenlocker.secure.offline.CheckExpiryFromSuperAdmin;
import com.screenlocker.secure.service.CheckUpdateService;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.socket.receiver.DeviceStatusReceiver;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.views.KeyboardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import timber.log.Timber;

import static android.content.Context.JOB_SCHEDULER_SERVICE;
import static androidx.core.app.NotificationCompat.GROUP_ALERT_SUMMARY;
import static com.screenlocker.secure.service.DeviceNotificationListener.TAG;
import static com.screenlocker.secure.socket.utils.utils.chatLogin;
import static com.screenlocker.secure.socket.utils.utils.getDeviceStatus;
import static com.screenlocker.secure.socket.utils.utils.getUserType;
import static com.screenlocker.secure.socket.utils.utils.loginAsEncrypted;
import static com.screenlocker.secure.socket.utils.utils.loginAsGuest;
import static com.screenlocker.secure.socket.utils.utils.registerDeviceStatusReceiver;
import static com.screenlocker.secure.socket.utils.utils.wipeDevice;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.LOCK_SCREEN_STATUS;
import static com.screenlocker.secure.utils.AppConstants.LOGIN_ATTEMPTS;
import static com.screenlocker.secure.utils.AppConstants.OFFLINE_DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.TIME_REMAINING;
import static com.screenlocker.secure.utils.AppConstants.TIME_REMAINING_REBOOT;
import static com.screenlocker.secure.utils.CommonUtils.getTimeRemaining;
import static com.screenlocker.secure.utils.CommonUtils.setTimeRemaining;

public class Utils {

    private static final long TIME_BACKGROUND_IMAGE_SHUFFLE = 30000L;
    private static final CharSequence NOTIFICATION_CHANNEL_NAME = "com.vortex.screen.locker";
    private static final String NOTIFICATION_CHANNEL_ID = "1475";
    private static final String NOTIFICATION_CHANNEL_DESC = "Screen locker notification channel.";


    /**
     * Get a notification for the running service.
     */
    public static Notification getNotification(Context context, int icon) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager mNM = (NotificationManager) context.getSystemService(
                    Context.NOTIFICATION_SERVICE);

            if (mNM != null) {
                NotificationChannel channel = mNM.getNotificationChannel(context.getString(R.string.app_name));
                if (channel == null) {
                    channel = new NotificationChannel(
                            context.getString(R.string.app_name),
                            context.getString(R.string.app_name),
                            NotificationManager.IMPORTANCE_DEFAULT
                    );
                    channel.setSound(null, null);
                    channel.setShowBadge(false);
                    mNM.createNotificationChannel(channel);

                }
            }
        }
        return new NotificationCompat.Builder(context, context.getString(R.string.app_name))
                .setOngoing(false)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.app_name))
                .setTicker(context.getString(R.string.app_name))
                .setSound(null)
                .setGroupAlertBehavior(GROUP_ALERT_SUMMARY)
                .setGroup("My group")
                .setGroupSummary(false)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                // .setPriority(Notification.PRIORITY_MIN)
                .setSmallIcon(icon)
                .build();
    }

    public static List<NotificationItem> getNotificationItems(StatusBarNotification[] notifications) {
        List<NotificationItem> notificationItems = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (StatusBarNotification notification : notifications) {
                if (!notification.getPackageName().equals(BuildConfig.APPLICATION_ID)) {
                    NotificationItem item = new NotificationItem();
                    item.id = notification.getId();
                    item.packageName = notification.getPackageName();
                    if (notification.getNotification() != null) {
                        item.icon = notification.getNotification().getLargeIcon();
                        item.title = notification.getNotification().extras.getString("android.title");
                        item.body = notification.getNotification().extras.getCharSequence("android.text");
                        item.extras = notification.getNotification().extras;
                        item.actions = notification.getNotification().actions;
                        item.category = notification.getNotification().category;
                        item.contentIntent = notification.getNotification().contentIntent;
                        item.deleteIntent = notification.getNotification().deleteIntent;
                        item.fullScreenIntent = notification.getNotification().fullScreenIntent;
                    }
                    notificationItems.add(item);
                }
            }
        }
        return notificationItems;
    }


    @SuppressLint("ResourceType")
    public static WindowManager.LayoutParams prepareLockScreenView(final RelativeLayout layout,
                                                                   List<NotificationItem> notifications, final Context context) {

        int windowType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            windowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            windowType = WindowManager.LayoutParams.TYPE_TOAST |
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        } else {
            windowType = WindowManager.LayoutParams.TYPE_PHONE;
        }

        DeviceStatusReceiver deviceStatusReceiver = new DeviceStatusReceiver();

        registerDeviceStatusReceiver(context, deviceStatusReceiver);


        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                windowType,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
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

        final LayoutInflater inflater = LayoutInflater.from(context);

        final View keypadView = inflater.inflate(R.layout.keypad_screen, layout);

        ConstraintLayout rootView = keypadView.findViewById(R.id.background);
        String bg = PrefUtils.getStringPref(context, AppConstants.KEY_LOCK_IMAGE);
        if (bg == null || bg.equals("")) {
            rootView.setBackgroundResource(R.raw.remountan);

        } else {
            rootView.setBackgroundResource(Integer.parseInt(bg));
        }

        final KeyboardView keyboardView = keypadView.findViewById(R.id.keypad);

        String device_id = PrefUtils.getStringPref(context, DEVICE_ID);

        if (device_id == null) {
            device_id = PrefUtils.getStringPref(context, OFFLINE_DEVICE_ID);
        }

        final String device_status = getDeviceStatus(context);

        if (device_status == null) {
            keyboardView.clearWaringText();
        }


        if (device_status != null) {
            switch (device_status) {
                case "suspended":
                    if (device_id != null) {
//                        keyboardView.setWarningText("Your account with Device ID = " + device_id + " is Suspended. Please contact support");
                        keyboardView.setWarningText(context.getResources().getString(R.string.account_device_id_suspended, device_id));

                    } else {
                        keyboardView.setWarningText(context.getResources().getString(R.string.account_device_id_suspended, "N/A"));

                    }
                    break;
                case "expired":
                    if (device_id != null) {
                        keyboardView.setWarningText(context.getResources().getString(R.string.account_device_id_expired, device_id));

                    } else {
//                        keyboardView.setWarningText("Your account with Device ID = N/A is Expired. Please contact support ");
                        keyboardView.setWarningText(context.getResources().getString(R.string.account_device_id_expired, "N/A"));


                    }
                    break;
            }
        }


        String finalDevice_id = device_id;
        deviceStatusReceiver.setListener(status -> {
            if (status == null) {
                keyboardView.clearWaringText();

            } else {
                if (status.equals("suspended")) {
                    if (finalDevice_id != null) {
//                        keyboardView.setWarningText("Your account with Device ID = " + finalDevice_id + " is Suspended. Please contact support");
                        keyboardView.setWarningText(context.getResources().getString(R.string.account_device_id_suspended, finalDevice_id));

                    } else {
                        keyboardView.setWarningText(context.getResources().getString(R.string.account_device_id_suspended, "N/A"));

                    }
                } else if (status.equals("expired")) {
                    if (finalDevice_id != null) {
                        keyboardView.setWarningText(context.getResources().getString(R.string.account_device_id_expired, finalDevice_id));

                    } else {
                        keyboardView.setWarningText(context.getResources().getString(R.string.account_device_id_expired, "N/A"));


                    }
                }
            }

        });

        Button unLockButton = keypadView.findViewById(R.id.ivUnlock);
        ImageView chatIcon = keyboardView.findViewById(R.id.chat_icon);
        TextView supportButton = keypadView.findViewById(R.id.t9_key_support);
        supportButton.setOnClickListener(v -> {
            chatLogin(context);
            keyboardView.setPassword(null);
        });
        long time_remaining = getTimeRemaining(context);


        int attempts = 10;
        int count = PrefUtils.getIntegerPref(context, LOGIN_ATTEMPTS);
        int x = attempts - count;

        if (time_remaining != 0) {

            if (count >= 5) {

                if (count > 9) {
                    wipeDevice(context);
                }

                switch (count) {
                    case 5:
                        remainingTime(context, keyboardView, unLockButton, time_remaining, count, x, AppConstants.attempt_5);
                        break;
                    case 6:
                        remainingTime(context, keyboardView, unLockButton, time_remaining, count, x, AppConstants.attempt_6);
                        break;
                    case 7:
                        remainingTime(context, keyboardView, unLockButton, time_remaining, count, x, AppConstants.attempt_7);
                        break;
                    case 8:
                        remainingTime(context, keyboardView, unLockButton, time_remaining, count, x, AppConstants.attempt_8);
                        break;
                    case 9:
                        remainingTime(context, keyboardView, unLockButton, time_remaining, count, x, AppConstants.attempt_9);
                        break;
                    case 10:
                        remainingTime(context, keyboardView, unLockButton, time_remaining, count, x, AppConstants.attempt_10);
                        break;
                }
            } else {
                PrefUtils.saveLongPref(context, TIME_REMAINING_REBOOT, 0);
                PrefUtils.saveLongPref(context, TIME_REMAINING, 0);
            }

        }


        String finalDevice_id1 = device_id;
        unLockButton.setOnClickListener(v -> {

            Timber.d("fvksdifgseifgueri");

            String enteredPin = keyboardView.getInputText().trim();
            String main_key = PrefUtils.getStringPref(context, AppConstants.KEY_MAIN_PASSWORD);
            String device_status1 = getDeviceStatus(context);
            if (enteredPin.length() != 0) {
                if (getUserType(enteredPin, context).equals("guest") && device_status1 == null) {
                    loginAsGuest(context);
                    keyboardView.setPassword(null);
                }
                //if input is for eyncrypted
                else if (getUserType(enteredPin, context).equals("encrypted") && device_status1 == null) {
                    loginAsEncrypted(context);
                    keyboardView.setPassword(null);
                    PrefUtils.saveIntegerPref(context, LOGIN_ATTEMPTS, 0);
                    boolean lock_screen = PrefUtils.getBooleanPref(context, LOCK_SCREEN_STATUS);
                    if (lock_screen) {
                        Intent intent = new Intent(context, LockScreenService.class);
                        context.stopService(intent);
                        PrefUtils.saveBooleanPref(context, LOCK_SCREEN_STATUS, false);
                    }
                } else if (getUserType(enteredPin, context).equals("duress") && device_status1 == null) {
                    wipeDevice(context);


                }
            /*else if (enteredPin.equals(AppConstants.SUPER_ADMIN_KEY)) {

// JUST a go through LOCK

                WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);

                if (windowManager != null) {
                    LockScreenService.removeLockScreenView(windowManager);
                //  handler.removeCallbacks(runnable);
                }

            }*/
                else if (device_status1 != null) {

                    switch (device_status1) {
                        case "suspended":
                            if (finalDevice_id1 != null) {
//                                keyboardView.setWarningText("Your account with Device ID = " + finalDevice_id1 + " is Suspended. Please contact support");
                                keyboardView.setWarningText(context.getResources().getString(R.string.account_device_id_suspended, finalDevice_id1));
                            } else {
//                                keyboardView.setWarningText("Your account with Device ID = N/A is Suspended. Please contact support");
                                keyboardView.setWarningText(context.getResources().getString(R.string.account_device_id_suspended, "N/A"));

                            }
                            break;
                        case "expired":
                            if (finalDevice_id1 != null) {
//                                keyboardView.setWarningText("Your account with Device ID = " + finalDevice_id1 + " is Expired. Please contact support ");
                                keyboardView.setWarningText(context.getResources().getString(R.string.account_device_id_expired, finalDevice_id1));

                            } else {
//                                keyboardView.setWarningText("Your account with Device ID = N/A is Expired. Please contact support ");
                                keyboardView.setWarningText(context.getResources().getString(R.string.account_device_id_expired, "N/A"));


                            }
                            break;
                    }
                } else {
//                    PrefUtils.saveIntegerPref(context, LOGIN_ATTEMPTS, 0);

                    int attempts1 = 10;
                    int count1 = PrefUtils.getIntegerPref(context, LOGIN_ATTEMPTS);
                    int x1 = attempts1 - count1;

                    if (count1 > 9) {
                        wipeDevice(context);
                    }

                    switch (count1) {

                        case 5:
                            CountDownTimer countDownTimer = timer(unLockButton, keyboardView, AppConstants.attempt_5, x1, context, count1);
                            if (countDownTimer != null)
                                countDownTimer.start();
                            break;
                        case 6:
                            countDownTimer = timer(unLockButton, keyboardView, AppConstants.attempt_6, x1, context, count1);
                            if (countDownTimer != null)
                                countDownTimer.start();
                            break;
                        case 7:
                            countDownTimer = timer(unLockButton, keyboardView, AppConstants.attempt_7, x1, context, count1);
                            if (countDownTimer != null)
                                countDownTimer.start();
                            break;
                        case 8:
                            countDownTimer = timer(unLockButton, keyboardView, AppConstants.attempt_8, x1, context, count1);
                            if (countDownTimer != null)
                                countDownTimer.start();
                            break;
                        case 9:
                            countDownTimer = timer(unLockButton, keyboardView, AppConstants.attempt_9, x1, context, count1);
                            if (countDownTimer != null)
                                countDownTimer.start();
                            break;
                        case 10:
                            countDownTimer = timer(unLockButton, keyboardView, AppConstants.attempt_10, x1, context, count1);
                            if (countDownTimer != null)
                                countDownTimer.start();
                            break;
                        default:
                            PrefUtils.saveIntegerPref(context, LOGIN_ATTEMPTS, count1 + 1);
                            unLockButton.setEnabled(true);
                            unLockButton.setClickable(true);
                            keyboardView.setPassword(null);
//                            String text_view_str = "Incorrect PIN ! <br><br> You have " + x + " attempts before device resets <br > and all data is lost ! ";
                            String text_view_str = context.getResources().getString(R.string.incorrect_pin) + " <br><br> " + context.getResources().getString(R.string.number_of_attempts_remaining, x1 + "");
                            keyboardView.setWarningText(String.valueOf(Html.fromHtml(text_view_str)));
                    }

                }

            }

        });


        return params;
    }

    private static void remainingTime(Context context, KeyboardView keyboardView, Button unLockButton, long time_remaining, int count, int x, int attempt_10) {
        long time;
        CountDownTimer countDownTimer;
        unLockButton.setEnabled(false);
        unLockButton.setClickable(false);
        time = (time_remaining > attempt_10) ? attempt_10 : time_remaining;
        PrefUtils.saveLongPref(context, TIME_REMAINING_REBOOT, 0);
        PrefUtils.saveLongPref(context, TIME_REMAINING, 0);
        countDownTimer = timer(unLockButton, keyboardView, time, x, context, count);
        if (countDownTimer != null)
            countDownTimer.start();
    }

    private static CountDownTimer timer(Button unLockButton, KeyboardView keyboardView, long timeRemaining, int x, Context context, int count) {

        CountDownTimer countDownTimer = null;
        try {

            unLockButton.setEnabled(false);
            unLockButton.setClickable(false);

            countDownTimer = new CountDownTimer(timeRemaining, 1000) {
                @Override
                public void onTick(long l) {
//                    String text_view_str = "Incorrect PIN! <br><br>You have " + x + " attempts before device resets <br>and all data is lost!<br><br>Next attempt in <b>" + String.format("%1$tM:%1$tS", l) + "</b>";
                    String text_view_str = context.getResources().getString(R.string.incorrect_pin) + "<br><br>" + context.getResources().getString(R.string.number_of_attempts_remaining, x + "") + "<br><br>" + context.getResources().getString(R.string.next_attempt_in) + " " + "<b>" + String.format("%1$tM:%1$tS", l) + "</b>";
                    keyboardView.setPassword(null);
                    keyboardView.setWarningText(String.valueOf(Html.fromHtml(text_view_str)));
                    PrefUtils.saveLongPref(context, TIME_REMAINING, l);
                    setTimeRemaining(context);
                }

                @Override
                public void onFinish() {
                    unLockButton.setEnabled(true);
                    unLockButton.setClickable(true);
                    keyboardView.setPassword(null);
                    keyboardView.clearWaringText();
                    PrefUtils.saveIntegerPref(context, LOGIN_ATTEMPTS, count + 1);
                    PrefUtils.saveLongPref(context, TIME_REMAINING, 0);
                    PrefUtils.saveLongPref(context, TIME_REMAINING_REBOOT, 0);
                }
            };
        } catch (Exception ignored) {

        }

        return countDownTimer;
    }


//    protected void onStop() {
//        super.onStop();
//        handler.removeCallbacks(runnable);
//    }

    public static void refreshKeypad(View view) {


        TextView k0 = view.findViewById(R.id.t9_key_0),
                k1 = view.findViewById(R.id.t9_key_1),
                k2 = view.findViewById(R.id.t9_key_2),
                k3 = view.findViewById(R.id.t9_key_3),
                k4 = view.findViewById(R.id.t9_key_4),
                k5 = view.findViewById(R.id.t9_key_5),
                k6 = view.findViewById(R.id.t9_key_6),
                k7 = view.findViewById(R.id.t9_key_7),
                k8 = view.findViewById(R.id.t9_key_8),
                k9 = view.findViewById(R.id.t9_key_9);

        int[] arr = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        setUpRandomizedArray(arr);

        k0.setText(String.valueOf(arr[0]));
        k1.setText(String.valueOf(arr[1]));
        k2.setText(String.valueOf(arr[2]));
        k3.setText(String.valueOf(arr[3]));
        k4.setText(String.valueOf(arr[4]));
        k5.setText(String.valueOf(arr[5]));
        k6.setText(String.valueOf(arr[6]));
        k7.setText(String.valueOf(arr[7]));
        k8.setText(String.valueOf(arr[8]));
        k9.setText(String.valueOf(arr[9]));


    }

    private static void setUpRandomizedArray(int[] arr) {

        for (int i = 0; i < 9; ++i) {

            Random r = new Random();
            int pos = i + r.nextInt(9 - i);

            int temp = arr[i];
            arr[i] = arr[pos];
            arr[pos] = temp;
        }
    }


    public static boolean isAccessServiceEnabled(Context mContext, Class accessibilityServiceClass) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + accessibilityServiceClass.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }

    public static void sendMessageToActivity(String msg, Context context) {

        Intent intent = new Intent(AppConstants.BROADCAST_ACTION);
// You can also include some extra data.
        intent.putExtra(AppConstants.BROADCAST_KEY, msg);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void startNfcSettingsActivity(Context context) {
        try {

            context.startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
        } catch (Exception e) {
            Toast.makeText(context, "Your phone has no NFC", Toast.LENGTH_SHORT).show();
        }
    }

    public static void copyToClipBoard(Context context, String label, String text, String toastText) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
    }

    public static void scheduleUpdateCheck(Context context) {

        ComponentName componentName = new ComponentName(context, CheckUpdateService.class);
        JobInfo info = new JobInfo.Builder(123, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(24 * 60 * 60 * 1000L)
                .build();
        JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        if (utils.isJobServiceOn(context, 123)) {
            scheduler.cancel(123);
        }
        int resultCode = scheduler.schedule(info);

        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            //Log.d(TAG, "Job scheduled");
        } else {
            //Log.d(TAG, "Job scheduling failed");
        }

    }

    public static void scheduleExpiryCheck(Context context) {

        ComponentName componentName = new ComponentName(context, CheckExpiryFromSuperAdmin.class);
        JobInfo info = new JobInfo.Builder(1345, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(24 * 60 * 60 * 1000L)
                .build();
        JobScheduler scheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
        if (utils.isJobServiceOn(context, 1345)) {
            scheduler.cancel(1345);
        }
        if (utils.isJobServiceOn(context, 1345)) {
            scheduler.cancel(1345);
        }
        int resultCode = scheduler.schedule(info);

        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            //Log.d(TAG, "Job scheduled");
        } else {
            //Log.d(TAG, "Job scheduling failed");
        }

    }


    private static int currentVolume = 0;
    private static boolean haSilence = false;

    /**
     * 扬声器免提
     *
     * @param context
     */
    public static void speaker(Context context) {
        Timber.d("speakerOn");
        AudioManager audioManager = getAudioManager(context);
        if (audioManager.isSpeakerphoneOn())
            return;
        audioManager.setSpeakerphoneOn(true);
        if (currentVolume == 0)
            currentVolume = audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        // currentVolume
        // =audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                currentVolume, AudioManager.STREAM_VOICE_CALL);
    }

    public static void micOff(Context context) {
        Timber.d("speakerOn");
        AudioManager audioManager = getAudioManager(context);
        if (audioManager.isMicrophoneMute())
            return;
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setMicrophoneMute(true);
    }

    /**
     * 静音
     *
     * @param context
     */
    public static void speakerOff(Context context) {
        Timber.d("speakerOff: ");
        AudioManager audioManager = getAudioManager(context);
        if (!audioManager.isSpeakerphoneOn())
            return;
        currentVolume = audioManager
                .getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        audioManager.setSpeakerphoneOn(false);
        audioManager.setMicrophoneMute(!audioManager.isSpeakerphoneOn());
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0,
                AudioManager.STREAM_VOICE_CALL);
    }

    public static void silence(Context context) {
        AudioManager audioManager = getAudioManager(context);
        if (haSilence)
            audioManager.setStreamMute(AudioManager.MODE_IN_CALL, false);
        else
            audioManager.setStreamMute(AudioManager.MODE_IN_CALL, true);
    }

    private static AudioManager getAudioManager(Context context) {
        AudioManager audioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        // audioManager.setMode(AudioManager.ROUTE_SPEAKER);
        return audioManager;
    }


}
