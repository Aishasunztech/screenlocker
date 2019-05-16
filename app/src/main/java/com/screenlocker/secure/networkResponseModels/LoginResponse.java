package com.screenlocker.secure.networkResponseModels;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("token")
    private String token;
    @SerializedName("status")
    private boolean status;

    public String getToken() {
        return token;
    }

    public boolean isStatus() {
        return status;
    }

}
