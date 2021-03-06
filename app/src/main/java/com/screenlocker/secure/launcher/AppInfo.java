package com.screenlocker.secure.launcher;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class AppInfo {

    @NonNull
    @PrimaryKey
    private String uniqueName;
    private String label;
    private String packageName;
    @Ignore
    private String setting_id;
    private boolean guest;
    private boolean enable;
    private boolean encrypted;
    private boolean systemApp;
    private boolean extension;
    private boolean visible;
    private boolean defaultApp;

    public int getNumberOfnotifications() {
        return numberOfnotifications;
    }

    public void setNumberOfnotifications(int numberOfnotifications) {
        this.numberOfnotifications = numberOfnotifications;
    }

    @Ignore
    private int numberOfnotifications = 0;


    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] icon;

    public boolean isSystemApp() {
        return systemApp;
    }

    public boolean isExtension() {
        return extension;
    }

    public void setSystemApp(boolean systemApp) {
        this.systemApp = systemApp;
    }


    public void setExtension(boolean extension) {
        this.extension = extension;
    }


    public boolean isDefaultApp() {
        return defaultApp;
    }

    public void setDefaultApp(boolean defaultApp) {
        this.defaultApp = defaultApp;
    }

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

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Ignore
    public AppInfo() {

    }


    public boolean isVisible() {
        return visible;
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

    public String getSetting_id() {
        return setting_id;
    }

    public void setSetting_id(String setting_id) {
        this.setting_id = setting_id;
    }
}
