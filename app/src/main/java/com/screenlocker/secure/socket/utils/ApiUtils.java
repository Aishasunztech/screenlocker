package com.screenlocker.secure.socket.utils;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.screenlocker.secure.R;
import com.screenlocker.secure.mdm.MainActivity;
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

import static com.screenlocker.secure.socket.utils.utils.saveLiveUrl;
import static com.screenlocker.secure.socket.utils.utils.suspendedDevice;
import static com.screenlocker.secure.socket.utils.utils.unlinkDeviceWithMsg;
import static com.screenlocker.secure.utils.AppConstants.ACTIVE;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS_CHANGE_RECEIVER;
import static com.screenlocker.secure.utils.AppConstants.DUPLICATE_MAC;
import static com.screenlocker.secure.utils.AppConstants.DUPLICATE_MAC_AND_SERIAL;
import static com.screenlocker.secure.utils.AppConstants.DUPLICATE_SERIAL;
import static com.screenlocker.secure.utils.AppConstants.EXPIRED;
import static com.screenlocker.secure.utils.AppConstants.FLAGGED;
import static com.screenlocker.secure.utils.AppConstants.KEY_DEVICE_LINKED;
import static com.screenlocker.secure.utils.AppConstants.NEW_DEVICE;
import static com.screenlocker.secure.utils.AppConstants.PENDING;
import static com.screenlocker.secure.utils.AppConstants.PENDING_ACTIVATION;
import static com.screenlocker.secure.utils.AppConstants.SUSPENDED;
import static com.screenlocker.secure.utils.AppConstants.TOKEN;
import static com.screenlocker.secure.utils.AppConstants.TRIAL;
import static com.screenlocker.secure.utils.AppConstants.UNLINKED_DEVICE;
import static com.screenlocker.secure.utils.AppConstants.USER_ID;
import static com.screenlocker.secure.utils.AppConstants.VALUE_EXPIRED;

public class ApiUtils implements ApiRequests {

    private Context context;
    private String macAddress;
    private String serialNo;
    private String ip;
    private String uniqueName;

    private boolean isFailSafe;


    public ApiUtils(Context context, String macAddress, String serialNo) {
        this.context = context;
        this.macAddress = macAddress;
        this.serialNo = serialNo;
        this.ip = DeviceIdUtils.getIPAddress(true);
        this.uniqueName = context.getPackageName() + context.getString(R.string.app_name);
        this.isFailSafe = false;

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

                        Timber.i("-------> Response Status : %s", responseStatus);

                        if (responseStatus) {


                            DeviceStatusResponse deviceStatusResponse = response.body();

                            if (deviceStatusResponse != null) {

                                Timber.i("------> Response Body is not null :) ");

                                String msg = deviceStatusResponse.getMsg();

                                msg = msg == null ? "n/a" : msg;

                                Timber.d("-----> msg from server :%s", msg);

                                boolean deviceStatus = deviceStatusResponse.isStatus();

                                Timber.i("-----> Device Status :%s", deviceStatus);

                                if (deviceStatus) {

                                    String device_id = deviceStatusResponse.getDevice_id();
                                    String expiry_date = deviceStatusResponse.getExpiry_date();
                                    String dealer_pin = deviceStatusResponse.getDealer_pin();

                                    Timber.i("----> device info : (1) device_id : " + device_id + " (2) expiry_date : " + expiry_date + " (3) dealer_pin : " + dealer_pin);

                                    saveInfo(deviceStatusResponse.getToken(), device_id, expiry_date, dealer_pin,deviceStatusResponse.getUser_id());

                                    switch (msg) {
                                        case ACTIVE:
                                        case TRIAL:
                                            PrefUtils.saveBooleanPref(context, PENDING_ACTIVATION, false);
                                            PrefUtils.saveBooleanPref(context, DEVICE_LINKED_STATUS, true);
                                            utils.unSuspendDevice(context);
                                            break;
                                        case EXPIRED:
                                            PrefUtils.saveBooleanPref(context, PENDING_ACTIVATION, false);
                                            PrefUtils.saveBooleanPref(context, DEVICE_LINKED_STATUS, true);
                                            utils.suspendedDevice(context, "expired");
                                            break;
                                        case SUSPENDED:
                                            PrefUtils.saveBooleanPref(context, PENDING_ACTIVATION, false);
                                            PrefUtils.saveBooleanPref(context, DEVICE_LINKED_STATUS, true);
                                            utils.suspendedDevice(context, "suspended");
                                            break;
                                        case FLAGGED:
                                            PrefUtils.saveBooleanPref(context, PENDING_ACTIVATION, false);
                                            PrefUtils.saveBooleanPref(context, DEVICE_LINKED_STATUS, true);
                                            suspendedDevice(context, "flagged");
                                            break;
                                        case PENDING:
                                            PrefUtils.saveBooleanPref(context, PENDING_ACTIVATION, true);
                                            break;
                                    }

                                } else {

                                    PrefUtils.saveBooleanPref(context, PENDING_ACTIVATION, false);
                                    switch (msg) {
                                        case UNLINKED_DEVICE:
                                            utils.unlinkDeviceWithMsg(context, true, "unlinked");
                                            break;
                                        case NEW_DEVICE:
                                            utils.newDevice(context, true);
                                            break;
                                        case DUPLICATE_MAC:
                                            utils.unlinkDeviceWithMsg(context, true, DUPLICATE_MAC);
                                            break;
                                        case DUPLICATE_SERIAL:
                                            utils.unlinkDeviceWithMsg(context, true, DUPLICATE_SERIAL);
                                            break;
                                        case DUPLICATE_MAC_AND_SERIAL:
                                            utils.unlinkDeviceWithMsg(context, true, DUPLICATE_MAC_AND_SERIAL);
                                            break;

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
                        Timber.d("onFailure : %s", t.getMessage());

                        if (t instanceof UnknownHostException) {
                            Timber.e("-----------> something very dangerous happen with domain : %s", t.getMessage());

                            if (isFailSafe) {
                                Timber.e("------------> FailSafe domain is also not working. ");
                            } else {
                                Timber.i("<<< New Api call with failsafe domain >>>");
                                checkDeviceStatus(RetrofitClientInstance.getFailSafeInstanceForWhiteLabel());
                                isFailSafe = true;
                            }

                            return;
                        } else if (t instanceof IOException) {
                            Timber.e(" ----> IO Exception :%s", t.getMessage());
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
                            case "unlinked":
                                unlinkDeviceWithMsg(context, true,"unlinked");
                                break;
                        }

                        context.stopService(socketIntent);
                    }
                });

    }

    private void saveInfo(String token, String device_id, String expiry_date, String dealer_pin,String userId) {

        Timber.i("-----------> saving device info . ");

        saveLiveUrl(isFailSafe);

        PrefUtils.saveStringPref(context, TOKEN, token);
        PrefUtils.saveStringPref(context, DEVICE_ID, device_id);
        PrefUtils.saveStringPref(context, VALUE_EXPIRED, expiry_date);
        PrefUtils.saveStringPref(context, KEY_DEVICE_LINKED, dealer_pin);
        PrefUtils.saveStringPref(context, USER_ID, userId);


    }

}
