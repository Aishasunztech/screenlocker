package com.screenlocker.secure.permissions;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.solver.widgets.ConstraintAnchor;
import androidx.fragment.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class FinishFragment extends AbstractStep {

    private Context mContext;
    public FinishFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_finish, container, false);;
                CardView card = view.findViewById(R.id.complete_card);
        card.setBackgroundResource(R.drawable.black_circle);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public String name() {
        return MyApplication.getAppContext().getResources().getString(R.string.launch_application);
    }

}
