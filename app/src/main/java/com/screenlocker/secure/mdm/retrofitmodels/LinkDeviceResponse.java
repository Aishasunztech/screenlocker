package com.screenlocker.secure.mdm.retrofitmodels;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LinkDeviceResponse {

    @SerializedName("status")
    @Expose
    private boolean status;
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("device_id")
    @Expose
    String device_id;
    @SerializedName("dealer_pin")
    @Expose
    String dealer_pin;

    @SerializedName("success")
    @Expose
    boolean success;

    public boolean isSuccess() {
        return success;
    }

    public boolean isStatus() {
        return status;
    }



    public String getMsg() {
        return msg;
    }

    public String getDevice_id() {
        return device_id;
    }

    public String getDealer_pin() {
        return dealer_pin;
    }
}