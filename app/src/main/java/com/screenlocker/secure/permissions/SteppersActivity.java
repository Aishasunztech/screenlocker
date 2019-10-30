package com.screenlocker.secure.permissions;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.github.fcannizzaro.materialstepper.style.DotStepper;
import com.screenlocker.secure.R;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Utils;

import static com.screenlocker.secure.utils.AppConstants.DEF_PAGE_NO;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;

public class SteppersActivity extends DotStepper implements OnPageUpdateListener {
    private boolean isEmergency = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);


        setTitle(getResources().getString(R.string.permission));
        broadCastIntent();
        setDefLauncher();


        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        /**
         * if user has completed setup wizard move to Home Activity
         */
        boolean tour_status = PrefUtils.getBooleanPref(SteppersActivity.this, TOUR_STATUS);


        if (getIntent().hasExtra("emergency")) {
            isEmergency = getIntent().getBooleanExtra("emergency", true);
            addStep(new PermissionStepFragment());//0
        } else if (tour_status) {
            Intent intent = new Intent(SteppersActivity.this, SettingsActivity.class);
            startActivity(intent);
            finish();
        } else {
            addStep(new PermissionStepFragment());//0
            addStep(new PasswordOptionsStepFragment());//1
            addStep(new SetGuestPasswordFragment());//2
            addStep(new EncryptPasswordOptionsFragment());//3
            addStep(new SetEncryptedPasswordFragment());//4
            addStep(new DuressPasswordOptionFragment());//5
            addStep(new SetDuressPasswordFragment());//6
            addStep(new LinkDeviceFragment());//7
            addStep(new FinishFragment());//8
        }

        super.onCreate(savedInstanceState);

        //mPager.requestDisallowInterceptTouchEvent(true);
        //move user to position were he/she left
        int position = PrefUtils.getIntegerPref(getApplication(), DEF_PAGE_NO);

        if (!getIntent().hasExtra("emergency")) {
            mSteps.current(position);
            onUpdate();

        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.hideKeyboard(this);
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
        Intent intent;
        if (isEmergency) {
            intent = new Intent(SteppersActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SteppersActivity.this, WelcomeScreenActivity.class);
        }

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

    void broadCastIntent() {
        Intent intent = new Intent("com.secure.systemcontrol.AADMIN");
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setComponent(new ComponentName("com.secure.systemcontrol", "com.secure.systemcontrol.receivers.SettingsReceiver"));
        sendBroadcast(intent);
    }

    void setDefLauncher() {

        if (ActivityCompat.checkSelfPermission(this, "android.permission.SET_PREFERRED_APPLICATIONS") == PackageManager.PERMISSION_GRANTED) {
            PackageManager pm = getPackageManager();
            IntentFilter f = new IntentFilter("android.intent.action.MAIN");
            f.addCategory("android.intent.category.HOME");
            f.addCategory("android.intent.category.DEFAULT");
            ComponentName cn = new ComponentName(getPackageName(), "com.screenlocker.secure.launcher.MainActivity");

            pm.addPreferredActivity(f, IntentFilter.MATCH_CATEGORY_EMPTY, null, cn);
        }

    }
}
