package com.screenlocker.secure.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.widget.EditText;

public interface SettingContract
{
    interface SettingsMvpView{}
    interface SettingsMvpPresenter{
        boolean isServiceRunning();
        void startLockService(Intent lockScreenIntent);
        void stopLockService(Intent lockScreenIntent);
        void showAlertDialog(EditText input, DialogInterface.OnClickListener onClickListener, DialogInterface.OnClickListener onNegativeClick, String title);
        String get_IMEI_number(TelephonyManager telephonyManager);
        boolean isMyLauncherDefault();
    }
    interface SettingsMvpModel{
        boolean isServiceRunning();

        void startLockService(Intent lockScreenIntent);

        void stopLockService(Intent lockScreenIntent);

        void showAlertDialog(EditText input, DialogInterface.OnClickListener onClickListener, DialogInterface.OnClickListener onNegativeClick, String title);

        String get_IMEI_number(TelephonyManager telephonyManager);
        boolean isMyLauncherDefault();
    }
}
