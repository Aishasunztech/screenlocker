package com.screenlocker.secure.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.CountDownTimer;
import android.service.notification.StatusBarNotification;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.R;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.notifications.NotificationItem;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.settings.SetUpLockActivity;
import com.screenlocker.secure.settings.SettingContract;
import com.screenlocker.secure.settings.SettingsModel;
import com.screenlocker.secure.settings.SettingsPresenter;
import com.screenlocker.secure.socket.receiver.DeviceStatusReceiver;
import com.screenlocker.secure.views.KeyboardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static com.screenlocker.secure.settings.ManagePasswords.REQUEST_CODE_PASSWORD;
import static com.screenlocker.secure.socket.utils.utils.getDeviceStatus;
import static com.screenlocker.secure.socket.utils.utils.getUserType;
import static com.screenlocker.secure.socket.utils.utils.loginAsEncrypted;
import static com.screenlocker.secure.socket.utils.utils.loginAsGuest;
import static com.screenlocker.secure.socket.utils.utils.registerDeviceStatusReceiver;
import static com.screenlocker.secure.socket.utils.utils.wipeDevice;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.LOCK_SCREEN_STATUS;
import static com.screenlocker.secure.utils.AppConstants.LOGIN_ATTEMPTS;
import static com.screenlocker.secure.utils.AppConstants.TIME_REMAINING;
import static com.screenlocker.secure.utils.AppConstants.VALUE_EXPIRED;
import static com.screenlocker.secure.utils.CommonUtils.getRemainingDays;
import static com.screenlocker.secure.utils.CommonUtils.getTimeRemaining;

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
                    mNM.createNotificationChannel(channel);

                }
            }
        }

        return new NotificationCompat.Builder(context, context.getString(R.string.app_name))
                .setOngoing(true)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.app_name))
                .setTicker(context.getString(R.string.app_name))
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

        final LayoutInflater inflater = LayoutInflater.from(context);

        final View keypadView = inflater.inflate(R.layout.keypad_screen, layout);

        ConstraintLayout rootView = keypadView.findViewById(R.id.background);
        String bg = PrefUtils.getStringPref(context, AppConstants.KEY_LOCK_IMAGE);
        if (bg == null || bg.equals("")) {
            rootView.setBackgroundResource(R.drawable.background_icon);

        } else {
            rootView.setBackgroundResource(Integer.parseInt(bg));
        }

        final KeyboardView keyboardView = keypadView.findViewById(R.id.keypad);

        final String device_id = PrefUtils.getStringPref(context, DEVICE_ID);

        final String device_status = getDeviceStatus(context);

        if (device_status == null) {
            keyboardView.clearWaringText();
        }


        if (device_status != null) {
            switch (device_status) {
                case "suspended":
                    if (device_id != null) {
                        keyboardView.setWarningText("Your account with Device ID = " + device_id + " is Suspended. Please contact support", device_id);
                    } else {
                        keyboardView.setWarningText("Your account with Device ID = N/A is suspended.Please contact support ", "N/A");
                    }
                    break;
                case "expired":
                    if (device_id != null) {
                        keyboardView.setWarningText("Your account with Device ID = " + device_id + " is Expired. Please contact support ", device_id);
                    } else {
                        keyboardView.setWarningText("Your account with Device ID = N/A is Expired. Please contact support ", "N/A");

                    }
                    break;
            }
        }


        deviceStatusReceiver.setListener(status -> {
            if (status == null) {
                keyboardView.clearWaringText();

            } else {
                if (status.equals("suspended")) {
                    if (device_id != null) {
                        keyboardView.setWarningText("Your account with Device ID = " + device_id + " is Suspended. Please contact support", device_id);
                    } else {
                        keyboardView.setWarningText("Your account with Device ID = N/A is suspended.Please contact support ", "N/A");
                    }
                } else if (status.equals("expired")) {
                    if (device_id != null) {
                        keyboardView.setWarningText("Your account with Device ID = " + device_id + " is Expired. Please contact support ", device_id);
                    } else {
                        keyboardView.setWarningText("Your account with Device ID = N/A is Expired. Please contact support ", "N/A");

                    }
                }
            }

        });

        Button unLockButton = keypadView.findViewById(R.id.ivUnlock);
        long time_remaining = getTimeRemaining(context);


        if (time_remaining != 0) {
            unLockButton.setEnabled(false);
            unLockButton.setClickable(false);
            int attempts = 10;
            int count = PrefUtils.getIntegerPref(context, LOGIN_ATTEMPTS);
            int x = attempts - count;
            CountDownTimer countDownTimer = timer(unLockButton, keyboardView, time_remaining, x, context, count);
            if (countDownTimer != null)
                countDownTimer.start();
        }


        unLockButton.setOnClickListener(v -> {

            String enteredPin = keyboardView.getInputText().trim();
            String main_key = PrefUtils.getStringPref(context, AppConstants.KEY_MAIN_PASSWORD);
            Timber.d("enteredPin:%s", enteredPin);
            Timber.d("mainkey:%s", main_key);
            String device_status1 = getDeviceStatus(context);
            Timber.d("device status %s", device_status1);

            if (enteredPin.length() != 0) {
                if (getUserType(enteredPin, context).equals("guest") && device_status1 == null) {
                    loginAsGuest(context);
                }
                //if input is for eyncrypted
                else if (getUserType(enteredPin, context).equals("encrypted") && device_status1 == null) {
                    loginAsEncrypted(context);
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
                // TODO handle the super key for unlocking the dialer screen ( uncomment it to make super key run)
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
                            if (device_id != null) {
                                keyboardView.setWarningText("Your account with Device ID = " + device_id + " is Suspended. Please contact support", device_id);
                            } else {
                                keyboardView.setWarningText("Your account with Device ID = N/A is suspended.Please contact support ", "N/A");
                            }
                            break;
                        case "expired":
                            if (device_id != null) {
                                keyboardView.setWarningText("Your account with Device ID = " + device_id + " is Expired. Please contact support ", device_id);
                            } else {
                                keyboardView.setWarningText("Your account with Device ID = N/A is Expired. Please contact support ", "N/A");

                            }
                            break;
                    }
                } else {
//                    PrefUtils.saveIntegerPref(context, LOGIN_ATTEMPTS, 0);
                    int attempts = 10;
                    int count = PrefUtils.getIntegerPref(context, LOGIN_ATTEMPTS);
                    int x = attempts - count;

                    if (count > 9) {
                        wipeDevice(context);
                    }

                    switch (count) {

                        case 5:
                            CountDownTimer countDownTimer = timer(unLockButton, keyboardView, 1000 * 60 * AppConstants.attempt_5, x, context, count);
                            if (countDownTimer != null)
                                countDownTimer.start();
                            break;
                        case 6:
                            countDownTimer = timer(unLockButton, keyboardView, 1000 * 60 * AppConstants.attempt_6, x, context, count);
                            if (countDownTimer != null)
                                countDownTimer.start();
                            break;
                        case 7:
                            countDownTimer = timer(unLockButton, keyboardView, 1000 * 60 * AppConstants.attempt_7, x, context, count);
                            if (countDownTimer != null)
                                countDownTimer.start();
                            break;
                        case 8:
                            countDownTimer = timer(unLockButton, keyboardView, 1000 * 60 * AppConstants.attempt_8, x, context, count);
                            if (countDownTimer != null)
                                countDownTimer.start();
                            break;
                        case 9:
                            countDownTimer = timer(unLockButton, keyboardView, 1000 * 60 * AppConstants.attempt_9, x, context, count);
                            if (countDownTimer != null)
                                countDownTimer.start();
                            break;
                        case 10:
                            countDownTimer = timer(unLockButton, keyboardView, 1000 * 60 * AppConstants.attempt_10, x, context, count);
                            if (countDownTimer != null)
                                countDownTimer.start();
                            break;
                        default:
                            PrefUtils.saveIntegerPref(context, LOGIN_ATTEMPTS, count + 1);
                            unLockButton.setEnabled(true);
                            unLockButton.setClickable(true);
                            keyboardView.setPassword(null);
                            String text_view_str = "Incorrect PIN ! <br><br> You have " + x + " attempts before device resets <br > and all data is lost ! ";
                            keyboardView.setWarningText(String.valueOf(Html.fromHtml(text_view_str)), null);
                    }

                }

            }

        });


        return params;
    }

    private static CountDownTimer timer(Button unLockButton, KeyboardView keyboardView, long timeRemaining, int x, Context context, int count) {

        CountDownTimer countDownTimer = null;
        try {

            unLockButton.setEnabled(false);
            unLockButton.setClickable(false);
            countDownTimer = new CountDownTimer(timeRemaining, 1000) {
                @Override
                public void onTick(long l) {
                    String text_view_str = "Incorrect PIN! <br><br>You have " + x + " attempts before device resets <br>and all data is lost!<br><br>Next attempt in <b>" + String.format("%1$tM:%1$tS", l) + "</b>";
                    keyboardView.setPassword(null);
                    keyboardView.setWarningText(String.valueOf(Html.fromHtml(text_view_str)), null);
                    PrefUtils.saveLongPref(context, TIME_REMAINING, l);
                }

                @Override
                public void onFinish() {
                    unLockButton.setEnabled(true);
                    unLockButton.setClickable(true);
                    keyboardView.setPassword(null);
                    keyboardView.clearWaringText();
                    PrefUtils.saveIntegerPref(context, LOGIN_ATTEMPTS, count + 1);
                    PrefUtils.saveLongPref(context, TIME_REMAINING, 0);
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





    public static String twoDatesBetweenTime(String oldtime) {
        // TODO Auto-generated method stub
        int day = 0;
        int hh = 0;
        int mm = 0;
        try
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date oldDate = dateFormat.parse(oldtime);
            Date cDate = new Date();
            Long timeDiff = cDate.getTime() - oldDate.getTime();
            day = (int) TimeUnit.MILLISECONDS.toDays(timeDiff);
            hh = (int) (TimeUnit.MILLISECONDS.toHours(timeDiff) - TimeUnit.DAYS.toHours(day));
            mm = (int) (TimeUnit.MILLISECONDS.toMinutes(timeDiff) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeDiff)));
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        if(day==0)
        {
            return hh + " hour " + mm + " min";
        }
        else if(hh==0)
        {
            return mm + " min";
        }
        else
        {
            return day + " days " + hh + " hour " + mm + " min";
        }
    }


    public static String getDate(long milliSeconds) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

}
