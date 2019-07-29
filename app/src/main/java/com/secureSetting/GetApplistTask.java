package com.secureSetting;

import android.content.Context;
import android.os.AsyncTask;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;

import java.lang.ref.WeakReference;
import java.util.List;

public class GetApplistTask extends AsyncTask<Void,Void, List<AppInfo>>
    {

        private WeakReference<Context> contextWeakReference;
        private GetAppsListener listener;

        public GetApplistTask(Context context) {
            contextWeakReference = new WeakReference<>(context);
            listener = (GetAppsListener) context;
            
        }

        @Override
        protected List<AppInfo> doInBackground(Void... voids) {
            return MyApplication.getAppDatabase(contextWeakReference.get()).getDao().getApps();
            
        }

        @Override
        protected void onPostExecute(List<AppInfo> appInfos) {
            if(appInfos != null && appInfos.size()>0)
            {
                listener.getApps(appInfos);
            }
        }
        
        public interface GetAppsListener{
            void getApps(List<AppInfo> appInfoList);
        }
    }