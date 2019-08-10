package com.screenlocker.secure.views;

import android.graphics.Rect;
import android.text.method.TransformationMethod;
import android.view.View;

/**
 * @author Muhammad Nadeem
 * @Date 8/3/2019.
 */
public class HiddenPassTransformationMethod implements TransformationMethod {

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
