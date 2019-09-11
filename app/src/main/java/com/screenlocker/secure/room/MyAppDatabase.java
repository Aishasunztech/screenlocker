package com.screenlocker.secure.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;


import com.contactSupport.ChatMessages;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.socket.model.Settings;

@Database(entities = {AppInfo.class, SubExtension.class, SimEntry.class, ChatMessages.class, Settings.class}, version = 15, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class MyAppDatabase extends RoomDatabase {
    public abstract MyDao getDao();

    @Override
    public void clearAllTables() {

    }

}
