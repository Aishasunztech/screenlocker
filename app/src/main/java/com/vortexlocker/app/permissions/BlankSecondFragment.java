package com.vortexlocker.app.permissions;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vortexlocker.app.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class BlankSecondFragment extends Fragment {


    public BlankSecondFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank_second, container, false);
    }

}