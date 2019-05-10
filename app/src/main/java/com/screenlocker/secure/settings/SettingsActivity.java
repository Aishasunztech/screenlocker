package com.screenlocker.secure.settings;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.snackbar.Snackbar;
import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.mdm.MainActivity;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.networkResponseModels.NetworkResponse;
import com.screenlocker.secure.permissions.SteppersActivity;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.settings.Wallpaper.WallpaperActivity;
import com.screenlocker.secure.settings.codeSetting.CodeSettingActivity;
import com.screenlocker.secure.settings.codeSetting.installApps.UpdateModel;
import com.screenlocker.secure.socket.interfaces.NetworkListener;
import com.screenlocker.secure.socket.receiver.NetworkReceiver;
import com.screenlocker.secure.socket.service.SocketService;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.updateDB.BlurWorker;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.AppInstallReciever;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PermissionUtils;
import com.screenlocker.secure.utils.PrefUtils;
import com.secureSetting.SecureSettingsMain;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static com.screenlocker.secure.launcher.MainActivity.RESULT_ENABLE;
import static com.screenlocker.secure.utils.AppConstants.CHAT_ID;
import static com.screenlocker.secure.utils.AppConstants.DB_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEFAULT_MAIN_PASS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_STATUS;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.PERMISSION_REQUEST_READ_PHONE_STATE;
import static com.screenlocker.secure.utils.AppConstants.PGP_EMAIL;
import static com.screenlocker.secure.utils.AppConstants.SIM_ID;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
import static com.screenlocker.secure.utils.AppConstants.VALUE_EXPIRED;
import static com.screenlocker.secure.utils.CommonUtils.getRemainingDays;
import static com.screenlocker.secure.utils.CommonUtils.hideKeyboard;
import static com.screenlocker.secure.utils.PermissionUtils.permissionAdmin;
import static com.screenlocker.secure.utils.PermissionUtils.permissionModify;

/***
 * this activity show the settings for the app
 * this activity is the launcher activity it means that whenever you open the app this activity will be shown
 */
public class SettingsActivity extends BaseActivity implements View.OnClickListener, SettingContract.SettingsMvpView, CompoundButton.OnCheckedChangeListener, NetworkListener {

    private AlertDialog isActiveDialog;
    private AlertDialog noNetworkDialog;

    private DevicePolicyManager devicePolicyManager;


    private ComponentName compName;

    private Toolbar mToolbar;
    /**
     * request code for the set password activity
     */
    public static final int REQUEST_CODE_PASSWORD = 883;
    private InputMethodManager imm;
    private Switch switchEnableVpn;

    private SettingsPresenter settingsPresenter;
    private ConstraintLayout rootLayout;
    private boolean isEncryptedChecked;
    private String currentVersion;
    private String mMacAddress;
    private TelephonyManager telephonyManager;
    NetworkReceiver networkReceiver;

    private TextView tvlinkDevice;


    private ConstraintLayout constraintLayout;

    private ProgressBar progressBar;

    private Dialog aboutDialog = null, accountDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        constraintLayout = findViewById(R.id.rootLayout);
        constraintLayout.setVisibility(View.GONE);
        progressBar = findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);


        OneTimeWorkRequest insertionWork =
                new OneTimeWorkRequest.Builder(BlurWorker.class)
                        .build();
        WorkManager.getInstance().enqueue(insertionWork);

        WorkManager.getInstance().getWorkInfoByIdLiveData(insertionWork.getId())
                .observe(this, workInfo -> {
                    // Do something with the status
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        PrefUtils.saveBooleanPref(SettingsActivity.this, DB_STATUS, true);
                        if (!PrefUtils.getBooleanPref(this, TOUR_STATUS)) {
                            Intent intent = new Intent(this, SteppersActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            boolean linkStatus = PrefUtils.getBooleanPref(this, DEVICE_LINKED_STATUS);
                            constraintLayout.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                            init();
                            if (!linkStatus) {
                                tvlinkDevice.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });


    }


    public void init() {
        Intent receiver = getIntent();
        if (receiver != null && receiver.hasExtra("update")){
            new AlertDialog.Builder(this)
                    .setTitle("Success")
                    .setMessage("App Updated Successfully")
                    .setPositiveButton("DISMISS", (dialog, which) -> {
                        dialog.dismiss();
                    }).show();
        }

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);
        setIds();
        setToolbar(mToolbar);
        setListeners();


        settingsPresenter = new SettingsPresenter(this, new SettingsModel(this));

        networkReceiver = new NetworkReceiver(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);

//        setSwipeToApiRequest();

        // switch change listener(on off service for vpn)
        switchEnableVpn.setOnCheckedChangeListener(this);
        createActiveDialog();
        createNoNetworkDialog();
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (manager != null) {
            mMacAddress = CommonUtils.getMacAddress();
        } else {
            mMacAddress = null;
        }


        final Intent lockScreenIntent = new Intent(SettingsActivity.this, LockScreenService.class);
        lockScreenIntent.setAction("lock screen");
        //  check for the can draw over permission ,is it enabled or not
        if (PermissionUtils.canDrawOver(SettingsActivity.this)) {
            // check for the permission to allow notification
            if (PermissionUtils.canControlNotification(SettingsActivity.this)) {
                if (PrefUtils.getStringPref(SettingsActivity.this, KEY_MAIN_PASSWORD) == null) {
                    // main password is not set
                    PrefUtils.saveStringPref(SettingsActivity.this, KEY_MAIN_PASSWORD, DEFAULT_MAIN_PASS);
                }
                if (!settingsPresenter.isServiceRunning()) {
                    settingsPresenter.startLockService(lockScreenIntent);
                }

            } else {
                // request user to allow notification for our app
                PermissionUtils.requestNotificationAccessibilityPermission(SettingsActivity.this);
            }
        } else {
            // request user to enable over lay permission for our app
            PermissionUtils.requestOverlayPermission(SettingsActivity.this);
        }


    }

    AppInstallReciever mInstallReciever;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mInstallReciever != null) {
                unregisterReceiver(mInstallReciever);
            }
            if (networkReceiver != null) {
                unregisterReceiver(networkReceiver);
            }
        } catch (Exception ignored) {
        }

    }


    @Override
    protected void onPause() {
        hideKeyboard(SettingsActivity.this);

        if (tvlinkDevice != null) {
            tvlinkDevice.setVisibility(View.GONE);
        }
        if (aboutDialog != null) {
            aboutDialog.dismiss();
        }
        if (accountDialog != null) {
            accountDialog.dismiss();
        }

        super.onPause();
    }

    private void createActiveDialog() {
        isActiveDialog = new AlertDialog.Builder(this).setMessage("").setCancelable(false).create();
    }


    private void setIds() {
        switchEnableVpn = findViewById(R.id.switchEnableVpn);
        rootLayout = findViewById(R.id.rootLayout);
        mToolbar = findViewById(R.id.toolbar);

        tvlinkDevice = findViewById(R.id.tvlinkDevice);
    }

    private void addExpiryDate() {
//if there is no data  which means user have deleted the data or its the first time so..
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            // things do here
            String imei_number = settingsPresenter.get_IMEI_number(telephonyManager);
            if (TextUtils.isEmpty(imei_number)) {
                addExpiryDate();
            } else {

                if (CommonUtils.isNetworkAvailable(this))
                    apiIsExpiryDateThereOrNot(imei_number);
                else {
                    //TODO change this
                    if (PrefUtils.getStringPref(SettingsActivity.this, AppConstants.KEY_DEVICE_ACTIVE) != null) {
                        if (PrefUtils.getStringPref(SettingsActivity.this, AppConstants.KEY_DEVICE_ACTIVE).equals(VALUE_EXPIRED)) {
                            isActiveDialog.setTitle(PrefUtils.getStringPref(this, AppConstants.KEY_DEVICE_MSG));
                            isActiveDialog.show();
                        } else {
                            if (isActiveDialog.isShowing())
                                isActiveDialog.cancel();
                        }
                    } else {
                        if (noNetworkDialog == null)
                            createNoNetworkDialog();
                        if (!noNetworkDialog.isShowing())
                            noNetworkDialog.show();
                    }


                }
            }
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                Snackbar.make(rootLayout, "We need this permission to read hardware ids to secure your device",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", view -> ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_READ_PHONE_STATE))
                        .show();
            } else {
                Snackbar.make(rootLayout, "We do not have permission.", Snackbar.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_READ_PHONE_STATE);
            }
        }


    }

    private void apiIsExpiryDateThereOrNot(final String imei_number) {
        if (mMacAddress != null) {
            ((MyApplication) getApplicationContext())
                    .getApiOneCaller()
                    .checkStatus(imei_number, mMacAddress)
                    .enqueue(new Callback<NetworkResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<NetworkResponse> call, @NonNull Response<NetworkResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                PrefUtils.saveStringPref(SettingsActivity.this, AppConstants.KEY_DEVICE_MSG, response.body().getMsg());
                                if (response.body().isStatus()) {
                                    PrefUtils.saveStringPref(SettingsActivity.this, AppConstants.KEY_DEVICE_ACTIVE, AppConstants.VALUE_ACTIVE);
                                    if (isActiveDialog.isShowing()) {
                                        isActiveDialog.cancel();
                                    }
                                } else {
                                    PrefUtils.saveStringPref(SettingsActivity.this, AppConstants.KEY_DEVICE_ACTIVE, VALUE_EXPIRED);
                                    isActiveDialog.setTitle(response.body().getMsg());
                                    if (!isActiveDialog.isShowing()) {
                                        isActiveDialog.show();
                                    }
                                }

                            } else {
                                //something went wrong
                                Snackbar.make(rootLayout, "something went wrong", Snackbar.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<NetworkResponse> call, @NonNull Throwable t) {
                            Snackbar.make(rootLayout, "something went wrong", Snackbar.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Snackbar.make(rootLayout, "failed to get macAddress", Snackbar.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_PHONE_STATE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (TextUtils.isEmpty(settingsPresenter.get_IMEI_number(telephonyManager))) {
                    addExpiryDate();
                } else {
                    if (CommonUtils.isNetworkAvailable(this))
                        apiIsExpiryDateThereOrNot(settingsPresenter.get_IMEI_number(telephonyManager));
                    else {

                        if (PrefUtils.getStringPref(SettingsActivity.this, AppConstants.KEY_DEVICE_ACTIVE) != null) {

                            if (PrefUtils.getStringPref(SettingsActivity.this, AppConstants.KEY_DEVICE_ACTIVE).equals(VALUE_EXPIRED)) {
                                isActiveDialog.setTitle(PrefUtils.getStringPref(this, AppConstants.KEY_DEVICE_MSG));
                                isActiveDialog.show();
                            } else {
                                if (isActiveDialog.isShowing())
                                    isActiveDialog.cancel();
                            }
                        } else {

                            if (noNetworkDialog == null)
                                createNoNetworkDialog();
                            if (!noNetworkDialog.isShowing())
                                noNetworkDialog.show();
                        }


                    }
                }

            } else {
                new AlertDialog.Builder(this).
                        setTitle("Permission denied")
                        .setMessage("Please allow the premission for application to run").setPositiveButton("Allow", (dialogInterface, i) -> {
                    dialogInterface.cancel();
                    addExpiryDate();
                }).show();
            }
        }


    }

    private void createNoNetworkDialog() {
        noNetworkDialog = new AlertDialog.Builder(this).setTitle("Please check your internet connection").setPositiveButton("Retry", (dialogInterface, i) -> {
            dialogInterface.cancel();
            addExpiryDate();
        }).create();
    }

    @Override
    protected void onResume() {

        boolean linkStatus = PrefUtils.getBooleanPref(this, DEVICE_LINKED_STATUS);

        if (linkStatus) {
            if (devicePolicyManager != null && compName != null) {
                permissionAdmin(SettingsActivity.this, devicePolicyManager, compName);
            }
            permissionModify(SettingsActivity.this);
            if (!PermissionUtils.canControlNotification(SettingsActivity.this)) {
                PermissionUtils.requestNotificationAccessibilityPermission(SettingsActivity.this);
            }


        } else {
            if (tvlinkDevice != null) {
                tvlinkDevice.setVisibility(View.VISIBLE);
            }
        }
        super.onResume();

    }


    private void setToolbar(Toolbar mToolbar) {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.toolbar_title);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
    }

    /**
     * set listeners
     */
    private void setListeners() {

        findViewById(R.id.tvManagePasswords).setOnClickListener(this);
        findViewById(R.id.tvChooseBackground).setOnClickListener(this);
        findViewById(R.id.tvAbout).setOnClickListener(this);
        findViewById(R.id.tvCode).setOnClickListener(this);
        findViewById(R.id.tvCheckForUpdate).setOnClickListener(this);
        findViewById(R.id.tvlinkDevice).setOnClickListener(this);
        findViewById(R.id.tvAccount).setOnClickListener(this);
        findViewById(R.id.tvAccount).setVisibility(View.VISIBLE);
        findViewById(R.id.tvAccount).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (isOverLayAllowed()) {
            switch (v.getId()) {
                case R.id.tvManagePasswords:
                    Intent passwordsIntent = new Intent(SettingsActivity.this, ManagePasswords.class);
                    startActivity(passwordsIntent);
                    break;
                case R.id.tvChooseBackground:     // handle the choose apps click event
//                    handleChooseABackground();
                    Intent cwi = new Intent(this, WallpaperActivity.class);
                    startActivity(cwi);
                    break;

                case R.id.tvCode:
                    handleCodeAdmin();
                    break;

                case R.id.tvAbout:
                    //handle the about click event
                    createAboutDialog();
                    break;
                case R.id.tvAccount:
                    createAccountDialog();
                    break;
                case R.id.tvCheckForUpdate:     //handle the about click event
                    handleCheckForUpdate();
                    //Crashlytics.getInstance().crash(); // Force a crash
                    break;
                case R.id.tvlinkDevice:

                    ConnectivityManager cm =
                            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    boolean isConnected = activeNetwork != null &&
                            activeNetwork.isConnected();

                    if (isConnected) {
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        showNetworkDialog();
                    }


                    break;
            }
        } else {
            if (!gerOverlayDialog().isShowing())
                gerOverlayDialog().show();
        }


    }


    private void handleCodeAdmin() {

        if (PrefUtils.getStringPref(this, AppConstants.KEY_CODE_PASSWORD) == null) {
            Intent intent = new Intent(this, SetUpLockActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_CODE);
            startActivityForResult(intent, REQUEST_CODE_PASSWORD);
        } else {
            final EditText input = new EditText(SettingsActivity.this);
            settingsPresenter.showAlertDialog(input, (dialogInterface, i) -> {
                if (TextUtils.isEmpty(input.getText().toString().trim())) {
//                    Snackbar.make(rootLayout, R.string.please_enter_your_current_password, Snackbar.LENGTH_SHORT).show();
                    showAlertDialog(SettingsActivity.this, "Invalid Password!", "The password you entered is incorrect.", android.R.drawable.stat_sys_warning);
                    return;
                }
                if (input.getText().toString().equalsIgnoreCase(PrefUtils.getStringPref(SettingsActivity.this, AppConstants.KEY_CODE_PASSWORD))) {
                    // start Code settings activity if the code password entered is correct

                    startActivity(new Intent(SettingsActivity.this, CodeSettingActivity.class));
                    dialogInterface.dismiss();
                } else {
                    showAlertDialog(SettingsActivity.this, "Invalid Password", "The password you entered is incorrect.", android.R.drawable.ic_dialog_alert);
                }
            }, null, getString(R.string.please_enter_code_admin_password));
        }
    }

    private void handleCheckForUpdate() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Update");
        dialog.setMessage("Checking For Updates");
        dialog.show();
        try {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (currentVersion != null)
            if (CommonUtils.isNetworkAvailable(this)) {

                ((MyApplication) getApplicationContext())
                        .getApiOneCaller()
                        .getUpdate("getUpdate/" + currentVersion + "/" + getPackageName())
                        .enqueue(new Callback<UpdateModel>() {
                            @Override
                            public void onResponse(@NonNull Call<UpdateModel> call, @NonNull Response<UpdateModel> response) {
                                dialog.dismiss();
                                if (response.body() != null) {

                                    if (response.body().isApkStatus()) {
                                        AlertDialog.Builder dialog = new AlertDialog.Builder(SettingsActivity.this)
                                                .setTitle("Update Available")
                                                .setMessage("New update available! Press OK to update your system.")
                                                .setPositiveButton("OK", (dialog12, which) -> {
                                                    String url = response.body().getApkUrl();
                                                    DownLoadAndInstallUpdate obj = new DownLoadAndInstallUpdate(SettingsActivity.this, AppConstants.STAGING_BASE_URL + "getApk/" + CommonUtils.splitName(url));
                                                    obj.execute();
                                                }).setNegativeButton("Cancel", (dialog1, which) -> {
                                                    dialog1.dismiss();
                                                });
                                        dialog.show();


                                    } else
                                        Toast.makeText(SettingsActivity.this, "you are currently up to date", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SettingsActivity.this, "you are currently up to date", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<UpdateModel> call, @NonNull Throwable t) {
                                dialog.dismiss();
                                Toast.makeText(SettingsActivity.this, "An error occurred, Please Try latter.", Toast.LENGTH_LONG).show();

                            }
                        });
            } else {
                dialog.dismiss();
                Toast.makeText(this, getString(R.string.please_check_network_connection), Toast.LENGTH_SHORT).show();
            }

    }

    private void showNetworkDialog() {

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Network Not Connected!");
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);

        alertDialog.setMessage("Please connect to the internet before proceeding.");

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "NETWORK SETUP", (dialog, which) -> {
            Intent intent = new Intent(SettingsActivity.this, SecureSettingsMain.class);
            intent.putExtra("show_default", "show_default");
            startActivity(intent);

        });


        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();

    }

    private void showAlertDialog(AppCompatActivity activity, String title, String msg, int icon) {

        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        alertDialog.setIcon(icon);
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();

    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, final boolean isChecked) {

    }


    @Override
    public void onNetworkChange(boolean status) {


        if (PrefUtils.getBooleanPref(SettingsActivity.this, DEVICE_LINKED_STATUS)) {

            Intent intent = new Intent(this, SocketService.class);
            if (status) {
                String macAddress = CommonUtils.getMacAddress();
                String serialNo = DeviceIdUtils.getSerialNumber();
                if (macAddress != null && serialNo != null) {
                    new ApiUtils(SettingsActivity.this, macAddress, serialNo);
                }
            } else {
                stopService(intent);
                Snackbar.make(rootLayout, "no internet", Snackbar.LENGTH_SHORT).show();
            }

        }

    }


    private void createAboutDialog() {
//        about device dialog

        aboutDialog = new Dialog(this);
        aboutDialog.setContentView(R.layout.dialog_about_background);
        WindowManager.LayoutParams params = Objects.requireNonNull(aboutDialog.getWindow()).getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        aboutDialog.getWindow().setAttributes(params);
        aboutDialog.setCancelable(true);

        // Version Code
        TextView tvVersionCode = aboutDialog.findViewById(R.id.tvVersionCode);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersionCode.setText(String.format("v%s", String.valueOf(pInfo.versionName)));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            tvVersionCode.setText("");
        }

        /*// Expiry Date
        TextView tvExpiresIn = aboutDialog.findViewById(R.id.tvExpiresIn);
        TextView textView16 = aboutDialog.findViewById(R.id.textView16);

        String remaining_days = getRemainingDays(SettingsActivity.this);

        if (remaining_days != null) {
            textView16.setVisibility(View.VISIBLE);
            tvExpiresIn.setVisibility(View.VISIBLE);
            tvExpiresIn.setText(remaining_days);
//            else {
//                suspendedDevice(SettingsActivity.this, this, device_id, "expired");
//            }
        }*/

        /*// Device ID
        TextView tvDeviceId = aboutDialog.findViewById(R.id.tvDeviceId);
        TextView textView17 = aboutDialog.findViewById(R.id.textView17);
        String device_id = PrefUtils.getStringPref(SettingsActivity.this, DEVICE_ID);
        if (device_id != null) {
            tvDeviceId.setVisibility(View.VISIBLE);
            textView17.setVisibility(View.VISIBLE);
            tvDeviceId.setText(device_id);
        }
*/
        // PGP Email
        TextView tvPgpEmail = aboutDialog.findViewById(R.id.tvPgpEmail);
        TextView textView18 = aboutDialog.findViewById(R.id.textView18);
        String pgpEmail = PrefUtils.getStringPref(SettingsActivity.this, PGP_EMAIL);
        if (pgpEmail != null) {
            textView18.setVisibility(View.VISIBLE);
            tvPgpEmail.setVisibility(View.VISIBLE);
            tvPgpEmail.setText(pgpEmail);
        }

        // Chat ID
        TextView tvChatId = aboutDialog.findViewById(R.id.tvChatId);
        TextView textView19 = aboutDialog.findViewById(R.id.textView19);
        String chatId = PrefUtils.getStringPref(SettingsActivity.this, CHAT_ID);
        if (chatId != null) {
            textView19.setVisibility(View.VISIBLE);
            tvChatId.setVisibility(View.VISIBLE);
            tvChatId.setText(chatId);
        }
        // Sim ID
        TextView tvSimId = aboutDialog.findViewById(R.id.tvSimId);
        TextView textView20 = aboutDialog.findViewById(R.id.textView20);
        String simId = PrefUtils.getStringPref(SettingsActivity.this, SIM_ID);
        if (simId != null) {
            textView20.setVisibility(View.VISIBLE);
            tvSimId.setVisibility(View.VISIBLE);
            tvSimId.setText(simId);
        }


        aboutDialog.show();

    }

    private void createAccountDialog() {
//        account device dialog

        accountDialog = new Dialog(this);
        accountDialog.setContentView(R.layout.dialoge_account);
        WindowManager.LayoutParams params = Objects.requireNonNull(accountDialog.getWindow()).getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        accountDialog.getWindow().setAttributes(params);
        accountDialog.setCancelable(true);

        // Device ID
        TextView tvDeviceId = accountDialog.findViewById(R.id.tvDeviceId);
        TextView textView17 = accountDialog.findViewById(R.id.textViewDeviceId);
        String device_id = PrefUtils.getStringPref(SettingsActivity.this, DEVICE_ID);
        if (device_id != null) {
            tvDeviceId.setVisibility(View.VISIBLE);
            textView17.setVisibility(View.VISIBLE);
            tvDeviceId.setText(device_id);
        }

        /*Status*/
        TextView tvStatus = accountDialog.findViewById(R.id.tvDeviceStatus);
        TextView textView18 = accountDialog.findViewById(R.id.textViewStatus);
        String device_status = PrefUtils.getStringPref(SettingsActivity.this, DEVICE_STATUS);
        boolean b = PrefUtils.getBooleanPref(SettingsActivity.this, DEVICE_LINKED_STATUS);
        if (b) {
            tvStatus.setVisibility(View.VISIBLE);
            textView18.setVisibility(View.VISIBLE);

            if (device_status == null) {
                tvStatus.setText("Active");
            } else
                tvStatus.setText(device_status);
        }


        // Expiry Date
        TextView tvExpiresIn = accountDialog.findViewById(R.id.tvExpiresIn);
        TextView textView16 = accountDialog.findViewById(R.id.textViewExpiry);

        String remaining_days = getRemainingDays(SettingsActivity.this);

        if (remaining_days != null) {
            textView16.setVisibility(View.VISIBLE);
            tvExpiresIn.setVisibility(View.VISIBLE);
            tvExpiresIn.setText(remaining_days);
//            else {
//                suspendedDevice(SettingsActivity.this, this, device_id, "expired");
//            }
        }


        List<String> imeis = DeviceIdUtils.getIMEI(SettingsActivity.this);

        // IMEI 1
        TextView tvImei1 = accountDialog.findViewById(R.id.tvImei1);
        TextView textViewImei = accountDialog.findViewById(R.id.textViewImei);

        if (imeis.size() > 0) {
            String imei = imeis.get(0);
            if (imei != null) {
                tvImei1.setVisibility(View.VISIBLE);
                textViewImei.setVisibility(View.VISIBLE);
                tvImei1.setText(imei);
            }
        }

        // IMEI 2
        TextView tvImei2 = accountDialog.findViewById(R.id.tvImei2);
        TextView textViewImei2 = accountDialog.findViewById(R.id.textViewImei2);

        if (imeis.size() > 1) {
            String imei2 = imeis.get(1);
            if (imei2 != null) {
                tvImei2.setVisibility(View.VISIBLE);
                textViewImei2.setVisibility(View.VISIBLE);
                tvImei2.setText(imei2);
            }
        }


        accountDialog.show();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    PrefUtils.saveBooleanPref(this, AppConstants.KEY_ADMIN_ALLOWED, true);
                    Toast.makeText(SettingsActivity.this, "You have enabled the Admin Device features", Toast.LENGTH_SHORT).show();
                } else {
                    PrefUtils.saveBooleanPref(this, AppConstants.KEY_ADMIN_ALLOWED, false);
                    Toast.makeText(SettingsActivity.this, "Problem to enable the Admin Device features", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_CODE_PASSWORD:
                if (resultCode == RESULT_OK) {
                    showAlertDialog(SettingsActivity.this, "Password Changed!", "Password Successfully Changed.", R.drawable.ic_checked);
//                    Snackbar.make(rootLayout, R.string.password_changed, Snackbar.LENGTH_SHORT).show();
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    Timber.e("onActivityResult: BG_CHANGER : %s", resultUri);
                    if (isEncryptedChecked) {
                        Toast.makeText(this, "Background saved for encrypted", Toast.LENGTH_SHORT).show();
                        PrefUtils.saveStringPref(SettingsActivity.this, AppConstants.KEY_MAIN_IMAGE, resultUri.toString());

                    } else {
                        Toast.makeText(this, "Background saved for guest", Toast.LENGTH_SHORT).show();
                        PrefUtils.saveStringPref(SettingsActivity.this, AppConstants.KEY_GUEST_IMAGE, resultUri.toString());
                    }
                }
//                 else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                    //Exception error = result.getError();
//                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onBackPressed() {

        try {
            if (settingsPresenter.isMyLauncherDefault()) {
                Intent home = new Intent(SettingsActivity.this, com.screenlocker.secure.launcher.MainActivity.class);
                startActivity(home);
                finish();
            } else {
                super.onBackPressed();
            }
        } catch (Exception ignored) {
        }


    }

    private static class DownLoadAndInstallUpdate extends AsyncTask<Void, Integer, Uri> {
        private String appName, url;
        private WeakReference<Context> contextWeakReference;
        private ProgressDialog dialog;

        DownLoadAndInstallUpdate(Context context, final String url) {
            contextWeakReference = new WeakReference<>(context);
            this.url = url;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(contextWeakReference.get());
            dialog.setTitle("Downloading Update, Please Wait");
            dialog.setCancelable(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.show();
        }

        @Override
        protected Uri doInBackground(Void... voids) {
            return downloadApp();
        }


        private Uri downloadApp() {
            FileOutputStream fileOutputStream = null;
            InputStream input = null;
            try {
                appName = new Date().getTime() + ".apk";
                File apksPath = new File(contextWeakReference.get().getFilesDir(), "apk");
                File file = new File(apksPath, appName);
                if (!apksPath.exists()) {
                    apksPath.mkdir();
                }

                try {
                    fileOutputStream = new FileOutputStream(file);

                    URL downloadUrl = new URL(url);
                    URLConnection connection = downloadUrl.openConnection();
                    int contentLength = connection.getContentLength();

                    // input = body.byteStream();
                    input = new BufferedInputStream(downloadUrl.openStream());
                    byte data[] = new byte[contentLength];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        publishProgress((int) ((total * 100) / contentLength));
                        fileOutputStream.write(data, 0, count);
                    }
                    Uri contentUri = FileProvider.getUriForFile(contextWeakReference.get(), "com.vortexlocker.app.fileprovider", file);

                    //Uri uri =  FileProvider.getUriForFile(contextWeakReference.get(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
                    Log.d("downloadApp", "downloadApp: "+contentUri.toString());
                    return contentUri;


                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    if (fileOutputStream != null) {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                    if (input != null)
                        input.close();
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            dialog.setProgress(values[0]);
//            tvProgressText.setText(String.valueOf(values[0]));
        }

        @Override
        protected void onPostExecute(Uri uri) {
            super.onPostExecute(uri);
            if (dialog != null)
                dialog.dismiss();
            if (uri != null) {
                showInstallDialog(uri);
            }else {
                Toast.makeText(contextWeakReference.get(), "Some Error Occured", Toast.LENGTH_SHORT).show();
            }

        }

        private void showInstallDialog(Uri apkUri) {


//            contextWeakReference.get().grantUriPermission("com.secure.systemcontrol",apkUri,
//                    FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//
//            contextWeakReference.get().revokeUriPermission(apkUri,FLAG_GRANT_READ_URI_PERMISSION |Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Intent launchIntent = contextWeakReference.get().getPackageManager().getLaunchIntentForPackage("com.secure.systemcontrol");
//                        launchIntent.setAction(Intent.ACTION_VIEW);
            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            launchIntent.setData(apkUri);
            launchIntent.putExtra("package", contextWeakReference.get().getPackageName());
            launchIntent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
//            contextWeakReference.get().sendBroadcast(sender);

          contextWeakReference.get().startActivity(launchIntent);
        }

        private IntentSender createIntentSender(int sessionId) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:0123456789"));
            PendingIntent pendingIntent = PendingIntent.getActivity(contextWeakReference.get(), sessionId, intent, 0);
            return pendingIntent.getIntentSender();
        }
    }


}
