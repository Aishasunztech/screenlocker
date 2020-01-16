package com.secureMarket;

import android.content.Context;
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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.service.AppExecutor;
import com.screenlocker.secure.settings.codeSetting.installApps.ServerAppInfo;
import com.screenlocker.secure.utils.PrefUtils;
import com.secure.launcher.R;

import java.util.List;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.LOGO_END_POINT;

public class SecureMarketAdapter extends RecyclerView.Adapter<SecureMarketAdapter.MyViewHolder> {
    private List<ServerAppInfo> appModelServerAppInfo;
    private Context context;
    private AppInstallUpdateListener listener;
    private String userSpace;
    private String fragmentType;


    public SecureMarketAdapter(List<ServerAppInfo> appModelServerAppInfo, Context context,
                               AppInstallUpdateListener listener, String fragmentType) {
        this.appModelServerAppInfo = appModelServerAppInfo;
        this.context = context;
        this.listener = listener;
        userSpace = PrefUtils.getStringPref(context, CURRENT_KEY);
        this.fragmentType = fragmentType;


    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.secure_market_item_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        ServerAppInfo app = appModelServerAppInfo.get(position);
        holder.apkSize.setText(app.getApk_size());

        if (app.isInstalled()) {
            if (app.isUpdate()) {
                holder.btnUpdate.setVisibility(View.VISIBLE);
                holder.btnUninstall.setVisibility(View.GONE);
                holder.btnInstall.setVisibility(View.GONE);
            } else {
                holder.btnUpdate.setVisibility(View.GONE);
                holder.btnUninstall.setVisibility(View.VISIBLE);
                holder.btnInstall.setVisibility(View.GONE);
            }

        } else {
            holder.btnUpdate.setVisibility(View.GONE);
            holder.btnUninstall.setVisibility(View.GONE);
            holder.btnInstall.setVisibility(View.VISIBLE);
        }

        String live_url = PrefUtils.getStringPref(context, LIVE_URL);

        Timber.d("skljdgvhsdgsgsj :%s", live_url + LOGO_END_POINT + appModelServerAppInfo.get(position).getLogo());


        Glide.with(context)
                .load(live_url.replaceAll("/api/v2/mobile/", "") + LOGO_END_POINT + appModelServerAppInfo.get(position).getLogo())
                .apply(new RequestOptions().centerCrop().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                .into(holder.imageView);

        holder.tv_name.setText(appModelServerAppInfo.get(position).getApkName());

        if (app.getType() == ServerAppInfo.PROG_TYPE.GONE) {
//            holder.progressBar.setVisibility(View.GONE);
            holder.progress_container.setVisibility(View.GONE);
            holder.status.setVisibility(View.GONE);
            holder.btnInstall.setEnabled(true);
        } else if (app.getType() == ServerAppInfo.PROG_TYPE.VISIBLE) {
//            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progress_container.setVisibility(View.VISIBLE);
            holder.progressBar.setProgress(app.getProgres());
            holder.progressBar.setIndeterminate(false);
            holder.status.setVisibility(View.VISIBLE);
            holder.status.setText(app.getSpeed() / 1000 + " Kb/s");
            holder.btnInstall.setEnabled(false);
        } else if (app.getType() == ServerAppInfo.PROG_TYPE.LOADING) {
            holder.status.setText("Pending Installation");
            holder.status.setVisibility(View.VISIBLE);
//            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progress_container.setVisibility(View.VISIBLE);

            holder.progressBar.setIndeterminate(true);
            holder.btnInstall.setEnabled(false);
        } else {
            holder.status.setText("Installation...");
            holder.status.setVisibility(View.VISIBLE);
//            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progress_container.setVisibility(View.VISIBLE);
            holder.progressBar.setIndeterminate(true);
            holder.btnInstall.setEnabled(false);
        }

    }

    public void updateProgressOfItem(ServerAppInfo app, int index) {
        appModelServerAppInfo.set(index, app);
        notifyItemChanged(index);
    }

    @Override
    public int getItemCount() {
        return appModelServerAppInfo.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        ImageView imageView,cancel_download;

        TextView tv_name, apkSize, status;
        Button btnInstall, btnUninstall, btnUpdate;
        ProgressBar progressBar;
        LinearLayout progress_container;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.marketImageView);
            tv_name = itemView.findViewById(R.id.market_app_name);
            btnInstall = itemView.findViewById(R.id.btnInstall);
            btnUninstall = itemView.findViewById(R.id.btnUnInstall);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            apkSize = itemView.findViewById(R.id.apkSize);
            progressBar = itemView.findViewById(R.id.progress);
            status = itemView.findViewById(R.id.status);
            progress_container = itemView.findViewById(R.id.progress_container);
            cancel_download = itemView.findViewById(R.id.cancel_download);
            btnInstall.setOnClickListener(this);
            btnUninstall.setOnClickListener(this);
            btnUpdate.setOnClickListener(this);
            cancel_download.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            ServerAppInfo app = appModelServerAppInfo.get(getAdapterPosition());


            if (v.getId() == R.id.btnInstall) {
                if (listener != null) {
                    listener.onInstallClick(app, getAdapterPosition(), false);
                }
            } else if (v.getId() == R.id.btnUpdate) {
                if (listener != null) {
                    listener.onInstallClick(app, getAdapterPosition(), true);
                }
            } else if (v.getId() == R.id.btnUnInstall) {

                switch (userSpace) {

                    case KEY_GUEST_PASSWORD:

                        new Thread(() -> {
                            boolean isGuest = MyApplication.getAppDatabase(MyApplication.getAppContext()).getDao().checkGuest(app.getPackageName());

                            AppExecutor.getInstance().getMainThread().execute(() -> {
                                if (isGuest) {
                                    if (app.getIs_restrict_uninstall() == 0) {
                                        listener.onUnInstallClick(app, true);
                                    } else {
                                        listener.onUnInstallClick(app, false);
                                    }

                                } else {
                                    listener.onUnInstallClick(app, false);
                                }
                            });
                        }).start();


                        break;
                    case KEY_MAIN_PASSWORD:

                        new Thread(() -> {
                            boolean isEncrypted = MyApplication.getAppDatabase(MyApplication.getAppContext()).getDao().checkEncrypt(app.getPackageName());
                            AppExecutor.getInstance().getMainThread().execute(() -> {
                                if (isEncrypted) {
                                    if (app.getIs_restrict_uninstall() == 0) {
                                        listener.onUnInstallClick(app, true);
                                    } else {
                                        listener.onUnInstallClick(app, false);
                                    }

                                } else {
                                    listener.onUnInstallClick(app, false);
                                }
                            });
                        }).start();
                        break;
                }

            }else if(v.getId() == R.id.cancel_download)
            {
                Log.d("lkdfh","ButtonLCicked" + app.getRequest_id());
               listener.onCancelClick(app.getRequest_id());
            }

        }

    }

    public void setItems(List<ServerAppInfo> list) {
        appModelServerAppInfo = list;
    }

}
