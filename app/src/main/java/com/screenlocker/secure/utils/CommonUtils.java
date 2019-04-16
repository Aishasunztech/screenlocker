package com.screenlocker.secure.utils;

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


        String uniqueName = "com.secureSetting.SettingsMainActivity";

        List<SubExtension> subExtensions = new ArrayList<>();

        // wifi menu
        Drawable wifi_drawable = context.getResources().getDrawable(R.drawable.wifi_icons);
        byte[] wifi_icon = CommonUtils.convertDrawableToByteArray(wifi_drawable);
        SubExtension wifi = new SubExtension();
        wifi.setLabel("wi-fi");
        wifi.setIcon(wifi_icon);
        wifi.setUniqueName(uniqueName);
        wifi.setGuest(false);
        wifi.setEncrypted(false);
        subExtensions.add(wifi);

        for (SubExtension subExtension : subExtensions) {
            Log.d("igjioejigtete", "setSecureSettingsMenu: "+subExtension.getUniqueName());
            MyApplication.getAppDatabase(context).getDao().insertSubExtensions(subExtension);
        }

        List<SubExtension> subExtension = MyApplication.getAppDatabase(context).getDao().getSubExtensions(AppConstants.SECURE_SETTINGS_UNIQUE);

        Log.d("igjioejigtete", "setSecureSettingsMenu: " + subExtension.size());
    }


}
