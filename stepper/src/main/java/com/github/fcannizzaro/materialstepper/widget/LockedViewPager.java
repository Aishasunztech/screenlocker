package com.github.fcannizzaro.materialstepper.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

/**
 * Non-swipeable ViewPager
 * Created by alashov on 07/05/16.
 */
public class LockedViewPager extends ViewPager {

    public LockedViewPager(Context context) {
        super(context);
    }

    public LockedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Disable swipe
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Disable swipe
        return false;
    }


}
