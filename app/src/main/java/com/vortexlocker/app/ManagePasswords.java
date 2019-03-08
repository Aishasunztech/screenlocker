//package com.vortexlocker.app;
//
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.constraint.ConstraintLayout;
//import android.support.design.widget.Snackbar;
//import android.text.TextUtils;
//import android.view.View;
//import android.widget.EditText;
//
//import com.vortexlocker.app.base.BaseActivity;
//import com.vortexlocker.app.settings.SetUpLockActivity;
//import com.vortexlocker.app.settings.SettingsActivity;
//import com.vortexlocker.app.utils.AppConstants;
//import com.vortexlocker.app.utils.PrefUtils;
//
//import static com.vortexlocker.app.utils.AppConstants.KEY_GUEST_PASSWORD;
//
//public class ManagePasswords extends BaseActivity implements View.OnClickListener {
//    private ConstraintLayout rootLayout;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_manage_passwords);
//        setListeners();
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//    }
//
//    /**
//     * set listeners
//     */
//    private void setListeners() {
//        findViewById(R.id.tvSetGuestPassword).setOnClickListener(this);
//        findViewById(R.id.tvSetMainPassword).setOnClickListener(this);
//        findViewById(R.id.tvSetDuressPassword).setOnClickListener(this);
//    }
//
//    private void setIds() {
//        rootLayout = findViewById(R.id.rootLayout);
//    }
//
//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.tvSetGuestPassword:// handle the set guest password click event
//                /**
//                 * start the {@link SetUpLockActivity} to get the password
//                 */
//                handleSetGuestPassword();
//                break;
//            case R.id.tvSetMainPassword:// handle the set main password click event
//                /**
//                 * start the {@link SetUpLockActivity} to get the password
//                 */
//                handleSetMainPassword();
//                break;
//            case R.id.tvSetDuressPassword:// handle the set duress password click event
//                /**
//                 * start the {@link SetUpLockActivity} to get the password
//                 */
//
//                handleSetDuressPassword();
//                break;
//
//        }
//
//    }
//
//
//    private void handleSetGuestPassword() {
//
//        if (PrefUtils.getStringPref(this, KEY_GUEST_PASSWORD) == null) {
//            Intent intent = new Intent(this, SetUpLockActivity.class);
//            intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_GUEST);
//            startActivityForResult(intent, REQUEST_CODE_PASSWORD);
//        } else {
//            final EditText input = new EditText(SettingsActivity.this);
//            settingsPresenter.showAlertDialog(input, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//
//                    if (TextUtils.isEmpty(input.getText().toString().trim())) {
//                        Snackbar.make(rootLayout, R.string.please_enter_your_current_password, Snackbar.LENGTH_SHORT).show();
////                        Toast.makeText(SettingsActivity.this, R.string.please_enter_your_current_password, Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    if (input.getText().toString().
//                            equalsIgnoreCase(PrefUtils.getStringPref(SettingsActivity.this,
//                                    KEY_GUEST_PASSWORD))) {
//                        // if password is right then allow user to change it
//
//                        Intent intent = new Intent(SettingsActivity.this, SetUpLockActivity.class);
//                        intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_GUEST);
//                        startActivityForResult(intent, REQUEST_CODE_PASSWORD);
//
//                    } else {
//                        Snackbar.make(rootLayout, R.string.wrong_password_entered, Snackbar.LENGTH_SHORT).show();
//                    }
//                }
//            }, null, getString(R.string.please_enter_current_guest_password));
//        }
//
//
//    }
//}
