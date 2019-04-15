package com.secureSetting.bluetooth.BluetoothDialogs;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.screenlocker.secure.R;


public class BluetoothNameDialog extends Dialog {
    private TextView btnCancel,btnRename;
    private EditText et_rename;
    private LinearLayout container;
    private boolean isTextChanged = false;
    ChangeBNameListener changeBNameListener;

    private BluetoothAdapter bluetoothAdapter;
    public BluetoothNameDialog(Context context,ChangeBNameListener changeBNameListener) {
        super(context);

        this.changeBNameListener = changeBNameListener;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_bluetooth_name);

        et_rename = findViewById(R.id.et_rename_bluetooth);
        btnCancel = findViewById(R.id.btnCancelRename);
        btnRename = findViewById(R.id.btnRenameBT);
        container = findViewById(R.id.name_containerBT);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        final String rename = bluetoothAdapter.getName();
        if(rename != null)
        {
            et_rename.setText(rename);
        }

        et_rename.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isTextChanged = true;
                btnRename.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        btnRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(isTextChanged)
               {
                   bluetoothAdapter.setName(et_rename.getText().toString());
                   changeBNameListener.nameChanged();
                   BluetoothNameDialog.this.dismiss();
               }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothNameDialog.this.dismiss();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        container.setMinimumWidth(width);

    }

    public interface ChangeBNameListener{
        void nameChanged();
    }
}
