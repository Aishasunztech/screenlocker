package com.secureMarket;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeviceAndSpace {


    @SerializedName("linkCode")
    @Expose
    private String dealerId;

    @SerializedName("spaceType")
    @Expose
    private String space;

    public DeviceAndSpace(String dealerId, String space) {
        this.dealerId = dealerId;
        this.space = space;
    }

    public String getDealerId() {
        return dealerId;
    }

    public void setDealerId(String dealerId) {
        this.dealerId = dealerId;
    }

    public String getSpace() {
        return space;
    }

    public void setSpace(String space) {
        this.space = space;
    }
}
