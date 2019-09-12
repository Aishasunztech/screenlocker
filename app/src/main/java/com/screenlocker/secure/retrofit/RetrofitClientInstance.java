package com.screenlocker.secure.retrofit;

import com.screenlocker.secure.retrofitapis.ApiOneCaller;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.screenlocker.secure.utils.AppConstants.FAIL_SAFE_URL_FOR_SUPPER_ADMIN;
import static com.screenlocker.secure.utils.AppConstants.FAIL_SAFE_URL_FOR_WHITE_LABEL;
import static com.screenlocker.secure.utils.AppConstants.SUPER_ADMIN;
import static com.screenlocker.secure.utils.AppConstants.SUPER_ADMIN_URL;
import static com.screenlocker.secure.utils.AppConstants.URL_1;
import static com.screenlocker.secure.utils.AppConstants.URL_2;
import static com.screenlocker.secure.utils.AppConstants.WHITE_LABEL_URL;


public class RetrofitClientInstance {

    private static Retrofit retrofit;
    private static Retrofit retrofit1;
    private static Retrofit retrofit2;

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

    public static Retrofit getRetrofitThirdInstance(String url) {
        if (retrofit2 != null) {
            retrofit2 = null;
        }
        retrofit2 = new retrofit2.Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(url)
                .build();
        return retrofit2;
    }

    // instance for white labels e.g LM/TL etc
    private static ApiOneCaller whiteLabelInstance;

    public static ApiOneCaller getWhiteLabelInstance() {
        if (whiteLabelInstance == null)
            whiteLabelInstance = new retrofit2.Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(WHITE_LABEL_URL)
                    .build().create(ApiOneCaller.class);
        return whiteLabelInstance;
    }


    // instance for SA panel
    private static Retrofit superAdminPanelInstance;

    public static Retrofit getSuperAdminPanelInstance() {
        if (superAdminPanelInstance == null)
            superAdminPanelInstance = new retrofit2.Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(SUPER_ADMIN_URL)
                    .build();
        return superAdminPanelInstance;
    }


    //  failSafe instance for supper admin
    private static Retrofit failSafeInstanceForSupperAdmin;

    public static Retrofit getFailSafeInstanceForSupperAdmin() {
        if (failSafeInstanceForSupperAdmin == null)
            failSafeInstanceForSupperAdmin = new retrofit2.Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(FAIL_SAFE_URL_FOR_SUPPER_ADMIN)
                    .build();
        return failSafeInstanceForSupperAdmin;
    }


    //  failSafe instance for white label
    private static ApiOneCaller failSafeInstanceForWhiteLabel;

    public static ApiOneCaller getFailSafeInstanceForWhiteLabel() {
        if (failSafeInstanceForWhiteLabel == null)
            failSafeInstanceForWhiteLabel = new retrofit2.Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(FAIL_SAFE_URL_FOR_WHITE_LABEL)
                    .build().create(ApiOneCaller.class);
        return failSafeInstanceForWhiteLabel;
    }


}

