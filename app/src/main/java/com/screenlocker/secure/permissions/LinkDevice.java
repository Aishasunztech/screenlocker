package com.screenlocker.secure.permissions;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

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

import static com.screenlocker.secure.utils.AppConstants.DEF_PAGE_NO;
import static com.screenlocker.secure.utils.AppConstants.DEVICE_LINKED_STATUS;


/**
 * A simple {@link Fragment} subclass.
 */
public class LinkDevice extends AbstractStep {


    public LinkDevice() {
        // Required empty public constructor
    }

    @Override
    public void onNext() {
        PrefUtils.saveIntegerPref(MyApplication.getAppContext(),DEF_PAGE_NO,5);
    }

    @Override
    public void onSkip() {
        super.onSkip();
        PrefUtils.saveIntegerPref(MyApplication.getAppContext(),DEF_PAGE_NO,5);
    }

    @Override
    public boolean nextIf() {
        return PrefUtils.getBooleanPref(MyApplication.getAppContext(),DEVICE_LINKED_STATUS);
    }

    @Override
    public String error() {
        return "Please Link Device or Skip";
    }

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
                startActivity(intent);
            }
        });

    }

    @Override
    public String name() {
        return "Link Device";
    }

}
