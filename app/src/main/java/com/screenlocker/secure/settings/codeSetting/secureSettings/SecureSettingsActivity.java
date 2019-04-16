package com.screenlocker.secure.settings.codeSetting.secureSettings;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.appSelection.AppListAdapter;
import com.screenlocker.secure.appSelection.AppSelectionActivity;
import com.screenlocker.secure.appSelection.SelectionContract;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.settings.codeSetting.CodeSettingActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.LifecycleReceiver;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import timber.log.Timber;

import static com.screenlocker.secure.utils.LifecycleReceiver.BACKGROUND;

public class SecureSettingsActivity extends BaseActivity implements SelectionContract.SelectionMvpView, CompoundButton.OnCheckedChangeListener {

    private boolean isBackPressed;
    private ConstraintLayout containerLayout;

    // switches
    private Switch switchGuest, switchEncrypt, switchEnable;

    // status
    boolean guest = false, encrypted = false, enable = false;


    private RecyclerView rvSubExtensions;

    private Adapter adapter;

    private List<SubExtension> extensionsList;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_app_permissions);
        setToolbar();
        // setting ids
        setIds();
        // setting listeners
        setListeners();
        // setting checks
        setChecks();

        populateApps();
    }


    // setIds
    private void setIds() {
        containerLayout = findViewById(R.id.container_layout);
        switchGuest = findViewById(R.id.switchGuest);
        switchEncrypt = findViewById(R.id.switchEncrypt);
        switchEnable = findViewById(R.id.switchEnable);
        progressBar = findViewById(R.id.progress);

    }

    //setListeners
    private void setListeners() {
        switchGuest.setOnCheckedChangeListener(this);
        switchEncrypt.setOnCheckedChangeListener(this);
        switchEnable.setOnCheckedChangeListener(this);
    }

    private void setRecyclerView(List<SubExtension> subExtensions) {
        containerLayout = findViewById(R.id.container_layout);
        rvSubExtensions = findViewById(R.id.extensionList);
        adapter = new Adapter(subExtensions);
        rvSubExtensions.setAdapter(adapter);
    }


    @SuppressLint("StaticFieldLeak")
    private void populateApps() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... voids) {

                extensionsList = MyApplication.getAppDatabase(SecureSettingsActivity.this).getDao().getSubExtensions(AppConstants.SECURE_SETTINGS_UNIQUE);
                // add the data to the list to show apps
                Log.d("igjioejigtete", "doInBackground: " + extensionsList.size());

                Timber.e("doInBackground: data is added to the database");
                return null;
            }

            @Override
            protected void onPostExecute(Void models) {
                super.onPostExecute(models);
                progressBar.setVisibility(View.GONE);
                setRecyclerView(extensionsList);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//
    }

    //set checked
    private void setChecks() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                AppInfo appInfo = MyApplication.getAppDatabase(SecureSettingsActivity.this).getDao().getAppStatus(AppConstants.SECURE_SETTINGS_UNIQUE);

                if (appInfo != null) {
                    guest = appInfo.isGuest();
                    encrypted = appInfo.isEncrypted();
                    enable = appInfo.isEnable();
                }

                AppExecutor.getInstance().getMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        switchEnable.setChecked(enable);
                        switchGuest.setChecked(guest);
                        switchEncrypt.setChecked(encrypted);
                    }
                });


            }
        }).start();


    }

    // set toolbar
    private void setToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Secure Settings Permission");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isBackPressed) {
            try {
                containerLayout.setVisibility(View.INVISIBLE);
                if (CodeSettingActivity.codeSettingsInstance != null) {
                    this.finish();
                    //  finish previous activity and this activity
                    CodeSettingActivity.codeSettingsInstance.finish();

                }
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isBackPressed = false;

    }


    @Override
    public void onStateChange(int state) {            //<---
        switch (state) {
            case LifecycleReceiver.FOREGROUND:
                Timber.e("onStateChange: FOREGROUND");
                break;

            case BACKGROUND:
                Timber.e("onStateChange: BACKGROUND");
                if (CodeSettingActivity.codeSettingsInstance != null) {
                    //  finish previous activity and this activity
                    CodeSettingActivity.codeSettingsInstance.finish();
                    this.finish();
                }
                break;

            default:
                Timber.e("onStateChange: SOMETHING");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
    }

    // set secure app permissions
    private void setSecurePermissions(boolean guest, boolean encrypted, boolean enable) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                MyApplication.getAppDatabase(SecureSettingsActivity.this).getDao().updateParticularApp(guest, encrypted, enable, AppConstants.SECURE_SETTINGS_UNIQUE);
                PrefUtils.saveBooleanPref(SecureSettingsActivity.this, AppConstants.SS_GUEST, guest);
                PrefUtils.saveBooleanPref(SecureSettingsActivity.this, AppConstants.SS_ENCRYPTED, encrypted);
                PrefUtils.saveBooleanPref(SecureSettingsActivity.this, AppConstants.SS_ENABLE, enable);
            }
        }).start();
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.switchGuest:
                guest = isChecked;
                setSecurePermissions(guest, encrypted, enable);
                break;
            case R.id.switchEncrypt:
                encrypted = isChecked;
                setSecurePermissions(guest, encrypted, enable);
                break;
            case R.id.switchEnable:
                enable = isChecked;
                setSecurePermissions(guest, encrypted, enable);
                break;
        }
    }
}
