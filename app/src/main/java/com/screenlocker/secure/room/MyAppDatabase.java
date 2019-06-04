package com.screenlocker.secure.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;


import com.screenlocker.secure.launcher.AppInfo;

@Database(entities = {AppInfo.class, SubExtension.class,SimEntry.class}, version = 10, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class MyAppDatabase extends RoomDatabase {
    public abstract MyDao getDao();

    @Override
    public void clearAllTables() {

    }

}
