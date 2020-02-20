package com.screenlocker.secure.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class PrefUtils {
    private static PrefUtils instance;
    private SharedPreferences sharedPref;

    public  static  synchronized  PrefUtils getInstance(Context context){
        if (instance==null){
            instance = new PrefUtils(context);
        }
        return instance;
    }

    public PrefUtils(Context context) {
        sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
    }

    public static final String PREF_FILE = "settings_pref";

    public  void saveToPref( boolean isEnabled) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("is_enabled", isEnabled);
        editor.apply();
    }
//    public static void saveStringSetPref(Context context, String key, Set<String> value) {
//        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putStringSet(key, value);
//        editor.apply();
//    }
//
//    public static void saveStringPref(Context context, String key, String value) {
//        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putString(key, value);
//        editor.apply();
//    }
//
//    public static String getStringPref(Context context, String key) {
//        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
//        return sharedPref.getString(key, null);
//    }
//
//    public static Set<String> getStringSet(Context context, String key) {
//        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
//        return sharedPref.getStringSet(key, null);
//    }
//
//    public static boolean getBooleanPref(Context context, String key) {
//        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
//        return sharedPref.getBoolean(key, false);
//    }
//
//    public static boolean getBooleanPrefWithDefTrue(Context context, String key) {
//        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
//        return sharedPref.getBoolean(key, true);
//    }
//
//    public static void saveBooleanPref(Context context, String key, boolean value) {
//        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putBoolean(key, value);
//        editor.apply();
//    }
//
//    public static void saveIntegerPref(Context context, String key, int value) {
//        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putInt(key, value);
//        editor.apply();
//    }
//
//    public static int getIntegerPref(Context context, String key) {
//        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
//        return sharedPref.getInt(key, 0);
//    }
//
//    public static void saveLongPref(Context context, String key, long value) {
//        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putLong(key, value);
//        editor.apply();
//    }
//
//    public static long getLongPref(Context context, String key) {
//        SharedPreferences sharedPref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
//        return sharedPref.getLong(key, 0);
//
//    }

    public void saveStringSetPref(String key, Set<String> value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(key, value);
        editor.apply();
    }

    public  void saveStringPref( String key, String value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getStringPref( String key) {
        return sharedPref.getString(key, null);
    }

    public Set<String> getStringSet( String key) {
        return sharedPref.getStringSet(key, null);
    }

    public boolean getBooleanPref( String key) {
        return sharedPref.getBoolean(key, false);
    }

    public boolean getBooleanPrefWithDefTrue( String key) {
        return sharedPref.getBoolean(key, true);
    }

    public void saveBooleanPref( String key, boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void saveIntegerPref(String key, int value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int getIntegerPref( String key) {
        return sharedPref.getInt(key, 0);
    }

    public void saveLongPref(String key, long value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public long getLongPref( String key) {
        return sharedPref.getLong(key, 0);
    }

}
