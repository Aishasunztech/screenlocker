package com.screenlocker.secure.async;

import android.app.ProgressDialog;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.screenlocker.secure.BuildConfig;
import com.screenlocker.secure.socket.model.InstallModel;
import com.screenlocker.secure.socket.utils.utils;
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

import timber.log.Timber;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static com.screenlocker.secure.settings.codeSetting.installApps.DownLoadAndInstallUpdate.onAppAvailable;
import static com.screenlocker.secure.utils.AppConstants.SYSTEM_LOGIN_TOKEN;

public class DownLoadAndInstallUpdate extends AsyncTask<Void, Integer, Uri> {
    private String appName, url;
    private WeakReference<Context> contextWeakReference;
    private ProgressDialog dialog;
    private boolean isSilent;
    private JobParameters jobParameters;
    private ArrayList<InstallModel> list_apps;

    private static int counterFailed = 0 ;

    public DownLoadAndInstallUpdate(Context context, final String url, boolean isSilent, JobParameters jobParameters, ArrayList<InstallModel> list_apps) {
        contextWeakReference = new WeakReference<>(context);
        this.url = url;
        this.isSilent = isSilent;
        this.jobParameters = jobParameters;
        this.list_apps = list_apps;
        for(InstallModel installModel : list_apps){
            Log.i("checkpolicy", "DownLoadAndInstallUpdate: constructur for : "+ list_apps.size() + "  ...... "+ list_apps.get(0).getApk());
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(list_apps==null){
            if (!isSilent) {
                dialog = new ProgressDialog(contextWeakReference.get());
                dialog.setTitle("Downloading Update, Please Wait");
                dialog.setCancelable(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.show();
            }
        }
    }
    @Override
    protected Uri doInBackground(Void... voids) {
        return downloadApp();
    }


    private Uri downloadApp() {
        FileOutputStream fileOutputStream = null;
        InputStream input = null;
        try {

            URL downloadUrl;
            URLConnection connection;



            appName = new Date().getTime() + ".apk";
            File apksPath = new File(contextWeakReference.get().getFilesDir(), "apk");
            File file = new File(apksPath, appName);
            if (!apksPath.exists()) {
                apksPath.mkdir();
            }

            try {
                fileOutputStream = new FileOutputStream(file);
                if(list_apps == null){
                    downloadUrl = new URL(url);
                    connection = downloadUrl.openConnection();
                    connection.setRequestProperty("authorization", PrefUtils.getStringPref(contextWeakReference.get(), SYSTEM_LOGIN_TOKEN));
                }else{
                    downloadUrl = new URL(list_apps.get(0).getApk());
                    connection = downloadUrl.openConnection();
                    connection.setRequestProperty("authorization", list_apps.get(0).getToken());
                }
                Log.i("checkpolicy", "downloadApp: download url is : "+downloadUrl.toString());
                int contentLength = connection.getContentLength();
                Timber.d("downloadUrl: %s ", downloadUrl.toString());
                Timber.d("downloadUrl: %s ", url);
                // input = body.byteStream();
                input = new BufferedInputStream(downloadUrl.openStream());
                byte data[] = new byte[contentLength];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress((int) ((total * 100) / contentLength));
                    fileOutputStream.write(data, 0, count);
                }
                Uri contentUri = FileProvider.getUriForFile(contextWeakReference.get(), contextWeakReference.get().getPackageName() + ".fileprovider", file);

               // Uri uri =  FileProvider.getUriForFile(contextWeakReference.get(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
                Timber.d("downloadApp: %s ", contentUri.toString());
                return contentUri;

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
        Log.i("checkpolicy", "onProgressUpdate: /////// "+values[0]);
        if(list_apps == null){
            if (!isSilent) {
                dialog.setProgress(values[0]);
            }

        }
//            tvProgressText.setText(String.valueOf(values[0]));
    }

    @Override
    protected void onPostExecute(Uri uri) {
        super.onPostExecute(uri);

        Log.i("checkpolicy", "onPostExecute: uri get is >>>>>>>>>>  "+uri);

        if(list_apps == null){
            if (!isSilent) {
                if (dialog != null)
                    dialog.dismiss();
            }
            if (uri != null) {
                showInstallDialog(uri);
            } else {
                if (!isSilent)
                    Toast.makeText(contextWeakReference.get(), "Some Error Occured", Toast.LENGTH_SHORT).show();
            }
        }else{
            if(uri!=null){

                Log.i("checkpolicy", "onPostExecute: post list size is : "+list_apps.size());

                if(list_apps.size()==1){
                    Log.i("checkpolicy", "onPostExecute: size == 1 ");
                    // add and remove for launch
                    InstallModel installModel = list_apps.get(0);

                    ArrayList<InstallModel> installModels =  utils.getArrayList(contextWeakReference.get());
                    if(installModels==null){
                        installModels = new ArrayList<>();
                    }
                    installModel.setApk(uri.toString());
                    installModels.add(installModel);
                    utils.saveArrayList(installModels,contextWeakReference.get());
                    list_apps.remove(0);
                    if(onAppAvailable!=null){ onAppAvailable.showPolicyApps();}
                }

                if(list_apps.size()>1){
                    Log.i("checkpolicy", "onPostExecute: size > 1  ");
                    InstallModel installModel = list_apps.get(0);
                    ArrayList<InstallModel> installModels =  utils.getArrayList(contextWeakReference.get());
                    if(installModels==null){
                        installModels = new ArrayList<>();
                    }
                    installModel.setApk(uri.toString());
                    installModels.add(installModel);
                    utils.saveArrayList(installModels,contextWeakReference.get());
                    list_apps.remove(0);
                    new DownLoadAndInstallUpdate(contextWeakReference.get(),null,true,null,list_apps).execute();

                }

            }else{

                if(counterFailed < 2){
                    counterFailed =+1;
                    new DownLoadAndInstallUpdate(contextWeakReference.get(),null,true,null,list_apps).execute();

                }else{
                    counterFailed = 0;
                    if(list_apps.size()>1){
                        list_apps.remove(0);
                        new DownLoadAndInstallUpdate(contextWeakReference.get(),null,true,null,list_apps).execute();
                    }else{
                        if(onAppAvailable!=null){ onAppAvailable.showPolicyApps();}
                        // launch screen for install ............
                    }

                }

            }

        }

    }

    private void showInstallDialog(Uri apkUri) {


//            contextWeakReference.get().grantUriPermission("com.secure.systemcontrol",apkUri,
//                    FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//
//            contextWeakReference.get().revokeUriPermission(apkUri,FLAG_GRANT_READ_URI_PERMISSION |Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        Intent launchIntent = new Intent();
        ComponentName componentName = new ComponentName("com.secure.systemcontrol", "com.secure.systemcontrol.MainActivity");
//                        launchIntent.setAction(Intent.ACTION_VIEW);
        launchIntent.setAction(Intent.ACTION_MAIN);
        launchIntent.setComponent(componentName);
        launchIntent.setData(apkUri);
        launchIntent.putExtra("package", contextWeakReference.get().getPackageName());
        launchIntent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
//            contextWeakReference.get().sendBroadcast(sender);
        contextWeakReference.get().startActivity(launchIntent);

        if (jobParameters != null) {
            JobService js = (JobService) contextWeakReference.get();
            js.jobFinished(jobParameters, false);
        }
    }

}