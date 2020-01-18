package com.screenlocker.secure.settings.notification;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.screenlocker.secure.socket.model.DeviceMessagesModel;
import com.secure.launcher.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author : Muhammad Nadeem
 * Created at: 1/13/2020
 */
public class NotificationAdaptor extends RecyclerView.Adapter<NotificationAdaptor.NotificationViewHolder> {

    private List<DeviceMessagesModel> models;
    private NotificationActivity.NotificationLongClickListener mListener;

    public NotificationAdaptor(List<DeviceMessagesModel> models, NotificationActivity.NotificationLongClickListener listener) {
        mListener = listener;
        this.models = models;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return  new NotificationViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification,parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        DeviceMessagesModel model = models.get(position);
        holder.notification.setText(model.getMsg());
        if (!model.isSeen()){
            holder.notification.setTypeface(holder.notification.getTypeface(), Typeface.BOLD);
        }else {
            holder.notification.setTypeface(holder.notification.getTypeface(), Typeface.NORMAL);
        }
        SimpleDateFormat dt = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
        Date date = new Date(model.getDate());
        holder.date.setText(dt.format(date));
        holder.parent.setOnLongClickListener(v -> {
            mListener.onNotificationLongClick(model);
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder{
        private TextView notification, date;
        private LinearLayout parent;
        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notification = itemView.findViewById(R.id.notification);
            date = itemView.findViewById(R.id.time);
            parent = itemView.findViewById(R.id.parent);
        }
    }
}
