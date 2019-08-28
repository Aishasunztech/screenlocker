package com.screenlocker.secure.permissions;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.screenlocker.secure.utils.AppConstants.ENCRYPT_PASSORD_OPTION;
import static com.screenlocker.secure.utils.AppConstants.OPTION_PATTERN;
import static com.screenlocker.secure.utils.AppConstants.OPTION_PIN;
import static com.screenlocker.secure.utils.Utils.hideKeyboard;

/**
 * A simple {@link Fragment} subclass.
 */
public class EncryptPasswordOptionsFragment extends AbstractStep {


    public EncryptPasswordOptionsFragment() {
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
        tittleIcon.setImageResource(R.drawable.ic_encrypted_third);
        titleText.setText("ENCRYPT SPACE LOCK");


        layout_pin.setOnClickListener(v -> {
            PrefUtils.saveIntegerPref(MyApplication.getAppContext(), ENCRYPT_PASSORD_OPTION, OPTION_PIN);
            isSelected = true;
            mListener.onPageUpdate(4);


        });
        layout_pattern.setOnClickListener(v -> {
            PrefUtils.saveIntegerPref(MyApplication.getAppContext(), ENCRYPT_PASSORD_OPTION, OPTION_PATTERN);
            isSelected = true;
            mListener.onPageUpdate(4);



        });
        layout_combination.setOnClickListener(v -> {
            Toast.makeText(MyApplication.getAppContext(), "Coming Soon", Toast.LENGTH_SHORT).show();
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
    public String error() {
        return "Please Select An Option";
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
        hideKeyboard(Objects.requireNonNull(getActivity()));
    }
}
