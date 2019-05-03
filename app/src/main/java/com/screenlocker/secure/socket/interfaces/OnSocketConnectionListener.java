package com.screenlocker.secure.socket.interfaces;

public interface OnSocketConnectionListener {
    void onSocketEventFailed();
    void onSocketConnectionStateChange(int socketState);
    void onInternetConnectionStateChange(int socketState);
}