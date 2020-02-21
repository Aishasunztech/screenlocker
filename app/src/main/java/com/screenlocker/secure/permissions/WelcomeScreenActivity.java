package com.screenlocker.secure.permissions;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.secure.launcher.R;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.utils.PrefUtils;

import static com.screenlocker.secure.utils.AppConstants.SECUREMARKETSIM;
import static com.screenlocker.secure.utils.AppConstants.SECUREMARKETWIFI;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
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
        PrefUtils prefUtils = PrefUtils.getInstance(this);
        prefUtils.saveBooleanPref( TOUR_STATUS, true);
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
        if (prefUtils.getIntegerPref( UPDATEWIFI) == 0) {
            prefUtils.saveIntegerPref( UPDATEWIFI, 1);
        }
        //sm wifi
        if (prefUtils.getIntegerPref( SECUREMARKETWIFI) == 0) {
            prefUtils.saveIntegerPref( SECUREMARKETWIFI, 1);
        }
        //sm sim
        if (prefUtils.getIntegerPref( SECUREMARKETSIM) == 0) {
            prefUtils.saveIntegerPref( SECUREMARKETSIM, 2);
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
