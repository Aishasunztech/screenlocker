package com.screenlocker.secure.mdm;

import android.app.job.JobParameters;
import android.app.job.JobService;

import androidx.annotation.NonNull;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.CheckInstance;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceModel;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceStatusResponse;
import com.screenlocker.secure.mdm.ui.LinkDeviceActivity;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.utils.PrefUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.screenlocker.secure.utils.AppConstants.ACTIVE;
import static com.screenlocker.secure.utils.AppConstants.DEALER_NOT_FOUND;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DUPLICATE_MAC;
import static com.screenlocker.secure.utils.AppConstants.DUPLICATE_MAC_AND_SERIAL;
import static com.screenlocker.secure.utils.AppConstants.DUPLICATE_SERIAL;
import static com.screenlocker.secure.utils.AppConstants.EXPIRED;
import static com.screenlocker.secure.utils.AppConstants.KEY_DEVICE_LINKED;
import static com.screenlocker.secure.utils.AppConstants.NEW_DEVICE;
import static com.screenlocker.secure.utils.AppConstants.PENDING;
import static com.screenlocker.secure.utils.AppConstants.SUSPENDED;
import static com.screenlocker.secure.utils.AppConstants.TOKEN;
import static com.screenlocker.secure.utils.AppConstants.TRIAL;
import static com.screenlocker.secure.utils.AppConstants.UNLINKED_DEVICE;
import static com.screenlocker.secure.utils.AppConstants.VALUE_EXPIRED;

/**
 * @author Muhammad Nadeem
 * @Date 6/24/2019.
 */
public class LinkStatusJobScheduler extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        String device_id = PrefUtils.getStringPref(this, DEVICE_ID);
        /*if (device_id != null) {
            new CheckInstance(internet -> {
                if (internet) {
                    MyApplication.oneCaller
                            .checkDeviceStatus(new DeviceModel(DeviceIdUtils.getSerialNumber(), DeviceIdUtils.getIPAddress(true), getPackageName() + getString(R.string.app_name), DeviceIdUtils.getMacAddress()))
                            .enqueue(new Callback<DeviceStatusResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<DeviceStatusResponse> call, @NonNull Response<DeviceStatusResponse> response) {

                                    if (response.isSuccessful() && response.body() != null) {

                                        String msg = response.body().getMsg();


                                        boolean isLinked = PrefUtils.getBooleanPref(LinkStatusJobScheduler.this, DEVICE_LINKED_STATUS);
                                        if (response.body().isStatus()) {

                                            switch (msg) {
                                                case ACTIVE:
                                                    DeviceStatusResponse deviceStatusResponse = response.body();
                                                    saveInfo(response.body().getToken(), deviceStatusResponse.getDevice_id(), deviceStatusResponse.getExpiry_date(), deviceStatusResponse.getDealer_pin());
                                                    utils.unSuspendDevice(LinkStatusJobScheduler.this);
                                                    PrefUtils.saveBooleanPref(LinkStatusJobScheduler.this, DEVICE_LINKED_STATUS, true);
                                                    finishedRefreshing();
                                                    approvedLinkViewState();
                                                    break;
                                                case EXPIRED:
                                                    saveInfo(response.body().getToken(), response.body().getDevice_id(), response.body().getExpiry_date(), response.body().getDealer_pin());
                                                    utils.suspendedDevice(LinkStatusJobScheduler.this, "expired");
                                                    PrefUtils.saveBooleanPref(LinkStatusJobScheduler.this, DEVICE_LINKED_STATUS, true);
                                                    finish();
                                                    break;
                                                case SUSPENDED:
                                                    saveInfo(response.body().getToken(), response.body().getDevice_id(), response.body().getExpiry_date(), response.body().getDealer_pin());
                                                    utils.suspendedDevice(LinkStatusJobScheduler.this, "suspended");
                                                    PrefUtils.saveBooleanPref(LinkStatusJobScheduler.this, DEVICE_LINKED_STATUS, true);

                                                    finish();
                                                    break;
                                                case TRIAL:
                                                    saveInfo(response.body().getToken(), response.body().getDevice_id(), response.body().getExpiry_date(), response.body().getDealer_pin());
                                                    utils.unSuspendDevice(LinkStatusJobScheduler.this);
                                                    PrefUtils.saveBooleanPref(LinkStatusJobScheduler.this, DEVICE_LINKED_STATUS, true);
                                                    finish();
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
                                                        utils.unlinkDevice(LinkStatusJobScheduler.this, false);
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

                                }
                            });
                }
            });


        }*/
        return true ;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
    private void saveInfo(String token, String device_id, String expiry_date, String dealer_pin) {
        PrefUtils.saveStringPref(LinkStatusJobScheduler.this, TOKEN, token);
        PrefUtils.saveStringPref(LinkStatusJobScheduler.this, VALUE_EXPIRED, expiry_date);
        PrefUtils.saveStringPref(LinkStatusJobScheduler.this, DEVICE_ID, device_id);
        PrefUtils.saveStringPref(LinkStatusJobScheduler.this, KEY_DEVICE_LINKED, dealer_pin);
    }
}
