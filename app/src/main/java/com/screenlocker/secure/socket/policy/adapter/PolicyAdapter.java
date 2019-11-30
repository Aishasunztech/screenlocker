package com.screenlocker.secure.socket.policy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.secure.launcher.R;

import com.screenlocker.secure.socket.policy.model.PolicyModel;

import java.util.List;

public class PolicyAdapter extends RecyclerView.Adapter<PolicyAdapter.ViewHolder> {

    private PolicyModel[] steps;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public PolicyAdapter(Context context, PolicyModel[] steps) {
        this.mInflater = LayoutInflater.from(context);
        this.steps = steps;
    }


    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.app_name.setText(steps[position].getStepName());
        holder.progressBar.setVisibility(View.VISIBLE);
        holder.progressBar.setProgress(steps[position].getProgress());
        holder.app_usage.setText(steps[position].getStatus());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return steps.length;
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView app_name, app_time, app_usage;
        ImageView app_image;
        ProgressBar progressBar;


        ViewHolder(View itemView) {
            super(itemView);
            app_name = itemView.findViewById(R.id.app_name);
            app_time = itemView.findViewById(R.id.app_time);
            app_usage = itemView.findViewById(R.id.app_usage);
            app_image = itemView.findViewById(R.id.app_image);
            progressBar = itemView.findViewById(R.id.progressBar);

        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return steps[id].getStepName();
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}