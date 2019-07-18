package com.screenlocker.secure.manual_load;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.launcher.MainActivity;
import com.screenlocker.secure.socket.model.InstallModel;
import com.screenlocker.secure.socket.utils.utils;
import com.screenlocker.secure.utils.CommonUtils;
import com.secureMarket.MarketFragment;
import com.secureMarket.SecureMarketAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ManualPullPush extends AppCompatActivity implements ManualPushPullAdapter.PushPullAppsListener , MainActivity.PolicyRefreshListener {

    public static final String PUSH_APP = "push_apps";
    public static final String PULL_APP = "pull_apps";

    @BindView(R.id.recyclerViewManualList)
    RecyclerView recyclerView;
    ManualPushPullAdapter manualPushPullAdapter;

    private static String app_check_Install = null;
    private static String app_check_uninstall = null;
    public static int UNINSTALL_REQUEST_CODE = 100;
    private static InstallModel installModell = null;


    private static final String TAG = "checkpolicy";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_pull_push);
        ButterKnife.bind(this);
        MainActivity.policyRefreshListener = (MainActivity.PolicyRefreshListener)ManualPullPush.this;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        manualPushPullAdapter = new ManualPushPullAdapter(ManualPullPush.this,new ArrayList<>(),ManualPullPush.this);
        setRecyclerAdapter();

    }

    @Override
    protected void onResume() {
        super.onResume();

        ArrayList<InstallModel> appsList = utils.getArrayList(ManualPullPush.this);
        Log.i(TAG, "onResume: called in manual push for size "+appsList.size());

        if(appsList!=null){
            if(app_check_Install!=null){
                if(isPackageInstalled(app_check_Install,getPackageManager())){

                    AppInfo app = new AppInfo(String.valueOf(installModell.loadLabel(pm)),
                            ri.activityInfo.packageName, CommonUtils.convertDrawableToByteArray(ri.activityInfo.loadIcon(pm)));

                    if(installModell!=null){
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                MyApplication.getAppDatabase(ManualPullPush.this).getDao().insertApps(app);
                            }
                        }).start();

                    }

                    if(appsList.size()>0){
                        for(int i = 0 ;i<appsList.size();i++){
                            if(app_check_Install.equals(appsList.get(i).getPackage_name())){
                                appsList.remove(i);
                                break;
                            } } }
                    app_check_Install = null;

                     utils.saveArrayList(appsList,ManualPullPush.this);
                    if(appsList.size()>0){
                        setRecyclerAdapter();
                    }
                    if(appsList.size()==0){
                        finish();
                    } }
            }else {
                if (appsList.size() > 0) {
                        for (InstallModel model : appsList) {
                            Log.i("checkpolicy", "onResume: model is : ... " + model.getApk());
                        }
                        setRecyclerAdapter();
                } else {
                    finish();
                }
            }

        }else{ finish();}
    }

    private void setRecyclerAdapter() {
        ArrayList<InstallModel> list =  utils.getArrayList(ManualPullPush.this);

        if(list!=null){
            Log.i(TAG, "setRecyclerAdapter: for size ... : "+ list.size());
            for(InstallModel model : list){
                Log.i(TAG, "setRecyclerAdapter: list model is : .. package name is  ... "+model.getPackage_name() + " ... app name is  ... "+model.getApk_name());
            }


            if(list.size()>0){
                manualPushPullAdapter = new ManualPushPullAdapter(ManualPullPush.this,list,ManualPullPush.this);
                recyclerView.setAdapter(manualPushPullAdapter);
                recyclerView.setHasFixedSize(true);
            }else {finish();}

        }else{
            Log.i(TAG, "setRecyclerAdapter: list null in ManualPullPush adapter");
        }



    }

    @Override
    public void appTextButtonClick(int position, InstallModel installModel) {

        if(PUSH_APP.equals(installModel.getType_operation())){
            installModell = installModel;
            appInstallRequest(installModel.getApk(),installModel.getPackage_name());
        }

        if(PULL_APP.equals(installModel.getType_operation())){
            if(isPackageInstalled(installModel.getPackage_name(),getPackageManager())){
                appUninstallRequest(installModel.getPackage_name());
            }else{

                ArrayList<InstallModel> appsList = utils.getArrayList(ManualPullPush.this);
                Log.i(TAG, "appTextButtonClick: else condition app not installed ... first  size is : .. "+appsList.size());
                if(appsList.size()>0){
                    for(int i = 0 ;i<appsList.size();i++){
                        if(app_check_uninstall.equals(appsList.get(i).getPackage_name())){
                            Log.i(TAG, "appTextButtonClick: else condition app not installed ... removing package is : .. "+appsList.get(i).getPackage_name());
                            appsList.remove(i);
                            break;
                        } } }
                Log.i(TAG, "appTextButtonClick: else condition app not installed ... second  size is : .. "+appsList.size());
                setRecyclerAdapter();
            }

        }

    }


    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        boolean found = true;
        try {
            packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {

            found = false;
        }

        return found;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UNINSTALL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                ArrayList<InstallModel> appsList = utils.getArrayList(ManualPullPush.this);
                if(appsList.size()>0){
                    for(int i = 0 ;i<appsList.size();i++){
                        if(app_check_uninstall.equals(appsList.get(i).getPackage_name())){
                            appsList.remove(i);
                            break;
                        } } }
                utils.saveArrayList(appsList,ManualPullPush.this);
                setRecyclerAdapter();
            } else if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "onActivityResult: user canceled the (un)install");
                Toast.makeText(this, "application unInstall Request Canceled", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_FIRST_USER) {
                Log.d(TAG, "onActivityResult: failed to (un)install");
                Toast.makeText(this, "application unInstall Request Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void appUninstallRequest(String app_pkg_name){
        app_check_uninstall = app_pkg_name;
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.setData(Uri.parse("package:" + app_pkg_name));
        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
        startActivityForResult(intent, UNINSTALL_REQUEST_CODE);
    }

    private void appInstallRequest(String app_uri,String app_pkg_name){
        app_check_Install = app_pkg_name;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(getPackageManager().canRequestPackageInstalls()){
                Intent intent = ShareCompat.IntentBuilder.from(ManualPullPush.this)
                        .setStream(Uri.parse(app_uri))
                        .setText("text/html")
                        .getIntent()
                        .setAction(Intent.ACTION_VIEW)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .setDataAndType(Uri.parse(app_uri),"application/vnd.android.package-archive");
                startActivity(intent);
            }else{
                startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:"+getPackageName())));

            }
        }
    }

    @Override
    public void onBackPressed() {

        ArrayList<InstallModel> list =  utils.getArrayList(ManualPullPush.this);
        if(list!=null){
            if(list.size()>0){
                Toast.makeText(this, "Please Complete Operation", Toast.LENGTH_SHORT).show();
            }else{
                finish();
            }
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void refreshPolicy() {
        setRecyclerAdapter();
    }
}
