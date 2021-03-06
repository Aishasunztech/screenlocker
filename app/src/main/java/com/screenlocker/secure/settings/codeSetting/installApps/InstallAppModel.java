package com.screenlocker.secure.settings.codeSetting.installApps;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
public class InstallAppModel {


    @SerializedName("success")
    @Expose
    private boolean success;
    @SerializedName("list")
    @Expose
    private List<ServerAppInfo> serverAppInfo = null;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<ServerAppInfo> getServerAppInfo() {
        return serverAppInfo;
    }

    public void setServerAppInfo(List<ServerAppInfo> serverAppInfo) {
        this.serverAppInfo = serverAppInfo;
    }


    public InstallAppModel() {
    }


}
