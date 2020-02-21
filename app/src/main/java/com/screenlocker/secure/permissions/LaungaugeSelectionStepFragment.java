package com.screenlocker.secure.permissions;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.utils.PrefUtils;
import com.secure.launcher.R;

import java.util.Locale;

import static com.screenlocker.secure.permissions.SteppersActivity.STEP_LANGUAGE;
import static com.screenlocker.secure.utils.AppConstants.DEF_PAGE_NO;

public class LaungaugeSelectionStepFragment extends ExtentedAbstractStep {
    TextView language;
    public LaungaugeSelectionStepFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public String name() {
        return MyApplication.getAppContext().getResources().getString(R.string.language);
    }
    @Override
    public boolean isSkipable() {
        return false;
    }


    @Override
    public boolean isPreviousAllow() {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_laungauge_selection_step, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        language = view.findViewById(R.id.default_launguage);
        Button change_button = view.findViewById(R.id.btn_change_language);
        change_button.setOnClickListener(v -> {
            Intent i = new Intent(Settings.ACTION_LOCALE_SETTINGS);
            startActivity(i);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        language.setText(Locale.getDefault().getDisplayName());
    }

    @Override
    public void onNext() {
        super.onNext();
        prefUtils.saveIntegerPref( DEF_PAGE_NO, STEP_LANGUAGE);
    }
}
