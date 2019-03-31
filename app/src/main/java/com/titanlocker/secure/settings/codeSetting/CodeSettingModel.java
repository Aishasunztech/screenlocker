package com.titanlocker.secure.settings.codeSetting;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.titanlocker.secure.R;

public class CodeSettingModel implements CodeSettingContract.CodeSettingMvpModel {
    private Context mContext;

    public CodeSettingModel(Context context) {
        mContext = context;
    }

    @Override
    public void handleResetPassword(final CodeSettingContract.CodeSettingMvpView mvpView) {

        new AlertDialog.Builder(mContext)
                .setCancelable(false)
                .setTitle("Warning")
                .setMessage("This will reset Guest and Encrypted \tpasswords.Do you want to reset passwords?")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mvpView.resetPassword();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }).show();
    }
}
