package com.liveClientChat;

import android.app.NotificationManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.secure.launcher.R;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;
import static com.screenlocker.secure.utils.AppConstants.IS_LIVE_CLIENT_VISIBLE;
import static com.screenlocker.secure.utils.AppConstants.NUMBER_OF_NOTIFICATIONS;
import static com.screenlocker.secure.utils.AppConstants.OFFLINE_DEVICE_ID;

public class LiveClientChatActivity extends AppCompatActivity {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.webview)
    WebView webview;
    private PrefUtils prefUtils;
//    @BindView(R.id.swipe_refresh)
//    SwipeRefreshLayout swipeRefresh;


    //    String url = "http://devlivechat.lockmesh.com/livezilla/chat.php?ptn="; //Live Url
    String url = "";
    @BindView(R.id.webviewProgress)
    ProgressBar progressbar;
    @BindView(R.id.no_internet_layout)
    LinearLayout no_internet_layout;

    private LockScreenService mService;
    private boolean isSocketConnect;

    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_client_chat);
        ButterKnife.bind(this);
        prefUtils = PrefUtils.getInstance(this);
        isSocketConnect = prefUtils.getBooleanPref( AppConstants.CLIENT_CHAT_SOCKET);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


//        webview.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                progressbar.setVisibility(View.GONE);
//                Log.d("lksjhdf","notification cleared");
//                prefUtils.saveIntegerPref(LiveClientChatActivity.this,NUMBER_OF_NOTIFICATIONS,0);
//                notificationManager.cancelAll();
//
//                super.onPageFinished(view, url);
//            }
//
//            @Override
//            public void onLoadResource(WebView view, String url) {
//                super.onLoadResource(view, url);
//
//                prefUtils.saveIntegerPref(LiveClientChatActivity.this,NUMBER_OF_NOTIFICATIONS,0);
//                notificationManager.cancelAll();
//
//            }
//        });
        progressbar.setVisibility(View.GONE);

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {

                progressbar.setVisibility(View.GONE);
                prefUtils.saveIntegerPref( NUMBER_OF_NOTIFICATIONS, 0);
                notificationManager.cancelAll();

                super.onPageFinished(view, url);
            }

        });
        progressbar.setVisibility(View.GONE);

        String deviceId = prefUtils.getStringPref( DEVICE_ID);
        String title = "";
        String subTitle = "";

        if (deviceId != null) {
            title = getResources().getString(R.string.live_client_device_id);
            subTitle = deviceId;
        } else {
            title = getResources().getString(R.string.live_client_device_id);

            String offline_device = prefUtils.getStringPref( OFFLINE_DEVICE_ID);

            if (offline_device == null) {
                subTitle = "N/A";
            } else {
                subTitle = offline_device;
            }

            deviceId = DeviceIdUtils.getSerialNumber();
        }
        url = AppConstants.CLIENT_CHAT_URL + deviceId + "&pto=true&ptq=";
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setSubtitle(subTitle);


//        swipeRefresh.setOnRefreshListener(() -> {
//            loadWebView();
////            if(isConnected() && mService == null)
////            {
////                bindToService();
////            }
//            swipeRefresh.setRefreshing(false);
//        });

        loadWebView();


    }


    private void loadWebView() {
        if (isConnected()) {
            progressbar.setVisibility(View.VISIBLE);
            webview.loadUrl(url);
            no_internet_layout.setVisibility(View.GONE);
        } else {
            no_internet_layout.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.btn_try_again)
    public void onViewClicked(View vi) {
        loadWebView();
    }

    private boolean isConnected() {
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        if (netInfo == null) {
            return false;
        } else {
            return true;
        }
    }
//
//    private ServiceConnection connection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName className,
//                                       IBinder service) {
//            // We've bound to LocalService, cast the IBinder and get LocalService instance
//            LockScreenService.LocalBinder binder = (LockScreenService.LocalBinder) service;
//            mService = binder.getService();
//            mService.connectClientChatSocket();
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//        }
//    };

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        if(isConnected()) {
//            bindToService();
//        }
//
//    }

    @Override
    protected void onResume() {
        super.onResume();

        prefUtils.saveBooleanPref( IS_LIVE_CLIENT_VISIBLE, true);
    }

//    private void bindToService() {
//        Intent intent = new Intent(this, LockScreenService.class);
//        bindService(intent, connection, Context.BIND_AUTO_CREATE);
//    }

    @Override
    protected void onStop() {
        super.onStop();

        prefUtils.saveBooleanPref( IS_LIVE_CLIENT_VISIBLE, false);

//        if (mService != null)
//            unbindService(connection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh_chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_refresh:
                loadWebView();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
