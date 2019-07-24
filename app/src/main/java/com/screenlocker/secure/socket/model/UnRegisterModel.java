package com.screenlocker.secure.socket.model;

/**
 * @author Muhammad Nadeem
 * @Date 7/24/2019.
 */
public class UnRegisterModel {
    private  String id;
    private String device_id;
    private boolean guest;
    private boolean encrypt;

    public UnRegisterModel(String id, String device_id, boolean guest, boolean encrypt) {
        this.id = id;
        this.device_id = device_id;
        this.guest = guest;
        this.encrypt = encrypt;
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

    public boolean isGuest() {
        return guest;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public void setEncrypt(boolean encrypt) {
        this.encrypt = encrypt;
    }
}
