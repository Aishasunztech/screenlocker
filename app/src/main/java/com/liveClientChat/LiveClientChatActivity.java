package com.liveClientChat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.IBinder;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.screenlocker.secure.R;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;
import com.screenlocker.secure.service.LockScreenService;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.screenlocker.secure.utils.AppConstants.DEVICE_ID;

public class LiveClientChatActivity extends AppCompatActivity {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.webview)
    WebView webview;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;


    //    String url = "http://devlivechat.lockmesh.com/livezilla/chat.php?ptn="; //Live Url
    String url = "";
    @BindView(R.id.webviewProgress)
    ProgressBar progressbar;

    private LockScreenService mService;
    private boolean isSocketConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_client_chat);
        ButterKnife.bind(this);
        isSocketConnect = PrefUtils.getBooleanPref(this, AppConstants.CLIENT_CHAT_SOCKET);


        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {

                progressbar.setVisibility(View.GONE);

                super.onPageFinished(view, url);
            }

        });
        setSupportActionBar(toolbar);
        progressbar.setVisibility(View.GONE);

        String deviceId = PrefUtils.getStringPref(LiveClientChatActivity.this, DEVICE_ID);
        String title = "";

        if (deviceId != null ) {
            title = "Live Customer Support (" + deviceId + ")";
        } else {
            title = "Live Customer Support N/A";
            deviceId = DeviceIdUtils.getSerialNumber();
        }
        url = AppConstants.CLIENT_CHAT_URL + deviceId + "&pto=true";


        getSupportActionBar().setTitle(title);


        swipeRefresh.setOnRefreshListener(() -> {
            loadWebView();
            if(isConnected() && mService == null)
            {
                bindToService();
            }
            swipeRefresh.setRefreshing(false);
        });

        loadWebView();


    }


    private void loadWebView() {
        if (isConnected()) {
            progressbar.setVisibility(View.VISIBLE);
            webview.loadUrl(url);
        } else {
            Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
        }
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

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LockScreenService.LocalBinder binder = (LockScreenService.LocalBinder) service;
            mService = binder.getService();
            mService.connectClientChatSocket();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        if(isConnected()) {
            bindToService();
        }

    }

    private void bindToService() {
        Intent intent = new Intent(this, LockScreenService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mService != null)
            unbindService(connection);
    }

}
