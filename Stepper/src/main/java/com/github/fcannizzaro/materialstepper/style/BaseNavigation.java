package com.github.fcannizzaro.materialstepper.style;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.github.fcannizzaro.materialstepper.R;
import com.github.fcannizzaro.materialstepper.util.TintUtils;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

/**
 * @author Francesco Cannizzaro (fcannizzaro).
 */
public class BaseNavigation extends BasePager implements View.OnClickListener {

    // view
    protected TextView mPrev, mNext, mEnd, mError;
    protected ViewSwitcher mSwitch;
    Toolbar toolbar;

    @Override
    protected void init() {

        super.init();

        mPrev = (TextView) findViewById(R.id.stepPrev);
        mNext = (TextView) findViewById(R.id.stepNext);
        mEnd = (TextView) findViewById(R.id.stepEnd);
        mError = (TextView) findViewById(R.id.stepError);
        mSwitch = (ViewSwitcher) findViewById(R.id.stepSwitcher);

        assert mSwitch != null;
        mSwitch.setDisplayedChild(0);
        mSwitch.setInAnimation(BaseNavigation.this, R.anim.in_from_bottom);
        mSwitch.setOutAnimation(BaseNavigation.this, R.anim.out_to_bottom);

        // tint & color
        TintUtils.tintTextView(mPrev, tintColor);
        TintUtils.tintTextView(mNext, tintColor);

        mEnd.setTextColor(primaryColor);

        // listener
        mPrev.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mEnd.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        AbstractStep step = mSteps.getCurrent();

        if (view == mPrev) {
            if (step.isSkipable()) {
                step.onSkip();
                onSkip();
            } else if (step.isPreviousAllow()) {
                step.onPrevious();
                onPrevious();
            }

        } else if (view == mNext || view == mEnd) {
            step.onNext();
            onNext();
        }

    }

    @Override
    public void onSkip() {
        boolean isLast = mSteps.current() == mSteps.total() - 1;
        if (!isLast) {
            if (mSteps.current() == 5) {
                mSteps.current(7);
            } else
                mSteps.current(mSteps.current() + 1);
            onUpdate();
        }
    }


    @Override
    public void onError() {
        mError.setText(Html.fromHtml(mErrorString));
        if (mSwitch.getDisplayedChild() == 0)
            mSwitch.setDisplayedChild(1);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSwitch.getDisplayedChild() == 1) mSwitch.setDisplayedChild(0);
            }
        }, getErrorTimeout() + 300);

    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        boolean isLast = mSteps.current() == mSteps.total() - 1;
        boolean isSkipable = mSteps.getCurrent().isSkipable();
        boolean isFirst = mSteps.current() == 0;

        if (isLast)
            mPrev.setVisibility(View.GONE);
        else
            mPrev.setVisibility(isSkipable ? View.VISIBLE : View.GONE);
        mNext.setVisibility(isLast ? View.GONE : View.VISIBLE);
        mEnd.setVisibility(!isLast ? View.GONE : View.VISIBLE);

        if (!isSkipable) {
            boolean isPreviouse = mSteps.getCurrent().isPreviousAllow();
            if (isPreviouse) {
                mPrev.setVisibility(View.VISIBLE);
                mPrev.setText(R.string.ms_prev1);
//                Drawable drawable = changeDrawableColor(getBaseContext(), R.drawable.ic_keyboard_arrow_left_black_24dp, Color.WHITE);
//                mPrev.setCompoundDrawables(drawable, null, null, null);
//
            } else {
                mPrev.setVisibility(View.GONE);
                mPrev.setText(R.string.ms_prev);
            }
        } else {
            mPrev.setVisibility(View.VISIBLE);
            mPrev.setText(R.string.ms_prev);
        }

        getToolbar().setTitle(mSteps.getCurrent().name());
        if (mSwitch.getDisplayedChild() != 0) mSwitch.setDisplayedChild(0);
    }


//    public static Drawable changeDrawableColor(Context context, int icon, int newColor) {
//        Drawable mDrawable = ContextCompat.getDrawable(context, icon).mutate();
//        mDrawable.setColorFilter(new PorterDuffColorFilter(newColor, PorterDuff.Mode.SRC_IN));
//        return mDrawable;
//    }


}
