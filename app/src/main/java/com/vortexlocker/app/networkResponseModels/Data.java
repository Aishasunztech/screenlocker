package com.vortexlocker.app.networkResponseModels;

import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("device_id")
    private String device_id;
    @SerializedName("dId")
    private String dId;

    public String getDevice_id() {
        return device_id;
    }

    public String getdId() {
        return dId;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public void setdId(String dId) {
        this.dId = dId;
    }
}
