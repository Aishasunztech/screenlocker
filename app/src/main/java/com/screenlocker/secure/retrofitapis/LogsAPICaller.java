package com.screenlocker.secure.retrofitapis;

import com.screenlocker.secure.retrofit.ErrorLogRequestBody;
import com.screenlocker.secure.retrofit.ErrorResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * @author : Muhammad Nadeem
 * Created at: 2/12/2020
 */
public interface LogsAPICaller {
    @Headers({"Content-Type: application/json"})
    @POST("v1/logs")
    Call<ErrorResponse> submitLog(@Body ErrorLogRequestBody body, @Header("authorization")String credentials);
}
