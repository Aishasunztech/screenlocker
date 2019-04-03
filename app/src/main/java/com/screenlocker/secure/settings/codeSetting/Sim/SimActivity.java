package com.screenlocker.secure.settings.codeSetting.Sim;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.screenlocker.secure.R;
import com.screenlocker.secure.base.BaseActivity;

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
        SimAdapter adapter=new SimAdapter();
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
}
