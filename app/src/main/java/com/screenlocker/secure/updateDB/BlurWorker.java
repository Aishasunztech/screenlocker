package com.screenlocker.secure.updateDB;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.Log;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.socket.model.InstallModel;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import timber.log.Timber;

import static com.screenlocker.secure.utils.CommonUtils.setSecureSettingsMenu;

public class BlurWorker extends Worker {
    public BlurWorker(
            @NonNull Context appContext,
            @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    private static final String TAG = BlurWorker.class.getSimpleName();

    private boolean isSystemApp(String packageName, Context context) {

        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(packageName, 0);
            //Non-system app
            //System app
            return !info.sourceDir.startsWith("/data/app/");
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


    @NonNull
    @Override

    public Result doWork() {

        Context applicationContext = getApplicationContext();

        Timber.d("sjhdbfsjhfhsafsa");
        try {

            PackageManager pm = applicationContext.getPackageManager();

            List<AppInfo> dbApps = MyApplication.getAppDatabase(applicationContext).getDao().getAppsForBlurWorker(false);

            for (AppInfo info : dbApps) {
                String packageName = info.getPackageName();
                if (!isAppInstalled(applicationContext, packageName) && !isSystemApp(packageName, applicationContext) && !info.isSystemApp()) {
                    Timber.w("DELETING PACKAGE : %s", packageName);
                    MyApplication.getAppDatabase(applicationContext).getDao().deleteOne(packageName);
                }
            }


            Intent i = new Intent(Intent.ACTION_MAIN, null);
            i.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> allApps = pm.queryIntentActivities(i, 0);

            Intent intent = new Intent(Settings.ACTION_SETTINGS);

            List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            String settingPackageName = resolveInfos.get(0).activityInfo.packageName;
            Timber.d(settingPackageName);

            // adding data to the model
            //getRunningApps(pm);

            for (int j = 0; j < allApps.size(); j++) {
                ResolveInfo ri = allApps.get(j);
                AppInfo app = new AppInfo(String.valueOf(ri.loadLabel(pm)),
                        ri.activityInfo.packageName, CommonUtils.convertDrawableToByteArray(ri.activityInfo.loadIcon(pm)));
                app.setUniqueName(app.getPackageName());
                Timber.d("app package %s", ri.loadLabel(pm));
//                if (!dbLabel.equals(label)) {
//                    dbApps.get(j).setLabel(label);
//                    MyApplication.getAppDatabase(applicationContext).getDao().updateApps(dbApps.get(j));
//                }

                if (!dbApps.contains(app)) {

                    // own app
                    if (app.getPackageName().equals(applicationContext.getPackageName())) {

                        app.setGuest(false);
                        app.setEncrypted(true);
                        app.setEnable(true);
                        app.setExtension(false);
                        app.setVisible(true);
                        app.setDefaultApp(true);
                        app.setSystemApp(true);


                    } else if (app.getPackageName().equals("com.rim.mobilefusion.client")) {

                        app.setEncrypted(true);
                        app.setGuest(false);
                        app.setEnable(true);
                        app.setExtension(false);
                        app.setVisible(true);
                        app.setDefaultApp(false);
                        app.setSystemApp(true);

                    } else if (app.getPackageName().equals(settingPackageName)) {

                        app.setGuest(false);
                        app.setVisible(false);
                        app.setDefaultApp(false);
                        app.setEncrypted(false);
                        app.setExtension(false);
                        app.setSystemApp(true);

                    } else if (app.getPackageName().equals("com.secure.launcher1") && applicationContext.getResources().getString(R.string.apktype).equals("BYOD")) {
                        app.setGuest(false);
                        app.setEncrypted(false);
                        app.setEnable(false);
                        app.setExtension(false);
                        app.setVisible(true);
                        app.setDefaultApp(false);
                        app.setSystemApp(true);
                    } else {
                        app.setGuest(true);
                        app.setEncrypted(false);
                        app.setEnable(false);
                        app.setExtension(false);
                        app.setVisible(true);
                        app.setDefaultApp(false);
                        app.setSystemApp(isSystemApp(app.getPackageName(), MyApplication.getAppContext()));
                    }

                    if (!app.getPackageName().equals("com.secure.launcher1"))
                        MyApplication.getAppDatabase(applicationContext).getDao().insertApps(app);
                } else {

                    if (app.getPackageName().equals(applicationContext.getPackageName()) && app.getLabel().contains("BYOD")) {
                        app.setLabel(app.getLabel().replace("BYOD", ""));
                        app.setGuest(app.isGuest());
                        app.setEncrypted(true);
                        app.setEnable(true);
                        app.setExtension(false);
                        app.setVisible(true);
                        app.setDefaultApp(true);
                        app.setSystemApp(true);
                        MyApplication.getAppDatabase(applicationContext).getDao().updateApps(app);
                    }

                    if (app.getPackageName().equals("com.secure.launcher1")) {
                        MyApplication.getAppDatabase(applicationContext).getDao().deleteOne(app.getUniqueName());
                    }

                    if (app.getPackageName().equals(settingPackageName)) {
                        app.setGuest(false);
                        app.setVisible(false);
                        app.setDefaultApp(false);
                        app.setEncrypted(false);
                        app.setExtension(false);
                        app.setSystemApp(true);
                        MyApplication.getAppDatabase(applicationContext).getDao().updateApps(app);
                    }
                }

            }

            AppInfo appInfo = MyApplication.getAppDatabase(applicationContext).getDao().getParticularApp(AppConstants.SECURE_SETTINGS_UNIQUE);

            if (appInfo == null) {
                //Secure settings Extension
                Drawable secureSettingsDrawable = applicationContext.getResources().getDrawable(R.drawable.ic_secure_settings);
                byte[] secure_settings_icon = CommonUtils.convertDrawableToByteArray(secureSettingsDrawable);
                AppInfo secureSettingsExtension = new AppInfo("Secure Settings", AppConstants.SECURE_SETTINGS_PACKAGE, secure_settings_icon);
                secureSettingsExtension.setUniqueName(secureSettingsExtension.getPackageName() + secureSettingsExtension.getLabel());
                secureSettingsExtension.setExtension(true);
                secureSettingsExtension.setGuest(true);
                secureSettingsExtension.setEncrypted(true);
                secureSettingsExtension.setEnable(true);
                secureSettingsExtension.setVisible(true);
                secureSettingsExtension.setDefaultApp(false);
                secureSettingsExtension.setSystemApp(true);

                MyApplication.getAppDatabase(applicationContext).getDao().insertApps(secureSettingsExtension);
            } else {
                appInfo.setExtension(true);
                Drawable wifi_drawable = applicationContext.getResources().getDrawable(R.drawable.ic_secure_settings);
                byte[] secure_settings_icon = CommonUtils.convertDrawableToByteArray(wifi_drawable);
                appInfo.setIcon(secure_settings_icon);
                appInfo.setSystemApp(true);
                MyApplication.getAppDatabase(applicationContext).getDao().updateApps(appInfo);
            }

            AppInfo secureCleanInfo = MyApplication.getAppDatabase(applicationContext).getDao().getParticularApp(AppConstants.SECURE_CLEAR_UNIQUE);

            if (secureCleanInfo == null) {
                //Secure clear Extension
                Drawable clear_drawable = applicationContext.getResources().getDrawable(R.drawable.ic_new_cleaner);
                byte[] secure_clear_icon = CommonUtils.convertDrawableToByteArray(clear_drawable);
                AppInfo clearExtension = new AppInfo("Secure Clear", AppConstants.SECURE_CLEAR_PACKAGE, secure_clear_icon);
                clearExtension.setUniqueName(clearExtension.getPackageName() + clearExtension.getLabel());
                clearExtension.setExtension(false);
                clearExtension.setGuest(false);
                clearExtension.setEncrypted(true);
                clearExtension.setEnable(true);
                clearExtension.setVisible(true);
                clearExtension.setDefaultApp(false);
                clearExtension.setSystemApp(true);
                MyApplication.getAppDatabase(applicationContext).getDao().insertApps(clearExtension);
            } else {
                Drawable clear_drawable = applicationContext.getResources().getDrawable(R.drawable.ic_new_cleaner);
                byte[] secure_clear_icon = CommonUtils.convertDrawableToByteArray(clear_drawable);
                secureCleanInfo.setIcon(secure_clear_icon);
                secureCleanInfo.setExtension(false);
                secureCleanInfo.setSystemApp(true);
                MyApplication.getAppDatabase(applicationContext).getDao().updateApps(secureCleanInfo);
            }


            AppInfo liveClientChatInfo = MyApplication.getAppDatabase(applicationContext).getDao().getParticularApp(AppConstants.LIVE_CLIENT_CHAT_UNIQUE);

            if (liveClientChatInfo == null) {
                //Live Client Chat Extension
                Drawable liveClientChatDrawable = applicationContext.getResources().getDrawable(R.drawable.ic_chat);
                byte[] live_chat_icon = CommonUtils.convertDrawableToByteArray(liveClientChatDrawable);
                AppInfo clientChatExtension = new AppInfo("Live Chat Support", AppConstants.LIVE_CLIENT_CHAT_PACKAGE, live_chat_icon);
                clientChatExtension.setUniqueName(clientChatExtension.getPackageName() + clientChatExtension.getLabel());
                clientChatExtension.setExtension(false);
                clientChatExtension.setGuest(false);
                clientChatExtension.setEncrypted(true);
                clientChatExtension.setEnable(true);
                clientChatExtension.setVisible(true);
                clientChatExtension.setDefaultApp(false);
                clientChatExtension.setSystemApp(true);
                MyApplication.getAppDatabase(applicationContext).getDao().insertApps(clientChatExtension);
            } else {
                Drawable liveClientChatDrawable = applicationContext.getResources().getDrawable(R.drawable.ic_chat);
                byte[] live_chat_icon = CommonUtils.convertDrawableToByteArray(liveClientChatDrawable);
                liveClientChatInfo.setIcon(live_chat_icon);
                liveClientChatInfo.setExtension(false);
                liveClientChatInfo.setSystemApp(true);
                MyApplication.getAppDatabase(applicationContext).getDao().updateApps(liveClientChatInfo);
            }


            AppInfo supportInfo = MyApplication.getAppDatabase(applicationContext).getDao().getParticularApp(AppConstants.SUPPORT_UNIQUE);

            if (supportInfo == null) {
                //Contact Support Extension
                Drawable support_drawable = applicationContext.getResources().getDrawable(R.drawable.ic_chat);
                byte[] support_icon = CommonUtils.convertDrawableToByteArray(support_drawable);
                AppInfo supportExtension = new AppInfo("Contact Support", AppConstants.SUPPORT_PACKAGE, support_icon);
                supportExtension.setUniqueName(supportExtension.getPackageName() + supportExtension.getLabel());
                supportExtension.setExtension(false);
                supportExtension.setGuest(false);
                supportExtension.setEncrypted(false);
                supportExtension.setEnable(true);
                supportExtension.setVisible(true);
                supportExtension.setDefaultApp(false);
                supportExtension.setSystemApp(true);

                MyApplication.getAppDatabase(applicationContext).getDao().insertApps(supportExtension);
            } else {
                supportInfo.setExtension(false);
                supportInfo.setSystemApp(true);
                MyApplication.getAppDatabase(applicationContext).getDao().updateApps(supportInfo);
            }


            AppInfo sfmInfo = MyApplication.getAppDatabase(applicationContext).getDao().getParticularApp(AppConstants.SFM_UNIQUE);

            if (sfmInfo == null) {
                //Secure clear Extension
                Drawable sfm_drawable = applicationContext.getResources().getDrawable(R.drawable.ic_sheild_folder);
                byte[] sfm_icon = CommonUtils.convertDrawableToByteArray(sfm_drawable);
                AppInfo sfmExtension = new AppInfo("Secure File Manager", AppConstants.SFM_PACKAGE, sfm_icon);
                sfmExtension.setUniqueName(sfmExtension.getPackageName() + sfmExtension.getLabel());
                sfmExtension.setExtension(false);
                sfmExtension.setGuest(false);
                sfmExtension.setEncrypted(true);
                sfmExtension.setEnable(true);
                sfmExtension.setVisible(true);
                sfmExtension.setDefaultApp(false);
                sfmExtension.setSystemApp(true);
                MyApplication.getAppDatabase(applicationContext).getDao().insertApps(sfmExtension);
            } else {
                sfmInfo.setExtension(false);
                sfmInfo.setSystemApp(true);
                MyApplication.getAppDatabase(applicationContext).getDao().updateApps(sfmInfo);
            }

            AppInfo secureMarketInfo = MyApplication.getAppDatabase(applicationContext).getDao().getParticularApp(AppConstants.SECURE_MARKET_UNIQUE);

            if (secureMarketInfo == null) {
                //Secure Market Extension
                Drawable market_drawable = applicationContext.getResources().getDrawable(R.drawable.ic_secure_market);
                byte[] secure_market_icon = CommonUtils.convertDrawableToByteArray(market_drawable);
                AppInfo marketExtension = new AppInfo("Secure Market", AppConstants.SECURE_MARKET_PACKAGE, secure_market_icon);
                marketExtension.setUniqueName(marketExtension.getPackageName() + marketExtension.getLabel());
                marketExtension.setExtension(false);
                marketExtension.setGuest(true);
                marketExtension.setEncrypted(true);
                marketExtension.setEnable(true);
                marketExtension.setVisible(true);
                marketExtension.setDefaultApp(false);
                marketExtension.setSystemApp(true);
                MyApplication.getAppDatabase(applicationContext).getDao().insertApps(marketExtension);
            } else {
                Drawable market_drawable = applicationContext.getResources().getDrawable(R.drawable.ic_secure_market);
                byte[] secure_market_icon = CommonUtils.convertDrawableToByteArray(market_drawable);
                secureMarketInfo.setIcon(secure_market_icon);
                secureMarketInfo.setExtension(false);
                secureMarketInfo.setSystemApp(true);
                MyApplication.getAppDatabase(applicationContext).getDao().updateApps(secureMarketInfo);
            }

            List<SubExtension> dbExtensions = MyApplication.getAppDatabase(applicationContext).getDao().getSubExtensions(AppConstants.SECURE_SETTINGS_UNIQUE);
            List<SubExtension> subExtensions = setSecureSettingsMenu(applicationContext);


            boolean isPresent = false;


            if (dbExtensions == null || dbExtensions.size() == 0) {
                //Secure settings Menu
                for (SubExtension subExtension : subExtensions) {
                    MyApplication.getAppDatabase(applicationContext).getDao().insertSubExtensions(subExtension);
                }

            } else {

                if (dbExtensions.size() != subExtensions.size()) {

                    for (SubExtension subExtension : subExtensions) {

                        for (SubExtension dbExtension : dbExtensions) {

                            if (dbExtension.getUniqueExtension().equals(subExtension.getUniqueExtension())) {
                                isPresent = true;
                                break;
                            }
                        }
                        if (!isPresent) {
                            MyApplication.getAppDatabase(applicationContext).getDao().insertSubExtensions(subExtension);
                        } else {
                            isPresent = false;
                        }


                    }
                }

            }

            List<com.screenlocker.secure.socket.model.Settings> settings = MyApplication.getAppDatabase(applicationContext)
                    .getDao().getSettings();
            if (settings != null && settings.size() == 0) {
                Log.d(TAG, "doWork: Yahan par sirf aik bar ana chahiye");
                List<com.screenlocker.secure.socket.model.Settings> localSettings = CommonUtils.getDefaultSetting(applicationContext);
                for (com.screenlocker.secure.socket.model.Settings localSetting : localSettings) {
                    if (!settings.contains(localSetting)) {
                        MyApplication.getAppDatabase(applicationContext).getDao().insertSetting(localSetting);
                    }
                }
            } else if (settings != null && settings.size() == 2) {
                Timber.d("adding screen capture settings");
                com.screenlocker.secure.socket.model.Settings screen_capture_settings = new com.screenlocker.secure.socket.model.Settings(AppConstants.SET_SS, PrefUtils.getBooleanPref(applicationContext, AppConstants.KEY_DISABLE_SCREENSHOT));
                MyApplication.getAppDatabase(applicationContext).getDao().insertSetting(screen_capture_settings);
            }

            return Result.success();
        } catch (Throwable throwable) {

            // Technically WorkManager will return Worker.Result.FAILURE
            // but it's best to be explicit about it.
            // Thus if there were errors, we're return FAILURE
            Timber.tag(TAG).e(throwable, "Error applying blur");
            return Result.failure();
        }
    }


    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}