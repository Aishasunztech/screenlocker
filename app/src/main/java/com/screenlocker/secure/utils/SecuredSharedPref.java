package com.screenlocker.secure.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Set;

/**
 * @author : Muhammad Nadeem
 * Created at: 2/3/2020
 */
public class SecuredSharedPref {

    private SharedPreferences sharedPreferences = null;
    private static SecuredSharedPref instance;
    public static SecuredSharedPref getInstance(Context context){
        if (instance==null){
            instance = new SecuredSharedPref(context);
        }return  instance;
    }

    private SecuredSharedPref(Context context) {

        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    "secret_shared_prefs",
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        // use the shared preferences and editor as you normally would
    }

    public void saveStringPref(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getStringPref(String key) {
        return sharedPreferences.getString(key, null);
    }

    public boolean getBooleanPref( String key) {
        return sharedPreferences.getBoolean(key, false);
    }
    public boolean getBooleanPrefWithDefTrue( String key) {
        return sharedPreferences.getBoolean(key, true);
    }

    public void saveBooleanPref(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public void saveIntegerPref( String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int getIntegerPref( String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public void saveLongPref(String key, long value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public long getLongPref(String key) {
        return sharedPreferences.getLong(key, 0);
    }



}
