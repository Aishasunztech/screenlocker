package com.screenlocker.secure.manual_load;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.socket.model.InstallModel;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.KEY_SUPPORT_PASSWORD;

public class ManualPullPush extends AppCompatActivity implements ManualPushPullAdapter.PushPullAppsListener, MainActivity.PolicyRefreshListener {

    public static final String PUSH_APP = "push_apps";
    public static final String PULL_APP = "pull_apps";

    @BindView(R.id.recyclerViewManualList)
    RecyclerView recyclerView;
    ManualPushPullAdapter manualPushPullAdapter;

    private static String app_check_Install = null;
    private static String app_check_uninstall = null;
    public static int UNINSTALL_REQUEST_CODE = 100;

    private static InstallModel installModell = null;


    private static final String TAG = "checkpolicy";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_pull_push);
        ButterKnife.bind(this);
        MainActivity.policyRefreshListener = ManualPullPush.this;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        manualPushPullAdapter = new ManualPushPullAdapter(ManualPullPush.this, new ArrayList<>(), ManualPullPush.this);
        setRecyclerAdapter();

    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<InstallModel> appsList = utils.getArrayList(ManualPullPush.this);
        if (appsList != null) {
            if (app_check_Install != null) {
                if (isPackageInstalled(app_check_Install, getPackageManager())) {
                    if (installModell != null) {
                        enterAppData();
                    }
                    if (appsList.size() > 0) {
                        for (int i = 0; i < appsList.size(); i++) {
                            if (app_check_Install.equals(appsList.get(i).getPackage_name())) {
                                appsList.remove(i);
                                break;
                            }
                        }
                    }
                    app_check_Install = null;

                    utils.saveArrayList(appsList, ManualPullPush.this);
                    if (appsList.size() > 0) {
                        setRecyclerAdapter();
                    }
                    if (appsList.size() == 0) {
                        finish();
                    }
                }
            } else {
                if (appsList.size() > 0) {
                    for (InstallModel model : appsList) {
                        Log.i("checkpolicy", "onResume: model is : ... " + model.getApk());
                    }
                    setRecyclerAdapter();
                } else {
                    finish();
                }
            }

        } else {
            finish();
        }
    }

    private void setRecyclerAdapter() {

        if (PrefUtils.getStringPref(this, CURRENT_KEY).equals(KEY_SUPPORT_PASSWORD)) {
            finish();
        }
        ArrayList<InstallModel> list = utils.getArrayList(ManualPullPush.this);


        if (list != null) {
            for (InstallModel model : list) {
                Log.i(TAG, "setRecyclerAdapter: apk name is ////// ...  " + model.getApk_name());
            }
            Log.i(TAG, "setRecyclerAdapter: for size ... : " + list.size());
            for (InstallModel model : list) {
                Log.i(TAG, "setRecyclerAdapter: list model is : .. package name is  ... " + model.getPackage_name() + " ... app name is  ... " + model.getApk_name());
            }
            if (list.size() > 0) {
                if (recyclerView != null) {
                    manualPushPullAdapter = new ManualPushPullAdapter(ManualPullPush.this, list, ManualPullPush.this);
                    recyclerView.setAdapter(manualPushPullAdapter);
                    recyclerView.setHasFixedSize(true);
                }

            } else {
                finish();
            }

        } else {
            Log.i(TAG, "setRecyclerAdapter: list null in ManualPullPush adapter");
        }


    }

    @Override
    public void appTextButtonClick(int position, InstallModel installModel) {
        ArrayList<InstallModel> appsList = utils.getArrayList(ManualPullPush.this);


        Timber.d("<< appTextButtonClick >>>");
        Timber.d(String.valueOf(position));

        if (installModel.getPackage_name() != null) {
            Timber.d(installModel.getPackage_name());
        }

        if (PUSH_APP.equals(installModel.getType_operation())) {
            installModell = installModel;
            appInstallRequest(installModel.getApk(), installModel.getPackage_name());
        }

        if (PULL_APP.equals(installModel.getType_operation())) {
            if (isPackageInstalled(installModel.getPackage_name(), getPackageManager())) {
                appUninstallRequest(installModel.getPackage_name());
            } else {
                Log.i(TAG, "appTextButtonClick: else condition app not installed ... first  size is : .. " + appsList.size());
                app_check_uninstall = installModel.getPackage_name();
                if (appsList.size() > 0) {

                    ArrayList<InstallModel> tempApps = new ArrayList<>();

                    for (int i = 0; i < appsList.size(); i++) {
                        if (app_check_uninstall.equals(appsList.get(i).getPackage_name())) {
                            Log.i(TAG, "appTextButtonClick: else condition app not installed ... removing package is : .. " + appsList.get(i).getPackage_name());
                        } else {
                            tempApps.add(appsList.get(i));
                        }
                    }

                    utils.saveArrayList(tempApps, this);
                    Log.i(TAG, "appTextButtonClick: else condition app not installed ... second  size is : .. " + tempApps.size());
                }

                setRecyclerAdapter();
            }

        }

    }


    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        boolean found = true;

        HashSet<String> packages = new HashSet<>();
        packages.add("com.paraphron.youtube");

        if (packages.contains(packageName) && getResources().getString(R.string.apktype).equals("BYOD")) {
            return found;
        }

        try {
            packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            found = false;
        }

        return found;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UNINSTALL_REQUEST_CODE) {

            if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "onActivityResult: user canceled the (un)install");
                Toast.makeText(this, "application unInstall Request Canceled", Toast.LENGTH_SHORT).show();

            } else {
                new Thread(() -> MyApplication.getAppDatabase(ManualPullPush.this).getDao().deletePackage(app_check_uninstall)).start();
                ArrayList<InstallModel> appsList = utils.getArrayList(ManualPullPush.this);
                if (appsList.size() > 0) {
                    for (int i = 0; i < appsList.size(); i++) {
                        if (app_check_uninstall.equals(appsList.get(i).getPackage_name())) {
                            appsList.remove(i);
                            break;
                        }
                    }
                }
                utils.saveArrayList(appsList, ManualPullPush.this);
                setRecyclerAdapter();
            }
        }
    }

    private void appUninstallRequest(String app_pkg_name) {
        app_check_uninstall = app_pkg_name;
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.setData(Uri.parse("package:" + app_pkg_name));
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        startActivityForResult(intent, UNINSTALL_REQUEST_CODE);
    }

    private void appInstallRequest(String app_uri, String app_pkg_name) {
        app_check_Install = app_pkg_name;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (getPackageManager().canRequestPackageInstalls()) {
                Intent intent = ShareCompat.IntentBuilder.from(ManualPullPush.this)
                        .setStream(Uri.parse(app_uri))
                        .setText("text/html")
                        .getIntent()
                        .putExtra(Intent.EXTRA_RETURN_RESULT, true)
                        .setAction(Intent.ACTION_VIEW)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .setDataAndType(Uri.parse(app_uri), "application/vnd.android.package-archive");
                startActivity(intent);
            } else {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName()));
                intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                startActivity(intent);

            }
        }
    }

    @Override
    public void onBackPressed() {

        ArrayList<InstallModel> list = utils.getArrayList(ManualPullPush.this);
        if (list != null) {
            if (list.size() > 0) {
                Toast.makeText(this, "Please Complete Operation", Toast.LENGTH_SHORT).show();
            } else {
                finish();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void refreshPolicy() {
        setRecyclerAdapter();
    }

    private void enterAppData() {

        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(installModell.getPackage_name(), 0);
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> allApps = pm.queryIntentActivities(intent, 0);


            for (ResolveInfo ri : allApps) {
                if (ri.activityInfo.packageName.equals(installModell.getPackage_name())) {
                    AppInfo app = new AppInfo(String.valueOf(ri.loadLabel(pm)),
                            ri.activityInfo.packageName, CommonUtils.convertDrawableToByteArray(ri.activityInfo.loadIcon(pm)));
                    app.setUniqueName(app.getPackageName());
                    app.setExtension(false);
                    app.setDefaultApp(false);
                    app.setEncrypted(installModell.isEncrypted());
                    app.setGuest(installModell.isGuest());
                    app.setVisible(true);
                    app.setEnable(installModell.isEnable());
                    new Thread(() -> MyApplication.getAppDatabase(ManualPullPush.this).getDao().insertApps(app)).start();
                }
            }


        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void code_secret(View view) {
        utils.saveArrayList(null, this);
        finish();
    }

    ///   admin!dev#lm@786
}
