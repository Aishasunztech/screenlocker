package com.screenlocker.secure.socket.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * @author : Muhammad Nadeem
 * Created at: 1/11/2020
 */
@Entity(tableName = "device_msg")
public class DeviceMessagesModel {

    @PrimaryKey
    private int job_id;
    private String msg;
    private long date;
    private boolean isSeen = false;

    public DeviceMessagesModel() {
    }

    @Ignore
    public DeviceMessagesModel(String msg, int job_id) {
        this.msg = msg;
        this.job_id = job_id;
    }

    public int getJob_id() {
        return job_id;
    }

    public void setJob_id(int job_id) {
        this.job_id = job_id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
