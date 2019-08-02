package com.secureSetting;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.utils.AppConstants;
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
                return MyApplication.getAppDatabase(contextWeakReference.get()).getDao().getEncryptedApps(true);
            }else {
                return MyApplication.getAppDatabase(contextWeakReference.get()).getDao().getGuestApps(true);
            }

            
        }

        @Override
        protected void onPostExecute(List<AppInfo> appInfos) {
            if(appInfos != null && appInfos.size()>0){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    appInfos.removeIf(appInfo -> appInfo.getUniqueName().equals(AppConstants.SECURE_SETTINGS_UNIQUE) ||
                            appInfo.getUniqueName().equals(AppConstants.SECURE_CLEAR_UNIQUE)||
                            appInfo.getUniqueName().equals(AppConstants.SECURE_MARKET_UNIQUE) ||
                            appInfo.getUniqueName().equals(AppConstants.SFM_UNIQUE) ||
                            appInfo.getUniqueName().equals(AppConstants.SUPPORT_UNIQUE) );
                }
                listener.getApps(appInfos);
            }
        }
        
        public interface GetAppsListener{
            void getApps(List<AppInfo> appInfoList);
        }
    }