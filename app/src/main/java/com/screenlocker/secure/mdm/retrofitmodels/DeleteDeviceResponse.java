package com.screenlocker.secure.mdm.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Muhammad Nadeem
 * @Date 6/3/2019.
 */
public class DeleteDeviceResponse {

    @SerializedName("status")
    @Expose
    private boolean status;
    @SerializedName("msg")
    @Expose
    private String msg;

    public boolean isStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }
}
