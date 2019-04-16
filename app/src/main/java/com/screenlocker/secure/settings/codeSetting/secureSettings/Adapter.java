package com.screenlocker.secure.settings.codeSetting.secureSettings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.screenlocker.secure.R;
import com.screenlocker.secure.room.SubExtension;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private final List<SubExtension> subExtensionList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tv;
        final ImageView img;
        final Switch guestSwitch, encryptedSwitch, enabledSwitch;

        ViewHolder(final View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.appName);
            img = itemView.findViewById(R.id.appImage);
            guestSwitch = itemView.findViewById(R.id.guestSwitch);
            encryptedSwitch = itemView.findViewById(R.id.encryptedSwitch);
            enabledSwitch = itemView.findViewById(R.id.enabledSwitch);
            enabledSwitch.setVisibility(View.GONE);

            guestSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    subExtensionList.get(getAdapterPosition()).setGuest(isChecked);
                }
            });

            encryptedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    subExtensionList.get(getAdapterPosition()).setEncrypted(isChecked);
                }
            });

        }

    }

    public Adapter(List<SubExtension> subExtensions) {

        this.subExtensionList = subExtensions;
    }


    @Override
    public void onBindViewHolder(@NonNull Adapter.ViewHolder vh, int i) {

        //Here we use the information in the list we created to define the views

        SubExtension info = subExtensionList.get(i);
        vh.tv.setText(info.getLabel());

        // vh.img.setImageDrawable(info.getIcon());
        Glide.with(vh.img.getContext())
                .load(subExtensionList.get(i).getIcon())
                .apply(new RequestOptions().centerCrop())
                .into(vh.img);
        vh.guestSwitch.setChecked(info.isGuest());
        vh.encryptedSwitch.setChecked(info.isEncrypted());

        vh.enabledSwitch.setVisibility(View.VISIBLE);
        vh.encryptedSwitch.setVisibility(View.VISIBLE);

    }


    @Override
    public int getItemCount() {

        //This method needs to be overridden so that Androids knows how many items
        //will be making it into the list

        return subExtensionList.size();
    }


    @NonNull
    @Override
    public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //This is what adds the code we've written in here to our target view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View v = inflater.inflate(R.layout.selectable_app_list_item, parent, false);

        return new Adapter.ViewHolder(v);
    }
}