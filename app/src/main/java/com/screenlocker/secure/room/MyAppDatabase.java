package com.screenlocker.secure.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.screenlocker.secure.launcher.AppInfo;

@Database(entities = {AppInfo.class, SubExtension.class}, version = 6)
public abstract class MyAppDatabase extends RoomDatabase {
    public abstract MyDao getDao();

    @Override
    public void clearAllTables() {

    }

}
