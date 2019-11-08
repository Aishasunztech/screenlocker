package com.screenlocker.secure.settings;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.screenlocker.secure.R;
import com.screenlocker.secure.async.DownLoadAndInstallUpdate;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.mdm.utils.NetworkChangeReceiver;
import com.screenlocker.secure.permissions.SteppersActivity;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.settings.Wallpaper.WallpaperActivity;
import com.screenlocker.secure.settings.codeSetting.CodeSettingActivity;
import com.screenlocker.secure.settings.codeSetting.LanguageControls.LanguageAdapter;
import com.screenlocker.secure.settings.codeSetting.LanguageControls.LanguageModel;
import com.screenlocker.secure.settings.codeSetting.installApps.UpdateModel;
import com.screenlocker.secure.settings.managepassword.ManagePasswords;
import com.screenlocker.secure.settings.managepassword.SetUpLockActivity;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.updateDB.BlurWorker;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;
import com.secureSetting.SecureSettingsMain;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.app.MyApplication.saveToken;
import static com.screenlocker.secure.launcher.MainActivity.RESULT_ENABLE;
import static com.screenlocker.secure.socket.utils.utils.saveLiveUrl;
import static com.screenlocker.secure.utils.AppConstants.CHAT_ID;
import static com.screenlocker.secure.utils.AppConstants.CONNECTED;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_NETWORK_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DB_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.LIMITED;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.PGP_EMAIL;
import static com.screenlocker.secure.utils.AppConstants.SIM_ID;
import static com.screenlocker.secure.utils.AppConstants.SOCKET_STATUS;
import static com.screenlocker.secure.utils.AppConstants.STOP_SOCKET;
import static com.screenlocker.secure.utils.AppConstants.SYSTEM_LOGIN_TOKEN;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
import static com.screenlocker.secure.utils.AppConstants.UPDATESIM;
import static com.screenlocker.secure.utils.CommonUtils.hideKeyboard;
import static com.screenlocker.secure.utils.CommonUtils.isNetworkAvailable;
import static com.screenlocker.secure.utils.CommonUtils.isSocketConnected;
import static com.screenlocker.secure.utils.PrefUtils.PREF_FILE;

/***
 * this activity show the settings for the app
 * this activity is the launcher activity it means that whenever you open the app this activity will be shown
 */
public class SettingsActivity extends BaseActivity implements View.OnClickListener, SettingContract.SettingsMvpView, CompoundButton.OnCheckedChangeListener {

    private Toolbar mToolbar;
    /**
     * request code for the set password activity
     */
    public static final int REQUEST_CODE_PASSWORD = 883;
    private InputMethodManager imm;
    private Switch switchEnableVpn;

    private SettingsPresenter settingsPresenter;
    private boolean isEncryptedChecked;
    private String currentVersion;


    @BindView(R.id.tvManagePasswords)
    TextView tvManagePasswords;
    @BindView(R.id.tvChooseBackground)
    TextView tvChooseBackground;
    @BindView(R.id.tvAbout)
    TextView tvAbout;
    @BindView(R.id.tvCode)
    TextView tvCode;
    @BindView(R.id.tvCheckForUpdate)
    TextView tvCheckForUpdate;
    @BindView(R.id.tvAccount)
    TextView tvAccount;
    @BindView(R.id.tvLanguage)
    TextView tvLanguage;
    @BindView(R.id.tvAdvance)
    TextView tvAdvance;
    @BindView(R.id.dividerAdvance)
    View dividerAdvance;
    private TextView tvlinkDevice;

    private ConstraintLayout constraintLayout;

    private ProgressBar progressBar;

    private Dialog aboutDialog = null, accountDialog = null;
    private AlertDialog limitedDialog;


    public static String splitName(String s) {
        return s.replace(".apk", "");

    }

    private NetworkChangeReceiver networkChangeReceiver;
    private SharedPreferences sharedPref;

    private void registerNetworkPref() {
        sharedPref = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(networkChange);
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void unRegisterNetworkPref() {
        if (sharedPref != null)
            sharedPref.unregisterOnSharedPreferenceChangeListener(networkChange);
        if (networkChangeReceiver != null)
            unregisterReceiver(networkChangeReceiver);
    }

    SharedPreferences.OnSharedPreferenceChangeListener networkChange = (sharedPreferences, key) -> {

        if (key.equals(CURRENT_NETWORK_STATUS)) {
            String networkStatus = sharedPreferences.getString(CURRENT_NETWORK_STATUS, LIMITED);
            boolean isConnected = networkStatus.equals(CONNECTED);

            if (PrefUtils.getBooleanPref(SettingsActivity.this, DEVICE_LINKED_STATUS)) {


                if (isConnected) {
                    String macAddress = DeviceIdUtils.generateUniqueDeviceId(this);
                    String serialNo = DeviceIdUtils.getSerialNumber();
                    if (!isSocketConnected()) {
                        new ApiUtils(SettingsActivity.this, macAddress, serialNo);
                    }

                    if(limitedDialog != null && limitedDialog.isShowing())
                    {
                        limitedDialog.dismiss();
                        Intent linkedIntent = new Intent(this, com.screenlocker.secure.mdm.MainActivity.class);
                        startActivity(linkedIntent);
                    }
                } else {
                    Intent intent = new Intent(this, LockScreenService.class);
                    intent.putExtra(SOCKET_STATUS,STOP_SOCKET);
                    ActivityCompat.startForegroundService(this,intent);
                }

            }
        }
    };


    private void runShellCommand(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerNetworkPref();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unRegisterNetworkPref();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        ButterKnife.bind(this);

//        String crash[] = null;
//        Toast.makeText(this, crash[0], Toast.LENGTH_SHORT).show();

//        runShellCommand("adb shell pm hide ")

//        Toast.makeText(this, "Current version : " + android.os.Build.VERSION.SDK_INT, Toast.LENGTH_SHORT).show();


        init();

        tvAbout.setPaintFlags(tvAbout.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        constraintLayout = findViewById(R.id.rootLayout);
        constraintLayout.setVisibility(View.VISIBLE);
        progressBar = findViewById(R.id.progress);
        progressBar.setVisibility(View.GONE);


        if (!PrefUtils.getBooleanPref(this, TOUR_STATUS)) {
            Intent intent = new Intent(this, SteppersActivity.class);
            startActivity(intent);
            finish();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();

        String currentKey = PrefUtils.getStringPref(this, CURRENT_KEY);

        if (currentKey != null && currentKey.equals(AppConstants.KEY_SUPPORT_PASSWORD)) {
            tvManagePasswords.setVisibility(View.GONE);
            tvChooseBackground.setVisibility(View.GONE);
            tvCode.setVisibility(View.GONE);
            tvLanguage.setVisibility(View.VISIBLE);
            findViewById(R.id.divider).setVisibility(View.GONE);
            findViewById(R.id.divider5).setVisibility(View.GONE);
            findViewById(R.id.divider15).setVisibility(View.GONE);
            findViewById(R.id.divider).setVisibility(View.GONE);
            dividerAdvance.setVisibility(View.GONE);
            tvAdvance.setVisibility(View.GONE);
        } else {

            tvManagePasswords.setVisibility(View.VISIBLE);
            tvChooseBackground.setVisibility(View.VISIBLE);
            tvCode.setVisibility(View.VISIBLE);
            tvLanguage.setVisibility(View.VISIBLE);
            findViewById(R.id.divider).setVisibility(View.VISIBLE);
            findViewById(R.id.divider5).setVisibility(View.VISIBLE);
            findViewById(R.id.divider15).setVisibility(View.VISIBLE);
            findViewById(R.id.divider).setVisibility(View.VISIBLE);
            dividerAdvance.setVisibility(View.VISIBLE);
            tvAdvance.setVisibility(View.VISIBLE);
        }

        if (PrefUtils.getBooleanPref(this, TOUR_STATUS)) {
            if (!utils.isMyServiceRunning(LockScreenService.class, this)) {
                Intent intent = new Intent(this, LockScreenService.class);

                ActivityCompat.startForegroundService(this, intent);
            }
        }

    }

    public void init() {
        setIds();
        setToolbar(mToolbar);
        setListeners();
        settingsPresenter = new SettingsPresenter(this, new SettingsModel(this));
//        setSwipeToApiRequest();
        // switch change listener(on off service for vpn)
        switchEnableVpn.setOnCheckedChangeListener(this);
        createActiveDialog();
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String mMacAddress;
        if (manager != null) {
            mMacAddress = DeviceIdUtils.generateUniqueDeviceId(this);
        } else {
            mMacAddress = null;
        }

    }


    @Override
    protected void onPause() {
        hideKeyboard(SettingsActivity.this);

        if (aboutDialog != null) {
            aboutDialog.dismiss();
        }
        if (accountDialog != null) {
            accountDialog.dismiss();
        }

        super.onPause();
    }

    private void createActiveDialog() {
        AlertDialog isActiveDialog = new AlertDialog.Builder(this).setMessage("").setCancelable(false).create();
    }


    private void setIds() {
        switchEnableVpn = findViewById(R.id.switchEnableVpn);
        ConstraintLayout rootLayout = findViewById(R.id.rootLayout);
        mToolbar = findViewById(R.id.toolbar);

        tvlinkDevice = findViewById(R.id.tvlinkDevice);
    }

    private void setListeners() {
        findViewById(R.id.tvManagePasswords).setOnClickListener(this);
        findViewById(R.id.tvChooseBackground).setOnClickListener(this);
        findViewById(R.id.tvAbout).setOnClickListener(this);
        findViewById(R.id.tvCode).setOnClickListener(this);
        findViewById(R.id.tvCheckForUpdate).setOnClickListener(this);
        findViewById(R.id.tvlinkDevice).setOnClickListener(this);
        findViewById(R.id.tvAccount).setVisibility(View.VISIBLE);
        findViewById(R.id.tvAccount).setOnClickListener(this);
        findViewById(R.id.tvLanguage).setOnClickListener(this);

        tvAdvance.setOnClickListener(this);

    }


    private void setToolbar(Toolbar mToolbar) {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.toolbar_title);
        String deviceId = PrefUtils.getStringPref(this,DEVICE_ID);
        if(deviceId != null && !deviceId.equals(""))
        {
            getSupportActionBar().setSubtitle("Device ID: " + deviceId);
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
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
                    Intent account = new Intent(SettingsActivity.this, AboutActivity.class);
                    startActivity(account);
                    break;
                case R.id.tvCheckForUpdate:     //handle the about click event
                    handleCheckForUpdate();
                    //Crashlytics.getInstance().crash(); // Force a crash
                    break;
                case R.id.tvAdvance:
                    startActivity(new Intent(this, AdvanceSettings.class));
                    break;
                case R.id.tvlinkDevice:

                    if (!isNetworkAvailable(this)) {
                        showNetworkDialog(getResources().getString(R.string.network_not_connected),getResources().getString(R.string.network_not_connected_message),getResources().getString(R.string.network_setup));
                    }
//                    else if (!isNetworkConneted(this)) {
//                        showNetworkDialog(getResources().getString(R.string.network_limited),getResources().getString(R.string.network_limited_message),getResources().getString(R.string.change_network));
//                    }
                    else {
                        Intent intent = new Intent(this, com.screenlocker.secure.mdm.MainActivity.class);
                        startActivity(intent);
                    }


                    break;
                case R.id.tvLanguage:
                    languageDialogue();
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
                    showAlertDialog(SettingsActivity.this, getResources().getString(R.string.invalid_password_title), getResources().getString(R.string.invalid_password_message), android.R.drawable.stat_sys_warning);
                    return;
                }
                if (input.getText().toString().equalsIgnoreCase(PrefUtils.getStringPref(SettingsActivity.this, AppConstants.KEY_CODE_PASSWORD))) {
                    // start Code settings activity if the code password entered is correct


                    constraintLayout.setVisibility(View.GONE);
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
                                    constraintLayout.setVisibility(View.VISIBLE);
                                    progressBar.setVisibility(View.INVISIBLE);
                                    startActivity(new Intent(SettingsActivity.this, CodeSettingActivity.class));
                                    dialogInterface.dismiss();
                                }
                            });


                } else {
                    showAlertDialog(SettingsActivity.this, getResources().getString(R.string.invalid_password_title), getResources().getString(R.string.invalid_password_message), android.R.drawable.ic_dialog_alert);
                }
            }, null, getString(R.string.please_enter_code_admin_password));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void handleCheckForUpdate() {
        try {
            ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            Network n = manager.getActiveNetwork();
            NetworkCapabilities nc = manager.getNetworkCapabilities(n);
            if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                if (PrefUtils.getIntegerPref(this, UPDATESIM) != 1) {
                    new AlertDialog.Builder(this)
                            .setTitle("Warning!")
                            .setMessage("Using SIM data for Updating Device may require data over 100MBs, please use WIFI instead or continue anyways.")
                            .setPositiveButton(getResources().getString(R.string.continue_anyway), (dialog, which) -> {
                                proccedToDownload();
                            })
                            .setNegativeButton(R.string.cancel, (dialog, which) -> {
                                dialog.dismiss();
                            }).show();
                }
            } else {
                proccedToDownload();
            }
        } catch (Exception e) {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }

    }

    private void proccedToDownload() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle(getResources().getString(R.string.update_dialog_title));
        dialog.setMessage(getResources().getString(R.string.update_dialog_message));
        dialog.show();
        try {
            currentVersion = String.valueOf(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (currentVersion != null)
            if (CommonUtils.isNetworkConneted(this)) {
                requestCheckForUpdate(dialog);
            } else {
                dialog.dismiss();
                Toast.makeText(this, getString(R.string.please_check_network_connection), Toast.LENGTH_SHORT).show();
            }
    }

    private void requestCheckForUpdate(ProgressDialog dialog) {
        Timber.d("<<< Checking for update >>>");

        update(dialog, RetrofitClientInstance.getWhiteLabelInstance());
    }


    boolean isFailSafe = false;

    private void update(ProgressDialog dialog, ApiOneCaller apiOneCaller) {


        apiOneCaller.getUpdate("getUpdate/" + currentVersion + "/" + getPackageName() + "/" + getString(R.string.apk_label), PrefUtils.getStringPref(this, SYSTEM_LOGIN_TOKEN))
                .enqueue(new Callback<UpdateModel>() {
                    @Override
                    public void onResponse(@NonNull Call<UpdateModel> call, @NonNull Response<UpdateModel> response) {


                        Timber.i("-------> Api Check for update on response .");

                        boolean responseStatus = response.isSuccessful();

                        Timber.i("--------> response status : %s", responseStatus);


                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }

                        if (responseStatus) {

                            if (response.body() != null) {
                                UpdateModel updateModel = response.body();

                                boolean validationStatus = updateModel.isSuccess();

                                Timber.i("----------> token validation status :%s", validationStatus);

                                if (validationStatus) {

                                    boolean updateStatus = updateModel.isApkStatus();

                                    Timber.i("------------> update available status : %s", updateStatus);

                                    if (updateStatus) {

                                        AlertDialog.Builder dialog = new AlertDialog.Builder(SettingsActivity.this)
                                                .setTitle(getResources().getString(R.string.update_available_title))
                                                .setMessage(getResources().getString(R.string.update_available_message))
                                                .setPositiveButton(getResources().getString(R.string.ok_text), (dialog12, which) -> {

                                                    String apkUrl = updateModel.getApkUrl();

                                                    Timber.i("------------> updated apk url : %s", apkUrl);

                                                    saveLiveUrl(isFailSafe);

                                                    String live_url = PrefUtils.getStringPref(SettingsActivity.this, LIVE_URL);
                                                    Timber.i("------------> Live Server Url :%s ", live_url);

                                                    DownLoadAndInstallUpdate obj = new DownLoadAndInstallUpdate(SettingsActivity.this, live_url + "getApk/" + CommonUtils.splitName(apkUrl), false, null);
                                                    obj.execute();


                                                }).setNegativeButton(getResources().getString(R.string.cancel_text), (dialog1, which) -> {
                                                    dialog1.dismiss();
                                                });
                                        dialog.show();
                                    } else {
                                        Timber.i("-------------> Application is already up to date . :)");
                                        Toast.makeText(SettingsActivity.this, getString(R.string.uptodate), Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    Timber.i("-----------> token validation failed . Request for new token. ");
                                    saveToken(RetrofitClientInstance.getWhiteLabelInstance());
                                    Timber.i("-------------> Again checking for update .");
                                    update(dialog, apiOneCaller);
                                }

                            } else {
                                Timber.i("---------> oops response body is null. ");
                            }


                        } else {
                            Timber.i("-----------> invalid response code :(");
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<UpdateModel> call, @NonNull Throwable t) {
                        if (dialog != null && dialog.isShowing())
                            dialog.dismiss();
                        Timber.d("onFailure : %s", t.getMessage());

                        if (t instanceof UnknownHostException) {
                            Timber.e("-----------> something very dangerous happen with domain : %s", t.getMessage());
                            if (isFailSafe) {
                                Timber.e("------------> FailSafe domain is also not working. ");
                            } else {
                                Timber.i("<<< New Api call with failsafe domain >>>");
                                update(dialog, RetrofitClientInstance.getFailSafeInstanceForWhiteLabel());
                                isFailSafe = true;
                            }

                        } else if (t instanceof IOException) {
                            Timber.e(" ----> IO Exception :%s", t.getMessage());
                            Toast.makeText(SettingsActivity.this, getResources().getString(R.string.error_occured_toast), Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }

    private void showNetworkDialog(String title, String msg,String btnTitle) {

        limitedDialog = new AlertDialog.Builder(this).create();
        limitedDialog.setTitle(title);
        limitedDialog.setIcon(android.R.drawable.ic_dialog_info);

        limitedDialog.setMessage(msg);

        limitedDialog.setButton(AlertDialog.BUTTON_POSITIVE, btnTitle, (dialog, which) -> {
            Intent intent = new Intent(SettingsActivity.this, SecureSettingsMain.class);
            intent.putExtra("show_default", "show_default");
            startActivity(intent);


        });


        limitedDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.cancel_text),
                (dialog, which) -> dialog.dismiss());
        limitedDialog.show();

    }

    private void showAlertDialog(AppCompatActivity activity, String title, String msg, int icon) {

        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        alertDialog.setIcon(icon);
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.ok_text),
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();

    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, final boolean isChecked) {

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

        aboutDialog.findViewById(R.id.tvWhatsNew).setOnClickListener(v -> {
            startActivity(new Intent(this, WhatsNew.class));

        });

        aboutDialog.findViewById(R.id.tvWhatsNew).setOnClickListener(v -> {
            startActivity(new Intent(SettingsActivity.this, WhatsNew.class));
        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    PrefUtils.saveBooleanPref(this, AppConstants.KEY_ADMIN_ALLOWED, true);
                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.enable_admin_device_feature), Toast.LENGTH_SHORT).show();
                } else {
                    PrefUtils.saveBooleanPref(this, AppConstants.KEY_ADMIN_ALLOWED, false);
                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.problem_admin_device_feature), Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_CODE_PASSWORD:
                if (resultCode == RESULT_OK) {
                    showAlertDialog(SettingsActivity.this, getResources().getString(R.string.password_changed_title), getResources().getString(R.string.password_changed_message), R.drawable.ic_checked);
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    Timber.e("onActivityResult: BG_CHANGER : %s", resultUri);
                    if (isEncryptedChecked) {
                        Toast.makeText(this, getResources().getString(R.string.bg_save_encrypted), Toast.LENGTH_SHORT).show();
                        PrefUtils.saveStringPref(SettingsActivity.this, AppConstants.KEY_MAIN_IMAGE, resultUri.toString());

                    } else {
                        Toast.makeText(this, getResources().getString(R.string.bg_save_guest), Toast.LENGTH_SHORT).show();
                        PrefUtils.saveStringPref(SettingsActivity.this, AppConstants.KEY_GUEST_IMAGE, resultUri.toString());
                    }
                }
                break;

            case 1445:

                Timber.d("skdfgijosijdg %s", resultCode);

                break;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private void languageDialogue() {
        int item;
        AtomicInteger selected = new AtomicInteger();
        if (PrefUtils.getBooleanPref(this, AppConstants.KEY_THEME)) {
            item = 0;
            selected.set(0);
        } else {
            item = 1;
            selected.set(1);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Language");
        ArrayList<LanguageModel> models = new ArrayList<>();
        String[] languages = getResources().getStringArray(R.array.languages);

        for (String language : languages) {
            String language_key = language.split(":")[0];
            String language_name = language.split(":")[1];
            LanguageModel languageModel2;

            switch (language_key) {
                case "en":
                    languageModel2 = new LanguageModel(language_key, language_name, R.drawable.ic_flag_of_the_united_states);
                    break;
                case "fr":
                    languageModel2 = new LanguageModel(language_key, language_name, R.drawable.ic_flag_of_france);
                    break;
                case "vi":
                    languageModel2 = new LanguageModel(language_key, language_name, R.drawable.ic_flag_of_vietnam);
                    break;
                case "zh":
                    languageModel2 = new LanguageModel(language_key, language_name, R.drawable.ic_chinese_flag);
                    break;
                case "es":
                    languageModel2 = new LanguageModel(language_key, language_name, R.drawable.ic_flag_of_spain);
                    break;
                case "ar":
                    languageModel2 = new LanguageModel(language_key, language_name, R.drawable.ic_flag_of_saudi_arabia);
                    break;
                default:
                    languageModel2 = new LanguageModel(language_key, language_name, R.drawable.ic_flag_of_saudi_arabia);
                    break;

            }

            models.add(languageModel2);
        }
        String saved = PrefUtils.getStringPref(this, AppConstants.LANGUAGE_PREF);
        if (saved == null || saved.equals("")) {
            saved = "en";
        }
        LanguageAdapter adapter = new LanguageAdapter(this, languages, saved, models);
        builder.setAdapter(adapter, (dialog, which) -> {

        });
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            changeLanguage(adapter.getSelectedText());

            constraintLayout.setVisibility(View.GONE);
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
                            constraintLayout.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });

        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    private void changeLanguage(String code) {

        CommonUtils.setAppLocale(code, SettingsActivity.this);
        PrefUtils.saveStringPref(this, AppConstants.LANGUAGE_PREF, code);
//        recreate();
        restartActivity();

    }

    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

}
