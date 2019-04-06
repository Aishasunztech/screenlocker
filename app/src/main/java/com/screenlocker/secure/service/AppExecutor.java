package com.screenlocker.secure.service;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutor {


    private Executor singleThreadExecutor;
    private Executor mainThread;
    private static AppExecutor appExecutor;
    private ExecutorService secondSingleThreadExecutor;


    public static AppExecutor getInstance() {
        if (appExecutor == null) {
            appExecutor = new AppExecutor();
        }
        return appExecutor;
    }

    private AppExecutor() {
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        secondSingleThreadExecutor = Executors.newFixedThreadPool(3);
        mainThread = new MainThreadExecutor();

    }

    public Executor getSingleThreadExecutor() {
        return singleThreadExecutor;
    }

    public ExecutorService getSecondSingleThreadExecutor() {
        return secondSingleThreadExecutor;
    }

    public Executor getMainThread() {
        return mainThread;
    }


    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}