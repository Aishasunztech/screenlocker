package com.contactSupport;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.screenlocker.secure.R;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.utils.AppConstants;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.Date;

public class ChatActivity extends AppCompatActivity implements MessagesListAdapter.OnMessageLongClickListener {
    private ChatViewModel viewModel;
    private MessagesListAdapter<ChatMessages> adapter;
    private MessageInput input;
    private MessagesList list;


    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            finish();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(AppConstants.BROADCAST_ACTION));
        Toolbar mToolBar = findViewById(R.id.toolbar);
        input = findViewById(R.id.input);
        list = findViewById(R.id.messagesList);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Admin Chat");
        adapter = new MessagesListAdapter<>(DeviceIdUtils.getSerialNumber(), (imageView, url, payload) -> Glide.with(ChatActivity.this).load(url).into(imageView));
        list.setAdapter(adapter);
        adapter.setOnMessageLongClickListener(this);
        viewModel = ViewModelProviders.of(this).get(ChatViewModel.class);
        viewModel.getAllMessages().observe(this, chatMessages -> {
            viewModel.getAllMessages().removeObservers(this);
            adapter.addToEnd(chatMessages, false);
        });
        input.setInputListener(input1 -> {
            ChatMessages msg = new ChatMessages(input1.toString(), DeviceIdUtils.getSerialNumber(), new Date(), null);
            viewModel.insertMessage(msg);
            adapter.addToStart(msg,true);
            return true;
        });

    }

    @Override
    public void onMessageLongClick(IMessage message) {
        ChatMessages msg = (ChatMessages) message;
        new AlertDialog.Builder(this)
                .setTitle("Delete")
                .setIcon(android.R.drawable.ic_delete)
                .setMessage("Do yo want To delete this message?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteMessage(msg);
                    adapter.deleteById(String.valueOf(msg.getId()));
                }).setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                }).show();
    }
}
