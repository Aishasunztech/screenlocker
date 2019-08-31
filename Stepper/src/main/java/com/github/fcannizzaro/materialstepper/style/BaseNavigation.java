package com.github.fcannizzaro.materialstepper.style;

import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.github.fcannizzaro.materialstepper.R;
import com.github.fcannizzaro.materialstepper.util.TintUtils;

import androidx.appcompat.widget.Toolbar;

/**
 * @author Francesco Cannizzaro (fcannizzaro).
 */
public class BaseNavigation extends BasePager implements View.OnClickListener {

    // view
    protected TextView mSkip,mPrev, mNext, mEnd, mError;
    protected ViewSwitcher mSwitch;
    Toolbar toolbar;

    @Override
    protected void init() {

        super.init();

        mSkip = (TextView) findViewById(R.id.stepPrev);
        mPrev = (TextView) findViewById(R.id.stepPrevious);
        mNext = (TextView) findViewById(R.id.stepNext);
        mEnd = (TextView) findViewById(R.id.stepEnd);
        mError = (TextView) findViewById(R.id.stepError);
        mSwitch = (ViewSwitcher) findViewById(R.id.stepSwitcher);

        assert mSwitch != null;
        mSwitch.setDisplayedChild(0);
        mSwitch.setInAnimation(BaseNavigation.this, R.anim.in_from_bottom);
        mSwitch.setOutAnimation(BaseNavigation.this, R.anim.out_to_bottom);

        // tint & color
        TintUtils.tintTextView(mSkip, tintColor);
        TintUtils.tintTextView(mNext, tintColor);

        mEnd.setTextColor(primaryColor);

        // listener
        mSkip.setOnClickListener(this);
        mNext.setOnClickListener(this);
        mEnd.setOnClickListener(this);
        mPrev.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        AbstractStep step = mSteps.getCurrent();

        if (view == mSkip) {
            step.onSkip();
            onSkip();
        } else if (view == mNext || view == mEnd) {
            step.onNext();
            onNext();
        }else if (view == mPrev){
            onPrevious();
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
    public void onPrevious() {
        if (mSteps.current() <= 0)
            return;

        mSteps.current(mSteps.current() - 1);
        onUpdate();
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
        boolean isPreviousAllow = mSteps.getCurrent().isPreviousAllow();
        boolean isFirst = mSteps.current() == 0;
        if (isLast)
            mSkip.setVisibility(View.GONE);
        else {
            mSkip.setVisibility(isSkipable ? View.VISIBLE : View.GONE);
        }
        mPrev.setVisibility(isPreviousAllow?View.VISIBLE:View.GONE);
        mNext.setVisibility(isLast ? View.GONE : View.VISIBLE);
        mEnd.setVisibility(!isLast ? View.GONE : View.VISIBLE);
        getToolbar().setTitle(mSteps.getCurrent().name());
        if (mSwitch.getDisplayedChild() != 0) mSwitch.setDisplayedChild(0);
    }
}
