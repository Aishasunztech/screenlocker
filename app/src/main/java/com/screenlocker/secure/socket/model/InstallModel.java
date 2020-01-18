package com.screenlocker.secure.socket.model;

import android.net.Uri;

public class InstallModel {

    private String apk;
    private String apk_name;
    private String package_name;
    private String version_name;
    private boolean guest;
    private boolean encrypted;
    private String version;
    private String token;
    private boolean update;
    private boolean install;
    private boolean enable;

    private String settingId;

    public String getSettingId() {
        return settingId;
    }

    public void setSettingId(String settingId) {
        this.settingId = settingId;
    }

    private String apk_uri;

    private boolean policy;

    public boolean isPolicy() {
        return policy;
    }

    public String getApk_uri() {
        return apk_uri;
    }

    public void setApk_uri(String apk_uri) {
        this.apk_uri = apk_uri;
    }

    public void setPolicy(boolean policy) {
        this.policy = policy;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type;


    public InstallModel() {
    }

    public InstallModel(String apk, String apk_name, boolean guest, boolean encrypted, boolean enable, String type, String apk_uri, boolean policy) {
        this.apk = apk;
        this.apk_name = apk_name;
        this.guest = guest;
        this.encrypted = encrypted;
        this.enable = enable;
        this.type = type;
        this.apk_uri = apk_uri;
        this.policy = policy;
    }


    public InstallModel(String apk,
                        String apk_name,
                        String package_name,
                        String version_name,
                        boolean guest,
                        boolean encrypted,
                        String token,
                        boolean enable) {
        this.apk = apk;
        this.apk_name = apk_name;
        this.package_name = package_name;
        this.version_name = version_name;
        this.guest = guest;
        this.encrypted = encrypted;
        this.token = token;
        this.enable = enable;
    }


    public boolean isInstall() {
        return install;
    }

    public void setInstall(boolean install) {
        this.install = install;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
