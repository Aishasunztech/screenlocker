package com.screenlocker.secure.settings.codeSetting.policy;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.screenlocker.secure.R;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.settings.codeSetting.CodeSettingActivity;
import com.screenlocker.secure.utils.LifecycleReceiver;

import timber.log.Timber;

import static com.screenlocker.secure.utils.LifecycleReceiver.BACKGROUND;
import static com.screenlocker.secure.utils.LifecycleReceiver.LIFECYCLE_ACTION;
import static com.screenlocker.secure.utils.LifecycleReceiver.STATE;

public class PolicyActivity extends BaseActivity implements View.OnClickListener {


    private boolean isBackPressed;

    private boolean isPolicyDialogOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy);
        setToolbar();
        findViewById(R.id.btnDefaultPolicy).setOnClickListener(this);


    }


    private void setToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Load Policy");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnDefaultPolicy) {
            handleLoadPolicy();
        }
    }


    // method to load default policy
    private void handleLoadPolicy() {
        isPolicyDialogOpen = true;
        new AlertDialog.Builder(PolicyActivity.this).
                setTitle("Warning!")
                .setMessage("This will push the default Policy set by your Dealer").setPositiveButton("Ok", (dialogInterface, i) -> {
            Toast.makeText(this, "Default policy loading", Toast.LENGTH_SHORT).show();
        })
                .setNegativeButton("Cancel", (dialogInterface, i) -> {
                    dialogInterface.cancel();
                })
                .show();
    }

    @Override
    public void onStateChange(int state) {
        switch (state) {

            case LifecycleReceiver.FOREGROUND:
                Timber.e("onStateChange: FOREGROUND");
                break;

            case LifecycleReceiver.BACKGROUND:
                Timber.tag("TAGLLLL").e("onStateChange: BACKGROUND");
                this.finish();
                break;

            default:
                Timber.e("onStateChange: SOMETHING");
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isBackPressed = false;
        isPolicyDialogOpen = false;

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isBackPressed && !isPolicyDialogOpen) {
            try {
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
    protected void onStop() {
        super.onStop();

        if (!isBackPressed && !isPolicyDialogOpen) {
            Intent intent = new Intent(LIFECYCLE_ACTION);
            intent.putExtra(STATE, BACKGROUND);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
    }

}
