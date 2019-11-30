package com.screenlocker.secure.room;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity
public class DisableModel {
    @NonNull
    @PrimaryKey
    private String packageName;


    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }


}
