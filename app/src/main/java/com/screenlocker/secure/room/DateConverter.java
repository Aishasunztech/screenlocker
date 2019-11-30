package com.screenlocker.secure.room;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * @author Muhammad Nadeem
 * @Date 6/1/2019.
 */
public class DateConverter {

    @TypeConverter
    public static Date longTODate(Long timeStamp) {
        return timeStamp == null ? null : new Date(timeStamp);
    }


    @TypeConverter
    public static Long dateTOLong(Date date) {
        return date == null ? null : date.getTime();
    }
}
