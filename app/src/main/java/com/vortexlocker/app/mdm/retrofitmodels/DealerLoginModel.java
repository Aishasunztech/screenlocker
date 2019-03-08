package com.vortexlocker.app.mdm.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DealerLoginModel {

    @SerializedName("link_code")
    @Expose
    private String linkCode;

    public DealerLoginModel(String linkCode) {
        this.linkCode = linkCode;
    }

    public String getLinkCode() {
        return linkCode;
    }

    public void setLinkCode(String linkCode) {
        this.linkCode = linkCode;
    }

}