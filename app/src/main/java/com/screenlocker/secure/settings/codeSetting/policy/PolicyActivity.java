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
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.secure.launcher.R;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.mdm.MainActivity;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;
import com.secureSetting.SecureSettingsMain;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.LOADING_POLICY;
import static com.screenlocker.secure.utils.AppConstants.POLICY_NAME;
import static com.screenlocker.secure.utils.LifecycleReceiver.BACKGROUND;
import static com.screenlocker.secure.utils.LifecycleReceiver.LIFECYCLE_ACTION;
import static com.screenlocker.secure.utils.LifecycleReceiver.STATE;

public class PolicyActivity extends BaseActivity implements View.OnClickListener, LockScreenService.PolicyResponse {


    private boolean isBackPressed;

    private boolean isNetworkDialogOpen, isLinkDialogOpen;
    private ConstraintLayout containerLayout;
    private CardView cardView;

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
        if (!CommonUtils.isNetworkConneted(this)) {
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
        cardView = findViewById(R.id.policy_card);
        cardView.setBackgroundResource(R.drawable.black_circle);


        containerLayout = findViewById(R.id.rootView);
        progressBar = findViewById(R.id.progress);
        etPolicyName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });
    }

    private void setToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getString(R.string.policy_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnDefaultPolicy:
                handleLoadPolicy(getResources().getString(R.string.default_policy), getResources().getString(R.string.default_policy_message));
                break;
            case R.id.btnLoadPolicy:
                submit();
                break;
        }

    }

    private void submit() {
        String policyName = etPolicyName.getText().toString();
        if (TextUtils.isEmpty(policyName)) {
            etPolicyName.setError(getResources().getString(R.string.enter_policy_name));
        } else {
//            handleLoadPolicy(policyName, "This will load the policy \"" + policyName + "\" to this device.");
            handleLoadPolicy(policyName, getResources().getString(R.string.load_policy_to_device, policyName));
        }
    }


    // method to load default or  policy
    private void handleLoadPolicy(String policyName, String text) {


        new AlertDialog.Builder(PolicyActivity.this).
                setTitle(getResources().getString(R.string.policy_warning_title))
                .setMessage(text).setPositiveButton(getResources().getString(R.string.ok_text), (dialogInterface, i) -> {

            if (mService != null) {
                mService.onLoadPolicy(policyName);
                PrefUtils.saveStringPref(PolicyActivity.this, POLICY_NAME, policyName);
                processingView();
            }

        })
                .setNegativeButton(getResources().getString(R.string.cancel_text), (dialogInterface, i) -> {
                    dialogInterface.cancel();
                })
                .show();
    }


    private void showLinkDialog() {

        isLinkDialogOpen = true;

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getResources().getString(R.string.device_not_linked));
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);

        alertDialog.setCancelable(false);
        alertDialog.setMessage(getResources().getString(R.string.link_device_to_proceed));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.link_device_button), (dialog, which) -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

        });


        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.cancel_capital),
                (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                });
        alertDialog.show();

    }

    LockScreenService mService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Set the service messenger and connected status
            // cast the IBinder and get MyService instance
            LockScreenService.LocalBinder binder = (LockScreenService.LocalBinder) service;

            mService = binder.getService();
            mService.setListener(PolicyActivity.this); // register


        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void doBindService() {

        bindService(new Intent(getBaseContext(),
                LockScreenService.class), connection, Context.BIND_AUTO_CREATE);

    }

    private void doUnbindService() {
        if (mService != null)
            mService.setListener(null);
        unbindService(connection);
    }

    private void showNetworkDialog() {

        isNetworkDialogOpen = true;

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getResources().getString(R.string.network_not_connected));
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);

        alertDialog.setCancelable(false);
        alertDialog.setMessage(getResources().getString(R.string.network_not_connected_message));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.network_setup), (dialog, which) -> {
            Intent intent = new Intent(this, SecureSettingsMain.class);
            intent.putExtra("show_default", "show_default");
            startActivity(intent);

        });


        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.cancel_capital),
                (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                });
        alertDialog.show();

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
            PrefUtils.saveBooleanPref(this, LOADING_POLICY, true);
            AppExecutor.getInstance().getMainThread().execute(this::successView);
        } else {
            AppExecutor.getInstance().getMainThread().execute(this::errorView);
        }
    }


    private void errorView() {
        etPolicyName.setEnabled(true);
        btnLoadPolicy.setEnabled(true);
        btnDefault.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        etPolicyName.setError(getResources().getString(R.string.invalid_policy_name));
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
