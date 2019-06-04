package com.screenlocker.secure.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Update;


import com.contactSupport.ChatMessages;
import com.screenlocker.secure.launcher.AppInfo;

import java.util.List;

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

    @Query("UPDATE AppInfo SET guest=:guest , enable =:enable , encrypted =:encrypted ")
    void updateAllApps(boolean guest, boolean enable, boolean encrypted);

    @Query("UPDATE AppInfo SET extension=:extension WHERE uniqueName=:uniqueName")
    void updateExtension(boolean extension, String uniqueName);

    @Query("UPDATE SubExtension SET guest=:guest  , encrypted =:encrypted WHERE uniqueExtension=:uniqueExtension ")
    int updateExtensionStatusFromServer(boolean guest, boolean encrypted, String uniqueExtension);

    @Query("UPDATE SubExtension SET guest=:guest , encrypted=:encrypted")
    void updateAllExtensions(boolean guest, boolean encrypted);

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

    @Query("DELETE FROM AppInfo where packageName=:packageName")
    void deletePackage(String packageName);


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
    boolean checkGuest(String packageName);

    @Query("SELECT encrypted from Appinfo where packageName = :packageName")
    boolean checkEncrypt(String packageName);

    @Insert
    void insertSim(SimEntry entry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateSim(SimEntry sim);

    @Delete
    void deleteSim(SimEntry entry);

    @Query("SELECT * FROM sim")
    LiveData<List<SimEntry>> getAllSims();
    @Query("SELECT * FROM sim")
    List<SimEntry> getAllSimInService();
    @Query("SELECT * FROM messages order by mDate desc")
    LiveData<List<ChatMessages>> getAllMessages();
    @Update
    void updateMessage(ChatMessages msg);
    @Insert
    void insertMessage(ChatMessages msg);
    @Delete
    void deleteMessage(ChatMessages msg);



}
