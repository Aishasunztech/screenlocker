package com.screenlocker.secure.manual_load;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.screenlocker.secure.R;
import com.screenlocker.secure.socket.model.InstallModel;

import java.util.ArrayList;

public class ManualPushPullAdapter extends RecyclerView.Adapter<ManualPushPullAdapter.AdapterViewHold> {

    private Context context;
    private ArrayList<InstallModel> installModelArrayList;
    private PushPullAppsListener pushPullAppsListener;

    public ManualPushPullAdapter(Context context, ArrayList<InstallModel> installModelArrayList, PushPullAppsListener pushPullAppsListener) {
        this.context = context;
        this.installModelArrayList = installModelArrayList;
        this.pushPullAppsListener = pushPullAppsListener;
    }

    interface PushPullAppsListener{
        void appTextButtonClick(int position,InstallModel installModel);
    }

    @NonNull
    @Override
    public AdapterViewHold onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdapterViewHold(LayoutInflater.from(parent.getContext()).inflate(R.layout.market_app_list_item,null));
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterViewHold holder, int position) {
        InstallModel installModel = installModelArrayList.get(0);

        if(installModel.getType_operation().equals(ManualPullPush.PULL_APP)){
            holder.btnUnInstall.setVisibility(View.VISIBLE);
            holder.btnInstall.setVisibility(View.GONE);
        }
        if(installModel.getType_operation().equals(ManualPullPush.PUSH_APP)){
            holder.btnUnInstall.setVisibility(View.GONE);
            holder.btnInstall.setVisibility(View.VISIBLE);
        }
        holder.tv_name.setText(installModel.getApk_name());
        Glide.with(context).load(installModel.getApk()).thumbnail(0.5f).into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return installModelArrayList.size();
    }

     class AdapterViewHold extends RecyclerView.ViewHolder implements View.OnClickListener{

         ImageView imageView;

         TextView tv_name, btnInstall, btnUnInstall, apkSize;

       public AdapterViewHold(@NonNull View itemView) {
           super(itemView);
           imageView = itemView.findViewById(R.id.marketImageView);
           tv_name = itemView.findViewById(R.id.market_app_name);
           btnInstall = itemView.findViewById(R.id.btnInstall);
           btnUnInstall = itemView.findViewById(R.id.btnUnInstall);
           apkSize = itemView.findViewById(R.id.apkSize);
           apkSize.setVisibility(View.GONE);

           btnInstall.setOnClickListener(this);
           btnUnInstall.setOnClickListener(this);

       }

         @Override
         public void onClick(View v) {
             switch (v.getId()){
                case R.id.btnInstall:
                    if(pushPullAppsListener!=null){
                        pushPullAppsListener.appTextButtonClick(getAdapterPosition(),installModelArrayList.get(getAdapterPosition()));
                    }
                    break;
                 case R.id.btnUnInstall:
                     if(pushPullAppsListener!=null){
                         pushPullAppsListener.appTextButtonClick(getAdapterPosition(),installModelArrayList.get(getAdapterPosition()));
                     }
                     break;
                 default:
                     break;

             }
         }
     }
}
