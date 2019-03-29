package com.vortexlocker.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BlockStatusBar {
    Context context;

    // To keep track of activity's window focus
    boolean currentFocus;
    // To keep track of activity's foreground/background status
    boolean isPaused;

    public static Handler collapseNotificationHandler;
    Method collapseStatusBar = null;


    public BlockStatusBar(Context context, boolean isPaused) {
        this.context = context;
        this.isPaused = isPaused;
        collapseNow();
    }

    public void collapseNow() {

        // Initialize 'collapseNotificationHandler'
        if (collapseNotificationHandler == null) {
            collapseNotificationHandler = new Handler();
        }

        // If window focus has been lost && activity is not in a paused state
        // Its a valid check because showing of notification panel
        // steals the focus from current activity's window, but does not
        // 'pause' the activity
        if (!currentFocus && !isPaused) {

            // Post a Runnable with some delay - currently set to 300 ms
            collapseNotificationHandler.postDelayed(new Runnable() {

                @SuppressLint("PrivateApi")
                @Override
                public void run() {

                    // Use reflection to trigger a method from 'StatusBarManager'

                    @SuppressLint("WrongConstant") Object statusBarService = context.getSystemService("statusbar");
                    Class<?> statusBarManager = null;

                    try {
                        statusBarManager = Class.forName("android.app.StatusBarManager");
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }


                    try {

                        // Prior to API 17, the method to call is 'collapse()'
                        // API 17 onwards, the method to call is `collapsePanels()`

                        if (statusBarManager != null) {
                            collapseStatusBar = statusBarManager.getMethod("collapsePanels");
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                    collapseStatusBar.setAccessible(true);

                    try {
                        collapseStatusBar.invoke(statusBarService);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }

                    // Check if the window focus has been returned
                    // If it hasn't been returned, post this Runnable again
                    // Currently, the delay is 100 ms. You can change this
                    // value to suit your needs.
                    if (!currentFocus && !isPaused) {
                        collapseNotificationHandler.postDelayed(this, 100L);
                    }

                    if (!currentFocus && isPaused) {
                        collapseNotificationHandler.removeCallbacksAndMessages(null);
                    }

                }
            }, 1L);
        }
    }
}