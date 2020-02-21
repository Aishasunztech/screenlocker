package com.screenlocker.secure.retrofit;

import android.os.Build;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.retrofitapis.LogsAPICaller;
import com.screenlocker.secure.room.MyAppDatabase;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.utils.PrefUtils;
import com.secure.launcher.BuildConfig;
import com.secure.launcher.R;

import org.jetbrains.annotations.NotNull;

import okhttp3.Credentials;
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
import static com.screenlocker.secure.utils.AppConstants.URL_3;


public class RetrofitClientInstance {

    private static Retrofit retrofit;
    private static Retrofit retrofit1;
    private static Retrofit retrofit2;


    public static Retrofit getRetrofitSecondInstance(String url) {
        if (retrofit1 != null) {
            retrofit1 = null;
        }
        retrofit1 = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(url)
                .build();
        return retrofit1;
    }

    public static Retrofit getRetrofitLogsInstance(String url) {
        if (retrofit2 == null) {
            retrofit2 = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(url)
                    .build();
        }

        return retrofit2;
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
                            PrefUtils.getInstance(MyApplication.getAppContext()).getStringPref( DEVICE_ID),
                            BuildConfig.VERSION_NAME,
                            MyApplication.getAppContext().getResources().getString(R.string.app_name),
                            MyApplication.getAppContext().getResources().getString(R.string.apktype),
                            Build.DISPLAY

                    ));
                    AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
                        long id = MyAppDatabase.getInstance(MyApplication.getAppContext()).getDao().insertError(log);
                        log.setRequestId(id);
                        submitLogs(log);
                    });


//                    Intent intent =new Intent(MyApplication.getAppContext(), GlobleErrorLogActivity.class);
//                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
//                    MyApplication.getAppContext().startActivity(intent);
                    return response;
                }

                return response;
            })
            .build();

    private static void submitLogs(ErrorLogRequestBody log) {
        LogsAPICaller caller = RetrofitClientInstance.getRetrofitLogsInstance(URL_3+MOBILE_END_POINT).create(LogsAPICaller.class);
        Timber.d("submitLogs: " + log.getMessage() + "\n" + log.getCode() + "\n" + log.getRequestUrl() + "\n" + log.getApiResponseTime());
        caller.submitLog(log, Credentials.basic("JBtRRpFqVcYFMggnsxpPh", "2qouqd#uk$*UcnQYwQKXoP4TX9vJSD")).
                enqueue(new Callback<ErrorResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<ErrorResponse> call, @NotNull retrofit2.Response<ErrorResponse> response) {
                        if (response.isSuccessful()) {
                            AppExecutor.getInstance().getSingleThreadExecutor().execute(() -> {
                                if (response.body() != null) {
                                    Timber.d("ID:%s", String.valueOf(response.body().getRequestId()));
                                    MyAppDatabase.getInstance(MyApplication.getAppContext()).getDao().deleteErrorLog(response.body().getRequestId());
                                }
                            });

                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<ErrorResponse> call, @NotNull Throwable t) {

                    }
                });
    }

    public static Retrofit getRetrofitInstance(String url) {
        if (retrofit != null) {
            retrofit = null;
        }
        retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(url)
                .client(okHttpClient)
                .build();
        return retrofit;
    }

}

