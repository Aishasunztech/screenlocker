package com.screenlocker.secure.mdm.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeviceModel {

    @SerializedName("serial_no")
    @Expose
    private String serialNo;
    @SerializedName("ip")
    @Expose
    private String ip;

    @SerializedName("unique_name")
    @Expose
    private String uniqueName;

    @SerializedName("mac_address")
    @Expose
    private String macAddress;
    @SerializedName("dealer_pin")
    @Expose
    private String dealerPin;

    public DeviceModel(String serialNo, String ip, String uniqueName, String macAddress) {
        this.serialNo = serialNo;
        this.ip = ip;
        this.uniqueName = uniqueName;
        this.macAddress = macAddress;
    }
}