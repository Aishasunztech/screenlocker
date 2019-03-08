package com.vortexlocker.app.settings.codeSetting.installApps;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
public class InstallAppModel {


    @SerializedName("success")
    @Expose
    private boolean success;
    @SerializedName("list")
    @Expose
    private java.util.List<com.vortexlocker.app.settings.codeSetting.installApps.List> list = null;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<com.vortexlocker.app.settings.codeSetting.installApps.List> getList() {
        return list;
    }

    public void setList(List<com.vortexlocker.app.settings.codeSetting.installApps.List> list) {
        this.list = list;
    }


    public InstallAppModel() {
    }


}
