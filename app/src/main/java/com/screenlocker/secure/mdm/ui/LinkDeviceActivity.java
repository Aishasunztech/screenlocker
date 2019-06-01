package com.screenlocker.secure.mdm.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.mdm.base.BaseActivity;
import com.screenlocker.secure.mdm.retrofitmodels.DeleteDeviceResponse;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceStatusModel;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceStatusResponse;
import com.screenlocker.secure.mdm.retrofitmodels.LinkDeviceModel;
import com.screenlocker.secure.mdm.retrofitmodels.LinkDeviceResponse;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.ACTIVE;
import static com.screenlocker.secure.utils.AppConstants.ACTIVE_STATE;
import static com.screenlocker.secure.utils.AppConstants.CHAT_ID;
import static com.screenlocker.secure.utils.AppConstants.DEALER_NOT_FOUND;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS_KEY;
import static com.screenlocker.secure.utils.AppConstants.DUPLICATE_MAC;
import static com.screenlocker.secure.utils.AppConstants.DUPLICATE_MAC_AND_SERIAL;
import static com.screenlocker.secure.utils.AppConstants.DUPLICATE_SERIAL;
import static com.screenlocker.secure.utils.AppConstants.EXPIRED;
import static com.screenlocker.secure.utils.AppConstants.KEY_DEVICE_LINKED;
import static com.screenlocker.secure.utils.AppConstants.NEW_DEVICE;
import static com.screenlocker.secure.utils.AppConstants.PENDING;
import static com.screenlocker.secure.utils.AppConstants.PENDING_STATE;
import static com.screenlocker.secure.utils.AppConstants.PGP_EMAIL;
import static com.screenlocker.secure.utils.AppConstants.SIM_ID;
import static com.screenlocker.secure.utils.AppConstants.SUSPENDED;
import static com.screenlocker.secure.utils.AppConstants.TOKEN;
import static com.screenlocker.secure.utils.AppConstants.TRIAL;
import static com.screenlocker.secure.utils.AppConstants.UNLINKED_DEVICE;
import static com.screenlocker.secure.utils.AppConstants.VALUE_EXPIRED;


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

    // Pgp Email view
    @BindView(R.id.pgpEmail)
    TableRow pgpEmail;
    @BindView(R.id.tvPgpEmail)
    TextView tvPgpEmail;

    // Chat ID view
    @BindView(R.id.chatId)
    TableRow chatId;
    @BindView(R.id.tvChatId)
    TextView tvChatId;

    // Sim ID view
    @BindView(R.id.simId)
    TableRow simId;
    @BindView(R.id.tvSimId)
    TextView tvSimId;


    @Override
    protected int getContentView() {
        return R.layout.activity_link_device;
    }


    public LinkDeviceActivity() {

    }

    @SuppressLint({"LogNotTimber", "HardwareIds"})
    @Override
    protected void init() {


        Intent intent = getIntent();

        String status = intent.getStringExtra(DEVICE_STATUS_KEY);


        if (status != null && status.equals(PENDING_STATE)) {
            pendingLinkViewState();
        } else if (status != null && status.equals(ACTIVE_STATE)) {
            approvedLinkViewState();
        } else {
            newLinkViewState();
        }


        currentDealerID = PrefUtils.getStringPref(this, AppConstants.KEY_DEALER_ID);
        connectedDid = PrefUtils.getStringPref(this, AppConstants.KEY_CONNECTED_ID);

        Log.d("ConnectedDealerId", connectedDid);

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



        tvCurrentDealerID.setText(currentDealerID);


        tvSerialNo.setText(SerialNo);
        tvMAC.setText(MAC);
        tvIP.setText(IP);


        lytSwipeReferesh.setOnRefreshListener(() -> {
            Timber.i("<<<<<SwipedToRefresh>>>>>");
            freshViewState();

            String device_id = PrefUtils.getStringPref(LinkDeviceActivity.this, DEVICE_ID);

            if (device_id != null) {

                ((MyApplication) getApplicationContext())
                        .getApiOneCaller()
                        .checkDeviceStatus(new DeviceStatusModel(SerialNo, DeviceIdUtils.getMacAddress()))
                        .enqueue(new Callback<DeviceStatusResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<DeviceStatusResponse> call, @NonNull Response<DeviceStatusResponse> response) {

                                if (response.isSuccessful() && response.body() != null) {

                                    String msg = response.body().getMsg();


                                    boolean isLinked = PrefUtils.getBooleanPref(LinkDeviceActivity.this, DEVICE_LINKED_STATUS);
                                    if (response.body().isStatus()) {

                                        switch (msg) {
                                            case ACTIVE:
                                                DeviceStatusResponse deviceStatusResponse = response.body();
                                                saveInfo(response.body().getToken(), deviceStatusResponse.getDevice_id(), deviceStatusResponse.getExpiry_date(), deviceStatusResponse.getDealer_pin());
                                                utils.unSuspendDevice(LinkDeviceActivity.this);
                                                PrefUtils.saveBooleanPref(LinkDeviceActivity.this, DEVICE_LINKED_STATUS, true);
                                                approvedLinkViewState();
                                                break;
                                            case EXPIRED:
                                                saveInfo(response.body().getToken(), response.body().getDevice_id(), response.body().getExpiry_date(), response.body().getDealer_pin());
                                                utils.suspendedDevice(LinkDeviceActivity.this, "expired");
                                                finish();
                                                break;
                                            case SUSPENDED:
                                                saveInfo(response.body().getToken(), response.body().getDevice_id(), response.body().getExpiry_date(), response.body().getDealer_pin());
                                                utils.suspendedDevice(LinkDeviceActivity.this, "suspended");
                                                finish();
                                                break;
                                            case TRIAL:
                                                saveInfo(response.body().getToken(), response.body().getDevice_id(), response.body().getExpiry_date(), response.body().getDealer_pin());
                                                utils.unSuspendDevice(LinkDeviceActivity.this);
                                                finish();
                                                break;
                                            case PENDING:
                                                Intent intent = new Intent(LinkDeviceActivity.this, LinkDeviceActivity.class);
                                                intent.putExtra(DEVICE_STATUS_KEY, PENDING_STATE);
                                                startActivity(intent);
                                                finish();
                                                break;
                                        }
                                    } else {
                                        switch (msg) {
                                            case UNLINKED_DEVICE:
                                                finish();
                                                break;
                                            case NEW_DEVICE:
                                                if (isLinked) {
                                                    utils.unlinkDevice(LinkDeviceActivity.this, false);
                                                    finish();
                                                } else {
                                                    finish();
                                                }
                                                break;
                                            case DUPLICATE_MAC:
//                                            showError("Error 321 Device ID (" + response.body().getDevice_id() + ") please contact support");
                                                break;
                                            case DUPLICATE_SERIAL:
//                                            showError("Error 322 Device ID (" + response.body().getDevice_id() + ") please contact support");
                                                break;
                                            case DUPLICATE_MAC_AND_SERIAL:
//                                            showError("Error 323 Device ID (" + response.body().getDevice_id() + ") please contact support");
                                                break;
                                            case DEALER_NOT_FOUND:
//                                            showMainContent();
                                                break;
                                        }
                                    }
                                } else {
                                    // if any error occurred show error view
//                                showError(AppConstants.SEVER_NOT_RESPONSIVE);
                                }


                            }

                            @Override
                            public void onFailure(@NonNull Call<DeviceStatusResponse> call, @NonNull Throwable t) {

                            }
                        });

            } else {
                finishedRefreshing();
                newLinkViewState();
            }
        });

    }


    private void saveInfo(String token, String device_id, String expiry_date, String dealer_pin) {
        PrefUtils.saveStringPref(LinkDeviceActivity.this, TOKEN, token);
        PrefUtils.saveStringPref(LinkDeviceActivity.this, VALUE_EXPIRED, expiry_date);
        PrefUtils.saveStringPref(LinkDeviceActivity.this, DEVICE_ID, device_id);
        PrefUtils.saveStringPref(LinkDeviceActivity.this, KEY_DEVICE_LINKED, dealer_pin);
    }

    private void setDealerPin(String id_or_msg) {
        tvLinkedDealerPin.setText(id_or_msg);
    }

    @OnClick(R.id.btnLinkDevice)
    public void onClickBtnLinkDevice() {

        if (btnLinkDevice.getText().equals("Next")) {
            setResult(RESULT_OK);
            finish();
        } else {
            processingLinkViewState();

            ((MyApplication) getApplicationContext())
                    .getApiOneCaller()
                    .linkDeviceToDealer(
                            new LinkDeviceModel(currentDealerID, connectedDid, IMEI, SimNo, SerialNo, MAC, IP),
                            PrefUtils.getStringPref(LinkDeviceActivity.this, TOKEN)
//                                +"INVALID_TOKEN"
                    )
                    .enqueue(new Callback<LinkDeviceResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<LinkDeviceResponse> call, @NonNull Response<LinkDeviceResponse> response) {

                            if (response.isSuccessful() && response.body() != null) {
                                LinkDeviceResponse ldr = response.body();
                                if (ldr.isStatus()) {
                                    PrefUtils.saveStringPref(LinkDeviceActivity.this, KEY_DEVICE_LINKED, ldr.getDealer_pin());
                                    PrefUtils.saveStringPref(LinkDeviceActivity.this, DEVICE_ID, ldr.getDevice_id());
                                    pendingLinkViewState();
                                } else {

                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<LinkDeviceResponse> call, @NonNull Throwable t) {
//                        newLinkViewState();
//                            checkLinkDeviceStatus();
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
                        MAC, SerialNo,
                        PrefUtils.getStringPref(LinkDeviceActivity.this, TOKEN)
//                                +"INVALID_TOKEN"
                )
                .enqueue(new Callback<DeleteDeviceResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DeleteDeviceResponse> call, @NonNull Response<DeleteDeviceResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            DeleteDeviceResponse dDr = response.body();

                            if (dDr.isStatus()) {
                                PrefUtils.saveStringPref(LinkDeviceActivity.this, DEVICE_ID, null);
                                PrefUtils.saveStringPref(LinkDeviceActivity.this, KEY_DEVICE_LINKED, null);
                                newLinkViewState();
                            } else {
                                pendingLinkViewState();
                            }

                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DeleteDeviceResponse> call, @NonNull Throwable t) {


                    }
                });


    }

    private void newLinkViewState() {
        setDealerPin("not linked yet");
        btnLinkDevice.setText(R.string.link_device);
        btnLinkDevice.setVisibility(View.VISIBLE);
        btnLinkDevice.setEnabled(true);
        btnStopLink.setVisibility(View.GONE);
        tvLinkedStatus.setText(R.string.device_not_linked);
        tvLinkedStatus.setTextColor(Color.RED);
        tvLinkedStatus.setVisibility(View.VISIBLE);
        tvDeviceId.setText("");
    }


    boolean linked = false;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (linked) {
            setResult(RESULT_OK);
            finish();
        }

    }


    private void approvedLinkViewState() {
        finishedRefreshing();
        btnLinkDevice.setVisibility(View.VISIBLE);
        btnStopLink.setVisibility(View.GONE);
        btnLinkDevice.setText("Next");
        btnLinkDevice.setEnabled(true);
        linked = true;
        tvDeviceId.setText(PrefUtils.getStringPref(LinkDeviceActivity.this,DEVICE_ID));

        tvLinkedDealerPin.setText(PrefUtils.getStringPref(LinkDeviceActivity.this,KEY_DEVICE_LINKED));

        tvLinkedStatus.setText(R.string.device_already_linked);
        tvLinkedStatus.setTextColor(ContextCompat.getColor(this, R.color.green_dark));
        tvLinkedStatus.setVisibility(View.VISIBLE);
        // pgp Email
        String pgp_Email = PrefUtils.getStringPref(LinkDeviceActivity.this, PGP_EMAIL);
        if (pgp_Email != null) {
            pgpEmail.setVisibility(View.VISIBLE);
            tvPgpEmail.setText(pgp_Email);
        }
        // chat ID
        String chat_Id = PrefUtils.getStringPref(LinkDeviceActivity.this, CHAT_ID);
        if (chat_Id != null) {
            chatId.setVisibility(View.VISIBLE);
            tvChatId.setText(chat_Id);
        }
        // sim ID
        String sim_Id = PrefUtils.getStringPref(LinkDeviceActivity.this, SIM_ID);
        if (sim_Id != null) {
            simId.setVisibility(View.VISIBLE);
            tvChatId.setText(chat_Id);
        }

    }

    private void pendingLinkViewState() {
        setDealerPin(PrefUtils.getStringPref(LinkDeviceActivity.this, KEY_DEVICE_LINKED));
        btnLinkDevice.setVisibility(View.GONE);
        btnStopLink.setText(R.string.stop_linking);
        btnStopLink.setVisibility(View.VISIBLE);
        btnStopLink.setEnabled(true);
        tvLinkedStatus.setText(R.string.link_request_pending);
        tvLinkedStatus.setTextColor(Color.BLUE);
        tvLinkedStatus.setVisibility(View.VISIBLE);
        tvDeviceId.setText(PrefUtils.getStringPref(LinkDeviceActivity.this, DEVICE_ID));
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
