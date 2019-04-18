package com.screenlocker.secure.permissions;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Validator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.screenlocker.secure.socket.utils.utils.passwordsOk;
import static com.screenlocker.secure.utils.AppConstants.DEF_PAGE_NO;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;

public class GuestPassword extends AbstractStep implements View.OnClickListener {
    @Override
    public String name() {
        return "Guess Password";
    }

    @Override
    public void onNext() {
        PrefUtils.saveIntegerPref(MyApplication.getAppContext(),DEF_PAGE_NO,2);
    }

    @Override
    public boolean nextIf() {
        if (PrefUtils.getStringPref(MyApplication.getAppContext(), KEY_GUEST_PASSWORD) != null ) {
            return true;
        }
        return false;
    }

    @Override
    public boolean setSkipable() {
        return false;
    }

    @Override
    public String error() {
        return "Please Set Guest Password";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
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
        View v =  inflater.inflate(R.layout.guess_password_layout,container, false);
        ButterKnife.bind(this,v);
        etEnterPin.setHint(R.string.hint_please_enter_guest_pin);
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
                            PrefUtils.saveStringPref(MyApplication.getAppContext(), AppConstants.KEY_GUEST_PASSWORD, reEnteredPassword);
                            btnConfirm.setText("Pin Confirmed");
                            btnConfirm.setEnabled(false);
                        } else {
                            etConfirmPin.setError("This password is taken please try again");
                        }


                }

                else {
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
