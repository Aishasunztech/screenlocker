package com.vortexlocker.app.mdm.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.vortexlocker.app.R;
import com.vortexlocker.app.app.MyApplication;
import com.vortexlocker.app.mdm.MainActivity;
import com.vortexlocker.app.mdm.base.BaseActivity;
import com.vortexlocker.app.mdm.retrofitmodels.LinkDeviceModel;
import com.vortexlocker.app.mdm.retrofitmodels.LinkDeviceResponse;
import com.vortexlocker.app.mdm.retrofitmodels.LinkStatusModel;
import com.vortexlocker.app.mdm.retrofitmodels.LinkStatusResponse;
import com.vortexlocker.app.mdm.utils.DeviceIdUtils;
import com.vortexlocker.app.settings.SettingsActivity;
import com.vortexlocker.app.utils.AppConstants;
import com.vortexlocker.app.utils.PrefUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.vortexlocker.app.utils.AppConstants.AUTH_TOKEN;
import static com.vortexlocker.app.utils.AppConstants.AUTO_LOGIN_PIN;
import static com.vortexlocker.app.utils.AppConstants.DEVICE_LINKED;
import static com.vortexlocker.app.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.vortexlocker.app.utils.AppConstants.DEVICE_NEW;
import static com.vortexlocker.app.utils.AppConstants.DEVICE_PENDING;
import static com.vortexlocker.app.utils.AppConstants.TEMP_AUTO_LOGIN_PIN;
import static com.vortexlocker.app.utils.AppConstants.TOKEN_EXPIRED;
import static com.vortexlocker.app.utils.AppConstants.TOKEN_INVALID;
import static com.vortexlocker.app.utils.AppConstants.TOKEN_NOT_PROVIDED;
import static com.vortexlocker.app.utils.AppConstants.TOUR_STATUS;


public class LinkDeviceActivity extends BaseActivity {

    private static final String TAG = LinkDeviceActivity.class.getSimpleName();
    private static final String DEALER_ID_DEFAULT = "not linked yet";

    @BindView(R.id.lytSwipeReferesh)
    SwipeRefreshLayout lytSwipeReferesh;

    @BindView(R.id.tvLinkedStatus)
    TextView tvLinkedStatus;

    @BindView(R.id.tvLinkedDealerPin)
    TextView tvLinkedDealerPin;
    String linkedDealerPin;
    @BindView(R.id.tvCurrentDealerID)
    TextView tvCurrentDealerID;
    @BindView(R.id.tvDeviceId)
    TextView tvDeviceId;
    String currentDealerID;
    @BindView(R.id.tvIMEI)
    TextView tvIMEI;
    List<String> IMEI;
    @BindView(R.id.tvSimNo)
    TextView tvSimNo;
    List<String> SimNo;
    @BindView(R.id.tvSerialNo)
    TextView tvSerialNo;
    String SerialNo;
    @BindView(R.id.tvMAC)
    TextView tvMAC;
    String MAC;
    @BindView(R.id.tvIP)
    TextView tvIP;
    String IP;

    @BindView(R.id.btnLinkDevice)
    Button btnLinkDevice;
    @BindView(R.id.btnStopLink)
    Button btnStopLink;
    String connectedDid;
    @BindView(R.id.imei2)
    TableRow imei2;
    @BindView(R.id.simno2)
    TableRow simno2;
    @BindView(R.id.tvSimNo2)
    TextView tvSimNo2;
    @BindView(R.id.tvIMEI2)
    TextView tvIMEI2;

    String defaultImei;

    @Override
    protected int getContentView() {
        return R.layout.activity_link_device;
    }

    @SuppressLint({"LogNotTimber", "HardwareIds"})
    @Override
    protected void init() {


        currentDealerID = PrefUtils.getStringPref(this, AppConstants.KEY_DEALER_ID);
        connectedDid = PrefUtils.getStringPref(this, AppConstants.KEY_CONNECTED_ID);

        linkedDealerPin = DEALER_ID_DEFAULT;

        //get imei of device
        IMEI = DeviceIdUtils.getIMEI(this);
        // get sim number
        SimNo = DeviceIdUtils.getSimNumber(this);
        //get serial number
        SerialNo = DeviceIdUtils.getSerialNumber();
        // get ip address
        IP = DeviceIdUtils.getIPAddress(true);
        // get mac address
        MAC = DeviceIdUtils.getMacAddress();

        defaultImei = (IMEI.size() >= 1) ? IMEI.get(0) : "";

        Log.e(TAG, "init: \n" +
                IMEI + "\n" +
                SimNo + "\n" +
                SerialNo + "\n" +
                IP + "\n" +
                MAC
        );

        currentDealerID = (currentDealerID == null) ? "" : currentDealerID;
        connectedDid = (connectedDid == null) ? "" : connectedDid;
        linkedDealerPin = (linkedDealerPin == null) ? "" : linkedDealerPin;

        SerialNo = (SerialNo == null) ? "" : SerialNo;
        MAC = (MAC == null) ? "" : MAC;
        IP = (IP == null) ? "" : IP;


        int size = (SimNo.size() >= 2) ? 2 : SimNo.size();

        switch (size) {
            case 0:
                tvSimNo.setText("");
                simno2.setVisibility(View.GONE);
                break;
            case 1:
                tvSimNo.setText(SimNo.get(0));
                simno2.setVisibility(View.GONE);
                break;
            case 2:
                tvSimNo.setText(SimNo.get(0));
                tvSimNo2.setText(SimNo.get(1));
                break;
        }

        int imeiSize = (IMEI.size() >= 2) ? 2 : IMEI.size();

        switch (imeiSize) {
            case 0:
                tvIMEI.setText("");
                imei2.setVisibility(View.GONE);
                break;
            case 1:
                tvIMEI.setText(IMEI.get(0));
                imei2.setVisibility(View.GONE);
                break;
            case 2:
                tvIMEI.setText(IMEI.get(0));
                tvIMEI2.setText(IMEI.get(1));
                break;
        }

        tvLinkedDealerPin.setText(linkedDealerPin);
        tvCurrentDealerID.setText(currentDealerID);


        tvSerialNo.setText(SerialNo);
        tvMAC.setText(MAC);
        tvIP.setText(IP);

        checkLinkDeviceStatus();

        lytSwipeReferesh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Timber.i("<<<<<SwipedToRefresh>>>>>");
                checkLinkDeviceStatus();
//                checkDeviceAccountStatus();
            }
        });

    }


//    private void checkDeviceAccountStatus() {
//
//        ((MyApplication) getApplicationContext())
//                .getApiOneCaller()
//                .checkAccountStatus(new AccountStatusModel(DeviceIdUtils.getIMEI(this),DeviceIdUtils.getMacAddress()))
//                .enqueue(new Callback<AccountStatusResponse>() {
//                    @Override
//                    public void onResponse(Call<AccountStatusResponse> call, Response<AccountStatusResponse> response) {
//
//                        if (response.isSuccessful()){
//
//                            if (response.body().getStatus()){
//
//                            }else {
//
//                            }
//
//                        }else {
////                            setAccountStatus();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<AccountStatusResponse> call, Throwable t) {
//
//                    }
//                });
//    }

    /**
     * check if its linked or  pending or un-linked
     */
    public void checkLinkDeviceStatus() {

        freshViewState();

        ((MyApplication) getApplicationContext())
                .getApiOneCaller()
                .linkDeviceStatus(
                        new LinkStatusModel(defaultImei, MAC),
                        PrefUtils.getStringPref(this, AUTH_TOKEN)
//                        +"INVALID_TOKEN"
                )
                .enqueue(new Callback<LinkStatusResponse>() {
                    @Override
                    public void onResponse(Call<LinkStatusResponse> call, Response<LinkStatusResponse> response) {

                        if (response.isSuccessful()) {

                            LinkStatusResponse lsr = response.body();

                            if (lsr.getStatus() != null) {
                                tvDeviceId.setText(lsr.getDeviceId());

                                switch (lsr.getStatus()) {

                                    case DEVICE_NEW:
                                        newLinkViewState();
                                        setDealerPin(DEALER_ID_DEFAULT/*+linkedDealerPin*/);
                                        break;

                                    case DEVICE_PENDING:
                                        String tempPin = PrefUtils.getStringPref(LinkDeviceActivity.this, TEMP_AUTO_LOGIN_PIN);
                                        PrefUtils.saveStringPref(LinkDeviceActivity.this,
                                                AUTO_LOGIN_PIN,
                                                tempPin);

                                        Log.e(TAG, "onResponse: AUTOLOGINADDED" + tempPin);
                                        pendingLinkViewState();
//                                        setDealerPin(lsr.getDealer_id());
                                        setDealerPin("" + PrefUtils.getStringPref(LinkDeviceActivity.this, AUTO_LOGIN_PIN));
                                        break;

                                    case DEVICE_LINKED:
                                        String tempPin1 = PrefUtils.getStringPref(LinkDeviceActivity.this, TEMP_AUTO_LOGIN_PIN);
                                        PrefUtils.saveStringPref(LinkDeviceActivity.this,
                                                AUTO_LOGIN_PIN,
                                                tempPin1);

                                        Log.e(TAG, "onResponse: AUTOLOGINADDED" + tempPin1);


                                        approvedLinkViewState();
//                                        setDealerPin(lsr.getDealer_id());
                                        setDealerPin("" + PrefUtils.getStringPref(LinkDeviceActivity.this, AUTO_LOGIN_PIN));


                                        break;
                                }
                            } else if (lsr.getMsg() != null) {

                                switch (lsr.getMsg()) {

                                    case TOKEN_EXPIRED:
                                    case TOKEN_INVALID:
                                    case TOKEN_NOT_PROVIDED:
                                        //start main activity if token is not provided
                                        Toast.makeText(LinkDeviceActivity.this, "Session expired", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LinkDeviceActivity.this, MainActivity.class));
                                        finish();
                                }
                            }

                        } else {
                            Toast.makeText(LinkDeviceActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                        }

                        Log.e(TAG, "REFRESHED: " + PrefUtils.getStringPref(LinkDeviceActivity.this, AUTO_LOGIN_PIN));

                        finishedRefreshing();
                    }

                    @Override
                    public void onFailure(Call<LinkStatusResponse> call, Throwable t) {

                        finishedRefreshing();
                    }
                });
    }

    private void setDealerPin(String id_or_msg) {
        tvLinkedDealerPin.setText(id_or_msg);
    }

    @OnClick(R.id.btnLinkDevice)
    public void onClickBtnLinkDevice() {
        if (btnLinkDevice.getText().equals("Next")) {
            PrefUtils.saveBooleanPref(LinkDeviceActivity.this, DEVICE_LINKED_STATUS, true);
            Intent intent = new Intent(LinkDeviceActivity.this, SettingsActivity.class);
            startActivity(intent);
            finish();
        } else {
            processingLinkViewState();

            ((MyApplication) getApplicationContext())
                    .getApiOneCaller()
                    .linkDeviceToDealer(
                            new LinkDeviceModel(currentDealerID, connectedDid, IMEI, SimNo, SerialNo, MAC, IP),
                            PrefUtils.getStringPref(LinkDeviceActivity.this, AUTH_TOKEN)
//                                +"INVALID_TOKEN"
                    )
                    .enqueue(new Callback<LinkDeviceResponse>() {
                        @Override
                        public void onResponse(Call<LinkDeviceResponse> call, Response<LinkDeviceResponse> response) {

                            if (response.isSuccessful()) {

                                LinkDeviceResponse ldr = response.body();

                                if (ldr.getStatus() != null) {

                                    if (ldr.getStatus().equals("true")) {
                                        checkLinkDeviceStatus();

                                    } else {

                                        checkLinkDeviceStatus();
                                    }

                                } else if (ldr.getMsg() != null) {

                                    switch (ldr.getMsg()) {

                                        case TOKEN_EXPIRED:
                                        case TOKEN_INVALID:
                                        case TOKEN_NOT_PROVIDED:
                                            Toast.makeText(LinkDeviceActivity.this, "Session expired", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(LinkDeviceActivity.this, MainActivity.class));
                                            finish();
                                    }
                                } else {
                                    checkLinkDeviceStatus();
                                }
                            } else {
                                checkLinkDeviceStatus();
                            }
                        }

                        @Override
                        public void onFailure(Call<LinkDeviceResponse> call, Throwable t) {
//                        newLinkViewState();
                            checkLinkDeviceStatus();
                        }
                    });
        }

    }

    @OnClick(R.id.btnStopLink)
    public void onClickBtnUnlinkDevice() {

        processingUnlinkViewState();

        ((MyApplication) getApplicationContext())
                .getApiOneCaller()
                .stopLinkingDevice(
                        defaultImei,
                        PrefUtils.getStringPref(LinkDeviceActivity.this, AUTH_TOKEN)
//                                +"INVALID_TOKEN"
                )
                .enqueue(new Callback<LinkDeviceResponse>() {
                    @Override
                    public void onResponse(Call<LinkDeviceResponse> call, Response<LinkDeviceResponse> response) {

                        if (response.isSuccessful()) {

                            LinkDeviceResponse ldr = response.body();

                            if (ldr.getStatus().equals("true")) {

                                if (response.body().getStatus().equals("true")) {
                                    Log.e(TAG, "onResponse: AUTOLOGINREMOVED");
                                    PrefUtils.saveStringPref(LinkDeviceActivity.this, AUTO_LOGIN_PIN, null);
                                    checkLinkDeviceStatus();
                                } else {

                                    checkLinkDeviceStatus();
                                }

                            } else if (ldr.getMsg() != null) {

                                switch (ldr.getMsg()) {

                                    case TOKEN_EXPIRED:
                                    case TOKEN_INVALID:
                                    case TOKEN_NOT_PROVIDED:
                                        Toast.makeText(LinkDeviceActivity.this, R.string.session_expired, Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(LinkDeviceActivity.this, MainActivity.class));
                                        finish();
                                }
                            }

                        } else {
                            checkLinkDeviceStatus();
                        }
                    }

                    @Override
                    public void onFailure(Call<LinkDeviceResponse> call, Throwable t) {

                        checkLinkDeviceStatus();
                    }
                });
    }

    private void newLinkViewState() {
        btnLinkDevice.setText(R.string.link_device);
        btnLinkDevice.setVisibility(View.VISIBLE);
        btnLinkDevice.setEnabled(true);
        btnStopLink.setVisibility(View.GONE);
        tvLinkedStatus.setText(R.string.device_not_linked);
        tvLinkedStatus.setTextColor(Color.RED);
        tvLinkedStatus.setVisibility(View.VISIBLE);
    }

    private void approvedLinkViewState() {
        btnLinkDevice.setVisibility(View.VISIBLE);
        btnStopLink.setVisibility(View.GONE);
        btnLinkDevice.setText("Next");
        tvLinkedStatus.setText(R.string.device_already_linked);
        tvLinkedStatus.setTextColor(ContextCompat.getColor(this, R.color.green_dark));
        tvLinkedStatus.setVisibility(View.VISIBLE);

    }

    private void pendingLinkViewState() {
        btnLinkDevice.setVisibility(View.GONE);
        btnStopLink.setText(R.string.stop_linking);
        btnStopLink.setVisibility(View.VISIBLE);
        btnStopLink.setEnabled(true);
        tvLinkedStatus.setText(R.string.link_request_pending);
        tvLinkedStatus.setTextColor(Color.BLUE);
        tvLinkedStatus.setVisibility(View.VISIBLE);
    }

    /**
     * showing the processing state
     */
    private void processingLinkViewState() {
        btnLinkDevice.setEnabled(false);
        btnLinkDevice.setText(R.string.processing);
        btnLinkDevice.setVisibility(View.VISIBLE);
        btnStopLink.setVisibility(View.GONE);
        tvLinkedStatus.setText(R.string.link_request_pending);
        tvLinkedStatus.setTextColor(Color.BLUE);
        tvLinkedStatus.setVisibility(View.INVISIBLE);
    }

    private void processingUnlinkViewState() {
        btnLinkDevice.setVisibility(View.GONE);
        btnStopLink.setEnabled(false);
        btnStopLink.setText(R.string.processing);
        btnStopLink.setVisibility(View.VISIBLE);
        tvLinkedStatus.setText(R.string.link_request_pending);
        tvLinkedStatus.setTextColor(Color.BLUE);
        tvLinkedStatus.setVisibility(View.INVISIBLE);
    }

    private void freshViewState() {
        btnLinkDevice.setVisibility(View.GONE);
        btnStopLink.setVisibility(View.GONE);
        tvLinkedStatus.setVisibility(View.GONE);
    }

    /**
     * set refreshing to false
     */
    private void finishedRefreshing() {
        if (!(LinkDeviceActivity.this.isDestroyed() || LinkDeviceActivity.this.isFinishing()) && lytSwipeReferesh.isRefreshing()) {
            lytSwipeReferesh.setRefreshing(false);
        }
    }

}
