package com.screenlocker.secure.socket.policy.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.screenlocker.secure.R;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.socket.policy.adapter.PolicyAdapter;
import com.screenlocker.secure.socket.policy.model.PolicyModel;

public class PolicyActivity extends BaseActivity implements PolicyAdapter.ItemClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps);

        PolicyModel[] steps = {new PolicyModel("Apps Permission", "Pending", 0, 0), new PolicyModel("System Permission", "Pending", 0, 0), new PolicyModel("Secure Settings Permission", "Pending", 0, 0), new PolicyModel("Push Apps", "Pending", 0, 0)};

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        PolicyAdapter adapter = new PolicyAdapter(this, steps);
        adapter.setClickListener(this);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

    }




    @Override
    public void onItemClick(View view, int position) {

        Toast.makeText(this, String.valueOf(position), Toast.LENGTH_SHORT).show();
    }
}
