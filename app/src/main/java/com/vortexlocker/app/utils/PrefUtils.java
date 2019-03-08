package com.vortexlocker.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtils {

    private static final String PREF_FILE = "settings_pref";

    public static void saveToPref(Context context, boolean isEnabled) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("is_enabled", isEnabled);
        editor.apply();
    }

    public static boolean isLockScreenEnabled(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        return sharedPref.getBoolean("is_enabled", false);
    }

    public static void saveStringPref(Context context, String key, String value) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getStringPref(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        return sharedPref.getString(key, null);
    }

    public static boolean getBooleanPref(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(key, false);
    }

    public static void saveBooleanPref(Context context, String key, boolean value) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void saveIntegerPref(Context context, String key, int value) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getIntegerPref(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        return sharedPref.getInt(key, 0);
    }
}
