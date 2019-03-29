package com.vortexlocker.app.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vortexlocker.app.BuildConfig;
import com.vortexlocker.app.R;
import com.vortexlocker.app.notifications.NotificationItem;
import com.vortexlocker.app.service.LockScreenService;
import com.vortexlocker.app.socket.interfaces.DeviceStatus;
import com.vortexlocker.app.socket.receiver.DeviceStatusReceiver;
import com.vortexlocker.app.views.KeyboardView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.vortexlocker.app.socket.utils.utils.getDeviceStatus;
import static com.vortexlocker.app.socket.utils.utils.getUserType;
import static com.vortexlocker.app.socket.utils.utils.loginAsEncrypted;
import static com.vortexlocker.app.socket.utils.utils.loginAsGuest;
import static com.vortexlocker.app.socket.utils.utils.registerDeviceStatusReceiver;
import static com.vortexlocker.app.socket.utils.utils.wipeDevice;
import static com.vortexlocker.app.utils.AppConstants.LOCK_SCREEN_STATUS;
import static com.vortexlocker.app.utils.AppConstants.LOGIN_ATTEMPTS;

public class Utils {

    private static final long TIME_BACKGROUND_IMAGE_SHUFFLE = 30000L;

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
                            NotificationManager.IMPORTANCE_LOW
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


    public static WindowManager.LayoutParams prepareLockScreenView(final RelativeLayout layout, List<NotificationItem> notifications, final Context context) {


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
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        params.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        params.gravity = Gravity.CENTER;


//        ((MdmMainActivity)context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final LayoutInflater inflater = LayoutInflater.from(context);
        final View keypadView = inflater.inflate(R.layout.keypad_screen, layout);
        final KeyboardView keyboardView = keypadView.findViewById(R.id.keypad);

        final String device_id = PrefUtils.getStringPref(context, AppConstants.DEVICE_ID);

        final String device_status = getDeviceStatus(context);

        if (device_status == null) {
            keyboardView.clearWaringText();
        }
        if (device_status != null) {
            switch (device_status) {
                case "suspended":
                    if (device_id != null) {
                        keyboardView.setWarningText("Please contact Admin. \nAccount: SUSPENDED ", device_id);
                    } else {
                        keyboardView.setWarningText("Please contact Admin. \nAccount: SUSPENDED ", "N/A");
                    }
                    break;
                case "expired":
                    if (device_id != null) {
                        keyboardView.setWarningText("Please contact Admin. \nAccount: EXPIRED ", device_id);
                    } else {
                        keyboardView.setWarningText("Please contact Admin. \nAccount: EXPIRED ", "N/A");
                    }
                    break;
            }
        }


        deviceStatusReceiver.setListener(status -> {
            if (status == null) {
                keyboardView.clearWaringText();
//                    loginAsGuest(context);
            } else {
                if (status.equals("suspended")) {
                    if (device_id != null) {
                        keyboardView.setWarningText("Please contact Admin. \nAccount: SUSPENDED ", device_id);
                    } else {
                        keyboardView.setWarningText("Please contact Admin. \nAccount: SUSPENDED ", "N/A");
                    }
                } else if (status.equals("expired")) {
                    if (device_id != null) {
                        keyboardView.setWarningText("Please contact Admin. \nAccount: EXPIRED ", device_id);
                    } else {
                        keyboardView.setWarningText("Please contact Admin. \nAccount: EXPIRED ", "not found");
                    }
                }
            }

        });
//        ConstraintLayout backgroundLayout = keypadView.findViewById(R.id.background);
//        backgroundLayout.setBackground(BitmapDrawable.createFromPath(PrefUtils.saveStringPref(this, AppConstants.KEY_MAIN_IMAGE)));

        //   final ImageView backgroundView = keypadView.findViewById(R.id.background);
//        final Random random = new Random();
//        final Handler handler = new Handler();
//        final int[] imageList = new int[]{
//                R.drawable.ic_image_2,
//                R.drawable.ic_image_1,
//                R.drawable.ic_image_3
//        };
//
//
//        final Runnable runnable = new Runnable() {
//            public void run() {
//                int randomNum = random.nextInt(3);
//                backgroundView.setImageDrawable(ContextCompat.getDrawable(context, imageList[randomNum]));
//                handler.postDelayed(this, TIME_BACKGROUND_IMAGE_SHUFFLE);
//            }
//        };
//        backgroundView.setImageDrawable(ContextCompat.getDrawable(context, imageList[random.nextInt(3)]));
//        handler.postDelayed(runnable, TIME_BACKGROUND_IMAGE_SHUFFLE);
        //backgroundView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        //  backgroundView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.lock_background));
        Button unLockButton = keypadView.findViewById(R.id.ivUnlock);
        unLockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Timber.d("on unlock: click ");
                String enteredPin = keyboardView.getInputText().trim();
                String main_key = PrefUtils.getStringPref(context, AppConstants.KEY_MAIN_PASSWORD);
                Timber.d("enteredPin:%s", enteredPin);
                Timber.d("mainkey:%s", main_key);
                String device_status = getDeviceStatus(context);
                Timber.d("device status %s", device_status);

                if (enteredPin.length() != 0) {


                    if (getUserType(enteredPin, context).equals("guest") && device_status == null) {
                        loginAsGuest(context);
                        PrefUtils.saveIntegerPref(context, LOGIN_ATTEMPTS, 0);
                    }
                    //if input is for eyncrypted
                    else if (getUserType(enteredPin, context).equals("encrypted") && device_status == null) {
                        loginAsEncrypted(context);
                        PrefUtils.saveIntegerPref(context, LOGIN_ATTEMPTS, 0);
                        boolean lock_screen = PrefUtils.getBooleanPref(context, LOCK_SCREEN_STATUS);
                        if (lock_screen) {
                            Intent intent = new Intent(context, LockScreenService.class);
                            context.stopService(intent);
                            PrefUtils.saveBooleanPref(context, LOCK_SCREEN_STATUS, false);
                        }
                    } else if (getUserType(enteredPin, context).equals("duress")) {
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
                    else if (device_status != null) {

                        switch (device_status) {
                            case "suspended":
                                if (device_id != null) {
                                    keyboardView.setWarningText("Please contact Admin. \nAccount: SUSPENDED ", device_id);
                                } else {
                                    keyboardView.setWarningText("Please contact Admin. \nAccount: SUSPENDED ", "N/A");
                                }
                                break;
                            case "expired":
                                if (device_id != null) {
                                    keyboardView.setWarningText("Please contact Admin. \nAccount: EXPIRED ", device_id);
                                } else {
                                    keyboardView.setWarningText("Please contact Admin. \nAccount: EXPIRED ", "N/A");
                                }
                                break;
                        }
                    } else {

                        int attempts = 10;
                        int count = PrefUtils.getIntegerPref(context, LOGIN_ATTEMPTS);
                        PrefUtils.saveIntegerPref(context, LOGIN_ATTEMPTS, count + 1);

                        int x = attempts - count;

                        if (count < 10) {
                            keyboardView.setPassword(null);
                            keyboardView.setWarningText("After " + x + " more attempts,\n this device will reset automatically!", null);
                        } else {
                            wipeDevice(context);
                        }

                    }

                }

            }
        });


        return params;
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


    public static void collapseNow(Context context) {

        try {

            // Initialize 'collapseNotificationHandler'
            Handler collapseNotificationHandler = new Handler();

            // Post a Runnable with some delay - currently set to 300 ms
            collapseNotificationHandler.postDelayed(new Runnable() {

                @Override
                public void run() {

                    // Use reflection to trigger a method from 'StatusBarManager'
                    @SuppressLint("WrongConstant") Object statusBarService = context.getSystemService("statusbar");
                    Class<?> statusBarManager = null;

                    try {
                        statusBarManager = Class.forName("android.app.StatusBarManager");
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    Method collapseStatusBar = null;
                    try {
                        // Prior to API 17, the method to call is 'collapse()'
                        // API 17 onwards, the method to call is `collapsePanels()`
                        if (Build.VERSION.SDK_INT > 16) {
                            collapseStatusBar = statusBarManager.getMethod("collapsePanels");
                        } else {
                            collapseStatusBar = statusBarManager.getMethod("collapse");
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                    collapseStatusBar.setAccessible(true);

                    try {
                        collapseStatusBar.invoke(statusBarService);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    // Currently, the delay is 10 ms. You can change this
                    // value to suit your needs.
                    collapseNotificationHandler.postDelayed(this, 10L);
                }
            }, 10L);
        } catch (Exception e) {
            Log.d("helsssslo", "collapseNow: " + e.getMessage());
        }
    }


}
