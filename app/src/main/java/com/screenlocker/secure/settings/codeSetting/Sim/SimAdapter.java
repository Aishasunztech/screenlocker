package com.screenlocker.secure.settings.codeSetting.Sim;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.telephony.SubscriptionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.screenlocker.secure.R;
import com.screenlocker.secure.room.SimEntry;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;

import static com.screenlocker.secure.utils.AppConstants.KEY_ENABLE;
import static com.screenlocker.secure.utils.AppConstants.KEY_ENCRYPTED;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST;

public class SimAdapter extends RecyclerView.Adapter<SimAdapter.MyViewHolder>{
    private Context context;
    private List<SimEntry> simEntries;
    private OnSimPermissionChangeListener mListener;
    public interface OnSimPermissionChangeListener {
        void onSimPermissionChange(SimEntry entry, String type , boolean isChecked);
    }

    public SimAdapter(Context context, List<SimEntry> simEntries, OnSimPermissionChangeListener listener ) {
        this.context = context;
        this.simEntries = simEntries;
        mListener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_sim,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        SimEntry entry = simEntries.get(position);
        holder.tvSimICCID.setText(entry.getIccid());
        holder.tvSlote.setText(String.valueOf(entry.getSlotNo()));
        holder.tvSimName.setText(entry.getProviderName());
        holder.encrypted_sim_switch.setChecked(entry.isEncrypted());
        holder.encrypted_sim_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mListener.onSimPermissionChange(entry,  KEY_ENCRYPTED, isChecked);
        });
        holder.guest_sim_switch.setChecked(entry.isGuest());
        holder.guest_sim_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mListener.onSimPermissionChange(entry,  KEY_GUEST, isChecked);
        });
        holder.enable_sim_switch.setChecked(entry.isEnable());
        holder.enable_sim_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mListener.onSimPermissionChange(entry, KEY_ENABLE, isChecked);
        });
        holder.tvStatus.setText(entry.getStatus());
    }

    @Override
    public int getItemCount() {
        return simEntries.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvSimICCID ,tvSlote,tvSimName , tvStatus;
        Switch guest_sim_switch ,encrypted_sim_switch ,enable_sim_switch;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvSimICCID = itemView.findViewById(R.id.tvSimICCID);
            tvSlote = itemView.findViewById(R.id.tvSlote);
            tvSimName = itemView.findViewById(R.id.tvSimName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            guest_sim_switch = itemView.findViewById(R.id.guest_sim_switch);
            encrypted_sim_switch = itemView.findViewById(R.id.encrypted_sim_switch);
            enable_sim_switch = itemView.findViewById(R.id.enable_sim_switch);
        }
    }



}