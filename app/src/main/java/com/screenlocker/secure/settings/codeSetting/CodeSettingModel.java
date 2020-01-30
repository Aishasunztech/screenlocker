package com.screenlocker.secure.settings.codeSetting;

import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

import com.secure.launcher.R;

public class CodeSettingModel implements CodeSettingContract.CodeSettingMvpModel {
    private Context mContext;

    CodeSettingModel(Context context) {
        mContext = context;
    }

    @Override
    public void handleResetPassword(final CodeSettingContract.CodeSettingMvpView mvpView) {

        new AlertDialog.Builder(mContext)
                .setCancelable(false)
                .setTitle(mContext.getResources().getString(R.string.warning))
                .setMessage(mContext.getResources().getString(R.string.rest_pass_msg))
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> mvpView.resetPassword())
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.cancel()).show();
    }
}
