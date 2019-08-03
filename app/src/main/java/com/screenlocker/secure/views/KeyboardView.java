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

import com.screenlocker.secure.R;
import com.screenlocker.secure.socket.interfaces.RefreshListener;

import java.util.Random;

import static com.screenlocker.secure.socket.utils.utils.getDeviceStatus;

public class KeyboardView extends LinearLayout implements View.OnClickListener, RefreshListener {
    private TextView btnUnlock;
    private OnKeyClickListener mListener;

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

        btnUnlock = $(R.id.t9_unlock);




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
//            mPasswordField.append(((TextView) v).getText());
            if (mListener != null) {
                mListener.onKeyClick(((TextView) v).getText().toString());
            }
        }
    }



    private <T extends View> T $(@IdRes int id) {
        return (T) super.findViewById(id);
    }

    @Override
    public void onSwipe() {

    }




    public void setOnKeyClickListener(OnKeyClickListener listener) {
        mListener = listener;
    }

    public void setOnUnlockButtonClickListener(OnUnlockButtonClickListener listener) {
        btnUnlock.setOnClickListener(listener);
    }

}