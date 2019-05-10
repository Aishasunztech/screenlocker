package com.screenlocker.secure.socket;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import com.screenlocker.secure.R;

public class TransparentActivity extends AppCompatActivity {

    private boolean isOkPressed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new AlertDialog.Builder(this)
                .setTitle("Restart Device")
                .setMessage("Please restart your device to change IMEI")
                .setCancelable(false)
                .show();
        getWindow().setBackgroundDrawable(new ColorDrawable(0));

//        getWindow().setBackgroundDrawable(new ColorDrawable(0));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getColor(android.R.color.transparent));
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isOkPressed)
        {
            this.finish();
        }
    }
}
