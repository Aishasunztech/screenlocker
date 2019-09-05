package com.liveClientChat;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.screenlocker.secure.R;
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


    String url = "http://devlivechat.lockmesh.com/livezilla/chat.php?ptn=";
    @BindView(R.id.webviewProgress)
    ProgressBar progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_client_chat);
        ButterKnife.bind(this);

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.setWebViewClient(new WebViewClient(){
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

            if (deviceId != null && !deviceId.equals("") && !deviceId.equals("N/A")) {
                title = "Live Customer Support (" + deviceId + ")";
            } else {
                title = "Live Customer Support N/A";
            }
            url = AppConstants.CLIENT_CHAT_URL + deviceId;


        getSupportActionBar().setTitle(title);


        swipeRefresh.setOnRefreshListener(() -> {
            loadWebView();
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

}
