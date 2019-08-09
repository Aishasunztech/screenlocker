package com.screenlocker.secure.offline;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.screenlocker.secure.R;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.mdm.base.DeviceExpiryResponse;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceModel;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.utils.PrefUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.ACTIVE;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DUPLICATE_MAC;
import static com.screenlocker.secure.utils.AppConstants.DUPLICATE_MAC_AND_SERIAL;
import static com.screenlocker.secure.utils.AppConstants.DUPLICATE_SERIAL;
import static com.screenlocker.secure.utils.AppConstants.EXPIRED;
import static com.screenlocker.secure.utils.AppConstants.OFFLINE_DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.SUPER_ADMIN;
import static com.screenlocker.secure.utils.AppConstants.SUPER_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.SUSPENDED;
import static com.screenlocker.secure.utils.AppConstants.URL_2;

/**
 * @author Muhammad Nadeem
 * @Date 6/17/2019.
 */
public class CheckExpiryFromSuperAdmin extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {

        if (!PrefUtils.getBooleanPref(this, DEVICE_LINKED_STATUS)) {
            checkOfflineExpiry(CheckExpiryFromSuperAdmin.this);
        }

        return false;
    }

    private AsyncCalls asyncCalls;

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private void checkOfflineExpiry(Context context) {

        Timber.d("Checking offline Expiry");

        String[] urls = {SUPER_ADMIN, URL_2};

        if (asyncCalls != null) {
            asyncCalls.cancel(true);
        }
        asyncCalls = new AsyncCalls(output -> {
            if (output != null) {

                String url = output + SUPER_END_POINT;

                ApiOneCaller service = RetrofitClientInstance.getRetrofitSecondInstance(url).create(ApiOneCaller.class);
                service.getOfflineExpiry(new DeviceModel(DeviceIdUtils.getSerialNumber(), DeviceIdUtils.getIPAddress(true), getPackageName() + getString(R.string.app_name), DeviceIdUtils.generateUniqueDeviceId(context))).enqueue(new Callback<DeviceExpiryResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DeviceExpiryResponse> call, @NonNull Response<DeviceExpiryResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {

                            DeviceExpiryResponse deviceExpiryResponse = response.body();
                            Timber.d("DEVICE_STATUS %s", deviceExpiryResponse.getDeviceStatus());
                            if (deviceExpiryResponse.isStatus()) {

                                /*
                                 * Offline device status
                                 * */
                                String of_device_status = deviceExpiryResponse.getDeviceStatus();
                                /*
                                 * Offline device ID for dealers to assist
                                 * */
                                String of_device_id = deviceExpiryResponse.getOfDeviceId();
                                PrefUtils.saveStringPref(context, OFFLINE_DEVICE_ID, of_device_id);
                                if (of_device_status != null) {

                                    switch (of_device_status) {
                                        case ACTIVE:
                                            /*
                                             * Activate device
                                             * */
                                            utils.unSuspendDevice(context);
                                            break;
                                        case EXPIRED:
                                            /*
                                             * Expire device
                                             * */
                                            utils.suspendedDevice(context, "expired");
                                            break;
                                        case SUSPENDED:
                                            /*
                                             * Suspend device
                                             * */
                                            utils.suspendedDevice(context, "suspended");
                                            break;
                                    }
                                }

                            } else {
                                String msg = response.body().getMsg();
                                switch (msg) {
                                    case DUPLICATE_MAC:
                                        utils.suspendedDevice(context, "suspended");
                                        break;
                                    case DUPLICATE_SERIAL:
                                        utils.suspendedDevice(context, "suspended");
                                        break;
                                    case DUPLICATE_MAC_AND_SERIAL:
                                        utils.suspendedDevice(context, "suspended");
                                        break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DeviceExpiryResponse> call, @NonNull Throwable t) {
                        Timber.e(t);
                    }
                });

            }
        }, context, urls);

        asyncCalls.execute();
    }

    private void expire(Context context) {
        PrefUtils.saveStringPref(context, DEVICE_STATUS, "expired");
        Intent intent = new Intent(context, LockScreenService.class);
        intent.setAction("expired");
        ActivityCompat.startForegroundService(context, intent);
    }
}
