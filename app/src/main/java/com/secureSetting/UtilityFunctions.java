package com.secureSetting;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.screenlocker.secure.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.secureSetting.AppConstants.CODE_WRITE_SETTINGS_PERMISSION;
import static com.secureSetting.AppConstants.LOCATION_SETTINGS_CODE;


public class UtilityFunctions {
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectionManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiCheck = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiCheck.isConnected();
    }

    public static String getWifiSecurity(WifiManager wifiManager, String networkBSSID) {
        List<ScanResult> networkList = wifiManager.getScanResults();

//get current connected SSID for comparison to ScanResult
        WifiInfo wi = wifiManager.getConnectionInfo();
        String currentBSSID = wi.getBSSID();

        if (networkList != null) {
            for (ScanResult network : networkList) {
                //check if current connected SSID
                if (currentBSSID.contains(networkBSSID)) {
                    //get capabilities of current connection
                    String Capabilities = network.capabilities;

                    if (Capabilities.contains("WPA2") && Capabilities.contains("WPA")) {
                        return "WPA/WPA2";
                    } else if (Capabilities.contains("WPA")) {
                        return "WPA";
                    } else if (Capabilities.contains("WEP")) {
                        return "WEP";
                    } else if (Capabilities.contains("WPA2")) {
                        return "WPA2";
                    } else{
                        return "None";
                    }


                }
            }
        }
        return "None";
    }

    public static void clearConnectedPreference(Context context)
    {
        SharedPreferences connectedWifi = context.getSharedPreferences(context.getString(R.string.connectNetwork),Context.MODE_PRIVATE);
        connectedWifi.edit().clear().apply();
    }

    public static void clickForget(Context context,String SSID) {
        Toast.makeText(context, "Disconnecting...", Toast.LENGTH_LONG).show();

        WifiManager wifiManager  = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        SharedPreferences connectedNetworks = context.getSharedPreferences(context.getString(R.string.connectNetwork), Context.MODE_PRIVATE);
        int networkId = connectedNetworks.getInt("networkId",0);
        wifiManager.removeNetwork(networkId);
        wifiManager.saveConfiguration();
        wifiManager.disableNetwork(networkId);

        SharedPreferences removedNetworks = context.getSharedPreferences(context.getString(R.string.removedNetwork),Context.MODE_PRIVATE);
        removedNetworks.edit().putString("wifiName",SSID).apply();

        connectedNetworks.edit().clear().apply();


    }

    public static String returnSignalStrength(int strengthValue)
    {

        if (strengthValue <= 0 && strengthValue >= -50) {
            return "Excellent";

        } else if (strengthValue < -50 && strengthValue >= -70) {
            return "Good";


        } else if (strengthValue < -70 && strengthValue >= -80) {
            return "Weak";


        } else if (strengthValue < -80 && strengthValue >= -100) {
            return "Very Weak";

        } else {
            return "No Signal";
        }

    }

    public static boolean isWifiOpen(ScanResult scanResult)
    {
        String capabilities = scanResult.capabilities;
        if(capabilities.contains("WPA") || capabilities.contains("WEP") || capabilities.contains("WPA2"))
        {
            return false;
        }
        return true;
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
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
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
                        activity.startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_SETTINGS_CODE);
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
}
