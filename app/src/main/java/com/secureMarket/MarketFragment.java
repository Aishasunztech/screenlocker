package com.secureMarket;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.settings.codeSetting.installApps.InstallAppModel;
import com.screenlocker.secure.settings.codeSetting.installApps.List;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.INSTALLED_PACKAGES;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.UNINSTALLED_PACKAGES;
import static com.secureMarket.MarketUtils.savePackages;

/**
 * A simple {@link Fragment} subclass.
 */
public class MarketFragment extends Fragment implements
        SecureMarketAdapter.AppInstallUpdateListener, SecureMarketActivity.SearchQueryListener {


    private String fragmentType;
    private RecyclerView rc;
    private ProgressBar progressBar;
    private TextView tvInfo;
    private java.util.List<List> appModelList;
    private PackageManager mPackageManager;
    private java.util.List<List> installedApps = new ArrayList<>();

    private java.util.List<List> unInstalledApps = new ArrayList<>();
    private ProgressDialog progressDialog;
    private String userSpace;
    private DownLoadAndInstallUpdate downLoadAndInstallUpdate;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("com.secure.systemcontrol.PACKAGE_ADDED_SECURE_MARKET")) {
                refreshList();
                String appName = intent.getStringExtra("appName");
                Snackbar snackbar = Snackbar.make(
                        ((ViewGroup) activity.findViewById(android.R.id.content))
                                .getChildAt(0)
                        , appName + " is installed"
                        , 3000);
                snackbar.show();
            } else if (intent.getAction() != null && intent.getAction().equals("com.secure.systemcontroll.PackageDeleted")) {
                if (intent.getBooleanExtra("SecureMarket", false)) {
                    refreshList();
                    String appName = intent.getStringExtra("label");
                    Snackbar snackbar = Snackbar.make(
                            ((ViewGroup) activity.findViewById(android.R.id.content))
                                    .getChildAt(0)
                            , appName + " is uninstalled"
                            , 3000);
                    snackbar.show();
                }
            }
        }
    };

    private void refreshList() {
        tvInfo.setVisibility(View.GONE);
        if (activity != null) {
            String dealerId = PrefUtils.getStringPref(activity, AppConstants.KEY_DEVICE_LINKED);
//        Log.d("ConnectedDealer",dealerId);
            if (dealerId == null || dealerId.equals("")) {
                getAdminApps();
            } else {
                getAllApps(dealerId);
            }
        }

    }


    public MarketFragment() {
        // Required empty public constructor
    }

    private Activity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.activity = (Activity) context;
    }

    @Override
    public void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.secure.systemcontrol.PACKAGE_ADDED_SECURE_MARKET");
        intentFilter.addAction("com.secure.systemcontroll.PackageDeleted");
        activity.registerReceiver(broadcastReceiver, intentFilter);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (activity != null) {
            activity.unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentType = getArguments().getString("check");
        appModelList = new ArrayList<>();
        mPackageManager = activity.getPackageManager();
        progressDialog = new ProgressDialog(activity);
        userSpace = PrefUtils.getStringPref(activity, CURRENT_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.from(activity).inflate(R.layout.fragment_market, container, false);
        rc = view.findViewById(R.id.appList);
        tvInfo = view.findViewById(R.id.tvNoDataFound);
        progressBar = view.findViewById(R.id.marketFragmentProgress);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        tvInfo.setVisibility(View.GONE);

        String dealerId = PrefUtils.getStringPref(activity, AppConstants.KEY_DEVICE_LINKED);
//        Log.d("ConnectedDealer",dealerId);
        if (dealerId == null || dealerId.equals("")) {
            getAdminApps();
        } else {
            getAllApps(dealerId);
        }


    }

    private void getAllApps(String dealerId) {

        if (CommonUtils.isNetworkAvailable(activity)) {
            ((MyApplication) activity.getApplicationContext())
                    .getApiOneCaller()
                    .getAllApps("marketApplist/" + dealerId)

                    .enqueue(new Callback<InstallAppModel>() {
                        @Override
                        public void onResponse(@NonNull Call<InstallAppModel> call, @NonNull Response<InstallAppModel> response) {
                            if (response.body() != null) {
                                if (response.body().isSuccess()) {

                                    appModelList.clear();
                                    appModelList.addAll(response.body().getList());
                                    installedApps.clear();
                                    unInstalledApps.clear();

                                    checkAppInstalledOrNot(appModelList);
                                    for (List app : appModelList) {
                                        if (app.isInstalled()) {

                                            if (isShow(app.getPackageName(), activity)) {
                                                installedApps.add(app);
                                            }

                                        } else {

                                            if (isShow(app.getPackageName(), activity)) {
                                                unInstalledApps.add(app);

                                            }
                                        }
                                    }

                                    if (fragmentType.equals("install")) {
                                        rc.setAdapter(new SecureMarketAdapter(unInstalledApps, activity, MarketFragment.this));
                                    } else if (fragmentType.equals("uninstall")) {
                                        rc.setAdapter(new SecureMarketAdapter(installedApps, activity, MarketFragment.this));
                                    }
                                    rc.setLayoutManager(new GridLayoutManager(activity, 1));
                                    tvInfo.setVisibility(View.GONE);

                                } else {
                                    if (response.body().getList() == null) {
                                        appModelList.clear();
                                        tvInfo.setVisibility(View.VISIBLE);

                                    }
                                }

                            }
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onFailure(Call<InstallAppModel> call, Throwable t) {
                            Toast.makeText(activity, "list is empty", Toast.LENGTH_SHORT).show();

                            progressBar.setVisibility(View.GONE);
                        }
                    });


        }
    }

    private void getAdminApps() {
        if (CommonUtils.isNetworkAvailable(activity)) {
            ((MyApplication) activity.getApplicationContext())
                    .getApiOneCaller()
                    .getAdminApps()

                    .enqueue(new Callback<InstallAppModel>() {
                        @Override
                        public void onResponse(Call<InstallAppModel> call, Response<InstallAppModel> response) {
                            if (response.body() != null) {
                                if (response.body().isSuccess()) {

                                    appModelList.clear();
                                    appModelList.addAll(response.body().getList());
                                    installedApps.clear();
                                    unInstalledApps.clear();

                                    checkAppInstalledOrNot(appModelList);
                                    for (List app : appModelList) {

                                        if (app.isInstalled()) {

                                            if (isShow(app.getPackageName(), activity)) {
                                                installedApps.add(app);
                                            }

                                        } else {
                                            if (isShow(app.getPackageName(), activity)) {
                                                unInstalledApps.add(app);
                                            }

                                        }
                                    }

                                    if (fragmentType.equals("install")) {
                                        rc.setAdapter(new SecureMarketAdapter(unInstalledApps, activity, MarketFragment.this));
                                    } else if (fragmentType.equals("uninstall")) {
                                        rc.setAdapter(new SecureMarketAdapter(installedApps, activity, MarketFragment.this));
                                    } else {
                                        rc.setAdapter(new SecureMarketAdapter(unInstalledApps, activity, MarketFragment.this));
                                    }
                                    rc.setLayoutManager(new GridLayoutManager(activity, 1));


                                } else {
                                    if (response.body().getList() == null) {
                                        appModelList.clear();

                                    }
                                }

                            }
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onFailure(Call<InstallAppModel> call, Throwable t) {

                            progressBar.setVisibility(View.GONE);
                        }
                    });


        }
    }

    private void checkAppInstalledOrNot(java.util.List<List> list) {
        if (list != null && list.size() > 0) {
            for (com.screenlocker.secure.settings.codeSetting.installApps.List app :
                    list) {
//                String fileName = app.getApk();
//                Log.d("APKNAME",app.getApk() + ": " + app.getPackageName());
////                File file = activity.getFileStreamPath(fileName);
//                File apksPath = new File(activity.getFilesDir(), "apk");
//                File file = new File(apksPath, fileName);
//                if (file.exists()) {
//                    String appPackageName = getAppLabel(mPackageManager, file.getAbsolutePath());
                String appPackageName = app.getPackageName();
                if (appPackageName != null)
                    app.setInstalled(appInstalledOrNot(appPackageName));
            }
//            }
        }

    }

    private boolean appInstalledOrNot(String uri) {
        try {
            mPackageManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void onInstallClick(List app) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle("Download");
        alertDialog.setIcon(android.R.drawable.stat_sys_download);

        alertDialog.setMessage("Are you sure you want to download this app?");

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {

            String live_url = PrefUtils.getStringPref(activity,LIVE_URL);

            downLoadAndInstallUpdate = new DownLoadAndInstallUpdate(activity, live_url+MOBILE_END_POINT + "getApk/" +
                    CommonUtils.splitName(app.getApk()), app.getApk(), progressDialog, app.getPackageName());
            downLoadAndInstallUpdate.execute();
            AppConstants.INSTALLING_APP_NAME = app.getApkName();
            AppConstants.INSTALLING_APP_PACKAGE = app.getPackageName();

        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();

    }

    @Override
    public void onUnInstallClick(List app, boolean status) {


        AppExecutor.getInstance().getMainThread().execute(() -> {
            if (!status) {
                Toast.makeText(activity, "Uninstall permission denied", Toast.LENGTH_LONG).show();
            } else {

                savePackages(app.getPackageName(), UNINSTALLED_PACKAGES, userSpace, activity);
                Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
//            intent.setData(Uri.parse("package:" + getAppLabel(mPackageManager, fileApk.getAbsolutePath())));
                intent.setData(Uri.parse("package:" + app.getPackageName()));
                activity.startActivity(intent);

//                try {
//                    PackageManager pm = activity.getPackageManager();
//                    pm.getPackageInfo("com.secure.systemcontrol", 0);
//                    if (activity != null) {
//
//
//                        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
//                        alertDialog.setTitle("Uninstall");
//                        alertDialog.setIcon(android.R.drawable.ic_delete
//                        );
//                        alertDialog.setMessage("Are you sure you want to uninstall this app?");
//
//                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {
//                            InstallModel installModel = new InstallModel(app.getApk(), app.getApkName(), app.getPackageName(), null, false, false, null, false);
//                            ArrayList<InstallModel> apps = new ArrayList<>();
//                            apps.add(installModel);
//                            String json = new Gson().toJson(apps);
//                            final Intent intent = new Intent();
//                            intent.setAction("com.secure.systemcontrol.DELETE_PACKAGES");
//                            intent.putExtra("json", json);
//                            intent.putExtra("SecureMarket", true);
//                            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
//                            intent.setComponent(new ComponentName("com.secure.systemcontrol", "com.secure.systemcontrol.receivers.PackageUninstallReceiver"));
//
//                            if (activity != null) {
//                                activity.sendBroadcast(intent);
//                            }
//                        });
//
//                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
//                                (dialog, which) -> dialog.dismiss());
//                        alertDialog.show();
//
//                    }
//
//                } catch (PackageManager.NameNotFoundException e) {
//
//                    savePackages(app.getPackageName(), UNINSTALLED_PACKAGES, userSpace, activity);
//                    Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
////            intent.setData(Uri.parse("package:" + getAppLabel(mPackageManager, fileApk.getAbsolutePath())));
//                    intent.setData(Uri.parse("package:" + app.getPackageName()));
//                    activity.startActivity(intent);
//
//                }
            }
        });


    }


    @Override
    public void searchOnSubmit(String query) {
        if (fragmentType.equals("install")) {

            searchUninstalledApps(query);

        } else if (fragmentType.equals("uninstall")) {

            searchInstalledApps(query);
        }
    }

    @Override
    public void searchOnQueryChange(String query) {
        if (fragmentType.equals("install")) {

            searchUninstalledApps(query);

        } else if (fragmentType.equals("uninstall")) {

            searchInstalledApps(query);
        }
    }

    private void searchInstalledApps(String query) {
        if (installedApps.size() > 0) {
            if (!query.equals("")) {
                java.util.List<List> searchedList = new ArrayList<>();
                for (List app : installedApps) {
                    String apkName = app.getApkName().toLowerCase();
                    if (apkName.contains(query)) {
                        searchedList.add(app);
                    }
                }
                rc.setAdapter(new SecureMarketAdapter(searchedList, activity, MarketFragment.this));
                rc.setLayoutManager(new GridLayoutManager(activity, 1));
            } else {
                rc.setAdapter(new SecureMarketAdapter(installedApps, activity, MarketFragment.this));
                rc.setLayoutManager(new GridLayoutManager(activity, 1));
            }

        }
    }

    private void searchUninstalledApps(String query) {
        if (unInstalledApps.size() > 0) {
            if (!query.equals("")) {
                java.util.List<List> searchedList = new ArrayList<>();
                for (List app : unInstalledApps) {
                    String apkName = app.getApkName().toLowerCase();
                    if (apkName.contains(query)) {
                        searchedList.add(app);
                    }
                }
                rc.setAdapter(new SecureMarketAdapter(searchedList, activity, MarketFragment.this));
                rc.setLayoutManager(new GridLayoutManager(activity, 1));
            } else {
                rc.setAdapter(new SecureMarketAdapter(unInstalledApps, activity, MarketFragment.this));
                rc.setLayoutManager(new GridLayoutManager(activity, 1));
            }

        }
    }

    private static class DownLoadAndInstallUpdate extends AsyncTask<Void, Integer, Uri> {
        private String appName, url;
        private WeakReference<Activity> contextWeakReference;
        private ProgressDialog dialog;
        private String userType, packageName;
        private boolean isCanceled = false;
        private File file;


        DownLoadAndInstallUpdate(Activity context, final String url, String appName, ProgressDialog dialog, String packageName) {
            contextWeakReference = new WeakReference<>(context);
            this.url = url;
            this.appName = appName;
            this.dialog = dialog;
            this.packageName = packageName;
            userType = PrefUtils.getStringPref(contextWeakReference.get(), CURRENT_KEY);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            dialog = new ProgressDialog(contextWeakReference.get());
            dialog.setTitle("Downloading App, Please Wait");
            dialog.setCancelable(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    isCanceled = true;
                }
            });
//
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

                File apksPath = new File(contextWeakReference.get().getFilesDir(), "apk");
                file = new File(apksPath, appName);
//                File file = new File(Environment.getExternalStorageDirectory() + "/" + appName);
                if (!apksPath.exists()) {
                    apksPath.mkdir();
                }

                if (file.exists())
                    return FileProvider.getUriForFile(contextWeakReference.get(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
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
                        if(!isCanceled) {
                            total += count;
                            int value = (int) ((total * 100) / contentLength);
                            publishProgress(value);
                            fileOutputStream.write(data, 0, count);
                        }else{
                            file.delete();
                            break;
                        }
                    }

                    return FileProvider.getUriForFile(contextWeakReference.get(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
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
                    file.setReadable(true, false);


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
            Log.d("kikhfihfihdihso", "onPostExecute: " + uri);
            if (dialog != null)
                dialog.dismiss();
            if (uri != null  && !isCanceled) {
                showInstallDialog(uri, packageName);
            }

        }

        private void showInstallDialog(Uri uri, String packageName) {

            savePackages(packageName, INSTALLED_PACKAGES, userType, contextWeakReference.get());

            Timber.d("packageName: %s", packageName);

            Intent intent = ShareCompat.IntentBuilder.from((Activity) contextWeakReference.get())
                    .setStream(uri) // uri from FileProvider
                    .setType("text/html")
                    .getIntent()
                    .setAction(Intent.ACTION_VIEW) //Change if needed
                    .setDataAndType(uri, "application/vnd.android.package-archive")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            contextWeakReference.get().startActivity(intent);

//            try {
//                PackageManager pm = contextWeakReference.get().getPackageManager();
//                pm.getPackageInfo("com.secure.systemcontrol", 0);
//                if (!AppConstants.INSTALLING_APP_NAME.equals("") && !AppConstants.INSTALLING_APP_PACKAGE.equals("")) {
//                    AlertDialog alertDialog = new AlertDialog.Builder(contextWeakReference.get()).create();
//                    alertDialog.setTitle(AppConstants.INSTALLING_APP_NAME);
//
//
//                    alertDialog.setMessage("Are you sure you want to install this app?");
//
//                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "INSTALL", (dialog, which) -> {
//
//                        Intent launchIntent = new Intent();
//                        ComponentName componentName = new ComponentName("com.secure.systemcontrol", "com.secure.systemcontrol.MainActivity");
////                        launchIntent.setAction(Intent.ACTION_VIEW);
//                        launchIntent.setAction(Intent.ACTION_MAIN);
//                        launchIntent.setComponent(componentName);
//                        launchIntent.setData(uri);
//                        launchIntent.putExtra("package", AppConstants.INSTALLING_APP_PACKAGE);
//                        launchIntent.putExtra("user_space", userType);
//                        launchIntent.putExtra("SecureMarket", true);
//                        launchIntent.putExtra("appName", AppConstants.INSTALLING_APP_NAME);
//                        launchIntent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
////            contextWeakReference.get().sendBroadcast(sender);
//
//                        contextWeakReference.get().startActivity(launchIntent);
//                        Snackbar snackbar = Snackbar.make(
//                                ((ViewGroup) contextWeakReference.get().findViewById(android.R.id.content))
//                                        .getChildAt(0)
//                                , contextWeakReference.get().getString(R.string.install_app_message)
//                                , 3000);
//
//                        snackbar.show();
//
//                    });
//
//                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
//                            (dialog, which) -> dialog.dismiss());
//                    alertDialog.show();
//                }
//            } catch (PackageManager.NameNotFoundException e) {
//
//                savePackages(packageName, INSTALLED_PACKAGES, userType, contextWeakReference.get());
//
//                Intent intent = ShareCompat.IntentBuilder.from((Activity) contextWeakReference.get())
//                        .setStream(uri) // uri from FileProvider
//                        .setType("text/html")
//                        .getIntent()
//                        .setAction(Intent.ACTION_VIEW) //Change if needed
//                        .setDataAndType(uri, "application/vnd.android.package-archive")
//                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                contextWeakReference.get().startActivity(intent);
//            } catch (Exception e) {
//                Timber.e(e);
//            }

//

        }


    }

    @Override
    public void onPause() {
        super.onPause();
        progressDialog.dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        progressDialog.dismiss();
    }


    private boolean isShow(String packageName, Activity activity) {
        if (packageName.equals(activity.getPackageName())) {
            return false;
        }
        if (packageName.equals("com.secure.systemcontrol")) {
            return false;
        }
        return true;
    }
}
