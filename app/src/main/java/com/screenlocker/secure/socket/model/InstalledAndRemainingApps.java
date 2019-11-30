package com.screenlocker.secure.socket.model;

import java.util.List;

public class InstalledAndRemainingApps {

    private List<InstallModel> remainingApps;
    private List<InstallModel> installedApps;

    public List<InstallModel> getRemainingApps() {
        return remainingApps;
    }

    public void setRemainingApps(List<InstallModel> remainingApps) {
        this.remainingApps = remainingApps;
    }

    public List<InstallModel> getInstalledApps() {
        return installedApps;
    }

    public void setInstalledApps(List<InstallModel> installedApps) {
        this.installedApps = installedApps;
    }

    public InstalledAndRemainingApps(List<InstallModel> remainingApps, List<InstallModel> installedApps) {
        this.remainingApps = remainingApps;
        this.installedApps = installedApps;
    }
}
