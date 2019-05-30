package com.screenlocker.secure.settings.codeSetting.Sim;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.room.MyAppDatabase;
import com.screenlocker.secure.room.SimEntry;
import com.screenlocker.secure.service.AppExecutor;

import java.util.List;

/**
 * @author Muhammad Nadeem
 * @Date 5/24/2019.
 */
public class SimViewModel extends AndroidViewModel {
    private MyAppDatabase database;
    private LiveData<List<SimEntry>> simEntries;
    public SimViewModel(@NonNull Application application) {
        super(application);
        database = MyApplication.getAppDatabase(this.getApplication());
        simEntries = database.getDao().getAllSims();
    }
    public LiveData<List<SimEntry>> getAllSimEntries(){return simEntries;}
    public void updateSimEntry(SimEntry sim){
        AppExecutor.getInstance().getSingleThreadExecutor().execute(()->{
            database.getDao().updateSim(sim);
        });
    }
    public void insertSimEntry(SimEntry sim){
        AppExecutor.getInstance().getSingleThreadExecutor().execute(()->{
            database.getDao().insertSim(sim);
        });
    }
    public void deleteSimEntry(SimEntry sim){
        AppExecutor.getInstance().getSingleThreadExecutor().execute(()->{
            database.getDao().deleteSim(sim);
        });
    }
}
