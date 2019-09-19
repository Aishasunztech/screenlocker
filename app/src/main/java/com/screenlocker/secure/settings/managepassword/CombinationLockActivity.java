package com.screenlocker.secure.settings.managepassword;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.screenlocker.secure.R;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.views.patternlock.PatternLockView;
import com.screenlocker.secure.views.patternlock.PatternLockWithDotsOnly;
import com.screenlocker.secure.views.patternlock.listener.PatternLockViewListener;
import com.screenlocker.secure.views.patternlock.listener.PatternLockWithDotListener;
import com.screenlocker.secure.views.patternlock.utils.PatternLockUtils;

import java.util.List;

public class CombinationLockActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combination_lock);
        NCodeView codeView = findViewById(R.id.NCodeView);
        codeView.setListener(new NCodeView.OnPFCodeListener() {
            @Override
            public void onCodeCompleted(String code) {

            }

            @Override
            public void onCodeNotCompleted(String code) {

            }
        });
        PatternLockView patternLockView = findViewById(R.id.patter_lock_view);
        patternLockView.addPatternLockListener(new PatternLockViewListener() {
            @Override
            public void onStarted() {

            }

            @Override
            public void onProgress(List<PatternLockView.Dot> progressPattern) {

            }

            @Override
            public void onComplete(List<PatternLockView.Dot> pattern) {
                if (pattern.size() == 1){
                    codeView.input(String.valueOf(pattern.get(0).getRandom()));
                }
            }

            @Override
            public void onCleared() {

            }
        });
    }
}
