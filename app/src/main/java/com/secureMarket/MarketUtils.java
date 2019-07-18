package com.secureMarket;

import android.content.Context;

import com.screenlocker.secure.utils.PrefUtils;

public class MarketUtils {


    public static void savePackages(String packageName, String type, String userSpace, Context context) {

        String packages = PrefUtils.getStringPref(context, type);

        if (packages == null) {
            packages = "";
        }

        PrefUtils.saveStringPref(context, type, packages + packageName + ":" + userSpace + ",");
    }
}
