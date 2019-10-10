package com.screenlocker.secure.socket.interfaces;

import com.screenlocker.secure.room.SimEntry;

import java.util.ArrayList;
import java.util.Map;

public interface SocketEvents {

    void getSyncStatus();

    void getAppliedSettings();

    void sendApps();

    void sendExtensions();

    void getDeviceStatus();

    void sendSettings();

    void sendSimSettings(ArrayList<SimEntry> simEntries);

    void sendAppliedStatus(String setting_id);

    void sendAppsWithoutIcons();

    void sendExtensionsWithoutIcons();

    void getPushedApps();

    void getPulledApps();

    void sendPushedAppsStatus(Map<String, Boolean> hashMap);

    void sendPulledAPpsStatus(Map<String, Boolean> hashMap);

    void finishPushedApps(String setting_id);

    void finishPulledApps(String setting_id);

    void writeImei();

    void imeiApplied();

    void imeiHistory();

    void loadPolicy(String policyName);

    void getPolicy();

    void forceUpdateCheck();

    void finishPolicyPushApps(String setting_id);

    void finishPolicyApps(String hId);

    void finishPolicySettings(String hId);

    void finishPolicyExtensions(String hId);

    void finishPolicy(String setting_id);

    void getSimUpdates();

    void sendSystemEvents();

    void getSystemEvents();

}
