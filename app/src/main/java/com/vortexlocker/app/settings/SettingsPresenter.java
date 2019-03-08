package com.vortexlocker.app.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.widget.EditText;

public class SettingsPresenter implements SettingContract.SettingsMvpPresenter {
    private SettingContract.SettingsMvpView mvpView;
    private SettingContract.SettingsMvpModel mvpModel;

    public SettingsPresenter(SettingContract.SettingsMvpView settingsMvpView, SettingContract.SettingsMvpModel settingsMvpModel) {
        this.mvpView = settingsMvpView;
        this.mvpModel = settingsMvpModel;
    }

    @Override
    public boolean isServiceRunning() {
        return mvpModel.isServiceRunning();
    }

    @Override
    public void startLockService(Intent lockScreenIntent) {
        mvpModel.startLockService(lockScreenIntent);
    }

    @Override
    public void stopLockService(Intent lockScreenIntent) {
        mvpModel.stopLockService(lockScreenIntent);
    }

    @Override
    public void showAlertDialog(EditText input, DialogInterface.OnClickListener onClickListener, DialogInterface.OnClickListener onNegativeClick, String title) {
        mvpModel.showAlertDialog(input, onClickListener, onNegativeClick, title);
    }

    @Override
    public String get_IMEI_number(TelephonyManager telephonyManager) {
        return mvpModel.get_IMEI_number(telephonyManager);
    }

    @Override
    public boolean isMyLauncherDefault() {
        return mvpModel.isMyLauncherDefault();

    }
}
