package com.screenlocker.secure.socket.interfaces;

import com.screenlocker.secure.launcher.AppInfo;

import java.util.List;

public interface GetApplications {
    void onAppsReady(List<AppInfo> infos);
}
