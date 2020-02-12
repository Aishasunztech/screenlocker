package com.screenlocker.secure.room.migrations;

import android.content.Context;

import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.SecuredSharedPref;

import static com.screenlocker.secure.utils.AppConstants.LOGIN_ATTEMPTS;
import static com.screenlocker.secure.utils.AppConstants.TIME_REMAINING;
import static com.screenlocker.secure.utils.AppConstants.TIME_REMAINING_REBOOT;

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
        securedSharedPref.saveStringPref( AppConstants.ENCRYPT_DEFAULT_CONFIG, PrefUtils.getStringPref(context,AppConstants.ENCRYPT_DEFAULT_CONFIG));
        securedSharedPref.saveStringPref( AppConstants.ENCRYPT_COMBO_PATTERN, PrefUtils.getStringPref(context,AppConstants.ENCRYPT_COMBO_PATTERN));
        securedSharedPref.saveStringPref( AppConstants.ENCRYPT_COMBO_PIN, PrefUtils.getStringPref(context,AppConstants.ENCRYPT_COMBO_PIN));
        securedSharedPref.saveStringPref( AppConstants.KEY_MAIN_PASSWORD, PrefUtils.getStringPref(context,AppConstants.KEY_MAIN_PASSWORD));
        securedSharedPref.saveStringPref( AppConstants.ENCRYPT_PATTERN, PrefUtils.getStringPref(context,AppConstants.ENCRYPT_PATTERN));
        //Guest pass migrations
        securedSharedPref.saveStringPref( AppConstants.GUEST_DEFAULT_CONFIG, PrefUtils.getStringPref(context,AppConstants.GUEST_DEFAULT_CONFIG));
        securedSharedPref.saveStringPref( AppConstants.GUEST_COMBO_PATTERN, PrefUtils.getStringPref(context,AppConstants.GUEST_COMBO_PATTERN));
        securedSharedPref.saveStringPref( AppConstants.GUEST_COMBO_PIN, PrefUtils.getStringPref(context,AppConstants.GUEST_COMBO_PIN));
        securedSharedPref.saveStringPref( AppConstants.KEY_GUEST_PASSWORD, PrefUtils.getStringPref(context,AppConstants.KEY_GUEST_PASSWORD));
        securedSharedPref.saveStringPref( AppConstants.GUEST_PATTERN, PrefUtils.getStringPref(context,AppConstants.GUEST_PATTERN));
        //Duress pass migrations
        securedSharedPref.saveStringPref( AppConstants.DUERESS_DEFAULT_CONFIG, PrefUtils.getStringPref(context,AppConstants.DUERESS_DEFAULT_CONFIG));
        securedSharedPref.saveStringPref( AppConstants.DURESS_COMBO_PATTERN, PrefUtils.getStringPref(context,AppConstants.DURESS_COMBO_PATTERN));
        securedSharedPref.saveStringPref( AppConstants.DURESS_COMBO_PIN, PrefUtils.getStringPref(context,AppConstants.DURESS_COMBO_PIN));
        securedSharedPref.saveStringPref( AppConstants.KEY_DURESS_PASSWORD, PrefUtils.getStringPref(context,AppConstants.KEY_DURESS_PASSWORD));
        securedSharedPref.saveStringPref( AppConstants.DURESS_PATTERN, PrefUtils.getStringPref(context,AppConstants.DURESS_PATTERN));
        //keyCode migrations
        securedSharedPref.saveStringPref( AppConstants.KEY_CODE_PASSWORD, PrefUtils.getStringPref(context,AppConstants.KEY_CODE_PASSWORD));

        ///other critical info

        securedSharedPref.saveIntegerPref( AppConstants.LOGIN_ATTEMPTS, PrefUtils.getIntegerPref(context,AppConstants.LOGIN_ATTEMPTS));
        securedSharedPref.saveLongPref( AppConstants.TIME_REMAINING_REBOOT, PrefUtils.getLongPref(context,AppConstants.TIME_REMAINING_REBOOT));
        securedSharedPref.saveLongPref( AppConstants.TIME_REMAINING, PrefUtils.getLongPref(context,AppConstants.TIME_REMAINING));




    }
}
