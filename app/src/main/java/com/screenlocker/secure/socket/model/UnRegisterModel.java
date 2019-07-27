package com.screenlocker.secure.socket.model;

/**
 * @author Muhammad Nadeem
 * @Date 7/24/2019.
 */
public class UnRegisterModel {
    private  String id;
    private String device_id;
    private boolean unrGuest;
    private boolean unrEncrypt;

    public UnRegisterModel(String id, String device_id, boolean unrGuest, boolean unrEncrypt) {
        this.id = id;
        this.device_id = device_id;
        this.unrGuest = unrGuest;
        this.unrEncrypt = unrEncrypt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public boolean isUnrGuest() {
        return unrGuest;
    }

    public void setUnrGuest(boolean unrGuest) {
        this.unrGuest = unrGuest;
    }

    public boolean isUnrEncrypt() {
        return unrEncrypt;
    }

    public void setUnrEncrypt(boolean unrEncrypt) {
        this.unrEncrypt = unrEncrypt;
    }
}
