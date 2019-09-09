package com.secureMarket;

import com.screenlocker.secure.settings.codeSetting.installApps.ServerAppInfo;

/**
 * @author Muhammad Nadeem
 * @Date 9/6/2019.
 */
public interface AppInstallUpdateListener {

    void onInstallClick(ServerAppInfo app, int position, boolean isupdate);

    void onUnInstallClick(ServerAppInfo app, boolean status);
}
