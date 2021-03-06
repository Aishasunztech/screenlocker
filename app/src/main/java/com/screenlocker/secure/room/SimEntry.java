package com.screenlocker.secure.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

/**
 * @author Muhammad Nadeem
 * @Date 5/24/2019.
 */
@Entity(tableName = "sim")
public class
SimEntry {
    @NonNull
    @PrimaryKey
    private String iccid;
    @SerializedName("name")
    private String providerName;
    @SerializedName("note")
    private String apn;
    private int slotNo;
    @SerializedName("guest")
    private boolean isGuest;
    @SerializedName("encrypt")
    private boolean isEncrypted;
    private boolean isEnable;
    private String status;

    public SimEntry(String iccid, String providerName, String apn, int slotNo, boolean isGuest, boolean isEncrypted, boolean isEnable, String status) {
        this.iccid = iccid;
        this.providerName = providerName;
        this.apn = apn;
        this.slotNo = slotNo;
        this.isGuest = isGuest;
        this.isEncrypted = isEncrypted;
        this.isEnable = isEnable;
        this.status = status;
    }
    @Ignore
    public SimEntry(String iccid, String providerName, String apn, boolean isGuest, boolean isEncrypted) {
        this.iccid = iccid;
        this.providerName = providerName;
        this.apn = apn;
        this.isGuest = isGuest;
        this.isEncrypted = isEncrypted;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getApn() {
        return apn;
    }

    public void setApn(String apn) {
        this.apn = apn;
    }

    public int getSlotNo() {
        return slotNo;
    }

    public void setSlotNo(int slotNo) {
        this.slotNo = slotNo;
    }

    public boolean isGuest() {
        return isGuest;
    }

    public void setGuest(boolean guest) {
        isGuest = guest;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }
    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
