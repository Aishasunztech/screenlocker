package com.screenlocker.secure.settings.codeSetting.Sim;

import android.content.Context;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.screenlocker.secure.R;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.settings.codeSetting.CodeSettingActivity;

import java.util.ArrayList;
import java.util.List;

public class SimActivity extends BaseActivity {

    private boolean isBackPressed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sim);

        setToolbar();
        setRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        isBackPressed = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isBackPressed) {

            if (CodeSettingActivity.codeSettingsInstance != null) {
                //  finish previous activity and this activity
                CodeSettingActivity.codeSettingsInstance.finish();
            }
            this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        isBackPressed = true;
    }

    private void setRecyclerView() {
        RecyclerView rvSim = findViewById(R.id.rvSim);
        rvSim.setLayoutManager(new LinearLayoutManager(this));
        SimAdapter adapter = new SimAdapter(this, null);
        rvSim.setAdapter(adapter);
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.sim);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sim_menu, menu);

        return true;
    }
}
