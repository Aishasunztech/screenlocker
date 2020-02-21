package com.screenlocker.secure.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.service.NetworkSocketAlarm;
import com.screenlocker.secure.service.OfflineExpiryAlarm;
import com.screenlocker.secure.socket.SocketManager;
import com.screenlocker.secure.socket.model.Settings;
import com.secure.launcher.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static android.os.UserManager.DISALLOW_CONFIG_BLUETOOTH;
import static android.os.UserManager.DISALLOW_CONFIG_TETHERING;
import static android.os.UserManager.DISALLOW_CONFIG_WIFI;
import static android.os.UserManager.DISALLOW_UNMUTE_MICROPHONE;
import static com.screenlocker.secure.utils.AppConstants.CONNECTED;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_NETWORK_STATUS;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.SUB_AdminPanel;
import static com.screenlocker.secure.utils.AppConstants.SUB_Battery;
import static com.screenlocker.secure.utils.AppConstants.SUB_Bluetooth;
import static com.screenlocker.secure.utils.AppConstants.SUB_Brightness;
import static com.screenlocker.secure.utils.AppConstants.SUB_DataRoaming;
import static com.screenlocker.secure.utils.AppConstants.SUB_DateTime;
import static com.screenlocker.secure.utils.AppConstants.SUB_Finger;
import static com.screenlocker.secure.utils.AppConstants.SUB_Hotspot;
import static com.screenlocker.secure.utils.AppConstants.SUB_LanguagesInput;
import static com.screenlocker.secure.utils.AppConstants.SUB_MobileData;
import static com.screenlocker.secure.utils.AppConstants.SUB_Notifications;
import static com.screenlocker.secure.utils.AppConstants.SUB_SIM;
import static com.screenlocker.secure.utils.AppConstants.SUB_Sleep;
import static com.screenlocker.secure.utils.AppConstants.SUB_Sound;
import static com.screenlocker.secure.utils.AppConstants.SUB_WIFI;
import static com.screenlocker.secure.utils.AppConstants.TIME_REMAINING;
import static com.screenlocker.secure.utils.AppConstants.TIME_REMAINING_REBOOT;
import static com.screenlocker.secure.utils.AppConstants.VALUE_EXPIRED;

public class CommonUtils {

    public static boolean IsReachable(Context context, String host) {
        Timber.d("isReachAble");
        // First, check we have any sort of connectivity
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = connMgr.getActiveNetworkInfo();

        boolean isReachable = false;

        if (netInfo != null && netInfo.isConnected()) {
            // Some sort of connection is open, check if server is reachable
            try {
                URL url = new URL(host);
                //URL url = new URL("http://10.0.2.2");
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setRequestProperty("User-Agent", "Android Application");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(10 * 1000);

                try {
                    urlc.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                isReachable = (urlc.getResponseCode() == 200);

                Timber.d("Response code :%s", urlc.getResponseCode());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return isReachable;
    }


    public static String getDate(long timeStamp) {

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        } catch (Exception ex) {
            return "";
        }
    }

    public static byte[] convertDrawableToByteArray(Drawable d) {
        Bitmap bitmap;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // only for gingerbread and newer versions
            bitmap = getBitmapFromDrawable(d);
        } else {
            bitmap = ((BitmapDrawable) d).getBitmap();
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, stream);
        return stream.toByteArray();
    }

    @NonNull
    static private Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

    public static String splitName(String s) {
        return s.replace(".apk", "");

    }

    //get time remaining
    public static long getTimeRemaining(PrefUtils prefUtils) {
        long current_time = new Date().getTime();
        long time_remaining = prefUtils.getLongPref( TIME_REMAINING_REBOOT);
        if (time_remaining - current_time <= 0) {
            return 0;
        } else {
            return time_remaining - current_time;
        }
    }

    //set time remaining
    public static void setTimeRemaining( PrefUtils prefUtils) {
        long current_time = new Date().getTime();
        long remaining_time = prefUtils.getLongPref(TIME_REMAINING);
        prefUtils.saveLongPref(TIME_REMAINING_REBOOT, current_time + remaining_time);
    }

    public static void hideKeyboard(AppCompatActivity activity) {

        try {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } catch (Exception ignored) {
        }
    }


    // adding secure settings menus

    public static List<SubExtension> setSecureSettingsMenu(Context context) {


        List<SubExtension> subExtensions = new ArrayList<>();

        // wifi menu
        Drawable wifi_drawable = context.getResources().getDrawable(R.drawable.ic_wifi);
        byte[] wifi_icon = CommonUtils.convertDrawableToByteArray(wifi_drawable);
        SubExtension wifi = new SubExtension();
        wifi.setLabel(SUB_WIFI);
        wifi.setIcon(wifi_icon);
        wifi.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        wifi.setGuest(false);
        wifi.setSystemApp(true);
        wifi.setEncrypted(true);
        wifi.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + SUB_WIFI);
        subExtensions.add(wifi);


        // bluetooth menu
        Drawable bluetooth_drawable = context.getResources().getDrawable(R.drawable.ic_bluetooth);
        byte[] bluetooth_icon = CommonUtils.convertDrawableToByteArray(bluetooth_drawable);
        SubExtension bluetooth = new SubExtension();
        bluetooth.setLabel(SUB_Bluetooth);
        bluetooth.setIcon(bluetooth_icon);
        bluetooth.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        bluetooth.setGuest(false);
        bluetooth.setEncrypted(false);
        bluetooth.setSystemApp(true);
        bluetooth.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + SUB_Bluetooth);
        subExtensions.add(bluetooth);

        // SIM CARDS
        Drawable sim_card_drawable = context.getResources().getDrawable(R.drawable.ic_sim_card);
        byte[] sim_card_icon = CommonUtils.convertDrawableToByteArray(sim_card_drawable);
        SubExtension sim_card = new SubExtension();
        sim_card.setLabel(SUB_SIM);
        sim_card.setIcon(sim_card_icon);
        sim_card.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        sim_card.setGuest(false);
        sim_card.setEncrypted(true);
        sim_card.setSystemApp(true);
        sim_card.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + SUB_SIM);
        subExtensions.add(sim_card);

        //DATA ROAMING
        Drawable data_roaming_drawable = context.getResources().getDrawable(R.drawable.ic_roaming);
        byte[] data_roaming_icon = CommonUtils.convertDrawableToByteArray(data_roaming_drawable);
        SubExtension dataRoaming = new SubExtension();
        dataRoaming.setLabel(SUB_DataRoaming);
        dataRoaming.setIcon(data_roaming_icon);
        dataRoaming.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        dataRoaming.setGuest(false);
        dataRoaming.setEncrypted(true);
        dataRoaming.setSystemApp(true);
        dataRoaming.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + SUB_DataRoaming);
        subExtensions.add(dataRoaming);

        // MOBILE DATA
        Drawable mobile_data_drawable = context.getResources().getDrawable(R.drawable.ic_mobile_data);
        byte[] mobile_data_icon = CommonUtils.convertDrawableToByteArray(mobile_data_drawable);
        SubExtension mobileData = new SubExtension();
        mobileData.setLabel(SUB_MobileData);
        mobileData.setIcon(mobile_data_icon);
        mobileData.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        mobileData.setGuest(false);
        mobileData.setEncrypted(true);
        mobileData.setSystemApp(true);
        mobileData.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + SUB_MobileData);
        subExtensions.add(mobileData);


        // HOT SPOT
        Drawable hotspot_drawable = context.getResources().getDrawable(R.drawable.ic_wifi_hotspot);
        byte[] hotspot_icon = CommonUtils.convertDrawableToByteArray(hotspot_drawable);
        SubExtension hotspot = new SubExtension();
        hotspot.setLabel(SUB_Hotspot);
        hotspot.setIcon(hotspot_icon);
        hotspot.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        hotspot.setGuest(false);
        hotspot.setEncrypted(false);
        hotspot.setSystemApp(true);
        hotspot.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + SUB_Hotspot);
        subExtensions.add(hotspot);

        // SCREEN LOCK
        Drawable screenLock_drawable = context.getResources().getDrawable(R.drawable.ic_screen_lock);
        byte[] screenLock_icon = CommonUtils.convertDrawableToByteArray(screenLock_drawable);
        SubExtension screenLock = new SubExtension();
        screenLock.setLabel(SUB_Finger);
        screenLock.setIcon(screenLock_icon);
        screenLock.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        screenLock.setGuest(false);
        screenLock.setEncrypted(true);
        screenLock.setSystemApp(true);
        screenLock.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + SUB_Finger);
        subExtensions.add(screenLock);

        // HOT SPOT
        Drawable brightness_drawable = context.getResources().getDrawable(R.drawable.ic_settings_brightness);
        byte[] brightness_icon = CommonUtils.convertDrawableToByteArray(brightness_drawable);
        SubExtension brightness = new SubExtension();
        brightness.setLabel(SUB_Brightness);
        brightness.setIcon(brightness_icon);
        brightness.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        brightness.setGuest(false);
        brightness.setEncrypted(true);
        brightness.setSystemApp(true);
        brightness.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + SUB_Brightness);
        subExtensions.add(brightness);

        // SLEEP
        Drawable sleep_drawable = context.getResources().getDrawable(R.drawable.ic_sleep);
        byte[] sleep_icon = CommonUtils.convertDrawableToByteArray(sleep_drawable);
        SubExtension sleep = new SubExtension();
        sleep.setLabel(SUB_Sleep);
        sleep.setIcon(sleep_icon);
        sleep.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        sleep.setGuest(false);
        sleep.setEncrypted(true);
        sleep.setSystemApp(true);
        sleep.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + SUB_Sleep);
        subExtensions.add(sleep);

        // BATTERY
        Drawable battery_drawable = context.getResources().getDrawable(R.drawable.ic_battery);
        byte[] battery_icon = CommonUtils.convertDrawableToByteArray(battery_drawable);
        SubExtension battery = new SubExtension();
        battery.setLabel(SUB_Battery);
        battery.setIcon(battery_icon);
        battery.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        battery.setGuest(false);
        battery.setEncrypted(true);
        battery.setSystemApp(true);
        battery.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + SUB_Battery);
        subExtensions.add(battery);

        // SOUND
        Drawable sound_drawable = context.getResources().getDrawable(R.drawable.ic_sound);
        byte[] sound_icon = CommonUtils.convertDrawableToByteArray(sound_drawable);
        SubExtension sound = new SubExtension();
        sound.setLabel(SUB_Sound);
        sound.setIcon(sound_icon);
        sound.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        sound.setGuest(false);
        sound.setEncrypted(true);
        sound.setSystemApp(true);
        sound.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + SUB_Sound);
        subExtensions.add(sound);

        // DATE & TIME
        Drawable dateTime_drawable = context.getResources().getDrawable(R.drawable.ic_date_time);
        byte[] dateTime_icon = CommonUtils.convertDrawableToByteArray(dateTime_drawable);
        SubExtension dateTime = new SubExtension();
        dateTime.setLabel(SUB_DateTime);
        dateTime.setIcon(dateTime_icon);
        dateTime.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        dateTime.setGuest(false);
        dateTime.setEncrypted(true);
        dateTime.setSystemApp(true);
        dateTime.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + SUB_DateTime);
        subExtensions.add(dateTime);


        // Languages and input

        Drawable language_drawable = context.getResources().getDrawable(R.drawable.ic_language);
        byte[] language_icon = CommonUtils.convertDrawableToByteArray(language_drawable);
        SubExtension language = new SubExtension();
        language.setLabel(SUB_LanguagesInput);
        language.setIcon(language_icon);
        language.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        language.setGuest(false);
        language.setEncrypted(true);
        language.setSystemApp(true);
        language.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + SUB_LanguagesInput);
        subExtensions.add(language);


        // Notifications

        Drawable notificaitons_drawable = context.getResources().getDrawable(R.drawable.ic_notifications);
        byte[] notificaitons_icon = CommonUtils.convertDrawableToByteArray(notificaitons_drawable);
        SubExtension notificaitons = new SubExtension();
        notificaitons.setLabel(SUB_Notifications);
        notificaitons.setIcon(notificaitons_icon);
        notificaitons.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        notificaitons.setGuest(false);
        notificaitons.setEncrypted(true);
        notificaitons.setSystemApp(true);
        notificaitons.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + SUB_Notifications);
        subExtensions.add(notificaitons);

        Drawable admin_panel_drawable = context.getResources().getDrawable(R.drawable.ic_lock_screen_s);
        byte[] admin_panel_icon = CommonUtils.convertDrawableToByteArray(admin_panel_drawable);
        SubExtension adminPanel = new SubExtension();
        adminPanel.setLabel(SUB_AdminPanel);
        adminPanel.setIcon(admin_panel_icon);
        adminPanel.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        adminPanel.setGuest(false);
        adminPanel.setEncrypted(true);
        adminPanel.setSystemApp(true);
        adminPanel.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + SUB_AdminPanel);
        subExtensions.add(adminPanel);


        return subExtensions;

    }

    // calculate expiry date

    public static String getRemainingDays(Context context, PrefUtils prefUtils) {

        String daysLeft = null;

        String value_expired = prefUtils.getStringPref(VALUE_EXPIRED);


        if (value_expired != null) {
            long current_time = System.currentTimeMillis();
            long expired_time = Long.parseLong(value_expired);
            long remaining_miliseconds = expired_time - current_time;
            int remaining_days = (int) (remaining_miliseconds / (60 * 60 * 24 * 1000));

            if (remaining_days >= 0) {
                daysLeft = Integer.toString(remaining_days);
            } else {
                daysLeft = context.getResources().getString(R.string.expired);
            }
        }

        return daysLeft;
    }

    public static List<UsageStats> getCurrentApp(Context context) {

        try {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();

            if (usm != null) {
                return usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, time - 86400000, time);
            }


            return null;
        } catch (Exception e) {
            Log.d("getCurrentApp: %s", e.getMessage());

            return null;
        }
    }


    public static boolean isSocketConnected() {
        return (SocketManager.getInstance().getSocket() != null && SocketManager.getInstance().getSocket().connected());
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();

    }

    public static boolean isNetworkConneted(PrefUtils prefUtils) {
        String state = prefUtils.getStringPref( CURRENT_NETWORK_STATUS);
        return state != null && state.equals(CONNECTED);
    }

    public static void setAppLocale(String locale, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(new Locale(locale.toLowerCase()));
        resources.updateConfiguration(configuration, displayMetrics);
    }

    /**
     * @param alarmType 0 for offline expiry 1 for socket and network connection checker
     */
    public static void setAlarmManager(Context context, long timeInMillis, int alarmType) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = null;

        if (alarmType == 0) {
            intent = new Intent(context, OfflineExpiryAlarm.class);
        } else if (alarmType == 1) {
            intent = new Intent(context, NetworkSocketAlarm.class);
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0);

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
    }


    public static List<Settings> getDefaultSetting(Context context) {
        List<Settings> settings = new ArrayList<>();
        DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName compName = new ComponentName(context, MyAdmin.class);
        boolean isfileSharing = false;
        if (mDPM.isDeviceOwnerApp(context.getPackageName())) {
            mDPM.setBluetoothContactSharingDisabled(compName, true);
            mDPM.addUserRestriction(compName, DISALLOW_CONFIG_TETHERING);
            mDPM.clearUserRestriction(compName, DISALLOW_UNMUTE_MICROPHONE);
            mDPM.clearUserRestriction(compName, DISALLOW_CONFIG_WIFI);
            mDPM.clearUserRestriction(compName, DISALLOW_CONFIG_BLUETOOTH);
            mDPM.setMasterVolumeMuted(compName, false);
            mDPM.setCameraDisabled(compName, false);
            mDPM.setScreenCaptureDisabled(compName, true);

        } else {
            isfileSharing = true;
        }
        PrefUtils prefUtils = PrefUtils.getInstance(context);
        prefUtils.saveBooleanPref( AppConstants.KEY_DISABLE_CALLS, false);
        prefUtils.saveBooleanPref( AppConstants.KEY_DISABLE_SCREENSHOT, true);

        settings.add(new Settings(AppConstants.SET_WIFI, true));
        settings.add(new Settings(AppConstants.SET_BLUETOOTH, true));
//        settings.add(new Settings(AppConstants.SET_BLUE_FILE_SHARING, isfileSharing));
//        settings.add(new Settings(AppConstants.SET_HOTSPOT, true));
        settings.add(new Settings(AppConstants.SET_SS, isfileSharing));
        settings.add(new Settings(AppConstants.SET_CALLS, false));
        settings.add(new Settings(AppConstants.SET_CAM, true));
//        settings.add(new Settings(AppConstants.SET_MIC, true));
//        settings.add(new Settings(AppConstants.SET_SPEAKER, true));

        return settings;
    }

    public static String getTimeString(long l) {
        int seconds = (int) (l / 1000);
        int minutes = (int) Math.floor(seconds / 60);
        seconds = (int) Math.floor(seconds % 60);
        String minuteString = "" + minutes;
        String secondString = "" + seconds;

        if (minutes < 10) {
            minuteString = "0" + minuteString;
        }
        if (seconds < 10) {
            secondString = "0" + secondString;
        }

        return minuteString + ":" + secondString;
    }

    public static String currentSpace(PrefUtils prefUtils) {
        String space = prefUtils.getStringPref( CURRENT_KEY);
        if (KEY_MAIN_PASSWORD.equals(space)) {
            return "encrypted";
        }
        return "guest";
    }


}