package com.screenlocker.secure.settings.notification;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.room.MyAppDatabase;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.socket.model.DeviceMessagesModel;

import java.util.List;

/**
 * @author : Muhammad Nadeem
 * Created at: 1/13/2020
 */
public class NotificationViewModel extends AndroidViewModel {
    private LiveData<List<DeviceMessagesModel>> msgLists;
    private MyAppDatabase database;
    private LiveData<Integer> unReadCount;
    public NotificationViewModel(@NonNull Application application) {
        super(application);
        database = MyAppDatabase.getInstance(application);
        msgLists = database.getDao().getLiveMessage();
        unReadCount = database.getDao().getUnSeenCount();
    }

    public LiveData<List<DeviceMessagesModel>> getMsgLists() {
        return msgLists;
    }

    public LiveData<Integer> getUnReadCount() {
        return unReadCount;
    }

    public void updateMessage(DeviceMessagesModel model){
        AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
            database.getDao().updateDeviceMessage(model);
        });
    }
    public void removeNotification(DeviceMessagesModel model){
        AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
            database.getDao().deleteDeviceMessage(model);
        });
    }
    public void readAllNotification(){
        AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
            database.getDao().updateSeenNotification();
        });
    }
}
