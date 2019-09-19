package com.screenlocker.secure.room.migrations;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import timber.log.Timber;

public class Migration_15_13 extends Migration {
    /**
     * Creates a new migration between {@code startVersion} and {@code endVersion}.
     *
     * @param startVersion The start version of the database.
     * @param endVersion   The end version of the database after this migration is applied.
     */
    public Migration_15_13(int startVersion, int endVersion) {
        super(startVersion, endVersion);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {

        Timber.d("kvjsddnsdsvdjvsjdfvsdvf");

        database.execSQL("DROP TABLE 'Settings'");
        // Create the new table
        database.execSQL("CREATE TABLE SubExtension_new (label TEXT, uniqueName TEXT, uniqueExtension TEXT NOT NULL, icon BLOB, guest INTEGER NOT NULL, encrypted INTEGER NOT NULL, PRIMARY KEY(uniqueExtension))");
        // Copy the data
        database.execSQL("INSERT INTO SubExtension_new (uniqueExtension, icon, guest,label,uniqueName,encrypted) SELECT uniqueExtension, icon, guest,label,uniqueName,encrypted FROM SubExtension");
        // Remove the old table
        database.execSQL("DROP TABLE SubExtension");
        // Change the table name to the correct one
        database.execSQL("ALTER TABLE SubExtension_new RENAME TO SubExtension");

    }


}
