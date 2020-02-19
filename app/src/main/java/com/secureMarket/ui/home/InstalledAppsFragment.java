package com.secureMarket.ui.home;

import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.screenlocker.secure.settings.codeSetting.installApps.ServerAppInfo;
import com.secure.launcher.R;
import com.secureMarket.AppInstallUpdateListener;
import com.secureMarket.SecureMarketAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import timber.log.Timber;

import static com.screenlocker.secure.utils.CommonUtils.isNetworkAvailable;

public class InstalledAppsFragment extends Fragment implements AppInstallUpdateListener {


    private AppInstallUpdateListener mListener;
    private RecyclerView rc;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout errorLayout;
    private ImageView errorImage;
    private TextView errorText;
    private Button errorBtn;
    private ProgressBar progressBar;
    private List<ServerAppInfo> installedApps = new ArrayList<>();
    private SecureMarketAdapter installedAdapter;
    private SharedViwModel viwModel;


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
        errorLayout = view.findViewById(R.id.error_layout);
        errorImage = view.findViewById(R.id.error_image);
        errorBtn = view.findViewById(R.id.error_btn);
        errorText = view.findViewById(R.id.error_text);

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
        viwModel.getInstalled().observe(this, serverAppInfos -> {
            Timber.d("setupApps: %s", serverAppInfos.size());
            installedApps.clear();
            if (serverAppInfos.size() == 0) {
                errorImage.setImageResource(R.drawable.ic_android);
                errorText.setText("No Apps Installed");
                if(isNetworkAvailable(getActivity()))
                {
                    errorBtn.setVisibility(View.GONE);
                }
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
            }else if (msg==Msgs.SERVER_ERROR){
                swipeRefreshLayout.setRefreshing(false);
                onServerError();
            }
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
    public void onAppsRefreshRequest() {
        //not for this app
    }

    @Override
    public void onCancelClick(String requestId) {
        Timber.d("FragmentClicked");

        mListener.onCancelClick(requestId);
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
                if (searchedServerAppInfo.size() == 0) {
                    errorImage.setImageResource(R.drawable.ic_android);
                    errorText.setText("No App Available");
                    if(isNetworkAvailable(getActivity()))
                    {
                        errorBtn.setVisibility(View.GONE);
                    }
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

    public void onInstallationComplete(String pn) {
        int index = IntStream.range(0, installedApps.size())
                .filter(i -> Objects.nonNull(installedApps.get(i)))
                .filter(i -> pn.equals(installedApps.get(i).getPackageName()))
                .findFirst()
                .orElse(-1);
        if (index != -1) {
            installedApps.remove(index);
            installedAdapter.notifyItemRemoved(index);
            if (installedAdapter.getItemCount() == 0) {
                errorImage.setImageResource(R.drawable.ic_android);
                errorText.setText("No App Available");
                if(isNetworkAvailable(getActivity()))
                {
                    errorBtn.setVisibility(View.GONE);
                }
                errorLayout.setVisibility(View.VISIBLE);
            }
        }
    }


    public void addPackageToList(ServerAppInfo info) {
        errorLayout.setVisibility(View.GONE);
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

    public void onNetworkError() {
        errorLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        errorImage.setImageResource(R.drawable.ic_no_internet_connection);
        rc.setVisibility(View.GONE);
        errorText.setText(getResources().getString(R.string.no_internet_connection));
    }
    public void onServerError() {
        errorLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        errorImage.setImageResource(R.drawable.ic_server_error);
        rc.setVisibility(View.GONE);
        errorText.setText(getResources().getString(R.string.internal_server_error));
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

