package com.secureSetting.wifisettings.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
;import com.screenlocker.secure.R;

import static com.secureSetting.UtilityFunctions.clickForget;
import static com.secureSetting.wifisettings.WifiMainActivity.networkId;


public class PasswordDialog extends Dialog {
    LinearLayout container;
    TextView btnConnect, btnForget,btnCancel;
    EditText etPass;
    String nameSSID;
    TextView tvTitle;
    CheckBox showPasworrdBox;
    private Context context;
    private WifiManager wifiManager;
    private int width;

    private String nameBSSID;
    private boolean isAlreadyConnected;


    public PasswordDialog(Context context, String ssid,String bssid,WifiManager wifiManager,int width,boolean isAlreadyConnected) {

        super(context);
        nameSSID =ssid;
        this.nameBSSID =bssid;
        this.context = context;
        this.wifiManager = wifiManager;
        this.width = width;
        this.isAlreadyConnected = isAlreadyConnected;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_alert_dialog);
        etPass = findViewById(R.id.editText);
        showPasworrdBox = findViewById(R.id.is_showPAssword);
        container = findViewById(R.id.container);
        tvTitle = findViewById(R.id.tv_wifi_title);
        btnConnect = findViewById(R.id.btnAddpassword);
        btnForget = findViewById(R.id.btnDisconnect);
        btnCancel = findViewById(R.id.btnCancel);
        etPass.setFocusableInTouchMode(true);

        if(etPass.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        showPasworrdBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    etPass.setTransformationMethod(null);
                }else{
                    etPass.setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });


        if(isAlreadyConnected)
        {
            btnConnect.setVisibility(View.GONE);
            btnForget.setVisibility(View.VISIBLE);
            etPass.setVisibility(View.GONE);
        }
        else{
            btnForget.setVisibility(View.GONE);
            btnConnect.setVisibility(View.VISIBLE);
            etPass.setVisibility(View.VISIBLE);


        }

        container.setMinimumWidth(width);
        tvTitle.setText(nameSSID);


        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectNetwork(false);
            }

        });

        btnForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickForget(context,nameSSID);
                PasswordDialog.this.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PasswordDialog.this.dismiss();
            }
        });
    }



    public void connectNetwork(boolean isOpen) {

        WifiConfiguration conf = new WifiConfiguration();
        conf.hiddenSSID = true;
        conf.SSID = "\"" + nameSSID + "\"";

        if(isOpen)
        {
            conf.priority = 0xBADBAD;
            conf.status = WifiConfiguration.Status.ENABLED;
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        }
        else{
            conf.preSharedKey = "\""+ etPass.getText().toString()+"\"";
            conf.status = WifiConfiguration.Status.ENABLED;

            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            conf.priority = 99999;


        }
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        Toast.makeText(context, "Connecting...", Toast.LENGTH_LONG).show();

        new ConnectTask(wifiManager,conf).execute();

//        wifiManager.disconnect();
//        networkId = wifiManager.addNetwork(conf);
//        boolean enableNetwork = wifiManager.enableNetwork(networkId,true);
//
//        if(enableNetwork)
//        {
//            SharedPreferences connectedNetworks = context.getSharedPreferences(context.getString(R.string.connectNetwork), Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor= connectedNetworks.edit();
//            editor.putInt("networkId",networkId);
//            editor.putString("BSSID",nameBSSID);
//            editor.putString("SSID",nameSSID);
//            editor.apply();
//            SharedPreferences removedNetworks = context.getSharedPreferences(context.getString(R.string.removedNetwork),Context.MODE_PRIVATE);
//            removedNetworks.edit().clear().apply();
//        }
//        else{
//            Toast.makeText(context, "Wrong password", Toast.LENGTH_SHORT).show();
//        }
//        Log.d("enableNetwork",enableNetwork + "");
//        wifiManager.reconnect();
//


    }



    private class ConnectTask extends AsyncTask<Void,Void,Boolean>
    {

        private WifiManager wifiManager;
        private WifiConfiguration conf;


        public ConnectTask(WifiManager wifiManager, WifiConfiguration conf) {
            this.wifiManager = wifiManager;
            this.conf = conf;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            wifiManager.disconnect();
            networkId = wifiManager.addNetwork(conf);
            boolean enableNetwork = wifiManager.enableNetwork(networkId,true);
            wifiManager.reconnect();

            return enableNetwork;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean){
                SharedPreferences removedNetworks = context.getSharedPreferences(context.getString(R.string.removedNetwork),Context.MODE_PRIVATE);
                removedNetworks.edit().clear().apply();
            }
            else{
                Toast.makeText(context, "Unable to connect Try again", Toast.LENGTH_SHORT).show();
            }
            PasswordDialog.this.dismiss();

        }
    }


}