package com.screenlocker.secure.async;

import android.content.Context;
import android.os.AsyncTask;

import com.screenlocker.secure.interfaces.AsyncResponse;

import java.lang.ref.WeakReference;

import timber.log.Timber;

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


//    private boolean urlFound = false;

    @Override
    protected String doInBackground(Void... voids) {

        Context context = contextRef.get();

//        while (!urlFound) {
        Timber.d("finding urls ");

        for (String url : urls) {
            if ((IsReachable(context, url))) {
//                    urlFound = true;
                return url;
            }
//            }

//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }


        return null;
    }


    @Override
    protected void onPostExecute(String result) {

        Timber.d("result " + result);
        response.processFinish(result);
    }
}