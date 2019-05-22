package com.secureMarket;

import android.content.Context;
import android.telephony.SubscriptionManager;
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
import com.screenlocker.secure.R;
import com.screenlocker.secure.settings.codeSetting.installApps.List;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.screenlocker.secure.utils.AppConstants.CURRENT_KEY;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;

public class SecureMarketAdapter extends RecyclerView.Adapter<SecureMarketAdapter.MyViewHolder> {
    private java.util.List<List> appModelList;
    private Context context;
    private AppInstallUpdateListener listener;
    private String userSpace;


    public interface AppInstallUpdateListener{
        void onInstallClick(List app);
        void onUnInstallClick(List app);
    }

    public SecureMarketAdapter(java.util.List<List> appModelList, Context context, AppInstallUpdateListener listener) {
        this.appModelList = appModelList;
        this.context = context;
        this.listener = listener;
        userSpace = PrefUtils.getStringPref(context,CURRENT_KEY);


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
        holder.apkSize.setText(app.getApk_size());

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
                if(app.isGuest())
                {
                    if(userSpace.equals(KEY_GUEST_PASSWORD))
                    {
                        if(app.getIs_restrict_uninstall() == 1)
                        {
                            listener.onUnInstallClick(app);
                        }
                    }
                }
                else{
                    if(userSpace.equals(KEY_MAIN_PASSWORD))
                    {
                        if(app.getIs_restrict_uninstall() == 1)
                        {
                            listener.onUnInstallClick(app);
                        }
                    }
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return appModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{


        ImageView imageView;
        TextView tv_name,btnInstall,btnUnInstall,btnDownload,apkSize;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.marketImageView);
            tv_name = itemView.findViewById(R.id.market_app_name);
            btnInstall = itemView.findViewById(R.id.btnInstall);
            btnUnInstall = itemView.findViewById(R.id.btnUnInstall);
            apkSize = itemView.findViewById(R.id.apkSize);
//            btnDownload = itemView.findViewById(R.id.btnDownload);
        }
    }






}
