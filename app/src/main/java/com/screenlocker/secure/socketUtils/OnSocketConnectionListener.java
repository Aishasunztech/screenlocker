package com.screenlocker.secure.socketUtils;

public interface OnSocketConnectionListener {
    void onSocketEventFailed();
    void onSocketConnectionStateChange(int socketState);
    void onInternetConnectionStateChange(int socketState);
}