package com.screenlocker.secure.mdm.base;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeviceExpiryResponse {

    @SerializedName("status")
    @Expose
    private boolean status;
    @SerializedName("start_date")
    @Expose
    private String startDate;
    @SerializedName("end_date")
    @Expose
    private String endDate;

    @SerializedName("expires_in")
    @Expose
    private String expiresIn;

    public boolean isStatus() {
        return status;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getExpiresIn() {
        return expiresIn;
    }
}
