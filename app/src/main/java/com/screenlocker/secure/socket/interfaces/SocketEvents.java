package com.screenlocker.secure.socket.interfaces;

public interface SocketEvents {

    void getSyncStatus();

    void getAlliedSettings();

    void sendApps();

    void getDeviceStatus();

    void sendSettings();

    void sendAppliedStatus();

    void socketEventError();

    void closeSocket();

    void disconnectSocket();

    void connectSocket();

}