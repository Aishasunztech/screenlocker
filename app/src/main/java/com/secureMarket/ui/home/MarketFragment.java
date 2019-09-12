package com.secureMarket.ui.home;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.async.AsyncCalls;
import com.screenlocker.secure.retrofit.RetrofitClientInstance;
import com.screenlocker.secure.retrofitapis.ApiOneCaller;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.settings.codeSetting.installApps.InstallAppModel;
import com.screenlocker.secure.settings.codeSetting.installApps.ServerAppInfo;
import com.screenlocker.secure.socket.model.InstallModel;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;
import com.screenlocker.secure.utils.Utils;
import com.secureMarket.AppInstallUpdateListener;
import com.secureMarket.SecureMarketActivity;
import com.secureMarket.SecureMarketAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.SECUREMARKETSIM;
import static com.screenlocker.secure.utils.AppConstants.SECUREMARKETWIFI;
import static com.screenlocker.secure.utils.AppConstants.UNINSTALLED_PACKAGES;
import static com.screenlocker.secure.utils.AppConstants.URL_1;
import static com.screenlocker.secure.utils.AppConstants.URL_2;
import static com.secureMarket.MarketUtils.savePackages;
import static com.secureSetting.UtilityFunctions.getVersionCode;

/**
 * A simple {@link Fragment} subclass.
 */
public class MarketFragment extends Fragment implements AppInstallUpdateListener {


    private AppInstallUpdateListener mListener;
    private RecyclerView rc;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout errorLayout;
    private ImageView errorImage;
    private TextView errorText;
    private Button errorBtn;
    private ProgressBar progressBar;
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
        errorLayout = view.findViewById(R.id.error_layout);
        errorImage = view.findViewById(R.id.error_image);
        errorText = view.findViewById(R.id.error_text);
        errorBtn = view.findViewById(R.id.error_btn);
        progressBar = view.findViewById(R.id.marketFragmentProgress);
        errorBtn.setOnClickListener(v -> {
            //
            mListener.onAppsRefreshRequest();
        });
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
            if (serverAppInfos.size() == 0){
                errorImage.setImageResource(R.drawable.ic_android);
                errorText.setText("No App Available");
                errorBtn.setVisibility(View.GONE);
                errorLayout.setVisibility(View.VISIBLE);
            }
            installedApps.addAll(serverAppInfos);
            installedAdapter.setItems(installedApps);
            swipeRefreshLayout.setRefreshing(false);
            installedAdapter.notifyDataSetChanged();
        });
        viwModel.getMutableMsgs().observe(this, msg -> {
            if (msg == Msgs.ERROR) {
                swipeRefreshLayout.setRefreshing(false);
                onNetworkError();
            } else if (msg == Msgs.SUCCESS) {
                rc.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false);
                errorLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            } else if (msg == Msgs.LOADING) {
                rc.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(true);
                errorLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }
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
                java.util.List<ServerAppInfo> searchedServerAppInfo = new ArrayList<>();
                for (ServerAppInfo app : installedApps) {
                    String apkName = app.getApkName().toLowerCase();
                    if (apkName.contains(query)) {
                        searchedServerAppInfo.add(app);
                    }
                }
                if (searchedServerAppInfo.size() == 0){
                    errorImage.setImageResource(R.drawable.ic_android);
                    errorText.setText("No App Available");
                    errorBtn.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.VISIBLE);
                }

                installedAdapter.setItems(searchedServerAppInfo);
                installedAdapter.notifyDataSetChanged();

            } else {
                installedAdapter.setItems(installedApps);
                installedAdapter.notifyDataSetChanged();
                errorLayout.setVisibility(View.GONE);
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
            if (installedAdapter.getItemCount() == 0){
                errorImage.setImageResource(R.drawable.ic_android);
                errorText.setText("No App Available");
                errorBtn.setVisibility(View.GONE);
                errorLayout.setVisibility(View.VISIBLE);
            }
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

    public void onNetworkError() {
        errorLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        errorText.setText("No Internet Connection");
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

