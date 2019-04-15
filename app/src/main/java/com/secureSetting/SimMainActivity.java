package com.secureSetting;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.screenlocker.secure.R;

import java.util.List;

public class SimMainActivity extends AppCompatActivity {

    public static final String TAG = SimMainActivity.class.getSimpleName();
    TelephonyManager telMgr;
    private SubscriptionManager subscriptionManager;
    private List<SubscriptionInfo> subscriptionInfoList;

    private Toolbar toolbar;
    private TextView tvSim1, tvSim2, tvMobile, tvCalls, tvSms, tvSim1Name,tvSim2Name;
    private SwitchCompat sim_1_switch, sim_2_switch;
    private ConstraintLayout sim_1_container, sim_2_container;

    private BroadcastReceiver simReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: " + intent.getDataString());
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.SecureAppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sim_main);
        initializeViews();

        subscriptionManager = SubscriptionManager.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();

        Log.d("InfoListSIze",subscriptionInfoList.get(0).getDisplayName() + "");


        registerReceiver(simReceiver,new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("SIM cards");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


         telMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int simMainState = telMgr.getSimState(0);
            if(simMainState != TelephonyManager.SIM_STATE_READY)
            {
                tvSim1.setTextColor(tvSim1Name.getTextColors());
                sim_1_switch.setChecked(false);
                sim_1_switch.setEnabled(false);
                tvSim1Name.setText("SIM empty");


            }
            else{
                sim_1_switch.setChecked(true);
                sim_1_switch.setEnabled(true);
                if(subscriptionInfoList.size()>0)
                {
                    tvSim1Name.setText(subscriptionInfoList.get(0).getDisplayName());
                }
                sim_1_container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(SimMainActivity.this, "sim 1 is ready to use", Toast.LENGTH_SHORT).show();
                    }
                });
                tvSim1.setTextColor(ContextCompat.getColor(this,android.R.color.black));

            }
//            checkSimState(simMainState,1);
            int simSecondState = telMgr.getSimState(1);
            if(simSecondState != TelephonyManager.SIM_STATE_READY)
            {
                sim_2_switch.setChecked(false);
                sim_2_switch.setEnabled(false);
                tvSim2.setTextColor(tvSim1Name.getTextColors());


            }
            else{
                sim_2_switch.setEnabled(true);
                sim_2_switch.setChecked(true);
                if(subscriptionInfoList.size()>1)
                {

                    tvSim2Name.setText(subscriptionInfoList.get(1).getDisplayName());
                }
                tvSim2.setTextColor(ContextCompat.getColor(this,android.R.color.black));
                sim_2_container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(SimMainActivity.this, "sim 2 is ready to use", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if(simMainState == TelephonyManager.SIM_STATE_READY && simSecondState == TelephonyManager.SIM_STATE_READY)
            {
                changeTextBlack();
            }
            else{
                setDefaultTextColor();
            }
//            checkSimState(simSecondState,2);


        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.simToolbar);
        tvSim1= findViewById(R.id.sim_1_slot_label);
        tvSim2 = findViewById(R.id.sim_2_slot_label);
        tvMobile = findViewById(R.id.mobile_data_label);
        tvCalls = findViewById(R.id.calls_label);
        tvSms = findViewById(R.id.sms_label);
        sim_1_container = findViewById(R.id.sim_1_slot_container);
        sim_2_container = findViewById(R.id.sim_2_slot_container);
        tvSim1Name = findViewById(R.id.sim_1_name);
        tvSim2Name = findViewById(R.id.sim_2_name);
        sim_1_switch = findViewById(R.id.sim_1_switch);
        sim_2_switch = findViewById(R.id.sim_2_switch);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.home)
        {
            onBackPressed();
        }
        return true;
    }

    private void checkSimState(int simState,int simNumber) {

        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                if(simNumber == 1)
                {
                    sim_1_container.setOnClickListener(null);
                }
                setDefaultTextColor();
                Log.d(TAG, "checkSimState: Absent " + simNumber);
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                if(simNumber == 1)
                {
                    sim_1_container.setOnClickListener(null);
                    sim_1_container.setEnabled(false);
                }
                if(simNumber == 2)
                {
                    sim_1_container.setOnClickListener(null);
                }
                setDefaultTextColor();
                Log.d(TAG, "checkSimState: Netwrok Locked " + simNumber);

                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                if(simNumber == 1)
                {
                    sim_1_container.setOnClickListener(null);
                }
                setDefaultTextColor();
                Log.d(TAG, "checkSimState: PIN_REQUIRED " + simNumber);

                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                if(simNumber == 1)
                {
                    sim_1_container.setOnClickListener(null);
                }
                setDefaultTextColor();

                Log.d(TAG, "checkSimState: PUK_REQUIRED " + simNumber);

                break;
            case TelephonyManager.SIM_STATE_READY:
                if(simNumber ==1)
                {

                    sim_1_container.setOnClickListener(sim_1_click_listener);
                }
                changeTextBlack();
                Log.d(TAG, "checkSimState: SIM_STATE_READY " + simNumber);

                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                if(simNumber == 1)
                {
                    sim_1_container.setOnClickListener(null);
                }
                setDefaultTextColor();
                Log.d(TAG, "checkSimState: STATE_UNKNOWN " + simNumber);

                break;
        }
    }

    private void changeTextBlack() {

        tvMobile.setTextColor(getResources().getColor(android.R.color.black));
        tvCalls.setTextColor(getResources().getColor(android.R.color.black));
        tvSms.setTextColor(getResources().getColor(android.R.color.black));
    }

    private void setDefaultTextColor()
    {
        tvMobile.setTextColor(tvSim1Name.getTextColors());
        tvCalls.setTextColor(tvSim1Name.getTextColors());
        tvSms.setTextColor(tvSim1Name.getTextColors());
    }

    private View.OnClickListener sim_1_click_listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(SimMainActivity.this, "Sim 1 is ready to use", Toast.LENGTH_SHORT).show();
        }
    };
}
