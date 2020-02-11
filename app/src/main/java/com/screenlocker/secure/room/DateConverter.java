package com.screenlocker.secure.room;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.screenlocker.secure.retrofit.ErrorLogRequestBody;

import java.util.Date;

import okhttp3.Headers;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

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

    @TypeConverter
    public static String requestBodyToJason(RequestBody body){
        return new Gson().toJson(body);
    }
    @TypeConverter
    public static RequestBody fromJsonToRequest(String body){
        return new Gson().fromJson(body, RequestBody.class);
    }
    @TypeConverter
    public static String responseBodyToJason(ResponseBody body){
        return new Gson().toJson(body);
    }
    @TypeConverter
    public static ResponseBody fromJsonToResponse(String body){
        return new Gson().fromJson(body, ResponseBody.class);
    }
    @TypeConverter
    public static String headersToJason(Headers body){
        return new Gson().toJson(body);
    }
    @TypeConverter
    public static Headers fromJsonToHeaders(String body){
        return new Gson().fromJson(body, Headers.class);
    }
    @TypeConverter
    public static String DeviceInfoToJason(ErrorLogRequestBody.DeviceInfo body){
        return new Gson().toJson(body);
    }
    @TypeConverter
    public static ErrorLogRequestBody.DeviceInfo fromJsonToDeviceInfo(String body){
        return new Gson().fromJson(body, ErrorLogRequestBody.DeviceInfo.class);
    }
}
