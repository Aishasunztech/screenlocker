package com.screenlocker.secure.launcher.subsettings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.room.MyAppDatabase;
import com.screenlocker.secure.room.SubExtension;

import java.util.List;

/**
 * @author : Muhammad Nadeem
 * Created at: 1/24/2020
 */
public class SSettingsViewModel extends AndroidViewModel {

    LiveData<List<SubExtension>> subExtensions;

    public SSettingsViewModel(@NonNull Application application) {
        super(application);
        subExtensions = MyAppDatabase.getInstance(application).getDao().getExtensions();
    }

    public LiveData<List<SubExtension>> getSubExtensions() {
        return subExtensions;
    }
}
