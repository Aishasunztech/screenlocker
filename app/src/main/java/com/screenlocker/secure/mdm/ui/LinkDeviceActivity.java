package com.screenlocker.secure.mdm.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.R;
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

    @BindView(R.id.lytSwipeReferesh)
    SwipeRefreshLayout lytSwipeReferesh;
    @BindView(R.id.not_linked_container)
    LinearLayout notLinkedContainer;
    @BindView(R.id.linked_container)
    LinearLayout linkedContainer;
    @BindView(R.id.stop_linking_container)
    LinearLayout stopLinkingContainer;
    @BindView(R.id.btnLinkDevice)
    Button btnLinkDevice;
    @BindView(R.id.tv_linked_dealerPin)
    TextView tv_label_dealer_pin;
    @BindView(R.id.tv_linked_deviceId)
    TextView tv_linked_deviceId;
    @BindView(R.id.btnStopLink)
    Button btnStopLink;

    @BindView(R.id.toolbar)
    Toolbar toolbar;


    private boolean isFirstTime = true;

    String linkedDealerPin;
    String currentDealerID;
    List<String> IMEI;
    List<String> SimNo;

    String SerialNo;

    String MAC;
    String IP;
    String connectedDid;



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

        if (status != null && status.equals(PENDING_STATE)) {
            pendingLinkViewState();

        } else if (status != null && status.equals(ACTIVE_STATE)) {
            PrefUtils.saveBooleanPref(this, DEVICE_LINKED_STATUS, true);
            approvedLinkViewState();
        } else {
            newLinkViewState();
        }


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
        MAC = DeviceIdUtils.generateUniqueDeviceId(this);


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

        lytSwipeReferesh.setOnRefreshListener(listener);


    }


    private void saveInfo(String token, String device_id, String expiry_date, String dealer_pin) {
        PrefUtils.saveStringPref(LinkDeviceActivity.this, TOKEN, token);
        PrefUtils.saveStringPref(LinkDeviceActivity.this, VALUE_EXPIRED, expiry_date);
        PrefUtils.saveStringPref(LinkDeviceActivity.this, DEVICE_ID, device_id);
        PrefUtils.saveStringPref(LinkDeviceActivity.this, KEY_DEVICE_LINKED, dealer_pin);
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
                        PrefUtils.saveStringPref(this, LIVE_URL, output);
                        String live_url = PrefUtils.getStringPref(this, LIVE_URL);
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
                    PrefUtils.saveStringPref(this, LIVE_URL, output);
                    String live_url = PrefUtils.getStringPref(this, LIVE_URL);
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
        btnLinkDevice.setText(R.string.link_device);
        btnLinkDevice.setVisibility(View.VISIBLE);
        btnLinkDevice.setEnabled(true);
        btnStopLink.setVisibility(View.GONE);

        if (t != null) {
            t.cancel();
            t = null;
        }
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
        String device_id = PrefUtils.getStringPref(LinkDeviceActivity.this, DEVICE_ID);
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
                        PrefUtils.saveStringPref(this, LIVE_URL, output);
                        String live_url = PrefUtils.getStringPref(this, LIVE_URL);
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
                                        PrefUtils.saveBooleanPref(LinkDeviceActivity.this, DEVICE_LINKED_STATUS, true);
                                        finish();
                                        break;
                                    case SUSPENDED:
                                        saveInfo(response.body().getToken(), response.body().getDevice_id(), response.body().getExpiry_date(), response.body().getDealer_pin());
                                        utils.suspendedDevice(LinkDeviceActivity.this, "suspended");
                                        PrefUtils.saveBooleanPref(LinkDeviceActivity.this, DEVICE_LINKED_STATUS, true);

                                        finish();
                                        break;
                                    case TRIAL:
                                        saveInfo(response.body().getToken(), response.body().getDevice_id(), response.body().getExpiry_date(), response.body().getDealer_pin());
                                        utils.unSuspendDevice(LinkDeviceActivity.this);
                                        PrefUtils.saveBooleanPref(LinkDeviceActivity.this, DEVICE_LINKED_STATUS, true);
                                        approvedLinkViewState();
                                        break;
                                    case PENDING:
                                        saveInfo(response.body().getToken(), response.body().getDevice_id(), response.body().getExpiry_date(), response.body().getDealer_pin());
                                        finishedRefreshing();
                                        pendingLinkViewState();
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
                        Toast.makeText(LinkDeviceActivity.this, getResources().getString(R.string.server_error), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    boolean isPendingActivation = false;


    @Override
    protected void onStop() {
        super.onStop();

        if (t != null) {
            t.cancel();
            t = null;
        }
        PrefUtils.saveBooleanPref(this, AppConstants.PENDING_ACTIVATION, isPendingActivation);

        if (isPendingActivation) {
            if (mListener != null)
                mListener.onScheduleTimer(true);
        } else {
            if (mListener != null) {
                mListener.onScheduleTimer(false);
            }
        }
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



    private void pendingLinkViewState() {
        scheduleTimer();
        isPendingActivation = true;
        btnLinkDevice.setVisibility(View.GONE);
        btnStopLink.setText(R.string.stop_linking);
        btnStopLink.setVisibility(View.VISIBLE);
        btnStopLink.setEnabled(true);

        showContainer(1);

    }

    /**
     * showing the processing state
     */
    private void processingLinkViewState() {

        if (t != null) {
            t.cancel();
            t = null;
        }

        btnLinkDevice.setEnabled(false);
        btnLinkDevice.setVisibility(View.VISIBLE);
        btnStopLink.setVisibility(View.GONE);
    }

    private void processingUnlinkViewState() {

        if (t != null) {
            t.cancel();
            t = null;
        }

        btnLinkDevice.setVisibility(View.GONE);
        btnStopLink.setEnabled(false);
        btnStopLink.setVisibility(View.VISIBLE);

        showContainer(0);
    }

    private void freshViewState() {
        btnLinkDevice.setVisibility(View.INVISIBLE);
        btnStopLink.setVisibility(View.INVISIBLE);

    }

    private void approvedLinkViewState() {


        if (t != null) {
            t.cancel();
            t = null;
        }
        isPendingActivation = false;


        String macAddress = DeviceIdUtils.generateUniqueDeviceId(this);
        String serialNo = DeviceIdUtils.getSerialNumber();

        if (serialNo != null) {
            new ApiUtils(LinkDeviceActivity.this, macAddress, serialNo);
        }

        finishedRefreshing();
        btnLinkDevice.setVisibility(View.VISIBLE);
        btnStopLink.setVisibility(View.GONE);
        btnLinkDevice.setText(getResources().getString(R.string.next));
        btnLinkDevice.setEnabled(false);
        linked = true;

        String dealerPin = PrefUtils.getStringPref(LinkDeviceActivity.this, KEY_DEVICE_LINKED);
        if (dealerPin != null) {
            tv_label_dealer_pin.setText(String.format("%s: %s", getResources().getString(R.string.dealer_pin), dealerPin));
            tv_linked_deviceId.setText(String.format("%s: %s", getResources().getString(R.string.device_id), PrefUtils.getStringPref(LinkDeviceActivity.this, DEVICE_ID)));
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
}
