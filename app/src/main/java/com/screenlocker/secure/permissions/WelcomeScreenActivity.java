package com.screenlocker.secure.permissions;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.screenlocker.secure.R;
import com.screenlocker.secure.settings.SettingsActivity;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

public class WelcomeScreenActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

       new Handler().postDelayed(() -> {
           Intent intent = new Intent(WelcomeScreenActivity.this, SettingsActivity.class);
           startActivity(intent);
           finish();
       },3000);

    }
}
