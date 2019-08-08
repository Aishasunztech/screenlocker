package com.screenlocker.secure.settings.managepassword;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.screenlocker.secure.R;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Validator;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.screenlocker.secure.socket.utils.utils.passwordsOk;

/**
 * this activity set ups the password for the both guest and encrypted users
 */
public class SetUpLockActivity extends AppCompatActivity implements View.OnClickListener {
    /**
     * entered pin from the user
     */

    /**
     * to confirm the user entered password
     */

    /**
     * button to validate the password and save it
     */
    private AppCompatButton btnConfirm;
    private AppCompatButton btnDisableDuress;
    private boolean isBackPressed = false;

    private String from;

    @BindView(R.id.pin_input_layout)
    TextInputLayout pin_input_layout;
    @BindView(R.id.etEnterPin)
    TextInputEditText etEnterPin;

    @BindView(R.id.re_pin_input_layout)
    TextInputLayout re_pin_input_layout;
    @BindView(R.id.etConfirmPin)
    TextInputEditText etConfirmPin;

    @BindView(R.id.guest_image_icon)
    ImageView img_picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_lock);
        ButterKnife.bind(this);
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setToolbar(mToolbar);


        init();

        if (getIntent().hasExtra(Intent.EXTRA_TEXT)) {
            String setMainPwd = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            if (setMainPwd.equalsIgnoreCase(AppConstants.KEY_GUEST)) {
                from = AppConstants.KEY_GUEST;
                // setting toolbar name for guest type
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.set_guest_code);
//                etEnterPin.setHint(R.string.hint_please_enter_guest_pin);
//                etConfirmPin.setHint(R.string.hint_please_confirm_your_pin);
                img_picture.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_guest_icon));

                pin_input_layout.setHint(getResources().getString(R.string.hint_please_enter_guest_pin));
                re_pin_input_layout.setHint(getResources().getString(R.string.hint_please_confirm_your_pin));
            } else if (setMainPwd.equalsIgnoreCase(AppConstants.KEY_DURESS)) {
                from = AppConstants.KEY_DURESS;
                // setting toolbar name for guest type
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.set_duress_code);
                if (PrefUtils.getStringPref(this, AppConstants.KEY_DURESS_PASSWORD)!=null){

                    btnDisableDuress.setVisibility(View.VISIBLE );
                }
//                etEnterPin.setHint(R.string.hint_please_enter_duress_pin);
//                etConfirmPin.setHint(R.string.hint_please_confirm_your_pin);
                img_picture.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_duress_icon));

                pin_input_layout.setHint(getResources().getString(R.string.hint_please_enter_duress_pin));
                re_pin_input_layout.setHint(getResources().getString(R.string.hint_please_confirm_your_pin));
            } else if (setMainPwd.equalsIgnoreCase(AppConstants.KEY_CODE)) {
                from = AppConstants.KEY_CODE;
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.set_code_pin);
                etEnterPin.setHint(R.string.hint_please_enter_code_pin);
                etConfirmPin.setHint(R.string.hint_please_confirm_your_pin);
            } else {
                from = AppConstants.KEY_MAIN;
                // setting toolbar name for encrypted type
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.set_encrypted_code);
//                etEnterPin.setHint(R.string.hint_please_enter_encrypted_pin);
//                etConfirmPin.setHint(R.string.hint_please_confirm_your_pin);
                img_picture.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_encrypted_third));
                pin_input_layout.setHint(getResources().getString(R.string.hint_please_enter_encrypted_pin));
                re_pin_input_layout.setHint(getResources().getString(R.string.hint_please_confirm_your_pin));


            }
        }

    }

    private void setToolbar(Toolbar mToolbar) {
        setSupportActionBar(mToolbar);
        //setting back button in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void init() {
//        etEnterPin = findViewById(R.id.etEnterPin);
//        etConfirmPin = findViewById(R.id.etConfirmPin);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnDisableDuress = findViewById(R.id.disable_duress);
        btnDisableDuress.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackPressed = true;
        setResult(RESULT_CANCELED);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.disable_duress){
            PrefUtils.saveStringPref(this, AppConstants.KEY_DURESS_PASSWORD,null);
            btnDisableDuress.setEnabled(false);
            btnDisableDuress.setText(getResources().getString(R.string.duress_pin_disabled));
        }
        if (view.getId() == R.id.btnConfirm) {


            String enteredPassword = etEnterPin.getText().toString().trim();
            String reEnteredPassword = etConfirmPin.getText().toString().trim();

            boolean isValid = Validator.validAndMatch(enteredPassword, reEnteredPassword);


            if (isValid) {

                boolean keyOk = passwordsOk(this, reEnteredPassword);

                //Password password = new Password();
                // password.setUserPassword(reEnteredPassword);
                switch (from) {
                    case AppConstants.KEY_GUEST:

                        if (keyOk) {
                            PrefUtils.saveStringPref(SetUpLockActivity.this, AppConstants.GUEST_DEFAULT_CONFIG, AppConstants.PIN_PASSWORD);
                            PrefUtils.saveStringPref(SetUpLockActivity.this, AppConstants.GUEST_PATTERN, null);
                            PrefUtils.saveStringPref(SetUpLockActivity.this, AppConstants.KEY_GUEST_PASSWORD, reEnteredPassword);
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            etConfirmPin.setError(getResources().getString(R.string.password_taken));
                        }
                        break;
                    case AppConstants.KEY_CODE:

                        if (keyOk) {
                            PrefUtils.saveStringPref(this, AppConstants.KEY_CODE_PASSWORD, reEnteredPassword);
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            etConfirmPin.setError(getResources().getString(R.string.password_taken));
                        }

                        break;
                    case AppConstants.KEY_MAIN:

                        if (keyOk) {
                            PrefUtils.saveStringPref(this, AppConstants.ENCRYPT_DEFAULT_CONFIG, AppConstants.PIN_PASSWORD);
                            PrefUtils.saveStringPref(this, AppConstants.ENCRYPT_PATTERN, null);
                            finish();
                            PrefUtils.saveStringPref(this, AppConstants.KEY_MAIN_PASSWORD, reEnteredPassword);
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            etConfirmPin.setError(getResources().getString(R.string.password_taken));
                        }
                        break;

                    case AppConstants.KEY_DURESS:

                        if (keyOk) {
                            PrefUtils.saveStringPref(this, AppConstants.DUERESS_DEFAULT_CONFIG, AppConstants.PIN_PASSWORD);
                            PrefUtils.saveStringPref(this, AppConstants.DURESS_PATTERN, null);
                            PrefUtils.saveStringPref(this, AppConstants.KEY_DURESS_PASSWORD, reEnteredPassword);
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            etConfirmPin.setError(getResources().getString(R.string.password_taken));
                        }
                        break;
                }

                // finishing the current activity to go back with the result
            } else {
                if (TextUtils.isEmpty(enteredPassword)) {
                    etEnterPin.setError(getString(R.string.empty));
                } else if (reEnteredPassword.equals("")) {
                    etConfirmPin.setError(getString(R.string.empty));
                } else {
                    etConfirmPin.setError(getString(R.string.password_dont_match));
                }

            }
        }
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
            this.finish();
        }
    }


}
