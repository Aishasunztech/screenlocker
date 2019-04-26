package com.screenlocker.secure.socket.interfaces;

public interface SocketEvents {

    void getSyncStatus();

    void getAppliedSettings();

    void sendApps();

    void sendExtensions();

    void getDeviceStatus();

    void sendSettings();

    void sendAppliedStatus();

    void sendAppsWithoutIcons();

    void sendExtensionsWithoutIcons();

}
