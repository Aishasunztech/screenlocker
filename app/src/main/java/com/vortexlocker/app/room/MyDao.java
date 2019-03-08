package com.vortexlocker.app.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.vortexlocker.app.launcher.AppInfo;

import java.util.List;

@Dao
public interface MyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertApps(AppInfo appsModel);

    @Query("select * from AppInfo")
    List<AppInfo> getApps();

    @Query("SELECT * FROM AppInfo WHERE uniqueName= :value")
    AppInfo getParticularApp(String value);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateApps(AppInfo appsModel);

    @Query("DELETE FROM AppInfo where uniqueName=:packageName")
    void deleteOne(String packageName);



}
