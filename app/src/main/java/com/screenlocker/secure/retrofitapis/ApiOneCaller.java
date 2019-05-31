package com.screenlocker.secure.retrofitapis;


import com.screenlocker.secure.mdm.retrofitmodels.DeleteDeviceResponse;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceStatusModel;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceStatusResponse;
import com.screenlocker.secure.mdm.retrofitmodels.DeviceLoginModle;
import com.screenlocker.secure.mdm.retrofitmodels.LinkDeviceModel;
import com.screenlocker.secure.mdm.retrofitmodels.LinkDeviceResponse;
import com.screenlocker.secure.mdm.retrofitmodels.LinkStatusModel;
import com.screenlocker.secure.mdm.retrofitmodels.LinkStatusResponse;
import com.screenlocker.secure.networkResponseModels.DeviceLoginResponse;
import com.screenlocker.secure.networkResponseModels.LoginModel;
import com.screenlocker.secure.networkResponseModels.LoginResponse;
import com.screenlocker.secure.networkResponseModels.NetworkResponse;
import com.screenlocker.secure.settings.codeSetting.installApps.InstallAppModel;
import com.screenlocker.secure.settings.codeSetting.installApps.UpdateModel;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface ApiOneCaller {

    //   @Headers( "Content-Type: application/json" )
    @POST("adddevice")
    Call<NetworkResponse> addExpiryDate(
            @FieldMap Map<String, String> params
    );

    @FormUrlEncoded
    @POST("getinfo")
    Call<NetworkResponse> getExpiryDate(
            @Field("imei") String params
    );

    @Headers("Content-Type: application/json")
    @POST("login")
    Call<DeviceLoginResponse> getDeviceId(
            @Body DeviceLoginModle deviceLoginModle
    );

    @FormUrlEncoded
    @POST("checkdev")
    Call<NetworkResponse> checkDevice(
            @Field("imei") String params
    );

    @FormUrlEncoded
    @POST("addinfo")
    Call<NetworkResponse> setExpiryDate(
            @FieldMap Map<String, String> params
    );


    @FormUrlEncoded
    @POST("getstatus")
    Call<NetworkResponse> checkStatus(@Field("imei") String imeiNumber, @Field("mac") String macAddress);

    @FormUrlEncoded
    @POST("adddev")
    Call<NetworkResponse> addDevice(
            @FieldMap Map<String, String> params
    );

    @GET("apklist")
    Call<InstallAppModel> getApps();


    @GET
    Call<UpdateModel> getUpdate(@Url String url, @Header("authorization") String authHeader);

    @Streaming
    @GET
    Call<ResponseBody> downloadApp(@Url String url);

    Call<RequestBody> getDownloadUrl();

    @Headers("Content-Type: application/json")
    @POST("login")
    Call<DeviceLoginResponse> deviceLogin(
            @Body DeviceLoginModle deviceLoginModle
    );

    @Headers("Content-Type: application/json")
    @POST("linkdevice")
    Call<LinkDeviceResponse> linkDeviceToDealer(
            @Body LinkDeviceModel dealerLoginModel,
            @Header("authorization") String token
    );

    @Headers("Content-Type: application/json")
    @POST("getstatus")
    Call<LinkStatusResponse> linkDeviceStatus(
            @Body LinkStatusModel dealerLoginModel,
            @Header("authorization") String token
    );

    @Headers("Content-Type: application/json")
    @DELETE("unlink/{macAddr}/{serialNo}")
    Call<DeleteDeviceResponse> stopLinkingDevice(
            @Path("macAddr") String MAC,
            @Path("serialNo") String SerialNo,
            @Header("authorization") String token
    );
//
//    @Headers("Content-Type: application/json")
//    @POST("accountstatus")
//    Call<DeviceStatusResponse> checkDeviceStatus(
//            @Body DeviceStatusModel checkStatusModel
//    );


    @Headers("Content-Type: application/json")
    @POST("device_status")
    Call<DeviceStatusResponse> checkDeviceStatus(
            @Body DeviceStatusModel deviceStatusModel
    );


    @GET("admin/marketApplist")
    Call<InstallAppModel> getAdminApps();

    @GET
    Call<InstallAppModel> getAllApps(@Url String url);

    @Headers("Content-Type: application/json")
    @POST("systemlogin")
    Call<LoginResponse> login(
            @Body LoginModel loginModel
    );

}
