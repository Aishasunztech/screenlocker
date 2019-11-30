package com.screenlocker.secure.mdm.retrofitmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("dId")
    @Expose
    private int dId;
    @SerializedName("connected_dealer")
    @Expose
    private int connectedDealer;
    @SerializedName("type")
    @Expose
    private String type;

    public int getDId() {
        return dId;
    }

    public int getConnectedDealer() {
        return connectedDealer;
    }

    public String getType() {
        return type;
    }

}
