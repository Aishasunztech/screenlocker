package com.screenlocker.secure.settings.dataConsumption;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.screenlocker.secure.base.BaseActivity;
import com.secure.launcher.R;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.utils.PrefUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.screenlocker.secure.socket.utils.utils.cancelJob;
import static com.screenlocker.secure.socket.utils.utils.scheduleUpdateJob;
import static com.screenlocker.secure.utils.AppConstants.SECUREMARKETSIM;
import static com.screenlocker.secure.utils.AppConstants.SECUREMARKETWIFI;
import static com.screenlocker.secure.utils.AppConstants.UPDATESIM;
import static com.screenlocker.secure.utils.AppConstants.UPDATE_JOB;

public class DataConsumptionActivity extends BaseActivity {


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

            updateSim.setChecked(prefUtils.getBooleanPref( UPDATESIM));


        //sm wifi
        if (prefUtils.getIntegerPref( SECUREMARKETWIFI) == 1) {
            secureMarketWifi.setChecked(true);
        } else
            secureMarketWifi.setChecked(false);
        //sm sim
        if (prefUtils.getIntegerPref( SECUREMARKETSIM) == 1) {
            secureMarketSim.setChecked(true);
        } else
            secureMarketSim.setChecked(false);
        //checked change listener
//        updateWifi.setOnCheckedChangeListener( (buttonView, isChecked) -> {
//            if (isChecked){
//                prefUtils.saveIntegerPref(UPDATEWIFI,1);
//            }else
//                prefUtils.saveIntegerPref(UPDATEWIFI,2);
//        });
        updateSim.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefUtils.saveBooleanPref( UPDATESIM, isChecked);
        });

        secureMarketWifi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                prefUtils.saveIntegerPref( SECUREMARKETWIFI, 1);
            } else
                prefUtils.saveIntegerPref( SECUREMARKETWIFI, 2);
        });
        secureMarketSim.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                prefUtils.saveIntegerPref( SECUREMARKETSIM, 1);
            } else
                prefUtils.saveIntegerPref( SECUREMARKETSIM, 2);
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
