package com.screenlocker.secure.settings.codeSetting.Sim;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.screenlocker.secure.R;
import com.screenlocker.secure.room.SimEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Muhammad Nadeem
 * @Date 5/24/2019.
 */
public class AddSimDialog extends DialogFragment {
    private OnRegisterSimListener mListener;

    public interface OnRegisterSimListener {
        void onSimRegistered(SubscriptionInfo info);
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
    EditText manualIccid;
    @BindView(R.id.manualName)
    EditText manualName;
    @BindView(R.id.manualEnable)
    Switch manualEnable;
    @BindView(R.id.manualEncrypted)
    Switch manualEncrypted;
    @BindView(R.id.manualGuest)
    Switch manualGuest;
    @BindView(R.id.btnManualRegister)
    Button btnManualRegister;

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
        btnRegister1.setOnClickListener(v -> {
            mListener.onSimRegistered(infoSim1);
        });
        btnRegister2.setOnClickListener(v -> {
            mListener.onSimRegistered(infoSim2);
        });
        if (infoSim2 != null) {
            layout2.setVisibility(View.VISIBLE);
            iccid2.setText(infoSim2.getIccId());
            simName2.setText(infoSim2.getCarrierName());
            slot2.setText(String.valueOf(infoSim2.getSimSlotIndex()));

        }
        btnManualRegister.setOnClickListener(v -> {
            String iccid = manualIccid.getText().toString().trim();
            String name = manualName.getText().toString().trim();
            boolean isError = false;
            if (iccid.length() != 20) {
                manualIccid.setError("Please Enter a valid ICCID");
                isError = true;
            } else {
                manualIccid.setError(null);
            }
            if (name.length() < 2) {
                manualName.setError("Please Enter a valid Name");
                isError = true;
            }
            else {
                manualName.setError(null);
            }
            if (!isError){
                SimEntry se = new SimEntry(iccid,name,"",1,manualGuest.isChecked(),manualEncrypted.isChecked(),manualEnable.isChecked(),getResources().getString(R.string.status_not_inserted));
                mListener.onManualInsert(se);
            }

    });


}

    /**
     * The system calls this only when creating the layout in a dialog.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
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
