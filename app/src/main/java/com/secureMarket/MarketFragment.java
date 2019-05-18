package com.secureMarket;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.settings.codeSetting.installApps.InstallAppModel;
import com.screenlocker.secure.settings.codeSetting.installApps.List;
import com.screenlocker.secure.socket.model.InstallModel;
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

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;

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
    private java.util.List<List> guestApps = new ArrayList<>();
    private java.util.List<List> encryptedApps = new ArrayList<>();

    private java.util.List<List> unInstalledApps = new ArrayList<>();
    private ProgressDialog progressDialog;
    private String userSpace;


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals("com.secure.systemcontrol.PACKAGE_ADDED_SECURE_MARKET")) {
                refreshList();
                String appName = intent.getStringExtra("appName");
                Snackbar snackbar = Snackbar.make(
                        ((ViewGroup) getActivity().findViewById(android.R.id.content))
                                .getChildAt(0)
                        , appName + " is installed"
                        , 3000);
                snackbar.show();
            } else if (intent.getAction() != null && intent.getAction().equals("com.secure.systemcontroll.PackageDeleted")) {
                if (intent.getBooleanExtra("SecureMarket", false)) {
                    refreshList();
                    String appName = intent.getStringExtra("label");
                    Snackbar snackbar = Snackbar.make(
                            ((ViewGroup) getActivity().findViewById(android.R.id.content))
                                    .getChildAt(0)
                            , appName + " is uninstalled"
                            , 3000);
                    snackbar.show();
                }
            }
        }
    };

    public MarketFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.secure.systemcontrol.PACKAGE_ADDED_SECURE_MARKET");
            intentFilter.addAction("com.secure.systemcontroll.PackageDeleted");
            getActivity().registerReceiver(broadcastReceiver, intentFilter);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() != null) {
            getActivity().unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentType = getArguments().getString("check");
        appModelList = new ArrayList<>();
        mPackageManager = getActivity().getPackageManager();
        progressDialog = new ProgressDialog(getActivity());
        userSpace = PrefUtils.getStringPref(getActivity(), CURRENT_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.from(getActivity()).inflate(R.layout.fragment_market, container, false);
        rc = view.findViewById(R.id.appList);
        tvInfo = view.findViewById(R.id.tvNoDataFound);
        progressBar = view.findViewById(R.id.marketFragmentProgress);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();


    }

    private void refreshList() {
        tvInfo.setVisibility(View.GONE);

        String dealerId = PrefUtils.getStringPref(getActivity(), AppConstants.KEY_DEVICE_LINKED);
//        Log.d("ConnectedDealer",dealerId);
        if (dealerId == null || dealerId.equals("")) {
            getAdminApps();
        } else {
            getAllApps(dealerId);
        }
    }

    private void getAllApps(String dealerId) {
        Log.d("lakshdf", "getallApps");
        if (CommonUtils.isNetworkAvailable(getActivity())) {
            ((MyApplication) getActivity().getApplicationContext())
                    .getApiOneCaller()
                    .getAllApps("marketApplist/" + dealerId)

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
                                        Log.d("AppsList", app.getApkName());
                                        if (app.isInstalled()) {

//                                            new Thread(() -> {
//                                                boolean isGuest = MyApplication.getAppDatabase(MyApplication.getAppContext()).getDao().getAppUserSpace(app.getPackageName());
//                                                Log.d("ISGUEST", isGuest + "");
//                                                if (userSpace.equals(KEY_GUEST_PASSWORD)) {
//                                                    if (isGuest) {
//                                                        installedApps.add(app);
//                                                    }
//                                                } else if (userSpace.equals(KEY_MAIN_PASSWORD)) {
//                                                    if (!isGuest) {
//                                                        installedApps.add(app);
//                                                    }
//                                                }
//                                            }).start();

                                            installedApps.add(app);


                                        } else {

                                            unInstalledApps.add(app);
                                        }
                                    }

                                    if (fragmentType.equals("install")) {
                                        rc.setAdapter(new SecureMarketAdapter(unInstalledApps, getActivity(), MarketFragment.this, fragmentType));
                                    } else if (fragmentType.equals("uninstall")) {
//                                        new Thread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                for (List app : installedApps) {
//                                                    boolean isGuest = MyApplication.getAppDatabase(MyApplication.getAppContext()).getDao().getAppUserSpace(app.getPackageName());
//                                                    Log.d("Guest", "CheckGuest");
//                                                    if (isGuest) {
//                                                        guestApps.add(app);
//                                                    } else {
//                                                        encryptedApps.add(app);
//                                                    }
//
//                                                    switch (userSpace) {
//                                                        case KEY_GUEST_PASSWORD:
//                                                            getActivity().runOnUiThread(new Runnable() {
//                                                                @Override
//                                                                public void run() {
//                                                                    rc.setAdapter(new SecureMarketAdapter(guestApps, getActivity(), MarketFragment.this, fragmentType));
//
//                                                                }
//                                                            });
//                                                            break;
//                                                        case KEY_MAIN_PASSWORD:
//                                                            rc.setAdapter(new SecureMarketAdapter(encryptedApps, getActivity(), MarketFragment.this, fragmentType));
//
//                                                            break;
//                                                        default:
//                                                            break;
//                                                    }
//                                                }
//                                            }
//                                        }).start();
                                        rc.setAdapter(new SecureMarketAdapter(installedApps, getActivity(), MarketFragment.this, fragmentType));

                                    } else {
                                        rc.setAdapter(new SecureMarketAdapter(unInstalledApps, getActivity(), MarketFragment.this, fragmentType));
                                    }
                                    rc.setLayoutManager(new GridLayoutManager(getActivity(), 1));
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
                            Toast.makeText(getActivity(), "list is empty", Toast.LENGTH_SHORT).show();

                            progressBar.setVisibility(View.GONE);
                        }
                    });


        }
    }

    private void getAdminApps() {
        if (CommonUtils.isNetworkAvailable(getActivity())) {
            ((MyApplication) getActivity().getApplicationContext())
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
                                        Log.d("AppsList", app.getApkName());
                                        if (app.isInstalled()) {
//                                            new Thread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    boolean isGuest = MyApplication.getAppDatabase(MyApplication.getAppContext()).getDao().getAppUserSpace(app.getPackageName());
//                                                    Log.d("ISGUEST",isGuest + "");
//                                                    if(userSpace.equals(KEY_GUEST_PASSWORD))
//                                                    {
//                                                        if(isGuest)
//                                                        {
//                                                            installedApps.add(app);
//                                                        }
//                                                    }else if(userSpace.equals(KEY_MAIN_PASSWORD))
//                                                    {
//                                                        if(!isGuest)
//                                                        {
//                                                            installedApps.add(app);
//                                                        }
//                                                    }
//                                                }
//                                            }).start();
                                            installedApps.add(app);

                                        } else {
                                            unInstalledApps.add(app);
                                        }
                                    }

                                    if (fragmentType.equals("install")) {
                                        rc.setAdapter(new SecureMarketAdapter(unInstalledApps, getActivity(), MarketFragment.this, fragmentType));
                                    } else if (fragmentType.equals("uninstall")) {
                                        rc.setAdapter(new SecureMarketAdapter(installedApps, getActivity(), MarketFragment.this, fragmentType));
                                    } else {
                                        rc.setAdapter(new SecureMarketAdapter(unInstalledApps, getActivity(), MarketFragment.this, fragmentType));
                                    }
                                    rc.setLayoutManager(new GridLayoutManager(getActivity(), 1));


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
////                File file = getActivity().getFileStreamPath(fileName);
//                File apksPath = new File(getActivity().getFilesDir(), "apk");
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

    public String getAppLabel(PackageManager pm, String pathToApk) {
        PackageInfo packageInfo = pm.getPackageArchiveInfo(pathToApk, 0);
        if (packageInfo != null) {

            if (Build.VERSION.SDK_INT >= 8) {
                // those two lines do the magic:
                packageInfo.applicationInfo.sourceDir = pathToApk;
                packageInfo.applicationInfo.publicSourceDir = pathToApk;
            }

            CharSequence label = pm.getApplicationLabel(packageInfo.applicationInfo);
            Timber.e("getAppLabel: package name is " + packageInfo.packageName);
            return packageInfo.packageName;

        } else {
            return null;
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

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Download");
        alertDialog.setIcon(android.R.drawable.stat_sys_download);
        alertDialog.setMessage("Are you sure you want to download this app?");

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {

            DownLoadAndInstallUpdate downLoadAndInstallUpdate = new DownLoadAndInstallUpdate(getActivity(), AppConstants.STAGING_BASE_URL + "getApk/" +
                    CommonUtils.splitName(app.getApk()), app.getApk(), progressDialog);
            downLoadAndInstallUpdate.execute();
            AppConstants.INSTALLING_APP_NAME = app.getApkName();
            AppConstants.INSTALLING_APP_PACKAGE = app.getPackageName();

        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();

    }

    @Override
    public void onUnInstallClick(List app) {


        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Uninstall");
        alertDialog.setIcon(android.R.drawable.ic_delete
        );
        alertDialog.setMessage("Are you sure you want to uninstall this app?");

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {
            InstallModel installModel = new InstallModel(app.getApk(), app.getApkName(), app.getPackageName(), null, false, false, null, false);
            ArrayList<InstallModel> apps = new ArrayList<>();
            apps.add(installModel);
            String json = new Gson().toJson(apps);
            final Intent intent = new Intent();
            intent.setAction("com.secure.systemcontrol.DELETE_PACKAGES");
            intent.putExtra("json", json);
            intent.putExtra("SecureMarket", true);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.setComponent(new ComponentName("com.secure.systemcontrol", "com.secure.systemcontrol.receivers.PackageUninstallReceiver"));

            if (getActivity() != null) {
                getActivity().sendBroadcast(intent);
            }
        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();


//        String fileName = app.getApk();
//        File dir = new File(getActivity().getFilesDir(),"apk");
//
//        File fileApk = new File(dir,fileName);
//        if (fileApk.exists()) {
//        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
//            intent.setData(Uri.parse("package:" + getAppLabel(mPackageManager, fileApk.getAbsolutePath())));
//        intent.setData(Uri.parse("package:" + app.getPackageName()));

//        getActivity().startActivity(intent);
//        }
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
                rc.setAdapter(new SecureMarketAdapter(searchedList, getActivity(), MarketFragment.this, fragmentType));
                rc.setLayoutManager(new GridLayoutManager(getActivity(), 1));
            } else {
                rc.setAdapter(new SecureMarketAdapter(installedApps, getActivity(), MarketFragment.this, fragmentType));
                rc.setLayoutManager(new GridLayoutManager(getActivity(), 1));
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
                rc.setAdapter(new SecureMarketAdapter(searchedList, getActivity(), MarketFragment.this, fragmentType));
                rc.setLayoutManager(new GridLayoutManager(getActivity(), 1));
            } else {
                rc.setAdapter(new SecureMarketAdapter(unInstalledApps, getActivity(), MarketFragment.this, fragmentType));
                rc.setLayoutManager(new GridLayoutManager(getActivity(), 1));
            }

        }
    }

    private static class DownLoadAndInstallUpdate extends AsyncTask<Void, Integer, Uri> {
        private String appName, url;
        private WeakReference<Activity> contextWeakReference;
        private ProgressDialog dialog;
        private String userType;


        DownLoadAndInstallUpdate(Activity context, final String url, String appName, ProgressDialog dialog) {
            contextWeakReference = new WeakReference<>(context);
            this.url = url;
            this.appName = appName;
            this.dialog = dialog;
            userType = PrefUtils.getStringPref(contextWeakReference.get(), CURRENT_KEY);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            dialog = new ProgressDialog(contextWeakReference.get());
            dialog.setTitle("Downloading App, Please Wait");
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

                File apksPath = new File(contextWeakReference.get().getFilesDir(), "apk");
                File file = new File(apksPath, appName);
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
                        total += count;
                        publishProgress((int) ((total * 100) / contentLength));
                        fileOutputStream.write(data, 0, count);
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
            if (uri != null) {
                showInstallDialog(uri);
            }

        }

        private void showInstallDialog(Uri uri) {
            if (!AppConstants.INSTALLING_APP_NAME.equals("") && !AppConstants.INSTALLING_APP_PACKAGE.equals("")) {
                AlertDialog alertDialog = new AlertDialog.Builder(contextWeakReference.get()).create();
                alertDialog.setTitle(AppConstants.INSTALLING_APP_NAME);


                alertDialog.setMessage("Are you sure you want to install this app?");

                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "INSTALL", (dialog, which) -> {

                    Intent launchIntent = new Intent();
                    ComponentName componentName = new ComponentName("com.secure.systemcontrol", "com.secure.systemcontrol.MainActivity");
//                        launchIntent.setAction(Intent.ACTION_VIEW);
                    launchIntent.setAction(Intent.ACTION_MAIN);
                    launchIntent.setComponent(componentName);
                    launchIntent.setData(uri);
                    launchIntent.putExtra("package", AppConstants.INSTALLING_APP_PACKAGE);
                    launchIntent.putExtra("user_space", userType);
                    launchIntent.putExtra("SecureMarket", true);
                    launchIntent.putExtra("appName", AppConstants.INSTALLING_APP_NAME);
                    launchIntent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
//            contextWeakReference.get().sendBroadcast(sender);

                    contextWeakReference.get().startActivity(launchIntent);
                    Snackbar snackbar = Snackbar.make(
                            ((ViewGroup) contextWeakReference.get().findViewById(android.R.id.content))
                                    .getChildAt(0)
                            , contextWeakReference.get().getString(R.string.install_app_message)
                            , 3000);

                    snackbar.show();

                });

                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                        (dialog, which) -> dialog.dismiss());
                alertDialog.show();
            }
//            Intent intent = ShareCompat.IntentBuilder.from((Activity) contextWeakReference.get())
//                    .setStream(uri) // uri from FileProvider
//                    .setType("text/html")
//                    .getIntent()
//                    .setAction(Intent.ACTION_VIEW) //Change if needed
//                    .setDataAndType(uri, "application/vnd.android.package-archive")
//                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            contextWeakReference.get().startActivity(intent);

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
}
