package com.screenlocker.secure.service;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public class AppExecutor {


    private ExecutorService singleThreadExecutor;
    private ExecutorService executorForUpdatingList;
    private ScheduledExecutorService singleScheduleThreadExecutor;
    private Executor mainThread;
    private static AppExecutor appExecutor;
    private ExecutorService executorForSedulingRecentAppKill;


    public static AppExecutor getInstance() {
        if (appExecutor == null) {
            appExecutor = new AppExecutor();
        }
        return appExecutor;
    }

    private AppExecutor() {
        executorForUpdatingList = Executors.newSingleThreadExecutor();
        singleThreadExecutor = Executors.newSingleThreadExecutor();
        executorForSedulingRecentAppKill = Executors.newSingleThreadExecutor();
        mainThread = new MainThreadExecutor();
        singleScheduleThreadExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            private ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();

            @Override
            public Thread newThread(Runnable r) {
                Thread t = defaultThreadFactory.newThread(r);
                t.setPriority(Thread.NORM_PRIORITY);
                return t;
            };
        });

    }

    public ExecutorService getSingleThreadExecutor() {
        return singleThreadExecutor;
    }

    public ExecutorService getExecutorForSedulingRecentAppKill() {
        return executorForSedulingRecentAppKill;
    }

    public Executor getMainThread() {
        return mainThread;
    }

    public ScheduledExecutorService getSingleScheduleThreadExecutor() {
        return singleScheduleThreadExecutor;
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
    public void readyNewExecutor(){
        executorForSedulingRecentAppKill = Executors.newSingleThreadExecutor();

    }
    public ExecutorService getExecutorForUpdatingList() {
        return executorForUpdatingList;
    }
    public void prepareExecutorForUpdatingList() {
         executorForUpdatingList = Executors.newSingleThreadExecutor();
    }
    public void prepareSingleScheduleThreadExecutor() {
        singleScheduleThreadExecutor = Executors.newSingleThreadScheduledExecutor(
                 new ThreadFactory() {
                     private ThreadFactory defaultThreadFactory = Executors.defaultThreadFactory();

                     @Override
                     public Thread newThread(Runnable r) {
                         Thread t = defaultThreadFactory.newThread(r);
                         t.setPriority(Thread.NORM_PRIORITY);
                         return t;
                     };
                 }
         );
    }
}