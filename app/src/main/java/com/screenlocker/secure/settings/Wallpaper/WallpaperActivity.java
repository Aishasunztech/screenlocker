package com.screenlocker.secure.settings.Wallpaper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.screenlocker.secure.R;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static com.screenlocker.secure.utils.AppConstants.KEY_CODE;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN;

public class WallpaperActivity extends BaseActivity implements View.OnClickListener {

    private boolean isBackPressed = false;
    private boolean goToGuest,goToEncrypt,goToLockScreen;

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

        goToGuest = false;
        goToEncrypt = false;
        goToLockScreen = false;
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
                showAlertDialog("Enter Guest Password",KEY_GUEST);
                break;
            case R.id.btnEncryptedWallpaper:
                showAlertDialog("Enter Encrypted Password",KEY_MAIN);
                break;
            case R.id.btnLockScreenWallpaer:
                showAlertDialog("Enter Encrypted Password",KEY_CODE);
                break;
        }
    }

    public void showAlertDialog(String title, final String type) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(10, 10, 10, 10);
        final EditText input = new EditText(WallpaperActivity.this);
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
                , 0);

        alertDialog.setPositiveButton(R.string.ok,
                (dialog, which) -> {
                    String password = input.getText().toString().trim();
                    if (password.equals("")){
                        Snackbar.make(findViewById(R.id.rootLayout),"Please Enter Password",Snackbar.LENGTH_LONG).show();
                    }else
                        handlePassword(password , type);
                });

        alertDialog.setNegativeButton(R.string.cancel,
                (dialog, which) -> {
                    dialog.cancel();
                });

        alertDialog.show();

    }

    private void handlePassword(String password, String type) {
        switch (type) {
            case KEY_GUEST:
                if (password.equals(PrefUtils.getStringPref(WallpaperActivity.this, AppConstants.KEY_GUEST_PASSWORD))) {
                    //

                    goToGuest = true;
                    Intent intent = new Intent(this, ChangeWallpaper.class);
                    intent.putExtra("TYPE", KEY_GUEST);
                    startActivity(intent);
                }else
                    showInvalidPasswordDialog(this);
                break;
            case KEY_MAIN:
                if (password.equals(PrefUtils.getStringPref(WallpaperActivity.this, AppConstants.KEY_MAIN_PASSWORD))){

                    goToEncrypt = true;

                    Intent intent = new Intent(this, ChangeWallpaper.class);
                    intent.putExtra("TYPE", KEY_MAIN);
                    startActivity(intent);
                }
                else
                    showInvalidPasswordDialog(this);

                break;
            case KEY_CODE:
                if (password.equals(PrefUtils.getStringPref(WallpaperActivity.this, AppConstants.KEY_MAIN_PASSWORD))){

                    goToLockScreen = true;

                    Intent intent = new Intent(this, ChangeWallpaper.class);
                    intent.putExtra("TYPE", KEY_CODE);
                    startActivity(intent);
                }
                else
                    showInvalidPasswordDialog(this);

                break;
        }
    }

    private void showInvalidPasswordDialog(AppCompatActivity activity) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle("Invalid password");
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialog.setMessage("The password you entered is incorrect.");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        isBackPressed = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!isBackPressed)
        {
            if(!goToGuest && !goToEncrypt && !goToLockScreen)
            {
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
    }
}
