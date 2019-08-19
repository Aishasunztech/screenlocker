package com.screenlocker.secure.settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.screenlocker.secure.R;
import com.screenlocker.secure.settings.codeSetting.IMEIActivity;
import com.screenlocker.secure.settings.dataConsumption.DataConsumptionActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.secureSetting.t.ui.MainActivity;

import java.util.concurrent.atomic.AtomicInteger;

import static com.screenlocker.secure.utils.PrefUtils.PREF_FILE;

public class AdvanceSettings extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advance_settings);


        sharedPref = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(mPreferencesListener);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.advance));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.tvDataUSage).setOnClickListener(this);
        findViewById(R.id.tvDataCunsumption).setOnClickListener(this);
        findViewById(R.id.tv_IMEI).setOnClickListener(this);
        findViewById(R.id.tv_set_column).setOnClickListener(this);
        findViewById(R.id.tvTheme).setOnClickListener(this);
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
            case R.id.tv_set_column:
                setColumnSizes();
                break;
            case R.id.tvTheme:
                themeDialogue();
                break;
        }
    }

    private void setColumnSizes() {
        int item = PrefUtils.getIntegerPref(this, AppConstants.KEY_COLUMN_SIZE);
        AtomicInteger selected = new AtomicInteger();
        if (item != 0) {
            if (item == 3) {
                selected.set(0);

            } else {
                selected.set(1);
            }
        } else {
            selected.set(1);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Column Span");
        builder.setSingleChoiceItems(R.array.column_sizes, selected.get(), (dialog, which) -> {
            selected.set(which);
        });
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            if (selected.get() == 1) {
                PrefUtils.saveIntegerPref(this, AppConstants.KEY_COLUMN_SIZE, 4);
            } else if (selected.get() == 0) {
                PrefUtils.saveIntegerPref(this, AppConstants.KEY_COLUMN_SIZE, 3);
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    private void themeDialogue() {
        int item;
        AtomicInteger selected = new AtomicInteger();
        if (PrefUtils.getBooleanPref(this, AppConstants.KEY_THEME)) {
            item = 0;
            selected.set(0);
        } else {
            item = 1;
            selected.set(1);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick A Theme");
        builder.setSingleChoiceItems(R.array.themes, item, (dialog, which) -> {
            selected.set(which);
        });
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            if (selected.get() == 1) {
                PrefUtils.saveBooleanPref(this, AppConstants.KEY_THEME, false);
            } else if (selected.get() == 0) {
                PrefUtils.saveBooleanPref(this, AppConstants.KEY_THEME, true);
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    SharedPreferences.OnSharedPreferenceChangeListener mPreferencesListener = (sharedPreferences, key) -> {
        if (key.equals(AppConstants.KEY_THEME)) {
            if (PrefUtils.getBooleanPref(AdvanceSettings.this, AppConstants.KEY_THEME)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            getDelegate().applyDayNight();
            recreate();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPref.unregisterOnSharedPreferenceChangeListener(mPreferencesListener);
    }

    @Override
    protected void onPause() {

        super.onPause();
    }
}
