package com.screenlocker.secure.settings;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.socket.SocketManager;
import com.screenlocker.secure.socket.interfaces.OnSocketConnectionListener;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Utils;
import com.secureSetting.SecureSettingsMain;

import java.util.List;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.EMERGENCY_FLAG;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.URL_1;
import static com.screenlocker.secure.utils.AppConstants.URL_2;
import static com.screenlocker.secure.utils.CommonUtils.getRemainingDays;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener, OnSocketConnectionListener {

    private TextView tvImei1, tvImei2, tvExpiresIn, tvStatus, tvDeviceId, onlineStatus;
    private SwipeRefreshLayout swipeRefreshLayout;

    private SocketManager socketManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        url_1 = findViewById(R.id.url_1);


        socketManager = SocketManager.getInstance();
        socketManager.setSocketConnectionListener(this);

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
        swipeRefreshLayout = findViewById(R.id.lytSwipeReferesh);

        onlineStatus.setOnClickListener(this);
        tvDeviceId.setOnClickListener(this);
        tvStatus.setOnClickListener(this);
        tvImei1.setOnClickListener(this);
        tvImei2.setOnClickListener(this);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
            refresh();
        });

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

    @Override
    protected void onResume() {
        super.onResume();
        if (SocketManager.getInstance().getSocket() != null && SocketManager.getInstance().getSocket().connected()) {
            onlineStatus.setText(getResources().getString(R.string.status_online));
        } else {
            onlineStatus.setText(getResources().getString(R.string.status_disconnected));
        }


    }


    private void showNetworkDialog() {

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getResources().getString(R.string.network_not_connected));
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);

        alertDialog.setMessage(getResources().getString(R.string.network_not_connected_message));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.network_setup), (dialog, which) -> {
            Intent intent = new Intent(this, SecureSettingsMain.class);
            intent.putExtra("show_default", "show_default");
            startActivity(intent);

        });


        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.cancel_text),
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();

    }

    private AsyncCalls asyncCalls;

    private void refresh() {

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        if (isConnected) {

            if (SocketManager.getInstance().getSocket() != null && SocketManager.getInstance().getSocket().connected()) {
                runOnUiThread(() -> onlineStatus.setText(getResources().getString(R.string.status_online)));
            } else {
                runOnUiThread(() -> onlineStatus.setText(getResources().getString(R.string.status_disconnected)));
                String[] urls = {URL_1, URL_2};
                if (asyncCalls != null) {
                    asyncCalls.cancel(true);
                }
                asyncCalls = new AsyncCalls(output -> {
                    Timber.d("output : " + output);
                    if (output != null) {
                        PrefUtils.saveStringPref(this, LIVE_URL, output);
                        String live_url = PrefUtils.getStringPref(this, LIVE_URL);
                        Timber.d("live_url %s", live_url);
                        MyApplication.oneCaller = RetrofitClientInstance.getRetrofitInstance(live_url + MOBILE_END_POINT).create(ApiOneCaller.class);
                        boolean linkStatus = PrefUtils.getBooleanPref(this, AppConstants.DEVICE_LINKED_STATUS);
                        Timber.d("LinkStatus :" + linkStatus);
                        if (linkStatus) {
                            Timber.d("LinkStatus :" + linkStatus);
                            String macAddress = DeviceIdUtils.generateUniqueDeviceId(this);
                            String serialNo = DeviceIdUtils.getSerialNumber();
                            runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false));
                            new ApiUtils(this, macAddress, serialNo);
                        }
                    } else {
                        runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false));
                    }
                }, this, urls);// checking hosts
                asyncCalls.execute();
            }
        } else {
            showNetworkDialog();
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvDeviceId:
                Utils.copyToClipBoard(this, AppConstants.COPIED_DEVICE_ID, tvDeviceId.getText().toString(), "DeviceId copied to clipboard");
                break;
            case R.id.tvLinkedStatus:
                Utils.copyToClipBoard(this, AppConstants.COPIED_LINKED_STATUS, onlineStatus.getText().toString(), "LinkedStatus copied to clipboard");

                break;
            case R.id.tvDeviceStatus:
                Utils.copyToClipBoard(this, AppConstants.COPIED_DEVICE_STATUS, tvStatus.getText().toString(), "DeviceStatus copied to clipboard");

                break;
            case R.id.tvImei1:
                Utils.copyToClipBoard(this, AppConstants.COPIED_IMEI_1, tvImei1.getText().toString(), "IMEI copied to clipboard");

                break;
            case R.id.tvImei2:
                Utils.copyToClipBoard(this, AppConstants.COPIED_IMEI_2, tvImei2.getText().toString(), "IMEI copied to clipboard");

                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socketManager.removeSocketConnectionListener(this);
        socketManager.removeAllSocketConnectionListener();

    }

    @Override
    public void onSocketEventFailed() {
        Timber.d("Socket event failed");
        new ApiUtils(this, DeviceIdUtils.generateUniqueDeviceId(this), DeviceIdUtils.getSerialNumber());
    }

    @Override
    public void onSocketConnectionStateChange(int socketState) {
        if (socketState == 1) {
            runOnUiThread(() -> onlineStatus.setText("Connecting..."));

        } else if (socketState == 2) {
            Timber.d("Socket is connected");

            runOnUiThread(() -> onlineStatus.setText(getResources().getString(R.string.status_online)));

        } else if (socketState == 3) {
            Timber.d("Socket is disconnected");
            runOnUiThread(() -> onlineStatus.setText(getResources().getString(R.string.status_disconnected)));

        }
    }


    public void emergencyFlag(View view) {

        if (PrefUtils.getBooleanPref(this, EMERGENCY_FLAG)) {
            Toast.makeText(this, "EMERGENCY FLAG OFF", Toast.LENGTH_SHORT).show();
            PrefUtils.saveBooleanPref(this, EMERGENCY_FLAG, false);
        } else {
            Toast.makeText(this, "EMERGENCY FLAG ON", Toast.LENGTH_SHORT).show();
            PrefUtils.saveBooleanPref(this, EMERGENCY_FLAG, true);
        }

    }


    @Override
    public void onInternetConnectionStateChange(int socketState) {

    }

    private EditText url_1;

    public void changeUrl(View view) {
        String url = url_1.getText().toString();

        if (TextUtils.isEmpty(url)) {
            url_1.setError("Please enter valid url !");
            return;
        }


        URL_1 = url;

        Toast.makeText(this, "URL changed Successfully.", Toast.LENGTH_SHORT).show();
    }
}
