package com.screenlocker.secure.settings;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.async.DownLoadAndInstallUpdate;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.launcher.subsettings.BackuoAndRestoreActivity;
import com.screenlocker.secure.launcher.subsettings.ConnectionsSubSettings;
import com.screenlocker.secure.launcher.subsettings.SSettingsViewModel;
import com.screenlocker.secure.permissions.SteppersActivity;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.settings.Wallpaper.WallpaperActivity;
import com.screenlocker.secure.settings.codeSetting.CodeSettingActivity;
import com.screenlocker.secure.settings.codeSetting.installApps.UpdateModel;
import com.screenlocker.secure.settings.managepassword.ManagePasswords;
import com.screenlocker.secure.settings.managepassword.SetUpLockActivity;
import com.screenlocker.secure.settings.notification.NotificationActivity;
import com.screenlocker.secure.settings.notification.NotificationViewModel;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.SecuredSharedPref;
import com.secure.launcher.R;
import com.secureSetting.AllNotificationActivity;
import com.secureSetting.SecureSettingsMain;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.app.MyApplication.saveToken;
import static com.screenlocker.secure.launcher.MainActivity.RESULT_ENABLE;
import static com.screenlocker.secure.utils.AppConstants.CHAT_ID;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.GET_APK_ENDPOINT;
import static com.screenlocker.secure.utils.AppConstants.GET_UPDATE_ENDPOINT;
import static com.screenlocker.secure.utils.AppConstants.IS_SETTINGS_ALLOW;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.PGP_EMAIL;
import static com.screenlocker.secure.utils.AppConstants.SIM_ID;
import static com.screenlocker.secure.utils.AppConstants.SYSTEM_LOGIN_TOKEN;
import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;
import static com.screenlocker.secure.utils.AppConstants.UPDATESIM;
import static com.screenlocker.secure.utils.AppConstants.URL_1;
import static com.screenlocker.secure.utils.AppConstants.URL_2;
import static com.screenlocker.secure.utils.CommonUtils.hideKeyboard;
import static com.screenlocker.secure.utils.CommonUtils.isNetworkAvailable;

/***
 * this activity show the settings for the app
 * this activity is the launcher activity it means that whenever you open the app this activity will be shown
 */
public class SettingsActivity extends BaseActivity
        implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private Toolbar mToolbar;
    /**
     * request code for the set password activity
     */
    public static final int REQUEST_CODE_PASSWORD = 883;
    private boolean isEncryptedChecked;
    private String currentVersion;


    @BindView(R.id.tvChooseBackground)
    LinearLayout tvChooseBackground;
    @BindView(R.id.tvAbout)
    LinearLayout tvAbout;
    @BindView(R.id.tvCode)
    LinearLayout tvCode;
    @BindView(R.id.tvCheckForUpdate)
    LinearLayout tvCheckForUpdate;
    @BindView(R.id.tvAccount)
    LinearLayout tvAccount;
    @BindView(R.id.tvAdvance)
    LinearLayout tvAdvance;
    @BindView(R.id.screen_lock_container)
    LinearLayout screenLockContainer;
    @BindView(R.id.notification_container)
    LinearLayout notificationContainer;
    @BindView(R.id.sound_container)
    LinearLayout soundContainer;

    @BindView(R.id.connection_layout)
    LinearLayout tvConnection;
    @BindView(R.id.tvlinkDevice)
    LinearLayout tvlinkDevice;

    private int unreadCount = 0;

    private Dialog aboutDialog = null, accountDialog = null;
    private AlertDialog limitedDialog;

    private PrefUtils prefUtils ;
//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                    .detectDiskReads()
//                    .detectDiskWrites()
//                    .detectNetwork()   // or .detectAll() for all detectable problems
//                    .penaltyLog()
//                    .build());
//            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                    .detectLeakedSqlLiteObjects()
//                    .detectLeakedClosableObjects()
//                    .penaltyLog()
//                    .penaltyDeath()
//                    .build());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        prefUtils = PrefUtils.getInstance(this);

        ButterKnife.bind(this);
        init();


        if (!prefUtils.getBooleanPref( TOUR_STATUS)) {
            Intent intent = new Intent(this, SteppersActivity.class);
            startActivity(intent);

            finish();
            return;
        }

        String userType = prefUtils.getStringPref( CURRENT_KEY);
        SSettingsViewModel settingsViewModel = new ViewModelProvider(this).get(SSettingsViewModel.class);
        NotificationViewModel viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        viewModel.getUnReadCount().observe(this, integer -> {
            unreadCount = integer;
            invalidateOptionsMenu();
        });
        settingsViewModel.getSubExtensions().observe(this, subExtensions -> {
            if (userType.equals(AppConstants.KEY_MAIN_PASSWORD)) {
                setUpPermissionSettingsEncrypted(subExtensions);
            }else if (userType.equals(AppConstants.KEY_GUEST_PASSWORD)) {
                setUpPermissionSettingsGuest(subExtensions);
            }
        });



    }











    @Override
    protected void onResume() {
        super.onResume();
        prefUtils.saveBooleanPref( IS_SETTINGS_ALLOW, true);
        AppConstants.TEMP_SETTINGS_ALLOWED = true;
        String currentKey = prefUtils.getStringPref( CURRENT_KEY);

        if (currentKey != null && currentKey.equals(AppConstants.KEY_SUPPORT_PASSWORD)) {
//            tvManagePasswords.setVisibility(View.GONE);
            tvChooseBackground.setVisibility(View.GONE);
            tvCode.setVisibility(View.GONE);
            soundContainer.setVisibility(View.GONE);
            notificationContainer.setVisibility(View.GONE);
            screenLockContainer.setVisibility(View.GONE);
            findViewById(R.id.baclupAndRestore).setVisibility(View.GONE);
            // tvLanguage.setVisibility(View.VISIBLE);
            tvAdvance.setVisibility(View.GONE);
        } else {
//            tvManagePasswords.setVisibility(View.VISIBLE);
            tvChooseBackground.setVisibility(View.VISIBLE);
            screenLockContainer.setVisibility(View.VISIBLE);
            findViewById(R.id.baclupAndRestore).setVisibility(View.VISIBLE);
            //tvCode.setVisibility(View.VISIBLE);
            ///tvLanguage.setVisibility(View.VISIBLE);
            tvAdvance.setVisibility(View.VISIBLE);
        }

        if (prefUtils.getBooleanPref( TOUR_STATUS)) {
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
//        setSwipeToApiRequest();
        // switch change listener(on off service for vpn)
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




    private void setIds() {
        //ConstraintLayout rootLayout = findViewById(R.id.rootLayout);
        mToolbar = findViewById(R.id.toolbar);

    }

    private void setListeners() {
        findViewById(R.id.baclupAndRestore).setOnClickListener(this);
        findViewById(R.id.tvChooseBackground).setOnClickListener(this);
        findViewById(R.id.tvAbout).setOnClickListener(this);
        findViewById(R.id.tvCode).setOnClickListener(this);
        findViewById(R.id.tvCheckForUpdate).setOnClickListener(this);
        findViewById(R.id.tvlinkDevice).setOnClickListener(this);
        findViewById(R.id.tvAccount).setVisibility(View.VISIBLE);
        findViewById(R.id.tvAccount).setOnClickListener(this);
        notificationContainer.setOnClickListener(this);
        soundContainer.setOnClickListener(this);
        findViewById(R.id.screen_lock_container).setOnClickListener(this);
        // findViewById(R.id.tvLanguage).setOnClickListener(this);

        tvAdvance.setOnClickListener(this);
        tvConnection.setOnClickListener(this);

    }


    private void setToolbar(Toolbar mToolbar) {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.secure_settings_activity_title);
            String deviceid = prefUtils.getStringPref( DEVICE_ID);
            if (deviceid != null) {
                getSupportActionBar().setSubtitle(getResources().getString(R.string.device_id) + ": " + deviceid);
            }
            //getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        }
    }

    @Override
    public void onClick(View v) {
        if (isOverLayAllowed()) {
            switch (v.getId()) {
                case R.id.baclupAndRestore:
                    startActivity(new Intent(this, BackuoAndRestoreActivity.class));


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


//                    if (!isNetworkConneted(this)) {
//                        showNetworkDialog(getResources().getString(R.string.network_limited), getResources().getString(R.string.network_limited_message), getResources().getString(R.string.change_network));
//                    }
//                    else
                    if (!isNetworkAvailable(this)) {
                        showNetworkDialog(getResources().getString(R.string.network_not_connected), getResources().getString(R.string.network_not_connected_message), getResources().getString(R.string.network_setup));
                    } else {
                        Intent intent = new Intent(this, com.screenlocker.secure.mdm.MainActivity.class);
                        startActivity(intent);
                    }


                    break;
//                case R.id.tvLanguage:
//                    languageDialogue();
//                    break;
                case R.id.connection_layout:
                    startActivity(new Intent(this, ConnectionsSubSettings.class));
                    break;
                case R.id.notification_container:
                    startActivity(new Intent(SettingsActivity.this, AllNotificationActivity.class));
                    break;
                case R.id.sound_container:
                    startActivity(new Intent(Settings.ACTION_SOUND_SETTINGS));
                    break;
                case R.id.screen_lock_container:
                    Intent passwordsIntent = new Intent(SettingsActivity.this, ManagePasswords.class);
                    startActivity(passwordsIntent);
                    break;

            }
        } else {
            if (!gerOverlayDialog().isShowing())
                gerOverlayDialog().show();
        }


    }


    private void handleCodeAdmin() {
        SecuredSharedPref sharedPref = SecuredSharedPref.getInstance(this);
        if (sharedPref.getStringPref( AppConstants.KEY_CODE_PASSWORD) == null) {
            Intent intent = new Intent(this, SetUpLockActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, AppConstants.KEY_CODE);
            startActivityForResult(intent, REQUEST_CODE_PASSWORD);

        } else {
            final EditText input = new EditText(SettingsActivity.this);
            showAlertDialog(input, (dialogInterface, i) -> {
                if (TextUtils.isEmpty(input.getText().toString().trim())) {
//                    Snackbar.make(rootLayout, R.string.please_enter_your_current_password, Snackbar.LENGTH_SHORT).show();
                    showAlertDialog(SettingsActivity.this, getResources().getString(R.string.invalid_password_title), getResources().getString(R.string.invalid_password_message), android.R.drawable.stat_sys_warning);
                    return;
                }
                if (input.getText().toString().equalsIgnoreCase(sharedPref.getStringPref( AppConstants.KEY_CODE_PASSWORD))) {
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
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network n = manager.getActiveNetwork();
        try {
            NetworkCapabilities nc = manager.getNetworkCapabilities(n);
            if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                if (!prefUtils.getBooleanPref( UPDATESIM)) {
                    new AlertDialog.Builder(this)
                            .setTitle(getResources().getString(R.string.warning))
                            .setMessage(getResources().getString(R.string.sim_update_warning))
                            .setPositiveButton(getResources().getString(R.string.continue_anyway), (dialog, which) -> {
                                proccedToDownload();
                            })
                            .setNegativeButton(R.string.cancel, (dialog, which) -> {
                                dialog.dismiss();
                            }).show();
                } else {
                    proccedToDownload();
                }
            } else {
                proccedToDownload();
            }
        } catch (Exception e) {
            Toast.makeText(this, getResources().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
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
            if (CommonUtils.isNetworkConneted(prefUtils)) {
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
                    prefUtils.saveStringPref( LIVE_URL, output);
                    String live_url = prefUtils.getStringPref( LIVE_URL);
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
                .getUpdate(GET_UPDATE_ENDPOINT + currentVersion + "/" + getPackageName() + "/" + getString(R.string.my_apk_name),
                        prefUtils.getStringPref( SYSTEM_LOGIN_TOKEN))
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


                                                    String live_url = prefUtils.getStringPref( LIVE_URL);
                                                    Timber.i("------------> Live Server Url :%s ", live_url);

                                                    DownLoadAndInstallUpdate obj = new DownLoadAndInstallUpdate(SettingsActivity.this, live_url +MOBILE_END_POINT+ GET_APK_ENDPOINT + CommonUtils.splitName(apkUrl), false, null);
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
                                    saveToken();
                                    Timber.i("-------------> Again checking for update .");
                                    update(dialog);
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
                        dialog.dismiss();
                        Toast.makeText(SettingsActivity.this, getResources().getString(R.string.error_occured_toast), Toast.LENGTH_LONG).show();

                    }
                });
    }


    private void showNetworkDialog(String title, String msg, String btnTitle) {

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
        String pgpEmail = prefUtils.getStringPref( PGP_EMAIL);
        if (pgpEmail != null) {
            textView18.setVisibility(View.VISIBLE);
            tvPgpEmail.setVisibility(View.VISIBLE);
            tvPgpEmail.setText(pgpEmail);
        }

        // Chat ID
        TextView tvChatId = aboutDialog.findViewById(R.id.tvChatId);
        TextView textView19 = aboutDialog.findViewById(R.id.textView19);
        String chatId = prefUtils.getStringPref( CHAT_ID);
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
        String simId = prefUtils.getStringPref( SIM_ID);
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
                    prefUtils.saveBooleanPref( AppConstants.KEY_ADMIN_ALLOWED, true);
                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.enable_admin_device_feature), Toast.LENGTH_SHORT).show();
                } else {
                    prefUtils.saveBooleanPref( AppConstants.KEY_ADMIN_ALLOWED, false);
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
                        prefUtils.saveStringPref( AppConstants.KEY_MAIN_IMAGE, resultUri.toString());

                    } else {
                        Toast.makeText(this, getResources().getString(R.string.bg_save_guest), Toast.LENGTH_SHORT).show();
                        prefUtils.saveStringPref( AppConstants.KEY_GUEST_IMAGE, resultUri.toString());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.msg_alert, menu);
        final MenuItem itemMessages = menu.findItem(R.id.notification1);

        View badgeLayout = itemMessages.getActionView();
        TextView itemMessagesBadgeTextView = (TextView) badgeLayout.findViewById(R.id.badge_textView);
        if (unreadCount == 0) {
            itemMessagesBadgeTextView.setVisibility(View.GONE); // initially hidden}
        } else {
            itemMessagesBadgeTextView.setText(String.valueOf( unreadCount));
            itemMessagesBadgeTextView.setVisibility(View.VISIBLE);
        }

        ImageButton iconButtonMessages = (ImageButton) badgeLayout.findViewById(R.id.badge_icon_button);
//        iconButtonMessages.setText("{fa-envelope}");
//        iconButtonMessages.setTextColor(getResources().getColor(R.color.action_bar_icon_color_disabled));

        iconButtonMessages.setOnClickListener(view -> {
            startActivity(new Intent(this, NotificationActivity.class));
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.notification) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    void setUpPermissionSettingsEncrypted(List<SubExtension> settings) {
        for (SubExtension setting : settings) {
            Timber.d("setUpPermissionSettingsEncrypted: %s", setting.getUniqueExtension());
            switch (setting.getUniqueExtension()) {
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_AdminPanel:
                    if (setting.isEncrypted()) {
                        tvCode.setVisibility(View.VISIBLE);
                    } else tvCode.setVisibility(View.GONE);
                    break;
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_Notifications:
                    if (setting.isEncrypted()) {
                        notificationContainer.setVisibility(View.VISIBLE);
                    } else notificationContainer.setVisibility(View.GONE);
                    break;
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_Sound:
                    if (setting.isEncrypted()) {
                        soundContainer.setVisibility(View.VISIBLE);
                    } else soundContainer.setVisibility(View.GONE);
                    break;
            }
        }
    }

    void setUpPermissionSettingsGuest(List<SubExtension> settings) {
        for (SubExtension setting : settings) {
            Timber.d("setUpPermissionSettingsGuest: %s", setting.getUniqueExtension());
            switch (setting.getUniqueExtension()) {
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_AdminPanel:
                    if (setting.isGuest()) {
                        tvCode.setVisibility(View.VISIBLE);
                    } else tvCode.setVisibility(View.GONE);
                    break;
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_Notifications:
                    if (setting.isGuest()) {
                        notificationContainer.setVisibility(View.VISIBLE);
                    } else notificationContainer.setVisibility(View.GONE);
                    break;
                case AppConstants.SECURE_SETTINGS_UNIQUE + AppConstants.SUB_Sound:
                    if (setting.isGuest()) {
                        soundContainer.setVisibility(View.VISIBLE);
                    } else soundContainer.setVisibility(View.GONE);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void showAlertDialog(final EditText input, final DialogInterface.OnClickListener onClickListener, final DialogInterface.OnClickListener onNegativeClick, String title) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(10, 10, 10, 10);

        input.setGravity(Gravity.CENTER);

        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        //input.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_secure_settings);
        input.setFocusable(true);
        input.setFocusableInTouchMode(true);
        input.clearFocus();
        input.requestFocus();
        input.postDelayed(() -> {
            InputMethodManager keyboard=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(input,0);
        }
                ,100);
//

        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onClickListener.onClick(dialog, which);
                        try {
//                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        alertDialog.setNegativeButton(R.string.cancel, (dialog, which) -> {
                    try {
                        if (onNegativeClick != null)
                            onNegativeClick.onClick(dialog, which);
//                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dialog.cancel();
                });

        alertDialog.show();

    }
}
