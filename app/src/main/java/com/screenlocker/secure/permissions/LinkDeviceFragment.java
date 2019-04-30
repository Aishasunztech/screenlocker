package com.screenlocker.secure.permissions;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
        PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 6);
    }

    //next only if device is linked other wise skip
    @Override
    public boolean nextIf() {


        if (PrefUtils.getBooleanPref(MyApplication.getAppContext(), DEVICE_LINKED_STATUS)) {
            PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 6);
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

        linkDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivityForResult(intent, REQUEST_LINK_DEVICE);
            }
        });
        dialogh = new AlertDialog.Builder(getContext())
                .setTitle("Network Not Connected!")
                .setMessage("You are not connected to internet. Please go to Previous page or Skip and continue offline mode.")
                .setNegativeButton("PREVOIUS", (dialog, which) -> {
                    pageUpdate.onPageUpdate(4);
                    PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DEF_PAGE_NO, 4);
                }).setPositiveButton("SKIP", ((dialog, which) -> {
                    pageUpdate.onPageUpdate(6);
                    onSkip();
                })).setCancelable(false);

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
                pageUpdate.onPageUpdate(6);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStepVisible() {
        super.onStepVisible();
        ConnectivityManager cm =
                (ConnectivityManager) MyApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        if (!isConnected) {
            if (dialogh != null)
                dialogh.show();

        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}
