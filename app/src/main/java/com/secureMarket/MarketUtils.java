package com.secureMarket;

import android.content.Context;

import com.screenlocker.secure.utils.PrefUtils;

import static com.screenlocker.secure.utils.AppConstants.UNINSTALLED_PACKAGES;

public class MarketUtils {


    public static void savePackages(String packageName, String type, String userSpace, PrefUtils prefUtils) {

        String packages = prefUtils.getStringPref( type);
        if (packages == null) {
            packages = "";
        }

        prefUtils.saveStringPref( type, packages + packageName + ":" + userSpace + ",");
    }

    public static boolean isInUninstalled(PrefUtils context,String packageName)
    {
        String packages = context.getStringPref( UNINSTALLED_PACKAGES);
        if(packages != null)
        {
            if(packages.contains(packageName))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isGuest(String packageName,String type,String userSpace,PrefUtils prefUtils)
    {
        String[] packagesList = prefUtils.getStringPref(type).split(",");
        for(String packageItem : packagesList)
        {
            if(packageItem.contains(packageName+":" + userSpace))
            {
                return true;
            }
        }
        return false;
    }
}
