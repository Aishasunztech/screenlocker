package com.screenlocker.secure.utils;

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
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.service.notification.StatusBarNotification;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
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
import static android.view.View.INVISIBLE;
import static androidx.core.app.NotificationCompat.GROUP_ALERT_SUMMARY;
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
