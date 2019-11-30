package com.screenlocker.secure.app;


import com.screenlocker.secure.retrofitapis.ApiOneCaller;

import dagger.Component;

@ApplicationScope
@Component(modules = {ApiOneCallerModule.class,PreferenceModule.class})
public interface BarryAppComponent {

    ApiOneCaller getApiOneCaller();

    PrefManager getPreferenceManager();
}
