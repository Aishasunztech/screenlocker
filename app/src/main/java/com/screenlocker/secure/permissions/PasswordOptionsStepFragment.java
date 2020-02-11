package com.screenlocker.secure.permissions;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.secure.launcher.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.utils.PrefUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.screenlocker.secure.permissions.SteppersActivity.STEP_SETUP_GUEST;
import static com.screenlocker.secure.utils.AppConstants.GUEST_PASSORD_OPTION;
import static com.screenlocker.secure.utils.AppConstants.OPTION_COMBO;
import static com.screenlocker.secure.utils.AppConstants.OPTION_PATTERN;
import static com.screenlocker.secure.utils.AppConstants.OPTION_PIN;


/**
 * A simple {@link Fragment} subclass.
 */
public class PasswordOptionsStepFragment extends AbstractStep {


    public PasswordOptionsStepFragment() {
        // Required empty public constructor
    }

    private boolean isSelected = false;
    private OnPageUpdateListener mListener;

    @BindView(R.id.imageIcon)
    ImageView tittleIcon;
    @BindView(R.id.typeTitle)
    TextView titleText;
    @BindView(R.id.layout_pin)
    LinearLayout layout_pin;
    @BindView(R.id.layout_pattern)
    LinearLayout layout_pattern;
    @BindView(R.id.layout_combination)
    LinearLayout layout_combination;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_paasord_options_step, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        tittleIcon.setImageResource(R.drawable.ic_guest_icon);
        titleText.setText(MyApplication.getAppContext().getResources().getString(R.string.guest_space_lock));


        layout_pin.setOnClickListener(v -> {
            PrefUtils.saveIntegerPref(MyApplication.getAppContext(), GUEST_PASSORD_OPTION, OPTION_PIN);
            isSelected = true;
            //update to guest password
            mListener.onPageUpdate(STEP_SETUP_GUEST);


        });
        layout_pattern.setOnClickListener(v -> {
            PrefUtils.saveIntegerPref(MyApplication.getAppContext(), GUEST_PASSORD_OPTION, OPTION_PATTERN);
            isSelected = true;
            //update to guest password
            mListener.onPageUpdate(STEP_SETUP_GUEST);

        });
        layout_combination.setOnClickListener(v -> {
            PrefUtils.saveIntegerPref(MyApplication.getAppContext(), GUEST_PASSORD_OPTION, OPTION_COMBO);
            isSelected = true;
            //update to guest password
            mListener.onPageUpdate(STEP_SETUP_GUEST);
        });
    }

    @Override
    public String name() {
        return MyApplication.getAppContext().getResources().getString(R.string.chose_password_option);
    }

    @Override
    public boolean isSkipable() {
        return false;
    }

    @Override
    public boolean isPreviousAllow() {
        return true;
    }

    @Override
    public String error() {
        return MyApplication.getAppContext().getResources().getString(R.string.please_select_an_option);
    }

    @Override
    public boolean nextIf() {
        return isSelected;
    }

    @Override
    public void onNext() {
        super.onNext();
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (OnPageUpdateListener) context;
        } catch (Exception ignored) {

        }
    }

    @Override
    public void onStepVisible() {
        super.onStepVisible();
    }
}
