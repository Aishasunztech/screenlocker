package com.secureClear;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.screenlocker.secure.R;
import com.screenlocker.secure.utils.AppConstants;
import com.secureSetting.SecureSettingsMain;

import java.util.Set;

public class SecureClearActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout notificationContainer;
    private Toolbar toolbar;

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            finish();
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_clear);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(AppConstants.BROADCAST_ACTION));


        notificationContainer = findViewById(R.id.secure_clear_container);
        toolbar = findViewById(R.id.secureClearBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Secure Clear");
        notificationContainer.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
    switch (v.getId())
    {
        case R.id.secure_clear_container:
            Set<String> checkNotification = NotificationManagerCompat.getEnabledListenerPackages(SecureClearActivity.this);
            Object[] packages =  checkNotification.toArray();
            if (packages != null) {
                for(int i = 0; i<packages.length ; i++)
                {
                    Log.d("checkNotification",packages[i].toString());
                    if(packages[i].toString().contains("com.vortexlocker.app"))
                    {
                        Intent intent = new Intent("com.example.clearNotificaiton.NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
                        intent.putExtra("command","clearall");
                        sendBroadcast(intent);
                        break;
                    }

                }
            }
            break;
    }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
