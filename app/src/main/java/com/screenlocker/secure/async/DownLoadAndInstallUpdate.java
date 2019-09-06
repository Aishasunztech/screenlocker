package com.screenlocker.secure.async;

import android.app.ProgressDialog;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.screenlocker.secure.R;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.socket.model.InstallModel;
import com.screenlocker.secure.utils.AppConstants;
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

import static com.screenlocker.secure.utils.AppConstants.SYSTEM_LOGIN_TOKEN;
import static com.screenlocker.secure.utils.AppConstants.UNINSTALL_ALLOWED;

public class DownLoadAndInstallUpdate extends AsyncTask<Void, Integer, Uri> {
    private String appName, url;
    private WeakReference<Context> contextWeakReference;
    private ProgressDialog dialog;
    private boolean isSilent;
    private JobParameters jobParameters;

    private boolean isCanceled = false;


    public DownLoadAndInstallUpdate(Context context, final String url, boolean isSilent, JobParameters jobParameters) {
        contextWeakReference = new WeakReference<>(context);
        this.url = url;
        this.isSilent = isSilent;
        this.jobParameters = jobParameters;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (!isSilent) {
            dialog = new ProgressDialog(contextWeakReference.get());
            dialog.setTitle("Downloading Update, Please Wait");
            dialog.setCancelable(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, contextWeakReference.get().getResources().getText(R.string.cancel), (dialog, which) -> {
                dialog.dismiss();
                isCanceled = true;
            });


            dialog.show();
        }

    }

    @Override
    protected Uri doInBackground(Void... voids) {


        return downloadApp();
    }


    private File file;

    private Uri downloadApp() {
        FileOutputStream fileOutputStream = null;
        InputStream input = null;
        try {

            URL downloadUrl;
            URLConnection connection;
            appName = new Date().getTime() + ".apk";
            File apksPath = new File(contextWeakReference.get().getFilesDir(), "apk");
            file = new File(apksPath, appName);
            if (!apksPath.exists()) {
                apksPath.mkdir();
            }

            try {
                fileOutputStream = new FileOutputStream(file);
                downloadUrl = new URL(url);
                connection = downloadUrl.openConnection();
                connection.setRequestProperty("authorization", PrefUtils.getStringPref(contextWeakReference.get(), SYSTEM_LOGIN_TOKEN));
                int contentLength = connection.getContentLength();
                Timber.d("downloadUrl: %s ", url);
                // input = body.byteStream();
                input = new BufferedInputStream(downloadUrl.openStream());
                byte[] data = new byte[contentLength];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    if (isCanceled) {
                        file.delete();

                        break;
                    }
                    total += count;
                    publishProgress((int) ((total * 100) / contentLength));
                    fileOutputStream.write(data, 0, count);
                }
                Uri contentUri = FileProvider.getUriForFile(contextWeakReference.get(), contextWeakReference.get().getPackageName() + ".fileprovider", file);

                // Uri uri =  FileProvider.getUriForFile(contextWeakReference.get(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
                Timber.d("downloadApp: %s ", contentUri.toString());
                return contentUri;

            } catch (Exception e) {
                Log.i("checkpolicy", e.getMessage());
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
            Log.i("checkpolicy", e.getMessage());

        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Log.i("checkpolicy", "onProgressUpdate: /////// " + values[0]);

        if (!isSilent) {
            dialog.setProgress(values[0]);
        }


//            tvProgressText.setText(String.valueOf(values[0]));
    }

    @Override
    protected void onPostExecute(Uri uri) {
        super.onPostExecute(uri);

        if (!isSilent) {
            if (dialog != null)
                dialog.dismiss();
        }
        if (uri != null && !isCanceled) {
            showInstallDialog(uri, contextWeakReference.get());
        } else {
            if (!isSilent && !isCanceled)
                Toast.makeText(contextWeakReference.get(), "Some Error Occured", Toast.LENGTH_SHORT).show();
        }


    }

    private void showInstallDialog(Uri apkUri, Context context) {
        //for Build.VERSION.SDK_INT <= 24

        PrefUtils.saveBooleanPref(contextWeakReference.get(), AppConstants.IS_SETTINGS_ALLOW, false);
        PrefUtils.saveBooleanPref(contextWeakReference.get(), UNINSTALL_ALLOWED, true);

        Intent intent = new Intent(Intent.ACTION_VIEW, apkUri);
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //dont forget add this line
        context.startActivity(intent);

        if (jobParameters != null) {
            JobService js = (JobService) contextWeakReference.get();
            js.jobFinished(jobParameters, false);
        }
    }

}