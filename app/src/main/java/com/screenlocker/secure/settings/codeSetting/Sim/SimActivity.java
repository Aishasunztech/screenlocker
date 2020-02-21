package com.screenlocker.secure.settings.codeSetting.Sim;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.secure.launcher.R;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.room.SimEntry;
import com.screenlocker.secure.settings.codeSetting.CodeSettingActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.screenlocker.secure.utils.AppConstants.ALLOW_ENCRYPTED_ALL;
import static com.screenlocker.secure.utils.AppConstants.ALLOW_GUEST_ALL;
import static com.screenlocker.secure.utils.AppConstants.BROADCAST_APPS_ACTION;
import static com.screenlocker.secure.utils.AppConstants.DELETED_ICCIDS;
import static com.screenlocker.secure.utils.AppConstants.KEY_DATABASE_CHANGE;
import static com.screenlocker.secure.utils.AppConstants.KEY_ENABLE;
import static com.screenlocker.secure.utils.AppConstants.KEY_ENCRYPTED;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.KEY_MAIN_PASSWORD;
import static com.screenlocker.secure.utils.AppConstants.SIM_0_ICCID;
import static com.screenlocker.secure.utils.AppConstants.SIM_1_ICCID;
import static com.screenlocker.secure.utils.AppConstants.SIM_UNREGISTER_FLAG;
import static com.screenlocker.secure.utils.AppConstants.UNSYNC_ICCIDS;

public class SimActivity extends BaseActivity implements AddSimDialog.OnRegisterSimListener, SimAdapter.OnSimPermissionChangeListener {



    private Switch allowGuest, allowEncrypted, allowRegisterAllGuest, allowRegisterAllEncrypted;
    private SimViewModel viewModel;
    private List<SimEntry> entries;
    private SimAdapter adapter;
    private SubscriptionInfo infoSim1, infoSim2;
    private boolean isFirstRegister = false, isSecondRegister = false;
    private String iccid0, iccid1;
    private FragmentManager fragmentManager;
    private boolean isChanged = false;


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
            prefUtils.saveStringPref( SIM_0_ICCID, infoSim1.getIccId());
        }
        if (infoSim2 != null) {
            prefUtils.saveStringPref( SIM_1_ICCID, infoSim2.getIccId());
        }
        iccid0 = prefUtils.getStringPref( SIM_0_ICCID);
        iccid1 = prefUtils.getStringPref( SIM_1_ICCID);


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    private void setRecyclerView() {
        entries = new ArrayList<>();
        allowGuest = findViewById(R.id.allowAllGuest);
        allowEncrypted = findViewById(R.id.allowAllEncrypted);
        allowRegisterAllGuest = findViewById(R.id.allowRegisterAllGuest);
        allowRegisterAllEncrypted = findViewById(R.id.allowRegisterAllEncrypted);
        allowRegisterAllGuest.setOnClickListener(v -> {
            Switch s = (Switch) v;
            Set<String> set = prefUtils.getStringSet( UNSYNC_ICCIDS);
            if (set == null)
                set = new HashSet<>();

            if (s.isChecked()) {
                for (SimEntry entry : entries) {
                    entry.setGuest(true);
                    viewModel.updateSimEntry(entry);
                    isChanged = true;
                    set.add(entry.getIccid());
                }
            } else {
                for (SimEntry entry : entries) {
                    entry.setGuest(false);
                    viewModel.updateSimEntry(entry);
                    isChanged = true;
                    set.add(entry.getIccid());
                }
            }
            prefUtils.saveStringSetPref( UNSYNC_ICCIDS, set);
        });
        allowRegisterAllEncrypted.setOnClickListener((view) -> {
            Switch a = (Switch) view;
            Set<String> set = prefUtils.getStringSet( UNSYNC_ICCIDS);
            if (set == null)
                set = new HashSet<>();
            if (a.isChecked()) {
                for (SimEntry entry : entries) {
                    entry.setEncrypted(true);
                    viewModel.updateSimEntry(entry);
                    isChanged = true;
                    set.add(entry.getIccid());
                }
            } else {
                for (SimEntry entry : entries) {
                    entry.setEncrypted(false);
                    viewModel.updateSimEntry(entry);
                    isChanged = true;
                    set.add(entry.getIccid());
                }
            }
            prefUtils.saveStringSetPref( UNSYNC_ICCIDS, set);
        });
        RecyclerView rvSim = findViewById(R.id.rvSim);
        rvSim.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SimAdapter(this, entries, this);
        rvSim.setAdapter(adapter);
        if (prefUtils.getBooleanPrefWithDefTrue( ALLOW_GUEST_ALL)) {
            allowGuest.setChecked(true);
        } else
            allowGuest.setChecked(false);
        if (prefUtils.getBooleanPrefWithDefTrue( ALLOW_ENCRYPTED_ALL)) {
            allowEncrypted.setChecked(true);
        } else
            allowEncrypted.setChecked(false);
        allowGuest.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefUtils.saveBooleanPref( ALLOW_GUEST_ALL, isChecked);
            isChanged = true;
            prefUtils.saveBooleanPref( SIM_UNREGISTER_FLAG, true);
        });
        allowEncrypted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefUtils.saveBooleanPref( ALLOW_ENCRYPTED_ALL, isChecked);
            isChanged = true;
            prefUtils.saveBooleanPref( SIM_UNREGISTER_FLAG, true);
        });
        findViewById(R.id.btnadd).setOnClickListener(v -> {
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
    }

    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.sim);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SimViewModel.class);
        viewModel.getAllSimEntries().observe(this, simEntries -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                entries.clear();
                boolean guestAll = true, encryptedAll = true;
                for (SimEntry simEntry : simEntries) {
                    if (!simEntry.isGuest()) {
                        guestAll = false;
                    }
                    if (!simEntry.isEncrypted()) {
                        encryptedAll = false;
                    }
                    if (simEntry.getIccid().equals(iccid0)) {
                        boolean update = false;
                        isFirstRegister = true;
                        if (simEntry.getSlotNo() != 0) {
                            update = true;
                            simEntry.setSlotNo(0);
                        }
                        if (simEntry.isGuest() || simEntry.isEncrypted()) {
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
                        if (simEntry.isGuest() || simEntry.isEncrypted()) {
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
                        if (simEntry.getSlotNo() != -1) {
                            simEntry.setSlotNo(-1);
                            viewModel.updateSimEntry(simEntry);
                        }
                    }
                    entries.add(simEntry);
                }
                allowRegisterAllGuest.setChecked(guestAll);
                allowRegisterAllEncrypted.setChecked(encryptedAll);
            }
            adapter.notifyDataSetChanged();

        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void onSimRegistered(SubscriptionInfo info, String note) {
        SimEntry entry = new SimEntry(info.getIccId(), info.getCarrierName().toString(), note, info.getSimSlotIndex(), true, true, true, "Active");
        viewModel.insertSimEntry(entry);
        isChanged = true;
        Set<String> set = prefUtils.getStringSet( UNSYNC_ICCIDS);
        if (set == null)
            set = new HashSet<>();
        set.add(entry.getIccid());
        prefUtils.saveStringSetPref( UNSYNC_ICCIDS, set);
        fragmentManager.popBackStack();
    }

    @Override
    public void onManualInsert(SimEntry entry) {
        viewModel.insertSimEntry(entry);
        isChanged = true;
        Set<String> set = prefUtils.getStringSet( UNSYNC_ICCIDS);
        if (set == null)
            set = new HashSet<>();
        set.add(entry.getIccid());
        prefUtils.saveStringSetPref( UNSYNC_ICCIDS, set);
        fragmentManager.popBackStack();
    }


    @Override
    public void onSimPermissionChange(SimEntry entry, String type, boolean isChecked) {


        switch (type) {
            case KEY_GUEST:
                entry.setGuest(isChecked);
                if (prefUtils.getStringPref( AppConstants.CURRENT_KEY).equals(KEY_GUEST_PASSWORD)) {
                    if (entry.getStatus().equals(getResources().getString(R.string.status_active)) ||
                            entry.getStatus().equals(getResources().getString(R.string.status_disabled)))
                        broadCastIntent(isChecked, entry.getSlotNo());
                }
                break;
            case KEY_ENCRYPTED:
                entry.setEncrypted(isChecked);
                if (prefUtils.getStringPref( AppConstants.CURRENT_KEY).equals(KEY_MAIN_PASSWORD)) {
                    if (entry.getStatus().equals(getResources().getString(R.string.status_active)) ||
                            entry.getStatus().equals(getResources().getString(R.string.status_disabled)))
                        broadCastIntent(isChecked, entry.getSlotNo());
                }
                break;
            case KEY_ENABLE:
                break;
        }
        viewModel.updateSimEntry(entry);
        isChanged = true;
        Set<String> set = prefUtils.getStringSet( UNSYNC_ICCIDS);
        if (set == null)
            set = new HashSet<>();
        set.add(entry.getIccid());
        prefUtils.saveStringSetPref( UNSYNC_ICCIDS, set);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onDeleteEntry(SimEntry entry) {
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.delete_title))
                .setIcon(android.R.drawable.ic_delete)
                .setMessage(getResources().getString(R.string.want_to_delete, entry.getIccid()))
                .setPositiveButton(getResources().getString(R.string.delete_title), (dialog, which) -> {
                    isChanged = true;
                    Set<String> set = prefUtils.getStringSet( DELETED_ICCIDS);
                    if (set == null)
                        set = new HashSet<>();
                    set.add(entry.getIccid());
                    prefUtils.saveStringSetPref( DELETED_ICCIDS, set);
                    viewModel.deleteSimEntry(entry);
                })
                .setNegativeButton(getResources().getString(R.string.cancel_text), (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();

    }

    @Override
    public void onUpdateEntry(SimEntry entry) {
        viewModel.updateSimEntry(entry);
//        TODO: mark as un sync
        isChanged = true;
        Set<String> set = prefUtils.getStringSet( UNSYNC_ICCIDS);
        if (set == null)
            set = new HashSet<>();
        set.add(entry.getIccid());
        prefUtils.saveStringSetPref( UNSYNC_ICCIDS, set);

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

    @Override
    protected void onStop() {
        if (isChanged) {
            Intent intent = new Intent(BROADCAST_APPS_ACTION);
            intent.putExtra(KEY_DATABASE_CHANGE, "simSettings");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
        try {
            fragmentManager.popBackStack();
        } catch (Exception ignored) {

        }
        super.onStop();
    }

}
