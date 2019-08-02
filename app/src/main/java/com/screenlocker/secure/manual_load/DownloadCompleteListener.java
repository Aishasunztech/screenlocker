package com.screenlocker.secure.manual_load;

import com.screenlocker.secure.socket.model.InstallModel;

import java.util.ArrayList;

public interface DownloadCompleteListener {
    void onDownloadCompleted(ArrayList<InstallModel> downloadedApps);
}
