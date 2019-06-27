package com.screenlocker.secure.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.screenlocker.secure.R;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.socket.service.SocketService;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Utils;

import java.util.List;

import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.UNLINKED_DEVICE;
import static com.screenlocker.secure.utils.CommonUtils.getRemainingDays;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvImei1, tvImei2, tvExpiresIn, tvStatus, tvDeviceId, onlineStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.account));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView systemId = findViewById(R.id.tvSystemId);
        systemId.setText("N/A");

        onlineStatus = findViewById(R.id.tvLinkedStatus);
        tvDeviceId = findViewById(R.id.tvDeviceId);
        tvStatus = findViewById(R.id.tvDeviceStatus);
        tvExpiresIn = findViewById(R.id.tvExpiresIn);
        tvImei1 = findViewById(R.id.tvImei1);
        tvImei2 = findViewById(R.id.tvImei2);

        onlineStatus.setOnClickListener(this);
        tvDeviceId.setOnClickListener(this);
        tvStatus.setOnClickListener(this);
        tvImei1.setOnClickListener(this);
        tvImei2.setOnClickListener(this);


        if (isMyServiceRunning()) {
            onlineStatus.setText(getResources().getString(R.string.status_online));
        } else {
            onlineStatus.setText(getResources().getString(R.string.status_disconnected));
        }


        String device_id = PrefUtils.getStringPref(this, DEVICE_ID);
        if (device_id == null) {
            tvDeviceId.setText(getResources().getString(R.string.n_a));
        } else {
            tvDeviceId.setText(device_id);
        }

        /*Status*/
        TextView textView18 = findViewById(R.id.textViewStatus);
        String device_status = PrefUtils.getStringPref(this, DEVICE_STATUS);
        boolean b = PrefUtils.getBooleanPref(this, DEVICE_LINKED_STATUS);
        if (b) {
            tvStatus.setVisibility(View.VISIBLE);
            textView18.setVisibility(View.VISIBLE);

            if (PrefUtils.getBooleanPref(AboutActivity.this, DEVICE_LINKED_STATUS)) {
                if (device_status == null) {
                    tvStatus.setText(getResources().getString(R.string.active));
                } else
                    tvStatus.setText(device_status);
            } else {
                tvStatus.setText(getResources().getString(R.string.n_a));
            }

        }


        // Expiry Date
        TextView textView16 = findViewById(R.id.textViewExpiry);

        String remaining_days = getRemainingDays(this);

        if (remaining_days != null) {
            tvExpiresIn.setText(remaining_days);
        } else {
            tvExpiresIn.setText(getResources().getString(R.string.n_a));
        }


        List<String> imeis = DeviceIdUtils.getIMEI(this);


        // IMEI 1
        TextView textViewImei = findViewById(R.id.textViewImei);

        if (imeis.size() > 0) {
            String imei = imeis.get(0);
            if (imei != null) {
                tvImei1.setText(imei);
            } else {
                tvImei1.setText(getResources().getString(R.string.n_a));
            }
        }

        // IMEI 2
        TextView textViewImei2 = findViewById(R.id.textViewImei2);

        if (imeis.size() > 1) {
            String imei2 = imeis.get(1);
            if (imei2 != null) {
                tvImei2.setText(imei2);
            } else {
                tvImei2.setText(getResources().getString(R.string.n_a));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SocketService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvDeviceId:
                Utils.copyToClipBoard(this, AppConstants.COPIED_DEVICE_ID, tvDeviceId.getText().toString(),"DeviceId copied to clipboard");
                break;
            case R.id.tvLinkedStatus:
                Utils.copyToClipBoard(this, AppConstants.COPIED_LINKED_STATUS, onlineStatus.getText().toString(),"LinkedStatus copied to clipboard");

                break;
            case R.id.tvDeviceStatus:
                Utils.copyToClipBoard(this, AppConstants.COPIED_DEVICE_STATUS, tvStatus.getText().toString(),"DeviceStatus copied to clipboard");

                break;
            case R.id.tvImei1:
                Utils.copyToClipBoard(this, AppConstants.COPIED_IMEI_1, tvImei1.getText().toString(),"IMEI copied to clipboard");

                break;
            case R.id.tvImei2:
                Utils.copyToClipBoard(this, AppConstants.COPIED_IMEI_2, tvImei2.getText().toString(),"IMEI copied to clipboard");

                break;
        }
    }
}
