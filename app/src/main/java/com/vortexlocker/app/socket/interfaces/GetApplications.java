package com.vortexlocker.app.socket.interfaces;

import com.vortexlocker.app.launcher.AppInfo;

import java.util.List;

public interface GetApplications {
    void onAppsReady(List<AppInfo> appList);
}
