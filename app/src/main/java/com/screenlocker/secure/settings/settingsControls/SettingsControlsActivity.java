package com.screenlocker.secure.settings.settingsControls;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.screenlocker.secure.R;
import com.screenlocker.secure.base.BaseActivity;

public class SettingsControlsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_controls);
        setToolbar();

    }

    private void setToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings App permission");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


}
