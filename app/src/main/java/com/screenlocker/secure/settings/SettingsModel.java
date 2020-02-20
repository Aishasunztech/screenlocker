package com.screenlocker.secure.settings;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.secure.launcher.R;
import com.screenlocker.secure.service.LockScreenService;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class SettingsModel implements SettingContract.SettingsMvpModel {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private Context context;

    public SettingsModel(Context context) {
        this.context = context;

    }


    /**
     * checks that the passed service object is running or not
     *
     * @return true if that passed service is running else false
     */
    @Override
    public boolean isServiceRunning() {

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LockScreenService.class.getName().equals(service.service.getClassName())) {
                Timber.tag(TAG).i(String.valueOf(true));
                return true;
            }
        }
        Timber.i(String.valueOf(false));
        return false;
    }


    /**
     * this method starts the service {@link LockScreenService}
     *
     * @param lockScreenIntent intent to start service
     */
    @Override
    public void startLockService(Intent lockScreenIntent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("dklvmdvkd", "startLockService: ");
            context.startForegroundService(lockScreenIntent);
        } else {
            context.startService(lockScreenIntent);
        }

    }

    /**
     * stop service
     *
     * @param lockScreenIntent
     */
    @Override
    public void stopLockService(Intent lockScreenIntent) {
        context.stopService(lockScreenIntent);
    }


    @Override
    public void showAlertDialog(final EditText input, final DialogInterface.OnClickListener onClickListener, final DialogInterface.OnClickListener onNegativeClick, String title) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setCancelable(false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(10, 10, 10, 10);

        input.setGravity(Gravity.CENTER);

        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        //input.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        alertDialog.setIcon(R.drawable.ic_secure_settings);
        input.setFocusable(true);
        input.setFocusableInTouchMode(true);
        input.clearFocus();
        input.requestFocus();
        input.postDelayed(new Runnable(){
                               @Override public void run(){
                                   InputMethodManager keyboard=(InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                   keyboard.showSoftInput(input,0);
                               }
                           }
                ,100);
//

        alertDialog.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onClickListener.onClick(dialog, which);
                        try {
//                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if (onNegativeClick != null)
                                onNegativeClick.onClick(dialog, which);
//                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dialog.cancel();
                    }
                });

        alertDialog.show();

    }

    @Override
    public String get_IMEI_number(TelephonyManager telephonyManager) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            return "";
        }
        return telephonyManager.getDeviceId();
    }

    @Override
    public boolean isMyLauncherDefault() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<IntentFilter>();
        filters.add(filter);

        final String myPackageName = context.getPackageName();
        List<ComponentName> activities = new ArrayList<ComponentName>();
        final PackageManager packageManager = context.getPackageManager();

        // You can use name of your package here as third argument
        packageManager.getPreferredActivities(filters, activities, null);

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }
        return false;
    }

}
