package com.screenlocker.secure.room;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import android.os.Parcel;
import android.os.Parcelable;

import com.screenlocker.secure.launcher.AppInfo;

import java.util.Objects;

@Entity
public class SubExtension implements Parcelable {

    private String label;

    @ForeignKey(
            entity = AppInfo.class,
            parentColumns = "uniqueName",
            childColumns = "uniqueName"
    )

    private String uniqueName;

    @NonNull
    @PrimaryKey
    private String uniqueExtension;

    protected SubExtension(Parcel in) {
        label = in.readString();
        uniqueName = in.readString();
        uniqueExtension = Objects.requireNonNull(in.readString());
        icon = in.createByteArray();
        guest = in.readByte() != 0;
        encrypted = in.readByte() != 0;
    }

    public static final Creator<SubExtension> CREATOR = new Creator<SubExtension>() {
        @Override
        public SubExtension createFromParcel(Parcel in) {
            return new SubExtension(in);
        }

        @Override
        public SubExtension[] newArray(int size) {
            return new SubExtension[size];
        }
    };

    @NonNull
    public String getUniqueExtension() {
        return uniqueExtension;
    }

    public void setUniqueExtension(@NonNull String uniqueExtension) {
        this.uniqueExtension = uniqueExtension;
    }

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    private byte[] icon;
    private boolean guest;
    private boolean encrypted;

    public SubExtension(String label, @NonNull String uniqueName, byte[] icon, boolean guest, boolean encrypted) {
        this.label = label;
        this.uniqueName = uniqueName;
        this.icon = icon;
        this.guest = guest;
        this.encrypted = encrypted;
    }

    public SubExtension() {
    }


    public void setLabel(String label) {
        this.label = label;
    }

    public void setUniqueName(String uniqueName) {
        this.uniqueName = uniqueName;
    }

    public void setIcon(byte[] icon) {
        this.icon = icon;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }


    public String getLabel() {
        return label;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public byte[] getIcon() {
        return icon;
    }

    public boolean isGuest() {
        return guest;
    }

    public boolean isEncrypted() {
        return encrypted;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(label);
        dest.writeString(uniqueName);
        dest.writeString(uniqueExtension);
        dest.writeByteArray(icon);
        dest.writeByte((byte) (guest ? 1 : 0));
        dest.writeByte((byte) (encrypted ? 1 : 0));
    }
}
