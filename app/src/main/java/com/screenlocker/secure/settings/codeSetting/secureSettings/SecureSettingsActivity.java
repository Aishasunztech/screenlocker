package com.screenlocker.secure.settings.codeSetting.secureSettings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.appSelection.SelectionContract;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.room.MyDao;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.settings.codeSetting.CodeSettingActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.LifecycleReceiver;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.List;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.BROADCAST_APPS_ACTION;
import static com.screenlocker.secure.utils.AppConstants.EXTENSION_ENCRYPTED_CHECKED;
import static com.screenlocker.secure.utils.AppConstants.EXTENSION_GUEST_CHECKED;
import static com.screenlocker.secure.utils.AppConstants.KEY_DATABASE_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.SECURE_SETTINGS_CHANGE;
import static com.screenlocker.secure.utils.LifecycleReceiver.BACKGROUND;

public class SecureSettingsActivity extends BaseActivity implements SelectionContract.SelectionMvpView,
        CompoundButton.OnCheckedChangeListener, Adapter.MenuChecklistener {

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

//        populateApps();
    }


    // setIds
    private void setIds() {
        containerLayout = findViewById(R.id.container_layout);
        switchGuest = findViewById(R.id.switchGuest);
        switchEncrypt = findViewById(R.id.switchEncrypt);
        switchEnable = findViewById(R.id.switchEnable);
        progressBar = findViewById(R.id.progress);
        rvSubExtensions = findViewById(R.id.extensionList);


    }

    //setListeners
    private void setListeners() {
        switchGuest.setOnCheckedChangeListener(this);
        switchEncrypt.setOnCheckedChangeListener(this);
        switchEnable.setOnCheckedChangeListener(this);
    }

    private void setRecyclerView(List<SubExtension> subExtensions) {

        adapter = new Adapter(subExtensions, this);
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

        new Thread(() -> {
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


        }).start();


    }

    // set toolbar
    private void setToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.secure_settings_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        populateApps();

        isBackPressed = false;
    }

    @Override
    protected void onStop() {


        Intent intent = new Intent(BROADCAST_APPS_ACTION);
        intent.putExtra(KEY_DATABASE_CHANGE, "extensions");
        LocalBroadcastManager.getInstance(SecureSettingsActivity.this).sendBroadcast(intent);

        PrefUtils.saveBooleanPref(this, SECURE_SETTINGS_CHANGE, true);


        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.extension_select_menu, menu);

        MyDao myDao = MyApplication.getAppDatabase(SecureSettingsActivity.this).getDao();

        AppExecutor.getInstance().getSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                int guestStatus = myDao.checkGuestStatus(false);
                AppExecutor.getInstance().getMainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if(guestStatus == 0)
                        {
                            menu.findItem(R.id.extension_guest_all).setChecked(true);

                        }else{
                            menu.findItem(R.id.extension_guest_all).setChecked(false);
                        }
                    }
                });

                int encryptedStatus = myDao.checkEncryptedStatus(false);
                AppExecutor.getInstance().getMainThread().execute(() -> {
                    if(encryptedStatus == 0)
                    {
                        menu.findItem(R.id.extension_encryption_all).setChecked(true);

                    }else{
                        menu.findItem(R.id.extension_encryption_all).setChecked(false);

                    }
                });

            }
        });


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MyDao myDao = MyApplication.getAppDatabase(SecureSettingsActivity.this).getDao();
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.extension_guest_all:

                if (item.isChecked()) {
                    for (SubExtension subExtension : extensionsList) {
                        if (subExtension.isGuest())
                            subExtension.setGuest(false);
                    }
                    new Thread(() -> {
                        myDao.setAllGuest(AppConstants.SECURE_SETTINGS_UNIQUE, false);
                        extensionsList = myDao.getSubExtensions(AppConstants.SECURE_SETTINGS_UNIQUE);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setRecyclerView(extensionsList);

                            }
                        });

                    }).start();
                    PrefUtils.saveBooleanPref(this, EXTENSION_GUEST_CHECKED, false);
                    item.setChecked(false);
                } else {
                    for (SubExtension subExtension : extensionsList) {
                        if (!subExtension.isGuest())
                            subExtension.setGuest(true);
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            myDao.setAllGuest(AppConstants.SECURE_SETTINGS_UNIQUE, true);
                            extensionsList = myDao.getSubExtensions(AppConstants.SECURE_SETTINGS_UNIQUE);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setRecyclerView(extensionsList);

                                }
                            });

                        }
                    }).start();
                    PrefUtils.saveBooleanPref(this, EXTENSION_GUEST_CHECKED, true);

                    item.setChecked(true);
                }

//                adapter.notifyDataSetChanged();
                break;

            case R.id.extension_encryption_all:
                if (item.isChecked()) {

                    for (SubExtension subExtension : extensionsList) {
                        if (subExtension.isEncrypted())
                            subExtension.setEncrypted(false);
                    }
                    new Thread(() -> {
                        myDao.setAllEncrypted(AppConstants.SECURE_SETTINGS_UNIQUE, false);

                        extensionsList = myDao.getSubExtensions(AppConstants.SECURE_SETTINGS_UNIQUE);

                        runOnUiThread(() -> setRecyclerView(extensionsList));

                    }).start();
                    PrefUtils.saveBooleanPref(this, EXTENSION_ENCRYPTED_CHECKED, false);


                    item.setChecked(false);
                } else {
                    for (SubExtension subExtension : extensionsList) {
                        if (!subExtension.isEncrypted())
                            subExtension.setEncrypted(true);
                    }
                    new Thread(() -> {

                        myDao.setAllEncrypted(AppConstants.SECURE_SETTINGS_UNIQUE, true);
                        extensionsList = myDao.getSubExtensions(AppConstants.SECURE_SETTINGS_UNIQUE);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setRecyclerView(extensionsList);

                            }
                        });

                    }).start();
                    PrefUtils.saveBooleanPref(this, EXTENSION_ENCRYPTED_CHECKED, true);
                    item.setChecked(true);
                }

//                adapter.notifyDataSetChanged();
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
    }

    // set secure app permissions
    private void setSecurePermissions(boolean guest, boolean encrypted, boolean enable) {

        new Thread(() -> MyApplication.getAppDatabase(SecureSettingsActivity.this).getDao().updateParticularApp(guest, encrypted, enable, AppConstants.SECURE_SETTINGS_UNIQUE)).start();
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

    @Override
    public void updateMenu() {
        invalidateOptionsMenu();
    }
}
