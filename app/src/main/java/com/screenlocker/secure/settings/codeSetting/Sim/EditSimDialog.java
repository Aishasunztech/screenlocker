package com.screenlocker.secure.settings.codeSetting.Sim;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import com.secure.launcher.R;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

public class EditSimDialog extends Dialog {
    private ConstraintLayout container;

    public EditSimDialog(@NonNull Context context) {
        super(context);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_edit_sim_settings);

        container = findViewById(R.id.edit_sim_container);
//        container.setMinWidth(Resources.getSystem().getDisplayMetrics().widthPixels);
    }
}
