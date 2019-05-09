package com.screenlocker.secure.settings.codeSetting.installApps;

import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.screenlocker.secure.R;
import com.screenlocker.secure.utils.AppConstants;

import java.util.List;

public class InstallAppsAdapter extends RecyclerView.Adapter<InstallAppsAdapter.MyViewHolder> {

    private List<com.screenlocker.secure.settings.codeSetting.installApps.List> appModelList;

    public interface InstallAppListener {
        void onInstallClick(View v, com.screenlocker.secure.settings.codeSetting.installApps.List app, int position);

        void onUnInstallClick(View v, com.screenlocker.secure.settings.codeSetting.installApps.List app, int position);
    }

    InstallAppListener mListener;

    public InstallAppsAdapter(List<com.screenlocker.secure.settings.codeSetting.installApps.List> appModelList, InstallAppListener installAppListener) {
        this.appModelList = appModelList;
        mListener = installAppListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_install_apps, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bind(appModelList.get(position));
    }

    @Override
    public int getItemCount() {
        return appModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvAppName;
        ImageView ivLogo;
        Button btUnInstall;
        Button btInstall;
        PackageManager pm;

        public MyViewHolder(View itemView) {
            super(itemView);

            pm = itemView.getContext().getPackageManager();
            tvAppName = itemView.findViewById(R.id.tvAppName);
            btUnInstall = itemView.findViewById(R.id.btUnInstall);
            btInstall = itemView.findViewById(R.id.btInstall);
            ivLogo = itemView.findViewById(R.id.ivLogo);

            btInstall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onInstallClick(view, appModelList.get(getAdapterPosition()), getAdapterPosition());
                }
            });

            btUnInstall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onUnInstallClick(view, appModelList.get(getAdapterPosition()), getAdapterPosition());
                }
            });

        }

        //secret temptation green

        public void bind(com.screenlocker.secure.settings.codeSetting.installApps.List app) {
            tvAppName.setText(app.getApkName());
            //load logo
            Glide.with(itemView.getContext())
                    .load(AppConstants.LOGO_URL+app.getLogo())
                    .apply(new RequestOptions().centerCrop() .diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                     .into(ivLogo);

            if (app.isInstalled()) {
                btUnInstall.setVisibility(View.VISIBLE);
                btInstall.setVisibility(View.INVISIBLE);
            } else {
                btUnInstall.setVisibility(View.INVISIBLE);
                btInstall.setVisibility(View.VISIBLE);
            }


            //if app is installed then show app uninstall button else show install button

//            if (isAppInstalled(app.getAppPackage())) {
//                btUnInstall.setVisibility(View.VISIBLE);
//                btInstall.setVisibility(View.INVISIBLE);
//            } else {
//                btUnInstall.setVisibility(View.INVISIBLE);
//                btInstall.setVisibility(View.VISIBLE);
//            }

        }


    }
}
