package com.secureSetting.wifisettings.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.screenlocker.secure.R;

import static com.secureSetting.UtilityFunctions.clickForget;
import static com.secureSetting.UtilityFunctions.returnSignalStrength;


public class ForgetWifiDialog extends Dialog {
    private Context context;
    private LinearLayout container;
    private TextView tvSecurity,tvStatus,tvSignal,
            tvLinkSpeed,tvFrequency,tvTitle,btnForget,btnCancel;
    private SharedPreferences connectedPreference;
    private ConnectListener connectListener;
    public ForgetWifiDialog(Context context, ConnectListener connectListener) {
        super(context);
        this.context = context;
        this.connectListener = connectListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_forget_wifi);

        container = findViewById(R.id.forget_dialog_container);
        tvSecurity = findViewById(R.id.tv_security_type);
        tvStatus = findViewById(R.id.tv_status_type);
        tvSignal = findViewById(R.id.tv_signal_strength);
        tvLinkSpeed = findViewById(R.id.tv_link_speed);
        tvFrequency = findViewById(R.id.tv_frequency);
        tvTitle = findViewById(R.id.forget_wifi_title);
        btnForget = findViewById(R.id.btnForget);
        btnCancel = findViewById(R.id.btnForgetCancel);


        connectedPreference = context.getSharedPreferences(context.getString(R.string.connectNetwork),Context.MODE_PRIVATE);
        final String title = connectedPreference.getString(context.getString(R.string.savedSSID),"null");
        String security = connectedPreference.getString(context.getString(R.string.savedSecurity),"null");
        String status = "Connected";
        int signal = connectedPreference.getInt(context.getString(R.string.savedSignalStrength),0);
        int linkSpeed = connectedPreference.getInt(context.getString(R.string.savedLinkedSpeed),0);
        int frequency = connectedPreference.getInt(context.getString(R.string.savedFrequency),0);

        if(!security.contains("null"))
        {
            tvSecurity.setText(security);
        }
        if(!title.contains("null"))
        {
            tvTitle.setText(title);
        }

        tvStatus.setText(status);
       tvSignal.setText(returnSignalStrength(signal));
        tvLinkSpeed.setText(linkSpeed + " Mbps");

        frequency = frequency/1000;
        tvFrequency.setText(frequency + " Ghz");


        btnForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickForget(context,title);
                ForgetWifiDialog.this.dismiss();
                connectListener.disconnect();

            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForgetWifiDialog.this.dismiss();


            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        container.setMinimumWidth(width);

    }

    public interface ConnectListener{
        void disconnect();

    }
}
