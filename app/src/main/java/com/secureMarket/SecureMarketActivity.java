package com.secureMarket;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.screenlocker.secure.R;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.listener.OnAppsRefreshListener;
import com.screenlocker.secure.service.DownloadServiceCallBacks;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;
import com.secureMarket.ui.home.MarketFragment;

import static com.screenlocker.secure.socket.utils.utils.refreshApps;
import static com.screenlocker.secure.utils.AppConstants.UNINSTALL_ALLOWED;
import static com.screenlocker.secure.utils.Utils.hideKeyboard;

public class SecureMarketActivity extends BaseActivity implements OnAppsRefreshListener  , DownloadServiceCallBacks {

    private PackageManager mPackageManager;
    private ViewPager container;
    private TabLayout tabLayout;
    private EditText et_market_search;
    private LinearLayout root_layout;
    private LockScreenService mService = null;
    private SearchQueryListener listener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_market);

        ImageView logo = findViewById(R.id.logo);
        ImageView name = findViewById(R.id.name);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(2000);



        AnimationSet animation = new AnimationSet(false); //change to false
        animation.addAnimation(fadeIn);
        logo.setAnimation(animation);
        name.setAnimation(animation);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(
                    this, SMActivity.class
            ));
            SecureMarketActivity.this.finish();
        },3000);



/*
        et_market_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MarketFragment marketFragment = (MarketFragment) getSupportFragmentManager().getFragments().get(container.getCurrentItem());
                if (marketFragment != null) {
                    listener = marketFragment;
                }
                listener.searchOnQueryChange(et_market_search.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
*/



    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if ((view instanceof EditText)) {
                Rect outRect = new Rect();
                view.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    view.clearFocus();
                    hideKeyboard(SecureMarketActivity.this);
                }
            }


//            if(view.getId() != R.id.et_marketSearch)
//            {
//                et_market_search.setFocusable(false);
//                hideKeyboard(SecureMarketActivity.this);
//            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public interface SearchQueryListener {
        void searchOnSubmit(String query);

        void searchOnQueryChange(String query);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        PrefUtils.saveBooleanPref(this, UNINSTALL_ALLOWED, true);
        refreshApps(this);
        AppConstants.TEMP_SETTINGS_ALLOWED = true;
        //refreshApps(SecureMarketActivity.this);
    }


    @Override
    public void onDownLoadProgress(String pn, int progress, long speed) {

//        int index = IntStream.range(0, unInstalledApps.size())
//                .filter(i -> Objects.nonNull(unInstalledApps.get(i)))
//                .filter(i -> pn.equals(unInstalledApps.get(i).getPackageName()))
//                .findFirst()
//                .orElse(-1);
//        if (index != -1) {
//            ServerAppInfo info = unInstalledApps.get(index);
//            info.setProgres(progress);
//            info.setType(ServerAppInfo.PROG_TYPE.VISIBLE);
//            info.setSpeed(speed);
//            uninstalledAdapter.updateProgressOfItem(info, index);
//        }

    }

    @Override
    public void downloadComplete(String filePath, String pn) {

//        int index = IntStream.range(0, unInstalledApps.size())
//                .filter(i -> Objects.nonNull(unInstalledApps.get(i)))
//                .filter(i -> pn.equals(unInstalledApps.get(i).getPackageName()))
//                .findFirst()
//                .orElse(-1);
//        if (index != -1) {
//            ServerAppInfo info = unInstalledApps.get(index);
//            info.setType(ServerAppInfo.PROG_TYPE.INSTALLING);
//            uninstalledAdapter.updateProgressOfItem(info, index);
//        }
//        if (progressDialog.isShowing()) {
//            progressDialog.dismiss();
//        }
//        if (!filePath.equals("") && !pn.equals("")) {
//            showInstallDialog(new File(filePath), pn);
//        }

    }

    @Override
    public void downloadError(String pn) {
//        int index = IntStream.range(0, unInstalledApps.size())
//                .filter(i -> Objects.nonNull(unInstalledApps.get(i)))
//                .filter(i -> pn.equals(unInstalledApps.get(i).getPackageName()))
//                .findFirst()
//                .orElse(-1);
//        if (index != -1) {
//            ServerAppInfo info = unInstalledApps.get(index);
//            info.setProgres(0);
//            info.setType(ServerAppInfo.PROG_TYPE.GONE);
//            uninstalledAdapter.updateProgressOfItem(info, index);
//        }

    }

    @Override
    public void onDownloadStarted(String pn) {
//        int index = IntStream.range(0, unInstalledApps.size())
//                .filter(i -> Objects.nonNull(unInstalledApps.get(i)))
//                .filter(i -> pn.equals(unInstalledApps.get(i).getPackageName()))
//                .findFirst()
//                .orElse(-1);
//        if (index != -1) {
//            ServerAppInfo info = unInstalledApps.get(index);
//            info.setProgres(0);
//            info.setType(ServerAppInfo.PROG_TYPE.VISIBLE);
//            uninstalledAdapter.updateProgressOfItem(info, index);
//        }
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LockScreenService.LocalBinder binder = (LockScreenService.LocalBinder) service;
            mService = binder.getService();
            mService.setMarketDownloadListener(SecureMarketActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            if (intent != null) {
//                String pn = intent.getStringExtra(AppConstants.EXTRA_PACKAGE_NAME);
//                int index = IntStream.range(0, unInstalledApps.size())
//                        .filter(i -> Objects.nonNull(unInstalledApps.get(i)))
//                        .filter(i -> pn.equals(unInstalledApps.get(i).getPackageName()))
//                        .findFirst()
//                        .orElse(-1);
//                if (index != -1) {
//                    ServerAppInfo info = unInstalledApps.get(index);
//                    info.setProgres(0);
//                    info.setInstalled(true);
//                    info.setType(ServerAppInfo.PROG_TYPE.GONE);
//                    uninstalledAdapter.updateProgressOfItem(info, index);
//                }
//            }

        }
    };

}
