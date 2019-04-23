package com.screenlocker.secure.settings;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.snackbar.Snackbar;
import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.mdm.MainActivity;
import com.screenlocker.secure.networkResponseModels.NetworkResponse;
import com.screenlocker.secure.permissions.SteppersActivity;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.settings.codeSetting.CodeSettingActivity;
import com.screenlocker.secure.settings.codeSetting.installApps.UpdateModel;
import com.screenlocker.secure.socket.interfaces.DatabaseStatus;
import com.screenlocker.secure.socket.interfaces.NetworkListener;
import com.screenlocker.secure.socket.interfaces.RefreshListener;
import com.screenlocker.secure.socket.receiver.NetworkReceiver;
import com.screenlocker.secure.socket.service.SocketService;
import com.screenlocker.secure.updateDB.BlurWorker;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.AppInstallReciever;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PermissionUtils;
import com.screenlocker.secure.utils.PrefUtils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

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
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.launcher.MainActivity.RESULT_ENABLE;
import static com.screenlocker.secure.utils.AppConstants.CHAT_ID;
import static com.screenlocker.secure.utils.AppConstants.DB_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEFAULT_MAIN_PASS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
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

    private static RefreshListener listener;
    private static DatabaseStatus listener1;
    private TextView tvlinkDevice;

    public void setDatabaseStatus(DatabaseStatus databaseStatus) {
        listener1 = databaseStatus;
    }

    public void setRefreshListener(RefreshListener refreshListener) {
        listener = refreshListener;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fade_in, R.anim.fasdein);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        OneTimeWorkRequest insertionWork =
                new OneTimeWorkRequest.Builder(BlurWorker.class)
                        .build();


        WorkManager.getInstance().enqueue(insertionWork);

        WorkManager.getInstance().getWorkInfoByIdLiveData(insertionWork.getId())
                .observe(this, workInfo -> {
                    // Do something with the status
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        Timber.d("work completed");
                        if (listener1 != null) {
                            listener1.onDataInserted();
                        }
                        PrefUtils.saveBooleanPref(this, DB_STATUS, true);
                    }
                });


        if (!PrefUtils.getBooleanPref(this, TOUR_STATUS)) {
            Intent intent = new Intent(this, SteppersActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        init();

        boolean linkStatus = PrefUtils.getBooleanPref(SettingsActivity.this, DEVICE_LINKED_STATUS);

//        if (linkStatus && networkStatus) {
//            final Intent intent = new Intent(this, SocketService.class);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                intent.setAction("refresh");
//                startForegroundService(intent);
//            } else {
//                intent.setAction("refresh");
//                startService(intent);
//            }
//        }
    }


    public void init() {

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

        setSwipeToApiRequest();

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

        super.onPause();
    }

    private void createActiveDialog() {
        isActiveDialog = new AlertDialog.Builder(this).setMessage("").setCancelable(false).create();
    }


    private SwipeRefreshLayout swipeToApiRequest;


    private void setIds() {
        switchEnableVpn = findViewById(R.id.switchEnableVpn);
        rootLayout = findViewById(R.id.rootLayout);
        mToolbar = findViewById(R.id.toolbar);
        swipeToApiRequest = findViewById(R.id.swipe_to_api_request);
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
        super.onResume();
        boolean linkStatus = PrefUtils.getBooleanPref(this, DEVICE_LINKED_STATUS);
        if (linkStatus) {
            if (devicePolicyManager != null && compName != null) {
                permissionAdmin(SettingsActivity.this, devicePolicyManager, compName);
            }
            permissionModify(SettingsActivity.this);
            if (!PermissionUtils.canControlNotification(SettingsActivity.this)) {
                PermissionUtils.requestNotificationAccessibilityPermission(SettingsActivity.this);
            }

            if (tvlinkDevice != null) {
                tvlinkDevice.setVisibility(View.GONE);
            }

            if (networkStatus) {
                Intent intent = new Intent(this, SocketService.class);
                if (networkStatus) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        intent.setAction("refresh");
                        startForegroundService(intent);
                    } else {
                        intent.setAction("refresh");
                        startService(intent);
                    }
                } else {
                    stopService(intent);
                    Snackbar.make(rootLayout, "no internet", Snackbar.LENGTH_SHORT).show();
                }

            }


        }


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
                    handleChooseABackground();
                    break;

                case R.id.tvCode:
                    handleCodeAdmin();
                    break;

                case R.id.tvAbout:
                    //handle the about click event
                    createAboutDialog();
                    break;
                case R.id.tvCheckForUpdate:     //handle the about click event
                    handleCheckForUpdate();
                    //Crashlytics.getInstance().crash(); // Force a crash
                    break;
                case R.id.tvlinkDevice:
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
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
                    Snackbar.make(rootLayout, R.string.please_enter_your_current_password, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (input.getText().toString().equalsIgnoreCase(PrefUtils.getStringPref(SettingsActivity.this, AppConstants.KEY_CODE_PASSWORD))) {
                    // start Code settings activity if the code password entered is correct

                    startActivity(new Intent(SettingsActivity.this, CodeSettingActivity.class));
                    dialogInterface.dismiss();
                } else {
                    Snackbar.make(rootLayout, R.string.wrong_password_entered, Snackbar.LENGTH_SHORT).show();
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
                                                    DownLoadAndInstallUpdate obj = new DownLoadAndInstallUpdate(SettingsActivity.this, AppConstants.STAGING_BASE_URL + "/getApk/" + CommonUtils.splitName(url));
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
                                Toast.makeText(SettingsActivity.this, "An error occurred, Please Try latter.", Toast.LENGTH_LONG).show();

                            }
                        });
            } else {
                Toast.makeText(this, getString(R.string.please_check_network_connection), Toast.LENGTH_SHORT).show();
            }

    }


    public void handleSetMainPassword(AppCompatActivity activity, View rootLayout) {

        if (PrefUtils.getStringPref(activity, KEY_MAIN_PASSWORD) == null) {
            Intent i = new Intent(activity, SetUpLockActivity.class);
            i.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_MAIN);
            activity.startActivityForResult(i, REQUEST_CODE_PASSWORD);
        } else {
            final EditText input = new EditText(activity);
            if (settingsPresenter == null) {
                settingsPresenter = new SettingsPresenter(new SettingContract.SettingsMvpView() {
                    @Override
                    public int hashCode() {
                        return super.hashCode();
                    }
                }, new SettingsModel(activity));

            }
            settingsPresenter.showAlertDialog(input, (dialogInterface, i) -> {

                if (TextUtils.isEmpty(input.getText().toString().trim())) {
                    Snackbar.make(rootLayout, R.string.please_enter_your_current_password, Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (input.getText().toString().
                        equalsIgnoreCase(PrefUtils.getStringPref(activity,
                                KEY_MAIN_PASSWORD))) {
                    // if password is right then allow user to change it
                    Intent setUpLockActivityIntent = new Intent(activity, SetUpLockActivity.class);
                    setUpLockActivityIntent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_MAIN);
                    activity.startActivityForResult(setUpLockActivityIntent, REQUEST_CODE_PASSWORD);

                } else {
                    Snackbar.make(rootLayout, R.string.wrong_password_entered, Snackbar.LENGTH_SHORT).show();
//                        Toast.makeText(SettingsActivity.this, R.string.wrong_password_entered, Toast.LENGTH_SHORT).show();
                }
            }, null, activity.getString(R.string.please_enter_current_encrypted_password));
        }

    }

    public void handleSetDuressPassword(AppCompatActivity activity, View rootLayout) {
        if (PrefUtils.getStringPref(activity, AppConstants.KEY_DURESS_PASSWORD) == null) {
            new AlertDialog.Builder(activity).
                    setTitle("Warning!")
                    .setMessage("Entering Duress Pin when device is locked will wipe your phone data. You cannot undo this action. All data will be deleted from target device without any confirmation. There is no way to reverse this action.").setPositiveButton("Ok", (dialogInterface, i) -> {
                Intent intent = new Intent(activity, SetUpLockActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_DURESS);
                activity.startActivityForResult(intent, REQUEST_CODE_PASSWORD);
            })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel())
                    .show();

        } else {
            final EditText input = new EditText(activity);
            if (settingsPresenter == null) {
                settingsPresenter = new SettingsPresenter(new SettingContract.SettingsMvpView() {
                    @Override
                    public int hashCode() {
                        return super.hashCode();
                    }
                }, new SettingsModel(activity));

            }
            settingsPresenter.showAlertDialog(input, (dialogInterface, i) -> {

                if (TextUtils.isEmpty(input.getText().toString().trim())) {
                    Snackbar.make(rootLayout, R.string.please_enter_your_current_password, Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (input.getText().toString().
                        equalsIgnoreCase(PrefUtils.getStringPref(activity,
                                AppConstants.KEY_DURESS_PASSWORD))) {
                    // if password is right then allow user to change it
                    Intent setUpLockActivityIntent = new Intent(activity, SetUpLockActivity.class);
                    setUpLockActivityIntent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_DURESS);
                    activity.startActivityForResult(setUpLockActivityIntent, REQUEST_CODE_PASSWORD);

                } else {
                    Snackbar.make(rootLayout, R.string.wrong_password_entered, Snackbar.LENGTH_SHORT).show();
//                        Toast.makeText(SettingsActivity.this, R.string.wrong_password_entered, Toast.LENGTH_SHORT).show();
                }
            }, null, activity.getString(R.string.please_enter_current_duress_password));
        }
    }

    public void handleSetGuestPassword(AppCompatActivity activity, View rootLayout) {

        if (PrefUtils.getStringPref(activity, KEY_GUEST_PASSWORD) == null) {
            Intent intent = new Intent(activity, SetUpLockActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_GUEST);
            activity.startActivityForResult(intent, REQUEST_CODE_PASSWORD);
        } else {
            final EditText input = new EditText(activity);

            if (settingsPresenter == null) {
                settingsPresenter = new SettingsPresenter(new SettingContract.SettingsMvpView() {
                    @Override
                    public int hashCode() {
                        return super.hashCode();
                    }
                }, new SettingsModel(activity));

            }

            settingsPresenter.showAlertDialog(input, (dialogInterface, i) -> {

                if (TextUtils.isEmpty(input.getText().toString().trim())) {
                    Snackbar.make(rootLayout, R.string.please_enter_your_current_password, Snackbar.LENGTH_SHORT).show();
//                        Toast.makeText(SettingsActivity.this, R.string.please_enter_your_current_password, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (input.getText().toString().
                        equalsIgnoreCase(PrefUtils.getStringPref(activity,
                                KEY_GUEST_PASSWORD))) {
                    // if password is right then allow user to change it

                    Intent intent = new Intent(activity, SetUpLockActivity.class);
                    intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_GUEST);
                    activity.startActivityForResult(intent, REQUEST_CODE_PASSWORD);

                } else {
                    Snackbar.make(rootLayout, activity.getString(R.string.wrong_password_entered), Snackbar.LENGTH_SHORT).show();
                }
            }, null, activity.getResources().getString(R.string.please_enter_current_guest_password));
        }


    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, final boolean isChecked) {

        switch (compoundButton.getId()) {

            case R.id.switchEnableVpn:

                if (isChecked) {

                    Toast.makeText(this, "unckeked", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "unckeked", Toast.LENGTH_SHORT).show();

                }

                break;
        }
    }

    private boolean networkStatus = false;

    private void setSwipeToApiRequest() {

        final Intent intent = new Intent(this, SocketService.class);

        swipeToApiRequest.setOnRefreshListener(() -> {
            if (networkStatus) {
                boolean linkStatus = PrefUtils.getBooleanPref(this, DEVICE_LINKED_STATUS);
                if (linkStatus) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        intent.setAction("restart");
                        startForegroundService(intent);
                    } else {
                        intent.setAction("restart");
                        startService(intent);
                    }
                    if (listener != null) {
                        listener.onSwipe();
                    }
                }

            } else {
                stopService(intent);
                Snackbar.make(rootLayout, "no internet", Snackbar.LENGTH_SHORT).show();
            }

            swipeToApiRequest.setRefreshing(false);
        });
    }

    @Override
    public void onNetworkChange(boolean status) {
        networkStatus = status;

        boolean linkStatus = PrefUtils.getBooleanPref(this, DEVICE_LINKED_STATUS);

        if (linkStatus) {
            Intent intent = new Intent(this, SocketService.class);
            if (networkStatus) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    intent.setAction("refresh");
                    startForegroundService(intent);
                } else {
                    intent.setAction("refresh");
                    startService(intent);
                }
            } else {
                stopService(intent);
                Snackbar.make(rootLayout, "no internet", Snackbar.LENGTH_SHORT).show();
            }
        }
    }


    private void createAboutDialog() {
//        about device dialog

        Dialog aboutDialog = new Dialog(this);
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

        // Expiry Date
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
        }

        // Device ID
        TextView tvDeviceId = aboutDialog.findViewById(R.id.tvDeviceId);
        TextView textView17 = aboutDialog.findViewById(R.id.textView17);
        String device_id = PrefUtils.getStringPref(SettingsActivity.this, DEVICE_ID);
        if (device_id != null) {
            tvDeviceId.setVisibility(View.VISIBLE);
            textView17.setVisibility(View.VISIBLE);
            tvDeviceId.setText(device_id);
        }

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

    /**
     * handle the background image setup for the pin locker
     */
    private void handleChooseABackground() {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_choose_background);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(params);

        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        //dialog.setCancelable(false);
        RadioGroup rgUserType = dialog.findViewById(R.id.rgUserType);
        RadioButton rbGuest = dialog.findViewById(R.id.rbGuest);
        RadioButton rbEncrypted = dialog.findViewById(R.id.rbEncrypted);
        final EditText etPassword = dialog.findViewById(R.id.etPassword);
        if (isEncryptedChecked) {
            rbEncrypted.setChecked(true);


        } else {
            rbGuest.setChecked(true);

        }

        rgUserType.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            switch (checkedId) {
                case R.id.rbGuest:
                    isEncryptedChecked = false;
                    break;
                case R.id.rbEncrypted:
                    isEncryptedChecked = true;
                    break;
            }
        });
        dialog.findViewById(R.id.btOk).setOnClickListener(view -> {

            String enteredPassword = etPassword.getText().toString().toLowerCase();
            if (TextUtils.isEmpty(enteredPassword)) {
                Toast.makeText(SettingsActivity.this, "Please enter your Password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEncryptedChecked) {
                if (PrefUtils.getStringPref(SettingsActivity.this, KEY_MAIN_PASSWORD) == null) {
                    Toast.makeText(SettingsActivity.this, "Please set Encrypted Password First.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (enteredPassword.equals(PrefUtils.getStringPref(SettingsActivity.this, KEY_MAIN_PASSWORD))) {
                    // allow him to set background for encrypted user
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(SettingsActivity.this);
                } else {
                    // dont allow him to set background because password was wrong
                    Toast.makeText(SettingsActivity.this, "Entered Wrong Password.", Toast.LENGTH_SHORT).show();
                }

            } else {
                if (PrefUtils.getStringPref(SettingsActivity.this, KEY_GUEST_PASSWORD) == null) {
                    Toast.makeText(SettingsActivity.this, "Please set Guest Password First.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (enteredPassword.equals(PrefUtils.getStringPref(SettingsActivity.this, KEY_GUEST_PASSWORD))) {
                    // allow him to set background for encrypted user

                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(SettingsActivity.this);
                } else {
                    // dont allow him to set background because password was wrong
                    Toast.makeText(SettingsActivity.this, "Entered Wrong Password.", Toast.LENGTH_SHORT).show();
                }

            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            dialog.cancel();
        });
        dialog.findViewById(R.id.btCancel).setOnClickListener(view -> {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            dialog.cancel();
        });
        dialog.show();
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
                    Snackbar.make(rootLayout, R.string.password_changed, Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(rootLayout, R.string.password_not_changed, Snackbar.LENGTH_SHORT).show();
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

        if (settingsPresenter.isMyLauncherDefault()) {
            Intent home = new Intent(SettingsActivity.this, com.screenlocker.secure.launcher.MainActivity.class);
            startActivity(home);
            finish();
        } else {
            super.onBackPressed();
        }


    }

    private static class DownLoadAndInstallUpdate extends AsyncTask<Void, Integer, Boolean> {
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
        protected Boolean doInBackground(Void... voids) {
            return downloadApp();
        }


        private Boolean downloadApp() {
            FileOutputStream fileOutputStream = null;
            InputStream input = null;
            try {
//                File file = contextWeakReference.get().getFileStreamPath(appName);

                appName = new Date().getTime() + ".apk";

                try {
                    fileOutputStream = contextWeakReference.get().openFileOutput(appName, MODE_PRIVATE);

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

                    return true;

                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
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
            return false;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            dialog.setProgress(values[0]);
//            tvProgressText.setText(String.valueOf(values[0]));
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (dialog != null)
                dialog.dismiss();
            if (aBoolean) {
                showInstallDialog(appName);
            }

        }

        private void showInstallDialog(String appName) {
            File f = contextWeakReference.get().getFileStreamPath(appName);
            /*try {
                installPackage(appName);
            } catch (IOException e) {
                Log.d("dddddgffdgg", "showInstallDialog: "+e.getMessage());;
            }*/
            Uri apkUri = FileProvider.getUriForFile(contextWeakReference.get(), BuildConfig.APPLICATION_ID, f);

            Intent intent = ShareCompat.IntentBuilder.from((Activity) contextWeakReference.get())
                    .setStream(apkUri) // uri from FileProvider
                    .setType("text/html")
                    .getIntent()
                    .setAction(Intent.ACTION_VIEW) //Change if needed
                    .setDataAndType(apkUri, "application/vnd.android.package-archive")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            contextWeakReference.get().startActivity(intent);
        }

        private void installPackage(String inputStream)
                throws IOException {

            PackageInstaller packageInstaller = contextWeakReference.get().getPackageManager().getPackageInstaller();
            int sessionId = packageInstaller.createSession(new PackageInstaller
                    .SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL));
            PackageInstaller.Session session = packageInstaller.openSession(sessionId);

            long sizeBytes = 0;
            InputStream inputStream1 = new FileInputStream(inputStream);

            OutputStream out = null;
            out = session.openWrite("my_app_session", 0, sizeBytes);

            int total = 0;
            byte[] buffer = new byte[65536];
            int c;
            while ((c = inputStream1.read(buffer)) != -1) {
                total += c;
                out.write(buffer, 0, c);
            }
            session.fsync(out);
            inputStream1.close();
            out.close();

            session.commit(createIntentSender(sessionId));
        }

        private IntentSender createIntentSender(int sessionId) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:0123456789"));
            PendingIntent pendingIntent = PendingIntent.getActivity(contextWeakReference.get(), sessionId, intent, 0);
            return pendingIntent.getIntentSender();
        }
    }


}
