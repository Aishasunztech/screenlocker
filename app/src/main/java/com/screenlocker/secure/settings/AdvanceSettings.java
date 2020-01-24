package com.screenlocker.secure.settings;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.settings.codeSetting.IMEIActivity;
import com.screenlocker.secure.settings.dataConsumption.DataConsumptionActivity;
import com.secure.launcher.R;
import com.secureSetting.t.ui.MainActivity;

public class AdvanceSettings extends BaseActivity implements View.OnClickListener {

    private WindowManager.LayoutParams localLayoutParams;
    private WindowManager wm;
    FrameLayout mView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advance_settings);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.advance));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.tvDataUSage).setOnClickListener(this);
        findViewById(R.id.tvDataCunsumption).setOnClickListener(this);
        findViewById(R.id.tv_IMEI).setOnClickListener(this);

        findViewById(R.id.language_container).setOnClickListener(this);
        findViewById(R.id.dateTime_container).setOnClickListener(this);
        findViewById(R.id.battery_cotainer).setOnClickListener(this);
        mView = new FrameLayout(this);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvDataCunsumption:
                startActivity(new Intent(this, DataConsumptionActivity.class));
                break;
            case R.id.tvDataUSage:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.tv_IMEI:
                startActivity(new Intent(this, IMEIActivity.class));
                break;
            case R.id.language_container:
                final Intent intent = new Intent(Intent.ACTION_MAIN, null);
                ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.Settings$LanguageAndInputSettingsActivity");
                intent.setComponent(cn);
                startActivity(intent);
                break;
            case R.id.dateTime_container:
                startActivity(new Intent(Settings.ACTION_DATE_SETTINGS));
                break;
            case R.id.battery_cotainer:
                startActivityForResult(new Intent(Intent.ACTION_POWER_USAGE_SUMMARY), 3);

                getOverLayLayoutParams();
                break;
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    private void getOverLayLayoutParams() {

        if (localLayoutParams == null) {
            localLayoutParams = new WindowManager.LayoutParams();
        }
        createLayoutParams();
    }

    private void createLayoutParams() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;

        } else {

            localLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        localLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
// this is to enable the notification to recieve touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
// Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        localLayoutParams.y = (int) (24 * getResources().getDisplayMetrics().scaledDensity);

        localLayoutParams.width = (int) (56 * getResources().getDisplayMetrics().scaledDensity);
        localLayoutParams.height = (int) (56 * getResources().getDisplayMetrics().scaledDensity);

        localLayoutParams.format = PixelFormat.TRANSLUCENT;

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        mView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        wm.addView(mView, localLayoutParams);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 3) {
            if (wm != null && mView != null)
                wm.removeViewImmediate(mView);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void removeview() {
        if (mView != null && mView.getWindowToken() != null) {
            if (wm != null) {
                wm.removeViewImmediate(mView);
            }
        }
    }

    @Override
    protected void onDestroy() {
        removeview();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        removeview();
        super.onResume();
    }
}
