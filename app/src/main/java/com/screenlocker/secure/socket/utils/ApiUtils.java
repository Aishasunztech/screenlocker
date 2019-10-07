package com.screenlocker.secure.socket.utils;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceModel;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceStatusResponse;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.socket.interfaces.ApiRequests;
import com.screenlocker.secure.socket.service.SocketService;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.socket.utils.utils.suspendedDevice;
import static com.screenlocker.secure.socket.utils.utils.unlinkDevice;
import static com.screenlocker.secure.utils.AppConstants.ACTIVE;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS_CHANGE_RECEIVER;
import static com.screenlocker.secure.utils.AppConstants.EXPIRED;
import static com.screenlocker.secure.utils.AppConstants.FLAGGED;
import static com.screenlocker.secure.utils.AppConstants.KEY_DEVICE_LINKED;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.NEW_DEVICE;
import static com.screenlocker.secure.utils.AppConstants.PENDING;
import static com.screenlocker.secure.utils.AppConstants.SUSPENDED;
import static com.screenlocker.secure.utils.AppConstants.TOKEN;
import static com.screenlocker.secure.utils.AppConstants.TRIAL;
import static com.screenlocker.secure.utils.AppConstants.UNLINKED_DEVICE;
import static com.screenlocker.secure.utils.AppConstants.URL_1;
import static com.screenlocker.secure.utils.AppConstants.URL_2;
import static com.screenlocker.secure.utils.AppConstants.USER_ID;
import static com.screenlocker.secure.utils.AppConstants.VALUE_EXPIRED;

public class ApiUtils implements ApiRequests {

    private Context context;
    private String macAddress;
    private String serialNo;


    public ApiUtils(Context context, String macAddress, String serialNo) {
        this.context = context;
        this.macAddress = macAddress;
        this.serialNo = serialNo;

        Timber.d("serialNo :%s", serialNo);
        Timber.d("macAddress :%s", macAddress);

        connectToSocket();
    }


    @Override
    public void connectToSocket() {

        if (MyApplication.oneCaller == null) {

            String[] urls = {URL_1, URL_2};

            if (asyncCalls != null) {
                asyncCalls.cancel(true);
            }

            asyncCalls = new AsyncCalls(output -> {
                if (output != null) {
                    PrefUtils.saveStringPref(context, LIVE_URL, output);
                    String live_url = PrefUtils.getStringPref(context, LIVE_URL);
                    Timber.d("live_url %s", live_url);
                    MyApplication.oneCaller = RetrofitClientInstance.getRetrofitInstance(live_url + MOBILE_END_POINT).create(ApiOneCaller.class);
                    runSocket();
                }
            }, context, urls);// checking hosts
            asyncCalls.execute();

        } else {
            runSocket();
        }
    }

    private AsyncCalls asyncCalls;

    private void runSocket() {


        MyApplication.oneCaller
                .checkDeviceStatus(new DeviceModel(serialNo, DeviceIdUtils.getIPAddress(true), context.getPackageName() + context.getString(R.string.app_name), macAddress))
                .enqueue(new Callback<DeviceStatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DeviceStatusResponse> call, @NonNull Response<DeviceStatusResponse> response) {

                        if (response.isSuccessful() && response.body() != null) {
                            DeviceStatusResponse deviceStatusResponse = response.body();
                            String msg = deviceStatusResponse.getMsg();
                            Timber.d("response :" + msg);
                            if (deviceStatusResponse.isStatus()) {

                                saveInfo(deviceStatusResponse.getToken(), deviceStatusResponse.getDevice_id(), deviceStatusResponse.getExpiry_date(), deviceStatusResponse.getDealer_pin(), deviceStatusResponse.getUser_id());

                                switch (msg) {
                                    case ACTIVE:
                                    case TRIAL:
                                        PrefUtils.saveBooleanPref(context, AppConstants.PENDING_ACTIVATION, false);
                                        PrefUtils.saveBooleanPref(context, DEVICE_LINKED_STATUS, true);
                                        utils.unSuspendDevice(context);
                                        break;
                                    case EXPIRED:
                                        PrefUtils.saveBooleanPref(context, AppConstants.PENDING_ACTIVATION, false);
                                        PrefUtils.saveBooleanPref(context, DEVICE_LINKED_STATUS, true);
                                        utils.suspendedDevice(context, "expired");
                                        break;
                                    case SUSPENDED:
                                        PrefUtils.saveBooleanPref(context, AppConstants.PENDING_ACTIVATION, false);
                                        PrefUtils.saveBooleanPref(context, DEVICE_LINKED_STATUS, true);
                                        utils.suspendedDevice(context, "suspended");
                                        break;
                                    case FLAGGED:
                                        PrefUtils.saveBooleanPref(context, AppConstants.PENDING_ACTIVATION, false);
                                        PrefUtils.saveBooleanPref(context, DEVICE_LINKED_STATUS, true);
                                        suspendedDevice(context, "flagged");
                                        break;
                                    case PENDING:
                                        PrefUtils.saveBooleanPref(context, AppConstants.PENDING_ACTIVATION, true);
                                        break;
                                }
                            } else {
                                PrefUtils.saveBooleanPref(context, AppConstants.PENDING_ACTIVATION, false);
                                switch (msg) {
                                    case UNLINKED_DEVICE:
                                        PrefUtils.saveBooleanPref(context, AppConstants.PENDING_ACTIVATION, true);
                                        utils.unlinkDevice(context, true);
                                        break;
                                    case NEW_DEVICE:
                                        PrefUtils.saveBooleanPref(context, AppConstants.PENDING_ACTIVATION, true);
                                        utils.newDevice(context, true);
                                        break;

                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DeviceStatusResponse> call, @NonNull Throwable t) {
                        Timber.d(t);
                        String device_status = PrefUtils.getStringPref(context, DEVICE_STATUS);
                        Intent intent = new Intent(DEVICE_STATUS_CHANGE_RECEIVER);
                        Intent socketIntent = new Intent(context, SocketService.class);
                        if (device_status == null) {
                            intent.putExtra("device_status", (String) null);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            context.stopService(socketIntent);
                            return;
                        }
                        switch (device_status) {
                            case "expired":
                                intent.putExtra("device_status", "expired");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                break;
                            case "suspended":
                                intent.putExtra("device_status", "suspended");
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                                break;
                            case "unliked":
                                unlinkDevice(context, true);
                                break;
                        }

                        context.stopService(socketIntent);
                    }
                });
    }

    private void saveInfo(String token, String device_id, String expiry_date, String dealer_pin, String userId) {
        PrefUtils.saveStringPref(context, TOKEN, token);
        PrefUtils.saveStringPref(context, DEVICE_ID, device_id);
        PrefUtils.saveStringPref(context, VALUE_EXPIRED, expiry_date);
        PrefUtils.saveStringPref(context, USER_ID, userId);
        PrefUtils.saveStringPref(context, KEY_DEVICE_LINKED, dealer_pin);

    }

}
