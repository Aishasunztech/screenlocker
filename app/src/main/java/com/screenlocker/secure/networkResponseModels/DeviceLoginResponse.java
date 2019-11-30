package com.screenlocker.secure.networkResponseModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeviceLoginResponse {

    @SerializedName("token")
    private String token;
    @SerializedName("status")
    private boolean status;
    @SerializedName("msg")
    private String msg;
    @SerializedName("dealer_pin")
    private String dealer_pin;


    @SerializedName("device_id")
    private String device_id;
    @SerializedName("connected_dealer")
    @Expose
    private String connectedDid;
    @SerializedName("dId")
    @Expose
    private String dId;
    @SerializedName("user_id")
    private String user_id;

    public String getUser_id() {
        return user_id;
    }

    public String getDevice_id() {
        return device_id;
    }

    public String getConnectedDid() {
        return connectedDid;
    }

    public String getdId() {
        return dId;
    }

    public String getToken() {
        return token;
    }

    public boolean isStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public String getDealer_pin() {
        return dealer_pin;
    }
}
