package com.screenlocker.secure.permissions;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.widget.NestedScrollView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.screenlocker.secure.socket.utils.utils.passwordsOk;
import static com.screenlocker.secure.utils.AppConstants.DEF_PAGE_NO;
import static com.screenlocker.secure.utils.AppConstants.ENCRYPT_PASSORD_OPTION;
import static com.screenlocker.secure.utils.AppConstants.GUEST_PASSORD_OPTION;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.OPTION_PATTERN;
import static com.screenlocker.secure.utils.AppConstants.OPTION_PIN;
import static com.screenlocker.secure.utils.Utils.dpToPx;

public class SetEncryptedPasswordFragment extends AbstractStep {
    private String error = "";
    private int mTry = 0;
    private String tryPattern;
    private OnPageUpdateListener mListener;

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
                PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 5);

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
        switch (PrefUtils.getIntegerPref(MyApplication.getAppContext(), ENCRYPT_PASSORD_OPTION)) {
            case OPTION_PIN:
//                viewSwitcher.setDisplayedChild(1);
                pin_container.setVisibility(View.VISIBLE);
                pattern_container.setVisibility(View.GONE);
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
//                viewSwitcher.setDisplayedChild(0);
                pin_container.setVisibility(View.GONE);
                pattern_container.setVisibility(View.VISIBLE);
        }
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

    //    @BindView(R.id.view_switcher)
//    ViewSwitcher viewSwitcher;
    @BindView(R.id.pattern_container)
    LinearLayout pattern_container;
    @BindView(R.id.pin_container)
    NestedScrollView pin_container;
    @BindView(R.id.password_container)
    LinearLayout password_container;
    @BindView(R.id.main_layout)
    RelativeLayout main_layout;


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
        img_picture2.setImageDrawable(getResources().getDrawable(R.drawable.ic_encrypted_third));

        //Move when keyboard is shown
        main_layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();

                main_layout.getWindowVisibleDisplayFrame(r);

                int heightDiff = main_layout.getRootView().getHeight() - (r.bottom - r.top);
                if (heightDiff > 100) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) password_container.getLayoutParams();
                    params.bottomMargin = dpToPx(150);
                    password_container.setLayoutParams(params);
                }
            }
        });

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
                    if (tryPattern.equals(PrefUtils.getStringPref(MyApplication.getAppContext(), AppConstants.GUEST_PATTERN)) || tryPattern.equals(PrefUtils.getStringPref(MyApplication.getAppContext(), AppConstants.DURESS_PATTERN))) {
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
                        PrefUtils.saveStringPref(MyApplication.getAppContext(), AppConstants.ENCRYPT_DEFAULT_CONFIG, AppConstants.PATTERN_PASSWORD);
                        PrefUtils.saveStringPref(MyApplication.getAppContext(), AppConstants.ENCRYPT_PATTERN, tryPattern);
                        PrefUtils.saveStringPref(MyApplication.getAppContext(), KEY_MAIN_PASSWORD, null);
                        Toast.makeText(MyApplication.getAppContext(), "Pattern Updated", Toast.LENGTH_SHORT).show();
                        //move to next
                        PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 5);
                        mListener.onPageUpdate(5);

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
        try {
            mListener = (OnPageUpdateListener) context;
            mContext = context;
        } catch (Exception ignored) {

        }
    }
}

