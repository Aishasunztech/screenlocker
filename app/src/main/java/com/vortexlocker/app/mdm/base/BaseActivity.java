package com.vortexlocker.app.mdm.base;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

import com.vortexlocker.app.R;
import com.vortexlocker.app.mdm.utils.NetworkChangeReceiver;

import butterknife.ButterKnife;

import static com.vortexlocker.app.mdm.utils.LifecycleReceiver.BACKGROUND;
import static com.vortexlocker.app.mdm.utils.LifecycleReceiver.FOREGROUND;
import static com.vortexlocker.app.mdm.utils.LifecycleReceiver.LIFECYCLE_ACTION;
import static com.vortexlocker.app.mdm.utils.LifecycleReceiver.STATE;
import static com.vortexlocker.app.utils.Utils.collapseNow;


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
