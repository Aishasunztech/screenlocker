package com.screenlocker.secure.utils;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.socket.model.InstallModel;

import java.io.ByteArrayOutputStream;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static com.screenlocker.secure.utils.AppConstants.TIME_REMAINING;
import static com.screenlocker.secure.utils.AppConstants.TIME_REMAINING_REBOOT;
import static com.screenlocker.secure.utils.AppConstants.VALUE_EXPIRED;

public class CommonUtils {
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
        }
        return isAvailable;
    }

    public static String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();

//                Log.e("MACLENGTH", "getMacAddress: "+macBytes.length );
//                macBytes[0] = 0x0c;
//                for (byte b:macBytes) {
//                    Log.e("MACLENGTH", "getMacAddress: "+b );
//                }

                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
//                    res1.append( Integer.toHexString(b & 0xFF) + ":");

                    res1.append(
                            String.format(
                                    "%02X:",/*Integer.toHexString(*/
                                    b /*& 0xFF) + ":"*/
                            )
                    );
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
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
    public static long getTimeRemaining(Context context) {
        long current_time = new Date().getTime();
        long time_remaining = PrefUtils.getLongPref(context, TIME_REMAINING_REBOOT);
        if (time_remaining - current_time <= 0) {
            return 0;
        } else {
            return time_remaining - current_time;
        }
    }

    //set time remaining
    public static void setTimeRemaining(Context context) {
        long current_time = new Date().getTime();
        long remaining_time = PrefUtils.getLongPref(context, TIME_REMAINING);
        PrefUtils.saveLongPref(context, TIME_REMAINING_REBOOT, current_time + remaining_time);
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

    public static void setSecureSettingsMenu(Context context) {


        List<SubExtension> subExtensions = new ArrayList<>();

        // wifi menu
        Drawable wifi_drawable = context.getResources().getDrawable(R.drawable.ic_wifi);
        byte[] wifi_icon = CommonUtils.convertDrawableToByteArray(wifi_drawable);
        SubExtension wifi = new SubExtension();
        wifi.setLabel("wi-fi");
        wifi.setIcon(wifi_icon);
        wifi.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        wifi.setGuest(false);
        wifi.setEncrypted(false);
        wifi.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + "wi-fi");
        subExtensions.add(wifi);

        // bluetooth menu
        Drawable bluetooth_drawable = context.getResources().getDrawable(R.drawable.ic_bluetooth);
        byte[] bluetooth_icon = CommonUtils.convertDrawableToByteArray(bluetooth_drawable);
        SubExtension bluetooth = new SubExtension();
        bluetooth.setLabel("Bluetooth");
        bluetooth.setIcon(bluetooth_icon);
        bluetooth.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        bluetooth.setGuest(false);
        bluetooth.setEncrypted(false);
        bluetooth.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + "Bluetooth");
        subExtensions.add(bluetooth);

        // SIM CARDS
        Drawable sim_card_drawable = context.getResources().getDrawable(R.drawable.ic_sim_card);
        byte[] sim_card_icon = CommonUtils.convertDrawableToByteArray(sim_card_drawable);
        SubExtension sim_card = new SubExtension();
        sim_card.setLabel("SIM Cards");
        sim_card.setIcon(sim_card_icon);
        sim_card.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        sim_card.setGuest(false);
        sim_card.setEncrypted(false);
        sim_card.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + "SIM Cards");
        subExtensions.add(sim_card);

        //DATA ROAMING
        Drawable data_roaming_drawable = context.getResources().getDrawable(R.drawable.ic_roaming);
        byte[] data_roaming_icon = CommonUtils.convertDrawableToByteArray(data_roaming_drawable);
        SubExtension dataRoaming = new SubExtension();
        dataRoaming.setLabel("Data Roaming");
        dataRoaming.setIcon(data_roaming_icon);
        dataRoaming.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        dataRoaming.setGuest(false);
        dataRoaming.setEncrypted(false);
        dataRoaming.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + "Data Roaming");
        subExtensions.add(dataRoaming);

        // MOBILE DATA
        Drawable mobile_data_drawable = context.getResources().getDrawable(R.drawable.ic_mobile_data);
        byte[] mobile_data_icon = CommonUtils.convertDrawableToByteArray(mobile_data_drawable);
        SubExtension mobileData = new SubExtension();
        mobileData.setLabel("Mobile Data");
        mobileData.setIcon(mobile_data_icon);
        mobileData.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        mobileData.setGuest(false);
        mobileData.setEncrypted(false);
        mobileData.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + "Mobile Data");
        subExtensions.add(mobileData);


        // HOT SPOT
        Drawable hotspot_drawable = context.getResources().getDrawable(R.drawable.ic_wifi_hotspot);
        byte[] hotspot_icon = CommonUtils.convertDrawableToByteArray(hotspot_drawable);
        SubExtension hotspot = new SubExtension();
        hotspot.setLabel("Hotspot");
        hotspot.setIcon(hotspot_icon);
        hotspot.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        hotspot.setGuest(false);
        hotspot.setEncrypted(false);
        hotspot.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + "Hotspot");
        subExtensions.add(hotspot);

        // SCREEN LOCK
        Drawable screenLock_drawable = context.getResources().getDrawable(R.drawable.ic_screen_lock);
        byte[] screenLock_icon = CommonUtils.convertDrawableToByteArray(screenLock_drawable);
        SubExtension screenLock = new SubExtension();
        screenLock.setLabel("Finger Print + Lock");
        screenLock.setIcon(screenLock_icon);
        screenLock.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        screenLock.setGuest(false);
        screenLock.setEncrypted(false);
        screenLock.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + "Finger Print + Lock");
        subExtensions.add(screenLock);

        // HOT SPOT
        Drawable brightness_drawable = context.getResources().getDrawable(R.drawable.ic_settings_brightness);
        byte[] brightness_icon = CommonUtils.convertDrawableToByteArray(brightness_drawable);
        SubExtension brightness = new SubExtension();
        brightness.setLabel("Brightness");
        brightness.setIcon(brightness_icon);
        brightness.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        brightness.setGuest(false);
        brightness.setEncrypted(false);
        brightness.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + "Brightness");
        subExtensions.add(brightness);

        // SLEEP
        Drawable sleep_drawable = context.getResources().getDrawable(R.drawable.ic_sleep);
        byte[] sleep_icon = CommonUtils.convertDrawableToByteArray(sleep_drawable);
        SubExtension sleep = new SubExtension();
        sleep.setLabel("Sleep");
        sleep.setIcon(sleep_icon);
        sleep.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        sleep.setGuest(false);
        sleep.setEncrypted(false);
        sleep.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + "Sleep");
        subExtensions.add(sleep);

        // BATTERY
        Drawable battery_drawable = context.getResources().getDrawable(R.drawable.ic_battery);
        byte[] battery_icon = CommonUtils.convertDrawableToByteArray(battery_drawable);
        SubExtension battery = new SubExtension();
        battery.setLabel("Battery");
        battery.setIcon(battery_icon);
        battery.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        battery.setGuest(false);
        battery.setEncrypted(false);
        battery.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + "Battery");
        subExtensions.add(battery);

        // SOUND
        Drawable sound_drawable = context.getResources().getDrawable(R.drawable.ic_sound);
        byte[] sound_icon = CommonUtils.convertDrawableToByteArray(sound_drawable);
        SubExtension sound = new SubExtension();
        sound.setLabel("Sound");
        sound.setIcon(sound_icon);
        sound.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        sound.setGuest(false);
        sound.setEncrypted(false);
        sound.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + "Sound");
        subExtensions.add(sound);

        // DATE & TIME
        Drawable dateTime_drawable = context.getResources().getDrawable(R.drawable.ic_date_time);
        byte[] dateTime_icon = CommonUtils.convertDrawableToByteArray(dateTime_drawable);
        SubExtension dateTime = new SubExtension();
        dateTime.setLabel("Date & Time");
        dateTime.setIcon(dateTime_icon);
        dateTime.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        dateTime.setGuest(false);
        dateTime.setEncrypted(false);
        dateTime.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + "Date & Time");
        subExtensions.add(dateTime);

        // Air Plane Mode
        Drawable airplane_drawable = context.getResources().getDrawable(R.drawable.ic_airplane_mode);
        byte[] airplane_icon = CommonUtils.convertDrawableToByteArray(airplane_drawable);
        SubExtension airPlane = new SubExtension();
        airPlane.setLabel("Airplan mode");
        airPlane.setIcon(airplane_icon);
        airPlane.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        airPlane.setGuest(false);
        airPlane.setEncrypted(false);
        airPlane.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + "Airplan mode");
        subExtensions.add(airPlane);
        // Air Plane Mode
        Drawable language_drawable = context.getResources().getDrawable(R.drawable.ic_language);
        byte[] language_icon = CommonUtils.convertDrawableToByteArray(airplane_drawable);
        SubExtension language = new SubExtension();
        language.setLabel("Languages");
        language.setIcon(airplane_icon);
        language.setUniqueName(AppConstants.SECURE_SETTINGS_UNIQUE);
        language.setGuest(false);
        language.setEncrypted(false);
        language.setUniqueExtension(AppConstants.SECURE_SETTINGS_UNIQUE + "Languages");
        subExtensions.add(language);


        for (SubExtension subExtension : subExtensions) {
            MyApplication.getAppDatabase(context).getDao().insertSubExtensions(subExtension);
        }

    }

    // calculate expiry date

    public static String getRemainingDays(Context context) {

        String daysLeft = null;

        String value_expired = PrefUtils.getStringPref(context, VALUE_EXPIRED);


        if (value_expired != null) {
            long current_time = System.currentTimeMillis();
            long expired_time = Long.parseLong(value_expired);
            long remaining_miliseconds = expired_time - current_time;
            int remaining_days = (int) (remaining_miliseconds / (60 * 60 * 24 * 1000));

            if (remaining_days >= 0) {
                daysLeft = Integer.toString(remaining_days);
            } else {
                daysLeft = "expired";
            }
        }

        return daysLeft;
    }

    public static List<UsageStats> getCurrentApp(Context context) {

        try {
            UsageStatsManager usm =(UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
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
}