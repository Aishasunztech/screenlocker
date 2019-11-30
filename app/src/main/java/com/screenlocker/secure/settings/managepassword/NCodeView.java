package com.screenlocker.secure.settings.managepassword;

import android.content.Context;
import androidx.annotation.Nullable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.secure.launcher.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Muhammad Nadeem
 * @Date 9/18/2019.
 */


/**
 * Created by Aleksandr Nikiforov on 2018/02/07.
 */

public class NCodeView extends LinearLayout {

    private static final int DEFAULT_CODE_LENGTH = 4;
    List<CheckBox> mCodeViews = new ArrayList<>();
    private ArrayList<Integer> mCode = new ArrayList<>(DEFAULT_CODE_LENGTH);
    private int mCodeLength = DEFAULT_CODE_LENGTH;
    private OnPFCodeListener mListener;


    public void setColor(){
        for (CheckBox mCodeView : mCodeViews) {
            mCodeView.setButtonDrawable(R.drawable.code_selector_wrong);
        }
    }
    public void clearColor(){
        for (CheckBox mCodeView : mCodeViews) {
            mCodeView.setButtonDrawable(R.drawable.code_selector_n);
        }
    }

    public NCodeView(Context context) {
        super(context);
        init();
    }

    public NCodeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NCodeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_code_lockscreen, this);
        setUpCodeViews();
    }

    public void setCodeLength(int codeLength) {
        mCodeLength = codeLength;
        setUpCodeViews();
    }

    private void setUpCodeViews() {
        removeAllViews();
        mCodeViews.clear();
        mCode.clear();
        for (int i = 0; i < mCodeLength; i++) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            CheckBox view = (CheckBox) inflater.inflate(R.layout.view_code_checkbox, null);

            LinearLayout.LayoutParams layoutParams = new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int margin = getResources().getDimensionPixelSize(R.dimen.code_margin);
            layoutParams.setMargins(margin, margin, margin, margin);
            view.setLayoutParams(layoutParams);
            view.setChecked(false);
            addView(view);
            mCodeViews.add(view);
        }
        if (mListener != null) {
            mListener.onCodeNotCompleted(null);
        }
    }

    public int input(int number) {
        if (mCode.size() == mCodeLength) {
            return mCode.size();
        }
        mCodeViews.get(mCode.size()).toggle(); //.setChecked(true);
        mCode.add(number);
        if (mCode.size() == mCodeLength && mListener != null) {
            mListener.onCodeCompleted(mCode);
        }
        return mCode.size();
    }

    public int delete() {
        if (mListener != null) {
            mListener.onCodeNotCompleted(mCode);
        }
        if (mCode.size() == 0) {
            return mCode.size();
        }
        mCode.remove(mCode.size()-1);
        mCodeViews.get(mCode.size()).toggle();  //.setChecked(false);
        return mCode.size();
    }

    public void clearCode() {
        if (mListener != null) {
            mListener.onCodeNotCompleted(mCode);
        }
        mCode.clear();
        for (CheckBox codeView : mCodeViews) {
            codeView.setChecked(false);
        }
    }

    public int getInputCodeLength() {
        return mCode.size();
    }

    public ArrayList<Integer> getCode() {
        return mCode;
    }

    public void setListener(OnPFCodeListener listener) {
        mListener = listener;
    }

    public interface OnPFCodeListener {

        void onCodeCompleted(ArrayList<Integer> code);

        void onCodeNotCompleted(ArrayList<Integer> code);

    }
}
