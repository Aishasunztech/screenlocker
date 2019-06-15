package com.screenlocker.secure.permissions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.screenlocker.secure.R;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.updateDB.BlurWorker;
import com.screenlocker.secure.utils.PrefUtils;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

import static com.screenlocker.secure.utils.AppConstants.DB_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;

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
                        startActivity(lockScreen);
                        finish();
                    }
                });


    }
}
