
package com.screenlocker.secure.permissions;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Validator;
import com.screenlocker.secure.views.patternlock.PatternLockWithDotsOnly;
import com.screenlocker.secure.views.patternlock.listener.PatternLockWithDotListener;
import com.screenlocker.secure.views.patternlock.utils.PatternLockUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.screenlocker.secure.socket.utils.utils.passwordsOk;
import static com.screenlocker.secure.utils.AppConstants.DEF_PAGE_NO;
import static com.screenlocker.secure.utils.AppConstants.DURESS_PASSORD_OPTION;
import static com.screenlocker.secure.utils.AppConstants.GUEST_PASSORD_OPTION;
import static com.screenlocker.secure.utils.AppConstants.KEY_DURESS_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.OPTION_PATTERN;
import static com.screenlocker.secure.utils.AppConstants.OPTION_PIN;

public class SetDuressPasswordFragment extends AbstractStep {
    private String error = "";
    private Context mContext;

    private int mTry = 0;
    private String tryPattern;
    private OnPageUpdateListener mListener;
    @Override
    public String name() {
        return MyApplication.getAppContext().getResources().getString(R.string.duress_pin);
    }


    @Override
    public void onSkip() {
        PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 7);
        super.onSkip();
    }

    @Override
    public boolean nextIf() {
        if (setPassword()) {
            if (PrefUtils.getStringPref(MyApplication.getAppContext(), KEY_DURESS_PASSWORD) != null) {
                PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 7);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSkipable() {
        return false;
    }

    @Override
    public String error() {
        return error;
    }
    @Override
    public void onStepVisible() {
        super.onStepVisible();
        switch (PrefUtils.getIntegerPref(MyApplication.getAppContext(), DURESS_PASSORD_OPTION)) {
            case OPTION_PIN:
                viewSwitcher.setDisplayedChild(1);
                if (etEnterPin != null) {
                    etEnterPin.setFocusable(true);
                    etEnterPin.setFocusableInTouchMode(true);
                    etEnterPin.clearFocus();
                    etEnterPin.requestFocus();
                    etEnterPin.postDelayed(() -> {
                                InputMethodManager keyboard = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                                keyboard.showSoftInput(etEnterPin, 0);
                            }
                            , 0);
                }
                break;
            case OPTION_PATTERN:
                viewSwitcher.setDisplayedChild(0);

        }
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (OnPageUpdateListener) context;
            mContext = context;
        } catch (Exception ignored) {

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        error = getResources().getString(R.string.set_skip_password);
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

    @BindView(R.id.profile_image)
    ImageView img_picture2;
    @BindView(R.id.patter_lock_view)
    PatternLockWithDotsOnly patternLock;
    @BindView(R.id.profile_name)
    TextView responsTitle;

    @BindView(R.id.view_switcher)
    ViewSwitcher viewSwitcher;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.guess_password_layout, container, false);
        ButterKnife.bind(this, v);

        pin_input_layout.setHint(getResources().getString(R.string.hint_please_enter_duress_pin));
//        etEnterPin.setHint(R.string.hint_please_enter_duress_pin);
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
        re_pin_input_layout.setHint(getResources().getString(R.string.hint_please_confirm_your_pin));
        img_picture.setImageDrawable(getResources().getDrawable(R.drawable.ic_duress_icon));
        img_picture2.setImageDrawable(getResources().getDrawable(R.drawable.ic_duress_icon));
        patternLock.addPatternLockListener(new PatternLockWithDotListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onProgress(List<PatternLockWithDotsOnly.Dot> progressPattern) {

            }

            @Override
            public void onComplete(List<PatternLockWithDotsOnly.Dot> pattern) {

                if (pattern.size() < 4) {
                    Toast.makeText(MyApplication.getAppContext(), "Pattern is too Short", Toast.LENGTH_SHORT).show();
                    patternLock.setViewMode(PatternLockWithDotsOnly.PatternViewMode.WRONG);
                    new Handler().postDelayed(patternLock::clearPattern, 500);
                    return;
                }
                if (mTry == 0) {
                    tryPattern = PatternLockUtils.patternToString(patternLock, pattern);
                    if (tryPattern.equals(PrefUtils.getStringPref(MyApplication.getAppContext(), AppConstants.GUEST_PATTERN)) || tryPattern.equals(PrefUtils.getStringPref(MyApplication.getAppContext(), AppConstants.ENCRYPT_PATTERN))) {
                        Toast.makeText(MyApplication.getAppContext(), "Pattern already Taken", Toast.LENGTH_SHORT).show();
                        patternLock.setViewMode(PatternLockWithDotsOnly.PatternViewMode.WRONG);
                        new Handler().postDelayed(() -> {
                            patternLock.clearPattern();
                        }, 150);
                        return;
                    }


                    mTry++;
                    responsTitle.setText("Confirm Pattern");
                    patternLock.clearPattern();

                } else if (mTry == 1) {
                    if (tryPattern.equals(PatternLockUtils.patternToString(patternLock, pattern))) {
                        PrefUtils.saveStringPref(MyApplication.getAppContext(), AppConstants.DUERESS_DEFAULT_CONFIG, AppConstants.PATTERN_PASSWORD);
                        PrefUtils.saveStringPref(MyApplication.getAppContext(), AppConstants.DURESS_PATTERN, tryPattern);
                        PrefUtils.saveStringPref(MyApplication.getAppContext(), KEY_DURESS_PASSWORD, null);
                        Toast.makeText(MyApplication.getAppContext(), "Pattern Updated", Toast.LENGTH_SHORT).show();
                        //move to next
                        PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 7);
                        mListener.onPageUpdate(7);

                    }


                    //wrong pattern
                    else {
                        mTry = 0;
                        Toast.makeText(MyApplication.getAppContext(), "Pattern Did Not Match", Toast.LENGTH_SHORT).show();
                        patternLock.setViewMode(PatternLockWithDotsOnly.PatternViewMode.WRONG);
                        new Handler().postDelayed(() -> patternLock.clearPattern(), 500);
                        responsTitle.setText("Please Draw Pattern");

                    }
                }
            }


            @Override
            public void onCleared() {

            }
        });


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


