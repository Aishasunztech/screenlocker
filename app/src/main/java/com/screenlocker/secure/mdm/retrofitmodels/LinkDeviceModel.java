package com.screenlocker.secure.mdm.retrofitmodels;

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


}