package com.secureMarket;

/**
 * @author Muhammad Nadeem
 * @Date 9/16/2019.
 */
public class DownloadStatusCls {
    private int downloadID;
    private int status;

    public DownloadStatusCls(int downloadID,@SMActivity.DownlaodState int status) {
        this.downloadID = downloadID;
        this.status = status;
    }

    public int getDownloadID() {
        return downloadID;
    }

    public void setDownloadID(int downloadID) {
        this.downloadID = downloadID;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(@SMActivity.DownlaodState int status) {
        this.status = status;
    }
}
