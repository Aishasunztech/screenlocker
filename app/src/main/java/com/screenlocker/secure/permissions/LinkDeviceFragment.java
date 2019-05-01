package com.screenlocker.secure.permissions;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.mdm.MainActivity;
import com.screenlocker.secure.utils.PrefUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static com.screenlocker.secure.utils.AppConstants.DEF_PAGE_NO;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;


/**
 * A simple {@link Fragment} subclass.
 */
public class LinkDeviceFragment extends AbstractStep {
    public static final int REQUEST_LINK_DEVICE = 7;

    private PageUpdate pageUpdate;
    private AlertDialog.Builder dialogh;

    public interface PageUpdate {
        void onPageUpdate(int pageNo);
    }

    public LinkDeviceFragment() {
        // Required empty public constructor
    }
    //save the status of this step as completed


    @Override
    public void onSkip() {
        super.onSkip();
        //save the status of this step as completed
        PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 5);
    }

    //next only if device is linked other wise skip
    @Override
    public boolean nextIf() {


        if (PrefUtils.getBooleanPref(MyApplication.getAppContext(), DEVICE_LINKED_STATUS)) {
            PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 5);
            return true;
        }
        return false;
    }

    //if user try to next without linking device
    @Override
    public String error() {
        return "Please Link Device or Skip";
    }

    // user can skip the current step
    @Override
    public boolean setSkipable() {
        return true;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_link_device, container, false);
    }

    @BindView(R.id.link_device)
    Button linkDevice;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        linkDevice.setOnClickListener(v -> {
            if (checkNetwork()) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivityForResult(intent, REQUEST_LINK_DEVICE);
            }else
                showNetworkDialog(getContext());
        });

    }


    @Override
    public String name() {
        return "Link Device";
    }

    @Override
    public void onAttach(@NonNull Context context) {
        try {
            pageUpdate = (PageUpdate) context;
        } catch (Exception ignored) {

        }
        super.onAttach(context);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_LINK_DEVICE && resultCode == RESULT_OK) {
            if (PrefUtils.getBooleanPref(MyApplication.getAppContext(), DEVICE_LINKED_STATUS)) {
                /**
                 * @param 5 is launcher fragment page no
                 */
                pageUpdate.onPageUpdate(5);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStepVisible() {
        super.onStepVisible();


    }

    @Override
    public void onResume() {
        super.onResume();

    }
    private void showNetworkDialog( Context context) {

        androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(context).create();
        alertDialog.setTitle("Network Not Connected!");
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);

        alertDialog.setMessage("Please connect to the internet before proceeding.");

        alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, "WIFI", (dialog, which) -> {
            Intent wifiIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            startActivity(wifiIntent);
        });


        alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE, "MOBILE DATA",
                (dialog, which) -> {
                    Intent intent = new Intent("com.android.settings.sim.SIM_SUB_INFO_SETTINGS");
                    startActivity(intent);
                });
        alertDialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL, "CANCEL",
                (dialog, which) -> dialog.dismiss());
        alertDialog.show();

    }
    private boolean checkNetwork(){
        ConnectivityManager cm =
                (ConnectivityManager) MyApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return  activeNetwork != null &&
                activeNetwork.isConnected();
    }
}
