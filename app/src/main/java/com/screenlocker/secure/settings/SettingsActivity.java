package com.screenlocker.secure.settings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
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

import com.screenlocker.secure.socket.model.Settings;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.screenlocker.secure.MyAdmin;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.mdm.MainActivity;
import com.screenlocker.secure.networkResponseModels.NetworkResponse;
import com.screenlocker.secure.permissions.StepperActivity;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.settings.codeSetting.CodeSettingActivity;
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

import org.jsoup.Jsoup;

import java.util.Objects;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.launcher.MainActivity.RESULT_ENABLE;
import static com.screenlocker.secure.utils.AppConstants.CHAT_ID;
import static com.screenlocker.secure.utils.AppConstants.CODE_WRITE_SETTINGS_PERMISSION;
import static com.screenlocker.secure.utils.AppConstants.DB_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEFAULT_MAIN_PASS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.PERMISSION_REQUEST_READ_PHONE_STATE;
import static com.screenlocker.secure.utils.AppConstants.PGP_EMAIL;
import static com.screenlocker.secure.utils.AppConstants.REQUEST_READ_PHONE_STATE;
import static com.screenlocker.secure.utils.AppConstants.SIM_ID;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
import static com.screenlocker.secure.utils.AppConstants.VALUE_EXPIRED;
import static com.screenlocker.secure.utils.CommonUtils.hideKeyboard;
import static com.screenlocker.secure.utils.PermissionUtils.isPermissionGranted;
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
    private static final int REQUEST_CODE_PASSWORD = 883;
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

    public static Context setting_context = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        setting_context = SettingsActivity.this;

//        ApplicationInfo ai = null;
//        try {
//            ai = getPackageManager().getApplicationInfo("com.android.launcher3", 0);
//            boolean appStatus = ai.enabled;
//            Log.d("kjdjjsnv", "onCreate: " + ai.packageName);
//            Log.d("kjdjjsnv", "onCreate: " + appStatus);
//
//            if (appStatus) {
//                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                intent.setData(Uri.parse("package:" + ai.packageName));
//                startActivity(intent);
//            }
//
//        } catch (Exception e) {
//            Log.d("kjdjjsnv", "onCreate: " + e.getMessage());
//        }

        try {
            sendBroadcast(new Intent().setAction("com.mediatek.ppl.NOTIFY_LOCK"));
        } catch (Exception ignored) {
        }


        boolean tourStatus = PrefUtils.getBooleanPref(this, TOUR_STATUS);
        if (!tourStatus) {
            Intent intent = new Intent(this, StepperActivity.class);
            startActivity(intent);
            finish();
            return;
        }


        init();

        boolean linkStatus = PrefUtils.getBooleanPref(SettingsActivity.this, DEVICE_LINKED_STATUS);
        if (linkStatus && networkStatus) {
            final Intent intent = new Intent(this, SocketService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction("refresh");
                startForegroundService(intent);
            } else {
                intent.setAction("refresh");
                startService(intent);
            }
        }
    }


    public void init() {

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);
        setIds();
        setToolbar(mToolbar);
        setListeners();

//        String default_main_pass = PrefUtils.getStringPref(this, KEY_MAIN_PASSWORD);
//        String default_guest_pass = PrefUtils.getStringPref(this, KEY_GUEST_PASSWORD);
//
//        if (default_main_pass == null) {
//            PrefUtils.saveStringPref(this, KEY_MAIN_PASSWORD, DEFAULT_MAIN_PASS);
//        }
//        if (default_guest_pass == null) {
//            PrefUtils.saveStringPref(this, KEY_GUEST_PASSWORD, DEFAULT_GUEST_PASS);
//        }


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

//        if (!PermissionUtils.canControlNotification(SettingsActivity.this)) {
//            PermissionUtils.requestNotificationAccessibilityPermission(SettingsActivity.this);
//        }

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
                            listener1.onAppsInserted();
                        }
                        PrefUtils.saveBooleanPref(this, DB_STATUS, true);
                    }
                });


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
        if (mInstallReciever != null) {
            unregisterReceiver(mInstallReciever);
        }
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
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

    // TODO REMOVE PREFS FOR START DATE AND END DATE
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
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(SettingsActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_READ_PHONE_STATE);
                            }
                        })
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
                        public void onResponse(Call<NetworkResponse> call, Response<NetworkResponse> response) {
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
                        public void onFailure(Call<NetworkResponse> call, Throwable t) {
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
                        .setMessage("Please allow the premission for application to run").setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        addExpiryDate();
                    }
                }).show();
            }
        }
        if (requestCode == REQUEST_READ_PHONE_STATE) {
            Log.d("permissiond", "REQUEST_READ_PHONE_STATE");
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == CODE_WRITE_SETTINGS_PERMISSION) {
            Log.d("permissiond", "CODE_WRITE_SETTINGS_PERMISSION");
        }

    }

    private void createNoNetworkDialog() {
        noNetworkDialog = new AlertDialog.Builder(this).setTitle("Please check your internet connection").setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                addExpiryDate();
            }
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
            if (isPermissionGranted(SettingsActivity.this)) {
                Toast.makeText(this, "granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "not granted", Toast.LENGTH_SHORT).show();
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
                } else {
                    Snackbar.make(rootLayout, R.string.wrong_password_entered, Snackbar.LENGTH_SHORT).show();
                }
            }, null, getString(R.string.please_enter_code_admin_password));
        }
    }

    private void handleCheckForUpdate() {
        try {
            currentVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (currentVersion != null)
            new GetVersionCode().execute();

    }


    public void handleSetMainPassword(Activity activity, View rootLayout) {

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
            settingsPresenter.showAlertDialog(input, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

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
                }
            }, null, activity.getString(R.string.please_enter_current_encrypted_password));
        }

    }

    public void handleSetDuressPassword(Activity activity, View rootLayout) {
        if (PrefUtils.getStringPref(activity, AppConstants.KEY_DURESS_PASSWORD) == null) {
            new AlertDialog.Builder(activity).
                    setTitle("Warning!")
                    .setMessage("Entering Duress Pin when device is locked will wipe your phone data. You cannot undo this action. All data will be deleted from target device without any confirmation. There is no way to reverse this action.").setPositiveButton("Ok", (dialogInterface, i) -> {
                Intent intent = new Intent(activity, SetUpLockActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_DURESS);
                activity.startActivityForResult(intent, REQUEST_CODE_PASSWORD);
            })
                    .setNegativeButton("Cancel", (dialogInterface, i) -> {
                        dialogInterface.cancel();
                    })
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
            settingsPresenter.showAlertDialog(input, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

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
                }
            }, null, activity.getString(R.string.please_enter_current_duress_password));
        }
    }

    public void handleSetGuestPassword(Activity activity, View rootLayout) {

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
        Log.d("networkStatus", "onNetworkChange: " + status);
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


    private class GetVersionCode extends AsyncTask<Void, String, String> {
        @Override
        protected String doInBackground(Void... voids) {

            String newVersion = null;
            try {
                newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + SettingsActivity.this.getPackageName() + "&hl=it")
                        .timeout(30000)
                        .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                        .referrer("http://www.google.com")
                        .get()
                        .select("div[itemprop=softwareVersion]")
                        .first()
                        .ownText();
                return newVersion;
            } catch (Exception e) {
                return newVersion;
            }
        }

        @Override
        protected void onPostExecute(String onlineVersion) {
            super.onPostExecute(onlineVersion);
            if (onlineVersion != null && !onlineVersion.isEmpty()) {
                if (Float.valueOf(currentVersion) < Float.valueOf(onlineVersion)) {
                    //show dialog
                    Toast.makeText(SettingsActivity.this, "update is available", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, " no update is available", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(SettingsActivity.this, " no update is available", Toast.LENGTH_SHORT).show();
            }
            Timber.d("Current version " + currentVersion + "playstore version " + onlineVersion);
        }

    }

    @SuppressLint("SetTextI18n")
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
            tvVersionCode.setText("v" + String.valueOf(pInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            tvVersionCode.setText("");
        }

        // Expiry Date
        TextView tvExpiresIn = aboutDialog.findViewById(R.id.tvExpiresIn);
        TextView textView16 = aboutDialog.findViewById(R.id.textView16);
        String value_expired = PrefUtils.getStringPref(SettingsActivity.this, VALUE_EXPIRED);
        if (value_expired != null) {
            long current_time = System.currentTimeMillis();
            long expired_time = Long.parseLong(value_expired);
            long remaining_miliseconds = expired_time - current_time;
            int remaining_days = (int) (remaining_miliseconds / (60 * 60 * 24 * 1000));
            textView16.setVisibility(View.VISIBLE);
            tvExpiresIn.setVisibility(View.VISIBLE);
            if (remaining_days >= 0) {
                tvExpiresIn.setText(Integer.toString(remaining_days));
            } else {
                tvExpiresIn.setText("expired");
            }
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
        dialog.findViewById(R.id.btCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                dialog.cancel();
            }
        });
        dialog.show();
    }


    public static void resetPreferredLauncherAndOpenChooser(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, com.screenlocker.secure.launcher.FakeLauncherActivity.class);
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        Intent selector = new Intent(Intent.ACTION_MAIN);
        selector.addCategory(Intent.CATEGORY_HOME);
        selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(selector);
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
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
                    //TODO handle things when password change or updated
                    Snackbar.make(rootLayout, R.string.password_changed, Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(rootLayout, R.string.password_not_changed, Snackbar.LENGTH_SHORT).show();
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    Timber.e("onActivityResult: BG_CHANGER : " + resultUri);
                    if (isEncryptedChecked) {
                        Toast.makeText(this, "Background saved for encrypted", Toast.LENGTH_SHORT).show();
                        PrefUtils.saveStringPref(SettingsActivity.this, AppConstants.KEY_MAIN_IMAGE, resultUri.toString());

                    } else {
                        Toast.makeText(this, "Background saved for guest", Toast.LENGTH_SHORT).show();
                        PrefUtils.saveStringPref(SettingsActivity.this, AppConstants.KEY_GUEST_IMAGE, resultUri.toString());
                    }
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
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
}
