package com.titanlocker.secure.socket.model;

public class Settings {

    private boolean call_status;
    private boolean bluetooth_status;
    private boolean wifi_status;
    private boolean screenshot_status;
    private boolean hotspot_status;

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
}
