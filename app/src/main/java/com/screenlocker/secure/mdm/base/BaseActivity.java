package com.screenlocker.secure.mdm.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.screenlocker.secure.R;
import com.screenlocker.secure.network.InternetConnectivityListener;
import com.screenlocker.secure.service.NetworkSocketAlarm;

import butterknife.ButterKnife;

import static com.screenlocker.secure.mdm.utils.LifecycleReceiver.BACKGROUND;
import static com.screenlocker.secure.mdm.utils.LifecycleReceiver.FOREGROUND;
import static com.screenlocker.secure.mdm.utils.LifecycleReceiver.LIFECYCLE_ACTION;
import static com.screenlocker.secure.mdm.utils.LifecycleReceiver.STATE;


public abstract class BaseActivity
        extends AppCompatActivity
        implements InternetConnectivityListener {


    private Snackbar snackbar;

    private NetworkSocketAlarm networkSocketAlarm;

    private void setNetworkLister() {
        networkSocketAlarm = new NetworkSocketAlarm();
        networkSocketAlarm.setListener(this);
    }

    private void unSetNetworkLister() {
        if (networkSocketAlarm != null)
            networkSocketAlarm.unsetListener();
    }


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
        setNetworkLister();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unSetNetworkLister();
    }


    @Override
    public void onInternetStateChanged(boolean connected) {

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
