
package com.screenlocker.secure.permissions;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Validator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.screenlocker.secure.socket.utils.utils.passwordsOk;
import static com.screenlocker.secure.utils.AppConstants.DEF_PAGE_NO;
import static com.screenlocker.secure.utils.AppConstants.KEY_DURESS_PASSWORD;

public class SetDuressPasswordFragment extends AbstractStep implements View.OnClickListener {

    @Override
    public String name() {
        return "Duress Pin";
    }



    @Override
    public void onSkip() {
        PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 4);
        super.onSkip();
    }

    @Override
    public boolean nextIf() {
        if (PrefUtils.getStringPref(MyApplication.getAppContext(), KEY_DURESS_PASSWORD) != null) {
            PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 4);
            return true;
        }
        return false;
    }

    @Override
    public boolean setSkipable() {
        return true;
    }

    @Override
    public String error() {
        return "Please Set or Skip Password";
    }

    @Override
    public void onStepVisible() {
        if (PrefUtils.getStringPref(MyApplication.getAppContext(), AppConstants.KEY_DURESS_PASSWORD) == null && mContext != null) {

           builder.show();
        }
    }

    Context mContext = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
private  AlertDialog builder;
    @BindView(R.id.etEnterPin)
    AppCompatEditText etEnterPin;

    /**
     * to confirm the user entered password
     */
    @BindView(R.id.etConfirmPin)
    AppCompatEditText etConfirmPin;

    /**
     * button to validate the password and save it
     */
    @BindView(R.id.btnConfirm)
    AppCompatButton btnConfirm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.guess_password_layout, container, false);
        ButterKnife.bind(this, v);
         builder =  new AlertDialog.Builder(getContext()).
                setTitle("Warning!")
                .setMessage("Entering Duress Pin when device is locked will wipe your phone data. You cannot undo this action. All data will be deleted from target device without any confirmation. There is no way to reverse this action.")
                .setPositiveButton("Ok", (dialogInterface, i) -> {
                    dialogInterface.cancel();
                })

                .setNegativeButton("Cancel", (dialogInterface, i) -> {
//                        dialogInterface.cancel();
                    dialogInterface.dismiss();

                }).create();
         builder.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        etEnterPin.setHint(R.string.hint_please_enter_duress_pin);
        etEnterPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {
                etEnterPin.clearFocus();
                etEnterPin.requestFocus();
                etEnterPin.postDelayed(new Runnable(){
                                      @Override public void run(){
                                          InputMethodManager keyboard=(InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                          keyboard.showSoftInput(etEnterPin,0);
                                      }
                                  }
                        ,100);
            }
        });
        etConfirmPin.setHint(R.string.hint_please_confirm_your_pin);

        btnConfirm.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnConfirm) {


            String enteredPassword = etEnterPin.getText().toString().trim();
            String reEnteredPassword = etConfirmPin.getText().toString().trim();

            boolean isValid = Validator.validAndMatch(enteredPassword, reEnteredPassword);


            if (isValid) {

                boolean keyOk = passwordsOk(getContext(), reEnteredPassword);

                //Password password = new Password();
                // password.setUserPassword(reEnteredPassword);
                if (keyOk) {
                    PrefUtils.saveStringPref(getContext(), AppConstants.KEY_DURESS_PASSWORD, reEnteredPassword);
                    btnConfirm.setText("Pin Confirmed");
                    btnConfirm.setEnabled(false);
                } else {
                    etConfirmPin.setError("This password is taken please try again");
                }


            } else {
                if (TextUtils.isEmpty(enteredPassword)) {
                    etEnterPin.setError(getString(R.string.empty));
                } else if (reEnteredPassword.equals("")) {
                    etConfirmPin.setError(getString(R.string.empty));
                } else {
                    etConfirmPin.setError(getString(R.string.password_dont_match));
                }

            }
        }
    }

}


