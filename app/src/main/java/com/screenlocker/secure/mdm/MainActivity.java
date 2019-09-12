package com.screenlocker.secure.mdm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.mdm.base.BaseActivity;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceLoginModle;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceModel;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceStatusResponse;
import com.screenlocker.secure.mdm.ui.LinkDeviceActivity;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.networkResponseModels.DeviceLoginResponse;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.socket.service.SocketService;
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

import static com.screenlocker.secure.socket.utils.utils.suspendedDevice;
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
import static com.screenlocker.secure.utils.AppConstants.FLAGGED;
import static com.screenlocker.secure.utils.AppConstants.KEY_CONNECTED_ID;
import static com.screenlocker.secure.utils.AppConstants.KEY_DEALER_ID;
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


public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_READ_PHONE_STATE = 100;
    private static final int PERMISSION_REQUEST_ACCESS_WIFI_STATE = 101;

    private boolean isBackPressed = false;


    /**
     * main view for the app in which dealers enter pin
     */
    @BindView(R.id.main)
    ConstraintLayout mainView;
    /**
     * this is the view which we show the dealer that your account is expired please contact dealer
     */
    @BindView(R.id.contactDealer)
    View contactDealer;

    /**
     * loading bar view
     */
    @BindView(R.id.loading)
    View loading;

    /**
     * view to show the user that some error occured
     */
    @BindView(R.id.error)
    View error;

    @BindView(R.id.error_text)
    TextView error_text;
    /**
     * rootview of the main view (in which dealer enters the pin)
     */
    @BindView(R.id.rootView)
    ViewGroup rootView;
    /**
     * view to enter the pin of dealer
     */
    @BindView(R.id.edtTxtPin)
    EditText etPin;
    /**
     * submit button to login after entering the pin
     */
    @BindView(R.id.btnSubmit)
    Button btSubmit;
    /**
     * to refresh view for the latest information
     */
    @BindView(R.id.lytSwipeRefresh)
    SwipeRefreshLayout lytSwipeRefresh;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    /**
     * @return this is to setup the layout for this activity
     */
    @Override
    protected int getContentView() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return R.layout.mdm_main;
    }

    /**
     * starting method just like oncreate
     */
    @Override
    protected void init() {

        //get imei of device
        IMEI = DeviceIdUtils.getIMEI(this);
        // get sim number
        SimNo = DeviceIdUtils.getSimNumber(this);

        // get ip address
        IP = DeviceIdUtils.getIPAddress(true);
        // get mac address
        MAC = DeviceIdUtils.generateUniqueDeviceId(this);

        defaultImei = (IMEI.size() >= 1) ? IMEI.get(0) : "";

        //get serial number
        SerialNo = DeviceIdUtils.getSerialNumber();

        Timber.d("SerialNo %s", SerialNo);
        Timber.d("MacAddress %s", MAC);

        SerialNo = (SerialNo == null) ? "" : SerialNo;

        IP = (IP == null) ? "" : IP;

        setToolbar(toolbar);

        initAutoLoginWithPermissions();

        lytSwipeRefresh.setOnRefreshListener(() -> {
            Timber.i("<<<<<SwipedToRefresh>>>>>");
            initAutoLogin();
        });


    }

    private void setToolbar(Toolbar mToolbar) {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getString(R.string.link_device_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

    /**
     *
     */
    private void initAutoLoginWithPermissions() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                // if permission is allowed (wifi and phone state)
                initAutoLogin();
            } else {
                // request phone permission
                requestPermission(Manifest.permission.READ_PHONE_STATE, PERMISSION_REQUEST_READ_PHONE_STATE);
            }

        } else {
            // request wifi permission
            requestPermission(Manifest.permission.ACCESS_WIFI_STATE, PERMISSION_REQUEST_ACCESS_WIFI_STATE);
        }
    }

    /**
     * call api and check weather its allowed to login or not
     */

    List<String> IMEI;

    String defaultImei;

    List<String> SimNo;

    String MAC;

    String IP;

    String SerialNo;


    private AsyncCalls asyncCalls;

    private void initAutoLogin() {

        IMEI = DeviceIdUtils.getIMEI(MainActivity.this);
        defaultImei = (IMEI.size() >= 1) ? IMEI.get(0) : "";

        showLoading();

        if (MyApplication.oneCaller == null) {

            String[] urls = {URL_1, URL_2};
            if (asyncCalls != null) {
                asyncCalls.cancel(true);
            }
            asyncCalls = new AsyncCalls(output -> {
                if (output == null) {
                    showError(getResources().getString(R.string.server_error));
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


    }


    private void checkDeviceStatus() {
        MyApplication.oneCaller
                .checkDeviceStatus(new DeviceModel(DeviceIdUtils.getSerialNumber(), DeviceIdUtils.getIPAddress(true), getPackageName() + getString(R.string.app_name), DeviceIdUtils.generateUniqueDeviceId(this)))
                .enqueue(new Callback<DeviceStatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DeviceStatusResponse> call, @NonNull Response<DeviceStatusResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {

                            String msg = response.body().getMsg();
                            Timber.d("status from MDM :%s",msg);
                            boolean isLinked = PrefUtils.getBooleanPref(MainActivity.this, DEVICE_LINKED_STATUS);
                            Intent intent = new Intent(MainActivity.this, LinkDeviceActivity.class);

                            if (response.body().isStatus()) {

                                switch (msg) {

                                    case ACTIVE:
                                        saveInfo(response.body().getToken(), response.body().getDevice_id(), response.body().getExpiry_date(), response.body().getDealer_pin());
                                        utils.unSuspendDevice(MainActivity.this);
                                        intent.putExtra(DEVICE_STATUS_KEY, ACTIVE_STATE);
                                        startActivity(intent);
                                        PrefUtils.saveBooleanPref(MainActivity.this, DEVICE_LINKED_STATUS, true);
                                        finish();
                                        break;
                                    case EXPIRED:
                                        saveInfo(response.body().getToken(), response.body().getDevice_id(), response.body().getExpiry_date(), response.body().getDealer_pin());
                                        utils.suspendedDevice(MainActivity.this, "expired");
                                        PrefUtils.saveBooleanPref(MainActivity.this, DEVICE_LINKED_STATUS, true);
                                        finish();
                                        break;
                                    case SUSPENDED:
                                        saveInfo(response.body().getToken(), response.body().getDevice_id(), response.body().getExpiry_date(), response.body().getDealer_pin());
                                        utils.suspendedDevice(MainActivity.this, "suspended");
                                        PrefUtils.saveBooleanPref(MainActivity.this, DEVICE_LINKED_STATUS, true);
                                        finish();
                                        break;
                                    case TRIAL:
                                        saveInfo(response.body().getToken(), response.body().getDevice_id(), response.body().getExpiry_date(), response.body().getDealer_pin());
                                        utils.unSuspendDevice(MainActivity.this);
                                        intent.putExtra(DEVICE_STATUS_KEY, ACTIVE_STATE);
                                        startActivity(intent);
                                        PrefUtils.saveBooleanPref(MainActivity.this, DEVICE_LINKED_STATUS, true);
                                        finish();
                                        break;
                                    case PENDING:
//                                        pending = true;
                                        saveInfo(response.body().getToken(), response.body().getDevice_id(), response.body().getExpiry_date(), response.body().getDealer_pin());
                                        intent.putExtra(DEVICE_STATUS_KEY, PENDING_STATE);
                                        startActivity(intent);
                                        finish();
                                        break;
                                    case FLAGGED:
                                        suspendedDevice(MainActivity.this, "flagged");
                                        break;
                                }
                            } else {
                                switch (msg) {
                                    case UNLINKED_DEVICE:
                                        showMainContent();
                                        //stop sevice
                                        break;
                                    case NEW_DEVICE:
                                        if (isLinked) {
                                            utils.newDevice(MainActivity.this, true);
                                        } else {
                                            showMainContent();
                                        }
                                        break;
                                    case DUPLICATE_MAC:
                                        showError(getResources().getString(R.string.error_321) + response.body().getDevice_id() + getResources().getString(R.string.contact_support));
                                        break;
                                    case DUPLICATE_SERIAL:
                                        showError(getResources().getString(R.string.error_322) + response.body().getDevice_id() + getResources().getString(R.string.contact_support));
                                        break;
                                    case DUPLICATE_MAC_AND_SERIAL:
                                        showError(getResources().getString(R.string.error323) + response.body().getDevice_id() + getResources().getString(R.string.contact_support));
                                        break;
                                    case DEALER_NOT_FOUND:
                                        showMainContent();
                                        break;
                                }
                            }
                        } else {
                            // if any error occurred show error view
                            showError(AppConstants.SEVER_NOT_RESPONSIVE);
                        }

                        if (lytSwipeRefresh.isRefreshing()) {
                            lytSwipeRefresh.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DeviceStatusResponse> call, @NonNull Throwable t) {

                        showError(AppConstants.SEVER_NOT_RESPONSIVE);
                        if (lytSwipeRefresh.isRefreshing()) {
                            lytSwipeRefresh.setRefreshing(false);
                        }
                    }
                });
    }


    private void initLayoutWithPermission() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                initAutoLogin();
            } else {

                requestPermission(Manifest.permission.READ_PHONE_STATE, PERMISSION_REQUEST_READ_PHONE_STATE);
            }

        } else {

            requestPermission(Manifest.permission.ACCESS_WIFI_STATE, PERMISSION_REQUEST_ACCESS_WIFI_STATE);
        }
    }


    @OnClick(R.id.btnSubmit)
    public void onClickSubmit() {
        submitWithPermissions();
    }

    private void submitWithPermissions() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {

                handleSubmit();

            } else {

                requestPermission(Manifest.permission.READ_PHONE_STATE, PERMISSION_REQUEST_READ_PHONE_STATE);
            }

        } else {

            requestPermission(Manifest.permission.ACCESS_WIFI_STATE, PERMISSION_REQUEST_ACCESS_WIFI_STATE);
        }

    }


    private void saveInfo(String token, String device_id, String expiry_date, String dealer_pin) {
        PrefUtils.saveStringPref(MainActivity.this, TOKEN, token);
        PrefUtils.saveStringPref(MainActivity.this, DEVICE_ID, device_id);
        PrefUtils.saveStringPref(MainActivity.this, VALUE_EXPIRED, expiry_date);
        PrefUtils.saveStringPref(MainActivity.this, KEY_DEVICE_LINKED, dealer_pin);
    }

    private void handleSubmit() {

        final String dealerPin = etPin.getText().toString().trim();

        if (MyApplication.oneCaller == null) {
            String[] urls = {URL_1, URL_2};

            if (asyncCalls != null) {
                asyncCalls.cancel(true);
            }
            asyncCalls = new AsyncCalls(output -> {
                if (output == null) {
                    showError(getResources().getString(R.string.server_error));
                } else {
                    PrefUtils.saveStringPref(this, LIVE_URL, output);
                    String live_url = PrefUtils.getStringPref(this, LIVE_URL);
                    Timber.d("live_url %s", live_url);
                    MyApplication.oneCaller = RetrofitClientInstance.getRetrofitInstance(live_url + MOBILE_END_POINT).create(ApiOneCaller.class);
                    if (dealerPin.length() == 6) {
                        request(1, dealerPin);

                    } else if (dealerPin.length() == 7) {
                        request(2, dealerPin);

                    } else {
                        etPin.setError(getResources().getString(R.string.invaild_dealer_code));
                    }
                }
            }, this, urls);

        } else {
            if (dealerPin.length() == 6) {
                request(1, dealerPin);
            } else if (dealerPin.length() == 7) {
                request(2, dealerPin);

            } else {
                etPin.setError(getResources().getString(R.string.invaild_dealer_code));
            }
        }


    }


    private void request(int type, String dealerPin) {
        disableViews();

        if (type == 1) {

            MyApplication.oneCaller
                    .deviceLogin(new DeviceLoginModle(/*"856424"*/ dealerPin))
                    .enqueue(new Callback<DeviceLoginResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<DeviceLoginResponse> call, @NonNull Response<DeviceLoginResponse> response) {

                            if (response.isSuccessful() && response.body() != null) {
                                DeviceLoginResponse dlr = response.body();

                                if (dlr.isStatus()) {
                                    PrefUtils.saveStringPref(MainActivity.this, KEY_DEALER_ID, "" + dlr.getdId());
                                    PrefUtils.saveStringPref(MainActivity.this, KEY_DEVICE_LINKED, "" + dlr.getDealer_pin());
                                    PrefUtils.saveStringPref(MainActivity.this, KEY_CONNECTED_ID, "" + dlr.getConnectedDid());

                                    PrefUtils.saveStringPref(MainActivity.this, TOKEN, dlr.getToken());

                                    link = true;
                                    startActivity(new Intent(MainActivity.this, LinkDeviceActivity.class));
                                } else {
                                    etPin.setError(getResources().getString(R.string.invalid_link_code));
//                                            etPin.setError(dlr.getMsg());
                                }
                            }
                            enableViews();
                        }

                        @Override
                        public void onFailure(@NonNull Call<DeviceLoginResponse> call, @NonNull Throwable t) {
                            enableViews();
                        }
                    });


        } else if (type == 2) {

            MyApplication.oneCaller
                    .deviceLogin(new DeviceLoginModle(/*"856424"*/ dealerPin, IMEI, SimNo, SerialNo, MAC, IP,getResources().getString(R.string.apktype), BuildConfig.VERSION_NAME))
                    .enqueue(new Callback<DeviceLoginResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<DeviceLoginResponse> call, @NonNull Response<DeviceLoginResponse> response) {

                            if (response.isSuccessful() && response.body() != null) {
                                DeviceLoginResponse dlr = response.body();

                                if (dlr.isStatus()) {
                                    PrefUtils.saveStringPref(MainActivity.this, KEY_DEALER_ID, dlr.getdId());
                                    PrefUtils.saveStringPref(MainActivity.this, DEVICE_ID, dlr.getDevice_id());
                                    PrefUtils.saveStringPref(MainActivity.this, KEY_DEVICE_LINKED, dlr.getDealer_pin());
                                    PrefUtils.saveStringPref(MainActivity.this, KEY_CONNECTED_ID, dlr.getConnectedDid());
                                    PrefUtils.saveStringPref(MainActivity.this, TOKEN, dlr.getToken());
                                    initAutoLogin();
                                } else {
                                    etPin.setError(dlr.getMsg());
                                }
                            }
                            enableViews();
                        }

                        @Override
                        public void onFailure(@NonNull Call<DeviceLoginResponse> call, @NonNull Throwable t) {
                            enableViews();
                        }
                    });
        }


    }

    /**
     * disable the main screen views
     */
    private void disableViews() {
        if (!MainActivity.this.isFinishing() && !MainActivity.this.isDestroyed()) {
            etPin.setEnabled(false);
            btSubmit.setText(R.string.processing);
            btSubmit.setEnabled(false);
        }
    }

    /**
     * enable the main screen views
     */
    private void enableViews() {
        if (!MainActivity.this.isFinishing() && !MainActivity.this.isDestroyed()) {
            etPin.setEnabled(true);
            btSubmit.setText(R.string.submit);
            btSubmit.setEnabled(true);
        }
    }

    /**
     * @param permission      permission
     * @param permissionState request code
     */
    private void requestPermission(final String permission, final int permissionState) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

            Snackbar.make(
                    rootView,
                    R.string.permission_required,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(
                            R.string.ok,
                            new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {

                                    ActivityCompat.requestPermissions(
                                            MainActivity.this,
                                            new String[]{permission},
                                            permissionState);
                                }
                            })
                    .show();
        } else {
            Snackbar.make(
                    rootView,
                    R.string.dont_have_permission,
                    Snackbar.LENGTH_SHORT)
                    .show();

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{permission},
                    permissionState);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_ACCESS_WIFI_STATE:
            case PERMISSION_REQUEST_READ_PHONE_STATE:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    initLayoutWithPermission();
                } else {
                    Snackbar.make(rootView, R.string.permission_denied, Snackbar.LENGTH_SHORT)
                            .show();
                }
                break;
        }
    }

    /**
     * show error screen
     */
    private void showError(String message) {
        mainView.setVisibility(View.GONE);
        contactDealer.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        error.setVisibility(View.VISIBLE);
        error_text.setText(message);
    }

    /**
     * show loading bar
     */
    private void showLoading() {
        mainView.setVisibility(View.GONE);
        contactDealer.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        error.setVisibility(View.GONE);

    }

    /**
     * show contact dealer screen
     */
    private void showContactDealer() {
        mainView.setVisibility(View.GONE);
        contactDealer.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        error.setVisibility(View.GONE);
    }

    /**
     * show the main screen
     */
    private void showMainContent() {
        mainView.setVisibility(View.VISIBLE);
        contactDealer.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        error.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        isBackPressed = false;
        pending = false;
        link = false;
    }

    private boolean pending = false;
    private boolean link = false;

    @Override
    protected void onPause() {
        super.onPause();
        if (!isBackPressed) {
            this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
    }
}
