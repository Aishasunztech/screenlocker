package com.screenlocker.secure.permissions;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.secure.launcher.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.settings.managepassword.NCodeView;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Validator;
import com.screenlocker.secure.views.patternlock.PatternLockView;
import com.screenlocker.secure.views.patternlock.PatternLockWithDotsOnly;
import com.screenlocker.secure.views.patternlock.listener.PatternLockViewListener;
import com.screenlocker.secure.views.patternlock.listener.PatternLockWithDotListener;
import com.screenlocker.secure.views.patternlock.utils.PatternLockUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.screenlocker.secure.socket.utils.utils.passwordsOk;
import static com.screenlocker.secure.utils.AppConstants.DEF_PAGE_NO;
import static com.screenlocker.secure.utils.AppConstants.GUEST_PASSORD_OPTION;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.OPTION_COMBO;
import static com.screenlocker.secure.utils.AppConstants.OPTION_PATTERN;
import static com.screenlocker.secure.utils.AppConstants.OPTION_PIN;

public class SetGuestPasswordFragment extends AbstractStep {
    private volatile String error = "";
    private Context mContext;
    private int mTry = 0;
    private String tryPattern;
    private OnPageUpdateListener mListener;
    private String mCode;
    private String mPattern;
    private int mTryCombo = 0;

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
    ViewFlipper viewSwitcher;
    @BindView(R.id.textView7)
    TextView msg;
    @BindView(R.id.NCodeView)
    NCodeView codeView;
    @BindView(R.id.patter_lock_view_combo)
    PatternLockView patternLockView;
    @BindView(R.id.btntry)
    Button btnrTry;
    @BindView(R.id.btnConfirm)
    Button btnConfirm;

    @Override
    public String name() {
        return MyApplication.getAppContext().getResources().getString(R.string.guest_password);
    }

    @Override
    public void onStepVisible() {
        super.onStepVisible();
        switch (PrefUtils.getIntegerPref(MyApplication.getAppContext(), GUEST_PASSORD_OPTION)) {
            case OPTION_PIN:
                viewSwitcher.setDisplayedChild(1);
                if (etEnterPin != null) {
                    etEnterPin.setText(null);
                    etConfirmPin.setText(null);
                    pin_input_layout.setHint(getResources().getString(R.string.hint_please_enter_guest_pin));
                    re_pin_input_layout.setHint(getResources().getString(R.string.hint_please_confirm_your_pin));
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
                mTry = 0;
                tryPattern = "";
                responsTitle.setText("Please Draw Pattern");
                viewSwitcher.setDisplayedChild(0);
                break;
            case OPTION_COMBO:
                codeView.clearCode();
                mTryCombo = 0;
                mCode = null;
                mPattern = null;
                msg.setText("Input PIN");
                patternLockView.setNumberInputAllow(true);
                patternLockView.invalidate();
                viewSwitcher.setDisplayedChild(2);
                break;


        }
    }

    @Override
    public boolean nextIf() {
        if (setPassword()) {

            if (PrefUtils.getStringPref(MyApplication.getAppContext(), KEY_GUEST_PASSWORD) != null) {
                PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 3);
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
    public boolean isPreviousAllow() {
        return true;
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

    /**
     * to confirm the user entered password
     */
//    @BindView(R.id.etConfirmPin)
//    AppCompatEditText etConfirmPin;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.guess_password_layout, container, false);
        ButterKnife.bind(this, v);
        pin_input_layout.setHint(getResources().getString(R.string.hint_please_enter_guest_pin));
        re_pin_input_layout.setHint(getResources().getString(R.string.hint_please_confirm_your_pin));
        img_picture.setImageDrawable(getResources().getDrawable(R.drawable.ic_guest_icon));
        img_picture2.setImageDrawable(getResources().getDrawable(R.drawable.ic_guest_icon));
        ((ImageView) v.findViewById(R.id.profile_image_combo)).setImageResource(R.drawable.ic_guest_icon);
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
                    if (tryPattern.equals(PrefUtils.getStringPref(MyApplication.getAppContext(), AppConstants.ENCRYPT_PATTERN)) || tryPattern.equals(PrefUtils.getStringPref(MyApplication.getAppContext(), AppConstants.DURESS_PATTERN))) {
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
                        PrefUtils.saveStringPref(MyApplication.getAppContext(), AppConstants.GUEST_DEFAULT_CONFIG, AppConstants.PATTERN_PASSWORD);
                        PrefUtils.saveStringPref(MyApplication.getAppContext(), AppConstants.GUEST_PATTERN, tryPattern);
                        PrefUtils.saveStringPref(MyApplication.getAppContext(), AppConstants.KEY_GUEST_PASSWORD, null);
                        Toast.makeText(MyApplication.getAppContext(), "Pattern Updated", Toast.LENGTH_SHORT).show();
                        //move to next
                        PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 3);
                        mListener.onPageUpdate(3);

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnConfirm.setEnabled(false);
        btnrTry.setOnClickListener(v -> {
            mTryCombo = 0;
            mCode = "";
            mPattern = "";
            msg.setText("Input PIN");
            patternLockView.setInputEnabled(true);
            codeView.clearCode();
            patternLockView.clearPattern();
            patternLockView.setNumberInputAllow(true);
            patternLockView.invalidate();
            btnConfirm.setEnabled(false);
            codeView.clearColor();
        });
        btnConfirm.setOnClickListener(v -> {
            mTryCombo++;
            patternLockView.setInputEnabled(true);
            codeView.clearCode();
            patternLockView.clearPattern();
            patternLockView.setNumberInputAllow(true);
            patternLockView.invalidate();
            btnConfirm.setEnabled(false);
            msg.setText("Confirm PIN");
        });

        patternLockView.addPatternLockListener(
                new PatternLockViewListener() {
                    @Override
                    public void onStarted() {

                    }

                    @Override
                    public void onProgress(List<PatternLockView.Dot> progressPattern) {

                    }

                    @Override
                    public void onComplete(List<PatternLockView.Dot> pattern) {
                        if (pattern.size() == 1) {
                            codeView.input(pattern.get(0).getRandom());
                            patternLockView.clearPattern();
                            return;
                        }
                        if (patternLockView.isNumberInputAllow()) {
                            patternLockView.clearPattern();
                            return;
                        }
                        if (mTryCombo == 0) {
                            mPattern = PatternLockUtils.patternToString(patternLockView, pattern);
                            patternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                            patternLockView.setInputEnabled(false);
                            btnConfirm.setEnabled(true);
                            btnrTry.setEnabled(true);
                        } else {
                            if (mPattern.equals(PatternLockUtils.patternToString(patternLockView, pattern))) {
                                //write pattern
                                patternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);

                                PrefUtils.saveStringPref(MyApplication.getAppContext(), AppConstants.GUEST_DEFAULT_CONFIG, AppConstants.COMBO_PASSWORD);
                                PrefUtils.saveStringPref(MyApplication.getAppContext(), AppConstants.GUEST_COMBO_PATTERN, mPattern);
                                PrefUtils.saveStringPref(MyApplication.getAppContext(), AppConstants.GUEST_COMBO_PIN, mCode);
                                PrefUtils.saveStringPref(MyApplication.getAppContext(), AppConstants.KEY_GUEST_PASSWORD, null);
                                PrefUtils.saveStringPref(MyApplication.getAppContext(), AppConstants.GUEST_PATTERN, null);
                                //update code here
                                PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 3);
                                mListener.onPageUpdate(3);


                            } else {
                                patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                                btnrTry.setEnabled(true);

                            }
                        }
                    }


                    @Override
                    public void onCleared() {

                    }
                }
        );
        codeView.setListener(new NCodeView.OnPFCodeListener() {
            @Override
            public void onCodeCompleted(ArrayList<Integer> code) {
                if (mTryCombo == 0) {
                    if (code.toString().equals(PrefUtils.getStringPref(MyApplication.getAppContext(), AppConstants.ENCRYPT_COMBO_PIN)) ||
                            code.toString().equals(PrefUtils.getStringPref(MyApplication.getAppContext(), AppConstants.DURESS_COMBO_PIN))) {
                        //FIXME: duplicate
                        codeView.setColor();
                    } else {
                        mCode = code.toString();
                        patternLockView.setNumberInputAllow(false);
                        patternLockView.invalidate();
                        msg.setText("Draw Pattern");
                    }


                } else {
                    if (code.toString().equals(mCode)) {
                        patternLockView.setNumberInputAllow(false);
                        patternLockView.invalidate();
                        msg.setText("Confirm Pattern");
                    } else {
                        codeView.setColor();
                    }
                }
            }

            @Override
            public void onCodeNotCompleted(ArrayList<Integer> code) {

            }
        });
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
        try {
            mListener = (OnPageUpdateListener) context;
            mContext = context;
        } catch (Exception ignored) {

        }
    }

}
