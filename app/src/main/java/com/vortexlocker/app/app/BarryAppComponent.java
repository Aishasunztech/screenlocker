package com.vortexlocker.app.app;


import com.vortexlocker.app.retrofitapis.ApiOneCaller;

import dagger.Component;

@ApplicationScope
@Component(modules = {ApiOneCallerModule.class,PreferenceModule.class})
public interface BarryAppComponent {

    ApiOneCaller getApiOneCaller();

    PrefManager getPreferenceManager();
}
