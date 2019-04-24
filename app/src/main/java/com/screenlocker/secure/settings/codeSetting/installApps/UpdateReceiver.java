package com.screenlocker.secure.settings.codeSetting.installApps;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.widget.Toast;

import com.screenlocker.secure.settings.SettingsActivity;

public class UpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "In reciver", Toast.LENGTH_SHORT).show();
        try {
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
                if (context.getApplicationInfo().packageName.equals((intent.getData()).getSchemeSpecificPart())) {
                    Toast.makeText(context, "broadcast receiver: " + context, Toast.LENGTH_LONG).show();
                    Intent i = new Intent(context, SettingsActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                }
            }
        } catch (NullPointerException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }


}