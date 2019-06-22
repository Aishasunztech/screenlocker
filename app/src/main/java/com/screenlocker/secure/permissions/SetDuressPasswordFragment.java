
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

public class SetDuressPasswordFragment extends AbstractStep {
    private String error = "";

    @Override
    public String name() {
        return getResources().getString(R.string.duress_pin);
    }


    @Override
    public void onSkip() {
        PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 4);
        super.onSkip();
    }

    @Override
    public boolean nextIf() {
        if (setPassword()) {
            if (PrefUtils.getStringPref(MyApplication.getAppContext(), KEY_DURESS_PASSWORD) != null) {
                PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 4);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setSkipable() {
        return true;
    }

    @Override
    public String error() {
        return error;
    }
    @Override
    public void onStepVisible() {
        super.onStepVisible();
        if (PrefUtils.getStringPref(MyApplication.getAppContext(), AppConstants.KEY_DURESS_PASSWORD) == null && mContext != null) {

            builder.show();
        }
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


    Context mContext = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        error = getResources().getString(R.string.set_skip_password);
    }

    private AlertDialog builder;
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
        builder = new AlertDialog.Builder(getContext()).
                setTitle(getResources().getString(R.string.warning))
                .setMessage(getResources().getString(R.string.duress_pin_message))
                .setPositiveButton(getResources().getString(R.string.ok_text), (dialogInterface, i) -> {
                    dialogInterface.cancel();
                })

                .setNegativeButton(getResources().getString(R.string.cancel_text), (dialogInterface, i) -> {
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
                etEnterPin.postDelayed(new Runnable() {
                                           @Override
                                           public void run() {
                                               InputMethodManager keyboard = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                                               keyboard.showSoftInput(etEnterPin, 0);
                                           }
                                       }
                        , 100);
            }
        });
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
                PrefUtils.saveStringPref(MyApplication.getAppContext(), AppConstants.KEY_DURESS_PASSWORD, reEnteredPassword);
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
}


