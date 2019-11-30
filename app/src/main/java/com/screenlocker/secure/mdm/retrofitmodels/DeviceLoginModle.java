package com.screenlocker.secure.mdm.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DeviceLoginModle {

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

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("version")
    @Expose
    private String version;

    public DeviceLoginModle(String linkCode) {
        this.linkCode = linkCode;
    }


    public DeviceLoginModle(String linkCode, List<String> imei, List<String> simNo, String serialNo, String macAddr, String ipAddr, String type, String version) {
        this.imei = imei;
        this.simNo = simNo;
        this.serialNo = serialNo;
        this.macAddr = macAddr;
        this.ipAddr = ipAddr;
        this.linkCode = linkCode;
        this.type = type;
        this.version = version;
    }

    public String getLinkCode() {
        return linkCode;
    }

    public void setLinkCode(String linkCode) {
        this.linkCode = linkCode;
    }

}