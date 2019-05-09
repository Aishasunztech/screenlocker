package com.secureMarket;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.settings.codeSetting.installApps.InstallAppModel;
import com.screenlocker.secure.settings.codeSetting.installApps.List;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.secureClear.SecureClearActivity;

import java.io.File;
import java.util.ArrayList;

import static com.screenlocker.secure.utils.Utils.hideKeyboard;

public class SecureMarketActivity extends BaseActivity
        implements SecureMarketAdapter.AppInstallUpdateListener {

    private Toolbar toolbar;
    private PackageManager mPackageManager;
    private ViewPager container;
    private TabLayout tabLayout;
    private EditText et_market_search;
    private LinearLayout root_layout;
    private SearchQueryListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_market);

        mPackageManager = getPackageManager();

        toolbar = findViewById(R.id.marketToolbar);
        container = findViewById(R.id.marketContainer);
        tabLayout = findViewById(R.id.marketTabLayout);
        et_market_search = findViewById(R.id.et_marketSearch);
        root_layout = findViewById(R.id.root_layou_market);
//
//        container.setOnTouchListener(this);
//        tabLayout.setOnTouchListener(this);
//        et_market_search.setOnTouchListener(this);

        et_market_search.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }

        });






        et_market_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MarketFragment marketFragment = (MarketFragment) getSupportFragmentManager().getFragments().get(container.getCurrentItem());
                if(marketFragment != null)
                {
                    listener = marketFragment;
                }
                listener.searchOnQueryChange(et_market_search.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Secure Market");

        container.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                switch (position)
                {
                    case 0:
                        MarketFragment fragment = new MarketFragment();
                        Bundle b = new Bundle();
                        b.putString("check","install");
                        fragment.setArguments(b);

                        return fragment;

                    case 1:
                        MarketFragment fragmentUninstall = new MarketFragment();
                        Bundle b1 = new Bundle();
                        b1.putString("check","uninstall");
                        fragmentUninstall.setArguments(b1);

                        return fragmentUninstall;
                        default:
                            MarketFragment fragmentDefault = new MarketFragment();
                            Bundle b2 = new Bundle();
                            b2.putString("check","install");

                            fragmentDefault.setArguments(b2);

                            return fragmentDefault;
                }

            }

            @Override
            public int getCount() {
                return 2;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                switch (position)
                {
                    case 0:
                        return "Install";
                    case 1:
                        return "UnInstall";
                        default:
                            return "Install";

                }

            }
        });
        tabLayout.setupWithViewPager(container);

    }


    private String getAppLabel(PackageManager pm, String pathToApk) {
        PackageInfo packageInfo = pm.getPackageArchiveInfo(pathToApk, 0);
        if (packageInfo != null) {

            if (Build.VERSION.SDK_INT >= 8) {
                // those two lines do the magic:
                packageInfo.applicationInfo.sourceDir = pathToApk;
                packageInfo.applicationInfo.publicSourceDir = pathToApk;
            }

            CharSequence label = pm.getApplicationLabel(packageInfo.applicationInfo);
            Timber.e("getAppLabel: package name is " + packageInfo.packageName);
            return packageInfo.packageName;

        } else {
            return null;
        }
    }


    @Override
    public void onInstallClick(List app) {
//        DownLoadAndInstallUpdate downLoadAndInstallUpdate = new DownLoadAndInstallUpdate(this, AppConstants.STAGING_BASE_URL + "/getApk/" +
//                CommonUtils.splitName(app.getApk()),app.getApk(),getString(R.string.secure_market_activity));
//        downLoadAndInstallUpdate.execute();
    }

    @Override
    public void onUnInstallClick(List app) {
        File fileApk = getFileStreamPath(app.getApk());
        if (fileApk.exists()) {
            Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
            intent.setData(Uri.parse("package:" + getAppLabel(mPackageManager, fileApk.getAbsolutePath())));

            startActivity(intent);
        }
    }

//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//            if (v.getId() != R.id.et_marketSearch) {
//                et_market_search.clearFocus();
//                et_market_search.setFocusable(false);
//                hideKeyboard(SecureMarketActivity.this);
//            }
//        return false;
//    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN)
        {
            View view = getCurrentFocus();
            int id = view.getId();
            int searchId = R.id.root_layou_market;
            if((view instanceof EditText))
            {
                Rect outRect = new Rect();
                view.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)ev.getRawX(), (int)ev.getRawY())) {
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

    public interface SearchQueryListener{
        void searchOnSubmit(String query);
        void searchOnQueryChange(String query);
    }



}
