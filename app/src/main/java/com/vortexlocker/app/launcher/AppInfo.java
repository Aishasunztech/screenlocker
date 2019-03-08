package com.vortexlocker.app.launcher;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
@Entity
public class AppInfo {
    @NonNull
    @PrimaryKey
    private String uniqueName;

    private String label;
    private String packageName;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] icon;
    private boolean guest;
    private boolean enable;
    private boolean encrypted;

    @NonNull
    public String getUniqueName() {
        return uniqueName;
    }

    public void setUniqueName(@NonNull String uniqueName) {
        this.uniqueName = uniqueName;
    }



    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public AppInfo() {

    }

    public AppInfo(String label, String packageName, byte[] icon) {
        this.label = label;
        this.packageName = packageName;
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }


    public void setLabel(String label) {
        this.label = label;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public byte[] getIcon() {
        return icon;
    }

    public void setIcon(byte[] icon) {
        this.icon = icon;
    }

    public boolean isGuest() {
        return guest;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
    @Override
    public int hashCode() {
        int hashCode = 1;

        hashCode = hashCode * 37 + this.uniqueName.hashCode();
//        hashCode = hashCode * 37 + this.uniqueName.hashCode();

        return hashCode;
    }
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof AppInfo)) {
            return false;
        }

        AppInfo that = (AppInfo) other;

        // Custom equality check here.
        return this.uniqueName.equals(that.uniqueName);
    }
}