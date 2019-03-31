package com.titanlocker.secure.socket.interfaces;

import com.titanlocker.secure.launcher.AppInfo;

import java.util.List;

public interface GetApplications {
    void onAppsReady(List<AppInfo> appList);
}
