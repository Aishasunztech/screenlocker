package com.screenlocker.secure.permissions;

import android.content.Intent;
import android.os.Bundle;

import com.github.fcannizzaro.materialstepper.AbstractStep;
import com.github.fcannizzaro.materialstepper.style.DotStepper;
import com.github.fcannizzaro.materialstepper.style.TabStepper;
import com.screenlocker.secure.R;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.utils.PrefUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.screenlocker.secure.utils.AppConstants.TOUR_STATUS;

public class SteppersActivity extends DotStepper {
    private int i = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTitle("Permissions");

//        setPrimaryColor(R.color.colorPrimary);
//        setDarkPrimaryColor(R.color.colorPrimaryDark);


        addStep(createFragment(new PermissionStep()));
        addStep(createFragment(new GuestPassword()));
        addStep(createFragment(new EncryptedPassword()));
        addStep(createFragment(new DuressPassword()));
        addStep(createFragment(new LinkDevice()));
        addStep(createFragment(new DefaultLauncher()));
        addStep(createFragment(new Finish()));


        super.onCreate(savedInstanceState);


    }

    private AbstractStep createFragment(AbstractStep fragment) {
        Bundle b = new Bundle();
        b.putInt("position", i++);
        return fragment;
    }


    @Override
    public void onComplete(Bundle data) {
        super.onComplete(data);
        PrefUtils.saveBooleanPref(SteppersActivity.this, TOUR_STATUS, true);
        Intent intent = new Intent(SteppersActivity.this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
