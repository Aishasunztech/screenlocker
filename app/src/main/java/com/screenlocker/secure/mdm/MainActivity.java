package com.screenlocker.secure.mdm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.mdm.base.BaseActivity;
import com.screenlocker.secure.mdm.retrofitmodels.CheckStatusModel;
import com.screenlocker.secure.mdm.retrofitmodels.CheckStatusResponse;
import com.screenlocker.secure.mdm.retrofitmodels.DealerLoginModel;
import com.screenlocker.secure.mdm.retrofitmodels.DealerLoginResponse;
import com.screenlocker.secure.mdm.retrofitmodels.LinkStatusModel;
import com.screenlocker.secure.mdm.retrofitmodels.LinkStatusResponse;
import com.screenlocker.secure.mdm.ui.LinkDeviceActivity;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.socket.utils.utils.suspendedDevice;
import static com.screenlocker.secure.utils.AppConstants.AUTH_TOKEN;
import static com.screenlocker.secure.utils.AppConstants.AUTO_LOGIN_PIN;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_NEW;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_PENDING;
import static com.screenlocker.secure.utils.AppConstants.TEMP_AUTO_LOGIN_PIN;
import static com.screenlocker.secure.utils.AppConstants.TOKEN;
import static com.screenlocker.secure.utils.AppConstants.TOKEN_EXPIRED;
import static com.screenlocker.secure.utils.AppConstants.TOKEN_INVALID;
import static com.screenlocker.secure.utils.AppConstants.TOKEN_NOT_PROVIDED;


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
        MAC = DeviceIdUtils.getMacAddress();

        defaultImei = (IMEI.size() >= 1) ? IMEI.get(0) : "";

        //get serial number
        SerialNo = DeviceIdUtils.getSerialNumber();
        SerialNo = (SerialNo == null) ? "" : SerialNo;

        IP = (IP == null) ? "" : IP;

        setToolbar(toolbar);

        initAutoLoginWithPermissions();

        lytSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Timber.i("<<<<<SwipedToRefresh>>>>>");
                initAutoLogin();
            }
        });


    }

    private void setToolbar(Toolbar mToolbar) {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Link Device");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

    /**
     *
     */
    private void initAutoLoginWithPermissions() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "initAutoLoginWithPermissions: " + DeviceIdUtils.getMacAddress());

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


    private void initAutoLogin() {

        IMEI = DeviceIdUtils.getIMEI(MainActivity.this);
        defaultImei = (IMEI.size() >= 1) ? IMEI.get(0) : "";


        showLoading();
        ((MyApplication) getApplicationContext())
                .getApiOneCaller()
                .checkDeviceStatus(new CheckStatusModel(SerialNo, DeviceIdUtils.getMacAddress()))
                .enqueue(new Callback<CheckStatusResponse>() {
                    @Override
                    public void onResponse(Call<CheckStatusResponse> call, Response<CheckStatusResponse> response) {

                        if (response.isSuccessful()) {
                            // if status is true that means dealer is valid so login is allowed


                            String msg = response.body().getMsg();

                            if (response.body().getStatus()) {
                                if (msg != null && (msg.equals("Active") || msg.equals("Trial"))) {
                                    currentStatus();
                                    setResult(RESULT_OK);
                                    finish();
                                }else if (msg.equals("new device")){
                                    showMainContent();
                                }
                                else {
                                    autologin();
                                }


                            } else {
                                // status was false show failed message
                                currentStatus();
                                Timber.d(msg);
//                                showContactDealer();
                            }
                        } else {
                            // if any error occurred show error view
                            showError();
                        }
                        Log.i(TAG, "onResponse:1 " + response);
                        if (lytSwipeRefresh.isRefreshing()) {
                            lytSwipeRefresh.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckStatusResponse> call, Throwable t) {
                        Log.i(TAG, "onFailure: " + t);
                        showError();
                        if (lytSwipeRefresh.isRefreshing()) {
                            lytSwipeRefresh.setRefreshing(false);
                        }
                    }
                });
    }


    private void currentStatus() {
        String macAddress = CommonUtils.getMacAddress();
        String serialNo = DeviceIdUtils.getSerialNumber();
        if (macAddress != null && serialNo != null) {
            new ApiUtils(MainActivity.this, macAddress, serialNo);
        }
        setResult(RESULT_OK);
        finish();

    }


    /**
     * login api for dealer auto login
     */

    private void autologin() {

        final String autoLoginPin = PrefUtils.getStringPref(this, AUTO_LOGIN_PIN);
        final String autoLoginId = PrefUtils.getStringPref(this, AppConstants.KEY_DEALER_ID);

        if (autoLoginPin != null) {

            ((MyApplication) getApplicationContext())
                    .getApiOneCaller()
                    .dealerLogin(new DealerLoginModel(/*"856424"*/ autoLoginPin))
                    .enqueue(new Callback<DealerLoginResponse>() {
                        @Override
                        public void onResponse(Call<DealerLoginResponse> call, Response<DealerLoginResponse> response) {

                            if (response.isSuccessful()) {

                                DealerLoginResponse dlr = response.body();

                                if (dlr.getStatus()) {
                                    //updating the prefs
                                    PrefUtils
                                            .saveStringPref(MainActivity.this, AppConstants.KEY_DEALER_ID, "" + dlr.getData().getDId());
                                    PrefUtils
                                            .saveStringPref(MainActivity.this, AppConstants.KEY_CONNECTED_ID, "" + dlr.getData().getConnectedDealer());


                                    PrefUtils
                                            .saveStringPref(MainActivity.this, AUTH_TOKEN, dlr.getToken());

                                    Log.e(TAG, "onResponse: login_if");

                                    checkCurrentStatusAndProceedAutoLogin(autoLoginId);

                                } else {

                                    Log.e(TAG, "onResponse: login_else");
                                    etPin.setError(dlr.getMsg());
                                }

                            }
                        }

                        @Override
                        public void onFailure(Call<DealerLoginResponse> call, Throwable t) {
                            enableViews();
                        }
                    });
        } else {
            Log.e(TAG, "autologin: SKIPPED");
            initLayoutWithPermission();

            lytSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Timber.i("<<<<<SwipedToRefresh>>>>>");
                    initLayoutWithPermission();
                }
            });
        }
    }

    /**
     * check if its linked or  pending or un-linked
     */
    private void checkCurrentStatusAndProceedAutoLogin(final String autoLoginId) {


        ((MyApplication) getApplicationContext())
                .getApiOneCaller()
                .linkDeviceStatus(
                        new LinkStatusModel(SerialNo, DeviceIdUtils.getMacAddress()),
                        PrefUtils.getStringPref(MainActivity.this, AUTH_TOKEN)
                )
                .enqueue(new Callback<LinkStatusResponse>() {
                    @Override
                    public void onResponse(Call<LinkStatusResponse> call, Response<LinkStatusResponse> response) {
                        if (response.isSuccessful()) {

                            LinkStatusResponse lsr = response.body();

                            if (lsr.getStatus() != null) {

                                String dIdLinked = lsr.getDealer_id();

                                switch (response.body().getStatus()) {

                                    case DEVICE_NEW:
                                        //for the first time
                                        initLayoutWithPermission();
                                        break;

                                    case DEVICE_PENDING:
                                    case DEVICE_LINKED:

                                        if (dIdLinked.equals("" + autoLoginId)) {
                                            startActivity(new Intent(MainActivity.this, LinkDeviceActivity.class));
                                            finish();
                                        } else {
                                            initLayoutWithPermission();
                                        }
                                        break;

                                    default:
                                        showError();

                                }
                            } else if (lsr.getMsg() != null) {

                                switch (lsr.getMsg()) {

                                    case TOKEN_EXPIRED:
                                    case TOKEN_INVALID:
                                    case TOKEN_NOT_PROVIDED:
                                        Toast.makeText(MainActivity.this, "Session expired", Toast.LENGTH_SHORT).show();
                                        initLayoutWithPermission();
                                }
                            }

                        } else {
                            initLayoutWithPermission();
                        }
                    }

                    @Override
                    public void onFailure(Call<LinkStatusResponse> call, Throwable t) {
                        showError();
                    }
                });

    }


    private void initLayoutWithPermission() {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                initLayout();
            } else {

                requestPermission(Manifest.permission.READ_PHONE_STATE, PERMISSION_REQUEST_READ_PHONE_STATE);
            }

        } else {

            requestPermission(Manifest.permission.ACCESS_WIFI_STATE, PERMISSION_REQUEST_ACCESS_WIFI_STATE);
        }
    }

    private void initLayout() {


        IMEI = DeviceIdUtils.getIMEI(MainActivity.this);
        defaultImei = (IMEI.size() >= 1) ? IMEI.get(0) : "";
        showLoading();

        ((MyApplication) getApplicationContext())
                .getApiOneCaller()
                .checkDeviceStatus(new CheckStatusModel(SerialNo, DeviceIdUtils.getMacAddress()))
                .enqueue(new Callback<CheckStatusResponse>() {
                    @Override
                    public void onResponse(Call<CheckStatusResponse> call, Response<CheckStatusResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body().getStatus()) {


                                String msg = response.body().getMsg();
                                if (msg != null && (msg.equals("Active") || msg.equals("Trial"))) {
                                    checkCurrentStatusAndProceed();
                                } else {
                                    showMainContent();
                                }


                            } else {

                                currentStatus();
                                setResult(RESULT_OK);
                                finish();
//                                showContactDealer();
                            }
                        } else {
                            showError();
                        }
                        Log.i(TAG, "onResponse: " + response);
                        if (lytSwipeRefresh.isRefreshing()) {
                            lytSwipeRefresh.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckStatusResponse> call, Throwable t) {
                        Log.i(TAG, "onFailure: " + t);
                        showError();
                        if (lytSwipeRefresh.isRefreshing()) {
                            lytSwipeRefresh.setRefreshing(false);
                        }
                    }
                });
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

    private void handleSubmit() {

        final String dealerPin = etPin.getText().toString().trim();

        if (dealerPin.length() == 6) {

            request(1, dealerPin);
        } else if (dealerPin.length() == 7) {
            request(2, dealerPin);

        } else {
            etPin.setError("Invalid Dealer or Code");
        }


    }


    private void request(int type, String dealerPin) {
        disableViews();
        if (type == 1) {
            Log.d("kldjddf", "request: " + type);
            ((MyApplication) getApplicationContext())
                    .getApiOneCaller()
                    .dealerLogin(new DealerLoginModel(/*"856424"*/ dealerPin))
                    .enqueue(new Callback<DealerLoginResponse>() {
                        @Override
                        public void onResponse(Call<DealerLoginResponse> call, Response<DealerLoginResponse> response) {

                            if (response.isSuccessful()) {
                                DealerLoginResponse dlr = response.body();
                                if (dlr != null && dlr.getStatus()) {
                                    PrefUtils
                                            .saveStringPref(MainActivity.this, AppConstants.KEY_DEALER_ID, "" + dlr.getData().getDId());
                                    PrefUtils
                                            .saveStringPref(MainActivity.this, AppConstants.KEY_CONNECTED_ID, "" + dlr.getData().getConnectedDealer());
                                    PrefUtils
                                            .saveStringPref(MainActivity.this, AUTH_TOKEN, dlr.getToken());
                                    PrefUtils.saveStringPref(MainActivity.this, TEMP_AUTO_LOGIN_PIN, dealerPin);
                                    Log.e(TAG, "onResponse: TEMP_PIN_ADDED" + dealerPin);
                                    checkCurrentStatusAndProceed();
                                } else {
                                    etPin.setError(dlr.getMsg());
                                }
                            }
                            enableViews();
                        }

                        @Override
                        public void onFailure(Call<DealerLoginResponse> call, Throwable t) {
                            enableViews();
                        }
                    });
        } else if (type == 2) {

            Log.d("kldjddf", "request: " + type);
            Log.d("kldjddf", "mac: " + MAC);

            ((MyApplication) getApplicationContext())
                    .getApiOneCaller()
                    .dealerLogin(new DealerLoginModel(/*"856424"*/ dealerPin, IMEI, SimNo, SerialNo, MAC, IP))
                    .enqueue(new Callback<DealerLoginResponse>() {
                        @Override
                        public void onResponse(Call<DealerLoginResponse> call, Response<DealerLoginResponse> response) {

                            if (response.isSuccessful()) {
                                DealerLoginResponse dlr = response.body();
                                if (dlr != null && dlr.getStatus()) {
                                    PrefUtils
                                            .saveStringPref(MainActivity.this, AppConstants.KEY_DEALER_ID, "" + dlr.getData().getDId());
                                    PrefUtils
                                            .saveStringPref(MainActivity.this, AppConstants.KEY_CONNECTED_ID, "" + dlr.getData().getConnectedDealer());
                                    PrefUtils
                                            .saveStringPref(MainActivity.this, AUTH_TOKEN, dlr.getToken());
                                    PrefUtils.saveStringPref(MainActivity.this, TEMP_AUTO_LOGIN_PIN, dealerPin);
                                    Log.e(TAG, "onResponse: TEMP_PIN_ADDED" + dealerPin);
                                    checkCurrentStatusAndProceed();
                                } else {
                                    etPin.setError(dlr.getMsg());
                                }
                            }
                            enableViews();
                        }

                        @Override
                        public void onFailure(Call<DealerLoginResponse> call, Throwable t) {
                            enableViews();
                        }
                    });
        }


    }

    /**
     * check if its linked or  pending or un-linked
     */
    private void checkCurrentStatusAndProceed() {

        final String Did_current = PrefUtils.getStringPref(MainActivity.this, AppConstants.KEY_DEALER_ID);

        ((MyApplication) getApplicationContext())
                .getApiOneCaller()
                .linkDeviceStatus(
                        new LinkStatusModel(SerialNo, DeviceIdUtils.getMacAddress()),
                        PrefUtils.getStringPref(MainActivity.this, AUTH_TOKEN)
                )
                .enqueue(new Callback<LinkStatusResponse>() {
                    @Override
                    public void onResponse(Call<LinkStatusResponse> call, Response<LinkStatusResponse> response) {
                        if (response.isSuccessful()) {

                            LinkStatusResponse lsr = response.body();
                            if (lsr.getStatus() != null) {

                                String dIdLinked = lsr.getDealer_id();

                                switch (response.body().getStatus()) {

                                    case DEVICE_NEW:
                                        // if device is new link the device
                                        startActivity(new Intent(MainActivity.this, LinkDeviceActivity.class));
                                        finish();
                                        break;

                                    case DEVICE_PENDING:
                                    case DEVICE_LINKED:

                                        if (dIdLinked.equals("" + Did_current)) {
                                            startActivity(new Intent(MainActivity.this, LinkDeviceActivity.class));
                                            finish();
                                        } else {
                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle(R.string.info)
                                                    .setMessage(getString(R.string.device_already_linked))
                                                    .setPositiveButton(R.string.ok, null)
                                                    .create()
                                                    .show();
                                        }
                                        break;

                                    default:
                                        showError();

                                }
                            } else if (lsr.getMsg() != null) {

                                switch (lsr.getMsg()) {

                                    case TOKEN_EXPIRED:
                                    case TOKEN_INVALID:
                                    case TOKEN_NOT_PROVIDED:
                                        Toast.makeText(MainActivity.this, R.string.session_expired, Toast.LENGTH_SHORT).show();
                                        initLayoutWithPermission();
                                }
                            }

                        } else {
                            showError();
                        }
                    }

                    @Override
                    public void onFailure(Call<LinkStatusResponse> call, Throwable t) {
                        showError();
                    }
                });
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
    private void showError() {
        mainView.setVisibility(View.GONE);
        contactDealer.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        error.setVisibility(View.VISIBLE);
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!isBackPressed)
        {
            this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
    }
}
