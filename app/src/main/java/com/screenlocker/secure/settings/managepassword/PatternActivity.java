package com.screenlocker.secure.settings.managepassword;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.itsxtt.patternlock.PatternLockView;
import com.screenlocker.secure.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

class PatternActivity extends AppCompatActivity {


//    @BindView(R.id.patternLockView)
  //  PatternLockView patternLockView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_pattern);
     //   ButterKnife.bind(this);

//        patternLockView.setOnPatternListener(new PatternLockView.OnPatternListener() {
//            @Override
//            public void onStarted() {
//
//            }
//
//            @Override
//            public void onProgress(ArrayList<Integer> ids) {
//
//            }
//
//            @Override
//            public boolean onComplete(ArrayList<Integer> ids) {
//                /*
//                 * A return value required
//                 * if the pattern is not correct and you'd like change the pattern to error state, return false
//                 * otherwise return true
//                 */
//                //return isPatternCorrect();
//                Toast.makeText(PatternActivity.this, String.valueOf(ids), Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        });
//
    }
}
