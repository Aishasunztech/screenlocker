package com.secureSetting;

import android.content.Context;
import android.os.AsyncTask;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.utils.PrefUtils;

import java.lang.ref.WeakReference;
import java.util.List;

public class GetApplistTask extends AsyncTask<Void,Void, List<AppInfo>>
    {

        private WeakReference<Context> contextWeakReference;
        private GetAppsListener listener;
        private boolean isEnc;

        public GetApplistTask(Context context,boolean isEnc) {
            contextWeakReference = new WeakReference<>(context);
            listener = (GetAppsListener) context;
            this.isEnc = isEnc;
            
        }

        @Override
        protected List<AppInfo> doInBackground(Void... voids) {
            if (isEnc){
                return MyApplication.getAppDatabase(contextWeakReference.get()).getDao().getEncryptedApps(true,false);
            }else {
                return MyApplication.getAppDatabase(contextWeakReference.get()).getDao().getGuestApps(true,false);
            }

            
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