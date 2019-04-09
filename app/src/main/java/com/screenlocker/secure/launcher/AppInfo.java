package com.screenlocker.secure.launcher;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

@Entity
public class AppInfo implements Parcelable {
    @NonNull
    @PrimaryKey
    String uniqueName;


    private String label;
    private String packageName;

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] icon;
    private boolean guest;
    private boolean enable;
    private boolean encrypted;
    private boolean extension;


    public boolean isExtension() {
        return extension;
    }

    public void setExtension(boolean extension) {
        this.extension = extension;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uniqueName);
        dest.writeString(this.label);
        dest.writeString(this.packageName);
        dest.writeByteArray(this.icon);
        dest.writeByte(this.guest ? (byte) 1 : (byte) 0);
        dest.writeByte(this.enable ? (byte) 1 : (byte) 0);
        dest.writeByte(this.encrypted ? (byte) 1 : (byte) 0);
        dest.writeByte(this.extension ? (byte) 1 : (byte) 0);
    }

    protected AppInfo(Parcel in) {
        this.uniqueName = in.readString();
        this.label = in.readString();
        this.packageName = in.readString();
        this.icon = in.createByteArray();
        this.guest = in.readByte() != 0;
        this.enable = in.readByte() != 0;
        this.encrypted = in.readByte() != 0;
        this.extension = in.readByte() != 0;
    }

    public static final Parcelable.Creator<AppInfo> CREATOR = new Parcelable.Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel source) {
            return new AppInfo(source);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };
}