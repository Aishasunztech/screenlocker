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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author Muhammad Nadeem
 * @Date 5/24/2019.
 */
public class SimViewModel extends AndroidViewModel {
    private MyAppDatabase database;
    private LiveData<List<SimEntry>> simEntries;
    public SimViewModel(@NonNull Application application) {
        super(application);
        database = MyAppDatabase.getInstance(this.getApplication());
        simEntries = database.getDao().getAllSims();
    }
    LiveData<List<SimEntry>> getAllSimEntries(){return simEntries;}
    void updateSimEntry(SimEntry sim){
        AppExecutor.getInstance().getSingleThreadExecutor().execute(()->{
            database.getDao().updateSim(sim);
        });
    }
    void insertSimEntry(SimEntry sim){
       AppExecutor.getInstance().getSingleThreadExecutor().submit(() -> database.getDao().insertSim(sim));

    }
    void deleteSimEntry(SimEntry sim){
        AppExecutor.getInstance().getSingleThreadExecutor().execute(()->{
            database.getDao().deleteSim(sim);
        });
    }
}
