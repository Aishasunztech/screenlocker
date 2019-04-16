package com.secureSetting;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.widget.SeekBar;

import com.screenlocker.secure.R;

public class BrightnessDialog extends Dialog {
    private SeekBar seekBar;
    private Context context;
    private ConstraintLayout container;
    BrightnessChangeListener listener;
    public BrightnessDialog(Context context) {
        super(context);
        this.context = context;
        if(context instanceof BrightnessChangeListener)
        {
            this.listener = (BrightnessChangeListener) context;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_brightness);

        seekBar = findViewById(R.id.seek_bar);
        container = findViewById(R.id.brightness_container);

        int brightness = UtilityFunctions.getScreenBrightness(context);
        seekBar.setProgress(brightness);


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setScreenBrightness(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                listener.brightnessChanged();
            }
        });
    }


    // Change the screen brightness
    public void setScreenBrightness(int brightnessValue){

        if(brightnessValue >= 0 && brightnessValue <= 255){
            Settings.System.putInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    brightnessValue
            );

        }
    }



    @Override
    protected void onStart() {
        super.onStart();

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        container.setMinimumWidth(width);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

    }

    public interface BrightnessChangeListener{
        void brightnessChanged();
    }
}
