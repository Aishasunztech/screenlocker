package com.screenlocker.secure.settings.managepassword;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.screenlocker.secure.R;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.views.patternlock.PatternLockView;
import com.screenlocker.secure.views.patternlock.listener.PatternLockViewListener;
import com.screenlocker.secure.views.patternlock.utils.PatternLockUtils;

import java.util.List;

import static com.screenlocker.secure.socket.utils.utils.getDeviceStatus;
import static com.screenlocker.secure.socket.utils.utils.loginAsEncrypted;
import static com.screenlocker.secure.socket.utils.utils.loginAsGuest;
import static com.screenlocker.secure.socket.utils.utils.wipeDevice;

public class VerifyPatternActivity extends AppCompatActivity {
private String extra ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_pattern_acitivity);
        ImageView imageView = findViewById(R.id.profile_image);

        Intent intent =  getIntent();
        if (intent !=null){
            extra =intent.getStringExtra(Intent.EXTRA_TEXT);
            switch (extra){
                case AppConstants.KEY_MAIN:
                    imageView.setImageResource(R.drawable.ic_encrypted_third);
                    break;
                case AppConstants.KEY_GUEST:
                    imageView.setImageResource(R.drawable.ic_guest_icon);
                    break;
                case AppConstants.KEY_DURESS:
                    imageView.setImageResource(R.drawable.ic_duress_icon);
                    break;
            }
        }
        PatternLockView mPatternLockView = findViewById(R.id.patter_lock_view);
        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {
                Log.d(getClass().getName(), "Pattern drawing started");
            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {
//                Log.d(getClass().getName(), "Pattern progress: " +
//                        PatternLockUtils.patternToString(mPatternLockView, progressPattern));
            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {

                if (pattern.size() < 4) {

                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    mPatternLockView.clearPattern();
                    return;
                }
                String patternString = PatternLockUtils.patternToString(mPatternLockView, pattern);
                if (patternString.equals(PrefUtils.getStringPref(VerifyPatternActivity.this, AppConstants.GUEST_PATTERN)) && extra.equals(AppConstants.KEY_GUEST)) {
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                    new Handler().postDelayed(() -> {
                        mPatternLockView.clearPattern();
                        setResult(RESULT_OK);
                        onBackPressed();
                    }, 150);
                } else if (patternString.equals(PrefUtils.getStringPref(VerifyPatternActivity.this, AppConstants.ENCRYPT_PATTERN))&& extra.equals(AppConstants.KEY_MAIN)) {
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                    new Handler().postDelayed(() -> {
                        mPatternLockView.clearPattern();
                        setResult(RESULT_OK);
                        onBackPressed();
                    }, 150);
                } else if (patternString.equals(PrefUtils.getStringPref(VerifyPatternActivity.this, AppConstants.DURESS_PATTERN)) && extra.equals(AppConstants.KEY_DURESS)) {
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                    new Handler().postDelayed(() -> {
                        mPatternLockView.clearPattern();
                        setResult(RESULT_OK);
                        onBackPressed();
                    }, 150);
                } else {
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    new Handler().postDelayed(() -> {
                        mPatternLockView.clearPattern();
                    }, 500);
                }

//
            }

            @Override
            public void onCleared() {
                Log.d(getClass().getName(), "Pattern has been cleared");
            }
        });
    }
}
