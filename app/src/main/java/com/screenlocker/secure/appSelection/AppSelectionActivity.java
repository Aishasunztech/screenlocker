package com.screenlocker.secure.appSelection;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.settings.codeSetting.CodeSettingActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.LifecycleReceiver;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkManager;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.APPS_SETTING_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.BROADCAST_APPS_ACTION;
import static com.screenlocker.secure.utils.AppConstants.KEY_DATABASE_CHANGE;
import static com.screenlocker.secure.utils.LifecycleReceiver.BACKGROUND;
import static com.screenlocker.secure.utils.LifecycleReceiver.LIFECYCLE_ACTION;
import static com.screenlocker.secure.utils.LifecycleReceiver.STATE;


public class AppSelectionActivity extends BaseActivity implements SelectionContract.SelectionMvpView {


    public AppSelectionActivity() {
    }

    /**
     * recyclerview for the apps list to select it encrypt
     */
    private RecyclerView rvAppSelection;
    /**
     * holds the app list data
     */
    private ArrayList<AppInfo> mAppsList;
    /**
     * for showing loader while loading apps
     */
    private ProgressBar mProgress;
    /**
     * for package manager
     */
    private PackageManager mPackageManager;
    /**
     * hiding layout
     */
    private ConstraintLayout containerLayout;


    private AppListAdapter adapter;
    private SelectionPresenter selectionPresenter;
    private String packageName;
    private boolean isBackPressed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selection);
        isBackPressed = false;
        setToolbar();
        mProgress = findViewById(R.id.progress);
        selectionPresenter = new SelectionPresenter(this, new SelectionModel(this));
        packageName = getPackageName();
        mAppsList = new ArrayList<>();
        //appSelected = new ArrayList<>();
        setRecyclerView();
        mPackageManager = getPackageManager();

        populateApps();

    }

    /**
     * set the toolbar for the app selection
     */
    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Apps");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    /**
     * set up recyclerview and its adapter
     */
    private void setRecyclerView() {
        containerLayout = findViewById(R.id.container_layout);
        rvAppSelection = findViewById(R.id.appSelectionList);
        adapter = new AppListAdapter(getPackageName(), mAppsList);
        rvAppSelection.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_select, menu);

        if (PrefUtils.getBooleanPref(this, AppConstants.KEY_DISABLE_ALL)) {
            // all disabled switch is turned on
            menu.findItem(R.id.action_enable_all).setChecked(true);
        } else {
            // all disabled switch is turned off
            menu.findItem(R.id.action_enable_all).setChecked(false);
        }

        if (PrefUtils.getBooleanPref(this, AppConstants.KEY_GUEST_ALL)) {
            // all hide switch is turned on
            menu.findItem(R.id.action_guest_all).setChecked(true);
        } else {
            // all hide switch is turned off
            menu.findItem(R.id.action_guest_all).setChecked(false);
        }

        if (PrefUtils.getBooleanPref(this, AppConstants.KEY_ENCRYPTED_ALL)) {
            // all hide switch is turned on
            menu.findItem(R.id.action_encryption_all).setChecked(true);
        } else {
            // all hide switch is turned off
            menu.findItem(R.id.action_encryption_all).setChecked(false);
        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_guest_all:
                if (item.isChecked()) {
                    //  make all hide switch off
                    for (AppInfo model :
                            mAppsList) {
                        if (model.isGuest())
                            model.setGuest(false);
                    }
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                    for (AppInfo model :
                            mAppsList) {
                        if (!model.isGuest())
                            model.setGuest(true);
                    }
                }
                adapter.notifyDataSetChanged();
                break;
            case R.id.action_enable_all:
                if (item.isChecked()) {
                    //  make all disable switch off
                    for (AppInfo model :
                            mAppsList) {
                        if (model.isEnable()) {
                            if (model.getPackageName().contains(packageName)) {

                                model.setEnable(true);
                            } else {
                                model.setEnable(false);
                            }

                        }
                    }
                    item.setChecked(false);
                    //   PrefUtils.saveBooleanPref(this, AppConstants.KEY_DISABLE_ALL, false);
                } else {
                    //  make all disable switch on
                    for (AppInfo model :
                            mAppsList) {
                        if (!model.isEnable()) {
//                            if (!model.getPackageName().contains(packageName))
                            model.setEnable(true);
                        }
                    }
                    item.setChecked(true);
                    PrefUtils.saveBooleanPref(this, AppConstants.KEY_DISABLE_ALL, true);
                }
                adapter.notifyDataSetChanged();
                break;
            case R.id.action_encryption_all:
                if (item.isChecked()) {
                    //  make all disable switch off
                    for (AppInfo model :
                            mAppsList) {
                        if (model.isEncrypted()) {
                            if (model.getPackageName().contains(packageName)) {
                                model.setEncrypted(true);
                            } else {
                                model.setEncrypted(false);
                            }
                        }
                    }
                    item.setChecked(false);
                    PrefUtils.saveBooleanPref(this, AppConstants.KEY_ENCRYPTED_ALL, false);
                } else {
                    //  make all disable switch on
                    for (AppInfo model :
                            mAppsList) {
                        if (!model.isEncrypted()) {
                            if (!model.getPackageName().contains(packageName))
                                model.setEncrypted(true);
                        }
                    }
                    item.setChecked(true);
                    PrefUtils.saveBooleanPref(this, AppConstants.KEY_ENCRYPTED_ALL, true);
                }
                adapter.notifyDataSetChanged();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * doing the task of adding the apps and populating it in the  background thread
     */

    @SuppressLint("StaticFieldLeak")
    private void populateApps() {
        selectionPresenter.showProgress(mProgress);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                selectionPresenter.showProgress(mProgress);
            }

            @Override
            protected Void doInBackground(Void... voids) {

                List<AppInfo> apps = MyApplication.getAppDatabase(AppSelectionActivity.this).getDao().getAppsOrExtensions(false);
                // add the data to the list to show apps

                selectionPresenter.addAppsToList(mPackageManager, mAppsList, apps);
                Timber.e("doInBackground: data is added to the database");
                return null;
            }

            @Override
            protected void onPostExecute(Void models) {
                super.onPostExecute(models);
                selectionPresenter.hideProgress(mProgress);
                rvAppSelection.getAdapter().notifyDataSetChanged();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onPause() {
        super.onPause();
        if (!isBackPressed) {
            containerLayout.setVisibility(View.INVISIBLE);

            //ExitActivity.exitApplicationAndRemoveFromRecent(AppSelectionActivity.this);
            this.finish();
            try {

                if (CodeSettingActivity.codeSettingsInstance != null) {
                    //  finish previous activity and this activity
                    CodeSettingActivity.codeSettingsInstance.finish();

                }
            } catch (Exception e) {

            }

        }
    }


    @Override
    public void finish() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.finishAndRemoveTask();
        } else {
            super.finish();
        }
    }

    @Override
    protected void onResume() {
        isBackPressed = false;
        super.onResume();
    }

    @Override
    protected void onStop() {
        // update the db with new setting
        new Thread() {
            @Override
            public void run() {
                try {
                    for (AppInfo model : mAppsList) {
                        selectionPresenter.updateAppInDB(model);
                    }
                    PrefUtils.saveBooleanPref(AppSelectionActivity.this, APPS_SETTING_CHANGE, true);

                    Intent intent = new Intent(BROADCAST_APPS_ACTION);
                    intent.putExtra(KEY_DATABASE_CHANGE, "apps");
                    LocalBroadcastManager.getInstance(AppSelectionActivity.this).sendBroadcast(intent);


                } catch (Exception e) {
                    e.printStackTrace();
                }
                // here we are checking that weather we have to send the user to the outer main screen or not
                if (!isBackPressed) {
                    Intent intent = new Intent(LIFECYCLE_ACTION);
                    intent.putExtra(STATE, BACKGROUND);
                    sendBroadcast(intent);
                }
            }
        }.start();

        super.onStop();

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
        isBackPressed = true;
        super.onBackPressed();

    }
}
