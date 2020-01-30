package com.screenlocker.secure.permissions;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.github.fcannizzaro.materialstepper.style.DotStepper;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.service.apps.RefreshTimerTask;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Utils;
import com.secure.launcher.R;

import static com.screenlocker.secure.utils.AppConstants.DEF_PAGE_NO;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;

public class SteppersActivity extends DotStepper implements OnPageUpdateListener {
    private boolean isEmergency = false;
    public static final int STEP_LANGUAGE = 1;
    public static final int STEP_PERMISSION = 2;
    public static final int STEP_SETUP_GUEST = 3;
    public static final int STEP_GUEST_PASS = 4;
    public static final int STEP_SETUP_ENCRYPT = 5;
    public static final int STEP_ENCRYPT_PASS = 6;
    public static final int STEP_SETUP_WIPE = 7;
    public static final int STEP_WIPE_PASS = 8;
    public static final int STEP_DEFAULT_LAUNCHER = 9;
    public static final int STEP_LINK = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

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
        } else if (getIntent().hasExtra("emergencyLauncher")) {
            addStep(new SetDefaultLauncherFragment());
        } else if (tour_status) {
            Intent intent = new Intent(SteppersActivity.this, SettingsActivity.class);
            startActivity(intent);
            finish();
        } else {
            addStep(new LaungaugeSelectionStepFragment());//0
            addStep(new PermissionStepFragment());//01
            addStep(new PasswordOptionsStepFragment());//2
            addStep(new SetGuestPasswordFragment());//3
            addStep(new EncryptPasswordOptionsFragment());//4
            addStep(new SetEncryptedPasswordFragment());//5
            addStep(new DuressPasswordOptionFragment());//6
            addStep(new SetDuressPasswordFragment());//7
            addStep(new SetDefaultLauncherFragment());//8
            addStep(new LinkDeviceFragment());//9
            addStep(new FinishFragment());//10
        }


        super.onCreate(savedInstanceState);

        //mPager.requestDisallowInterceptTouchEvent(true);
        //move user to position were he/she left
        int position = PrefUtils.getIntegerPref(getApplication(), DEF_PAGE_NO);
        if (getIntent().hasExtra("emergencyLauncher")){
            isEmergency = true;

//            mSteps.current(7);
//            onUpdate();
        }else
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

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }
}
