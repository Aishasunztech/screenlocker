package com.screenlocker.secure.mdm.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.mdm.base.BaseActivity;
import com.screenlocker.secure.mdm.retrofitmodels.DeleteDeviceResponse;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceModel;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceStatusResponse;
import com.screenlocker.secure.mdm.retrofitmodels.LinkDeviceModel;
import com.screenlocker.secure.mdm.retrofitmodels.LinkDeviceResponse;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Utils;
import com.secure.launcher.BuildConfig;
import com.secure.launcher.R;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
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
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.NEW_DEVICE;
import static com.screenlocker.secure.utils.AppConstants.PENDING;
import static com.screenlocker.secure.utils.AppConstants.PENDING_STATE;
import static com.screenlocker.secure.utils.AppConstants.PGP_EMAIL;
import static com.screenlocker.secure.utils.AppConstants.SIM_ID;
import static com.screenlocker.secure.utils.AppConstants.SIM_ID2;
import static com.screenlocker.secure.utils.AppConstants.SUSPENDED;
import static com.screenlocker.secure.utils.AppConstants.TOKEN;
import static com.screenlocker.secure.utils.AppConstants.TRIAL;
import static com.screenlocker.secure.utils.AppConstants.UNLINKED_DEVICE;
import static com.screenlocker.secure.utils.AppConstants.URL_1;
import static com.screenlocker.secure.utils.AppConstants.URL_2;
import static com.screenlocker.secure.utils.AppConstants.VALUE_EXPIRED;


public class LinkDeviceActivity extends BaseActivity {

    private static final String TAG = LinkDeviceActivity.class.getSimpleName();
    private static final String DEALER_ID_DEFAULT = "not linked yet";
    @BindView(R.id.not_linked_container)
    LinearLayout notLinkedContainer;
    @BindView(R.id.linked_container)
    LinearLayout linkedContainer;
    @BindView(R.id.stop_linking_container)
    LinearLayout stopLinkingContainer;

    private boolean isFirstTime = true;

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
    @BindView(R.id.tv_linked_dealerPin)
    TextView tv_label_dealer_pin;
    @BindView(R.id.tv_linked_deviceId)
    TextView tv_linked_deviceId;

    // Sim ID view
    @BindView(R.id.simId)
    TableRow simId;
    @BindView(R.id.tvSimId)
    TextView tvSimId;

    private PrefUtils prefUtils;


    @Override
    protected int getContentView() {
        return R.layout.activity_link_device;
    }

    public interface OnScheduleTimerListener {
        void onScheduleTimer(boolean state);   //method, which can have parameters
    }

    public static OnScheduleTimerListener mListener; //listener field

    public LinkDeviceActivity() {

    }

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private void setToolbar(Toolbar mToolbar) {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.link_device));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @SuppressLint({"LogNotTimber", "HardwareIds"})
    @Override
    protected void init() {


        Intent intent = getIntent();

        String status = intent.getStringExtra(DEVICE_STATUS_KEY);


        setToolbar(toolbar);


        if (prefUtils == null) {
            prefUtils = PrefUtils.getInstance(this);
        }

        currentDealerID = prefUtils.getStringPref(AppConstants.KEY_DEALER_ID);
        connectedDid = prefUtils.getStringPref(AppConstants.KEY_CONNECTED_ID);


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
        MAC = DeviceIdUtils.generateUniqueDeviceId(this);

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
        lytSwipeReferesh.setOnRefreshListener(listener);
        if (status != null && status.equals(PENDING_STATE)) {
            pendingLinkViewState();

        } else if (status != null && status.equals(ACTIVE_STATE)) {
            prefUtils.saveBooleanPref(DEVICE_LINKED_STATUS, true);
            approvedLinkViewState();
        } else {
            processingLinkViewState();
            if (MyApplication.oneCaller == null) {
                if (asyncCalls != null) {
                    asyncCalls.cancel(true);
                }

                String[] urls = {URL_1, URL_2};
                asyncCalls = new AsyncCalls(output -> {
                    if (output == null) {
                        Toast.makeText(this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    } else {
                        prefUtils.saveStringPref(LIVE_URL, output);
                        String live_url = prefUtils.getStringPref(LIVE_URL);
                        Timber.d("live_url %s", live_url);
                        MyApplication.oneCaller = RetrofitClientInstance.getRetrofitInstance(live_url + MOBILE_END_POINT).create(ApiOneCaller.class);
                        linkDevice();
                    }
                }, this, urls);

            } else {
                linkDevice();
            }
        }


    }


    private void saveInfo(String token, String device_id, String expiry_date, String dealer_pin, String chatId, String pgpId, String simId1, String simId2) {
        prefUtils.saveStringPref(TOKEN, token);
        prefUtils.saveStringPref(VALUE_EXPIRED, expiry_date);
        prefUtils.saveStringPref(DEVICE_ID, device_id);
        prefUtils.saveStringPref(CHAT_ID, chatId);
        prefUtils.saveStringPref(PGP_EMAIL, pgpId);
        prefUtils.saveStringPref(SIM_ID, simId1);
        prefUtils.saveStringPref(SIM_ID2, simId2);
        prefUtils.saveStringPref(KEY_DEVICE_LINKED, dealer_pin);
    }

    private void setDealerPin(String id_or_msg) {
        tvLinkedDealerPin.setText(id_or_msg);
    }

    @OnClick(R.id.btnLinkDevice)
    public void onClickBtnLinkDevice() {

        if (btnLinkDevice.getText().equals(getResources().getString(R.string.next))) {
            setResult(RESULT_OK);
            finish();
        } else {
            processingLinkViewState();


            if (MyApplication.oneCaller == null) {
                if (asyncCalls != null) {
                    asyncCalls.cancel(true);
                }

                String[] urls = {URL_1, URL_2};
                asyncCalls = new AsyncCalls(output -> {
                    if (output == null) {
                        Toast.makeText(this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    } else {
                        prefUtils.saveStringPref(LIVE_URL, output);
                        String live_url = prefUtils.getStringPref(LIVE_URL);
                        Timber.d("live_url %s", live_url);
                        MyApplication.oneCaller = RetrofitClientInstance.getRetrofitInstance(live_url + MOBILE_END_POINT).create(ApiOneCaller.class);
                        linkDevice();
                    }
                }, this, urls);

            } else {
                linkDevice();
            }


        }

    }

    private void linkDevice() {

        MyApplication.oneCaller
                .linkDeviceToDealer(
                        new LinkDeviceModel(currentDealerID, connectedDid, IMEI, SimNo, SerialNo, MAC, IP, getResources().getString(R.string.apktype), BuildConfig.VERSION_NAME),
                        prefUtils.getStringPref(TOKEN)
//                                +"INVALID_TOKEN"
                )
                .enqueue(new Callback<LinkDeviceResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LinkDeviceResponse> call, @NonNull Response<LinkDeviceResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            LinkDeviceResponse ldr = response.body();
                            if (ldr.isStatus()) {
                                prefUtils.saveStringPref(KEY_DEVICE_LINKED, ldr.getDealer_pin());
                                prefUtils.saveStringPref(DEVICE_ID, ldr.getDevice_id());
                                pendingLinkViewState();
                            } else {
                                Toast.makeText(LinkDeviceActivity.this, getResources().getString(R.string.session_expired), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LinkDeviceResponse> call, @NonNull Throwable t) {
                        Toast.makeText(LinkDeviceActivity.this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void stopLinking() {

        MyApplication.oneCaller
                .stopLinkingDevice(
                        MAC, SerialNo,
                        prefUtils.getStringPref(TOKEN)
//                                +"INVALID_TOKEN"
                )
                .enqueue(new Callback<DeleteDeviceResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DeleteDeviceResponse> call, @NonNull Response<DeleteDeviceResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            DeleteDeviceResponse dDr = response.body();

                            if (dDr.isStatus()) {
                                prefUtils.saveStringPref(DEVICE_ID, null);
                                prefUtils.saveStringPref(KEY_DEVICE_LINKED, null);
                                newLinkViewState();
                            } else {
                                pendingLinkViewState();
                            }

                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DeleteDeviceResponse> call, @NonNull Throwable t) {
                        Toast.makeText(LinkDeviceActivity.this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @OnClick(R.id.btnStopLink)
    public void onClickBtnUnlinkDevice() {
        processingUnlinkViewState();

        if (MyApplication.oneCaller == null) {
            if (asyncCalls != null) {
                asyncCalls.cancel(true);
            }

            String[] urls = {URL_1, URL_2};
            asyncCalls = new AsyncCalls(output -> {
                if (output == null) {
                    Toast.makeText(this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                } else {
                    prefUtils.saveStringPref(LIVE_URL, output);
                    String live_url = prefUtils.getStringPref(LIVE_URL);
                    Timber.d("live_url %s", live_url);
                    MyApplication.oneCaller = RetrofitClientInstance.getRetrofitInstance(live_url + MOBILE_END_POINT).create(ApiOneCaller.class);
                    stopLinking();
                }
            }, this, urls);

        } else {
            stopLinking();
        }


    }

    private void newLinkViewState() {
        isPendingActivation = false;
        setDealerPin(getResources().getString(R.string.not_linked_yet));
        btnLinkDevice.setText(R.string.link_device);
        btnLinkDevice.setVisibility(View.VISIBLE);
        btnLinkDevice.setEnabled(true);
        btnStopLink.setVisibility(View.GONE);
        tvLinkedStatus.setText(R.string.device_not_linked);
        tvLinkedStatus.setTextColor(Color.RED);
        tvLinkedStatus.setVisibility(View.VISIBLE);
        tvDeviceId.setText("");

        if (t != null) {
            t.cancel();
            t = null;
        }

        setProgressViews(false);
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


    private AsyncCalls asyncCalls;

    SwipeRefreshLayout.OnRefreshListener listener = () -> {

        Timber.i("<<<<<SwipedToRefresh>>>>>");
        freshViewState();
        String device_id = prefUtils.getStringPref(DEVICE_ID);
        if (device_id != null) {
            if (MyApplication.oneCaller == null) {
                if (asyncCalls != null) {
                    asyncCalls.cancel(true);
                }
                String[] urls = {URL_1, URL_2};
                asyncCalls = new AsyncCalls(output -> {
                    if (output == null) {
                        Toast.makeText(this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    } else {
                        prefUtils.saveStringPref(LIVE_URL, output);
                        String live_url = prefUtils.getStringPref(LIVE_URL);
                        Timber.d("live_url %s", live_url);
                        MyApplication.oneCaller = RetrofitClientInstance.getRetrofitInstance(live_url + MOBILE_END_POINT).create(ApiOneCaller.class);
                        checkDeviceStatus();
                    }
                }, this, urls);

            } else {
                checkDeviceStatus();
            }

        } else {
            finishedRefreshing();
            newLinkViewState();
        }


    };


    private void checkDeviceStatus() {

        MyApplication.oneCaller
                .checkDeviceStatus(new DeviceModel(DeviceIdUtils.getSerialNumber(), DeviceIdUtils.getIPAddress(true), getPackageName() + getString(R.string.app_name), DeviceIdUtils.generateUniqueDeviceId(this)))
                .enqueue(new Callback<DeviceStatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DeviceStatusResponse> call, @NonNull Response<DeviceStatusResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            String msg = response.body().getMsg();


                            boolean isLinked = prefUtils.getBooleanPref(DEVICE_LINKED_STATUS);
                            if (response.body().isStatus()) {

                                switch (msg) {
                                    case ACTIVE:
                                        DeviceStatusResponse dsr = response.body();
                                        saveInfo(response.body().getToken(), dsr.getDevice_id(), dsr.getExpiry_date(), dsr.getDealer_pin(), dsr.getChatId(), dsr.getPgpID(), dsr.getSimId1(), dsr.getSimId2());
                                        utils.unSuspendDevice(LinkDeviceActivity.this);
                                        prefUtils.saveBooleanPref(DEVICE_LINKED_STATUS, true);
                                        approvedLinkViewState();
                                        break;
                                    case EXPIRED:
                                        saveInfo(response.body().getToken(), response.body().getDevice_id(), response.body().getExpiry_date(), response.body().getDealer_pin(), response.body().getChatId(), response.body().getPgpID(), response.body().getSimId1(), response.body().getSimId2());
                                        utils.suspendedDevice(LinkDeviceActivity.this, "expired");
                                        prefUtils.saveBooleanPref(DEVICE_LINKED_STATUS, true);
                                        isPendingActivation = false;
                                        finish();
                                        break;
                                    case SUSPENDED:
                                        saveInfo(response.body().getToken(), response.body().getDevice_id(), response.body().getExpiry_date(), response.body().getDealer_pin(), response.body().getChatId(), response.body().getPgpID(), response.body().getSimId1(), response.body().getSimId2());
                                        utils.suspendedDevice(LinkDeviceActivity.this, "suspended");
                                        prefUtils.saveBooleanPref(DEVICE_LINKED_STATUS, true);
                                        isPendingActivation = false;
                                        finish();
                                        break;
                                    case TRIAL:
                                        saveInfo(response.body().getToken(), response.body().getDevice_id(), response.body().getExpiry_date(), response.body().getDealer_pin(), response.body().getChatId(), response.body().getPgpID(), response.body().getSimId1(), response.body().getSimId2());
                                        utils.unSuspendDevice(LinkDeviceActivity.this);
                                        prefUtils.saveBooleanPref(DEVICE_LINKED_STATUS, true);
                                        approvedLinkViewState();
                                        break;
                                    case PENDING:
                                        saveInfo(response.body().getToken(), response.body().getDevice_id(), response.body().getExpiry_date(), response.body().getDealer_pin(), response.body().getChatId(), response.body().getPgpID(), response.body().getSimId1(), response.body().getSimId2());
                                        finishedRefreshing();
                                        pendingLinkViewState();
                                        break;
                                }
                            } else {
                                prefUtils.saveStringPref(DEVICE_ID, response.body().getDevice_id());

                                switch (msg) {
                                    case UNLINKED_DEVICE:
                                        isPendingActivation = false;
                                        finish();
                                        break;
                                    case NEW_DEVICE:
                                        isPendingActivation = false;
                                        if (isLinked) {
                                            utils.unlinkDeviceWithMsg(LinkDeviceActivity.this, false, "unlinked");
                                            finish();
                                        } else {
                                            finish();
                                        }
                                        break;
                                    case DUPLICATE_MAC:
                                        isPendingActivation = false;
                                        utils.unlinkDeviceWithMsg(LinkDeviceActivity.this, false, DUPLICATE_MAC);
                                        finish();
                                        break;
                                    case DUPLICATE_SERIAL:
                                        isPendingActivation = false;
                                        utils.unlinkDeviceWithMsg(LinkDeviceActivity.this, false, DUPLICATE_SERIAL);
                                        finish();
                                        break;
                                    case DUPLICATE_MAC_AND_SERIAL:
                                        isPendingActivation = false;
                                        utils.unlinkDeviceWithMsg(LinkDeviceActivity.this, false, DUPLICATE_MAC_AND_SERIAL);
                                        finish();
                                        break;
                                    case DEALER_NOT_FOUND:
                                        isPendingActivation = false;
                                        Toast.makeText(LinkDeviceActivity.this, "Dealer not found", Toast.LENGTH_SHORT).show();
                                        finish();
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
                        Toast.makeText(LinkDeviceActivity.this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                        isPendingActivation = false;
                    }
                });
    }


    boolean isPendingActivation = false;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeTimer();
    }

    private void closeTimer() {
        if (t != null) {
            t.cancel();
            t = null;
        }
        prefUtils.saveBooleanPref(AppConstants.PENDING_ACTIVATION, isPendingActivation);

        if (mListener != null)
            mListener.onScheduleTimer(isPendingActivation);

    }

    @Override
    protected void onStop() {
        super.onStop();

        closeTimer();
    }

    private Timer t;

    private void scheduleTimer() {

        if (mListener != null) {
            mListener.onScheduleTimer(false);
        }

        if (t != null) {
            t.cancel();
            t = null;
        }

        if (isFirstTime) {
            isFirstTime = false;
            lytSwipeReferesh.setRefreshing(true);
            listener.onRefresh();
            return;
        }

        t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    lytSwipeReferesh.setRefreshing(true);
                    listener.onRefresh();
                });
            }

        }, 5000);
    }


    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.textView)
    TextView progressTextView;

    private void setProgressViews(boolean status) {

        if (status) {
            progressBar.setVisibility(View.INVISIBLE);
            progressTextView.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
            progressTextView.setVisibility(View.INVISIBLE);
        }
    }

    private void pendingLinkViewState() {
        scheduleTimer();
        isPendingActivation = true;
        setDealerPin(prefUtils.getStringPref(KEY_DEVICE_LINKED));
        btnLinkDevice.setVisibility(View.GONE);
        btnStopLink.setText(R.string.stop_linking);
        btnStopLink.setVisibility(View.VISIBLE);
        btnStopLink.setEnabled(true);
        tvLinkedStatus.setText(R.string.link_request_pending);
        tvLinkedStatus.setTextColor(Color.BLUE);
        tvLinkedStatus.setVisibility(View.VISIBLE);
        tvDeviceId.setText(prefUtils.getStringPref(DEVICE_ID));

        showContainer(1);

        setProgressViews(true);
    }

    /**
     * showing the processing state
     */
    private void processingLinkViewState() {

        if (t != null) {
            t.cancel();
            t = null;
        }
        setProgressViews(false);

        btnLinkDevice.setEnabled(false);
        btnLinkDevice.setVisibility(View.VISIBLE);
        btnStopLink.setVisibility(View.GONE);
        tvLinkedStatus.setText(R.string.link_request_pending);
        tvLinkedStatus.setTextColor(Color.BLUE);
        tvLinkedStatus.setVisibility(View.INVISIBLE);
    }

    private void processingUnlinkViewState() {

        if (t != null) {
            t.cancel();
            t = null;
        }

        btnLinkDevice.setVisibility(View.GONE);
        btnStopLink.setEnabled(false);
        btnStopLink.setVisibility(View.VISIBLE);
        tvLinkedStatus.setText(R.string.link_request_pending);
        tvLinkedStatus.setTextColor(Color.BLUE);
        tvLinkedStatus.setVisibility(View.INVISIBLE);

        showContainer(0);
    }

    private void freshViewState() {
        btnLinkDevice.setVisibility(View.INVISIBLE);
        btnStopLink.setVisibility(View.INVISIBLE);
        tvLinkedStatus.setVisibility(View.INVISIBLE);

    }

    private void approvedLinkViewState() {


        if (t != null) {
            t.cancel();
            t = null;
        }
        isPendingActivation = false;

        setProgressViews(false);

        String macAddress = DeviceIdUtils.generateUniqueDeviceId(this);
        String serialNo = DeviceIdUtils.getSerialNumber();

        if (serialNo != null) {
            ApiUtils utils = new ApiUtils(LinkDeviceActivity.this, macAddress, serialNo);
            utils.connectToSocket();
        }

        finishedRefreshing();
        btnLinkDevice.setVisibility(View.VISIBLE);
        btnStopLink.setVisibility(View.GONE);
        btnLinkDevice.setText(getResources().getString(R.string.next));
        btnLinkDevice.setEnabled(false);
        linked = true;
        tvDeviceId.setText(prefUtils.getStringPref(DEVICE_ID));

        String dealerPin = prefUtils.getStringPref(KEY_DEVICE_LINKED);
        if (dealerPin != null) {

            tvLinkedDealerPin.setText(dealerPin);
            tv_label_dealer_pin.setText(String.format("%s: %s", getResources().getString(R.string.dealer_pin), dealerPin));
            tv_linked_deviceId.setText(String.format("Device ID: %s", prefUtils.getStringPref(DEVICE_ID)));
        }


        tvLinkedStatus.setText(R.string.device_already_linked);
        tvLinkedStatus.setTextColor(ContextCompat.getColor(this, R.color.green_dark));
        tvLinkedStatus.setVisibility(View.VISIBLE);
        // pgp Email
        String pgp_Email = prefUtils.getStringPref(PGP_EMAIL);
        if (pgp_Email != null) {
            pgpEmail.setVisibility(View.VISIBLE);
            tvPgpEmail.setText(pgp_Email);
        }
        // chat ID
        String chat_Id = prefUtils.getStringPref(CHAT_ID);
        if (chat_Id != null) {
            chatId.setVisibility(View.VISIBLE);
            tvChatId.setText(chat_Id);
        }
        // sim ID
        String sim_Id = prefUtils.getStringPref(SIM_ID);
        if (sim_Id != null) {
            simId.setVisibility(View.VISIBLE);
            tvChatId.setText(chat_Id);
        }
        showContainer(2);
    }

    private void showContainer(int type) {
        switch (type) {
            case 0:
                notLinkedContainer.setVisibility(View.VISIBLE);
                stopLinkingContainer.setVisibility(View.GONE);
                linkedContainer.setVisibility(View.GONE);
                break;
            case 1:
                stopLinkingContainer.setVisibility(View.VISIBLE);
                linkedContainer.setVisibility(View.GONE);
                notLinkedContainer.setVisibility(View.GONE);
                break;
            case 2:
                linkedContainer.setVisibility(View.VISIBLE);
                stopLinkingContainer.setVisibility(View.GONE);
                notLinkedContainer.setVisibility(View.GONE);
                break;
        }
    }


    /**
     * set refreshing to false
     */
    private void finishedRefreshing() {
        if (!(LinkDeviceActivity.this.isDestroyed() || LinkDeviceActivity.this.isFinishing()) && lytSwipeReferesh.isRefreshing()) {
            lytSwipeReferesh.setRefreshing(false);
        }
    }

    @OnClick(R.id.tvDeviceId)
    public void copyDeviceId() {
        String copied = tvDeviceId.getText().toString();
        if (copied.length() > 0) {
            String linkText = tvDeviceId.getText().toString().substring(1, tvDeviceId.getText().toString().length() - 1);
            Utils.copyToClipBoard(this, AppConstants.COPIED_URL, linkText, "DeviceId copied to clipboard");
        }
    }

    @OnClick(R.id.tvLinkedDealerPin)
    public void copyDealerPin() {
        String copied = tvLinkedDealerPin.getText().toString();
        if (copied.length() > 0) {
            String linkText = tvLinkedDealerPin.getText().toString().substring(1, tvLinkedDealerPin.getText().toString().length() - 1);
            Utils.copyToClipBoard(this, AppConstants.COPIED_URL, linkText, "DealerPin copied to clipboard");
        }
    }

    @OnClick(R.id.tvIMEI)
    public void copyIMEI1() {
        String copied = tvIMEI.getText().toString();
        if (copied.length() > 0) {
            String linkText = tvIMEI.getText().toString().substring(1, tvIMEI.getText().toString().length() - 1);
            Utils.copyToClipBoard(this, AppConstants.COPIED_URL, linkText, "IMEI copied to clipboard");
        }
    }

    @OnClick(R.id.tvSimNo)
    public void copytvSimNo() {
        String copied = tvSimNo.getText().toString();
        if (copied.length() > 0) {
            String linkText = tvSimNo.getText().toString().substring(1, tvSimNo.getText().toString().length() - 1);
            Utils.copyToClipBoard(this, AppConstants.COPIED_URL, linkText, "Sim No copied to clipboard");
        }
    }

    @OnClick(R.id.tvIMEI2)
    public void copytvIMEI2() {
        String copied = tvIMEI2.getText().toString();
        if (copied.length() > 0) {
            String linkText = tvIMEI2.getText().toString().substring(1, tvIMEI2.getText().toString().length() - 1);
            Utils.copyToClipBoard(this, AppConstants.COPIED_URL, linkText, "IMEI copied to clipboard");
        }
    }


    @OnClick(R.id.tvSerialNo)
    public void copytvSerialNo() {
        String copied = tvSerialNo.getText().toString();
        if (copied.length() > 0) {
            String linkText = tvSerialNo.getText().toString().substring(1, tvSerialNo.getText().toString().length() - 1);
            Utils.copyToClipBoard(this, AppConstants.COPIED_URL, linkText, "Serial copied to clipboard");
        }
    }

    @OnClick(R.id.tvSimNo2)
    public void copytvSimNo2() {
        String copied = tvSimNo2.getText().toString();
        if (copied.length() > 0) {
            String linkText = tvSimNo2.getText().toString().substring(1, tvSimNo2.getText().toString().length() - 1);
            Utils.copyToClipBoard(this, AppConstants.COPIED_URL, linkText, "Sim No copied to clipboard");
        }
    }

    @OnClick(R.id.tvMAC)
    public void copytvMAC() {
        String copied = tvMAC.getText().toString();
        if (copied.length() > 0) {
            String linkText = tvMAC.getText().toString().substring(1, tvMAC.getText().toString().length() - 1);
            Utils.copyToClipBoard(this, AppConstants.COPIED_URL, linkText, "MAC copied to clipboard");
        }

    }

    @OnClick(R.id.tvIP)
    public void copytvIP() {
        String copied = tvIP.getText().toString();
        if (copied.length() > 0) {
            String linkText = tvIP.getText().toString().substring(1, tvIP.getText().toString().length() - 1);
            Utils.copyToClipBoard(this, AppConstants.COPIED_URL, linkText, "IP address copied to clipboard");
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        prefUtils = PrefUtils.getInstance(this);
    }
}
