package com.screenlocker.secure.mdm.base;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.ViewGroup;

import com.screenlocker.secure.R;
import com.screenlocker.secure.internetavailabilitychecker.InternetAvailabilityChecker;
import com.screenlocker.secure.internetavailabilitychecker.InternetConnectivityListener;


import butterknife.ButterKnife;

import static com.screenlocker.secure.mdm.utils.LifecycleReceiver.BACKGROUND;
import static com.screenlocker.secure.mdm.utils.LifecycleReceiver.FOREGROUND;
import static com.screenlocker.secure.mdm.utils.LifecycleReceiver.LIFECYCLE_ACTION;
import static com.screenlocker.secure.mdm.utils.LifecycleReceiver.STATE;


public abstract class BaseActivity
        extends AppCompatActivity
        implements InternetConnectivityListener {


    private Snackbar snackbar;
    private InternetAvailabilityChecker mInternetAvailabilityChecker;

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
        mInternetAvailabilityChecker = InternetAvailabilityChecker.getInstance();
        mInternetAvailabilityChecker.addInternetConnectivityListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mInternetAvailabilityChecker.removeInternetConnectivityChangeListener(this);
    }


    @Override
    public void onInternetConnectivityChanged(boolean connected) {

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
