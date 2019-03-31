package com.titanlocker.secure.mdm.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DealerLoginModel {

    @SerializedName("link_code")
    @Expose
    private String linkCode;

    @SerializedName("imei")
    @Expose
    private List<String> imei;
    @SerializedName("simNo")
    @Expose
    private List<String> simNo;
    @SerializedName("serialNo")
    @Expose
    private String serialNo;
    @SerializedName("macAddr")
    @Expose
    private String macAddr;
    @SerializedName("ip")
    @Expose
    private String ipAddr;

    public DealerLoginModel(String linkCode) {
        this.linkCode = linkCode;
    }

    public DealerLoginModel(String linkCode, List<String> imei, List<String> simNo, String serialNo, String macAddr, String ipAddr) {
        this.imei = imei;
        this.simNo = simNo;
        this.serialNo = serialNo;
        this.macAddr = macAddr;
        this.ipAddr = ipAddr;
        this.linkCode = linkCode;
    }

    public String getLinkCode() {
        return linkCode;
    }

    public void setLinkCode(String linkCode) {
        this.linkCode = linkCode;
    }

}