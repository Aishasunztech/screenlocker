package com.screenlocker.secure.socket.utils;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.secure.launcher.R;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceModel;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceStatusResponse;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.socket.interfaces.ApiRequests;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.socket.utils.utils.suspendedDevice;
import static com.screenlocker.secure.socket.utils.utils.unlinkDeviceWithMsg;
import static com.screenlocker.secure.utils.AppConstants.ACTIVE;
import static com.screenlocker.secure.utils.AppConstants.CHAT_ID;
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
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.NEW_DEVICE;
import static com.screenlocker.secure.utils.AppConstants.PENDING;
import static com.screenlocker.secure.utils.AppConstants.PGP_EMAIL;
import static com.screenlocker.secure.utils.AppConstants.SIM_ID;
import static com.screenlocker.secure.utils.AppConstants.SIM_ID2;
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
    private PrefUtils prefUtils;


    public ApiUtils(Context context, String macAddress, String serialNo) {
        this.context = context;
        prefUtils = PrefUtils.getInstance(context);
        this.macAddress = macAddress;
        this.serialNo = serialNo;

        Timber.d("serialNo :%s", serialNo);
        Timber.d("macAddress :%s", macAddress);

        //connectToSocket();
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
                    prefUtils.saveStringPref( LIVE_URL, output);
                    String live_url = prefUtils.getStringPref( LIVE_URL);
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
                            DeviceStatusResponse dsr = response.body();
                            String msg = dsr.getMsg();
                            Timber.d("response :" + msg);
                            if (dsr.isStatus()) {

                                saveInfo(dsr.getToken(), dsr.getDevice_id(), dsr.getExpiry_date(), dsr.getDealer_pin(), dsr.getUser_id(), dsr.getChatId(), dsr.getPgpID(), dsr.getSimId1(), dsr.getSimId2());

                                switch (msg) {
                                    case ACTIVE:
                                    case TRIAL:
                                        prefUtils.saveBooleanPref( AppConstants.PENDING_ACTIVATION, false);
                                        prefUtils.saveBooleanPref( DEVICE_LINKED_STATUS, true);
                                        utils.unSuspendDevice(context);
                                        break;
                                    case EXPIRED:
                                        prefUtils.saveBooleanPref( AppConstants.PENDING_ACTIVATION, false);
                                        prefUtils.saveBooleanPref( DEVICE_LINKED_STATUS, true);
                                        utils.suspendedDevice(context, "expired");
                                        break;
                                    case SUSPENDED:
                                        prefUtils.saveBooleanPref( AppConstants.PENDING_ACTIVATION, false);
                                        prefUtils.saveBooleanPref( DEVICE_LINKED_STATUS, true);
                                        utils.suspendedDevice(context, "suspended");
                                        break;
                                    case FLAGGED:
                                        prefUtils.saveBooleanPref( AppConstants.PENDING_ACTIVATION, false);
                                        prefUtils.saveBooleanPref( DEVICE_LINKED_STATUS, true);
                                        suspendedDevice(context, "flagged");
                                        break;
                                    case PENDING:
                                        prefUtils.saveBooleanPref( AppConstants.PENDING_ACTIVATION, true);
                                        break;
                                }
                            } else {

                                prefUtils.saveStringPref( DEVICE_ID, response.body().getDevice_id());

                                prefUtils.saveBooleanPref( AppConstants.PENDING_ACTIVATION, false);
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
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DeviceStatusResponse> call, @NonNull Throwable t) {
                        Timber.d(t);
                        String device_status = prefUtils.getStringPref( DEVICE_STATUS);
                        Intent intent = new Intent(DEVICE_STATUS_CHANGE_RECEIVER);
                        if (device_status == null) {
                            intent.putExtra("device_status", (String) null);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            utils.stopSocket(context);
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
                                unlinkDeviceWithMsg(context, true, "unlinked");
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

                        utils.stopSocket(context);
                    }
                });
    }

    private void saveInfo(String token, String device_id, String expiry_date, String dealer_pin, String userId, String chatId, String pgpId, String simId1, String simId2) {
        prefUtils.saveStringPref( TOKEN, token);
        prefUtils.saveStringPref( DEVICE_ID, device_id);
        prefUtils.saveStringPref( VALUE_EXPIRED, expiry_date);
        prefUtils.saveStringPref( USER_ID, userId);
        prefUtils.saveStringPref( CHAT_ID, chatId);
        prefUtils.saveStringPref( PGP_EMAIL, pgpId);
        prefUtils.saveStringPref( SIM_ID, simId1);
        prefUtils.saveStringPref( SIM_ID2, simId2);
        prefUtils.saveStringPref( KEY_DEVICE_LINKED, dealer_pin);

    }

}
