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

    @SerializedName("user_id")
    private String user_id;

    @SerializedName("chat_id")
    private String chatId;

    @SerializedName("pgp_email")
    private String pgpID;

    @SerializedName("sim_id")
    private String simId1;

    @SerializedName("sim_id2")
    private String simId2;

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

    public String getUser_id() {
        return user_id;
    }

    public String getExpiry_date() {
        return expiry_date;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getPgpID() {
        return pgpID;
    }

    public void setPgpID(String pgpID) {
        this.pgpID = pgpID;
    }

    public String getSimId1() {
        return simId1;
    }

    public void setSimId1(String simId1) {
        this.simId1 = simId1;
    }

    public String getSimId2() {
        return simId2;
    }

    public void setSimId2(String simId2) {
        this.simId2 = simId2;
    }
}