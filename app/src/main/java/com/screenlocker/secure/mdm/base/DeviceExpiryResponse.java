package com.screenlocker.secure.mdm.base;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeviceExpiryResponse {

    @SerializedName("status")
    @Expose
    private boolean status;
    @SerializedName("msg")
    @Expose
    private String msg;

    public String getMsg() {
        return msg;
    }

    @SerializedName("device_status")
    @Expose
    private String deviceStatus;

    @SerializedName("of_device_id")
    @Expose
    private String ofDeviceId;

    public boolean isStatus() {
        return status;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public String getOfDeviceId() {
        return ofDeviceId;
    }
}
