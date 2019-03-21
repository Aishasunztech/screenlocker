package com.vortexlocker.app.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import com.vortexlocker.app.BuildConfig;

import java.util.Set;

import timber.log.Timber;

import static com.vortexlocker.app.utils.AppConstants.CODE_WRITE_SETTINGS_PERMISSION;
import static com.vortexlocker.app.utils.AppConstants.REQUEST_READ_PHONE_STATE;
import static com.vortexlocker.app.utils.AppConstants.RESULT_ENABLE;

public class PermissionUtils {


    public static void requestOverlayPermission(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (!Settings.canDrawOverlays(activity)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName()));
//            intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);



            activity.startActivityForResult(intent, 1);
        }
    }


    public static boolean canDrawOver(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;

        }

        return Settings.canDrawOverlays(activity);
    }

    public static boolean canControlNotification(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return true;
        }

        Set<String> notificationListenerSet = NotificationManagerCompat
                .getEnabledListenerPackages(activity);

        return notificationListenerSet.contains(activity.getPackageName());
    }

    public static void requestNotificationAccessibilityPermission(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }

        Set<String> notificationListenerSet = NotificationManagerCompat
                .getEnabledListenerPackages(activity);

        if (!notificationListenerSet.contains(activity.getPackageName())) {
            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            } else {
                intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivityForResult(intent, 2);
            }
        }
    }


    public static void permissionModify(Activity activity) {
        boolean permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(activity);
        } else {
            permission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        }
        if (!permission) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, CODE_WRITE_SETTINGS_PERMISSION);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_SETTINGS}, CODE_WRITE_SETTINGS_PERMISSION);
            }
        }




    }

    public static void permissionAdmin(Activity activity, DevicePolicyManager devicePolicyManager, ComponentName compName) {

        boolean adminActive = devicePolicyManager.isAdminActive(compName);

        if (!adminActive) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why we need this permission");
            activity.startActivityForResult(intent, RESULT_ENABLE);
        }
    }

    public static boolean isPermissionGranted(Activity activity) {

        if (Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(android.Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED && activity.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                Timber.v("Permission is granted");
                return true;
            } else {
                Timber.v("Permission is revoked");
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_READ_PHONE_STATE);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Timber.v("Permission is granted");
            return true;
        }
    }

    public static boolean isAccessGranted(Context context) {
        try {
            if(Build.VERSION.SDK_INT >=19) {


                PackageManager packageManager = context.getPackageManager();
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
                AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                int mode = 0;
                if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
                    mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                            applicationInfo.uid, applicationInfo.packageName);
                }
                return (mode == AppOpsManager.MODE_ALLOWED);
            }
            else{
                return true;
            }

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void requestUsageStatePermission(Context context)
    {
        if(!isAccessGranted(context))
        {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            context.startActivity(intent);
        }
    }
}
