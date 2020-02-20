package com.screenlocker.secure.settings.managepassword;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.SecuredSharedPref;
import com.screenlocker.secure.views.patternlock.PatternLockWithDotsOnly;
import com.screenlocker.secure.views.patternlock.listener.PatternLockWithDotListener;
import com.screenlocker.secure.views.patternlock.utils.PatternLockUtils;
import com.secure.launcher.R;

import java.util.List;

import timber.log.Timber;

public class VerifyPatternActivity extends AppCompatActivity {
    private String extra;
    private SecuredSharedPref pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = SecuredSharedPref.getInstance(this);
        setContentView(R.layout.activity_verify_pattern_acitivity);
        ImageView imageView = findViewById(R.id.profile_image);

        Intent intent = getIntent();
        if (intent != null) {
            extra = intent.getStringExtra(Intent.EXTRA_TEXT);
            switch (extra) {
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
        PatternLockWithDotsOnly mPatternLockView = findViewById(R.id.patter_lock_view);
        mPatternLockView.setHapticFeedbackEnabled(false);
        mPatternLockView.addPatternLockListener(new PatternLockWithDotListener() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onProgress(List<PatternLockWithDotsOnly.Dot> progressPattern) {
//                Log.d(getClass().getName(), "Pattern progress: " +
//                        PatternLockUtils.patternToString(mPatternLockView, progressPattern));
            }

            @Override
            public void onComplete(List<PatternLockWithDotsOnly.Dot> pattern) {

                if (pattern.size() < 4) {

                    mPatternLockView.setViewMode(PatternLockWithDotsOnly.PatternViewMode.WRONG);
                    mPatternLockView.clearPattern();
                    return;
                }
                String patternString = PatternLockUtils.patternToString(mPatternLockView, pattern);
                if (patternString.equals(pref.getStringPref( AppConstants.GUEST_PATTERN))
                        && extra.equals(AppConstants.KEY_GUEST)) {
                    mPatternLockView.setViewMode(PatternLockWithDotsOnly.PatternViewMode.CORRECT);
                    new Handler().postDelayed(() -> {
                        mPatternLockView.clearPattern();
                        setResult(RESULT_OK);
                        onBackPressed();
                    }, 150);
                } else if (patternString.equals(pref.getStringPref( AppConstants.ENCRYPT_PATTERN))
                        && extra.equals(AppConstants.KEY_MAIN)) {
                    mPatternLockView.setViewMode(PatternLockWithDotsOnly.PatternViewMode.CORRECT);
                    new Handler().postDelayed(() -> {
                        mPatternLockView.clearPattern();
                        setResult(RESULT_OK);
                        onBackPressed();
                    }, 150);
                } else if (patternString.equals(pref.getStringPref( AppConstants.DURESS_PATTERN))
                        && extra.equals(AppConstants.KEY_DURESS)) {
                    mPatternLockView.setViewMode(PatternLockWithDotsOnly.PatternViewMode.CORRECT);
                    new Handler().postDelayed(() -> {
                        mPatternLockView.clearPattern();
                        setResult(RESULT_OK);
                        onBackPressed();
                    }, 150);
                } else {
                    mPatternLockView.setViewMode(PatternLockWithDotsOnly.PatternViewMode.WRONG);
                    new Handler().postDelayed(() -> {
                        mPatternLockView.clearPattern();
                        setResult(RESULT_CANCELED);
                        finish();
                    }, 500);
                }

//
            }

            @Override
            public void onCleared() {
                Timber.d("Pattern has been cleared");
            }
        });
    }
}
