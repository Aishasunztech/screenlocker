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
import com.screenlocker.secure.utils.PrefUtils;

import java.util.List;

import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.CommonUtils.getRemainingDays;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView systemId = findViewById(R.id.tvSystemId);
        systemId.setText("N/A");

        TextView onlineStatus = findViewById(R.id.tvLinkedStatus);
        if (isMyServiceRunning()){
            onlineStatus.setText(getResources().getString(R.string.status_online));
        }else{
            onlineStatus.setText(getResources().getString(R.string.status_disconnected));
        }
        TextView tvDeviceId = findViewById(R.id.tvDeviceId);
        String device_id = PrefUtils.getStringPref(this, DEVICE_ID);
        if (device_id == null) {
            tvDeviceId.setText("N/A");
        }else {
            tvDeviceId.setText(device_id);
        }

        /*Status*/
        TextView tvStatus = findViewById(R.id.tvDeviceStatus);
        TextView textView18 = findViewById(R.id.textViewStatus);
        String device_status = PrefUtils.getStringPref(this, DEVICE_STATUS);
        boolean b = PrefUtils.getBooleanPref(this, DEVICE_LINKED_STATUS);
        if (b) {
            tvStatus.setVisibility(View.VISIBLE);
            textView18.setVisibility(View.VISIBLE);

            if (device_status == null) {
                tvStatus.setText("Active");
            } else
                tvStatus.setText(device_status);
        }


        // Expiry Date
        TextView tvExpiresIn = findViewById(R.id.tvExpiresIn);
        TextView textView16 = findViewById(R.id.textViewExpiry);

        String remaining_days = getRemainingDays(this);

        if (remaining_days != null) {
            tvExpiresIn.setText(remaining_days);
        }else {
            tvExpiresIn.setText("N/A");
        }


        List<String> imeis = DeviceIdUtils.getIMEI(this);


        // IMEI 1
        TextView tvImei1 = findViewById(R.id.tvImei1);
        TextView textViewImei = findViewById(R.id.textViewImei);

        if (imeis.size() > 0) {
            String imei = imeis.get(0);
            if (imei != null) {
                tvImei1.setText(imei);
            }else {
                tvImei1.setText("N/A");
            }
        }

        // IMEI 2
        TextView tvImei2 = findViewById(R.id.tvImei2);
        TextView textViewImei2 = findViewById(R.id.textViewImei2);

        if (imeis.size() > 1) {
            String imei2 = imeis.get(1);
            if (imei2 != null) {
                tvImei2.setText(imei2);
            }else {
                tvImei2.setText("N/A");
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
}
