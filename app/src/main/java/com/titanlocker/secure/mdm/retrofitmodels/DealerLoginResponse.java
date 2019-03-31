package com.titanlocker.secure.mdm.retrofitmodels;

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

//public class DealerLoginResponse {
//
//    @SerializedName("status")
//    @Expose
//    private boolean status;
//    @SerializedName("dId")
//    @Expose
//    private int dId;
//    @SerializedName("token")
//    @Expose
//    private String token;
//
//    public boolean getStatus() {
//        return status;
//    }
//
//    public void setStatus(boolean status) {
//        this.status = status;
//    }
//
//    public int getDId() {
//        return dId;
//    }
//
//    public void setDId(int dId) {
//        this.dId = dId;
//    }
//
//    public String getToken() {
//        return token;
//    }
//
//    public void setToken(String token) {
//        this.token = token;
//    }
//}