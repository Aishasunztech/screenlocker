package com.screenlocker.secure.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.retrofit.ErrorLogRequestBody;
import com.screenlocker.secure.room.security.DatabaseSecretProvider;
import com.screenlocker.secure.socket.model.DeviceMessagesModel;
import com.screenlocker.secure.socket.model.Settings;
import com.screenlocker.secure.utils.AppConstants;

import net.sqlcipher.database.SupportFactory;

@Database(entities = {AppInfo.class, SubExtension.class, SimEntry.class,
         Settings.class, DeviceMessagesModel.class, ErrorLogRequestBody.class}, version = 17, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class MyAppDatabase extends RoomDatabase {

    private static MyAppDatabase instance;

    public static synchronized MyAppDatabase getInstance(Context context) {
        if (instance == null) {
            DatabaseSecretProvider provider = new DatabaseSecretProvider(context);
            byte[] passphrase = provider.getOrCreateDatabaseSecret().asBytes();
            SupportFactory factory = new SupportFactory(passphrase);
            instance = Room.databaseBuilder(context, MyAppDatabase.class, AppConstants.DATABASE_NAME)
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build();

        }
        return instance;
    }
    public abstract MyDao getDao();


}
