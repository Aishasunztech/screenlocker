package com.screenlocker.secure.settings.codeSetting.systemControls;

import com.screenlocker.secure.socket.model.Settings;

/**
 * @author Muhammad Nadeem
 * @Date 8/31/2019.
 */
public interface PermissionSateChangeListener {
    void OnPermisionChangeListener(Settings setting, boolean isChecked);
}
