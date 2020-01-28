package com.screenlocker.secure.settings.notification;

import android.app.NotificationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.screenlocker.secure.socket.model.DeviceMessagesModel;
import com.secure.launcher.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    List<DeviceMessagesModel> msgList = new ArrayList<>();
    private NotificationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.notifications);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewModel = ViewModelProviders.of(this).get(NotificationViewModel.class);
        RecyclerView recyclerView = findViewById(R.id.notification);
        LinearLayout layout = findViewById(R.id.empty_image_view);
        NotificationAdaptor adaptor = new NotificationAdaptor(msgList, notification -> {
            new AlertDialog.Builder(this)
                    .setTitle("Remove Notification?")
                    .setMessage("Do you want to remove this notification?")
                    .setPositiveButton("Ok", (dialog, which) -> {
                        viewModel.removeNotification(notification);
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    }).show();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adaptor);
        viewModel.getMsgLists().observe(this, deviceMessagesModels -> {
            msgList.clear();
            msgList.addAll(deviceMessagesModels);
            if (msgList.size()==0){
                recyclerView.setVisibility(View.GONE);
                layout.setVisibility(View.VISIBLE);
            }else {
                recyclerView.setVisibility(View.VISIBLE);
                layout.setVisibility(View.GONE);
            }
            adaptor.notifyDataSetChanged();
        });
    }

    public interface NotificationLongClickListener {
        void onNotificationLongClick(DeviceMessagesModel notification);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            viewModel.readAllNotification();
        }, 5000);
        cancelNotification();
    }

    private void cancelNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.readAllNotification();
        cancelNotification();
    }
}
