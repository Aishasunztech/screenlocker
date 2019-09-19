package com.screenlocker.secure.settings.managepassword;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.screenlocker.secure.R;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.views.patternlock.PatternLockView;
import com.screenlocker.secure.views.patternlock.PatternLockWithDotsOnly;
import com.screenlocker.secure.views.patternlock.listener.PatternLockViewListener;
import com.screenlocker.secure.views.patternlock.listener.PatternLockWithDotListener;
import com.screenlocker.secure.views.patternlock.utils.PatternLockUtils;

import java.util.ArrayList;
import java.util.List;

public class VerifyComboPassword extends AppCompatActivity {

    private String extra;
    private TextView msg;
    private PatternLockView mPatternLockView;
    private NCodeView codeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_combo_password);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        msg = findViewById(R.id.textView7);
        ImageView profile = findViewById(R.id.profile_image);
        if (getIntent().hasExtra(Intent.EXTRA_TEXT)) {
            extra = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            if (extra.equalsIgnoreCase(AppConstants.KEY_GUEST)) {
                getSupportActionBar().setTitle("Guest Combo");
                profile.setImageResource(R.drawable.ic_guest_icon);
            } else if (extra.equalsIgnoreCase(AppConstants.KEY_DURESS)) {
                getSupportActionBar().setTitle("Duress Combo");
                profile.setImageResource(R.drawable.ic_duress_icon);
            } else if (extra.equalsIgnoreCase(AppConstants.KEY_MAIN)) {
                getSupportActionBar().setTitle("Encrypted Combo");
                profile.setImageResource(R.drawable.ic_encrypted_third);
            } else {
                //
            }
        }

        codeView = findViewById(R.id.NCodeView);
        mPatternLockView = findViewById(R.id.patter_lock_view);
        codeView.setListener(new NCodeView.OnPFCodeListener() {
            @Override
            public void onCodeCompleted(ArrayList<Integer> code) {
                if (code.toString().equals(PrefUtils.getStringPref(VerifyComboPassword.this, AppConstants.GUEST_COMBO_PIN)) && extra.equals(AppConstants.KEY_GUEST)) {
                    mPatternLockView.setNumberInputAllow(false);
                    mPatternLockView.invalidate();
                    msg.setText("Draw Pattern");
                } else if (code.toString().equals(PrefUtils.getStringPref(VerifyComboPassword.this, AppConstants.ENCRYPT_COMBO_PIN)) && extra.equals(AppConstants.KEY_MAIN)) {
                    mPatternLockView.setNumberInputAllow(false);
                    mPatternLockView.invalidate();
                    msg.setText("Draw Pattern");
                } else if (code.toString().equals(PrefUtils.getStringPref(VerifyComboPassword.this, AppConstants.DURESS_COMBO_PIN)) && extra.equals(AppConstants.KEY_DURESS)) {
                    mPatternLockView.setNumberInputAllow(false);
                    mPatternLockView.invalidate();
                    msg.setText("Draw Pattern");
                } else {
                    codeView.setColor();
                    new Handler().postDelayed(() -> {
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                    }, 500);
                }

            }

            @Override
            public void onCodeNotCompleted(ArrayList<Integer> code) {

            }
        });

        mPatternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {
            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {
            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {

                if (pattern.size() == 1) {
                    codeView.input(pattern.get(0).getRandom());
                    mPatternLockView.clearPattern();
                    return;
                }
                if (mPatternLockView.isNumberInputAllow()) {
                    mPatternLockView.clearPattern();
                    return;
                }
                String patternString = PatternLockUtils.patternToString(mPatternLockView, pattern);
                if (patternString.equals(PrefUtils.getStringPref(VerifyComboPassword.this, AppConstants.GUEST_COMBO_PATTERN)) && extra.equals(AppConstants.KEY_GUEST)) {
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                    new Handler().postDelayed(() -> {
                        mPatternLockView.clearPattern();
                        setResult(RESULT_OK);
                        onBackPressed();
                    }, 150);
                } else if (patternString.equals(PrefUtils.getStringPref(VerifyComboPassword.this, AppConstants.ENCRYPT_COMBO_PATTERN)) && extra.equals(AppConstants.KEY_MAIN)) {
                    mPatternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                    new Handler().postDelayed(() -> {
                        mPatternLockView.clearPattern();
                        setResult(RESULT_OK);
                        onBackPressed();
                    }, 150);
                } else if (patternString.equals(PrefUtils.getStringPref(VerifyComboPassword.this, AppConstants.DURESS_COMBO_PATTERN)) && extra.equals(AppConstants.KEY_DURESS)) {
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
                        setResult(Activity.RESULT_CANCELED);
                        finish();
                    }, 500);
                }

//
            }

            @Override
            public void onCleared() {
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()== android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
