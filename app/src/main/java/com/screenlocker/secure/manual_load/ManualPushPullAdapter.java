package com.screenlocker.secure.manual_load;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.screenlocker.secure.R;
import com.screenlocker.secure.socket.model.InstallModel;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.ArrayList;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.LOGO_END_POINT;

public class ManualPushPullAdapter extends RecyclerView.Adapter<ManualPushPullAdapter.AdapterViewHold> {

    private Context context;
    private ArrayList<InstallModel> installModelArrayList;
    private PushPullAppsListener pushPullAppsListener;

    public final String PUSH_APP = "push_apps";
    public final String PULL_APP = "pull_apps";


    public ManualPushPullAdapter(Context context, ArrayList<InstallModel> installModelArrayList, PushPullAppsListener pushPullAppsListener) {
        this.context = context;
        this.installModelArrayList = installModelArrayList;
        this.pushPullAppsListener = pushPullAppsListener;
    }

    interface PushPullAppsListener {
        void appTextButtonClick(int position, InstallModel installModel);
    }

    @NonNull
    @Override
    public AdapterViewHold onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdapterViewHold(LayoutInflater.from(parent.getContext()).inflate(R.layout.market_app_list_item, null));
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterViewHold holder, int position) {
        InstallModel installModel = installModelArrayList.get(position);

        if (installModel.getType().equals(PULL_APP)) {
            holder.btnUnInstall.setVisibility(View.VISIBLE);
            holder.btnInstall.setVisibility(View.INVISIBLE);
        }
        if (installModel.getType().equals(PUSH_APP)) {
            holder.btnUnInstall.setVisibility(View.INVISIBLE);
            holder.btnInstall.setVisibility(View.VISIBLE);
        }
        holder.tv_name.setText(installModel.getApk_name());

        Log.i("thumbanail_test", "onBindViewHolder: " + installModel.getApk_name());

        //   Glide.with(context).load(installModel.getApk()).thumbnail(0.5f).into(holder.imageView);

        String live_url = PrefUtils.getStringPref(context, LIVE_URL);

        Timber.d("skljdgvhsdgsgsj :%s", live_url.replaceAll("/mobile/", "") + LOGO_END_POINT + installModelArrayList.get(position).getApk());


    }

    @Override
    public int getItemCount() {
        return installModelArrayList.size();
    }

    class AdapterViewHold extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView imageView, size_icon;

        TextView tv_name, btnInstall, btnUnInstall, apkSize;

        public AdapterViewHold(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.marketImageView);
            tv_name = itemView.findViewById(R.id.market_app_name);
            btnInstall = itemView.findViewById(R.id.btnInstall);
            btnUnInstall = itemView.findViewById(R.id.btnUnInstall);
            apkSize = itemView.findViewById(R.id.apkSize);
            size_icon = itemView.findViewById(R.id.size_icon);
            apkSize.setVisibility(View.GONE);
            imageView.setVisibility(View.GONE);
            size_icon.setVisibility(View.GONE);

            btnInstall.setText("Install");
            btnInstall.setOnClickListener(this);
            btnUnInstall.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnInstall:
                    if (pushPullAppsListener != null) {
                        pushPullAppsListener.appTextButtonClick(getAdapterPosition(), installModelArrayList.get(getAdapterPosition()));
                    }
                    break;
                case R.id.btnUnInstall:
                    if (pushPullAppsListener != null) {
                        pushPullAppsListener.appTextButtonClick(getAdapterPosition(), installModelArrayList.get(getAdapterPosition()));
                    }
                    break;
                default:
                    break;

            }
        }
    }
}
