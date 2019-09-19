package com.screenlocker.secure.room;


import com.screenlocker.secure.launcher.AppInfo;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class SubExtension {

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

    public SubExtension(String label, @NonNull String uniqueName, @NonNull String uniqueExtension, byte[] icon, boolean guest, boolean encrypted) {
        this.label = label;
        this.uniqueName = uniqueName;
        this.icon = icon;
        this.guest = guest;
        this.uniqueExtension = uniqueExtension;
        this.encrypted = encrypted;
    }

    @Ignore
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


}
