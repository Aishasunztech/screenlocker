package com.screenlocker.secure.mdm.retrofitmodels;

import com.google.gson.annotations.SerializedName;

public class DeviceStatusResponse {

    @SerializedName("status")
    private boolean status;
    @SerializedName("msg")
    private String msg;
    @SerializedName("token")
    private String token;
    @SerializedName("device_id")
    private String device_id;
    @SerializedName("expiry_date")
    private String expiry_date;
    @SerializedName("dealer_pin")
    private String dealer_pin;

    public String getDealer_pin() {
        return dealer_pin;
    }

    public boolean isStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public String getToken() {
        return token;
    }

    public String getDevice_id() {
        return device_id;
    }

    public String getExpiry_date() {
        return expiry_date;
    }
}