package com.screenlocker.secure.permissions;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.secure.launcher.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.utils.PrefUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.screenlocker.secure.permissions.SteppersActivity.STEP_DEFAULT_LAUNCHER;
import static com.screenlocker.secure.utils.AppConstants.CODE_LAUNCHER;
import static com.screenlocker.secure.utils.AppConstants.DEF_PAGE_NO;
import static com.screenlocker.secure.utils.PermissionUtils.isMyLauncherDefault;


/**
 * A simple {@link Fragment} subclass.
 */
public class SetDefaultLauncherFragment extends ExtentedAbstractStep {


    OnPageUpdateListener mListener;
    private Context mContext;

    public SetDefaultLauncherFragment() {
        // Required empty public constructor
    }


    @Override
    public boolean nextIf() {
        if (isMyLauncherDefault(MyApplication.getAppContext())) {
            prefUtils.saveIntegerPref( DEF_PAGE_NO, STEP_DEFAULT_LAUNCHER);
            return true;
        }
        return false;
    }

    @Override
    public void onStepVisible() {
        super.onStepVisible();
        if (isMyLauncherDefault(MyApplication.getAppContext())) {
            prefUtils.saveIntegerPref( DEF_PAGE_NO, STEP_DEFAULT_LAUNCHER);
            if (mListener != null) {
                mListener.onPageUpdate(9);
            }
        }
    }

    @Override
    public boolean isSkipable() {
        return false;
    }

    @Override
    public String error() {
        return getResources().getString(R.string.set_as_default_launcher);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_default_launcher, container, false);
        CardView card = view.findViewById(R.id.default_card);
        card.setBackgroundResource(R.drawable.black_circle);

        // Inflate the layout for this fragment
        return view;
    }


    @BindView(R.id.set_launcher)
    Button setLauncher;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        if (isMyLauncherDefault(MyApplication.getAppContext())) {
            setLauncher.setEnabled(false);
        }
        setLauncher.setOnClickListener(v -> {
            try {
                {
                    Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
                    startActivityForResult(intent, CODE_LAUNCHER);
                }
            } catch (Exception ignored) {
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_LAUNCHER && isMyLauncherDefault(MyApplication.getAppContext())) {
            setLauncher.setText(getResources().getString(R.string.default_launcher_set));
            setLauncher.setEnabled(false);
            setLauncher.setClickable(false);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public String name() {
        return MyApplication.getAppContext().getResources().getString(R.string.set_default_launcher);
    }

    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (OnPageUpdateListener) context;
        } catch (Exception ignored) {

        }
        mContext = context;
    }
}
