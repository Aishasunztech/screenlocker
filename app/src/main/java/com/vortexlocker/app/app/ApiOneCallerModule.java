package com.vortexlocker.app.app;

import com.fatboyindustrial.gsonjodatime.DateTimeConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vortexlocker.app.retrofitapis.ApiOneCaller;
import com.vortexlocker.app.utils.AppConstants;

import org.joda.time.DateTime;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module(includes = {NetworkModule.class})
public class ApiOneCallerModule {

    @Provides
    @ApplicationScope
    public ApiOneCaller getApiOneCaller(Retrofit retrofit) {
        return retrofit.create(ApiOneCaller.class);
    }

    @Provides
    @ApplicationScope
    public Retrofit retrofit(OkHttpClient okHttpClient, Gson gson) {

        return new Retrofit
                .Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .baseUrl(AppConstants.STAGING_BASE_URL)
                .build();
    }

    @Provides
    @ApplicationScope
    public Gson gson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DateTime.class, new DateTimeConverter());
        return gsonBuilder.create();
    }


}
