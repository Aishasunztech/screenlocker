package com.screenlocker.secure.permissions;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.github.fcannizzaro.materialstepper.style.DotStepper;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.utils.PrefUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.screenlocker.secure.utils.AppConstants.CURRENT_STEP;
import static com.screenlocker.secure.utils.AppConstants.DEF_PAGE_NO;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;

public class SteppersActivity extends DotStepper implements LinkDeviceFragment.PageUpdate {
    private int i = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setTitle("Permissions");

        /**
         * if user has completed setup wizard move to Home Activity
         */
        boolean tour_status = PrefUtils.getBooleanPref(SteppersActivity.this, TOUR_STATUS);

        if (tour_status) {
            Intent intent = new Intent(SteppersActivity.this, SettingsActivity.class);
            startActivity(intent);
            finish();
        }


        addStep(new PermissionStepFragment());//0
        addStep(new SetGuestPasswordFragment());//1
        addStep(new SetEncryptedPasswordFragment());//2
        addStep(new SetDuressPasswordFragment());//3
//        addStep(new ConnectivityFragment());//4
        addStep(new LinkDeviceFragment());//4
        addStep(new SetDefaultLauncherFragment());//5
        addStep(new FinishFragment());//6


        super.onCreate(savedInstanceState);
        //move user to position were he/she left
        int position = PrefUtils.getIntegerPref(getApplication(), DEF_PAGE_NO);


        mSteps.current(position);

        onUpdate();


    }

    /**
     * User has complected all steps and ready to move welcome screen
     *
     * @param data bundle sent by last fragment
     */
    @Override
    public void onComplete(Bundle data) {
        super.onComplete(data);
        //save the tour complete status in database
        PrefUtils.saveBooleanPref(SteppersActivity.this, TOUR_STATUS, true);
        Intent intent = new Intent(SteppersActivity.this, WelcomeScreenActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPageUpdate(int pageNo) {
        mSteps.current(pageNo);
        onUpdate();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
