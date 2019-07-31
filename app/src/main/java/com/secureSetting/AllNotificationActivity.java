package com.secureSetting;



import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;


import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;

import com.screenlocker.secure.R;
import com.screenlocker.secure.base.BaseActivity;

public class AllNotificationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_notification);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.app_notification).setOnClickListener(v -> {

            Intent intent = new Intent(this, NotificationsActivity.class);
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
            startActivity(intent);
//
        });
        findViewById(R.id.gen_notification).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.Settings$ConfigureNotificationSettingsActivity");
            intent.setComponent(cn);
            startActivity(intent);
            addView(android.R.color.background_light);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
