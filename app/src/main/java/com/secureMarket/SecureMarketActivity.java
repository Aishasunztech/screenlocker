package com.secureMarket;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.screenlocker.secure.listener.OnAppsRefreshListener;
import com.screenlocker.secure.settings.codeSetting.installApps.InstallAppModel;
import com.screenlocker.secure.settings.codeSetting.installApps.List;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;
import com.secureClear.SecureClearActivity;

import java.io.File;
import java.util.ArrayList;

import static com.screenlocker.secure.socket.utils.utils.refreshApps;
import static com.screenlocker.secure.utils.AppConstants.UNINSTALL_ALLOWED;
import static com.screenlocker.secure.utils.Utils.hideKeyboard;

public class SecureMarketActivity extends BaseActivity implements OnAppsRefreshListener {

    private PackageManager mPackageManager;
    private ViewPager container;
    private TabLayout tabLayout;
    private EditText et_market_search;
    private LinearLayout root_layout;
    private SearchQueryListener listener;

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            finish();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_market);

        mPackageManager = getPackageManager();

        container = findViewById(R.id.marketContainer);
        tabLayout = findViewById(R.id.marketTabLayout);
        et_market_search = findViewById(R.id.et_marketSearch);
        root_layout = findViewById(R.id.root_layou_market);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(AppConstants.BROADCAST_ACTION));


        container.setOffscreenPageLimit(0);

//
//        container.setOnTouchListener(this);
//        tabLayout.setOnTouchListener(this);
//        et_market_search.setOnTouchListener(this);

//        et_market_search.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                return false;
//            }
//
//        });

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

        container.setAdapter(new MainMarketPagerAdapter(getSupportFragmentManager(), this));
        tabLayout.setupWithViewPager(container);


    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            int id = view.getId();
            int searchId = R.id.root_layou_market;
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

    @Override
    public void onAppsRefresh() {

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
    }
}
