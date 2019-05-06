package com.secureMarket;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.settings.codeSetting.installApps.DownLoadAndInstallUpdate;
import com.screenlocker.secure.settings.codeSetting.installApps.InstallAppModel;
import com.screenlocker.secure.settings.codeSetting.installApps.List;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MarketFragment extends Fragment implements
        SecureMarketAdapter.AppInstallUpdateListener, SecureMarketActivity.SearchQueryListener {

    private String fragmentType;
    private RecyclerView rc;
    private ProgressBar progressBar;
    private java.util.List<List> appModelList;
    private PackageManager mPackageManager;
    private java.util.List<List> installedApps = new ArrayList<>();
    private java.util.List<List> unInstalledApps = new ArrayList<>();

    public MarketFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentType = getArguments().getString("check");
        appModelList = new ArrayList<>();
        mPackageManager = getActivity().getPackageManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.from(getActivity()).inflate(R.layout.fragment_market, container, false);
        rc = view.findViewById(R.id.appList);
        progressBar = view.findViewById(R.id.marketFragmentProgress);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();





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
                            Toast.makeText(getActivity(), "list is empty", Toast.LENGTH_SHORT).show();

                            progressBar.setVisibility(View.GONE);
                        }
                    });


        }
    }

    private void checkAppInstalledOrNot(java.util.List<List> list) {
        if (list != null && list.size() > 0) {
            for (com.screenlocker.secure.settings.codeSetting.installApps.List app :
                    list) {
                File file = getActivity().getFileStreamPath(app.getApk());
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
                    CommonUtils.splitName(app.getApk()), app.getApk(), getString(R.string.secure_market_activity));
            downLoadAndInstallUpdate.execute();

        });

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();

    }

    @Override
    public void onUnInstallClick(List app) {
        File fileApk = getActivity().getFileStreamPath(app.getApk());
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
}
