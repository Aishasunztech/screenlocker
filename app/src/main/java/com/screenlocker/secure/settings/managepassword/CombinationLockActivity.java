package com.screenlocker.secure.settings.managepassword;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.utils.SecuredSharedPref;
import com.secure.launcher.R;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.views.patternlock.PatternLockView;
import com.screenlocker.secure.views.patternlock.listener.PatternLockViewListener;
import com.screenlocker.secure.views.patternlock.utils.PatternLockUtils;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class CombinationLockActivity extends BaseActivity implements NCodeView.OnPFCodeListener, PatternLockViewListener {

    private String extra;
    private String mCode;
    private String mPattern;
    private int mTry = 0;
    private TextView msg;
    private NCodeView codeView;
    private PatternLockView patternLockView;
    private Button btnCancel, btnConfirm;
    private SecuredSharedPref securedSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combination_lock);
        securedSharedPref = SecuredSharedPref.getInstance(this);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ImageView profile = findViewById(R.id.profile_image);
        msg = findViewById(R.id.textView7);
        if (getIntent().hasExtra(Intent.EXTRA_TEXT)) {
            extra = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            if (extra.equalsIgnoreCase(AppConstants.KEY_GUEST)) {
                getSupportActionBar().setTitle("Guest Combo");
                profile.setImageResource(R.drawable.ic_guest_icon);
            } else if (extra.equalsIgnoreCase(AppConstants.KEY_DURESS)) {
                getSupportActionBar().setTitle("Wipe Combo");
                profile.setImageResource(R.drawable.ic_duress_icon);
            } else if (extra.equalsIgnoreCase(AppConstants.KEY_MAIN)) {
                getSupportActionBar().setTitle("Encrypted Combo");
                profile.setImageResource(R.drawable.ic_encrypted_third);
            } else {
                //
            }
        }

        codeView = findViewById(R.id.NCodeView);
        patternLockView = findViewById(R.id.patter_lock_view);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm.setEnabled(false);
        btnCancel.setOnClickListener(v -> {
            mTry = 0;
            mCode = "";
            mPattern = "";
            msg.setText(getResources().getString(R.string.input_pin));
            patternLockView.setInputEnabled(true);
            codeView.clearCode();
            patternLockView.clearPattern();
            patternLockView.setNumberInputAllow(true);
            patternLockView.invalidate();
            btnConfirm.setEnabled(false);
            codeView.clearColor();
        });
        btnConfirm.setOnClickListener(v -> {
            mTry++;
            patternLockView.setInputEnabled(true);
            codeView.clearCode();
            patternLockView.clearPattern();
            patternLockView.setNumberInputAllow(true);
            patternLockView.invalidate();
            btnConfirm.setEnabled(false);
            msg.setText(getResources().getString(R.string.confirm_pin));
        });

        patternLockView.addPatternLockListener(this);
        codeView.setListener(this);
    }

    @Override
    public void onCodeCompleted(ArrayList<Integer> code) {
        Timber.d("onCodeCompleted: %s", code.toString());
        if (mTry == 0) {
            switch (extra) {
                case AppConstants.KEY_GUEST:
                    if (code.toString().equals(securedSharedPref.getStringPref( AppConstants.ENCRYPT_COMBO_PIN)) ||
                            code.toString().equals(securedSharedPref.getStringPref( AppConstants.DURESS_COMBO_PIN))) {
                        //FIXME: duplicate
                        codeView.setColor();
                    } else {
                        mCode = code.toString();
                        patternLockView.setNumberInputAllow(false);
                        patternLockView.invalidate();
                        msg.setText(getResources().getString(R.string.draw_pattern));
                    }
                    break;
                case AppConstants.KEY_DURESS:
                    if (code.toString().equals(securedSharedPref.getStringPref( AppConstants.ENCRYPT_COMBO_PIN)) ||
                            code.toString().equals(securedSharedPref.getStringPref( AppConstants.GUEST_COMBO_PIN))) {
                        //FIXME: duplicate
                        codeView.setColor();
                    } else {
                        mCode = code.toString();
                        patternLockView.setNumberInputAllow(false);
                        patternLockView.invalidate();
                        msg.setText(getResources().getString(R.string.draw_pattern));
                    }
                    break;
                case AppConstants.KEY_MAIN:
                    if (code.toString().equals(securedSharedPref.getStringPref( AppConstants.GUEST_COMBO_PIN)) ||
                            code.toString().equals(securedSharedPref.getStringPref( AppConstants.DURESS_COMBO_PIN))) {
                        //FIXME: duplicate
                        codeView.setColor();
                    } else {
                        mCode = code.toString();
                        patternLockView.setNumberInputAllow(false);
                        patternLockView.invalidate();
                        msg.setText(getResources().getString(R.string.draw_pattern));
                    }
                    break;
            }


        } else {
            if (code.toString().equals(mCode)) {
                patternLockView.setNumberInputAllow(false);
                patternLockView.invalidate();
                msg.setText(getResources().getString(R.string.confirm_pattern));
            } else {
                //FIXME: code did not match,  show a error
                codeView.setColor();
                Timber.d("FIXME: %s", code.toString());
            }
        }
    }

    @Override
    public void onCodeNotCompleted(ArrayList<Integer> code) {

    }

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
            patternLockView.clearPattern();
            return;
        }
        if (patternLockView.isNumberInputAllow()) {
            patternLockView.clearPattern();
            return;
        }
        if (mTry == 0) {
            mPattern = PatternLockUtils.patternToString(patternLockView, pattern);
            patternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
            patternLockView.setInputEnabled(false);
            btnConfirm.setEnabled(true);
            btnCancel.setEnabled(true);
        } else {
            if (mPattern.equals(PatternLockUtils.patternToString(patternLockView, pattern))) {
                //write pattern
                patternLockView.setViewMode(PatternLockView.PatternViewMode.CORRECT);
                switch (extra) {
                    case AppConstants.KEY_MAIN:
                        securedSharedPref.saveStringPref( AppConstants.ENCRYPT_DEFAULT_CONFIG, AppConstants.COMBO_PASSWORD);
                        securedSharedPref.saveStringPref( AppConstants.ENCRYPT_COMBO_PATTERN, mPattern);
                        securedSharedPref.saveStringPref( AppConstants.ENCRYPT_COMBO_PIN, mCode);
                        securedSharedPref.saveStringPref( AppConstants.KEY_MAIN_PASSWORD, null);
                        securedSharedPref.saveStringPref( AppConstants.ENCRYPT_PATTERN, null);
                        Toast.makeText(this, "Combo Updated", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    case AppConstants.KEY_GUEST:
                        securedSharedPref.saveStringPref( AppConstants.GUEST_DEFAULT_CONFIG, AppConstants.COMBO_PASSWORD);
                        securedSharedPref.saveStringPref( AppConstants.GUEST_COMBO_PATTERN, mPattern);
                        securedSharedPref.saveStringPref( AppConstants.GUEST_COMBO_PIN, mCode);
                        securedSharedPref.saveStringPref( AppConstants.KEY_GUEST_PASSWORD, null);
                        securedSharedPref.saveStringPref( AppConstants.GUEST_PATTERN, null);
                        Toast.makeText(this, "Combo Updated", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    case AppConstants.KEY_DURESS:
                        securedSharedPref.saveStringPref( AppConstants.DUERESS_DEFAULT_CONFIG, AppConstants.COMBO_PASSWORD);
                        securedSharedPref.saveStringPref( AppConstants.DURESS_COMBO_PATTERN, mPattern);
                        securedSharedPref.saveStringPref( AppConstants.DURESS_COMBO_PIN, mCode);
                        securedSharedPref.saveStringPref( AppConstants.KEY_DURESS_PASSWORD, null);
                        securedSharedPref.saveStringPref( AppConstants.DURESS_PATTERN, null);
                        Toast.makeText(this, "Combo Updated", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                }

            } else {
                //FIXME: wrong pattern confirmations
                patternLockView.setViewMode(PatternLockView.PatternViewMode.WRONG);
                btnCancel.setEnabled(true);

            }
        }
    }

    @Override
    public void onCleared() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
