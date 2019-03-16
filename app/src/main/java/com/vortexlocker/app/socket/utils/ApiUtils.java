package com.vortexlocker.app.socket.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.vortexlocker.app.app.MyApplication;
import com.vortexlocker.app.networkResponseModels.DealerLoginResponse;
import com.vortexlocker.app.settings.SettingContract.SettingsMvpView;
import com.vortexlocker.app.settings.SettingsActivity;
import com.vortexlocker.app.socket.SocketSingleton;
import com.vortexlocker.app.socket.interfaces.ApiRequests;
import com.vortexlocker.app.socket.interfaces.RefreshListener;
import com.vortexlocker.app.socket.service.SocketService;
import com.vortexlocker.app.utils.AppConstants;
import com.vortexlocker.app.utils.PrefUtils;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.vortexlocker.app.socket.utils.utils.suspendedDevice;
import static com.vortexlocker.app.socket.utils.utils.unSuspendDevice;
import static com.vortexlocker.app.socket.utils.utils.unlinkDevcie;
import static com.vortexlocker.app.utils.AppConstants.DEVICE_ID;
import static com.vortexlocker.app.utils.AppConstants.DEVICE_STATUS;
import static com.vortexlocker.app.utils.AppConstants.DEVICE_STATUS_CHANGE_RECEIVER;
import static com.vortexlocker.app.utils.AppConstants.TOKEN;
import static com.vortexlocker.app.utils.AppConstants.VALUE_EXPIRED;

public class ApiUtils implements ApiRequests, RefreshListener {

    private Context context;
    private String macAddress;

    public ApiUtils(Context context, String macAddress) {
        this.context = context;
        this.macAddress = macAddress;
        SettingsActivity settingsActivity = new SettingsActivity();
        settingsActivity.setRefreshListener(this);
        getDeviceId();
    }

    @Override
    public void getDeviceId() {
        Timber.d("<<< getting device id >>>");
        ((MyApplication) context.getApplicationContext())
                .getApiOneCaller()
                .getDeviceId(macAddress)
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
                            PrefUtils.saveStringPref(context, DEVICE_ID, device_id);
                            PrefUtils.saveStringPref(context, TOKEN, token);
                            if (device_id != null && token != null) {
                                new SocketUtils(device_id, context, token);
                            }
                            String msg = response.body().getMsg();
                            Intent socketIntent = new Intent(context, SocketService.class);
                            Timber.d(" msg : %S", msg);
                            if (msg != null) {
                                switch (msg) {
                                    case "suspended":
                                        suspendedDevice(context, (SettingsMvpView) context, device_id, "suspended");
                                        utils.sendBroadcast(context, "suspended");
                                        Timber.d("<<< device suspended >>>");
                                        break;
                                    case "success":
                                        unSuspendDevice(context);
                                        utils.sendBroadcast(context, null);
                                        Timber.d("<<< device activated >>>");
                                        break;
                                    case "expired":
                                        suspendedDevice(context, (SettingsMvpView) context, device_id, "expired");
                                        utils.sendBroadcast(context, "expired");
                                        Timber.d("<<< device expired >>>");
                                        break;
                                    case "unliked":
                                        unlinkDevcie(context);
                                        break;
                                    case "Device not linked":
                                        context.stopService(socketIntent);
                                        break;
                                    case "Invalid Device":
                                        context.stopService(socketIntent);
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
                                unlinkDevcie(context);
                                break;
                        }

                        context.stopService(socketIntent);
                    }
                });

    }

    @Override
    public void onSwipe() {
        Timber.d("<<< swipe to refresh >>>");
        String device_id = PrefUtils.getStringPref(context, AppConstants.DEVICE_ID);
        SocketSingleton.closeSocket(device_id);
        getDeviceId();
    }
}
