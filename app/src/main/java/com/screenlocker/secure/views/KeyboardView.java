package com.screenlocker.secure.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;

import androidx.annotation.IdRes;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.screenlocker.secure.utils.PrefUtils;
import com.secure.launcher.R;

import com.screenlocker.secure.socket.interfaces.RefreshListener;

import java.util.Random;

import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;

public class KeyboardView extends LinearLayout implements View.OnClickListener, RefreshListener {

    private EditText mPasswordField;
    private TextView txtWarning;
    private PrefUtils prefUtils;

    public KeyboardView(Context context) {
        super(context);
        init();
    }

    public KeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.keyboard, this);

        initViews();
    }

    private void initViews() {

        mPasswordField = $(R.id.password_field);
        prefUtils = PrefUtils.getInstance(getContext());
        txtWarning = $(R.id.txtWarning);

        $(R.id.t9_key_clear).setOnClickListener(this);

        $(R.id.t9_key_backspace).setOnClickListener(this);

        TextView k0 = $(R.id.t9_key_0),
                k1 = $(R.id.t9_key_1),
                k2 = $(R.id.t9_key_2),
                k3 = $(R.id.t9_key_3),
                k4 = $(R.id.t9_key_4),
                k5 = $(R.id.t9_key_5),
                k6 = $(R.id.t9_key_6),
                k7 = $(R.id.t9_key_7),
                k8 = $(R.id.t9_key_8),
                k9 = $(R.id.t9_key_9);

        int[] arr = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        setUpRandomizedArray(arr);

        k0.setText(String.valueOf(arr[0]));
        k1.setText(String.valueOf(arr[1]));
        k2.setText(String.valueOf(arr[2]));
        k3.setText(String.valueOf(arr[3]));
        k4.setText(String.valueOf(arr[4]));
        k5.setText(String.valueOf(arr[5]));
        k6.setText(String.valueOf(arr[6]));
        k7.setText(String.valueOf(arr[7]));
        k8.setText(String.valueOf(arr[8]));
        k9.setText(String.valueOf(arr[9]));

        k0.setOnClickListener(this);
        k1.setOnClickListener(this);
        k2.setOnClickListener(this);
        k3.setOnClickListener(this);
        k4.setOnClickListener(this);
        k5.setOnClickListener(this);
        k6.setOnClickListener(this);
        k7.setOnClickListener(this);
        k8.setOnClickListener(this);
        k9.setOnClickListener(this);

//        $(R.id.t9_key_9).setOnClickListener(this);

        mPasswordField.setTransformationMethod(new HiddenPassTransformationMethod());

        mPasswordField.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);


        mPasswordField.setTextColor(getResources().getColor(R.color.textColorPrimary));


        mPasswordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String device_status = prefUtils.getStringPref( DEVICE_STATUS);
                if (device_status == null) {
                    txtWarning.setVisibility(INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void setUpRandomizedArray(int[] arr) {

        for (int i = 0; i < 9; ++i) {

            Random r = new Random();
            int pos = i + r.nextInt(9 - i);

            int temp = arr[i];
            arr[i] = arr[pos];
            arr[pos] = temp;
        }
    }


    @Override
    public void onClick(View v) {
        // handle number button click
        if (v.getTag() != null && "number_button".equals(v.getTag())) {
            mPasswordField.append(((TextView) v).getText());
            return;
        }
        switch (v.getId()) {
            case R.id.t9_key_clear: { // handle clear button
                setPassword(null);
            }
            break;
            case R.id.t9_key_backspace: { // handle backspace button
                // delete one character
                Editable editable = mPasswordField.getText();
                int charCount = editable.length();
                if (charCount > 0) {
                    editable.delete(charCount - 1, charCount);
                }
            }
            break;
        }
    }


    public void setPassword(String password) {
        mPasswordField.setText(password);
    }

    //    Enableclear
    public void enableEditText() {
        mPasswordField.setFocusableInTouchMode(true);
        mPasswordField.setFocusable(true);
        mPasswordField.setEnabled(true);
    }


    //    Disable
    public void disableEditText() {
        mPasswordField.setFocusable(false);
        mPasswordField.setEnabled(false);
        mPasswordField.setCursorVisible(false);
        mPasswordField.setKeyListener(null);
        mPasswordField.setBackgroundColor(Color.TRANSPARENT);
    }

    public void clearWaringText() {
        txtWarning.setVisibility(INVISIBLE);
        txtWarning.setText(null);
    }

    public void setWarningText(String msg) {
        txtWarning.setVisibility(VISIBLE);

//        if(!msg.equals(getResources().getString(R.string.wrong_password_try_again))){
//            txtWarning.setBackgroundResource(R.drawable.error_msg);
//        }



        txtWarning.setText(msg);


    }

    public String getInputText() {
        return mPasswordField.getText().toString();
    }

    private <T extends View> T $(@IdRes int id) {
        return (T) super.findViewById(id);
    }

    @Override
    public void onSwipe() {

    }


    private class HiddenPassTransformationMethod implements TransformationMethod {

        private char DOT = '\u26AA';


        @Override
        public CharSequence getTransformation(final CharSequence charSequence, final View view) {
            return new PassCharSequence(charSequence);
        }

        @Override
        public void onFocusChanged(final View view, final CharSequence charSequence, final boolean b, final int i,
                                   final Rect rect) {
            //nothing to do here
        }

        private class PassCharSequence implements CharSequence {

            private final CharSequence charSequence;

            PassCharSequence(final CharSequence charSequence) {
                this.charSequence = charSequence;
            }

            @Override
            public char charAt(final int index) {
                return DOT;
            }

            @Override
            public int length() {
                return charSequence.length();
            }

            @Override
            public CharSequence subSequence(final int start, final int end) {
                return new PassCharSequence(charSequence.subSequence(start, end));
            }
        }
    }

}
