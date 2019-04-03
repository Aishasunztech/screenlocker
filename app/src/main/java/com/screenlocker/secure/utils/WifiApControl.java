package com.screenlocker.secure.utils;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.Method;

import timber.log.Timber;

public class WifiApControl {
    private static Method getWifiApState;
    private static Method isWifiApEnabled;
    private static Method setWifiApEnabled;
    private static Method getWifiApConfiguration;

    public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";

    public static final int WIFI_AP_STATE_DISABLED = WifiManager.WIFI_STATE_DISABLED;
    public static final int WIFI_AP_STATE_DISABLING = WifiManager.WIFI_STATE_DISABLING;
    public static final int WIFI_AP_STATE_ENABLED = WifiManager.WIFI_STATE_ENABLED;
    public static final int WIFI_AP_STATE_ENABLING = WifiManager.WIFI_STATE_ENABLING;
    public static final int WIFI_AP_STATE_FAILED = WifiManager.WIFI_STATE_UNKNOWN;

    public static final String EXTRA_PREVIOUS_WIFI_AP_STATE = WifiManager.EXTRA_PREVIOUS_WIFI_STATE;
    public static final String EXTRA_WIFI_AP_STATE = WifiManager.EXTRA_WIFI_STATE;

    static {
        // lookup methods and fields not defined publicly in the SDK.
        Class<?> cls = WifiManager.class;
        for (Method method : cls.getDeclaredMethods()) {
            String methodName = method.getName();
            if (methodName.equals("getWifiApState")) {
                getWifiApState = method;
            } else if (methodName.equals("isWifiApEnabled")) {
                isWifiApEnabled = method;
            } else if (methodName.equals("setWifiApEnabled")) {
                setWifiApEnabled = method;
            } else if (methodName.equals("getWifiApConfiguration")) {
                getWifiApConfiguration = method;
            }
        }
    }

    public static boolean isApSupported() {
        return (getWifiApState != null && isWifiApEnabled != null
                && setWifiApEnabled != null && getWifiApConfiguration != null);
    }

    private static WifiManager mgr;

    private WifiApControl(WifiManager mgr) {
        WifiApControl.mgr = mgr;
    }

    public static WifiApControl getApControl(WifiManager mgr) {
        if (!isApSupported())
            return null;
        return new WifiApControl(mgr);
    }

    public static boolean isWifiApEnabled(WifiManager wifiManager) {
        try {
            return (Boolean) isWifiApEnabled.invoke(wifiManager);
        } catch (Exception e) {
            Timber.v(e, e.toString()); // shouldn't happen
            return false;
        }
    }

    public int getWifiApState() {
        try {
            return (Integer) getWifiApState.invoke(mgr);
        } catch (Exception e) {
            Timber.v(e, e.toString()); // shouldn't happen
            return -1;
        }
    }

    private WifiConfiguration getWifiApConfiguration() {
        try {
            return (WifiConfiguration) getWifiApConfiguration.invoke(mgr);
        } catch (Exception e) {
            Timber.v(e, e.toString()); // shouldn't happen
            return null;
        }
    }

    private void setWifiApEnabled(WifiConfiguration config, boolean enabled) {
        try {
            setWifiApEnabled.invoke(mgr, config, enabled);
        } catch (Exception e) {
            Timber.v(e, e.toString()); // shouldn't happen
        }
    }

    public static void turnOnOffHotspot(boolean isTurnToOn, WifiManager wifiManager) {

        WifiApControl apControl = WifiApControl.getApControl(wifiManager);
        if (apControl != null) {

            // TURN OFF YOUR WIFI BEFORE ENABLE HOTSPOT
            //if (isWifiOn(context) && isTurnToOn) {
            //  turnOnOffWifi(context, false);
            //}
            apControl.setWifiApEnabled(apControl.getWifiApConfiguration(), isTurnToOn);
        }
    }

    public static void changeStateWifiAp(boolean activated, WifiManager wifiManager, WifiConfiguration wifiConfiguration) {
        Method method;
        try {
            method = wifiManager.getClass().getDeclaredMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            method.invoke(wifiManager, wifiConfiguration, activated);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}