package com.screenlocker.secure.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.screenlocker.secure.launcher.AppInfo;

@Database(entities = {AppInfo.class}, version = 5)
public abstract class MyAppDatabase extends RoomDatabase {
    public abstract MyDao getDao();

    @Override
    public void clearAllTables() {

    }

}
