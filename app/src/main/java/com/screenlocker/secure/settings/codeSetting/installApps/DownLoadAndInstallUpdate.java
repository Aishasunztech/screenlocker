package com.screenlocker.secure.settings.codeSetting.installApps;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.R;
import com.screenlocker.secure.socket.model.InstallModel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import androidx.core.content.FileProvider;

import static android.content.Context.MODE_PRIVATE;
import static com.screenlocker.secure.utils.AppConstants.INSTALLED_PACKAGES;
import static com.secureMarket.MarketUtils.savePackages;

public class DownLoadAndInstallUpdate extends AsyncTask<Void, Integer, Boolean> {
    private String appName, url;
    private WeakReference<Context> contextWeakReference;
    private ProgressDialog dialog;
    private String activityName;

    public DownLoadAndInstallUpdate(Context context, final String url, String appName, String activityName) {
        contextWeakReference = new WeakReference<>(context);
        this.url = url;
        this.appName = appName;
        this.activityName = activityName;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        dialog = new ProgressDialog(contextWeakReference.get());
        if (activityName.equals(contextWeakReference.get().getResources().getString(R.string.install_app_activity))) {
            dialog.setTitle(contextWeakReference.get().getResources().getString(R.string.downloading_update));
        } else if (activityName.equals(contextWeakReference.get().getResources().getString(R.string.secure_market_activity))) {
            dialog.setTitle(contextWeakReference.get().getResources().getString(R.string.downloading_app_title));
        } else {
            dialog.setTitle(contextWeakReference.get().getResources().getString(R.string.downloading_update));
        }
        dialog.setCancelable(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        return downloadApp();
    }


    private Boolean downloadApp() {
        FileOutputStream fileOutputStream = null;
        InputStream input = null;
        File file;
        try {
            appName = appName.substring(0, (appName.length() - 4));
            file = contextWeakReference.get().getFileStreamPath(appName);
            if (file.exists())
                return true;
            try {
                fileOutputStream = contextWeakReference.get().openFileOutput(appName, MODE_PRIVATE);
                URL downloadUrl = new URL(url);
                URLConnection connection = downloadUrl.openConnection();
                int contentLength = connection.getContentLength();

                // input = body.byteStream();
                input = new BufferedInputStream(downloadUrl.openStream());
                byte[] data = new byte[contentLength];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress((int) ((total * 100) / contentLength));
                    fileOutputStream.write(data, 0, count);
                }
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                Log.i("SocketServiceII", "downloadApp: exception 1 is : " + e.toString());
                return false;
            } finally {
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                if (input != null)
                    input.close();
            }
        } catch (Exception e) {
            e.printStackTrace();

            Log.i("SocketServiceII", "downloadApp: exception 2 is : " + e.toString());
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        dialog.setProgress(values[0]);
//            tvProgressText.setText(String.valueOf(values[0]));
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        Log.i("SocketServiceII", "onPostExecute: result is : " + aBoolean);
        if (dialog != null)
            dialog.dismiss();
        if (aBoolean) {
            //  showInstallDialog(appName);
            File f = contextWeakReference.get().getFileStreamPath(appName);

            Uri apkUri = FileProvider.getUriForFile(contextWeakReference.get(), BuildConfig.APPLICATION_ID, f);
        }
    }

    private void showInstallDialog(String appName) {

        File f = contextWeakReference.get().getFileStreamPath(appName);
            /*try {
                installPackage(appName);
            } catch (IOException e) {
                Log.d("dddddgffdgg", "showInstallDialog: "+e.getMessage());;
            }*/
        Uri apkUri = FileProvider.getUriForFile(contextWeakReference.get(), BuildConfig.APPLICATION_ID, f);

        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri,
                "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        contextWeakReference.get().startActivity(intent);
    }


}