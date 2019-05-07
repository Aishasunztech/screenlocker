package com.screenlocker.secure.settings.codeSetting.Sim;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.telephony.SubscriptionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        holder.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.btnRegister.getText().toString().contains("Register"))
                {
                    holder.btnRegister.setText("Edit");

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
        TextView tvSimName;
        Button btnRegister;
        public MyViewHolder(View itemView) {

            super(itemView);

            tvSimName = itemView.findViewById(R.id.tvSimName);
            btnRegister = itemView.findViewById(R.id.btSimRegister);
        }
    }



}