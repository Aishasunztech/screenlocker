package com.screenlocker.secure.permissions;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.secure.launcher.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.utils.PrefUtils;

import static com.screenlocker.secure.utils.AppConstants.DEF_PAGE_NO;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectivityFragment extends AbstractStep implements View.OnClickListener {


    public ConnectivityFragment() {
        // Required empty public constructor
    }


    @BindView(R.id.btnWifi)
    Button btnWifi;
    @BindView(R.id.btnSIM)
    Button btnSIM;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connectivity, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnWifi.setOnClickListener(this);
        btnSIM.setOnClickListener(this);
    }

    @Override
    public boolean isSkipable() {
        return true;
    }

    @Override
    public void onSkip() {
        PrefUtils.getInstance(MyApplication.getAppContext()).saveIntegerPref( DEF_PAGE_NO, 5);
        super.onSkip();
    }

    @Override
    public String name() {
        return "Connectivity";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnWifi:
                Intent wifiIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(wifiIntent);
                break;
            case R.id.btnSIM:
                Intent intent = new Intent("com.android.settings.sim.SIM_SUB_INFO_SETTINGS");
                startActivity(intent);
                break;
        }
    }
}
