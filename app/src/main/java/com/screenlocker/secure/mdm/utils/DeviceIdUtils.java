package com.screenlocker.secure.mdm.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import androidx.core.app.ActivityCompat;

import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.screenlocker.secure.utils.PrefUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.DFAULT_MAC;

public class DeviceIdUtils {

    /**
     * This method returns WLAN0's MAC Address
     *
     * @return WLAN0 MAC address or NULL if unable to get the mac address
     */
    @Nullable
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
                    return "02:00:00:00:00:00";
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
        } catch (Exception ignored) {
        }
        return "02:00:00:00:00:00";
    }


    /**
     * This method returns the device's WLAN MAC Address. We expect any device using the P2P sync module to
     * either have a bluetooth or Wifi module. We are going to assume that they atleast have  Wifi module
     *
     * @return
     */
    @Nullable
    @WorkerThread
    public static final String generateUniqueDeviceId(Context context) {
        String uniqueId = null;
        if (context != null) {

            uniqueId = getMacAddress();

            PrefUtils prefUtils = PrefUtils.getInstance(context);
            assert uniqueId != null;
            if (uniqueId.equals("02:00:00:00:00:00")) {

                uniqueId = prefUtils.getStringPref( DFAULT_MAC);

                if (uniqueId == null) {

                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    boolean wasWifiEnabled = wifiManager.isWifiEnabled();

                    if (!wifiManager.isWifiEnabled()) {
                        // ENABLE THE WIFI FIRST
                        wifiManager.setWifiEnabled(true);
                        try {
                            // Let's give the device some-time to start the wifi
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Timber.e(e);
                        }
                    }

                    uniqueId = getMacAddress();

                    if (!wasWifiEnabled) {
                        wifiManager.setWifiEnabled(false);
                    }


                }


            } else {
                prefUtils.saveStringPref(DFAULT_MAC, uniqueId);
            }

        }

        return uniqueId;
    }


    public static String getSerialNumber() {
        String serialNumber;

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);

            // (?) Lenovo Tab (https://stackoverflow.com/a/34819027/1276306)
            serialNumber = (String) get.invoke(c, "gsm.sn1");

            if (serialNumber.equals(""))
                // Samsung Galaxy S5 (SM-G900F) : 6.0.1
                // Samsung Galaxy S6 (SM-G920F) : 7.0
                // Samsung Galaxy Tab 4 (SM-T530) : 5.0.2
                // (?) Samsung Galaxy Tab 2 (https://gist.github.com/jgold6/f46b1c049a1ee94fdb52)
                serialNumber = (String) get.invoke(c, "ril.serialnumber");

            if (serialNumber.equals(""))
                // Archos 133 Oxygen : 6.0.1
                // Google Nexus 5 : 6.0.1
                // Hannspree HANNSPAD 13.3" TITAN 2 (HSG1351) : 5.1.1
                // Honor 5C (NEM-L51) : 7.0
                // Honor 5X (KIW-L21) : 6.0.1
                // Huawei M2 (M2-801w) : 5.1.1
                // (?) HTC Nexus One : 2.3.4 (https://gist.github.com/tetsu-koba/992373)
                serialNumber = (String) get.invoke(c, "ro.serialno");

            if (serialNumber.equals(""))
                // (?) Samsung Galaxy Tab 3 (https://stackoverflow.com/a/27274950/1276306)
                serialNumber = (String) get.invoke(c, "sys.serialnumber");

            if (serialNumber.equals(""))
                // Archos 133 Oxygen : 6.0.1
                // Honor 9 Lite (LLD-L31) : 8.0
                serialNumber = Build.SERIAL;

            // If none of the methods above worked
            if (serialNumber.equals(""))
                serialNumber = "123456789ABCDEF";
        } catch (Exception e) {
            e.printStackTrace();
            serialNumber = "123456789ABCDEF";
        }

        return serialNumber;
    }

    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        } // for now eat exceptions
        return "";
    }

    @SuppressLint("HardwareIds")
    public static List<String> getIMEI(Context context) {

        List<String> imies = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (tm != null) {
                    int sims = tm.getPhoneCount();
                    for (int i = 0; i < sims; i++) {
                        imies.add(tm.getDeviceId(i));
                        Log.d("xmdkmdkb", "getIMEI: " + tm.getDeviceId(i));
                    }
                }
            }
            if (tm != null && imies.size() < 1) {
                imies.add(tm.getDeviceId());
                Log.d("xmdkmdkb", "getIMEI: " + tm.getDeviceId());
            }
        }
        return imies;

    }

    @SuppressLint("HardwareIds")
    public static List<String> getSimNumber(Context context) {
        List<String> simNumbers = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                SubscriptionManager sManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

                List<SubscriptionInfo> infos;
                if (sManager != null) {
                    infos = sManager.getActiveSubscriptionInfoList();
                    if (infos != null) {
                        for (SubscriptionInfo info : infos) {
                            String ssn = info.getIccId();
                            if (ssn != null) {
                                simNumbers.add(ssn);
                            }
                        }
                    }

                }


            }
            if (simNumbers.size() < 1) {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null) {
                    simNumbers.add(tm.getSimSerialNumber());
                }
            }
        }
        return simNumbers;
    }


    public static boolean isValidImei(String s) {
        long n = 0;
        try {
            n = Long.parseLong(s);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
        int l = s.length();

        if (l != 15) // If length is not 15 then IMEI is Invalid
            return false;
        else {
            int d = 0, sum = 0;
            for (int i = 15; i >= 1; i--) {
                d = (int) (n % 10);

                if (i % 2 == 0) {
                    d = 2 * d; // Doubling every alternate digit
                }

                sum = sum + sumDig(d); // Finding sum of the digits

                n = n / 10;
            }

            System.out.println("Output : Sum = " + sum);

            if (sum % 10 == 0 && sum != 0)
                return true;
            else
                return false;
        }
    }

    private static int sumDig(
            int n) // Function for finding and returning sum of digits of a number
    {
        int a = 0;
        while (n > 0) {
            a = a + n % 10;
            n = n / 10;
        }
        return a;
    }


}
