package com.titanlocker.secure.mdm.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LinkStatusModel {

    @SerializedName("imei")
    @Expose
    private String imei;

    @SerializedName("mac")
    @Expose
    private String mac;

    public LinkStatusModel(String imei, String mac) {
        this.imei = imei;
        this.mac = mac;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}