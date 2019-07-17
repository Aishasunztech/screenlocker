package com.screenlocker.secure.settings.dataConsumption;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Switch;

import com.screenlocker.secure.R;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.utils.PrefUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.screenlocker.secure.socket.utils.utils.cancelJob;
import static com.screenlocker.secure.socket.utils.utils.scheduleUpdateJob;
import static com.screenlocker.secure.utils.AppConstants.LINKSIM;
import static com.screenlocker.secure.utils.AppConstants.SECUREMARKETSIM;
import static com.screenlocker.secure.utils.AppConstants.SECUREMARKETWIFI;
import static com.screenlocker.secure.utils.AppConstants.UPDATESIM;
import static com.screenlocker.secure.utils.AppConstants.UPDATEWIFI;
import static com.screenlocker.secure.utils.AppConstants.UPDATE_JOB;

public class DataConsumptionActivity extends AppCompatActivity {


    @BindView(R.id.switch_us)
    Switch updateSim;
    @BindView(R.id.switch_sw)
    Switch secureMarketWifi;
    @BindView(R.id.switch_ss)
    Switch secureMarketSim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_consumption);
        ButterKnife.bind(this);
        setToolbar();
        //update wifi toggle
        /*if (PrefUtils.getIntegerPref(this,UPDATEWIFI)==1){
            updateWifi.setChecked(true);
        }else
            updateWifi.setChecked(false);*/
        //update sim toggle
        if (PrefUtils.getIntegerPref(this, UPDATESIM) == 1) {
            updateSim.setChecked(true);
        } else
            updateSim.setChecked(false);

        //sm wifi
        if (PrefUtils.getIntegerPref(this, SECUREMARKETWIFI) == 1) {
            secureMarketWifi.setChecked(true);
        } else
            secureMarketWifi.setChecked(false);
        //sm sim
        if (PrefUtils.getIntegerPref(this, SECUREMARKETSIM) == 1) {
            secureMarketSim.setChecked(true);
        } else
            secureMarketSim.setChecked(false);
        //checked change listener
//        updateWifi.setOnCheckedChangeListener( (buttonView, isChecked) -> {
//            if (isChecked){
//                PrefUtils.saveIntegerPref(this,UPDATEWIFI,1);
//            }else
//                PrefUtils.saveIntegerPref(this,UPDATEWIFI,2);
//        });
        updateSim.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                PrefUtils.saveIntegerPref(this, UPDATESIM, 1);
                if (utils.isJobServiceOn(this, UPDATE_JOB)) {
                    cancelJob(this, UPDATE_JOB);
                }
                scheduleUpdateJob(this);
            } else {
                PrefUtils.saveIntegerPref(this, UPDATESIM, 2);
                if (utils.isJobServiceOn(this, UPDATE_JOB)) {
                    cancelJob(this, UPDATE_JOB);
                }
                scheduleUpdateJob(this);
            }
        });

        secureMarketWifi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                PrefUtils.saveIntegerPref(this, SECUREMARKETWIFI, 1);
            } else
                PrefUtils.saveIntegerPref(this, SECUREMARKETWIFI, 2);
        });
        secureMarketSim.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                PrefUtils.saveIntegerPref(this, SECUREMARKETSIM, 1);
            } else
                PrefUtils.saveIntegerPref(this, SECUREMARKETSIM, 2);
        });
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.data_consumption_settings));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true
            );
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void finish() {
        if (getCallingActivity() != null)
            setResult(Activity.RESULT_OK);
        super.finish();
    }
}
