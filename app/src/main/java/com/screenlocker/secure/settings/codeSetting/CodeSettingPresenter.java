package com.screenlocker.secure.settings.codeSetting;

public class CodeSettingPresenter implements CodeSettingContract.CodeSettingMvpPresenter {


    private CodeSettingContract.CodeSettingMvpModel mvpModel;
    private CodeSettingContract.CodeSettingMvpView mvpView;

    public CodeSettingPresenter(CodeSettingContract.CodeSettingMvpModel mvpModel,
                                CodeSettingContract.CodeSettingMvpView mvpView) {
        this.mvpModel = mvpModel;
        this.mvpView = mvpView;
    }

    @Override
    public void handleSetAppsPermission() {
        mvpView.handleSetAppsPermission();
    }

    @Override
    public void handleResetPassword() {
        mvpModel.handleResetPassword(mvpView);
    }
}
