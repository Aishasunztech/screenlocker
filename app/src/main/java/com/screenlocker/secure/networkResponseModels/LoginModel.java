package com.screenlocker.secure.networkResponseModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginModel {


    @SerializedName("serialNo")
    @Expose
    private String serialNo;
    @SerializedName("macAddr")
    @Expose
    private String macAddr;

    @SerializedName("ip")
    @Expose
    private String ip;

    public LoginModel(String serialNo, String macAddr, String ip) {
        this.serialNo = serialNo;
        this.macAddr = macAddr;
        this.ip = ip;
    }
}
