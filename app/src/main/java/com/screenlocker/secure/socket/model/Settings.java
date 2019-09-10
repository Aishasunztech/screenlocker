package com.screenlocker.secure.socket.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Settings {
    @NonNull
    @PrimaryKey
    private String setting_name;
    private boolean setting_status;

    public Settings() {
    }

    @Ignore
    public Settings(@NonNull String setting_name, boolean setting_status) {
        this.setting_name = setting_name;
        this.setting_status = setting_status;
    }

//    public String getSetting_name() {
//        return setting_name;
//    }

//    public void setSetting_name(String setting_name) {
//        this.setting_name = setting_name;
//    }

//    public boolean isSetting_status() {
//        return setting_status;
//    }

//    public void setSetting_status(boolean setting_status) {
//        this.setting_status = setting_status;
//    }

    public String getSetting_name() {
        return setting_name;
    }

    public void setSetting_name(String setting_name) {
        this.setting_name = setting_name;
    }

    public boolean isSetting_status() {
        return setting_status;
    }

    public void setSetting_status(boolean setting_status) {
        this.setting_status = setting_status;
    }
}
