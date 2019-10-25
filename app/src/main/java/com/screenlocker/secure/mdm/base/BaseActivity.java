package com.screenlocker.secure.mdm.base;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.screenlocker.secure.R;
import com.screenlocker.secure.network.NetworkChangeReceiver;

import butterknife.ButterKnife;

import static com.screenlocker.secure.mdm.utils.LifecycleReceiver.BACKGROUND;
import static com.screenlocker.secure.mdm.utils.LifecycleReceiver.FOREGROUND;
import static com.screenlocker.secure.mdm.utils.LifecycleReceiver.LIFECYCLE_ACTION;
import static com.screenlocker.secure.mdm.utils.LifecycleReceiver.STATE;
import static com.screenlocker.secure.utils.AppConstants.CONNECTED;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_NETWORK_STATUS;
import static com.screenlocker.secure.utils.AppConstants.LIMITED;
import static com.screenlocker.secure.utils.PrefUtils.PREF_FILE;


public abstract class BaseActivity
        extends AppCompatActivity {


    private Snackbar snackbar;

    private NetworkChangeReceiver networkChangeReceiver;
    private SharedPreferences sharedPref;

    private void registerNetworkPref() {
        sharedPref = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(networkChange);
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void unRegisterNetworkPref() {
        if (sharedPref != null)
            sharedPref.unregisterOnSharedPreferenceChangeListener(networkChange);
        if (networkChangeReceiver != null)
            unregisterReceiver(networkChangeReceiver);
    }

    SharedPreferences.OnSharedPreferenceChangeListener networkChange = (sharedPreferences, key) -> {

        if (key.equals(CURRENT_NETWORK_STATUS)) {
            String networkStatus = sharedPreferences.getString(CURRENT_NETWORK_STATUS, LIMITED);
            boolean isConnected = networkStatus.equals(CONNECTED);

            if (isConnected) {
                if (snackbar != null) {
                    snackbar.dismiss();
                }
            } else {
                snackbar = Snackbar.make(
                        ((ViewGroup) this.findViewById(android.R.id.content))
                                .getChildAt(0)
                        , getString(R.string.no_internet)
                        , Snackbar.LENGTH_INDEFINITE);
                snackbar.show();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());

        ButterKnife.bind(this);
//        Timber.plant(new Timber.DebugTree());

        init();


    }

    @Override
    protected void onStart() {
        super.onStart();
        registerNetworkPref();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unRegisterNetworkPref();
    }


    protected abstract void init();

    protected abstract int getContentView();


    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(LIFECYCLE_ACTION);
        intent.putExtra(STATE, FOREGROUND);
        sendBroadcast(intent);
    }


    @Override
    protected void onPause() {
        super.onPause();
        Intent intent = new Intent(LIFECYCLE_ACTION);
        intent.putExtra(STATE, BACKGROUND);
        sendBroadcast(intent);
    }
}
