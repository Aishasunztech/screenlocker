package com.screenlocker.secure.settings.codeSetting.Sim;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.MenuItem;
import android.widget.Switch;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.screenlocker.secure.R;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.room.SimEntry;
import com.screenlocker.secure.settings.codeSetting.CodeSettingActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;

import static com.screenlocker.secure.utils.AppConstants.ALLOW_ENCRYPTED_ALL;
import static com.screenlocker.secure.utils.AppConstants.ALLOW_GUEST_ALL;
import static com.screenlocker.secure.utils.AppConstants.KEY_ENABLE;
import static com.screenlocker.secure.utils.AppConstants.KEY_ENCRYPTED;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.SIM_0_ICCID;
import static com.screenlocker.secure.utils.AppConstants.SIM_1_ICCID;

public class SimActivity extends BaseActivity implements AddSimDialog.OnRegisterSimListener, SimAdapter.OnSimPermissionChangeListener {


    private boolean isBackPressed;
    private Switch allowGuest , allowEncrypted;
    private SimViewModel viewModel;
    private List<SimEntry> entries;
    private SimAdapter adapter;
    private SubscriptionInfo infoSim1, infoSim2;
    private boolean isFirstRegister = false, isSecondRegister = false;
    private String iccid0, iccid1;
    private FragmentManager fragmentManager;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sim);
        setToolbar();
        setRecyclerView();
        setupViewModel();
        SubscriptionManager sManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        infoSim1 = sManager.getActiveSubscriptionInfoForSimSlotIndex(0);
        infoSim2 = sManager.getActiveSubscriptionInfoForSimSlotIndex(1);
        if (infoSim1 != null) {
            PrefUtils.saveStringPref(this, SIM_0_ICCID, infoSim1.getIccId());
        }
        if (infoSim2 != null) {
            PrefUtils.saveStringPref(this, SIM_1_ICCID, infoSim2.getIccId());
        }
        iccid0 = PrefUtils.getStringPref(this, SIM_0_ICCID);
        iccid1 = PrefUtils.getStringPref(this, SIM_1_ICCID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isBackPressed = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isBackPressed) {

            if (CodeSettingActivity.codeSettingsInstance != null) {
                //  finish previous activity and this activity
                CodeSettingActivity.codeSettingsInstance.finish();
            }
            this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        isBackPressed = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void setRecyclerView() {
        entries = new ArrayList<>();
        allowGuest = findViewById(R.id.allowAllGuest);
        allowEncrypted = findViewById(R.id.allowAllEncrypted);
        RecyclerView rvSim = findViewById(R.id.rvSim);
        rvSim.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SimAdapter(this, entries, this);
        rvSim.setAdapter(adapter);
        findViewById(R.id.addSim).setOnClickListener(v -> {
            fragmentManager = getSupportFragmentManager();
            AddSimDialog newFragment = new AddSimDialog();
            Bundle bundle = new Bundle();
            if (!isFirstRegister && infoSim1 != null) {
                bundle.putParcelable("infoSim1", infoSim1);
            }
            if (!isSecondRegister && infoSim2 != null) {
                bundle.putParcelable("infoSim2", infoSim2);
            }
            newFragment.setArguments(bundle);
            // The device is smaller, so show the fragment fullscreen
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            // For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
            transaction.add(android.R.id.content, newFragment)
                    .addToBackStack(null).commit();
        });
        if (PrefUtils.getBooleanPrefWithDefTrue(this, ALLOW_GUEST_ALL)){
            allowGuest.setChecked(true);
        }else
            allowGuest.setChecked(false);
        if (PrefUtils.getBooleanPrefWithDefTrue(this, ALLOW_ENCRYPTED_ALL)){
            allowEncrypted.setChecked(true);
        }else
            allowEncrypted.setChecked(false);
        allowGuest.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PrefUtils.saveBooleanPref(this, ALLOW_GUEST_ALL,isChecked);
        });
        allowEncrypted.setOnCheckedChangeListener((buttonView, isChecked) ->{
            PrefUtils.saveBooleanPref(this, ALLOW_ENCRYPTED_ALL,isChecked);
        });
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.sim);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }



    private void setupViewModel() {
        viewModel = ViewModelProviders.of(this).get(SimViewModel.class);
        viewModel.getAllSimEntries().observe(this, simEntries -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                entries.clear();
                for (SimEntry simEntry : simEntries) {
                    if (simEntry.getIccid().equals(iccid0)) {
                        boolean update = false;
                        isFirstRegister = true;
                        if (simEntry.getSlotNo() != 0) {
                            update = true;
                            simEntry.setSlotNo(0);
                        }
                        if (simEntry.isEnable()) {
                            if (!simEntry.getStatus().equals(getResources().getString(R.string.status_active))) {
                                update = true;
                                simEntry.setStatus(getResources().getString(R.string.status_active));
                            }
                        } else if (!simEntry.getStatus().equals(getResources().getString(R.string.status_disabled))) {
                            update = true;
                            simEntry.setStatus(getResources().getString(R.string.status_disabled));
                        }
                        if (update) {
                            viewModel.updateSimEntry(simEntry);
                        }

                    } else if (simEntry.getIccid().equals(iccid1)) {
                        boolean update = false;
                        isSecondRegister = true;
                        if (simEntry.getSlotNo() != 1) {
                            update = true;
                            simEntry.setSlotNo(1);
                        }
                        if (simEntry.isEnable()) {
                            if (!simEntry.getStatus().equals(getResources().getString(R.string.status_active))) {
                                update = true;
                                simEntry.setStatus(getResources().getString(R.string.status_active));
                            }
                        } else if (!simEntry.getStatus().equals(getResources().getString(R.string.status_disabled))) {
                            update = true;
                            simEntry.setStatus(getResources().getString(R.string.status_disabled));
                        }
                        if (update) {
                            viewModel.updateSimEntry(simEntry);
                        }
                    } else {
                        if (!simEntry.getStatus().equals(getResources().getString(R.string.status_not_inserted))) {
                            simEntry.setStatus(getResources().getString(R.string.status_not_inserted));
                            viewModel.updateSimEntry(simEntry);
                        }
                    }
                    entries.add(simEntry);
                }
            }
            adapter.notifyDataSetChanged();

        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void onSimRegistered(SubscriptionInfo info) {
        SimEntry entry = new SimEntry(info.getIccId(), info.getCarrierName().toString(), "", info.getSimSlotIndex(), true, true, true, "Active");
        viewModel.insertSimEntry(entry);
        fragmentManager.popBackStack();
    }

    @Override
    public void onManualInsert(SimEntry sm) {
        viewModel.insertSimEntry(sm);
        fragmentManager.popBackStack();
    }


    @Override
    public void onSimPermissionChange(SimEntry entry, String type, boolean isChecked) {
        switch (type) {
            case KEY_GUEST:
                entry.setGuest(isChecked);
                if (PrefUtils.getStringPref(this, AppConstants.CURRENT_KEY).equals(KEY_GUEST_PASSWORD)) {
                    if (entry.getStatus().equals(getResources().getString(R.string.status_active)) ||
                            entry.getStatus().equals(getResources().getString(R.string.status_disabled)))
                        broadCastIntent(isChecked, entry.getSlotNo());
                }
                break;
            case KEY_ENCRYPTED:
                entry.setEncrypted(isChecked);
                if (PrefUtils.getStringPref(this, AppConstants.CURRENT_KEY).equals(KEY_MAIN_PASSWORD)) {
                    if (entry.getStatus().equals(getResources().getString(R.string.status_active)) ||
                            entry.getStatus().equals(getResources().getString(R.string.status_disabled)))
                        broadCastIntent(isChecked, entry.getSlotNo());
                }
                break;
            case KEY_ENABLE:
                entry.setEncrypted(isChecked);
                entry.setGuest(isChecked);
                entry.setEnable(isChecked);
                if (entry.getStatus().equals(getResources().getString(R.string.status_active)) ||
                        entry.getStatus().equals(getResources().getString(R.string.status_disabled)))
                    broadCastIntent(isChecked, entry.getSlotNo());
                break;
        }
        viewModel.updateSimEntry(entry);
    }

    void broadCastIntent(boolean enabled, int slot) {
        Intent intent = new Intent("com.secure.systemcontrol.SYSTEM_SETTINGS");
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.putExtra("isEnabled", enabled);
        intent.putExtra("slot", slot);
        intent.setComponent(new ComponentName("com.secure.systemcontrol", "com.secure.systemcontrol.receivers.SettingsReceiver"));
        sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.getAllSimEntries().removeObservers(this);
        viewModel = null;
    }
}
