package com.titanlocker.secure.mdm.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CheckStatusResponse {

    @SerializedName("status")
    @Expose
    private boolean status;

    public CheckStatusResponse(boolean status) {
        this.status = status;
    }

    public boolean getStatus() {
        return status;
    }


}