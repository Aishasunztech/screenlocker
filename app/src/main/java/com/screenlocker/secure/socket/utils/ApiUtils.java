package com.screenlocker.secure.socket.utils;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.CheckInstance;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceModel;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceStatusResponse;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.socket.interfaces.ApiRequests;
import com.screenlocker.secure.socket.service.SocketService;
import com.screenlocker.secure.utils.PrefUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.socket.utils.utils.unlinkDevice;
import static com.screenlocker.secure.utils.AppConstants.ACTIVE;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS_CHANGE_RECEIVER;
import static com.screenlocker.secure.utils.AppConstants.EXPIRED;
import static com.screenlocker.secure.utils.AppConstants.KEY_DEVICE_LINKED;
import static com.screenlocker.secure.utils.AppConstants.NEW_DEVICE;
import static com.screenlocker.secure.utils.AppConstants.SUSPENDED;
import static com.screenlocker.secure.utils.AppConstants.TOKEN;
import static com.screenlocker.secure.utils.AppConstants.TRIAL;
import static com.screenlocker.secure.utils.AppConstants.UNLINKED_DEVICE;
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
        new CheckInstance(internet -> {

            if (internet) {
                Timber.d("Instance" + MyApplication.oneCaller);

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
                                        saveInfo(deviceStatusResponse.getToken(), deviceStatusResponse.getDevice_id(), deviceStatusResponse.getExpiry_date(), deviceStatusResponse.getDealer_pin());
                                        switch (msg) {
                                            case ACTIVE:
                                                utils.unSuspendDevice(context);
                                                break;
                                            case EXPIRED:
                                                utils.suspendedDevice(context, "expired");
                                                break;
                                            case SUSPENDED:
                                                utils.suspendedDevice(context, "suspended");
                                                break;
                                            case TRIAL:
                                                utils.unSuspendDevice(context);
                                                break;
                                        }
                                    } else {
                                        switch (msg) {
                                            case UNLINKED_DEVICE:
                                                utils.unlinkDevice(context, true);
                                                break;
                                            case NEW_DEVICE:
                                                utils.unlinkDevice(context, true);
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
        });


    }

    private void saveInfo(String token, String device_id, String expiry_date, String dealer_pin) {
        PrefUtils.saveStringPref(context, TOKEN, token);
        PrefUtils.saveStringPref(context, DEVICE_ID, device_id);
        PrefUtils.saveStringPref(context, VALUE_EXPIRED, expiry_date);
        PrefUtils.saveStringPref(context, KEY_DEVICE_LINKED, dealer_pin);
    }

}
