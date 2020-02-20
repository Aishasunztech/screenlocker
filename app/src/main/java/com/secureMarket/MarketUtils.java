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

    public static boolean isInUninstalled(PrefUtils prefUtils,String packageName)
    {
        String packages = prefUtils.getStringPref( UNINSTALLED_PACKAGES);
        if(packages != null)
        {
            if(packages.contains(packageName))
            {
                return true;
            }
        }
        return false;
    }

}
