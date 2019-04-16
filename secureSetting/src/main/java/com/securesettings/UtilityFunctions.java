package com.securesettings;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;

import java.util.concurrent.TimeUnit;


import pub.devrel.easypermissions.EasyPermissions;

import static android.content.Context.WIFI_SERVICE;
import static com.securesettings.AppConstants.CODE_WRITE_SETTINGS_PERMISSION;
import static com.securesettings.AppConstants.LOCATION_SETTINGS_CODE;
import static com.securesettings.AppConstants.RC_PERMISSION;

public class UtilityFunctions {
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectionManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiCheck = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiCheck.isConnected();
    }


    public static boolean permissionModify(Activity activity)
    {
        boolean permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permission = Settings.System.canWrite(activity);
        } else {
            permission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED;
        }
        if (!permission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, CODE_WRITE_SETTINGS_PERMISSION);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_SETTINGS}, CODE_WRITE_SETTINGS_PERMISSION);
            }
        }
        return permission;
    }

    public static boolean checkLocationStatus(Context context)
    {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if( !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            return false;
        }
        return true;
    }

    public static void turnOnLocation(final Activity activity)
    {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.gps_not_found_title)  // GPS not found
                .setMessage(R.string.gps_not_found_message) // Want to enable?
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        activity.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_SETTINGS_CODE);
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(R.string.no, null).show();
    }

    public static int getScreenBrightness(Context context){
        int brightnessValue = Settings.System.getInt(
                context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,
                0
        );
        return brightnessValue;
    }

    public static void setScreenBrightness(Context context,int brightnessValue){

        if(brightnessValue >= 0 && brightnessValue <= 255){
            Settings.System.putInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightnessValue
            );
        }
    }

    public static double pxFromDp(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static int getSleepTime(Context context)
    {
        try {
            int time = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT);
                time = (int) TimeUnit.MILLISECONDS.toSeconds(time);

            return time;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String secondsToMintues(int seconds)
    {
        int minutes = seconds/60;
        if(minutes!= 0)
        {
            return minutes + " minutes";
        }
        return seconds + " seconds";
    }

    public static String getWifiStatus(Context context)
    {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);

        if(isWifiConnected(context))
        {

            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String SSID = wifiInfo.getSSID().substring(1,wifiInfo.getSSID().length()-1);
            if(SSID.contains("unknown"))
            {
                  return "Non Connected";
            }
            else{

                return SSID;

            }


        } else {
            int wifiState = wifiManager.getWifiState();
            if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                return "Non Connected";
            } else {
                return "Disabled";
            }
        }
    }

    public static String getBlueToothStatus()
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter.isEnabled())
        {
            return "Enabled";
        }else{
            return "Disabled";

        }
    }

    public static void checkPermissions(Activity activity) {
        String[] prems = {Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                };
        if (EasyPermissions.hasPermissions(activity,prems)){

        }else{
            EasyPermissions.requestPermissions(activity,activity.getString(R.string.fine_location_and_cross_location),
                    RC_PERMISSION, prems);
        }
    }



}
