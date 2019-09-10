package com.secureMarket.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.screenlocker.secure.settings.codeSetting.installApps.ServerAppInfo;

import java.util.List;

public class SharedViwModel extends ViewModel {

    private MutableLiveData<List<ServerAppInfo>> allApps;
    private MutableLiveData<List<ServerAppInfo>> installedApps;
    private MutableLiveData<List<ServerAppInfo>> updates;

    public SharedViwModel() {
        allApps = new MutableLiveData<>();
        installedApps = new MutableLiveData<>();
        updates = new MutableLiveData<>();
    }

    public LiveData<List<ServerAppInfo>> getAllApps() {
        return allApps;
    }

    public LiveData<List<ServerAppInfo>> getInstalled() {
        return installedApps;
    }

    public LiveData<List<ServerAppInfo>> getUpdates() {
        return updates;
    }

    public void setAllApps(List<ServerAppInfo> allApps) {
        this.allApps.postValue(allApps);

    }

    public void setInstalledApps(List<ServerAppInfo> installedApps) {
        this.installedApps.setValue(installedApps);
    }

    public void setUpdates(List<ServerAppInfo> updates) {
        this.updates.setValue(updates);
    }
}
