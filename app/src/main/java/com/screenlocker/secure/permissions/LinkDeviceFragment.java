package com.screenlocker.secure.permissions;


import android.content.Context;
import android.content.Intent;
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
    public static final int REQUEST_LINK_DEVICE = 5;

    private PageUpdate pageUpdate;

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

        linkDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivityForResult(intent, REQUEST_LINK_DEVICE);
            }
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
}
