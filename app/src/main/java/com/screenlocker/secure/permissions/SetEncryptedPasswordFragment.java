package com.screenlocker.secure.permissions;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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

public class SetEncryptedPasswordFragment extends AbstractStep {
    private String error = "";

    @Override
    public String name() {
        return MyApplication.getAppContext().getResources().getString(R.string.encrypted_password);
    }

    @Override
    public void onNext() {

    }

    private View v;
    private Context mContext;

    @Override
    public boolean nextIf() {
        if (setPassword()) {
            if (PrefUtils.getStringPref(MyApplication.getAppContext(), KEY_MAIN_PASSWORD) != null) {
                PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 3);

//                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                return true;
            }
        }
        return false;
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
    }

    @BindView(R.id.pin_input_layout)
    TextInputLayout pin_input_layout;
    @BindView(R.id.etEnterPin)
    TextInputEditText etEnterPin;

    @BindView(R.id.re_pin_input_layout)
    TextInputLayout re_pin_input_layout;
    @BindView(R.id.etConfirmPin)
    TextInputEditText etConfirmPin;
    @BindView(R.id.guest_image_icon)
    ImageView img_picture;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.guess_password_layout, container, false);
        ButterKnife.bind(this, v);
        pin_input_layout.setHint(getResources().getString(R.string.hint_please_enter_encrypted_pin));
//        etEnterPin.setHint(R.string.hint_please_enter_encrypted_pin);
        re_pin_input_layout.setHint(getResources().getString(R.string.hint_please_confirm_your_pin));
//        etConfirmPin.setHint(R.string.hint_please_confirm_your_pin);
        error = getResources().getString(R.string.please_set_encrypted_password);
        img_picture.setImageDrawable(getResources().getDrawable(R.drawable.ic_encrypted_third));

        return v;
    }


    private boolean setPassword() {


        String enteredPassword = etEnterPin.getText().toString().trim();
        String reEnteredPassword = etConfirmPin.getText().toString().trim();

        boolean isValid = Validator.validAndMatch(enteredPassword, reEnteredPassword);


        if (isValid) {

            boolean keyOk = passwordsOk(getContext(), reEnteredPassword);

            if (keyOk) {
                PrefUtils.saveStringPref(MyApplication.getAppContext(), AppConstants.KEY_MAIN_PASSWORD, reEnteredPassword);
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

