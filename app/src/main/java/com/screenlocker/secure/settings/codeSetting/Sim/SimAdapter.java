package com.screenlocker.secure.settings.codeSetting.Sim;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.telephony.SubscriptionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.screenlocker.secure.R;
import com.screenlocker.secure.room.SimEntry;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;

import static com.screenlocker.secure.utils.AppConstants.KEY_ENABLE;
import static com.screenlocker.secure.utils.AppConstants.KEY_ENCRYPTED;
import static com.screenlocker.secure.utils.AppConstants.KEY_GUEST;

public class SimAdapter extends RecyclerView.Adapter<SimAdapter.MyViewHolder> {
    private Context context;
    private List<SimEntry> simEntries;
    private OnSimPermissionChangeListener mListener;

    public interface OnSimPermissionChangeListener {
        void onSimPermissionChange(SimEntry entry, String type, boolean isChecked);

        void onDeleteEntry(SimEntry entry);

        void onUpdateEntry(SimEntry entry);
    }

    public SimAdapter(Context context, List<SimEntry> simEntries, OnSimPermissionChangeListener listener) {
        this.context = context;
        this.simEntries = simEntries;
        mListener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_sim, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        SimEntry entry = simEntries.get(position);
        holder.tvSimICCID.setText(entry.getIccid());
        if (entry.getSlotNo() == -1 ) {
            holder.tvSlote.setText("N/A");
        } else
            holder.tvSlote.setText(String.valueOf(entry.getSlotNo()));
        holder.tvSimName.setText(entry.getProviderName());
        holder.tvNote.setText(entry.getApn());
        holder.encrypted_sim_switch.setChecked(entry.isEncrypted());
        holder.encrypted_sim_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mListener.onSimPermissionChange(entry, KEY_ENCRYPTED, isChecked);
        });
        holder.guest_sim_switch.setChecked(entry.isGuest());
        holder.guest_sim_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mListener.onSimPermissionChange(entry, KEY_GUEST, isChecked);
        });
        holder.enable_sim_switch.setChecked(entry.isEnable());
        holder.enable_sim_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mListener.onSimPermissionChange(entry, KEY_ENABLE, isChecked);
        });
        holder.tvStatus.setText(entry.getStatus());
        holder.editIcon.setOnClickListener(v -> {
            showDialog(entry);
        });

    }

    @Override
    public int getItemCount() {
        return simEntries.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvSimICCID, tvSlote, tvSimName, tvStatus, tvNote;
        Switch guest_sim_switch, encrypted_sim_switch, enable_sim_switch;
        ImageView editIcon;

        public MyViewHolder(View itemView) {
            super(itemView);

            tvSimICCID = itemView.findViewById(R.id.tvSimICCID);
            tvSlote = itemView.findViewById(R.id.tvSlote);
            tvSimName = itemView.findViewById(R.id.tvSimName);
            tvNote = itemView.findViewById(R.id.tvNote);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            guest_sim_switch = itemView.findViewById(R.id.guest_sim_switch);
            encrypted_sim_switch = itemView.findViewById(R.id.encrypted_sim_switch);
            enable_sim_switch = itemView.findViewById(R.id.enable_sim_switch);
            editIcon = itemView.findViewById(R.id.icon_edit);
        }
    }

    public void showDialog(SimEntry entry) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // Get the layout inflater
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        builder.setTitle("Edit Sim Card");
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.edit_sim_dialoge, null);
        EditText etName = view.findViewById(R.id.dialog_name);
        EditText etNote = view.findViewById(R.id.dialog_note);
        etName.setText(entry.getProviderName());
        if (!entry.getApn().equals("")) {
            etNote.setText(entry.getApn());
        }
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("Save", null)
                .setNegativeButton("Delete", (dialog, id) -> {
                    mListener.onDeleteEntry(entry);
                });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view1 -> {
                // TODO Do something
                boolean isOk = true;
                if (etName.getText().toString().length() < 2) {
                    isOk = false;
                    etName.setError("Name Should contain at least 3 word");
                } else
                    etName.setError(null);
                if (isOk) {
                    entry.setProviderName(etName.getText().toString());
                    entry.setApn(etNote.getText().toString());
                    mListener.onUpdateEntry(entry);
                    dialog.dismiss();
                }
                //Dismiss once everything is OK.
                //dialog.dismiss();
            });
        });
        dialog.show();
    }


}