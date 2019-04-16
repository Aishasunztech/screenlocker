package com.screenlocker.secure.notifications;

import android.os.Build;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.screenlocker.secure.R;

public class NotificationView extends RecyclerView.ViewHolder {

    private final TextView tvTitle;
    private final TextView tvBody;
    private final ImageView ivIcon;

    public NotificationView(View itemView) {
        super(itemView);
        tvTitle = itemView.findViewById(R.id.tv_notification_title);
        tvBody = itemView.findViewById(R.id.tv_notification_body);
        TextView tvTime = itemView.findViewById(R.id.tv_notification_time);
        ivIcon = itemView.findViewById(R.id.iv_notification_icon);
    }

    public void bind(NotificationItem notification) {
        tvTitle.setText(notification.title);
        tvBody.setText(notification.body);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notification.icon != null)
                ivIcon.setImageIcon(notification.icon);
        }
    }
}
