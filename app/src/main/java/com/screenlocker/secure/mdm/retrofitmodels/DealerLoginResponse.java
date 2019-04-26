package com.screenlocker.secure.mdm.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class  DealerLoginResponse {

    @SerializedName("token")
    @Expose
    private String token;
    @SerializedName("status")
    @Expose
    private boolean status;
    @SerializedName("data")
    @Expose
    private Data data;
    @SerializedName("expiresIn")
    @Expose
    private String expiresIn;
    @SerializedName("msg")
    @Expose
    private String msg;


    public String getToken() {
        return token;
    }

    public boolean getStatus() {
        return status;
    }

    public Data getData() {
        return data;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public String getMsg() {
        return msg;
    }
}
