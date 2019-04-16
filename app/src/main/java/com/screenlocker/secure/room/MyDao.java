package com.screenlocker.secure.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

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

    @Query("UPDATE AppInfo SET guest=:guest , encrypted=:encrypted, enable=:enable WHERE uniqueName=:uniqueName")
    void updateParticularApp(boolean guest, boolean encrypted, boolean enable, String uniqueName);

    @Query("SELECT guest,encrypted,enable,extension,uniqueName from AppInfo  WHERE uniqueName=:uniqueName")
    AppInfo getAppStatus(String uniqueName);

    @Query("SELECT * from SubExtension  WHERE uniqueName=:uniqueName")
    List<SubExtension> getSubExtensions(String uniqueName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSubExtensions(SubExtension subExtensionModal);

    @Query("UPDATE SubExtension set guest=:guest WHERE uniqueExtension=:uniqueExtension")
    void setGuest(boolean guest, String uniqueExtension);

    @Query("UPDATE SubExtension set encrypted=:encrypted WHERE uniqueExtension=:uniqueExtension")
    void setEncrypted(boolean encrypted, String uniqueExtension);


    @Query("SELECT guest,encrypted,uniqueExtension FROM subextension WHERE uniqueName=:uniqueName AND guest=:status")
    List<SubExtension> getGuestExtensions(String uniqueName, boolean status);

    @Query("SELECT guest,encrypted,uniqueExtension FROM subextension WHERE uniqueName=:uniqueName AND encrypted=:status")
    List<SubExtension> getEncryptedExtensions(String uniqueName, boolean status);


}
