package com.screenlocker.secure.retrofit;

import android.content.Context;


import com.screenlocker.secure.utils.PrefUtils;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;


public class RetrofitClientInstance {

    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance(String url, Context context) {


        if (!PrefUtils.getStringPref(context, LIVE_URL).equals(url)) {
            retrofit = null;
            retrofit = new retrofit2.Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(url)
                    .build();
        } else if (retrofit == null) {
            retrofit = new retrofit2.Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(url)
                    .build();
        }

        return retrofit;
    }

}

