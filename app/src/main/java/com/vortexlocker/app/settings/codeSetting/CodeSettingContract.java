package com.vortexlocker.app.settings.codeSetting;

public interface CodeSettingContract {
    interface CodeSettingMvpView{
        void handleSetAppsPermission();


        void resetPassword();
    }
    interface CodeSettingMvpPresenter{
        void handleSetAppsPermission();

        void handleResetPassword();
    }
    interface CodeSettingMvpModel{
        void handleResetPassword(CodeSettingMvpView mvpView);
    }
}
