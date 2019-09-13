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
import com.secureMarket.SecureMarketAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class MarketFragment extends Fragment implements AppInstallUpdateListener {


    private AppInstallUpdateListener mListener;
    private RecyclerView rc;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<ServerAppInfo> installedApps = new ArrayList<>();
    private String url, fileName = "";
    private SecureMarketAdapter installedAdapter;
    private SharedViwModel viwModel;

    public SecureMarketAdapter getInstalledAdapter() {
        return installedAdapter;
    }


    public MarketFragment() {
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
        swipeRefreshLayout.setOnRefreshListener(() -> mListener.onAppsRefreshRequest());


        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viwModel.getAllApps().observe(this, serverAppInfos -> {
            Timber.d("setupApps: %s", serverAppInfos.size());
            installedApps.clear();
            installedApps.addAll(serverAppInfos);
            installedAdapter.setItems(installedApps);
            swipeRefreshLayout.setRefreshing(false);
            installedAdapter.notifyDataSetChanged();
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onInstallClick(ServerAppInfo app, int position, boolean isUpdate) {
        mListener.onInstallClick(app, position, isUpdate);
    }


    @Override
    public void onUnInstallClick(ServerAppInfo app, boolean status) {

        mListener.onUnInstallClick(app, status);
    }

    @Override
    public void onAppsRefreshRequest() {
        //not for this fragment
    }




    public void searchApps(String query) {
        if (installedApps.size() > 0) {
            if (!query.equals("")) {
                List<ServerAppInfo> searchedServerAppInfo = new ArrayList<>();
                for (ServerAppInfo app : installedApps) {
                    String apkName = app.getApkName().toLowerCase();
                    if (apkName.contains(query)) {
                        searchedServerAppInfo.add(app);
                    }
                }

                installedAdapter.setItems(searchedServerAppInfo);
                installedAdapter.notifyDataSetChanged();

            } else {
                installedAdapter.setItems(installedApps);
                installedAdapter.notifyDataSetChanged();
            }

        }
    }
    public void addPackageToList(ServerAppInfo info) {
        installedApps.add(info);
        installedAdapter.updateProgressOfItem(info, installedApps.indexOf(info));
    }
    public void onInstallationComplete(String pn) {
        int index = IntStream.range(0, installedApps.size())
                .filter(i -> Objects.nonNull(installedApps.get(i)))
                .filter(i -> pn.equals(installedApps.get(i).getPackageName()))
                .findFirst()
                .orElse(-1);
        if (index != -1) {
            installedApps.remove(index);
            installedAdapter.notifyItemRemoved(index);
        }
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

