package com.screenlocker.secure.utils;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.R;
import com.screenlocker.secure.notifications.NotificationItem;
import com.screenlocker.secure.offline.CheckExpiryFromSuperAdmin;
import com.screenlocker.secure.service.CheckUpdateService;
import com.screenlocker.secure.socket.receiver.AppsStatusReceiver;
import com.screenlocker.secure.socket.utils.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.content.Context.JOB_SCHEDULER_SERVICE;
import static androidx.core.app.NotificationCompat.GROUP_ALERT_SUMMARY;

public class Utils {



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





    public static void silentPullApp(Context context, String packageName, String label) {
        PackageManager packageManger = context.getPackageManager();
        PackageInstaller packageInstaller = packageManger.getPackageInstaller();
        Intent delIntent = new Intent(context, AppsStatusReceiver.class);
        delIntent.setComponent(new ComponentName(context.getPackageName(), "com.screenlocker.secure.socket.receiver.AppsStatusReceiver"));
        delIntent.setAction("com.secure.systemcontroll.PackageDeleted");
        delIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        //package name of deleted package
        delIntent.putExtra("package", packageName);
        delIntent.putExtra("label",label );
        Random generator = new Random();
        PendingIntent i = PendingIntent.getBroadcast(context, generator.nextInt(), delIntent, 0);
        packageInstaller.uninstall(packageName, i.getIntentSender());
    }

    public static boolean installSielentInstall(Context context, InputStream in, String packageName)
            throws IOException {

        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();

        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(packageName);

        // set params
        int sessionId = packageInstaller.createSession(params);
        PackageInstaller.Session session = packageInstaller.openSession(sessionId);
        OutputStream out = session.openWrite("COSU", 0, -1);
        byte[] buffer = new byte[65536];
        int c;
        while ((c = in.read(buffer)) != -1) {
            out.write(buffer, 0, c);
        }
        session.fsync(out);
        in.close();
        out.close();

        session.commit(createIntentSender(context, sessionId, packageName));
        return true;

    }

    private static IntentSender createIntentSender(Context context, int sessionId, String packageName) {


        Intent intent = new Intent(context, AppsStatusReceiver.class);
        intent.setComponent(new ComponentName(context.getPackageName(), "com.screenlocker.secure.socket.receiver.AppsStatusReceiver"));
        intent.setAction("com.secure.systemcontrol.PACKAGE_ADDED_SECURE_MARKET");
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.putExtra("packageName", packageName);
        intent.putExtra("userSpace", PrefUtils.getStringPref(context, AppConstants.CURRENT_KEY));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                intent,
                0);

        return pendingIntent.getIntentSender();
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

}
