package com.screenlocker.secure.retrofit;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import okhttp3.Headers;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * @author : Muhammad Nadeem
 * Created at: 2/10/2020
 */
@Entity()
public class ErrorLogRequestBody {
    @PrimaryKey(autoGenerate = true)
    private long requestId;
    private int code;
    private String message;
    private long apiResponseTime;
    private RequestBody requestBody;
    private Headers requestHeaders;
    private String requestUrl;
    private ResponseBody responseBody;
    private Headers responseHeaders;
    private DeviceInfo info;


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getApiResponseTime() {
        return apiResponseTime;
    }

    public void setApiResponseTime(long apiResponseTIme) {
        this.apiResponseTime = apiResponseTIme;
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    public Headers getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Headers requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public ResponseBody getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(ResponseBody responseBody) {
        this.responseBody = responseBody;
    }

    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Headers responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public DeviceInfo getInfo() {
        return info;
    }

    public void setInfo(DeviceInfo info) {
        this.info = info;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public static class DeviceInfo {
        private String deviceId;
        private String appVersion;
        private String appLabel;
        private String appType;
        private String firmwareInfo;

        public DeviceInfo(String deviceId, String appVersion, String appLabel, String appType, String firmwareInfo) {
            this.deviceId = deviceId;
            this.appVersion = appVersion;
            this.appLabel = appLabel;
            this.appType = appType;
            this.firmwareInfo = firmwareInfo;
        }
    }


}
