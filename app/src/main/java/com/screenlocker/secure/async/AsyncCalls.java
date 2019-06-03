package com.screenlocker.secure.async;

import android.content.Context;
import android.os.AsyncTask;

import com.screenlocker.secure.interfaces.AsyncResponse;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.screenlocker.secure.utils.AppConstants.URL_1;
import static com.screenlocker.secure.utils.AppConstants.URL_2;
import static com.screenlocker.secure.utils.CommonUtils.IsReachable;

public class AsyncCalls extends AsyncTask<Void, Void, String> {

    private WeakReference<Context> contextRef;

    private AsyncResponse response;

    private String[] urls;

    public AsyncCalls(AsyncResponse mresponse, Context context, String[] urls) {

        response = mresponse;

        contextRef = new WeakReference<>(context);

        this.urls = urls;

    }

    @Override
    protected String doInBackground(Void... voids) {

        Context context = contextRef.get();
        for (String url : urls) {
            if ((IsReachable(context, url))) {
                return url;
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        response.processFinish(result);
    }
}