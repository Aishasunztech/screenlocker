package com.screenlocker.secure.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.async.DownLoadAndInstallUpdate;
import com.screenlocker.secure.retrofit.ErrorLogRequestBody;
import com.screenlocker.secure.retrofit.ErrorResponse;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.retrofitapis.LogsAPICaller;
import com.screenlocker.secure.room.MyAppDatabase;
import com.screenlocker.secure.settings.codeSetting.installApps.UpdateModel;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;
import com.secure.launcher.R;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.app.MyApplication.saveToken;
import static com.screenlocker.secure.utils.AppConstants.GET_APK_ENDPOINT;
import static com.screenlocker.secure.utils.AppConstants.GET_UPDATE_ENDPOINT;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.SYSTEM_LOGIN_TOKEN;
import static com.screenlocker.secure.utils.AppConstants.URL_1;
import static com.screenlocker.secure.utils.AppConstants.URL_2;
import static com.screenlocker.secure.utils.AppConstants.URL_3;

public class CheckUpdateService extends JobService {

    boolean isFailSafe = false;
    private AsyncCalls asyncCalls;

    @Override
    public boolean onStartJob(JobParameters params) {
        if (MyApplication.oneCaller == null) {

            String[] urls = {URL_1, URL_2};

            if (asyncCalls != null) {
                asyncCalls.cancel(true);
            }

            asyncCalls = new AsyncCalls(output -> {
                Timber.d("output : %s", output);
                if (output != null) {
                    PrefUtils.saveStringPref(this, LIVE_URL, output);
                    String live_url = PrefUtils.getStringPref(this, LIVE_URL);
                    Timber.d("live_url %s", live_url);
                    MyApplication.oneCaller = RetrofitClientInstance.getRetrofitInstance(live_url + MOBILE_END_POINT).create(ApiOneCaller.class);
                    checkForDownload(params);
                }
            }, this, urls);// checking hosts
            asyncCalls.execute();

        } else {
            checkForDownload(params);

        }


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

    private void checkForDownload(JobParameters params) {

        String currentVersion = "1";
        try {
            currentVersion = String.valueOf(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.d(e);
        }

        MyApplication.oneCaller
                .getUpdate(GET_UPDATE_ENDPOINT + currentVersion + "/" + getPackageName() + "/" + getString(R.string.label), PrefUtils.getStringPref(this, SYSTEM_LOGIN_TOKEN))
                .enqueue(new Callback<UpdateModel>() {
                    @Override
                    public void onResponse(@NonNull Call<UpdateModel> call, @NonNull Response<UpdateModel> response) {

                        if (response.body() != null) {
                            if (response.body().isSuccess()) {
                                if (response.body().isApkStatus()) {
                                    String url = response.body().getApkUrl();
                                    String live_url = PrefUtils.getStringPref(MyApplication.getAppContext(), LIVE_URL);
                                    obj = new DownLoadAndInstallUpdate(CheckUpdateService.this, live_url + MOBILE_END_POINT + GET_APK_ENDPOINT + CommonUtils.splitName(url), true, params);
                                    obj.execute();

                                }  //                                            Toast.makeText(appContext, getString(R.string.uptodate), Toast.LENGTH_SHORT).show();


                            } else {
                                saveToken();
                                checkForDownload(params);
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
                                checkForDownload(params);
                                isFailSafe = true;
                            }

                        } else if (t instanceof IOException) {
                            Timber.e(" ----> IO Exception :%s", t.getMessage());
                        }
                    }
                });

        AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
            List<ErrorLogRequestBody> errorLogRequestBodies = MyAppDatabase.getInstance(MyApplication.getAppContext()).getDao().getAllErrorLogs();
            LogsAPICaller caller = RetrofitClientInstance.getRetrofitLogsInstance(URL_3+MOBILE_END_POINT).create(LogsAPICaller.class);
            for (ErrorLogRequestBody errorLogRequestBody : errorLogRequestBodies) {

                try {
                    Response<ErrorResponse> responseResponse = caller.submitLog(errorLogRequestBody, Credentials.basic("JBtRRpFqVcYFMggnsxpPh", "2qouqd#uk$*UcnQYwQKXoP4TX9vJSD")).execute();
                    if (responseResponse.isSuccessful()) {
                        AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
                            if (responseResponse.body() != null) {
                                Timber.d("ID:%s", String.valueOf(responseResponse.body().getRequestId()));
                                MyAppDatabase.getInstance(MyApplication.getAppContext()).getDao().deleteErrorLog(responseResponse.body().getRequestId());
                            }
                        });

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
    }
}




