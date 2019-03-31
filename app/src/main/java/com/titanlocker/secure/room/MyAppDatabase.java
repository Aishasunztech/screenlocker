package com.titanlocker.secure.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.titanlocker.secure.launcher.AppInfo;

@Database(entities = {AppInfo.class},version = 4)
public abstract class MyAppDatabase extends RoomDatabase {
    public abstract MyDao getDao();

    @Override
    public void clearAllTables() {

    }

}
