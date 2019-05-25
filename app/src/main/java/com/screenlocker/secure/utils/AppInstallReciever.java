package com.screenlocker.secure.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.screenlocker.secure.settings.SettingsActivity;

import timber.log.Timber;

public class AppInstallReciever extends BroadcastReceiver {

@Override
public void onReceive(Context context, Intent intent) {



 if (!intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED) &&
            intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)){
     Toast.makeText(context, "broadcast receiver: " + context, Toast.LENGTH_LONG).show();
     Intent i = new Intent(context, SettingsActivity.class);
     i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     context.startActivity(i);
 }
    // when package removed
  if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
      Timber.tag(" BroadcastReceiver ").e("onReceive called "
              + " PACKAGE_REMOVED ");
    Toast.makeText(context, " onReceive !!!! PACKAGE_REMOVED" + intent.getData().toString(),
                    Toast.LENGTH_LONG).show();

        }
     // when package installed
  else if (intent.getAction().equals(
                "android.intent.action.PACKAGE_ADDED")) {

      Timber.e("onReceive called " + "PACKAGE_ADDED");
    Toast.makeText(context, " onReceive !!!!." + "PACKAGE_ADDED",
                    Toast.LENGTH_LONG).show();

        }
    }
}