package com.screenlocker.secure.retrofitapis;


import com.screenlocker.secure.mdm.base.DeviceExpiryResponse;
import com.screenlocker.secure.mdm.retrofitmodels.DeleteDeviceResponse;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceModel;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceStatusResponse;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceLoginModle;
import com.screenlocker.secure.mdm.retrofitmodels.LinkDeviceModel;
import com.screenlocker.secure.mdm.retrofitmodels.LinkDeviceResponse;
import com.screenlocker.secure.networkResponseModels.DeviceLoginResponse;
import com.screenlocker.secure.networkResponseModels.LoginModel;
import com.screenlocker.secure.networkResponseModels.LoginResponse;
import com.screenlocker.secure.networkResponseModels.NetworkResponse;
import com.screenlocker.secure.retrofit.ErrorLogRequestBody;
import com.screenlocker.secure.retrofit.ErrorResponse;
import com.screenlocker.secure.settings.codeSetting.installApps.InstallAppModel;
import com.screenlocker.secure.settings.codeSetting.installApps.UpdateModel;
import com.secureMarket.DeviceAndSpace;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface ApiOneCaller {

    @Headers("Content-Type: application/json")
    @POST("v1/mobile/check_expiry")
    Call<DeviceExpiryResponse> getOfflineExpiry(
            @Body DeviceModel deviceModel
    );


    @FormUrlEncoded
    @POST("v2/mobile/getstatus")
    Call<NetworkResponse> checkStatus(@Field("imei") String imeiNumber, @Field("mac") String macAddress);

    @GET("apklist")
    Call<InstallAppModel> getApps();


    @GET
    Call<UpdateModel> getUpdate(@Url String url, @Header("authorization") String authHeader);

    @Headers("Content-Type: application/json")
    @POST("v2/mobile/login")
    Call<DeviceLoginResponse> deviceLogin(
            @Body DeviceLoginModle deviceLoginModle
    );

    @Headers("Content-Type: application/json")
    @POST("v2/mobile/linkdevice")
    Call<LinkDeviceResponse> linkDeviceToDealer(
            @Body LinkDeviceModel dealerLoginModel,
            @Header("authorization") String token
    );


    @Headers("Content-Type: application/json")
    @DELETE("v2/mobile/unlink/{macAddr}/{serialNo}")
    Call<DeleteDeviceResponse> stopLinkingDevice(
            @Path("macAddr") String MAC,
            @Path("serialNo") String SerialNo,
            @Header("authorization") String token
    );

    @Headers("Content-Type: application/json")
    @POST("v2/mobile/device_status")
    Call<DeviceStatusResponse> checkDeviceStatus(
            @Body DeviceModel deviceModel
    );

    @GET("v2/mobile/marketApplist/admin/{space}")
    Call<InstallAppModel> getAdminApps(@Path("space") String space);


    @POST("v2/mobile/market-app-list")
    Call<InstallAppModel> getAllApps(@Body DeviceAndSpace deviceAndSpace);

    @Headers("Content-Type: application/json")
    @POST("v2/mobile/systemlogin")
    Call<LoginResponse> login(
            @Body LoginModel loginModel
    );

    @Headers("Content-Type: aplication/json")
    @POST("api/v1/logs")
    Call<ErrorResponse> submitLog(@Body ErrorLogRequestBody body);
}
