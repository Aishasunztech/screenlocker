package com.screenlocker.secure.mdm.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Muhammad Nadeem
 * @Date 6/3/2019.
 */
public class DeviceModel {

    @SerializedName("serial_no")
    @Expose
    private String serialNo;
    @SerializedName("mac_address")
    @Expose
    private String macAddress;

    public DeviceModel(String serialNo, String macAddress) {
        this.serialNo = serialNo;
        this.macAddress = macAddress;
    }


}
