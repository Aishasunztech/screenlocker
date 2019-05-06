package com.secureMarket;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.screenlocker.secure.R;
import com.screenlocker.secure.settings.codeSetting.installApps.DownLoadAndInstallUpdate;
import com.screenlocker.secure.settings.codeSetting.installApps.List;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SecureMarketAdapter extends RecyclerView.Adapter<SecureMarketAdapter.MyViewHolder> {
    private java.util.List<List> appModelList;
    private Context context;
    private AppInstallUpdateListener listener;
    private String installOrNot;

    public interface AppInstallUpdateListener{
        void onInstallClick(List app);
        void onUnInstallClick(List app);
    }

    public SecureMarketAdapter(java.util.List<List> appModelList, Context context, AppInstallUpdateListener listener, String fragmentType) {
        this.appModelList = appModelList;
        this.context = context;
        this.listener = listener;
        installOrNot = fragmentType;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.market_app_list_item,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        List app = appModelList.get(position);

        if(app.isInstalled())
        {
            holder.btnUnInstall.setVisibility(View.VISIBLE);
            holder.btnInstall.setVisibility(View.GONE);

        }
        else{

            holder.btnInstall.setVisibility(View.VISIBLE);
            holder.btnUnInstall.setVisibility(View.GONE);
        }


        Glide.with(context)
                .load(AppConstants.LOGO_URL+appModelList.get(position).getLogo())
                .apply(new RequestOptions().centerCrop().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                .into(holder.imageView);
        holder.tv_name.setText(appModelList.get(position).getApkName());

        holder.btnInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onInstallClick(app);
            }

        });

        holder.btnUnInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onUnInstallClick(app);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{


        ImageView imageView;
        TextView tv_name,btnInstall,btnUnInstall,btnDownload;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.marketImageView);
            tv_name = itemView.findViewById(R.id.market_app_name);
            btnInstall = itemView.findViewById(R.id.btnInstall);
            btnUnInstall = itemView.findViewById(R.id.btnUnInstall);
//            btnDownload = itemView.findViewById(R.id.btnDownload);
        }
    }






}
