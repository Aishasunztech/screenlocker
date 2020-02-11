package com.screenlocker.secure.retrofit;

import android.content.Intent;
import android.os.Build;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.room.MyAppDatabase;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.utils.PrefUtils;
import com.secure.launcher.BuildConfig;
import com.secure.launcher.R;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.URL_1;
import static com.screenlocker.secure.utils.AppConstants.URL_2;


public class RetrofitClientInstance {

    private static Retrofit retrofit;
    private static Retrofit retrofit1;


    public static Retrofit getRetrofitSecondInstance(String url) {
        if (retrofit1 != null) {
            retrofit1 = null;
        }
        retrofit1 = new retrofit2.Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(url)
                .build();
        return retrofit1;
    }


    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Request request = chain.request();
                Response response = chain.proceed(request);

                // todo deal with the issues the way you need to
                if (!(response.code() >= 200 && response.code() < 300)) {
                    ErrorLogRequestBody log = new ErrorLogRequestBody();
                    log.setCode(response.code());
                    log.setApiResponseTime(response.receivedResponseAtMillis() - response.sentRequestAtMillis());
                    log.setMessage(response.message());
                    log.setRequestBody(request.body());
                    log.setResponseBody(response.body());
                    log.setRequestHeaders(request.headers());
                    log.setResponseHeaders(response.headers());
                    log.setRequestUrl(request.url().toString());
                    log.setInfo(new ErrorLogRequestBody.DeviceInfo(
                            PrefUtils.getStringPref(MyApplication.getAppContext(), DEVICE_ID),
                            BuildConfig.VERSION_NAME,
                            MyApplication.getAppContext().getResources().getString(R.string.app_name),
                            MyApplication.getAppContext().getResources().getString(R.string.apktype),
                            Build.DISPLAY

                    ));
                    AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
                        MyAppDatabase.getInstance(MyApplication.getAppContext()).getDao().insertError(log);
                    });

                    if (MyApplication.oneCaller == null) {
                        String[] urls = {URL_1, URL_2};
                        new AsyncCalls(output -> {
                            if (output == null) {
                                //showError(getResources().getString(R.string.server_error));
                            } else {
                                PrefUtils.saveStringPref(MyApplication.getAppContext(), LIVE_URL, output);
                                String live_url = PrefUtils.getStringPref(MyApplication.getAppContext(), LIVE_URL);
                                Timber.d("live_url %s", live_url);
                                MyApplication.oneCaller = RetrofitClientInstance.getRetrofitInstance(live_url + MOBILE_END_POINT).create(ApiOneCaller.class);
                                submitLogs(log);

                            }
                        }, MyApplication.getAppContext(), urls);

                    } else {
                        submitLogs(log);
                    }
                    MyApplication.getAppContext().startActivity(new Intent(MyApplication.getAppContext(), GlobleErrorLogActivity.class));
                    return response;
                }

                return response;
            })
            .build();

    private static void submitLogs(ErrorLogRequestBody log) {
        MyApplication.oneCaller.submitLog(log).enqueue(new Callback<ErrorResponse>() {
            @Override
            public void onResponse(@NotNull Call<ErrorResponse> call, @NotNull retrofit2.Response<ErrorResponse> response) {
                if (response.isSuccessful()) {
                    AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
                        if (response.body() != null)
                            MyAppDatabase.getInstance(MyApplication.getAppContext()).getDao().deleteErrorLog(response.body().getRequestId());
                    });

                }

            }


            @Override
            public void onFailure(@NotNull Call<ErrorResponse> call, @NotNull Throwable t) {
                if (t instanceof IOException) {
                    //TODO: save into data base
                }

            }
        });
    }

    public static Retrofit getRetrofitInstance(String url) {
        if (retrofit != null) {
            retrofit = null;
        }
        retrofit = new retrofit2.Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(url)
                .client(okHttpClient)
                .build();
        return retrofit;
    }

}

