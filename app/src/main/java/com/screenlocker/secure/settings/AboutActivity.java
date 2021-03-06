package com.screenlocker.secure.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.socket.SocketManager;
import com.screenlocker.secure.socket.interfaces.OnSocketConnectionListener;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Utils;
import com.secure.launcher.R;
import com.secureSetting.SecureSettingsMain;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.CHAT_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.EMERGENCY_FLAG;
import static com.screenlocker.secure.utils.AppConstants.KEY_DEVICE_LINKED;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.OFFLINE_DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.PGP_EMAIL;
import static com.screenlocker.secure.utils.AppConstants.SIM_ID;
import static com.screenlocker.secure.utils.AppConstants.URL_1;
import static com.screenlocker.secure.utils.AppConstants.URL_2;
import static com.screenlocker.secure.utils.AppConstants.USER_ID;
import static com.screenlocker.secure.utils.CommonUtils.getRemainingDays;

public class AboutActivity extends BaseActivity implements View.OnClickListener, OnSocketConnectionListener {



    @BindView(R.id.tvUserId)
    TextView tvUserId;

    @BindView(R.id.tvLinkedStatus)
    TextView tvLinkedStatus;
    @BindView(R.id.tvDeviceStatus)
    TextView tvDeviceStatus;
    @BindView(R.id.tvLinkedDealerPin)
    TextView tvLinkedDealerPin;
    @BindView(R.id.tvSimNo)
    TextView tvSimNo;
    @BindView(R.id.tvSimNo2)
    TextView tvSimNo2;
    @BindView(R.id.tvSerialNo)
    TextView tvSerialNo;
    @BindView(R.id.tvMAC)
    TextView tvMAC;
    @BindView(R.id.tvIP)
    TextView tvIP;
    @BindView(R.id.tvPgpEmail)
    TextView tvPgpEmail;
    @BindView(R.id.tvChatId)
    TextView tvChatId;
    private TextView tvImei1, tvImei2, tvExpiresIn, tvStatus, tvDeviceId, onlineStatus;
    private SwipeRefreshLayout swipeRefreshLayout;

    private SocketManager socketManager;

    private Button button, button2;
    private ApiUtils apiUtils;

    int clickCount = 0;

    private List<String> simNos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        url_1 = findViewById(R.id.url_1);
        url_1.setText(URL_1);


        simNos = DeviceIdUtils.getSimNumber(this);

        socketManager = SocketManager.getInstance();
        socketManager.setSocketConnectionListener(this);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.account));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        onlineStatus = findViewById(R.id.tvLinkedStatus);
        tvDeviceId = findViewById(R.id.tvDeviceId);
        tvStatus = findViewById(R.id.tvDeviceStatus);
        tvExpiresIn = findViewById(R.id.tvExpiresIn);
        tvImei1 = findViewById(R.id.tvImei1);
        tvImei2 = findViewById(R.id.tvImei2);
        swipeRefreshLayout = findViewById(R.id.lytSwipeReferesh);

        button2 = findViewById(R.id.button2);
        button = findViewById(R.id.button);


        onlineStatus.setOnClickListener(this);
        tvDeviceId.setOnClickListener(this);
        tvStatus.setOnClickListener(this);
        tvImei1.setOnClickListener(this);
        tvImei2.setOnClickListener(this);
        tvChatId.setOnClickListener(this);
        tvPgpEmail.setOnClickListener(this);
        tvUserId.setOnClickListener(this);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
            refresh();
        });
        tvUserId.setText(prefUtils.getStringPref( USER_ID) == null ? "N/A" : prefUtils.getStringPref( USER_ID));

        tvLinkedDealerPin.setOnClickListener(this);
        tvSimNo.setOnClickListener(this);
        tvSimNo.setText(simNos != null && simNos.size() > 0 ? simNos.get(0) : "N/A");


        tvSimNo2.setOnClickListener(this);

        tvSimNo2.setText(simNos != null && simNos.size() > 1 ? simNos.get(1) : "N/A");
        tvPgpEmail.setText(prefUtils.getStringPref( PGP_EMAIL) != null ? prefUtils.getStringPref( PGP_EMAIL) : "N/A");

        tvSerialNo.setOnClickListener(this);
        tvMAC.setOnClickListener(this);
        tvIP.setOnClickListener(this);
        tvPgpEmail.setOnClickListener(this);


        String device_id = prefUtils.getStringPref( DEVICE_ID);
        if (device_id == null) {
            String offline_device = prefUtils.getStringPref( OFFLINE_DEVICE_ID);
            if (offline_device == null) {
                tvDeviceId.setText(getResources().getString(R.string.n_a));
            } else {
                tvDeviceId.setText(offline_device);
            }
        } else {
            tvDeviceId.setText(device_id);
        }

        /*Status*/
        String device_status = prefUtils.getStringPref( DEVICE_STATUS);
        boolean b = prefUtils.getBooleanPref( DEVICE_LINKED_STATUS);
        if (b) {
            tvStatus.setVisibility(View.VISIBLE);

            if (prefUtils.getBooleanPref(DEVICE_LINKED_STATUS)) {
                if (device_status == null) {
                    tvStatus.setText(getResources().getString(R.string.active));
                } else
                    tvStatus.setText(device_status);
            } else {
                tvStatus.setText(getResources().getString(R.string.n_a));
            }

        }

        String remaining_days = getRemainingDays(this, prefUtils);

        if (remaining_days != null) {
            tvExpiresIn.setText(remaining_days);
        } else {
            tvExpiresIn.setText(getResources().getString(R.string.n_a));
        }


        List<String> imeis = DeviceIdUtils.getIMEI(this);

        if (imeis.size() > 0) {
            String imei = imeis.get(0);
            if (imei != null) {
                tvImei1.setText(imei);
            } else {
                tvImei1.setText(getResources().getString(R.string.n_a));
            }
        }

        if (imeis.size() > 1) {
            String imei2 = imeis.get(1);
            if (imei2 != null) {
                tvImei2.setText(imei2);
            } else {
                tvImei2.setText(getResources().getString(R.string.n_a));
            }
        }
        String linkedDealerPin = prefUtils.getStringPref( KEY_DEVICE_LINKED);
        if (linkedDealerPin != null) {
            tvLinkedDealerPin.setText(linkedDealerPin);
        }
        tvMAC.setText(DeviceIdUtils.getMacAddress());
        tvSerialNo.setText(DeviceIdUtils.getSerialNumber());
        tvIP.setText(DeviceIdUtils.getIPAddress(true));

        String chat_Id = prefUtils.getStringPref( CHAT_ID);
        if (chat_Id != null) {
            tvChatId.setText(chat_Id);
        }
        // sim ID
        String sim_Id = prefUtils.getStringPref( SIM_ID);
        if (sim_Id != null) {
//            tvSimId.setText(sim_Id);
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


        clickCount = 0;

        button.setVisibility(View.GONE);
        button2.setVisibility(View.GONE);
        url_1.setVisibility(View.GONE);


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
                        prefUtils.saveStringPref( LIVE_URL, output);
                        String live_url = prefUtils.getStringPref( LIVE_URL);
                        Timber.d("live_url %s", live_url);
                        MyApplication.oneCaller = RetrofitClientInstance.getRetrofitInstance(live_url + MOBILE_END_POINT).create(ApiOneCaller.class);
                        boolean linkStatus = prefUtils.getBooleanPref( DEVICE_LINKED_STATUS);
                        Timber.d("LinkStatus :" + linkStatus);
                        if (linkStatus) {
                            Timber.d("LinkStatus :" + linkStatus);
                            String macAddress = DeviceIdUtils.generateUniqueDeviceId(this);
                            String serialNo = DeviceIdUtils.getSerialNumber();
                            runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false));
                            if (apiUtils == null)
                                apiUtils = new ApiUtils(this, macAddress, serialNo);
                            apiUtils.connectToSocket();
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
            case R.id.tvLinkedDealerPin:
                Utils.copyToClipBoard(this, AppConstants.COPIED_DEALER_PIN, tvLinkedDealerPin.getText().toString(), "Dealer pin copied to clipboard");
                break;
            case R.id.tvSimNo:
                Utils.copyToClipBoard(this, AppConstants.COPIED_SIM_NO_1, tvSimNo.getText().toString(), "Sim number copied to clipboard");
                break;
            case R.id.tvSimNo2:
                Utils.copyToClipBoard(this, AppConstants.COPIED_SIM_NO_2, tvSimNo2.getText().toString(), "Sim number copied to clipboard");
                break;
            case R.id.tvSerialNo:
                Utils.copyToClipBoard(this, AppConstants.COPIED_SERIAL_NUMBER, tvSerialNo.getText().toString(), "Serial number copied to clipboard");
                break;
            case R.id.tvMAC:
                Utils.copyToClipBoard(this, AppConstants.COPIED_MAC_ADDRESS, tvMAC.getText().toString(), "Mac address copied to clipboard");
                break;
            case R.id.tvIP:
                Utils.copyToClipBoard(this, AppConstants.COPIED_IP, tvIP.getText().toString(), "IP address copied to clipboard");
                break;
            case R.id.tvPgpEmail:
                Utils.copyToClipBoard(this, AppConstants.COPIED_PGP, tvPgpEmail.getText().toString(), "PGP email copied to clipboard");
                break;
            case R.id.tvChatId:
                Utils.copyToClipBoard(this, AppConstants.COPIED_CHAT_ID, tvChatId.getText().toString(), "CHAT ID copied to clipboard");
                break;
            case R.id.tvUserId:
                Utils.copyToClipBoard(this, AppConstants.COPIED_USER_ID, tvUserId.getText().toString(), "USER ID copied to clipboard");
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
        if (apiUtils == null)
            apiUtils = new ApiUtils(this, DeviceIdUtils.generateUniqueDeviceId(this), DeviceIdUtils.getSerialNumber());
        apiUtils.connectToSocket();
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

        if (prefUtils.getBooleanPref( EMERGENCY_FLAG)) {
            Toast.makeText(this, "SECURITY ON", Toast.LENGTH_SHORT).show();
            prefUtils.saveBooleanPref( EMERGENCY_FLAG, false);
        } else {
            Toast.makeText(this, "SECURITY OFF", Toast.LENGTH_SHORT).show();
            prefUtils.saveBooleanPref( EMERGENCY_FLAG, true);
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

    public void counter(View view) {

//        clickCount++;
//        int total = 6;
//
//        if (clickCount == 6) {
//            Toast.makeText(this, "Developer mode is enabled successfully. ", Toast.LENGTH_LONG).show();
//            button.setVisibility(View.VISIBLE);
//            button2.setVisibility(View.VISIBLE);
//            url_1.setVisibility(View.VISIBLE);
//        } else {
//            if (clickCount >= 2 && clickCount <= 6) {
//                Toast.makeText(this, total - clickCount + " more clicks to enable developer mode.", Toast.LENGTH_SHORT).show();
//            }
//        }

    }
}
