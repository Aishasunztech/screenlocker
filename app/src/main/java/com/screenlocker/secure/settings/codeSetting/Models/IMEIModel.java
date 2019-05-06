package com.screenlocker.secure.settings.codeSetting.Models;

import java.util.List;

public class IMEIModel {
    private String device_id;
    private String serial;
    private String mac;
    private List<String> imei;

    public IMEIModel(String device_id, String serial, String mac, List<String> imei) {
        this.device_id = device_id;
        this.serial = serial;
        this.mac = mac;
        this.imei = imei;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public List<String> getImei() {
        return imei;
    }

    public void setImei(List<String> imei) {
        this.imei = imei;
    }
}
