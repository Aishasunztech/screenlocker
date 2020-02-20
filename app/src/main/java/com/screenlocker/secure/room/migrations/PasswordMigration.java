package com.screenlocker.secure.room.migrations;

import android.content.Context;

import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.SecuredSharedPref;

/**
 * @author : Muhammad Nadeem
 * Created at: 2/12/2020
 */
public class PasswordMigration {
    private Context context;

    public PasswordMigration(Context context) {
        this.context = context;
    }
    public void migrate(){
        SecuredSharedPref securedSharedPref = SecuredSharedPref.getInstance(context);
        // Encrypted pass migrations
        PrefUtils prefUtils = PrefUtils.getInstance(context);
        securedSharedPref.saveStringPref( AppConstants.ENCRYPT_DEFAULT_CONFIG, prefUtils.getStringPref(AppConstants.ENCRYPT_DEFAULT_CONFIG));
        securedSharedPref.saveStringPref( AppConstants.ENCRYPT_COMBO_PATTERN, prefUtils.getStringPref(AppConstants.ENCRYPT_COMBO_PATTERN));
        securedSharedPref.saveStringPref( AppConstants.ENCRYPT_COMBO_PIN, prefUtils.getStringPref(AppConstants.ENCRYPT_COMBO_PIN));
        securedSharedPref.saveStringPref( AppConstants.KEY_MAIN_PASSWORD, prefUtils.getStringPref(AppConstants.KEY_MAIN_PASSWORD));
        securedSharedPref.saveStringPref( AppConstants.ENCRYPT_PATTERN, prefUtils.getStringPref(AppConstants.ENCRYPT_PATTERN));
        //Guest pass migrations
        securedSharedPref.saveStringPref( AppConstants.GUEST_DEFAULT_CONFIG, prefUtils.getStringPref(AppConstants.GUEST_DEFAULT_CONFIG));
        securedSharedPref.saveStringPref( AppConstants.GUEST_COMBO_PATTERN, prefUtils.getStringPref(AppConstants.GUEST_COMBO_PATTERN));
        securedSharedPref.saveStringPref( AppConstants.GUEST_COMBO_PIN, prefUtils.getStringPref(AppConstants.GUEST_COMBO_PIN));
        securedSharedPref.saveStringPref( AppConstants.KEY_GUEST_PASSWORD, prefUtils.getStringPref(AppConstants.KEY_GUEST_PASSWORD));
        securedSharedPref.saveStringPref( AppConstants.GUEST_PATTERN, prefUtils.getStringPref(AppConstants.GUEST_PATTERN));
        //Duress pass migrations
        securedSharedPref.saveStringPref( AppConstants.DUERESS_DEFAULT_CONFIG, prefUtils.getStringPref(AppConstants.DUERESS_DEFAULT_CONFIG));
        securedSharedPref.saveStringPref( AppConstants.DURESS_COMBO_PATTERN, prefUtils.getStringPref(AppConstants.DURESS_COMBO_PATTERN));
        securedSharedPref.saveStringPref( AppConstants.DURESS_COMBO_PIN, prefUtils.getStringPref(AppConstants.DURESS_COMBO_PIN));
        securedSharedPref.saveStringPref( AppConstants.KEY_DURESS_PASSWORD, prefUtils.getStringPref(AppConstants.KEY_DURESS_PASSWORD));
        securedSharedPref.saveStringPref( AppConstants.DURESS_PATTERN, prefUtils.getStringPref(AppConstants.DURESS_PATTERN));
        //keyCode migrations
        securedSharedPref.saveStringPref( AppConstants.KEY_CODE_PASSWORD, prefUtils.getStringPref(AppConstants.KEY_CODE_PASSWORD));

        ///other critical info

        securedSharedPref.saveIntegerPref( AppConstants.LOGIN_ATTEMPTS, prefUtils.getIntegerPref(AppConstants.LOGIN_ATTEMPTS));
        securedSharedPref.saveLongPref( AppConstants.TIME_REMAINING_REBOOT, prefUtils.getLongPref(AppConstants.TIME_REMAINING_REBOOT));
        securedSharedPref.saveLongPref( AppConstants.TIME_REMAINING, prefUtils.getLongPref(AppConstants.TIME_REMAINING));




    }
}
