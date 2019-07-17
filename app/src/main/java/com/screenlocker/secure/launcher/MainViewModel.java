package com.screenlocker.secure.launcher;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.screenlocker.secure.app.MyApplication;

import java.util.List;

/**
 * @author Muhammad Nadeem
 * @Date 7/10/2019.
 */
public class MainViewModel extends AndroidViewModel {

    private LiveData<List<AppInfo>> mAppInfos;

    public MainViewModel(@NonNull Application application) {
        super(application);
        mAppInfos = MyApplication.getAppDatabase(application).getDao().getAllApps();
    }

    LiveData<List<AppInfo>> getAllApps() {
        return mAppInfos;
    }
}
