package com.titanlocker.secure.app;


import com.titanlocker.secure.retrofitapis.ApiOneCaller;

import dagger.Component;

@ApplicationScope
@Component(modules = {ApiOneCallerModule.class,PreferenceModule.class})
public interface BarryAppComponent {

    ApiOneCaller getApiOneCaller();

    PrefManager getPreferenceManager();
}
