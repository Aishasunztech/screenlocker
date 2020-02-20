package com.screenlocker.secure.permissions;

import android.os.Bundle;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.utils.PrefUtils;

/**
 * @author : Muhammad Nadeem
 * Created at: 2/20/2020
 */
public abstract class ExtentedAbstractStep extends AbstractStep {
    protected PrefUtils prefUtils;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefUtils = PrefUtils.getInstance(MyApplication.getAppContext());
    }
}
