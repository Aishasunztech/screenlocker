package com.secureSetting.bluetooth.BluetoothAdapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.screenlocker.secure.R;
import com.secureSetting.bluetooth.BluetoothDialogs.ForgetBluetoothDialog;

import java.util.ArrayList;

public class PairedDeviceAdapter extends RecyclerView.Adapter<PairedDeviceAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<BluetoothDevice> pairedDevice;
    private ForgetBluetoothDialog.UnpairDeviceListener listener;

    public PairedDeviceAdapter(Context context, ArrayList<BluetoothDevice> pairedDevice, ForgetBluetoothDialog.UnpairDeviceListener listener) {
        this.context = context;
        this.pairedDevice = pairedDevice;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.paired_device_item,viewGroup,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
           final BluetoothDevice bluetoothDevice = pairedDevice.get(i);
           myViewHolder.tvName.setText(bluetoothDevice.getName());
           myViewHolder.settings_icon.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   ForgetBluetoothDialog forgetBluetoothDialog = new ForgetBluetoothDialog(context,bluetoothDevice,listener);
                   forgetBluetoothDialog.show();
               }
           });
    }

    @Override
    public int getItemCount() {
        return pairedDevice.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tvName;
        ImageView settings_icon;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.paired_device_name);
            settings_icon = itemView.findViewById(R.id.paired_device_settings);
        }
    }
}
