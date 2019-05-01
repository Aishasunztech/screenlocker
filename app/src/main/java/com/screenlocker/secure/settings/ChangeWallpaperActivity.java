package com.screenlocker.secure.settings;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.screenlocker.secure.R;
import com.screenlocker.secure.utils.AppConstants;

import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN;

public class ChangeWallpaperActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_wallpaper);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Change Wallpaper");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.btnGuestWallpaper).setOnClickListener(this);
        findViewById(R.id.btnEncryptedWallpaper).setOnClickListener(this);
        findViewById(R.id.btnLockScreenWallpaer).setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGuestWallpaper:
                break;
            case R.id.btnEncryptedWallpaper:
                break;
            case R.id.btnLockScreenWallpaer:
                break;
        }
    }

    public void showAlertDialog(String title,String type) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(10, 10, 10, 10);
        final EditText input = new EditText(ChangeWallpaperActivity.this);
        input.setGravity(Gravity.CENTER);

        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        //input.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.mipmap.ic_launcher);
        input.setFocusable(true);
        input.setFocusableInTouchMode(true);
        input.clearFocus();
        input.requestFocus();
        input.postDelayed(() -> {
                    InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.showSoftInput(input, 0);
                }
                , 100);

        alertDialog.setPositiveButton(R.string.ok,
                (dialog, which) -> {

                });

        alertDialog.setNegativeButton(R.string.cancel,
                (dialog, which) -> {
                    dialog.cancel();
                });

        alertDialog.show();

    }
    private void handlePassword(String password, String type){
        switch (type){
            case KEY_GUEST:
                break;
            case KEY_MAIN:

        }
    }
}
