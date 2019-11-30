package com.screenlocker.secure.settings.codeSetting.systemControls;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.secure.launcher.R;
import com.screenlocker.secure.socket.model.Settings;

import java.util.List;

/**
 * @author Muhammad Nadeem
 * @Date 8/31/2019.
 */
public class SystemPermissionAdaptor extends RecyclerView.Adapter<SystemPermissionAdaptor.MyViewHolder> {


    private List<Settings> settings;
    private PermissionSateChangeListener mListener;

    public void setSettings(List<Settings> settings) {
        this.settings = settings;
    }

    public SystemPermissionAdaptor(List<Settings> settings, PermissionSateChangeListener mListener) {
        this.settings = settings;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.mypermission_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.aSwitch.setChecked(settings.get(position).isSetting_status());
        holder.aSwitch.setText(settings.get(position).getSetting_name());


    }

    @Override
    public int getItemCount() {
        return settings.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        private Switch aSwitch;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            aSwitch = itemView.findViewById(R.id._switch);
            aSwitch.setOnClickListener(v -> {
                mListener.OnPermisionChangeListener(settings.get(getAdapterPosition()), ((Switch)v).isChecked());
            });
        }
    }
}
