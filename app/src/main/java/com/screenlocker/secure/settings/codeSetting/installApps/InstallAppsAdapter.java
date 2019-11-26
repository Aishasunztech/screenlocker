package com.screenlocker.secure.settings.codeSetting.installApps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.secure.launcher.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.screenlocker.secure.utils.AppConstants.LOGO_END_POINT;

public class InstallAppsAdapter extends RecyclerView.Adapter<InstallAppsAdapter.MyViewHolder> {

    private List<ServerAppInfo> appModelServerAppInfo;

    public interface InstallAppListener {
        void onInstallClick(View v, ServerAppInfo app, int position);

        void onUnInstallClick(View v, ServerAppInfo app, int position);

        void onCancelClick(String requestId);
    }

    InstallAppListener mListener;

    public void updateProgressOfItem(ServerAppInfo app, int index) {
        appModelServerAppInfo.set(index, app);
        notifyItemChanged(index);
    }

    public InstallAppsAdapter(List<ServerAppInfo> appModelServerAppInfo, InstallAppListener installAppListener) {
        this.appModelServerAppInfo = appModelServerAppInfo;
        mListener = installAppListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_install_apps, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        ServerAppInfo app = appModelServerAppInfo.get(position);
        holder.tvAppName.setText(app.getApkName());
        //load logo

        String live_url = PrefUtils.getStringPref(MyApplication.getAppContext(), AppConstants.LIVE_URL);

        Glide.with(holder.itemView.getContext())
                .load(live_url.replaceAll("/api/v2/mobile/", "") + LOGO_END_POINT + app.getLogo())
                .apply(new RequestOptions().centerCrop().diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                .into(holder.ivLogo);

        if (app.isInstalled()) {
            //btUnInstall.setVisibility(View.VISIBLE);
            holder.btInstall.setText(R.string.uninstall);
            holder.btInstall.setEnabled(true);
//            holder.progressBar.setVisibility(View.GONE);
            holder.progress_container.setVisibility(View.GONE);
            holder.speedMsg.setVisibility(View.GONE);

        } else {
            //btUnInstall.setVisibility(View.INVISIBLE);
            holder.btInstall.setText(R.string.install);
            holder.btInstall.setEnabled(true);
//            holder.progressBar.setVisibility(View.GONE);
            holder.progress_container.setVisibility(View.GONE);
            holder.speedMsg.setVisibility(View.GONE);
        }

        if (app.getType() == ServerAppInfo.PROG_TYPE.GONE) {
//            holder.progressBar.setVisibility(View.GONE);
            holder.progress_container.setVisibility(View.GONE);
            holder.speedMsg.setVisibility(View.GONE);
            holder.btInstall.setEnabled(true);
        } else if (app.getType() == ServerAppInfo.PROG_TYPE.VISIBLE) {
//            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progress_container.setVisibility(View.VISIBLE);
            holder.progressBar.setProgress(app.getProgres());
            holder.speedMsg.setText(app.getSpeed() / 1000 + "Kb/s");
            holder.speedMsg.setVisibility(View.VISIBLE);
            holder.progressBar.setIndeterminate(false);
            holder.btInstall.setEnabled(false);
        } else if (app.getType() == ServerAppInfo.PROG_TYPE.LOADING) {
            holder.speedMsg.setText("Pending Installation");
            holder.speedMsg.setVisibility(View.VISIBLE);
            holder.progressBar.setIndeterminate(true);
            holder.btInstall.setEnabled(false);
        } else {
            holder.speedMsg.setText("Installation...");
            holder.speedMsg.setVisibility(View.VISIBLE);
            holder.progressBar.setIndeterminate(true);
            holder.btInstall.setEnabled(false);
        }

    }

    @Override
    public int getItemCount() {
        return appModelServerAppInfo.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvAppName, speedMsg;
        ImageView ivLogo,btnCancel;
        Button btInstall;
        ProgressBar progressBar;

        LinearLayout progress_container;

        public MyViewHolder(View itemView) {
            super(itemView);


            progressBar = itemView.findViewById(R.id.progress);
            tvAppName = itemView.findViewById(R.id.tvAppName);
            speedMsg = itemView.findViewById(R.id.msg);
            btInstall = itemView.findViewById(R.id.btInstall);
            btnCancel = itemView.findViewById(R.id.cancel_download_install);
            progress_container = itemView.findViewById(R.id.progress_container_install);
            ivLogo = itemView.findViewById(R.id.ivLogo);

            btInstall.setOnClickListener(view -> {
                if (btInstall.getText().toString().equals(itemView.getContext().getResources().getString(R.string.install))) {
                    mListener.onInstallClick(view, appModelServerAppInfo.get(getAdapterPosition()), getAdapterPosition());
                    progressBar.setIndeterminate(true);
                    speedMsg.setText("Pending Installation");
                    speedMsg.setVisibility(View.VISIBLE);
//                    progressBar.setVisibility(View.VISIBLE);
                    progress_container.setVisibility(View.VISIBLE);

                } else if (btInstall.getText().toString().equals(itemView.getContext().getResources().getString(R.string.uninstall))) {
                    mListener.onUnInstallClick(view, appModelServerAppInfo.get(getAdapterPosition()), getAdapterPosition());
                }
            });

            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onCancelClick(appModelServerAppInfo.get(getAdapterPosition()).getRequest_id());
                }
            });


        }


    }
}
