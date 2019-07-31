package com.screenlocker.secure.settings.managepassword;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.screenlocker.secure.R;

import butterknife.BindView;
import butterknife.ButterKnife;

class PasswordOptionsAcitivity extends AppCompatActivity implements View.OnClickListener {


    @BindView(R.id.tvPinOption)
    TextView tvPinOption;
    @BindView(R.id.tvPatternOption)
    TextView tvPatternOption;
    @BindView(R.id.tvFingerPrint)
    FrameLayout tvFingerPrint;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_options_acitivity);
        ButterKnife.bind(this);

        tvPinOption.setOnClickListener(this);
        tvPatternOption.setOnClickListener(this);
        tvFingerPrint.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvPinOption:

                break;
            case R.id.tvPatternOption:
                startActivity(new Intent(this, PatternActivity.class));
                break;
            case R.id.tvFingerPrint:

                break;
        }
    }
}
