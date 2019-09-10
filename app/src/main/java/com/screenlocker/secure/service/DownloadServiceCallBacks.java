package com.screenlocker.secure.service;

/**
 * @author Muhammad Nadeem
 * @Date 9/4/2019.
 */
public interface DownloadServiceCallBacks {
    /*FeedActivity about progress*/
    void onDownLoadProgress(String packageName , int progress, long speed);

    void downloadComplete(String filePath, String packageName);

    void downloadError(String packageNAme);

    void onDownloadStarted(String packageName);
}
