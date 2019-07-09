package com.screenlocker.secure.permissions;

import android.content.Intent;
import android.os.Bundle;

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

import static com.screenlocker.secure.utils.AppConstants.LINKSIM;
import static com.screenlocker.secure.utils.AppConstants.SECUREMARKETSIM;
import static com.screenlocker.secure.utils.AppConstants.SECUREMARKETWIFI;
import static com.screenlocker.secure.utils.AppConstants.UPDATESIM;
import static com.screenlocker.secure.utils.AppConstants.UPDATEWIFI;

public class WelcomeScreenActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);


        OneTimeWorkRequest insertionWork =
                new OneTimeWorkRequest.Builder(BlurWorker.class)
                        .build();
        WorkManager.getInstance().enqueue(insertionWork);

        WorkManager.getInstance().getWorkInfoByIdLiveData(insertionWork.getId())
                .observe(this, workInfo -> {
                    // Do something with the status
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        Intent lockScreen = new Intent(WelcomeScreenActivity.this, LockScreenService.class);
                        lockScreen.setAction("locked");
                        ActivityCompat.startForegroundService(this, lockScreen);
                        lockScreen = new Intent(WelcomeScreenActivity.this, MainActivity.class);
                        Intent finalLockScreen = lockScreen;
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                startActivity(finalLockScreen);
                                finish();
                            }
                        }, 5000);

                    }
                });
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
}
