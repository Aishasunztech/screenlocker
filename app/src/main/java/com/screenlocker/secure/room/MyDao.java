package com.screenlocker.secure.room;

import com.screenlocker.secure.launcher.AppInfo;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Update;

@Dao
public interface MyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertApps(AppInfo appsModel);

    @Query("SELECT * from AppInfo")
    List<AppInfo> getApps();

    @Query("SELECT * from SubExtension ")
    List<SubExtension> getAllSubExtensions();


    @Query("select * from AppInfo where guest= :isGuest and enable =:isEnable ")
    List<AppInfo> getGuestApps(boolean isGuest, boolean isEnable);

    @Query("select * from AppInfo where encrypted= :isEncrypted and enable =:isEnable ")
    List<AppInfo> getEncryptedApps(boolean isEncrypted, boolean isEnable);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select uniqueName ,label, packageName, guest ,enable ,defaultApp,encrypted,extension,visible from AppInfo ")
    List<AppInfo> getAppsWithoutIcons();

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("select uniqueName ,label, uniqueExtension, guest ,encrypted from SubExtension ")
    List<SubExtension> getExtensionsWithoutIcons();


    @Query("UPDATE AppInfo SET guest=:guest  , enable=:enable , encrypted =:encrypted WHERE uniqueName=:uniqueName ")
    int updateAppStatusFromServer(boolean guest, boolean encrypted, boolean enable, String uniqueName);

    @Query("UPDATE SubExtension SET guest=:guest  , encrypted =:encrypted WHERE uniqueExtension=:uniqueExtension ")
    int updateExtensionStatusFromServer(boolean guest, boolean encrypted, String uniqueExtension);


    @Query("select * from AppInfo where extension = :extension")
    List<AppInfo> getAppsOrExtensions(boolean extension);

    @Query("select * from AppInfo where extension = :extension")
    List<AppInfo> getAppsForBlurWorker(boolean extension);

    @Query("SELECT * FROM AppInfo WHERE uniqueName= :value")
    AppInfo getParticularApp(String value);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int updateApps(AppInfo appsModel);

    @Query("DELETE FROM AppInfo where uniqueName=:uniqueName")
    void deleteOne(String uniqueName);

    @Query("UPDATE AppInfo SET guest=:guest , encrypted=:encrypted, enable=:enable WHERE uniqueName=:uniqueName")
    void updateParticularApp(boolean guest, boolean encrypted, boolean enable, String uniqueName);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT guest,encrypted,enable,extension,uniqueName ,defaultApp, visible from AppInfo  WHERE uniqueName=:uniqueName")
    AppInfo getAppStatus(String uniqueName);

    @Query("SELECT * from SubExtension  WHERE uniqueName=:uniqueName")
    List<SubExtension> getSubExtensions(String uniqueName);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSubExtensions(SubExtension subExtensionModal);

    @Query("UPDATE SubExtension set guest=:guest WHERE uniqueExtension=:uniqueExtension")
    void setGuest(boolean guest, String uniqueExtension);

    @Query("UPDATE SubExtension set encrypted=:encrypted WHERE uniqueExtension=:uniqueExtension")
    void setEncrypted(boolean encrypted, String uniqueExtension);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT guest,encrypted,uniqueExtension FROM subextension WHERE uniqueName=:uniqueName AND guest=:status")
    List<SubExtension> getGuestExtensions(String uniqueName, boolean status);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT guest,encrypted,uniqueExtension FROM subextension WHERE uniqueName=:uniqueName AND encrypted=:status")
    List<SubExtension> getEncryptedExtensions(String uniqueName, boolean status);

    @Query("UPDATE subextension set guest=:status WHERE uniqueName=:uniqueName ")
    void setAllGuest(String uniqueName, boolean status);

    @Query("UPDATE subextension set encrypted=:status WHERE uniqueName=:uniqueName ")
    void setAllEncrypted(String uniqueName, boolean status);


    @Query("SELECT COUNT(guest)FROM subextension WHERE guest = :status")
    int checkGuestStatus(boolean status);

    @Query("SELECT COUNT(encrypted)FROM subextension WHERE encrypted = :status")
    int checkEncryptedStatus(boolean status);

    @Query("SELECT guest from Appinfo where packageName = :packageName")
    boolean getAppUserSpace(String packageName);

}
