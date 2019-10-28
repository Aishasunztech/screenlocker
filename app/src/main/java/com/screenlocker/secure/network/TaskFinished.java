package com.screenlocker.secure.network;

public interface TaskFinished<T> {
    void onTaskFinished(T data);
}
