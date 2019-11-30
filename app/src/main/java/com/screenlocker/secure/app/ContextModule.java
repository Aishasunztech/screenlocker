package com.screenlocker.secure.app;

import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class ContextModule {

    Context context;

    public ContextModule(Context context) {
        this.context = context;
    }

    @Provides
    @ApplicationScope
    Context context(){
        return context;
    }
}
