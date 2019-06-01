package com.screenlocker.secure.async;

import android.os.AsyncTask;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.utils.AppConstants;

import org.jetbrains.annotations.Async;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class CheckInstance extends AsyncTask<Void, Void, Boolean> {

    private CheckInstance.Consumer mConsumer;


    public interface Consumer {
        void accept(Boolean internet);
    }

    public CheckInstance(CheckInstance.Consumer consumer) {
        mConsumer = consumer;
        execute();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {


        do {
            if (AppConstants.result) {
                return true;
            }
        } while (AppConstants.isProgress);

        return false;
    }

    @Override
    protected void onPostExecute(Boolean internet) {
        mConsumer.accept(internet);
    }

}
