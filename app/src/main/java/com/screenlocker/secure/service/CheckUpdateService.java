package com.screenlocker.secure.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.DownLoadAndInstallUpdate;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.settings.codeSetting.installApps.UpdateModel;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import java.io.IOException;
import java.net.UnknownHostException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.app.MyApplication.saveToken;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.SYSTEM_LOGIN_TOKEN;

public class CheckUpdateService extends JobService {

    boolean isFailSafe = false;

    @Override
    public boolean onStartJob(JobParameters params) {

        checkForDownload(params, RetrofitClientInstance.getWhiteLabelInstance());

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        if (obj != null) {
            obj.cancel(true);
        }
        return true;
    }


    private DownLoadAndInstallUpdate obj;

    private void checkForDownload(JobParameters params, ApiOneCaller apiOneCaller) {

        String currentVersion = "1";
        try {
            currentVersion = String.valueOf(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.d(e);
        }

        apiOneCaller
                .getUpdate("getUpdate/" + currentVersion + "/" + getPackageName() + "/" + getString(R.string.label), PrefUtils.getStringPref(this, SYSTEM_LOGIN_TOKEN))
                .enqueue(new Callback<UpdateModel>() {
                    @Override
                    public void onResponse(@NonNull Call<UpdateModel> call, @NonNull Response<UpdateModel> response) {

                        if (response.body() != null) {
                            if (response.body().isSuccess()) {
                                if (response.body().isApkStatus()) {
                                    String url = response.body().getApkUrl();
                                    String live_url = PrefUtils.getStringPref(MyApplication.getAppContext(), LIVE_URL);
                                    obj = new DownLoadAndInstallUpdate(CheckUpdateService.this, live_url + "getApk/" + CommonUtils.splitName(url), true, params);
                                    obj.execute();

                                }  //                                            Toast.makeText(appContext, getString(R.string.uptodate), Toast.LENGTH_SHORT).show();


                            } else {
                                saveToken(RetrofitClientInstance.getWhiteLabelInstance());
                                checkForDownload(params, apiOneCaller);
                            }

                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UpdateModel> call, @NonNull Throwable t) {
                        if (t instanceof UnknownHostException) {
                            Timber.e("-----------> something very dangerous happen with domain : %s", t.getMessage());

                            if (isFailSafe) {
                                Timber.e("------------> FailSafe domain is also not working. ");
                            } else {
                                Timber.i("<<< New Api call with failsafe domain >>>");
                                checkForDownload(params, RetrofitClientInstance.getFailSafeInstanceForWhiteLabel());
                                isFailSafe = true;
                            }

                        } else if (t instanceof IOException) {
                            Timber.e(" ----> IO Exception :%s", t.getMessage());
                        }
                    }
                });


    }
}




