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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.settings.managepassword.NCodeView;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.SecuredSharedPref;
import com.screenlocker.secure.utils.Validator;
import com.screenlocker.secure.views.patternlock.PatternLockView;
import com.screenlocker.secure.views.patternlock.PatternLockWithDotsOnly;
import com.screenlocker.secure.views.patternlock.listener.PatternLockViewListener;
import com.screenlocker.secure.views.patternlock.listener.PatternLockWithDotListener;
import com.screenlocker.secure.views.patternlock.utils.PatternLockUtils;
import com.secure.launcher.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.screenlocker.secure.permissions.SteppersActivity.STEP_GUEST_PASS;
import static com.screenlocker.secure.socket.utils.utils.passwordsOk;
import static com.screenlocker.secure.utils.AppConstants.COMBO_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.DEF_PAGE_NO;
import static com.screenlocker.secure.utils.AppConstants.GUEST_COMBO_PATTERN;
import static com.screenlocker.secure.utils.AppConstants.GUEST_COMBO_PIN;
import static com.screenlocker.secure.utils.AppConstants.GUEST_DEFAULT_CONFIG;
import static com.screenlocker.secure.utils.AppConstants.GUEST_PASSORD_OPTION;
import static com.screenlocker.secure.utils.AppConstants.GUEST_PATTERN;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.OPTION_COMBO;
import static com.screenlocker.secure.utils.AppConstants.OPTION_PATTERN;
import static com.screenlocker.secure.utils.AppConstants.OPTION_PIN;

public class SetGuestPasswordFragment extends AbstractStep {
    private volatile String error = "";
    private Context mContext;
    private int mTry = 0;
    private String tryPattern;
    private String mCode;
    private String mPattern;
    private int mTryCombo = 0;
    private PrefUtils prefUtils;

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
    @BindView(R.id.confirm)
    Button btnPatternConfirm;
    @BindView(R.id.cancel)
    Button btnPatternCancel;

    private SecuredSharedPref sharedPref ;


    private boolean isAllowed = false;

    @Override
    public String name() {
        return MyApplication.getAppContext().getResources().getString(R.string.guest_password);
    }

    @Override
    public void onStepVisible() {
        super.onStepVisible();
        switch (prefUtils.getIntegerPref( GUEST_PASSORD_OPTION)) {
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
                responsTitle.setText(MyApplication.getAppContext().getResources().getString(R.string.please_draw_pattern));
                patternLock.setEnableHapticFeedback(false);
                viewSwitcher.setDisplayedChild(0);
                btnPatternCancel.setEnabled(false);
                btnPatternConfirm.setEnabled(false);

                break;
            case OPTION_COMBO:
                codeView.clearCode();
                mTryCombo = 0;
                mCode = null;
                mPattern = null;
                msg.setText(MyApplication.getAppContext().getResources().getString(R.string.input_pin));
                patternLock.setEnableHapticFeedback(false);
                patternLockView.setEnableHapticFeedback(false);
                patternLockView.setNumberInputAllow(true);
                patternLockView.invalidate();
                viewSwitcher.setDisplayedChild(2);
                break;


        }
    }

    @Override
    public boolean nextIf() {
        switch (prefUtils.getIntegerPref( GUEST_PASSORD_OPTION)) {
            case OPTION_PIN:
                if (setPassword()) {

                    if (sharedPref.getStringPref(KEY_GUEST_PASSWORD) != null) {
                        prefUtils.saveIntegerPref( DEF_PAGE_NO, STEP_GUEST_PASS);
                        return true;
                    }
                }
                break;
            case OPTION_PATTERN:
            case OPTION_COMBO:
                if (sharedPref.getStringPref(KEY_GUEST_PASSWORD) != null || isAllowed) {
                    prefUtils.saveIntegerPref( DEF_PAGE_NO, STEP_GUEST_PASS);
                    return true;
                }
                break;


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
        prefUtils = prefUtils.getInstance(MyApplication.getAppContext());
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
                    Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getResources().getString(R.string.pattern_is_too_short), Toast.LENGTH_SHORT).show();
                    patternLock.setViewMode(PatternLockWithDotsOnly.PatternViewMode.WRONG);
                    new Handler().postDelayed(patternLock::clearPattern, 500);
                    return;
                }
                if (mTry == 0) {
                    tryPattern = PatternLockUtils.patternToString(patternLock, pattern);
                    if (tryPattern.equals(sharedPref.getStringPref(AppConstants.ENCRYPT_PATTERN)) || tryPattern.equals(sharedPref.getStringPref(AppConstants.DURESS_PATTERN))) {
                        Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getResources().getString(R.string.pattern_already_aken), Toast.LENGTH_SHORT).show();
                        patternLock.setViewMode(PatternLockWithDotsOnly.PatternViewMode.WRONG);
                        new Handler().postDelayed(() -> {
                            patternLock.clearPattern();
                        }, 150);
                        return;
                    }
                    patternLock.setViewMode(PatternLockWithDotsOnly.PatternViewMode.CORRECT);
                    patternLock.setInputEnabled(false);
                    btnPatternCancel.setEnabled(true);
                    btnPatternConfirm.setEnabled(true);

                } else if (mTry == 1) {
                    if (tryPattern.equals(PatternLockUtils.patternToString(patternLock, pattern))) {

                        patternLock.setViewMode(PatternLockWithDotsOnly.PatternViewMode.CORRECT);
                        sharedPref.saveStringPref(GUEST_DEFAULT_CONFIG, AppConstants.PATTERN_PASSWORD);
                        sharedPref.saveStringPref(GUEST_PATTERN, tryPattern);
                        sharedPref.saveStringPref(KEY_GUEST_PASSWORD, null);
                        Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getResources().getString(R.string.pattern_updated), Toast.LENGTH_SHORT).show();
                        //move to next
                        prefUtils.saveIntegerPref( DEF_PAGE_NO, STEP_GUEST_PASS);
//                        mListener.onPageUpdate(STEP_GUEST_PASS);
                        patternLock.setInputEnabled(false);
                        btnPatternCancel.setEnabled(false);
                        btnPatternConfirm.setEnabled(false);
                        isAllowed = true;
                    }
                    //wrong pattern
                    else {
                        mTry = 0;
                        Toast.makeText(MyApplication.getAppContext(), MyApplication.getAppContext().getResources().getString(R.string.pattern_did_not_match), Toast.LENGTH_SHORT).show();
                        patternLock.setViewMode(PatternLockWithDotsOnly.PatternViewMode.WRONG);
                        new Handler().postDelayed(() -> patternLock.clearPattern(), 500);
                        responsTitle.setText(MyApplication.getAppContext().getResources().getString(R.string.please_draw_pattern));
                        btnPatternConfirm.setEnabled(false);
                        btnPatternCancel.setEnabled(false);

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
        sharedPref = SecuredSharedPref.getInstance(MyApplication.getAppContext());
        btnConfirm.setEnabled(false);
        btnPatternCancel.setOnClickListener(v -> {
            mTry = 0;
            tryPattern = "";
            responsTitle.setText(MyApplication.getAppContext().getResources().getString(R.string.please_draw_pattern));
            patternLock.clearPattern();
            patternLock.setInputEnabled(true);
            btnPatternCancel.setEnabled(false);
            btnPatternConfirm.setEnabled(false);
        });
        btnPatternConfirm.setOnClickListener(v -> {
            mTry = 1;
            patternLock.clearPattern();
            patternLock.setInputEnabled(true);
            btnPatternConfirm.setEnabled(false);
            responsTitle.setText(MyApplication.getAppContext().getResources().getString(R.string.confirm_pattern));
        });
        btnrTry.setOnClickListener(v -> {
            mTryCombo = 0;
            mCode = "";
            mPattern = "";
            msg.setText(MyApplication.getAppContext().getResources().getString(R.string.input_pin));
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
            msg.setText(MyApplication.getAppContext().getResources().getString(R.string.confirm_pattern));
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

                                sharedPref.saveStringPref(GUEST_DEFAULT_CONFIG, COMBO_PASSWORD);
                                sharedPref.saveStringPref(GUEST_COMBO_PATTERN, mPattern);
                                sharedPref.saveStringPref(GUEST_COMBO_PIN, mCode);
                                sharedPref.saveStringPref(KEY_GUEST_PASSWORD, null);
                                sharedPref.saveStringPref(GUEST_PATTERN, null);
                                //update code here
                                isAllowed = true;
//                                prefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, STEP_GUEST_PASS);
//                                mListener.onPageUpdate(STEP_GUEST_PASS);
                                Timber.d("onComplete: ");


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
                    if (code.toString().equals(prefUtils.getStringPref( AppConstants.ENCRYPT_COMBO_PIN)) ||
                            code.toString().equals(prefUtils.getStringPref( AppConstants.DURESS_COMBO_PIN))) {
                        codeView.setColor();
                    } else {
                        mCode = code.toString();
                        patternLockView.setNumberInputAllow(false);
                        patternLockView.invalidate();
                        msg.setText(MyApplication.getAppContext().getResources().getString(R.string.draw_pattern));
                    }


                } else {
                    if (code.toString().equals(mCode)) {
                        patternLockView.setNumberInputAllow(false);
                        patternLockView.invalidate();
                        msg.setText(MyApplication.getAppContext().getResources().getString(R.string.confirm_pattern));
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
                sharedPref.saveStringPref(KEY_GUEST_PASSWORD, reEnteredPassword);
                isAllowed = true;
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
            mContext = context;
        } catch (Exception ignored) {

        }
    }
}
