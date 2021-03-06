package com.screenlocker.secure.launcher;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.screenlocker.secure.room.MyAppDatabase;

import java.util.List;

/**
 * @author Muhammad Nadeem
 * @Date 7/10/2019.
 */
public class MainViewModel extends AndroidViewModel {

    private LiveData<List<AppInfo>> mAppInfos;
    private LiveData<Integer> mUnReadCount;

    public MainViewModel(@NonNull Application application) {
        super(application);
        mAppInfos = MyAppDatabase.getInstance(application).getDao().getAllApps();
        mUnReadCount = MyAppDatabase.getInstance(application).getDao().getUnSeenCount();
    }

    LiveData<List<AppInfo>> getAllApps() {
        return mAppInfos;
    }

    public LiveData<Integer> getmUnReadCount() {
        return mUnReadCount;
    }
}
