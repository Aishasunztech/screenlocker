package com.secureMarket;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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

import android.os.Environment;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.settings.codeSetting.installApps.DownLoadAndInstallUpdate;
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

import static android.content.Context.MODE_PRIVATE;

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

    public MarketFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentType = getArguments().getString("check");
        appModelList = new ArrayList<>();
        mPackageManager = getActivity().getPackageManager();
        progressDialog = new ProgressDialog(getActivity());
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
        tvInfo.setVisibility(View.GONE);

        String dealerId = PrefUtils.getStringPref(getActivity(),AppConstants.KEY_DEVICE_LINKED);
//        Log.d("ConnectedDealer",dealerId);
        if(dealerId == null || dealerId.equals(""))
        {
            getAdminApps();
        }
        else{
            getAllApps(dealerId);
        }


    }

    private void getAllApps(String dealerId) {
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
                                        Log.d("AppsList",app.getApkName());
                                        if (app.isInstalled()) {
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
                                        Log.d("AppsList",app.getApkName());
                                        if (app.isInstalled()) {
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
                String fileName = app.getApk().substring(0,(app.getApk().length()-4));
                File file = getActivity().getFileStreamPath(fileName);
                if (file.exists()) {
                    String appPackageName = getAppLabel(mPackageManager, file.getAbsolutePath());
                    if (appPackageName != null)
                        app.setInstalled(appInstalledOrNot(appPackageName));
                }
            }
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
        alertDialog.setIcon(R.drawable.ic_info_black_24dp);

        alertDialog.setMessage("Are you sure you want to download this app?");

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", (dialog, which) -> {

            DownLoadAndInstallUpdate downLoadAndInstallUpdate = new DownLoadAndInstallUpdate(getActivity(), AppConstants.STAGING_BASE_URL + "/getApk/" +
                    CommonUtils.splitName(app.getApk()), app.getApk(),progressDialog);
            downLoadAndInstallUpdate.execute();

        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();

    }

    @Override
    public void onUnInstallClick(List app) {
        String fileName = app.getApk().substring(0,(app.getApk().length()-4));
        File fileApk = getActivity().getFileStreamPath(fileName);
        if (fileApk.exists()) {
            Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
            intent.setData(Uri.parse("package:" + getAppLabel(mPackageManager, fileApk.getAbsolutePath())));

            getActivity().startActivity(intent);
        }
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

    private static class DownLoadAndInstallUpdate extends AsyncTask<Void, Integer, Boolean> {
        private String appName, url;
        private WeakReference<Context> contextWeakReference;
        private ProgressDialog dialog;


        DownLoadAndInstallUpdate(Context context, final String url, String appName,ProgressDialog dialog) {
            contextWeakReference = new WeakReference<>(context);
            this.url = url;
            this.appName = appName;
            this.dialog = dialog;
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
        protected Boolean doInBackground(Void... voids) {
            return downloadApp();
        }


        private Boolean downloadApp() {
            FileOutputStream fileOutputStream = null;
            InputStream input = null;
            try {
                appName = appName.substring(0,(appName.length() -4));
//                File file = contextWeakReference.get().getFileStreamPath(appName);
                File file = new File(Environment.getExternalStorageDirectory() + "/" + appName);

                if (file.exists())
                    return true;
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
                    file.setReadable(true, false);


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


            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri,
                    "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);


            contextWeakReference.get().startActivity(intent);
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
