package com.screenlocker.secure.manual_load;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.core.content.FileProvider;

import com.google.gson.Gson;
import com.screenlocker.secure.socket.model.InstallModel;
import com.screenlocker.secure.socket.model.Settings;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.GET_APK_ENDPOINT;
import static com.screenlocker.secure.utils.AppConstants.LIVE_URL;
import static com.screenlocker.secure.utils.AppConstants.MOBILE_END_POINT;
import static com.screenlocker.secure.utils.AppConstants.TOKEN;

public class DownloadPushedApps extends AsyncTask<Void, Integer, ArrayList<InstallModel>> {

    private String appName, setting_id;
    private ArrayList<InstallModel> InstallModels;
    private WeakReference<Context> contextWeakReference;


    private DownloadCompleteListener downloadCompleteListener;


    public DownloadPushedApps(DownloadCompleteListener downloadCompleteListener, Context context, final ArrayList<InstallModel> InstallModels, String setting_id) {
        contextWeakReference = new WeakReference<>(context);
        this.InstallModels = InstallModels;
        this.setting_id = setting_id;
        this.downloadCompleteListener = downloadCompleteListener;

    }


    private ArrayList<InstallModel> installModelList = new ArrayList<>();

    @Override
    protected ArrayList<InstallModel> doInBackground(Void... voids) {
        String live_url = PrefUtils.getStringPref(contextWeakReference.get(), LIVE_URL);

        for (int i = 0; i < InstallModels.size(); i++) {


            Timber.d("Downloading %s", i + 1 + "/" + InstallModels.size());

            InstallModel model = InstallModels.get(i);


            File file = downloadApp(live_url +MOBILE_END_POINT+ GET_APK_ENDPOINT + CommonUtils.splitName(model.getApk()));

            installModelList.add(model);

            if (file == null) {
                model.setApk_uri(null);
                sendBroadcast(contextWeakReference.get(), false, model, i == InstallModels.size() - 1, false, setting_id);
            } else {
                int file_size = Integer.parseInt(String.valueOf(file.length() / 1024));
                Timber.d("<<< File Size : >>> %s ", file_size);

                model.setApk_uri(null);
                if (file_size >= 101) {

                    Uri contentUri = FileProvider.getUriForFile(contextWeakReference.get(), contextWeakReference.get().getPackageName() + ".fileprovider", file);
                    model.setApk_uri(contentUri.toString());

                    ArrayList<InstallModel> list = utils.getArrayList(contextWeakReference.get());

                    Optional<InstallModel> result = list.stream().filter(model1 -> model1.getPackage_name().equals(model.getPackage_name())).findFirst();
                    result.ifPresent(list::remove);

                    list.add(model);

                    utils.saveArrayList(list, contextWeakReference.get());

                    sendBroadcast(contextWeakReference.get(), true, model, i == InstallModels.size() - 1, false, setting_id);
                } else {
                    model.setApk_uri(null);
                    sendBroadcast(contextWeakReference.get(), false, model, i == InstallModels.size() - 1, false,setting_id);
                }
            }


        }


        return installModelList;

    }


    //send Intent with success status of package push request
    private void sendBroadcast(Context context, boolean status, InstallModel model, boolean isLast, boolean insertApp, String Settings_id) {

        Timber.d(model.getPackage_name());

        Intent appInstalled = new Intent();
        appInstalled.setComponent(new ComponentName(context.getPackageName(), "com.screenlocker.secure.socket.receiver.AppsStatusReceiver"));
        appInstalled.setAction("com.secure.systemcontroll.PackageAdded");
        appInstalled.putExtra("packageAdded", new Gson().toJson(model));
        appInstalled.putExtra("status", status);
        appInstalled.putExtra("setting_id", Settings_id);
        appInstalled.putExtra("isPolicy", model.isPolicy());
        appInstalled.putExtra("isLast", isLast);
        appInstalled.putExtra("insertApp", insertApp);

        context.sendBroadcast(appInstalled);


    }

    private File downloadApp(String url) {
        FileOutputStream fileOutputStream = null;
        InputStream input = null;
        try {
            appName = new Date().getTime() + ".apk";
            File InstallModelsPath = new File(contextWeakReference.get().getFilesDir(), ".apk");
            File file = new File(InstallModelsPath, appName);
            if (!InstallModelsPath.exists()) {
                InstallModelsPath.mkdir();
            }
            try {
                fileOutputStream = new FileOutputStream(file);
                URL downloadUrl = new URL(url);
                URLConnection connection = downloadUrl.openConnection();
                connection.setRequestProperty("authorization", PrefUtils.getStringPref(contextWeakReference.get(), TOKEN));
                int contentLength = connection.getContentLength();
                // input = body.byteStream();
                input = new BufferedInputStream(downloadUrl.openStream());
                byte[] data = new byte[contentLength];
                long total = 0;
                int count;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    Timber.d("<<< Progress : >>> %s", (int) ((total * 100) / contentLength));
                    publishProgress((int) ((total * 100) / contentLength));
                    fileOutputStream.write(data, 0, count);
                }


                //Uri uri =  FileProvider.getUriForFile(contextWeakReference.get(), BuildConfig.APPLICATION_ID + ".fileprovider", file);


                return file;


            } catch (Exception e) {
                e.printStackTrace();
                return null;
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

        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

//            tvProgressText.setText(String.valueOf(values[0]));
    }

    @Override
    protected void onPostExecute(ArrayList<InstallModel> list) {
        super.onPostExecute(list);
        downloadCompleteListener.onDownloadCompleted(list);
    }
}


//Uri contentUri = FileProvider.getUriForFile(contextWeakReference.get(), contextWeakReference.get().getPackageName() + ".fileprovider", file);