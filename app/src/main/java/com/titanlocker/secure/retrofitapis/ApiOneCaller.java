package com.titanlocker.secure.retrofitapis;


import com.titanlocker.secure.mdm.retrofitmodels.CheckStatusModel;
import com.titanlocker.secure.mdm.retrofitmodels.CheckStatusResponse;
import com.titanlocker.secure.mdm.retrofitmodels.DealerLoginModel;
import com.titanlocker.secure.mdm.retrofitmodels.LinkDeviceModel;
import com.titanlocker.secure.mdm.retrofitmodels.LinkDeviceResponse;
import com.titanlocker.secure.mdm.retrofitmodels.LinkStatusModel;
import com.titanlocker.secure.mdm.retrofitmodels.LinkStatusResponse;
import com.titanlocker.secure.networkResponseModels.DealerLoginResponse;
import com.titanlocker.secure.networkResponseModels.NetworkResponse;
import com.titanlocker.secure.settings.codeSetting.installApps.InstallAppModel;

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

    @FormUrlEncoded
    @POST("login")
    Call<DealerLoginResponse> getDeviceId(
            @Field("mac_address") String params
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

    @Streaming
    @GET
    Call<ResponseBody> downloadApp(@Url String url);

    Call<RequestBody> getDownloadUrl();

    @Headers("Content-Type: application/json")
    @POST("login")
    Call<com.titanlocker.secure.mdm.retrofitmodels.DealerLoginResponse> dealerLogin(
            @Body DealerLoginModel dealerLoginModel
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
    @DELETE("unlink/{imei_to_unlink}")
    Call<LinkDeviceResponse> stopLinkingDevice(
            @Path("imei_to_unlink") String IMEI,
            @Header("authorization") String token
    );

    @Headers("Content-Type: application/json")
    @POST("accountstatus")
    Call<CheckStatusResponse> checkDeviceStatus(
            @Body CheckStatusModel checkStatusModel
    );
}
