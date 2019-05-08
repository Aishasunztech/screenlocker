package com.screenlocker.secure.socket.model;

public class InstallModel {

    private String apk;
    private String apk_name;
    private String package_name;
    private String version_name;
    private boolean guest;
    private boolean encrypted;

    public InstallModel(String apk, String apk_name, String package_name, String version_name, boolean guest, boolean encrypted, boolean enable) {
        this.apk = apk;
        this.apk_name = apk_name;
        this.package_name = package_name;
        this.version_name = version_name;
        this.guest = guest;
        this.encrypted = encrypted;
        this.enable = enable;
    }

    private boolean enable;

    public InstallModel() {
    }

    public String getApk() {
        return apk;
    }

    public String getApk_name() {
        return apk_name;
    }

    public String getPackage_name() {
        return package_name;
    }

    public String getVersion_name() {
        return version_name;
    }

    public boolean isGuest() {
        return guest;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setApk(String apk) {
        this.apk = apk;
    }

    public void setApk_name(String apk_name) {
        this.apk_name = apk_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
