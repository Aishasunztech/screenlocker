package com.screenlocker.secure.permissions;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.screenlocker.secure.R;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.updateDB.BlurWorker;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

import static android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_DEVICE;
import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME;
import static com.screenlocker.secure.utils.AppConstants.LINKSIM;
import static com.screenlocker.secure.utils.AppConstants.SECUREMARKETSIM;
import static com.screenlocker.secure.utils.AppConstants.SECUREMARKETWIFI;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
import static com.screenlocker.secure.utils.AppConstants.UPDATESIM;
import static com.screenlocker.secure.utils.AppConstants.UPDATEWIFI;

public class WelcomeScreenActivity extends AppCompatActivity {


    ImageView imageView;
    private boolean isLaunched = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        imageView = findViewById(R.id.rotating_image);
        //enable data and roaming
        broadCastIntent();
        PrefUtils.saveBooleanPref(WelcomeScreenActivity.this, TOUR_STATUS, true);
        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_infinite);
        rotation.setFillAfter(true);
        imageView.startAnimation(rotation);


        Handler handler = new Handler();

        handler.postDelayed(() -> {
            startActivity(new Intent(WelcomeScreenActivity.this, MainActivity.class));
            Intent lockScreen = new Intent(WelcomeScreenActivity.this, LockScreenService.class);
            lockScreen.setAction("locked");
            ActivityCompat.startForegroundService(this, lockScreen);
            isLaunched = false;

            finish();
        }, 5000);
        if (PrefUtils.getIntegerPref(this, UPDATEWIFI) == 0) {
            PrefUtils.saveIntegerPref(this, UPDATEWIFI, 1);
        }
        //update sim toggle
        if (PrefUtils.getIntegerPref(this, UPDATESIM) == 0) {
            PrefUtils.saveIntegerPref(this, UPDATESIM, 2);
        }
        //sm wifi
        if (PrefUtils.getIntegerPref(this, SECUREMARKETWIFI) == 0) {
            PrefUtils.saveIntegerPref(this, SECUREMARKETWIFI, 1);
        }
        //sm sim
        if (PrefUtils.getIntegerPref(this, SECUREMARKETSIM) == 0) {
            PrefUtils.saveIntegerPref(this, SECUREMARKETSIM, 2);
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isLaunched){
            startActivity(new Intent(WelcomeScreenActivity.this, MainActivity.class));
            Intent lockScreen = new Intent(WelcomeScreenActivity.this, LockScreenService.class);
            lockScreen.setAction("locked");
            ActivityCompat.startForegroundService(this, lockScreen);
            finish();
        }
    }

    void broadCastIntent() {
        Intent intent = new Intent("com.secure.systemcontrol.DATA_AND_ROAMING");
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setComponent(new ComponentName("com.secure.systemcontrol", "com.secure.systemcontrol.receivers.SettingsReceiver"));
        sendBroadcast(intent);
    }
}
