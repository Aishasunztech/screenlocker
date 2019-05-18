package com.screenlocker.secure.networkResponseModels;

import com.google.gson.annotations.SerializedName;

public class DealerLoginResponse {

    @SerializedName("token")
    private String token;
    @SerializedName("status")
    private boolean status;
    @SerializedName("msg")
    private String msg;
    @SerializedName("dId")
    private int dId;
    @SerializedName("dealer_pin")
    private String dealer_pin;

    public String getDealer_pin() {
        return dealer_pin;
    }

    public void setDealer_pin(String dealer_pin) {
        this.dealer_pin = dealer_pin;
    }

    @SerializedName("device_id")
    String device_id;
    @SerializedName("expiresIn")
    String expiresIn;


    public int getdId() {
        return dId;
    }

    public String getDevice_id() {
        return device_id;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public String getMsg() {
        return msg;

    }

    public String getToken() {
        return token;
    }

    public boolean isStatus() {
        return status;
    }

}
