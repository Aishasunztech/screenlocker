package com.screenlocker.secure.app;

import android.content.Context;
import android.content.SharedPreferences;


public class PrefManager {

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME="com.secureportal.barryapp.preferences";

    PrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    //DELETE

    public boolean getBoolean(String key){
        return sharedPreferences.getBoolean(key,false);
    }

    public String getString(String key){
        return sharedPreferences.getString(key,null);
    }

    public int getInt(String key){
        return sharedPreferences.getInt(key,0);
    }

    //ADD

    public void addString(String key,String value){
        sharedPreferences.edit().putString(key,value).apply();
    }

    public void addInt(String key,int value){
        sharedPreferences.edit().putInt(key,value).apply();
    }

    public void addBoolean(String key,boolean value){
        sharedPreferences.edit().putBoolean(key,value).apply();
    }

    public void clearPreferences(){
        sharedPreferences.edit().clear().apply();
    }
}
