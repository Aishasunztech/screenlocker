package com.vortexlocker.app.mdm.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LinkDeviceModel {

    @SerializedName("connected_dealer")
    @Expose
    public String connectedDid;
    @SerializedName("dId")
    @Expose
    private String dId;
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

    public LinkDeviceModel(String dId, String connectedDid, List<String> imei, List<String> simNo, String serialNo, String macAddr, String ipAddr) {
        this.dId = dId;
        this.imei = imei;
        this.simNo = simNo;
        this.serialNo = serialNo;
        this.macAddr = macAddr;
        this.ipAddr = ipAddr;
        this.connectedDid = connectedDid;
    }

    public String getDId() {
        return dId;
    }

    public void setDId(String dId) {
        this.dId = dId;
    }

    public List<String> getImei() {
        return imei;
    }

    public void setImei(List<String> imei) {
        this.imei = imei;
    }

    public List<String> getSimNo() {
        return simNo;
    }

    public void setSimNo(List<String> simNo) {
        this.simNo = simNo;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getMacAddr() {
        return macAddr;
    }

    public void setMacAddr(String macAddr) {
        this.macAddr = macAddr;
    }

    public String getdId() {
        return dId;
    }

    public void setdId(String dId) {
        this.dId = dId;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }
}