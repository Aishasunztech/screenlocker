package com.screenlocker.secure.settings.codeSetting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.screenlocker.secure.R;
import com.screenlocker.secure.mdm.utils.DeviceIdUtils;

import java.util.List;

public class IMEIActivity extends AppCompatActivity {

    @BindView(R.id.radio_group)
    RadioGroup radioGroup;
    @BindView(R.id.editImei)
    EditText editTextIMEI;
    @BindView(R.id.save_imei)
    Button saveButton;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.slote1)
    RadioButton slot1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imei);
        ButterKnife.bind(this);
        setToolbar();
        List<String> imeis = DeviceIdUtils.getIMEI(IMEIActivity.this);


        slot1.setChecked(true);

        if (imeis.size() != 0) {
            editTextIMEI.setText(imeis.get(0));
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.slote1:
                    if (imeis.size() != 0)
                        editTextIMEI.setText(imeis.get(0));
                    break;
                case R.id.slote2:
                    if (imeis.size() > 1)
                        editTextIMEI.setText(imeis.get(1));
                    break;
            }
        });
        saveButton.setOnClickListener(v -> {
            int id = radioGroup.getCheckedRadioButtonId();
            switch (id) {
                case R.id.slote1:
                    sendIntent(0, editTextIMEI.getText().toString());
                    break;
                case R.id.slote2:
                    sendIntent(1, editTextIMEI.getText().toString());
                    break;
                default:
                    Toast.makeText(this, "Please Select A Slot", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void setToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("IMEI Menu");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendIntent(int slot, String imei) {
        Intent intent = new Intent("com.sysadmin.action.APPLY_SETTING");
        intent.putExtra("setting", "write.imei");
        intent.putExtra("simSlotId", String.valueOf(slot));
        intent.putExtra("imei", imei);

        sendBroadcast(intent);
    }


    @Override
    protected void onPause() {
        super.onPause();

        finish();
    }
}
