package com.screenlocker.secure.settings.managepassword;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.screenlocker.secure.R;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.views.patternlock.PatternLockView;
import com.screenlocker.secure.views.patternlock.listener.PatternLockViewListener;
import com.screenlocker.secure.views.patternlock.utils.PatternLockUtils;

import java.util.List;

public class PatternActivity extends AppCompatActivity {
    private String extra;
    private TextView message;
    private PatternLockView mPatternView;
    private int mTry = 0;
    private String tryPattern;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern);
        mPatternView = findViewById(R.id.patter_lock_view);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        message = findViewById(R.id.profile_name);
        if (getIntent().hasExtra(Intent.EXTRA_TEXT)) {
            extra = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            if (extra.equalsIgnoreCase(AppConstants.KEY_GUEST)) {
                getSupportActionBar().setTitle("Guest Pattern");
                message.setText("Please Draw Pattern");
            } else if (extra.equalsIgnoreCase(AppConstants.KEY_DURESS)) {
                getSupportActionBar().setTitle("Duress Pattern");
                message.setText("Please Draw Pattern");
            } else if (extra.equalsIgnoreCase(AppConstants.KEY_MAIN)) {
                getSupportActionBar().setTitle("Encrypted Pattern");
                message.setText("Please Draw Pattern");

            } else {
                //
            }
        }
        mPatternView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {

            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {

                if (pattern.size() < 4) {
                    Toast.makeText(PatternActivity.this, "Pattern is too Short", Toast.LENGTH_SHORT).show();
                    mPatternView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                    new Handler().postDelayed(mPatternView::clearPattern, 500);
                    return;
                }
                if (mTry == 0) {
                    tryPattern = PatternLockUtils.patternToString(mPatternView, pattern);
                    mTry++;
                    message.setText("Confirm Pattern");
                    mPatternView.clearPattern();

                } else if (mTry == 1) {
                    if (tryPattern.equals(PatternLockUtils.patternToString(mPatternView, pattern))) {
                        switch (extra){
                            case AppConstants.KEY_MAIN:
                                PrefUtils.saveStringPref(PatternActivity.this, AppConstants.ENCRYPT_DEFAULT_CONFIG, AppConstants.PATTERN_PASSWORD);
                                PrefUtils.saveStringPref(PatternActivity.this, AppConstants.ENCRYPT_PATTERN, tryPattern);
                                PrefUtils.saveStringPref(PatternActivity.this, AppConstants.KEY_MAIN_PASSWORD, null);
                                Toast.makeText(PatternActivity.this, "Pattern Updated", Toast.LENGTH_SHORT).show();
                                finish();
                                break;
                            case AppConstants.KEY_GUEST:
                                PrefUtils.saveStringPref(PatternActivity.this, AppConstants.GUEST_DEFAULT_CONFIG, AppConstants.PATTERN_PASSWORD);
                                PrefUtils.saveStringPref(PatternActivity.this, AppConstants.GUEST_PATTERN, tryPattern);
                                PrefUtils.saveStringPref(PatternActivity.this, AppConstants.KEY_GUEST_PASSWORD, null);
                                Toast.makeText(PatternActivity.this, "Pattern Updated", Toast.LENGTH_SHORT).show();
                                finish();
                                break;
                            case AppConstants.KEY_DURESS:
                                PrefUtils.saveStringPref(PatternActivity.this, AppConstants.DUERESS_DEFAULT_CONFIG, AppConstants.PATTERN_PASSWORD);
                                PrefUtils.saveStringPref(PatternActivity.this, AppConstants.DURESS_PATTERN, tryPattern);
                                PrefUtils.saveStringPref(PatternActivity.this, AppConstants.KEY_DURESS_PASSWORD, null);
                                Toast.makeText(PatternActivity.this, "Pattern Updated", Toast.LENGTH_SHORT).show();
                                finish();
                                break;
                        }


                        //right pattern
                    } else {
                        mTry = 0;
                        message.setText("Please Draw Pattern");
                        mPatternView.clearPattern();
                    }
                }
            }

            @Override
            public void onCleared() {

            }
        });

    }
}
