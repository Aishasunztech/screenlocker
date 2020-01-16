package com.secureMarket;

import android.content.Context;

import com.screenlocker.secure.utils.PrefUtils;

import static com.screenlocker.secure.utils.AppConstants.UNINSTALLED_PACKAGES;

public class MarketUtils {


    public static void savePackages(String packageName, String type, String userSpace, Context context) {

        String packages = PrefUtils.getStringPref(context, type);
        if (packages == null) {
            packages = "";
        }

        PrefUtils.saveStringPref(context, type, packages + packageName + ":" + userSpace + ",");
    }

    public static boolean isInUninstalled(Context context,String packageName)
    {
        String packages = PrefUtils.getStringPref(context, UNINSTALLED_PACKAGES);
        if(packages != null)
        {
            if(packages.contains(packageName))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isGuest(String packageName,String type,String userSpace,Context context)
    {
        String[] packagesList = PrefUtils.getStringPref(context,type).split(",");
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
