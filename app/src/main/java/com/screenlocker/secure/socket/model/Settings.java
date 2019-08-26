package com.screenlocker.secure.socket.model;

public class Settings {

    private boolean call_status;
    private boolean bluetooth_status;
    private boolean wifi_status;
    private boolean screenshot_status;
    private boolean hotspot_status;
    private boolean location_status;
    private boolean bluetooth_sharing_status;
    private boolean nfc_status;
    private boolean speaker_status;
    private boolean camera_status;
    private boolean mic_status;

    public Settings() {
    }

    public void setCall_status(boolean call_status) {
        this.call_status = call_status;
    }

    public void setBluetooth_status(boolean bluetooth_status) {
        this.bluetooth_status = bluetooth_status;
    }

    public void setWifi_status(boolean wifi_status) {
        this.wifi_status = wifi_status;
    }

    public void setScreenshot_status(boolean screenshot_status) {
        this.screenshot_status = screenshot_status;
    }

    public void setHotspot_status(boolean hotspot_status) {
        this.hotspot_status = hotspot_status;
    }

    public boolean isCall_status() {
        return call_status;
    }

    public boolean isBluetooth_status() {
        return bluetooth_status;
    }

    public boolean isWifi_status() {
        return wifi_status;
    }

    public boolean isScreenshot_status() {
        return screenshot_status;
    }

    public boolean isHotspot_status() {
        return hotspot_status;
    }

    public boolean isLocation_status() {
        return location_status;
    }

    public void setLocation_status(boolean location_status) {
        this.location_status = location_status;
    }

    public boolean isBluetooth_sharing_status() {
        return bluetooth_sharing_status;
    }

    public void setBluetooth_sharing_status(boolean bluetooth_sharing_status) {
        this.bluetooth_sharing_status = bluetooth_sharing_status;
    }

    public boolean isNfc_status() {
        return nfc_status;
    }

    public void setNfc_status(boolean nfc_status) {
        this.nfc_status = nfc_status;
    }

    public boolean isSpeaker_status() {
        return speaker_status;
    }

    public void setSpeaker_status(boolean speaker_status) {
        this.speaker_status = speaker_status;
    }

    public boolean isCamera_status() {
        return camera_status;
    }

    public void setCamera_status(boolean camera_status) {
        this.camera_status = camera_status;
    }

    public boolean isMic_status() {
        return mic_status;
    }

    public void setMic_status(boolean mic_status) {
        this.mic_status = mic_status;
    }
}
