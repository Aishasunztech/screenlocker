package com.screenlocker.secure.settings.codeSetting;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.secure.launcher.R;

public class AppsPermissionAdapter extends RecyclerView.Adapter<AppsPermissionAdapter.MyViewHolder> {
    public AppsPermissionAdapter() {
    }

    @NonNull
    @Override
    public AppsPermissionAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.app_permission_item_row,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull AppsPermissionAdapter.MyViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public MyViewHolder(View itemView) {
            super(itemView);
        }
    }
}
