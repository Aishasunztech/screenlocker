package com.screenlocker.secure.settings.codeSetting.Sim;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.telephony.SubscriptionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.screenlocker.secure.R;

import java.util.ArrayList;

public class SimAdapter extends RecyclerView.Adapter<SimAdapter.MyViewHolder>{
    private Context context;
    private ArrayList<String> simIccids;

    public SimAdapter(Context context, ArrayList<String> simIccids) {
        this.context = context;
        this.simIccids = simIccids;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_sim,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.tvSimLabel.setText("Sim " + ++position);

        if(holder.btnRegister.getText().toString().contains("Edit"))
        {
            holder.guestSwitch.setVisibility(View.VISIBLE);
            holder.encryptedSwitch.setVisibility(View.VISIBLE);
            holder.enableSwitch.setVisibility(View.VISIBLE);
        }
        holder.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.btnRegister.getText().toString().contains("Register"))
                {
                    holder.btnRegister.setText("Edit");
                    holder.guestSwitch.setVisibility(View.VISIBLE);
                    holder.encryptedSwitch.setVisibility(View.VISIBLE);
                    holder.enableSwitch.setVisibility(View.VISIBLE);

                }
                else if(holder.btnRegister.getText().toString().contains("Edit"))
                {
                    EditSimDialog editSimDialog = new EditSimDialog(context);
                    editSimDialog.show();


                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvSimName,tvSimLabel;
        Button btnRegister;
        Switch guestSwitch,encryptedSwitch,enableSwitch;
        public MyViewHolder(View itemView) {

            super(itemView);

            tvSimLabel = itemView.findViewById(R.id.simName);
            tvSimName = itemView.findViewById(R.id.tvSimName);
            btnRegister = itemView.findViewById(R.id.btSimRegister);
            guestSwitch = itemView.findViewById(R.id.guest_sim_switch);
            encryptedSwitch = itemView.findViewById(R.id.encrypted_sim_switch);
            enableSwitch = itemView.findViewById(R.id.enable_sim_switch);
        }
    }



}