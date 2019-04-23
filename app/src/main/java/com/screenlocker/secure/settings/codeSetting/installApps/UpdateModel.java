package com.screenlocker.secure.settings.codeSetting.installApps;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateModel {

    public UpdateModel() {
    }

    @SerializedName("apk_status")
    @Expose
    private boolean apkStatus;


    public void setApkUrl(String apkUrl) {
        this.apkUrl = apkUrl;
    }

    public boolean isApkStatus() {
        return apkStatus;
    }

    public void setApkStatus(boolean apkStatus) {
        this.apkStatus = apkStatus;
    }

    @SerializedName("apk_url")
    @Expose
    private String apkUrl;



    public String getApkUrl() {
        return apkUrl;
    }


}
