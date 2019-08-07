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
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.AsyncCalls;
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
import com.screenlocker.secure.socket.SocketManager;
import com.screenlocker.secure.socket.service.SocketService;
import com.screenlocker.secure.socket.utils.ApiUtils;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.updateDB.BlurWorker;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;
import com.secureSetting.SecureSettingsMain;
import com.theartofdev.edmodo.cropper.CropImage;

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
import static com.screenlocker.secure.utils.AppConstants.BROADCAST_APPS_ACTION;
import static com.screenlocker.secure.utils.AppConstants.CHAT_ID;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.DB_STATUS;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;
import static com.screenlocker.secure.utils.AppConstants.KEY_DATABASE_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.PGP_EMAIL;
import static com.screenlocker.secure.utils.AppConstants.SIM_ID;
import static com.screenlocker.secure.utils.AppConstants.SYSTEM_LOGIN_TOKEN;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
import static com.screenlocker.secure.utils.AppConstants.UNINSTALL_ALLOWED;
import static com.screenlocker.secure.utils.AppConstants.UPDATESIM;
import static com.screenlocker.secure.utils.AppConstants.URL_1;
import static com.screenlocker.secure.utils.AppConstants.URL_2;
import static com.screenlocker.secure.utils.CommonUtils.hideKeyboard;
import static com.screenlocker.secure.utils.PrefUtils.PREF_FILE;

/***
 * this activity show the settings for the app
 * this activity is the launcher activity it means that whenever you open the app this activity will be shown
 */
public class SettingsActivity extends BaseActivity implements View.OnClickListener, SettingContract.SettingsMvpView, CompoundButton.OnCheckedChangeListener, NetworkChangeReceiver.NetworkChangeListener {
    private NetworkChangeReceiver networkChangeReceiver;
    private SharedPreferences sharedPref;
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

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        networkChangeReceiver.setNetworkChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(networkChangeReceiver);
        networkChangeReceiver.unsetNetworkChangeListener();
    }


    public static String splitName(String s) {
        return s.replace(".apk", "");

    }


    private void runShellCommand(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);
        ButterKnife.bind(this);


//        runShellCommand("adb shell pm hide ")

        sharedPref = getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(mPreferencesListener);
        networkChangeReceiver = new NetworkChangeReceiver();

//        Toast.makeText(this, "Current version : " + android.os.Build.VERSION.SDK_INT, Toast.LENGTH_SHORT).show();


        init();

        tvAbout.setPaintFlags(tvAbout.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        constraintLayout = findViewById(R.id.rootLayout);
        constraintLayout.setVisibility(View.GONE);
        progressBar = findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);


        if (!PrefUtils.getBooleanPref(this, TOUR_STATUS)) {
            Intent intent = new Intent(this, SteppersActivity.class);
            startActivity(intent);
            finish();
        } else {

            String currentKey = PrefUtils.getStringPref(this, CURRENT_KEY);

            if (currentKey != null && currentKey.equals(AppConstants.KEY_SUPPORT_PASSWORD)) {
                constraintLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);

                return;
            }
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
            findViewById(R.id.tvTheme).setVisibility(View.GONE);
            findViewById(R.id.tvthemeDevider).setVisibility(View.GONE);
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
            findViewById(R.id.tvTheme).setVisibility(View.VISIBLE);
            findViewById(R.id.tvthemeDevider).setVisibility(View.VISIBLE);
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
        findViewById(R.id.tvTheme).setOnClickListener(this);
        tvAdvance.setOnClickListener(this);

    }


    private void setToolbar(Toolbar mToolbar) {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.toolbar_title);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
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
                case R.id.tvTheme:
                    themeDialogue();
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

                    ConnectivityManager cm =
                            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    boolean isConnected = activeNetwork != null &&
                            activeNetwork.isConnected();

                    if (isConnected) {
                        Intent intent = new Intent(this, com.screenlocker.secure.mdm.MainActivity.class);
                        startActivity(intent);
                    } else {
                        showNetworkDialog();
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

                    startActivity(new Intent(SettingsActivity.this, CodeSettingActivity.class));
                    dialogInterface.dismiss();
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
            if (CommonUtils.isNetworkAvailable(this)) {
                requestCheckForUpdate(dialog);
            } else {
                dialog.dismiss();
                Toast.makeText(this, getString(R.string.please_check_network_connection), Toast.LENGTH_SHORT).show();
            }
    }


    private AsyncCalls asyncCalls;

    private void requestCheckForUpdate(ProgressDialog dialog) {

        if (MyApplication.oneCaller == null) {


            if (asyncCalls != null) {
                asyncCalls.cancel(true);
            }

            String[] urls = {URL_1, URL_2};

            asyncCalls = new AsyncCalls(output -> {

                if (output != null) {
                    PrefUtils.saveStringPref(this, LIVE_URL, output);
                    String live_url = PrefUtils.getStringPref(MyApplication.getAppContext(), LIVE_URL);
                    Timber.d("live_url %s", live_url);
                    MyApplication.oneCaller = RetrofitClientInstance.getRetrofitInstance(live_url + MOBILE_END_POINT).create(ApiOneCaller.class);
                    update(dialog);

                }
            }, this, urls);
            asyncCalls.execute();

        } else {
            update(dialog);
        }


    }

    private void update(ProgressDialog dialog) {
        MyApplication.oneCaller
                .getUpdate("getUpdate/" + currentVersion + "/" + getPackageName() + "/" + getString(R.string.app_name), PrefUtils.getStringPref(this, SYSTEM_LOGIN_TOKEN))
                .enqueue(new Callback<UpdateModel>() {
                    @Override
                    public void onResponse(@NonNull Call<UpdateModel> call, @NonNull Response<UpdateModel> response) {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();

                        }

                        if (response.body() != null) {
                            if (response.body().isSuccess()) {
                                if (response.body().isApkStatus()) {
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(SettingsActivity.this)
                                            .setTitle(getResources().getString(R.string.update_available_title))
                                            .setMessage(getResources().getString(R.string.update_available_message))
                                            .setPositiveButton(getResources().getString(R.string.ok_text), (dialog12, which) -> {
                                                String url = response.body().getApkUrl();
                                                String live_url = PrefUtils.getStringPref(MyApplication.getAppContext(), LIVE_URL);

                                                DownLoadAndInstallUpdate obj = new DownLoadAndInstallUpdate(SettingsActivity.this, live_url + MOBILE_END_POINT + "getApk/" + CommonUtils.splitName(url), false, null);
                                                obj.execute();
                                            }).setNegativeButton(getResources().getString(R.string.cancel_text), (dialog1, which) -> {
                                                dialog1.dismiss();
                                            });
                                    dialog.show();
                                } else {
                                    Toast.makeText(SettingsActivity.this, getString(R.string.uptodate), Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                saveToken();
                                requestCheckForUpdate(dialog);
                            }

                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UpdateModel> call, @NonNull Throwable t) {
                        dialog.dismiss();
                        Toast.makeText(SettingsActivity.this, getResources().getString(R.string.error_occured_toast), Toast.LENGTH_LONG).show();

                    }
                });
    }


    private void showNetworkDialog() {

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getResources().getString(R.string.network_not_connected));
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);

        alertDialog.setMessage(getResources().getString(R.string.network_not_connected_message));

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.network_setup), (dialog, which) -> {
            Intent intent = new Intent(SettingsActivity.this, SecureSettingsMain.class);
            intent.putExtra("show_default", "show_default");
            startActivity(intent);

        });


        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.cancel_text),
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();

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

   /* private void createAccountDialog() {
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

        *//*Status*//*
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

        tvImei1.setVisibility(View.VISIBLE);
        textViewImei.setVisibility(View.VISIBLE);
        tvImei1.setText("NULL");

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

        tvImei2.setVisibility(View.VISIBLE);
        textViewImei2.setVisibility(View.VISIBLE);
        tvImei2.setText("NULL");

        if (imeis.size() > 1) {
            String imei2 = imeis.get(1);
            if (imei2 != null) {
                tvImei2.setVisibility(View.VISIBLE);
                textViewImei2.setVisibility(View.VISIBLE);
                tvImei2.setText(imei2);
            }
        }


        accountDialog.show();


    }*/


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
//                    Snackbar.make(rootLayout, R.string.password_changed, Snackbar.LENGTH_SHORT).show();
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
//                 else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                    //Exception error = result.getError();
//                }
                break;

            case 1445:

                Timber.d("skdfgijosijdg %s", resultCode);

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

    @Override
    public void isConnected(boolean state) {

        if (PrefUtils.getBooleanPref(SettingsActivity.this, DEVICE_LINKED_STATUS)) {

            Intent intent = new Intent(this, SocketService.class);
            if (state) {
                String macAddress = DeviceIdUtils.generateUniqueDeviceId(this);
                String serialNo = DeviceIdUtils.getSerialNumber();
                if (SocketManager.getInstance().getSocket() != null && !SocketManager.getInstance().getSocket().connected()) {
                    new ApiUtils(SettingsActivity.this, macAddress, serialNo);
                }
            } else {
                stopService(intent);

            }

        }
    }

    private boolean asSupport = false;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.hasExtra("isSupport")) {
            Log.d("kkogkooikn", "true: ");
            asSupport = true;
        } else {
            Log.d("kkogkooikn", "false: ");
            asSupport = false;
        }
    }

    private void themeDialogue() {
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
        builder.setTitle("Pick A Theme");
        builder.setSingleChoiceItems(R.array.themes, item, (dialog, which) -> {
            selected.set(which);
        });
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            if (selected.get() == 1) {
                PrefUtils.saveBooleanPref(this, AppConstants.KEY_THEME, false);
            } else if (selected.get() == 0) {
                PrefUtils.saveBooleanPref(this, AppConstants.KEY_THEME, true);
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    SharedPreferences.OnSharedPreferenceChangeListener mPreferencesListener = (sharedPreferences, key) -> {
        if (key.equals(AppConstants.KEY_THEME)) {
            if (PrefUtils.getBooleanPref(SettingsActivity.this, AppConstants.KEY_THEME)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            getDelegate().applyDayNight();
            recreate();
        }
    };

    @Override
    protected void onDestroy() {
        sharedPref.unregisterOnSharedPreferenceChangeListener(mPreferencesListener);
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

        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.show();
    }

    private void changeLanguage(String code) {

        Intent intent = new Intent(BROADCAST_APPS_ACTION);
        intent.putExtra(KEY_DATABASE_CHANGE, "apps");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        CommonUtils.setAppLocale(code, SettingsActivity.this);
        PrefUtils.saveStringPref(this, AppConstants.LANGUAGE_PREF, code);
        recreate();


    }

}
