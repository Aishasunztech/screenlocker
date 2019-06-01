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

    public AsyncCalls(AsyncResponse mresponse,Context context){

            response = mresponse;

        contextRef = new WeakReference<>(context);

    }

    @Override
    protected String doInBackground(Void... voids) {

        Context context = contextRef.get();

        List<String> hosts  = new ArrayList<>();

        hosts.add(URL_1);
        hosts.add(URL_2);

        for (String host:hosts){
            Timber.d(host);
            if((IsReachable(context,host))){
                return host;
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        response.processFinish(result);
    }
}