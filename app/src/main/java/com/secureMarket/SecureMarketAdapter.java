package com.secureMarket;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.settings.codeSetting.installApps.List;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.secureSetting.t.AppConst;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Type;
import java.util.ArrayList;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.LOGO_END_POINT;

public class SecureMarketAdapter extends RecyclerView.Adapter<SecureMarketAdapter.MyViewHolder> {
    private java.util.List<List> appModelList;
    private Context context;
    private AppInstallUpdateListener listener;
    private String userSpace;
    private String fragmentType;



    public interface AppInstallUpdateListener {
        void onInstallClick(List app,ProgressBar progressBar);

        void onUnInstallClick(List app, boolean status);

        void setProgressBar(ProgressBar progressBar);
    }

    public SecureMarketAdapter(java.util.List<List> appModelList, Context context,
                               AppInstallUpdateListener listener, String fragmentType) {
        this.appModelList = appModelList;
        this.context = context;
        this.listener = listener;
        userSpace = PrefUtils.getStringPref(context, CURRENT_KEY);
        this.fragmentType = fragmentType;


    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.market_app_list_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        List app = appModelList.get(position);
        holder.apkSize.setText(app.getApk_size());

        if (app.isInstalled()) {
            holder.btnUnInstall.setVisibility(View.VISIBLE);
            holder.btnInstall.setVisibility(View.GONE);
            if (fragmentType.equals("update")) {
                holder.btnUnInstall.setText(context.getResources().getString(R.string.update));
            } else {
                holder.btnUnInstall.setText(context.getResources().getString(R.string.uninstall));
            }

        } else {
            holder.btnInstall.setVisibility(View.VISIBLE);
            holder.btnUnInstall.setVisibility(View.GONE);
        }

        String live_url = PrefUtils.getStringPref(context, LIVE_URL);

        Timber.d("skljdgvhsdgsgsj :%s", live_url + LOGO_END_POINT + appModelList.get(position).getLogo());


        Glide.with(context)
                .load(live_url + LOGO_END_POINT + appModelList.get(position).getLogo())
                .apply(new RequestOptions().centerCrop().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                .into(holder.imageView);

        holder.tv_name.setText(appModelList.get(position).getApkName());

    }

    @Override
    public int getItemCount() {
        return appModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        ImageView imageView;

        TextView tv_name, btnInstall, btnUnInstall, apkSize;
        ProgressBar progressBar;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.marketImageView);
            tv_name = itemView.findViewById(R.id.market_app_name);
            btnInstall = itemView.findViewById(R.id.btnInstall);
            btnUnInstall = itemView.findViewById(R.id.btnUnInstall);
            apkSize = itemView.findViewById(R.id.apkSize);
            progressBar = itemView.findViewById(R.id.downloadProgress);
//            btnDownload = itemView.findViewById(R.id.btnDownload);

            btnInstall.setOnClickListener(this);
            btnUnInstall.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            List app = appModelList.get(getAdapterPosition());

            if (v.getId() == R.id.btnInstall) {


                if (listener != null) {
                        listener.setProgressBar(progressBar);
                        listener.onInstallClick(app,progressBar);

                    //}
                }

            } else if (v.getId() == R.id.btnUnInstall) {

                switch (userSpace) {

                    case KEY_GUEST_PASSWORD:

                        new Thread(() -> {
                            boolean isGuest = MyApplication.getAppDatabase(MyApplication.getAppContext()).getDao().checkGuest(app.getPackageName());

                            if (isGuest) {
                                if (fragmentType.equals("update")) {
                                    if (listener != null) {
                                        listener.onInstallClick(app,progressBar);
                                    }
                                } else if (fragmentType.equals("uninstall")) {
                                    if (app.getIs_restrict_uninstall() == 0) {
                                        listener.onUnInstallClick(app, true);
                                    } else {
                                        listener.onUnInstallClick(app, false);
                                    }
                                }
                            } else {
                                if (fragmentType.equals("update")) {
                                    if (listener != null) {
                                        listener.onInstallClick(app,progressBar);
                                    }
                                } else if (fragmentType.equals("uninstall")) {
                                    listener.onUnInstallClick(app, false);
                                }
                            }
                        }).start();


                        break;
                    case KEY_MAIN_PASSWORD:

                        new Thread(() -> {
                            boolean isEncrypted = MyApplication.getAppDatabase(MyApplication.getAppContext()).getDao().checkEncrypt(app.getPackageName());
                            if (isEncrypted) {
                                if (fragmentType.equals("update")) {
                                    if (listener != null) {
                                        listener.onInstallClick(app,progressBar);
                                    }
                                } else if (fragmentType.equals("uninstall")) {
                                    if (app.getIs_restrict_uninstall() == 0) {
                                        listener.onUnInstallClick(app, true);
                                    } else {
                                        listener.onUnInstallClick(app, false);
                                    }
                                }
//                                if (app.getIs_restrict_uninstall() == 0) {
//                                    listener.onUnInstallClick(app, true);
//                                } else {
//                                    listener.onUnInstallClick(app, false);
//                                }
                            } else {
                                if (fragmentType.equals("update")) {
                                    if (listener != null) {
                                        listener.onInstallClick(app,progressBar);
                                    }
                                } else if (fragmentType.equals("uninstall")) {
                                    listener.onUnInstallClick(app, false);
                                }
//                                listener.onUnInstallClick(app, false);
                            }
                        }).start();
                        break;
                }

            }

        }

    }

}
