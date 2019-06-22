package com.screenlocker.secure.permissions;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

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

public class SetGuestPasswordFragment extends AbstractStep {
    private volatile String error = "";
    private Context mContext;

    @Override
    public String name() {
        return getResources().getString(R.string.guest_password);
    }

    @Override
    public void onStepVisible() {
        super.onStepVisible();
        if (etEnterPin != null){
            etEnterPin.setFocusable(true);
            etEnterPin.setFocusableInTouchMode(true);
            etEnterPin.clearFocus();
            etEnterPin.requestFocus();
            etEnterPin.postDelayed(() -> {
                InputMethodManager keyboard=(InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(etEnterPin,0);
            }
                    ,0);
        }
    }

    @Override
    public boolean nextIf() {
        if (setPassword()) {

            if (PrefUtils.getStringPref(MyApplication.getAppContext(), KEY_GUEST_PASSWORD) != null) {
                PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 2);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setSkipable() {
        return false;
    }

    @Override
    public String error() {
        return error;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        error = getResources().getString(R.string.please_enter_password);
    }

    @BindView(R.id.etEnterPin)
    AppCompatEditText etEnterPin;

    /**
     * to confirm the user entered password
     */
    @BindView(R.id.etConfirmPin)
    AppCompatEditText etConfirmPin;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.guess_password_layout, container, false);
        ButterKnife.bind(this, v);
        etEnterPin.setHint(R.string.hint_please_enter_guest_pin);
        etConfirmPin.setHint(R.string.hint_please_confirm_your_pin);

        return v;
    }

    private boolean setPassword() {


        String enteredPassword = etEnterPin.getText().toString().trim();
        String reEnteredPassword = etConfirmPin.getText().toString().trim();

        boolean isValid = Validator.validAndMatch(enteredPassword, reEnteredPassword);


        if (isValid) {

            boolean keyOk = passwordsOk(getContext(), reEnteredPassword);

            if (keyOk) {
                PrefUtils.saveStringPref(MyApplication.getAppContext(), AppConstants.KEY_GUEST_PASSWORD, reEnteredPassword);
                return true;

            } else {
                error = getResources().getString(R.string.password_taken);
                return false;
            }


        } else {
            if (TextUtils.isEmpty(enteredPassword)) {
                error = getString(R.string.empty);
            } else if (reEnteredPassword.equals("")) {
                error = getString(R.string.empty);
            } else {
                error = getString(R.string.password_dont_match);
            }
            return false;

        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }
}
