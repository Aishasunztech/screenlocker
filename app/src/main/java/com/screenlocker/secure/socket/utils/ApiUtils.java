package com.screenlocker.secure.socket.utils;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.screenlocker.secure.R;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceModel;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceStatusResponse;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.socket.interfaces.ApiRequests;
import com.screenlocker.secure.socket.service.SocketService;
import com.screenlocker.secure.utils.PrefUtils;

import java.io.IOException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.socket.utils.utils.suspendedDevice;
import static com.screenlocker.secure.socket.utils.utils.unlinkDevice;
import static com.screenlocker.secure.utils.AppConstants.ACTIVE;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS_CHANGE_RECEIVER;
import static com.screenlocker.secure.utils.AppConstants.EXPIRED;
import static com.screenlocker.secure.utils.AppConstants.FAIL_SAFE_URL_FOR_WHITE_LABEL;
import static com.screenlocker.secure.utils.AppConstants.FLAGGED;
import static com.screenlocker.secure.utils.AppConstants.KEY_DEVICE_LINKED;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.NEW_DEVICE;
import static com.screenlocker.secure.utils.AppConstants.SUSPENDED;
import static com.screenlocker.secure.utils.AppConstants.TIME_REMAINING;
import static com.screenlocker.secure.utils.AppConstants.TOKEN;
import static com.screenlocker.secure.utils.AppConstants.TRIAL;
import static com.screenlocker.secure.utils.AppConstants.UNLINKED_DEVICE;
import static com.screenlocker.secure.utils.AppConstants.VALUE_EXPIRED;
import static com.screenlocker.secure.utils.AppConstants.WHITE_LABEL_URL;

public class ApiUtils implements ApiRequests {

    private Context context;
    private String macAddress;
    private String serialNo;
    private String ip;
    private String uniqueName;

    private boolean failSafeStatus;


    public ApiUtils(Context context, String macAddress, String serialNo) {
        this.context = context;
        this.macAddress = macAddress;
        this.serialNo = serialNo;
        this.ip = DeviceIdUtils.getIPAddress(true);
        this.uniqueName = context.getPackageName() + context.getString(R.string.app_name);
        this.failSafeStatus = true;

        Timber.d("<<< ApiUtils >>>");

        connectToSocket();
    }


    @Override
    public void connectToSocket() {

        Timber.i("------------> Connecting to socket ... ");

        checkDeviceStatus(RetrofitClientInstance.getWhiteLabelInstance());

    }


    private void checkDeviceStatus(ApiOneCaller oneCaller) {

        Timber.i("-----------> Checking device status ... ");

        oneCaller
                .checkDeviceStatus(new DeviceModel(serialNo, ip, uniqueName, macAddress))
                .enqueue(new Callback<DeviceStatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DeviceStatusResponse> call, @NonNull Response<DeviceStatusResponse> response) {

                        Timber.i("-------> onResponse of checkDeviceStatus end point. ");

                        boolean responseStatus = response.isSuccessful();

                        Timber.i("-------> Response Status : " + responseStatus);

                        if (responseStatus) {


                            DeviceStatusResponse deviceStatusResponse = response.body();

                            if (deviceStatusResponse != null) {

                                Timber.i("------> Response Body is not null :) ");

                                String msg = deviceStatusResponse.getMsg();

                                msg = msg == null ? "n/a" : msg;

                                Timber.d("-----> msg from server :" + msg);

                                boolean deviceStatus = deviceStatusResponse.isStatus();

                                Timber.i("-----> Device Status :" + deviceStatus);

                                if (deviceStatus) {

                                    String device_id = deviceStatusResponse.getDevice_id();
                                    String expiry_date = deviceStatusResponse.getExpiry_date();
                                    String dealer_pin = deviceStatusResponse.getDealer_pin();

                                    Timber.i("----> device info : (1) device_id : " + device_id + " (2) expiry_date : " + expiry_date + " (3) dealer_pin : " + dealer_pin);

                                    saveInfo(deviceStatusResponse.getToken(), device_id, expiry_date, dealer_pin);

                                    switch (msg) {
                                        case ACTIVE:
                                            Timber.i("---------> device is active. ");
                                            utils.unSuspendDevice(context);
                                            break;
                                        case EXPIRED:
                                            Timber.i("---------> device is expired. ");
                                            utils.suspendedDevice(context, "expired");
                                            break;
                                        case SUSPENDED:
                                            Timber.i("---------> device is suspended. ");
                                            utils.suspendedDevice(context, "suspended");
                                            break;
                                        case TRIAL:
                                            Timber.i("---------> device is on trial. ");
                                            utils.unSuspendDevice(context);
                                            break;
                                        case FLAGGED:
                                            Timber.i("---------> device is flagged. ");
                                            suspendedDevice(context, "flagged");
                                            break;
                                        default:
                                            Timber.e("-------> wrong msg from server :" + msg);
                                    }
                                } else {

                                    switch (msg) {
                                        case UNLINKED_DEVICE:
                                            Timber.i("-----> device is unlinked. ");
                                            utils.unlinkDevice(context, true);
                                            break;
                                        case NEW_DEVICE:
                                            Timber.i("-----> new device");
                                            utils.newDevice(context, true);
                                            break;
                                        default:
                                            Timber.e("-------> wrong msg from server :" + msg);
                                    }
                                }

                            } else {
                                Timber.e("----------> Response Body is null :( ");
                            }

                        } else {
                            Timber.e("------> Response is not successful :( ");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DeviceStatusResponse> call, @NonNull Throwable t) {
                        Timber.d("onFailure : " + t);

                        if (t instanceof UnknownHostException) {
                            Timber.e("-----------> something very dangerous happen with domain : " + t.getMessage());

                            if (failSafeStatus) {
                                Timber.i("<<< New Api call with failsafe domain >>>");
                                checkDeviceStatus(RetrofitClientInstance.getFailSafeInstanceForWhiteLabel());
                                failSafeStatus = false;
                            } else {
                                Timber.e("------------> FailSafe domain is also not working. ");
                            }

                            return;
                        } else if (t instanceof IOException) {
                            Timber.e(" ----> IO Exception :" + t.getMessage());
                        }


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

    private void saveInfo(String token, String device_id, String expiry_date, String dealer_pin) {

        Timber.i("-----------> saving device info . ");

        if (failSafeStatus) {
            Timber.d("-------> Saving Live Url . " + WHITE_LABEL_URL);
            PrefUtils.saveStringPref(context, LIVE_URL, WHITE_LABEL_URL);
        } else {
            Timber.d("-------> Saving Live FailSafe Url . " + FAIL_SAFE_URL_FOR_WHITE_LABEL);

            PrefUtils.saveStringPref(context, LIVE_URL, FAIL_SAFE_URL_FOR_WHITE_LABEL);
        }

        PrefUtils.saveStringPref(context, TOKEN, token);
        PrefUtils.saveStringPref(context, DEVICE_ID, device_id);
        PrefUtils.saveStringPref(context, VALUE_EXPIRED, expiry_date);
        PrefUtils.saveStringPref(context, KEY_DEVICE_LINKED, dealer_pin);


    }

}
