package com.screenlocker.secure.permissions;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.screenlocker.secure.utils.AppConstants.DURESS_PASSORD_OPTION;
import static com.screenlocker.secure.utils.AppConstants.OPTION_PATTERN;
import static com.screenlocker.secure.utils.AppConstants.OPTION_PIN;
import static com.screenlocker.secure.utils.Utils.hideKeyboard;

/**
 * @author Muhammad Nadeem
 * @Date 8/21/2019.
 */
public class DuressPasswordOptionFragment extends AbstractStep {


    public DuressPasswordOptionFragment() {
        // Required empty public constructor
    }

    private boolean isSelected = false;
    private OnPageUpdateListener mListener;
    private AlertDialog builder;
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
        builder = new AlertDialog.Builder(getContext()).
                setTitle(getResources().getString(R.string.warning))
                .setMessage(getResources().getString(R.string.duress_pin_message))
                .setPositiveButton(getResources().getString(R.string.ok_text), (dialogInterface, i) -> {
                    dialogInterface.cancel();
                })

                .setNegativeButton(getResources().getString(R.string.cancel_text), (dialogInterface, i) -> {
//                        dialogInterface.cancel();
                    dialogInterface.dismiss();

                }).create();
        builder.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        tittleIcon.setImageResource(R.drawable.ic_duress_icon);
        titleText.setText("DURESS LOCK");


        layout_pin.setOnClickListener(v -> {
            PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DURESS_PASSORD_OPTION, OPTION_PIN);
            isSelected = true;
            mListener.onPageUpdate(6);


        });
        layout_pattern.setOnClickListener(v -> {
            PrefUtils.saveIntegerPref(MyApplication.getAppContext(), DURESS_PASSORD_OPTION, OPTION_PATTERN);
            isSelected = true;
            mListener.onPageUpdate(6);


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
        return true;
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
        try {
            hideKeyboard(Objects.requireNonNull(getActivity()));

            if (PrefUtils.getStringPref(MyApplication.getAppContext(), AppConstants.KEY_DURESS_PASSWORD) == null) {

                builder.show();
            }
        } catch (Exception ignored) {
        }
    }
}
