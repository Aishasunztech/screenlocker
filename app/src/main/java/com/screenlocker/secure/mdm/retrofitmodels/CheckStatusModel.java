package com.screenlocker.secure.mdm.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CheckStatusModel {

    @SerializedName("serial_number")
    @Expose
    private String serial_number;
    @SerializedName("mac")
    @Expose
    private String mac;

    public CheckStatusModel(String serial_number, String mac) {
        this.serial_number = serial_number;
        this.mac = mac;
    }

    public String getImei() {
        return serial_number;
    }

    public void setImei(String imei) {
        this.serial_number = imei;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

}