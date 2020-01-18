package com.screenlocker.secure.room.migrations;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * @author : Muhammad Nadeem
 * Created at: 1/11/2020
 */
public class Migration_15_16 extends Migration {
    /**
     * Creates a new migration between {@code startVersion} and {@code endVersion}.
     *
     * @param startVersion The start version of the database.
     * @param endVersion   The end version of the database after this migration is applied.
     */
    public Migration_15_16(int startVersion, int endVersion) {
        super(startVersion, endVersion);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE  IF NOT EXISTS 'device_msg' ('job_id' INTEGER NOT NULL PRIMARY KEY, "
                + "'msg' TEXT, 'date' INTEGER NOT NULL , 'isSeen' INTEGER NOT NULL DEFAULT 0)");
    }
}
