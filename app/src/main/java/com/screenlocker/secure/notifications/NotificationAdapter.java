package com.screenlocker.secure.notifications;

import android.app.PendingIntent;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.screenlocker.secure.R;
import com.screenlocker.secure.listener.OnItemClickListener;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationView> {

    private final List<NotificationItem> notifications;
    private final OnItemClickListener listener;

    public NotificationAdapter(List<NotificationItem> notifications, OnItemClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notificatio, parent, false);
        return new NotificationView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotificationView holder, int position) {
        holder.bind(notifications.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final int currentPosition = holder.getAdapterPosition();
                if (currentPosition != -1) {
                    final NotificationItem item = notifications.get(currentPosition);
                    if (item.contentIntent != null) {
                        try {
                            item.contentIntent.send();
                            notifications.remove(item);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    notifyItemRemoved(currentPosition);
                                    NotificationManagerCompat.from(view.getContext())
                                            .cancel(item.id);
                                }
                            }, 100);
                            listener.onItemClicked(item);
                        } catch (PendingIntent.CanceledException ignore) { }
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications == null ? 0 : notifications.size();
    }

}
