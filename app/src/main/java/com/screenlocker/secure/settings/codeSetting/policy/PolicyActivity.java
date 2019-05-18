package com.screenlocker.secure.settings.codeSetting.policy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.screenlocker.secure.R;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.mdm.MainActivity;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.settings.codeSetting.CodeSettingActivity;
import com.screenlocker.secure.socket.service.SocketService;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.LifecycleReceiver;
import com.screenlocker.secure.utils.PrefUtils;
import com.secureSetting.SecureSettingsMain;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.LOADING_POLICY;
import static com.screenlocker.secure.utils.LifecycleReceiver.BACKGROUND;
import static com.screenlocker.secure.utils.LifecycleReceiver.LIFECYCLE_ACTION;
import static com.screenlocker.secure.utils.LifecycleReceiver.STATE;

public class PolicyActivity extends BaseActivity implements View.OnClickListener, SocketService.PolicyResponse {


    private boolean isBackPressed;

    private boolean isNetworkDialogOpen, isLinkDialogOpen;
    private ConstraintLayout containerLayout;

    private Button btnDefault, btnLoadPolicy;
    private EditText etPolicyName;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy);
        doBindService();
        setToolbar();
        setIds();

    }


    @Override
    protected void onStart() {
        super.onStart();
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        if (!isConnected) {
            showNetworkDialog();

        } else {
            isNetworkDialogOpen = false;
        }


        boolean isDeviceLink = PrefUtils.getBooleanPref(this, AppConstants.DEVICE_LINKED_STATUS);

        if (isConnected && !isDeviceLink) {
            showLinkDialog();
        }

    }


    private void setIds() {
        btnDefault = findViewById(R.id.btnDefaultPolicy);
        btnDefault.setOnClickListener(this);
        btnLoadPolicy = findViewById(R.id.btnLoadPolicy);
        btnLoadPolicy.setOnClickListener(this);
        etPolicyName = findViewById(R.id.edtTxtPin);
        containerLayout = findViewById(R.id.rootView);
        progressBar = findViewById(R.id.progress);
    }

    private void setToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Load Policy");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnDefaultPolicy:
                handleLoadPolicy("default_policy", "This will push the default Policy set by your Dealer.");
                break;
            case R.id.btnLoadPolicy:
                String policyName = etPolicyName.getText().toString();
                if (TextUtils.isEmpty(policyName)) {
                    etPolicyName.setError("please enter policy name.");
                } else {
                    handleLoadPolicy(policyName, "This will load the policy \"" + policyName + "\" to this device.");
                }
                break;
        }

    }


    // method to load default or  policy
    private void handleLoadPolicy(String policyName, String text) {


        new AlertDialog.Builder(PolicyActivity.this).
                setTitle("Warning!")
                .setMessage(text).setPositiveButton("Ok", (dialogInterface, i) -> {

            if (mService != null) {
                mService.onLoadPolicy(policyName);
                processingView();
            }

        })
                .setNegativeButton("Cancel", (dialogInterface, i) -> {
                    dialogInterface.cancel();
                })
                .show();
    }


    private void showLinkDialog() {

        isLinkDialogOpen = true;

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Device Not Linked!");
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);

        alertDialog.setCancelable(false);
        alertDialog.setMessage("Please link your device before proceeding.");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "LINK DEVICE", (dialog, which) -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

        });


        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                });
        alertDialog.show();

    }

    SocketService mService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Set the service messenger and connected status
            // cast the IBinder and get MyService instance
            SocketService.LocalBinder binder = (SocketService.LocalBinder) service;

            mService = binder.getService();
            mService.setListener(PolicyActivity.this); // register


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void doBindService() {

        bindService(new Intent(getBaseContext(),
                SocketService.class), connection, Context.BIND_AUTO_CREATE);

    }

    private void doUnbindService() {
        mService.setListener(null);
        unbindService(connection);
    }

    private void showNetworkDialog() {

        isNetworkDialogOpen = true;

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Network Not Connected!");
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);

        alertDialog.setCancelable(false);
        alertDialog.setMessage("Please connect to the internet before proceeding.");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "NETWORK SETUP", (dialog, which) -> {
            Intent intent = new Intent(this, SecureSettingsMain.class);
            intent.putExtra("show_default", "show_default");
            startActivity(intent);

        });


        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                });
        alertDialog.show();

    }

    @Override
    public void onStateChange(int state) {
        switch (state) {

            case LifecycleReceiver.FOREGROUND:
                Timber.e("onStateChange: FOREGROUND");
                break;

            case LifecycleReceiver.BACKGROUND:
                Timber.tag("TAGLLLL").e("onStateChange: BACKGROUND");
                this.finish();
                break;

            default:
                Timber.e("onStateChange: SOMETHING");
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isBackPressed = false;
        isLinkDialogOpen = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isBackPressed && !isNetworkDialogOpen && !isLinkDialogOpen) {
            try {
                containerLayout.setVisibility(View.INVISIBLE);
                finish();
                if (CodeSettingActivity.codeSettingsInstance != null) {

                    //  finish previous activity and this activity
                    CodeSettingActivity.codeSettingsInstance.finish();
                }
            } catch (Exception ignored) {
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!isBackPressed && !isNetworkDialogOpen) {
            Intent intent = new Intent(LIFECYCLE_ACTION);
            intent.putExtra(STATE, BACKGROUND);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    @Override
    public void onResponse(boolean status) {
        Timber.d("Policy status %s", status);
        if (status) {
            if (!PrefUtils.getBooleanPref(this, LOADING_POLICY)) {
                AppExecutor.getInstance().getMainThread().execute(this::successView);
            } else {
                if (!getPolicyDialog().isShowing()) {
                    getPolicyDialog().show();
                }
            }

        } else {
            AppExecutor.getInstance().getMainThread().execute(this::errorView);
        }
    }


    private void errorView() {
        etPolicyName.setEnabled(true);
        btnLoadPolicy.setEnabled(true);
        btnDefault.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        etPolicyName.setError("Invalid policy name");
    }

    private void processingView() {
        etPolicyName.setEnabled(false);
        btnLoadPolicy.setEnabled(false);
        btnDefault.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void successView() {

        progressBar.setVisibility(View.GONE);
        if (!getPolicyDialog().isShowing()) {
            getPolicyDialog().show();
        }

    }

}
