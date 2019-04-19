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
    private int up = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);
        TextView update = findViewById(R.id.update);
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                up--;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (up != -1){
                            update.setText(String.valueOf(up));
                        }else {
                            Intent intent = new Intent(WelcomeScreenActivity.this, SettingsActivity.class);
                            startActivity(intent);
                            finish();
                            cancel();
                        }
                    }
                });

            }
        },0,1000);

    }
}
