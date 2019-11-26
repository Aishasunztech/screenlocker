package com.screenlocker.secure.settings.managepassword;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.secure.launcher.R;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.views.patternlock.PatternLockView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.screenlocker.secure.utils.AppConstants.KEY_DURESS_PASSWORD;

public class PasswordOptionsAcitivity extends AppCompatActivity implements View.OnClickListener {

    private String EXTRA;

    @BindView(R.id.tvPinOption)
    TextView tvPinOption;
    @BindView(R.id.tvPatternOption)
    TextView tvPatternOption;
    @BindView(R.id.tvFingerPrint)
    TextView tvFingerPrint;
    @BindView(R.id.rest_duress)
    Button resetDuress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_options_acitivity);
        ButterKnife.bind(this);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Setup Passwords");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getIntent().hasExtra(Intent.EXTRA_TEXT)) {
            EXTRA = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        }
        tvPinOption.setOnClickListener(this);
        tvPatternOption.setOnClickListener(this);
        tvFingerPrint.setOnClickListener(this);
        String passConfig = PrefUtils.getStringPref(this, AppConstants.DUERESS_DEFAULT_CONFIG);
        if (EXTRA != null && EXTRA.equals(AppConstants.KEY_DURESS)) {
            if (passConfig != null || PrefUtils.getStringPref(this, KEY_DURESS_PASSWORD) != null) {
                resetDuress.setVisibility(View.VISIBLE);
            }
        }

        resetDuress.setOnClickListener(v -> {

            PrefUtils.saveStringPref(this, AppConstants.DUERESS_DEFAULT_CONFIG, null);
            PrefUtils.saveStringPref(this, AppConstants.DURESS_COMBO_PATTERN, null);
            PrefUtils.saveStringPref(this, AppConstants.DURESS_COMBO_PIN, null);
            PrefUtils.saveStringPref(this, AppConstants.KEY_DURESS_PASSWORD, null);
            PrefUtils.saveStringPref(this, AppConstants.DURESS_PATTERN, null);
            finish();
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvPinOption:
                Intent intent = new Intent(this, SetUpLockActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, EXTRA);
                startActivity(intent);
                break;
            case R.id.tvPatternOption:
                Intent intent1 = new Intent(this, PatternActivity.class);
                intent1.putExtra(Intent.EXTRA_TEXT, EXTRA);
                startActivity(intent1);
                break;
            case R.id.tvFingerPrint:
                Intent intent2 = new Intent(this, CombinationLockActivity.class);
                intent2.putExtra(Intent.EXTRA_TEXT, EXTRA);
                startActivity(intent2);
//                Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
