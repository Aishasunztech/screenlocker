package com.screenlocker.secure;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BlockStatusBar {

    // To keep track of activity's window focus
    // To keep track of activity's foreground/background status
    private boolean isPaused;

    private Handler collapseNotificationHandler;
    private Method collapseStatusBar = null;
    private Object statusBarService;


    @SuppressLint("WrongConstant")
    public BlockStatusBar(Context context, boolean isPaused) {

         this.statusBarService = context.getSystemService("statusbar");
        this.isPaused = isPaused;

    }

    public void collapseNow(boolean stop) {

        // Initialize 'collapseNotificationHandler'


        if (stop) {
            if (collapseNotificationHandler != null)
                collapseNotificationHandler.removeCallbacksAndMessages(null);
            collapseNotificationHandler = null;
            return;
        }

        if (collapseNotificationHandler == null) {
            collapseNotificationHandler = new Handler();
        }


        // If window focus has been lost && activity is not in a paused state
        // Its a valid check because showing of notification panel
        // steals the focus from current activity's window, but does not
        // 'pause' the activity
        if (!isPaused) {

            // Post a Runnable with some delay - currently set to 300 ms
            collapseNotificationHandler.postDelayed(new Runnable() {

                @SuppressLint("PrivateApi")
                @Override
                public void run() {

                    // Use reflection to trigger a method from 'StatusBarManager'


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
                    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }

                    // Check if the window focus has been returned
                    // If it hasn't been returned, post this Runnable again
                    // Currently, the delay is 100 ms. You can change this
                    // value to suit your needs.
                    if (!isPaused) {
                        collapseNotificationHandler.postDelayed(this, 100L);
                    }

                    if (isPaused) {
                        collapseNotificationHandler.removeCallbacksAndMessages(null);
                    }

                }
            }, 300L);
        }
    }

}