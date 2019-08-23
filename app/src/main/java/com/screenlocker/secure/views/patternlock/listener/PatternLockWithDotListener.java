package com.screenlocker.secure.views.patternlock.listener;

import com.screenlocker.secure.views.patternlock.PatternLockWithDotsOnly;

import java.util.List;

/**
 * @author Muhammad Nadeem
 * @Date 8/21/2019.
 */

/**
 * The callback interface for detecting patterns entered by the user
 */
public interface PatternLockWithDotListener {

    /**
     * Fired when the pattern drawing has just started
     */
    void onStarted();

    /**
     * Fired when the pattern is still being drawn and progressed to
     * one more {@link com.secure.patterntest;}
     */
    void onProgress(List<PatternLockWithDotsOnly.Dot> progressPattern);

    /**
     * Fired when the user has completed drawing the pattern and has moved their finger away
     * from the view
     */
    void onComplete(List<PatternLockWithDotsOnly.Dot> pattern);

    /**
     * Fired when the patten has been cleared from the view
     */
    void onCleared();
}
