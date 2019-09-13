package com.screenlocker.secure.room.migrations;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * @author Muhammad Nadeem
 * @Date 9/11/2019.
 */

public class Migration_14_15 extends Migration {
    /**
     * Creates a new migration between {@code startVersion} and {@code endVersion}.
     *
     * @param startVersion The start version of the database.
     * @param endVersion   The end version of the database after this migration is applied.
     */
    public Migration_14_15(int startVersion, int endVersion) {
        super(startVersion, endVersion);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE 'SubExtension' ADD COLUMN 'systemApp' INTEGER NOT NULL DEFAULT 1");
    }
}
