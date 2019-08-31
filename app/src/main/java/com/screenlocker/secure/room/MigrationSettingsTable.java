package com.screenlocker.secure.room;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * @author Muhammad Nadeem
 * @Date 8/31/2019.
 */
public class MigrationSettingsTable extends Migration {

    /**
     * Creates a new migration between {@code startVersion} and {@code endVersion}.
     *
     * @param startVersion The start version of the database.
     * @param endVersion   The end version of the database after this migration is applied.
     */
    public MigrationSettingsTable(int startVersion, int endVersion) {
        super(startVersion, endVersion);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE 'Settings'  ( 'setting_name' TEXT  NOT NULL, 'setting_status' INTEGER NOT NULL DEFAULT 0, PRIMARY KEY('setting_name'))");
    }
}
