package com.screenlocker.secure.settings.codeSetting;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;

import android.content.ComponentName;
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
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.List;

import static com.screenlocker.secure.mdm.utils.DeviceIdUtils.isValidImei;

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
        if (isValidImei(imei)) {

            PrefUtils.saveBooleanPref(this, AppConstants.IMEI_CHANGED,true);

            Intent intent = new Intent("com.sysadmin.action.APPLY_SETTING");
            intent.putExtra("setting", "write.imei");
            intent.putExtra("simSlotId", String.valueOf(slot));
            intent.putExtra("imei", imei);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.setComponent(new ComponentName("com.omegamoon.sysadmin", "com.omegamoon.sysadmin.SettingsReceiver"));
            sendBroadcast(intent);

            Toast.makeText(this, "Restart your device", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please enter a valid IMEI", Toast.LENGTH_LONG).show();
        }

    }


    @Override
    protected void onPause() {
        super.onPause();


        finish();

    }
}
