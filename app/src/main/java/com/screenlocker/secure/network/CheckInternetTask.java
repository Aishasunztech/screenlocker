package com.screenlocker.secure.network;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class CheckInternetTask extends AsyncTask<Void, Void, Boolean> {

    private WeakReference<TaskFinished<Boolean>> mCallbackWeakReference;

    public CheckInternetTask(TaskFinished<Boolean> callback) {

        mCallbackWeakReference = new WeakReference<>(callback);
    }

    @Override
    protected Boolean doInBackground(Void... params) {


        try {
            URL[] urls = {
                    new URL("https://api.meshguard.co"),
                    new URL("https://devapi.meshguard.co"),
                    new URL("https://api.lockmesh.com"),
                    new URL("https://devapi.lockmesh.com"),
                    new URL("https://api.titansecureserver.com"),
                    new URL("https://clients3.google.com/generate_204"),
                    new URL("https://securenet.guru"),
            };


            for (URL value : urls) {

                try {

                    //open connection. If fails return false
                    HttpURLConnection urlConnection;
                    try {
                        urlConnection = (HttpURLConnection) value.openConnection();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }

                    urlConnection.setRequestProperty("User-Agent", "Android");
                    urlConnection.setRequestProperty("Connection", "close");
                    urlConnection.setConnectTimeout(1500);
                    urlConnection.setReadTimeout(1500);
                    urlConnection.connect();

                    if (urlConnection.getResponseCode() == 200 || urlConnection.getResponseCode() == 204) {
                        return true;
                    }
                } catch (IOException e) {
                    Log.i("dgjdgd", e.getMessage());
                }

            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return false;

    }

    @Override
    protected void onPostExecute(Boolean isInternetAvailable) {
        TaskFinished<Boolean> callback = mCallbackWeakReference.get();
        if (callback != null) {
            callback.onTaskFinished(isInternetAvailable);
        }
    }
}
