package com.screenlocker.secure.permissions;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.screenlocker.secure.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.utils.PrefUtils;

import static android.app.Activity.RESULT_OK;
import static com.screenlocker.secure.utils.AppConstants.CODE_LAUNCHER;
import static com.screenlocker.secure.utils.AppConstants.DEF_PAGE_NO;
import static com.screenlocker.secure.utils.PermissionUtils.isMyLauncherDefault;


/**
 * A simple {@link Fragment} subclass.
 */
public class SetDefaultLauncherFragment extends AbstractStep {

    private boolean allow = false;
    public SetDefaultLauncherFragment() {
        // Required empty public constructor
    }

    @Override
    public boolean nextIf() {
        if (isMyLauncherDefault(MyApplication.getAppContext())){
            PrefUtils.saveIntegerPref(MyApplication.getAppContext(),DEF_PAGE_NO,7);
            return true;
        }
        return false;
    }



    @Override
    public boolean setSkipable() {
        return false;
    }

    @Override
    public String error() {
        return "Please Set as Default Launcher";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_default_launcher, container, false);
    }


    @BindView(R.id.set_launcher)
    Button setLauncher;
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        setLauncher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                         Intent intent = new Intent(Settings.ACTION_HOME_SETTINGS);
                        startActivityForResult(intent,CODE_LAUNCHER);
                    } else {
                         Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        startActivityForResult(intent,CODE_LAUNCHER);
                    }
                } catch (Exception ignored) {
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_LAUNCHER && isMyLauncherDefault(MyApplication.getAppContext()) ){
            setLauncher.setText("Default Launcher set");
            setLauncher.setEnabled(false);
            setLauncher.setClickable(false);
            allow = true;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public String name() {
        return
                "Set Default Launcher";
    }
}
