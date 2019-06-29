package com.screenlocker.secure.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CheckBox;

import com.screenlocker.secure.R;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import static com.screenlocker.secure.utils.PrefUtils.PREF_FILE;

public class ChangeThemeActivity extends BaseActivity {
    private SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_theme);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
         sharedPref = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(mPreferencesListener);
        getSupportActionBar().setTitle(getResources().getString(R.string.theme));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CheckBox lightTheme = findViewById(R.id.light_theme_check);
        CheckBox darkTheme = findViewById(R.id.dark_theme_check);
        if (PrefUtils.getBooleanPref(this, AppConstants.KEY_THEME)){
            darkTheme.setChecked(true);
        }
        else
            lightTheme.setChecked(true);
        lightTheme.setOnClickListener(v -> {
            darkTheme.setChecked(false);
            lightTheme.setChecked(true);
            PrefUtils.saveBooleanPref(this, AppConstants.KEY_THEME,false);
        });

        darkTheme.setOnClickListener(v -> {
            darkTheme.setChecked(true);
            lightTheme.setChecked(false);
            PrefUtils.saveBooleanPref(this, AppConstants.KEY_THEME,true);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    SharedPreferences.OnSharedPreferenceChangeListener mPreferencesListener = (sharedPreferences, key) -> {
        if (key.equals(AppConstants.KEY_THEME)) {
            if (PrefUtils.getBooleanPref(ChangeThemeActivity.this,AppConstants.KEY_THEME)) {
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
}
