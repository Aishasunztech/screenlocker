package com.screenlocker.secure.mdm.base;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.screenlocker.secure.R;
import com.screenlocker.secure.mdm.utils.NetworkChangeReceiver;

import butterknife.ButterKnife;

import static com.screenlocker.secure.mdm.utils.LifecycleReceiver.BACKGROUND;
import static com.screenlocker.secure.mdm.utils.LifecycleReceiver.FOREGROUND;
import static com.screenlocker.secure.mdm.utils.LifecycleReceiver.LIFECYCLE_ACTION;
import static com.screenlocker.secure.mdm.utils.LifecycleReceiver.STATE;


public abstract class BaseActivity
        extends AppCompatActivity
        implements NetworkChangeReceiver.NetworkChangeListener {

    private NetworkChangeReceiver networkChangeReceiver;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());


        ButterKnife.bind(this);
//        Timber.plant(new Timber.DebugTree());
        networkChangeReceiver = new NetworkChangeReceiver();
        init();


    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        networkChangeReceiver.setNetworkChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(networkChangeReceiver);
        networkChangeReceiver.unsetNetworkChangeListener();
    }


    @Override
    public void isConnected(boolean connected) {

        if (connected) {
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
