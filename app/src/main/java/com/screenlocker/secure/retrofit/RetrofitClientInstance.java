package com.screenlocker.secure.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class RetrofitClientInstance {

    private static Retrofit retrofit;
    private static Retrofit retrofit1;

    public static Retrofit getRetrofitInstance(String url) {
        if (retrofit != null) {
            retrofit = null;
        }
        retrofit = new retrofit2.Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(url)
                .build();
        return retrofit;
    }

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

}

