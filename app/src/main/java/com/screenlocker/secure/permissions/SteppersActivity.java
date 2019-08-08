package com.screenlocker.secure.permissions;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.fcannizzaro.materialstepper.style.DotStepper;
import com.screenlocker.secure.R;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import static android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_DEVICE;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME;
import static com.screenlocker.secure.utils.AppConstants.DEF_PAGE_NO;
import static com.screenlocker.secure.utils.AppConstants.IS_EMERGANCY;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;

public class SteppersActivity extends DotStepper implements OnPageUpdateListener.PageUpdate {
    private boolean isEmergency = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setTitle(getResources().getString(R.string.permission));

        getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        /**
         * if user has completed setup wizard move to Home Activity
         */
        boolean tour_status = PrefUtils.getBooleanPref(SteppersActivity.this, TOUR_STATUS);
        boolean isEmer = PrefUtils.getBooleanPref(SteppersActivity.this, IS_EMERGANCY);


        if (getIntent().hasExtra("emergency" )) {
            isEmergency = getIntent().getBooleanExtra("emergency",true);
            addStep(new PermissionStepFragment());//0
        } else if (tour_status) {
            Intent intent = new Intent(SteppersActivity.this, SettingsActivity.class);
            startActivity(intent);
            finish();
        } else {
           /* if (!PrefUtils.getBooleanPref(this,"cmd2")){
                Intent intent = new Intent(ACTION_PROVISION_MANAGED_DEVICE);
                ComponentName cn = new ComponentName(getPackageName(),"com.screenlocker.secure.MyAdmin");
                intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                        cn);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, 1);
                    PrefUtils.saveBooleanPref(this,"cmd2",true);
                    //activity.finish();
                } else {
                    Toast.makeText(this, "Stopping.",Toast.LENGTH_SHORT).show();
                }
            }*/
            addStep(new PermissionStepFragment());//0
            addStep(new SetGuestPasswordFragment());//1
            addStep(new SetEncryptedPasswordFragment());//2
            addStep(new SetDuressPasswordFragment());//3
            addStep(new LinkDeviceFragment());//4
            addStep(new SetDefaultLauncherFragment());//5
            addStep(new FinishFragment());//6
        }

        super.onCreate(savedInstanceState);
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
        if (isEmergency){
            intent = new Intent(SteppersActivity.this, MainActivity.class);
        }else{
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

    @Override
    protected void onResume() {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        super.onResume();
    }
}
