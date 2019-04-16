package com.secureSetting;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.screenlocker.secure.R;

import java.util.Timer;
import java.util.TimerTask;

import static com.secureSetting.UtilityFunctions.getSleepTime;

public class SleepDialog extends Dialog {

    private LinearLayout container;
    private RadioGroup radioGroup;
    private Context context;
    private SleepChangerListener listener;
    private RadioButton radio_15,radio_30s,radio_1m,radio_2m,radio_5m,radio_10m,radio_30m;


    public SleepDialog( Context context) {
        super(context);
        this.context = context;

        if(context instanceof SleepChangerListener)
        {
            listener = (SleepChangerListener) context;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_sleep);

        container = findViewById(R.id.sleep_dialog_container);
        radioGroup = findViewById(R.id.rg_sleep_values);
        radio_15 = findViewById(R.id.rb_15_seconds);
        radio_30s = findViewById(R.id.rb_30_seconds);
        radio_1m = findViewById(R.id.rb_1_minute);
        radio_2m = findViewById(R.id.rb_2_minute);
        radio_5m = findViewById(R.id.rb_5_minute);
        radio_10m = findViewById(R.id.rb_10_minute);
        radio_30m = findViewById(R.id.rb_30_minute);

        if(getSleepTime(context) != -1)
        {
            switch (getSleepTime(context))
            {
                case 15:
                    radio_15.setChecked(true);
                    break;
                case 30:
                    radio_30s.setChecked(true);
                    break;
                case 60:
                    radio_1m.setChecked(true);
                    break;
                case 120:
                    radio_2m.setChecked(true);
                    break;
                case 300:
                    radio_5m.setChecked(true);
                    break;
                case 600:
                    radio_10m.setChecked(true);
                    break;
                case 1800:
                    radio_30m.setChecked(true);
                    break;

            }
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int time = -1;

                String minutes = "";
                if(checkedId == R.id.rb_15_seconds)
                {
                    time = 15000;
                    minutes = "15 seconds";
                }else if(checkedId == R.id.rb_30_seconds)
                {
                    time = 30000;
                    minutes = "30 seconds";


                }else if(checkedId == R.id.rb_1_minute)
                {
                    time = 60000;
                    minutes = "1 minute";


                }else if(checkedId == R.id.rb_2_minute)
                {
                    time = 120000;
                    minutes = "2 minutes";

                }else if(checkedId == R.id.rb_5_minute)
                {
                    time = 300000;
                    minutes = "5 minutes";

                }else if(checkedId == R.id.rb_10_minute)
                {
                    time = 10*60*1000;
                    minutes = "10 minutes";

                }else if(checkedId == R.id.rb_30_minute)
                {
                    time = 30*60*1000;
                    minutes = "30 minutes";

                }

                setSleepTime(time);
                listener.sleepTimeChanged("After " + minutes + " of inactivity");
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        SleepDialog.this.dismiss();

                    }
                },300);
            }
        });
    }

    private void setSleepTime(int time) {
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, time);
    }



    @Override
    protected void onStart() {
        super.onStart();

        container.setMinimumWidth(Resources.getSystem().getDisplayMetrics().widthPixels);
    }

    public interface SleepChangerListener{
        void sleepTimeChanged(String time);
    }
}
