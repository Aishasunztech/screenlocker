/*
 *Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.screenlocker.secure.network;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import retrofit2.http.Url;

//this async task tries to create a socket connection with google.com. If succeeds then return true otherwise false
public class CheckInternetTask extends AsyncTask<Void, Void, Boolean> {

    private WeakReference<TaskFinished<Boolean>> mCallbackWeakReference;

    public CheckInternetTask(TaskFinished<Boolean> callback) {

        mCallbackWeakReference = new WeakReference<>(callback);
    }

    @Override
    protected Boolean doInBackground(Void... params) {


        //parse url. if url is not parsed properly then return

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
