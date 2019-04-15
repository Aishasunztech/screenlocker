package com.secureSetting.wifisettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.screenlocker.secure.R;
import com.secureSetting.wifisettings.Dialogs.ForgetWifiDialog;
import com.secureSetting.wifisettings.Dialogs.PasswordDialog;

import java.util.List;

import static com.secureSetting.UtilityFunctions.isWifiConnected;
import static com.secureSetting.UtilityFunctions.isWifiOpen;

public class WifiAdapter extends RecyclerView.Adapter<WifiAdapter.MyViewHolder> {
    public static final String TAG = "Adapter";
    List<ScanResult> list;
    Context context;
    WifiManager wifiManager;
    ForgetWifiDialog.ConnectListener connectListener;
    private SharedPreferences removedNetworks;
    String mConnectedWifi;
    private SharedPreferences connectNetworks;
    List<WifiConfiguration> mConfiguredNetworks;

    public WifiAdapter(List<ScanResult> list, Context context, WifiManager wifiManager, ForgetWifiDialog.ConnectListener connectListener) {
        this.list = list;
        this.context = context;
        this.wifiManager = wifiManager;
        mConfiguredNetworks = wifiManager.getConfiguredNetworks();
        int wifiState = wifiManager.getWifiState();
        if(isWifiConnected(context))
        {

        if(wifiState == WifiManager.WIFI_STATE_ENABLED) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            if (wifiInfo.getNetworkId() == -1) {
                // Not connected to an access point

            } else {
                mConnectedWifi = wifiInfo.getSSID();
                Log.d(TAG, "WifiAdapter: "+ mConnectedWifi);
            }
        }}
        this.connectListener = connectListener;
        removedNetworks = context.getSharedPreferences(context.getString(R.string.removedNetwork),Context.MODE_PRIVATE);
        connectNetworks = context.getSharedPreferences(context.getString(R.string.connectNetwork),Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.wifi_item,viewGroup,false);
        return new MyViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder,  int position) {

            final ScanResult scanResult = list.get(position);

            final String capabilities = scanResult.capabilities;
            myViewHolder.tvWifiName.setText(scanResult.SSID);
            int level = scanResult.level;
            setWifiIcon(myViewHolder, scanResult, level);
            //String connectedNetwork = connectNetworks.getString(context.getString(R.string.savedBSSID),"null");
            if (mConnectedWifi != null && mConnectedWifi.contains(scanResult.SSID)){
                myViewHolder.tvWifiStatus.setVisibility(View.VISIBLE);
                if (position != 0) {
                    int index = list.indexOf(scanResult);
                    Log.d(TAG, "onBindViewHolder: "+ index);
//                    Collections.swap(list, index,0 );
                }
            }
            else{
                myViewHolder.tvWifiStatus.setVisibility(View.INVISIBLE);
            }
            /*{
                if(!connectedNetwork.contains("null"))
                {
                    if(isWifiConnected(context))
                    {
                        if(scanResult.BSSID.contains(connectedNetwork) && wifiInfo.getBSSID().contains(connectedNetwork))
                        {

                        }
                        else{
                            myViewHolder.tvWifiStatus.setVisibility(View.GONE);

                        }
                    } else{


                    }
                } else{
                    myViewHolder.tvWifiStatus.setVisibility(View.GONE);

                }
            }*/

//            if(removedNetwork.equals("")) {
//
//
//                if (scanResult.SSID.contains(cleanSSID)) {
//
//                    myViewHolder.tvWifiStatus.setVisibility(View.VISIBLE);
//                } else {
//                    myViewHolder.tvWifiStatus.setVisibility(View.GONE);
//                }
//            }

            myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int width = Resources.getSystem().getDisplayMetrics().widthPixels;
                    PasswordDialog pd = new PasswordDialog(context, scanResult.SSID,scanResult.BSSID, wifiManager, width,false);
                    boolean isOpen = isWifiOpen(scanResult);
                    String storedBSSID = connectNetworks.getString("BSSID","null");
                    if(!(storedBSSID != null && storedBSSID.contains("null")))
                    {
                        if(scanResult.BSSID.contains(storedBSSID) )
                        {
                            ForgetWifiDialog forgetDialog = new ForgetWifiDialog(context,connectListener);
                            forgetDialog.show();
                        }else{

                            if(!isOpen) {
                                pd.show();
                            }
                            else{
                                pd.connectNetwork(true);
                            }
                        }
                    }else{

                        if(!isOpen) {
                            pd.show();
                        }
                        else{
                            pd.connectNetwork(true);
                        }
                    }



                }
            });

    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setWifiIcon(@NonNull MyViewHolder myViewHolder, ScanResult scanResult, int level) {
        boolean isOpen = isWifiOpen(scanResult);

        if (level <= 0 && level >= -50 ) {
            if(isOpen)
            {
                myViewHolder.imgSignal.setImageDrawable(context.getDrawable(R.drawable.signal_4_open));
            }
            else{
                myViewHolder.imgSignal.setImageDrawable(context.getDrawable(R.drawable.signal_wifi_4_lock));

            }

        } else if (level < -50 && level >= -70) {
            if(isOpen)
            {
                myViewHolder.imgSignal.setImageDrawable(context.getDrawable(R.drawable.signal_3_open));

            }else{
                myViewHolder.imgSignal.setImageDrawable(context.getDrawable(R.drawable.signal_wifi_3_lock));
            }

        } else if (level < -70 && level >= -80) {
            if(isOpen)
            {
                myViewHolder.imgSignal.setImageDrawable(context.getDrawable(R.drawable.signal_2_open));

            }else{
                myViewHolder.imgSignal.setImageDrawable(context.getDrawable(R.drawable.signal_wifi_2_lock));
            }
        } else if (level < -80 && level >= -100) {
            if(isOpen)
            {
                myViewHolder.imgSignal.setImageDrawable(context.getDrawable(R.drawable.wifi_signal_1));
            }else{
                myViewHolder.imgSignal.setImageDrawable(context.getDrawable(R.drawable.signal_wifi_1_lock));
            }


        } else {
            if(isOpen)
            {
                myViewHolder.imgSignal.setImageDrawable(context.getDrawable(R.drawable.signal_0_open));
            }
            else{
                myViewHolder.imgSignal.setImageDrawable(context.getDrawable(R.drawable.signal_wifi_0_lock));

            }

        }
    }


    @Override
    public int getItemCount() {

        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tvWifiName,tvWifiStatus;
        ImageView imgSignal;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvWifiName = itemView.findViewById(R.id.wifi_name);
            tvWifiStatus = itemView.findViewById(R.id.connectionStatus);
            imgSignal = itemView.findViewById(R.id.wifi_signal_icon);
        }
    }
}
