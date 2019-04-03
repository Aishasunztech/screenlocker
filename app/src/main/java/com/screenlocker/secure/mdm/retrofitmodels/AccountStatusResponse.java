package com.screenlocker.secure.mdm.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AccountStatusResponse {

    @SerializedName("status")
    @Expose
    private boolean status;
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("dealer_id")
    @Expose
    private int dealerId;
    @SerializedName("account_status")
    @Expose
    private String accountStatus;

    public boolean getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public int getDealerId() {
        return dealerId;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

}
