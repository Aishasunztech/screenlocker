package com.screenlocker.secure.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.screenlocker.secure.launcher.AppInfo;

@Database(entities = {AppInfo.class, SubExtension.class,SimEntry.class}, version = 9, exportSchema = false)
public abstract class MyAppDatabase extends RoomDatabase {
    public abstract MyDao getDao();

    @Override
    public void clearAllTables() {

    }

}
