package com.screenlocker.secure.socket.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.mdm.retrofitmodels.DealerLoginModel;
import com.screenlocker.secure.networkResponseModels.DealerLoginResponse;
import com.screenlocker.secure.socket.interfaces.ApiRequests;
import com.screenlocker.secure.socket.service.SocketService;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.socket.utils.utils.suspendedDevice;
import static com.screenlocker.secure.socket.utils.utils.unSuspendDevice;
import static com.screenlocker.secure.socket.utils.utils.unlinkDevice;
import static com.screenlocker.secure.socket.utils.utils.wipeDevice;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS_CHANGE_RECEIVER;
import static com.screenlocker.secure.utils.AppConstants.TOKEN;
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

        getDeviceId();
    }


    @Override
    public void getDeviceId() {
        Timber.d("<<< getting device id >>>");
        ((MyApplication) context.getApplicationContext())
                .getApiOneCaller()
                .getDeviceId(new DealerLoginModel(serialNo, macAddress))
                .enqueue(new Callback<DealerLoginResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<DealerLoginResponse> call, @NotNull Response<DealerLoginResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {

                            Timber.d("Expiresin %s", response.body().getExpiresIn());
                            String expire_date = response.body().getExpiresIn();
                            if (expire_date != null) {
                                PrefUtils.saveStringPref(context, VALUE_EXPIRED, expire_date);
                            }
                            Timber.d(" response successful ");
                            String device_id = response.body().getDevice_id();
                            Timber.d(" device_id : %S", device_id);
                            String token = response.body().getToken();
                            Timber.d(" token : %S", token);
                            if (device_id != null) {
                                PrefUtils.saveStringPref(context, DEVICE_ID, device_id);
                            }
                            PrefUtils.saveStringPref(context, TOKEN, token);

                            if (device_id != null && token != null) {

                                Intent intent = new Intent(context, SocketService.class);
                                intent.setAction("start");

                                PrefUtils.saveStringPref(context,DEVICE_ID,device_id);
                                PrefUtils.saveStringPref(context,TOKEN,token);

                                PrefUtils.saveBooleanPref(context, AppConstants.DEVICE_LINKED_STATUS,true);

                                if (Build.VERSION.SDK_INT >= 26) {
                                    context.startForegroundService(intent);
                                } else {
                                    context.startService(intent);
                                }

                            }

                            String msg = response.body().getMsg();
                            Intent socketIntent = new Intent(context, SocketService.class);
                            Timber.d(" msg : %S", msg);
                            if (msg != null) {
                                switch (msg) {
                                    case "suspended":
                                        suspendedDevice(context, device_id, "suspended");
                                        utils.sendBroadcast(context, "suspended");
                                        Timber.d("<<< device suspended >>>");
                                        break;
                                    case "success":
                                        unSuspendDevice(context);
                                        utils.sendBroadcast(context, null);
                                        Timber.d("<<< device activated >>>");
                                        break;
                                    case "expired":
                                        suspendedDevice(context, device_id, "expired");
                                        utils.sendBroadcast(context, "expired");
                                        Timber.d("<<< device expired >>>");
                                        break;
                                    case "unlinked":
                                        unlinkDevice(context);
                                        break;
                                    case "Device not linked":
                                        context.stopService(socketIntent);
                                        break;
                                    case "Invalid Device":
                                        context.stopService(socketIntent);
                                        break;
                                    case "wiped":
                                        wipeDevice(context);
                                        break;
                                }
                            }

                        } else {
                            Timber.e(" response failure ");
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<DealerLoginResponse> call, @NotNull Throwable t) {
                        Timber.d("  error : %S", t.getMessage());
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
                                unlinkDevice(context);
                                break;
                        }

                        context.stopService(socketIntent);
                    }
                });

    }


}
