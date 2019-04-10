package com.screenlocker.secure.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.screenlocker.secure.launcher.AppInfo;

import java.util.List;

@Dao
public interface MyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertApps(AppInfo appsModel);

    @Query("select * from AppInfo")
    List<AppInfo> getApps();

    @Query("select * from AppInfo where guest= :isGuest and enable =:isEnable ")
    List<AppInfo> getGuestApps(boolean isGuest, boolean isEnable);

    @Query("select * from AppInfo where encrypted= :isEncrypted and enable =:isEnable ")
    List<AppInfo> getEncryptedApps(boolean isEncrypted, boolean isEnable);


    @Query("select uniqueName ,label, packageName, guest ,enable ,encrypted,extension from AppInfo ")
    List<AppInfo> getAppsWithoutIcons();


    @Query("select * from AppInfo where extension = :extension")
    List<AppInfo> getAppsOrExtensions(boolean extension);

    @Query("select * from AppInfo where extension = :extension")
    List<AppInfo> getAppsForBlurWorker(boolean extension);


    @Query("SELECT * FROM AppInfo WHERE uniqueName= :value")
    AppInfo getParticularApp(String value);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateApps(AppInfo appsModel);


    @Query("DELETE FROM AppInfo where uniqueName=:packageName")
    void deleteOne(String packageName);

}
