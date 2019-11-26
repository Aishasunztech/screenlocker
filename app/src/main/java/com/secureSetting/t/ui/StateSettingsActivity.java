package com.secureSetting.t.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.appcompat.widget.Toolbar;
import com.secure.launcher.R;

import com.screenlocker.secure.base.BaseActivity;
import com.secureSetting.t.util.PreferenceManager;

public class StateSettingsActivity extends BaseActivity {

    Switch mSwitchSystem;
    Switch mSwitchUninstall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setToolbar();

        // hide system
        mSwitchSystem = findViewById(R.id.switch_system_apps);
        mSwitchSystem.setOnCheckedChangeListener((compoundButton, b) -> {
            if (PreferenceManager.getInstance().getSystemSettings(PreferenceManager.PREF_SETTINGS_HIDE_SYSTEM_APPS) != b) {
                PreferenceManager.getInstance().putBoolean(PreferenceManager.PREF_SETTINGS_HIDE_SYSTEM_APPS, b);
                setResult(1);
            }
        });

        findViewById(R.id.group_system).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwitchSystem.performClick();
            }
        });

        // hide uninstall
        mSwitchUninstall = findViewById(R.id.switch_uninstall_appps);
        mSwitchUninstall.setOnCheckedChangeListener((compoundButton, b) -> {
            if (PreferenceManager.getInstance().getUninstallSettings(PreferenceManager.PREF_SETTINGS_HIDE_UNINSTALL_APPS) != b) {
                PreferenceManager.getInstance().putBoolean(PreferenceManager.PREF_SETTINGS_HIDE_UNINSTALL_APPS, b);
                setResult(1);
            }
        });

        findViewById(R.id.group_uninstall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSwitchUninstall.performClick();
            }
        });

        // ignore list
        findViewById(R.id.group_ignore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StateSettingsActivity.this, IgnoreActivity.class));
            }
        });

        // about


        // share


        restoreStatus();
    }

    private void setToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.settings);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void restoreStatus() {
        mSwitchSystem.setChecked(PreferenceManager.getInstance().getSystemSettings(PreferenceManager.PREF_SETTINGS_HIDE_SYSTEM_APPS));
        mSwitchUninstall.setChecked(PreferenceManager.getInstance().getUninstallSettings(PreferenceManager.PREF_SETTINGS_HIDE_UNINSTALL_APPS));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() ==android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
