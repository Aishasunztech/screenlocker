package com.screenlocker.secure.offline;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.mdm.base.DeviceExpiryResponse;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceModel;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.utils.PrefUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.SUPER_ADMIN;
import static com.screenlocker.secure.utils.AppConstants.SUPER_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.URL_2;

/**
 * @author Muhammad Nadeem
 * @Date 6/17/2019.
 */
public class CheckExpiryFromSuperAdmin extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
    private void checkOfflineExpiry() {

        Timber.d("Checking offline Expiry");

        String[] urls = {SUPER_ADMIN, URL_2};

        new AsyncCalls(output -> {
            if (output != null) {
                String url = output + SUPER_END_POINT;
                ApiOneCaller service = RetrofitClientInstance.getRetrofitSecondInstance(url).create(ApiOneCaller.class);
                service.getOfflineExpiry(new DeviceModel(DeviceIdUtils.getSerialNumber(), DeviceIdUtils.getMacAddress())).enqueue(new Callback<DeviceExpiryResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<DeviceExpiryResponse> call, @NonNull Response<DeviceExpiryResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {

                            DeviceExpiryResponse deviceExpiryResponse = response.body();
                            Timber.d("EspiresIn : %s", deviceExpiryResponse.getExpiresIn());
                            Timber.d("StartDate : %s", deviceExpiryResponse.getStartDate());
                            Timber.d("EndDate : %s", deviceExpiryResponse.getEndDate());

                            if (deviceExpiryResponse.isStatus()) {

                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<DeviceExpiryResponse> call, @NonNull Throwable t) {
                        Timber.e(t);
                    }
                });

            }
        }, this, urls).execute();
    }
    private void expire(Context context){
        PrefUtils.saveStringPref(context, DEVICE_STATUS, "expired");
        Intent intent =  new Intent(context, LockScreenService.class);
        intent.setAction("expired");
        ActivityCompat.startForegroundService(context, intent);
    }
}
