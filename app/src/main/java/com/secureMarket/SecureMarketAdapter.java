package com.secureMarket;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.room.AppsModel;
import com.screenlocker.secure.settings.codeSetting.installApps.List;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;

public class SecureMarketAdapter extends RecyclerView.Adapter<SecureMarketAdapter.MyViewHolder> {
    private java.util.List<List> appModelList;
    private Context context;
    private AppInstallUpdateListener listener;
    private String userSpace;


    public interface AppInstallUpdateListener {
        void onInstallClick(List app);

        void onUnInstallClick(List app, boolean status);
    }

    public SecureMarketAdapter(java.util.List<List> appModelList, Context context, AppInstallUpdateListener listener) {
        this.appModelList = appModelList;
        this.context = context;
        this.listener = listener;
        userSpace = PrefUtils.getStringPref(context, CURRENT_KEY);


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

        } else {
            holder.btnInstall.setVisibility(View.VISIBLE);
            holder.btnUnInstall.setVisibility(View.GONE);
        }


        Glide.with(context)
                .load(AppConstants.LOGO_URL + appModelList.get(position).getLogo())
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

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.marketImageView);
            tv_name = itemView.findViewById(R.id.market_app_name);
            btnInstall = itemView.findViewById(R.id.btnInstall);
            btnUnInstall = itemView.findViewById(R.id.btnUnInstall);
            apkSize = itemView.findViewById(R.id.apkSize);
//            btnDownload = itemView.findViewById(R.id.btnDownload);


            btnInstall.setOnClickListener(this);
            btnUnInstall.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            List app = appModelList.get(getAdapterPosition());

            if (v.getId() == R.id.btnInstall) {


                if (listener != null) {
                    listener.onInstallClick(app);
                }

            } else if (v.getId() == R.id.btnUnInstall) {

                switch (userSpace) {

                    case KEY_GUEST_PASSWORD:

                        new Thread(() -> {
                            boolean isGuest = MyApplication.getAppDatabase(MyApplication.getAppContext()).getDao().checkGuest(app.getPackageName());

                            if (isGuest) {
                                if (app.getIs_restrict_uninstall() == 1) {
                                    listener.onUnInstallClick(app, true);
                                } else {
                                    listener.onUnInstallClick(app, false);
                                }
                            } else {
                                listener.onUnInstallClick(app, false);
                            }
                        }).start();


                        break;
                    case KEY_MAIN_PASSWORD:

                        new Thread(() -> {
                            boolean isEncrypted = MyApplication.getAppDatabase(MyApplication.getAppContext()).getDao().checkEncrypt(app.getPackageName());
                            if (isEncrypted) {
                                if (app.getIs_restrict_uninstall() == 1) {
                                    listener.onUnInstallClick(app, true);
                                } else {
                                    listener.onUnInstallClick(app, false);
                                }
                            } else {
                                listener.onUnInstallClick(app, false);
                            }
                        }).start();


                        break;
                }

            }


        }
    }


}
