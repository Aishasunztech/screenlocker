package com.screenlocker.secure.settings.codeSetting.installApps;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class List {


    @SerializedName("apk_name")
    @Expose
    private String apkName;

    @SerializedName("logo")
    @Expose
    private String logo;

    @SerializedName("apk")
    @Expose
    private String apk;


    @SerializedName("apk_status")
    @Expose
    private String apkStatus;

    @SerializedName("package_name")
    @Expose
    private String packageName;

    public String getApk_size() {
        return apk_size;
    }

    public void setApk_size(String apk_size) {
        this.apk_size = apk_size;
    }

    @SerializedName("apk_size")
    @Expose
    private String apk_size;

    @SerializedName("is_restrict_uninstall")
    @Expose
    private int is_restrict_uninstall;


    public int getIs_restrict_uninstall() {
        return is_restrict_uninstall;
    }

    public void setIs_restrict_uninstall(int is_restrict_uninstall) {
        this.is_restrict_uninstall = is_restrict_uninstall;
    }

    private boolean installed;

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public String getApkName() {
        return apkName;
    }

    public void setApkName(String apkName) {
        this.apkName = apkName;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getApk() {
        return apk;
    }

    public void setApk(String apk) {
        this.apk = apk;
    }

    public String getApkStatus() {
        return apkStatus;
    }

    public void setApkStatus(String apkStatus) {
        this.apkStatus = apkStatus;
    }


    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }


}
