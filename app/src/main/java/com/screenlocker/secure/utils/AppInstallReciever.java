package com.screenlocker.secure.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import timber.log.Timber;

public class AppInstallReciever extends BroadcastReceiver {
 
    Context context;
 
@Override
public void onReceive(Context context, Intent intent) {
 
    this.context = context;
 
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