package com.secureSetting;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.secure.launcher.R;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class NotificationsActivity extends BaseActivity implements GetApplistTask.GetAppsListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.appsList)
    RecyclerView rc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        ButterKnife.bind(this);
        List<AppInfo> appInfos = new ArrayList<>();
        rc.setAdapter(new NotificationsAdapter(appInfos,this));
        rc.setLayoutManager(new LinearLayoutManager(this));

        String key = PrefUtils.getStringPref(this, AppConstants.CURRENT_KEY);
        if (key.equals(AppConstants.KEY_MAIN_PASSWORD)){
            new GetApplistTask(this,true).execute();
        }else if (key.equals(AppConstants.KEY_GUEST_PASSWORD)){
            new GetApplistTask(this,false).execute();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void getApps(List<AppInfo> appInfoList) {
        rc.setAdapter(new NotificationsAdapter(appInfoList,this));
    }

    private class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.MyViewHolder> {

        private List<AppInfo> appInfoList;
        private Context context;

        public NotificationsAdapter(List<AppInfo> appInfoList, Context context) {
            this.appInfoList = appInfoList;
            this.context = context;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.apps_list_item,parent,false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.setData(appInfoList.get(position));
        }

        @Override
        public int getItemCount() {
            return appInfoList.size();
        }

        private class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


            ImageView imageView;
            TextView textView;
            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
                imageView = itemView.findViewById(R.id.notification_app_image);
                textView = itemView.findViewById(R.id.notification_app_name);
            }

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("android.provider.extra.APP_PACKAGE", appInfoList.get(getAdapterPosition()).getPackageName());
                startActivity(intent);
                Intent intent1 = new Intent(AppConstants.BROADCAST_VIEW_ADD_REMOVE);
                intent1.putExtra("add",true);
                LocalBroadcastManager.getInstance(NotificationsActivity.this).sendBroadcast(intent1);
            }

            public void setData(AppInfo appInfo) {

                if(appInfo !=null && appInfo.getIcon() != null) {
                    Glide.with(context).load(appInfo.getIcon()).into(imageView);
                    textView.setText(appInfo.getLabel());
                }
            }
        }


    }


}
