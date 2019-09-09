package com.secureMarket.ui.home;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.screenlocker.secure.R;
import com.screenlocker.secure.settings.codeSetting.installApps.ServerAppInfo;
import com.secureMarket.AppInstallUpdateListener;
import com.secureMarket.SecureMarketActivity;
import com.secureMarket.SecureMarketAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import timber.log.Timber;

public class InstalledAppsFragment extends Fragment implements AppInstallUpdateListener, SecureMarketActivity.SearchQueryListener {


    private AppInstallUpdateListener mListener;
    private RecyclerView rc;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<ServerAppInfo> installedApps = new ArrayList<>();
    private String url, fileName = "";
    private SecureMarketAdapter installedAdapter;
    private SharedViwModel viwModel;


    private void refreshList() {

    }


    public InstalledAppsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        installedAdapter = new SecureMarketAdapter(installedApps, getContext(), this, "install");
        viwModel = ViewModelProviders.of(getActivity()).get(SharedViwModel.class);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_market, container, false);
        rc = view.findViewById(R.id.appList);

        rc.setAdapter(installedAdapter);
        ((SimpleItemAnimator) rc.getItemAnimator()).setSupportsChangeAnimations(false);
        rc.setLayoutManager(new LinearLayoutManager(container.getContext()));

        swipeRefreshLayout = view.findViewById(R.id.refresh_Market);
        swipeRefreshLayout.setOnRefreshListener(this::refreshList);


        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viwModel.getInstalled().observe(this, serverAppInfos -> {
            Timber.d("setupApps: %s", serverAppInfos.size());
            installedApps.clear();
            installedApps.addAll(serverAppInfos);
            installedAdapter.setItems(installedApps);
            installedAdapter.notifyDataSetChanged();
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onInstallClick(ServerAppInfo app, int position, boolean isUpddte) {
        mListener.onInstallClick(app, position, isUpddte);
    }


    @Override
    public void onUnInstallClick(ServerAppInfo app, boolean status) {

        mListener.onUnInstallClick(app, status);
    }


    @Override
    public void searchOnSubmit(String query) {
        searchUninstalledApps(query);
    }

    @Override
    public void searchOnQueryChange(String query) {
        searchUninstalledApps(query);
    }

    /*private void searchInstalledApps(String query) {
        if (installedApps.size() > 0) {
            if (!query.equals("")) {
                java.util.List<ServerAppInfo> searchedServerAppInfo = new ArrayList<>();
                for (ServerAppInfo app : installedApps) {
                    String apkName = app.getApkName().toLowerCase();
                    if (apkName.contains(query)) {
                        searchedServerAppInfo.add(app);
                    }
                }
                rc.setAdapter(new SecureMarketAdapter(searchedServerAppInfo, activity, MarketFragment.this, fragmentType));
                rc.setLayoutManager(new GridLayoutManager(activity, 1));
            } else {
                rc.setAdapter(new SecureMarketAdapter(installedApps, activity, MarketFragment.this, fragmentType));
                rc.setLayoutManager(new GridLayoutManager(activity, 1));
            }

        }
    }

    private void searchUpdateApps(String query) {
        if (installedApps.size() > 0) {
            if (!query.equals("")) {
                java.util.List<ServerAppInfo> searchedServerAppInfo = new ArrayList<>();
                for (ServerAppInfo app : updateApps) {
                    String apkName = app.getApkName().toLowerCase();
                    if (apkName.contains(query)) {
                        searchedServerAppInfo.add(app);
                    }
                }
                rc.setAdapter(new SecureMarketAdapter(searchedServerAppInfo, activity, MarketFragment.this, fragmentType));
                rc.setLayoutManager(new GridLayoutManager(activity, 1));
            } else {
                rc.setAdapter(new SecureMarketAdapter(installedApps, activity, MarketFragment.this, fragmentType));
                rc.setLayoutManager(new GridLayoutManager(activity, 1));
            }

        }
    }*/

    private void searchUninstalledApps(String query) {
        if (installedApps.size() > 0) {
            if (!query.equals("")) {
                java.util.List<ServerAppInfo> searchedServerAppInfo = new ArrayList<>();
                for (ServerAppInfo app : installedApps) {
                    String apkName = app.getApkName().toLowerCase();
                    if (apkName.contains(query)) {
                        searchedServerAppInfo.add(app);
                    }
                }
//                rc.setAdapter(new SecureMarketAdapter(searchedServerAppInfo, getContext(), MarketFragment.this, fragmentType));
//                rc.setLayoutManager(new GridLayoutManager(activity, 1));
            } else {
//                rc.setAdapter(new SecureMarketAdapter(unInstalledApps, activity, MarketFragment.this, fragmentType));
//                rc.setLayoutManager(new GridLayoutManager(activity, 1));
            }

        }
    }

    public void onInstallationComplete(String pn) {

    }


    public void onDownLoadProgress(String pn, int progress, long speed) {
        Timber.d("onDownLoadProgress: " + pn);
        int index = IntStream.range(0, installedApps.size())
                .filter(i -> Objects.nonNull(installedApps.get(i)))
                .filter(i -> pn.equals(installedApps.get(i).getPackageName()))
                .findFirst()
                .orElse(-1);
        if (index != -1) {
            ServerAppInfo info = installedApps.get(index);
            info.setProgres(progress);
            info.setType(ServerAppInfo.PROG_TYPE.VISIBLE);
            info.setSpeed(speed);
            installedAdapter.updateProgressOfItem(info, index);
        }

    }


    public void downloadComplete(String filePath, String pn) {

        int index = IntStream.range(0, installedApps.size())
                .filter(i -> Objects.nonNull(installedApps.get(i)))
                .filter(i -> pn.equals(installedApps.get(i).getPackageName()))
                .findFirst()
                .orElse(-1);
        if (index != -1) {
            ServerAppInfo info = installedApps.get(index);
            info.setType(ServerAppInfo.PROG_TYPE.INSTALLING);
            installedAdapter.updateProgressOfItem(info, index);
        }


    }


    public void addPackageToList(ServerAppInfo info){
        installedApps.add(info);
        installedAdapter.updateProgressOfItem(info, installedApps.indexOf(info));
    }


    public void downloadError(String pn) {
//        int index = IntStream.range(0, unInstalledApps.size())
//                .filter(i -> Objects.nonNull(unInstalledApps.get(i)))
//                .filter(i -> pn.equals(unInstalledApps.get(i).getPackageName()))
//                .findFirst()
//                .orElse(-1);
//        if (index != -1) {
//            ServerAppInfo info = unInstalledApps.get(index);
//            info.setProgres(0);
//            info.setType(ServerAppInfo.PROG_TYPE.GONE);
//            uninstalledAdapter.updateProgressOfItem(info, index);
//        }

    }


    public void onDownloadStarted(String pn) {
        int index = IntStream.range(0, installedApps.size())
                .filter(i -> Objects.nonNull(installedApps.get(i)))
                .filter(i -> pn.equals(installedApps.get(i).getPackageName()))
                .findFirst()
                .orElse(-1);
        if (index != -1) {
            ServerAppInfo info = installedApps.get(index);
            info.setProgres(0);
            info.setType(ServerAppInfo.PROG_TYPE.VISIBLE);
            installedAdapter.updateProgressOfItem(info, index);
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (AppInstallUpdateListener) context;
        } catch (Exception ignored) {

        }
    }
}

