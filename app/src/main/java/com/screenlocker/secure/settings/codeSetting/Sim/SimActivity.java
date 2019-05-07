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

import java.util.ArrayList;
import java.util.List;

public class SimActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sim);

        setToolbar();
        setRecyclerView();
    }



    private void setRecyclerView() {
        RecyclerView rvSim = findViewById(R.id.rvSim);
        rvSim.setLayoutManager(new LinearLayoutManager(this));
        SimAdapter adapter=new SimAdapter(this,null);
        rvSim.setAdapter(adapter);
    }

    private void setToolbar() {
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.sim);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
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
