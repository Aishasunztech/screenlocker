package com.screenlocker.secure.settings.codeSetting.Sim;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.santalu.maskedittext.MaskEditText;
import com.secure.launcher.R;
import com.screenlocker.secure.room.SimEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Muhammad Nadeem
 * @Date 5/24/2019.
 */
public class AddSimDialog extends Fragment {
    private OnRegisterSimListener mListener;

    public interface OnRegisterSimListener {
        void onSimRegistered(SubscriptionInfo info, String note);

        void onManualInsert(SimEntry sm);
    }

    @BindView(R.id.container1)
    LinearLayout layout1;
    @BindView(R.id.container2)
    LinearLayout layout2;
    @BindView(R.id.registerSim1)
    Button btnRegister1;
    @BindView(R.id.registerButton2)
    Button btnRegister2;
    @BindView(R.id.tvSlote1)
    TextView slot1;
    @BindView(R.id.tvSimICCID1)
    TextView iccid1;
    @BindView(R.id.tvSimName1)
    TextView simName1;
    @BindView(R.id.tvSlote2)
    TextView slot2;
    @BindView(R.id.tvSimICCID2)
    TextView iccid2;
    @BindView(R.id.tvSimName2)
    TextView simName2;
    @BindView(R.id.manualICCID)
    MaskEditText manualIccid;
    @BindView(R.id.manualName)
    EditText manualName;
    @BindView(R.id.manualEncrypted)
    Switch manualEncrypted;
    @BindView(R.id.manualGuest)
    Switch manualGuest;
    @BindView(R.id.btnManualRegister)
    Button btnManualRegister;
    @BindView(R.id.manualNote)
    EditText etNoteManual;
    @BindView(R.id.note1_input_layout)
    TextInputLayout note_input_layout;
    @BindView(R.id.etNote1)
    TextInputEditText etnote1;
    @BindView(R.id.note2_input_layout)
    TextInputLayout note2_input_layout;
    @BindView(R.id.etNote2)
    TextInputEditText etNote2;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        View v = inflater.inflate(R.layout.add_sim_fragment, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        assert bundle != null;
        SubscriptionInfo infoSim1 = bundle.getParcelable("infoSim1");
        SubscriptionInfo infoSim2 = bundle.getParcelable("infoSim2");
        if (infoSim1 != null) {
            layout1.setVisibility(View.VISIBLE);
            iccid1.setText(infoSim1.getIccId());
            simName1.setText(infoSim1.getCarrierName());
            slot1.setText(String.valueOf(infoSim1.getSimSlotIndex()));

        }
        note_input_layout.setHint("Note");
        note2_input_layout.setHint("Note");

        btnRegister1.setOnClickListener(v -> {
            etnote1 = view.findViewById(R.id.etNote1);
            mListener.onSimRegistered(infoSim1, etnote1.getText().toString());
        });
        btnRegister2.setOnClickListener(v -> {
            etNote2 = view.findViewById(R.id.etNote2);
            mListener.onSimRegistered(infoSim2, etNote2.getText().toString());
        });
        if (infoSim2 != null) {
            layout2.setVisibility(View.VISIBLE);
            iccid2.setText(infoSim2.getIccId());
            simName2.setText(infoSim2.getCarrierName());
            slot2.setText(String.valueOf(infoSim2.getSimSlotIndex()));

        }
        btnManualRegister.setOnClickListener(v -> {
            String iccid = manualIccid.getRawText().trim();
            String name = manualName.getText().toString().trim();
            boolean isError = false;
            if (iccid.length() != 20) {
                manualIccid.setError(getResources().getString(R.string.enter_valid_ICCID));
                isError = true;
            } else {
                manualIccid.setError(null);
            }
            if (name.length() < 2) {
                manualName.setError(getResources().getString(R.string.enter_a_valid_name));
                isError = true;
            } else {
                manualName.setError(null);
            }
            if (!isError) {
                SimEntry se = new SimEntry(iccid, name, etNoteManual.getText().toString(), -1, manualGuest.isChecked(), manualEncrypted.isChecked(), false, getResources().getString(R.string.status_not_inserted));
                mListener.onManualInsert(se);
            }

        });


    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (OnRegisterSimListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    String getApn(int mcc) {
        final Uri APN_TABLE_URI = Uri.parse("content://telephony/carriers");

        //path to preffered APNs
        final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
        //receiving cursor to preffered APN table
        Cursor c = getContext().getContentResolver().query(PREFERRED_APN_URI, null, "mcc='" + mcc + "'", null, null);

        //moving the cursor to beggining of the table
        c.moveToFirst();

        //now the cursor points to the first preffered APN and we can get some
        //information about it
        //for example first preffered APN id

        //we can get APN name by the same way
        int index = c.getColumnIndex("name");
        return c.getString(index);
    }
}
