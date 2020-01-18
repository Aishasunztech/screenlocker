package com.screenlocker.secure.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;


import com.contactSupport.ChatMessages;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.socket.model.DeviceMessagesModel;
import com.screenlocker.secure.socket.model.Settings;

@Database(entities = {AppInfo.class, SubExtension.class, SimEntry.class,
        ChatMessages.class, Settings.class, DeviceMessagesModel.class}, version = 16, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class MyAppDatabase extends RoomDatabase {
    public abstract MyDao getDao();

    @Override
    public void clearAllTables() {

    }

}
