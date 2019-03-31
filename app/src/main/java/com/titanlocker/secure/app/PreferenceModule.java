package com.titanlocker.secure.app;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class PreferenceModule {

    @Provides
    @ApplicationScope
    public PrefManager prefManager(Context context){
        return new PrefManager(context);
    }

}
