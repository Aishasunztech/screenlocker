package com.screenlocker.secure.appSelection;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
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
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.launcher.AppInfo;
import com.screenlocker.secure.utils.AppConstants;

import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {
    private String packageName;
    private final List<AppInfo> appsList;


    class ViewHolder extends RecyclerView.ViewHolder {
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

            guestSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> appsList.get(getAdapterPosition()).setGuest(isChecked));

            encryptedSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> appsList.get(getAdapterPosition()).setEncrypted(isChecked));

            enabledSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> appsList.get(getAdapterPosition()).setEnable(isChecked));
        }

    }


    AppListAdapter(String packageName, List<AppInfo> apps) {
        this.packageName = packageName;
        this.appsList = apps;
    }


    @Override
    public void onBindViewHolder(@NonNull AppListAdapter.ViewHolder vh, int i) {

        //Here we use the information in the list we created to define the views

        AppInfo info = appsList.get(i);
        vh.tv.setText(info.getLabel());

//        if (info.getUniqueName().equals(AppConstants.SECURE_CLEAR_UNIQUE)) {
//            vh.img.setColorFilter(ContextCompat.getColor(vh.img.getContext(), R.color.icon_tint), android.graphics.PorterDuff.Mode.MULTIPLY);
//        } else {
//            vh.img.setColorFilter(null);
//        }

        // vh.img.setImageDrawable(info.getIcon());
        Glide.with(vh.img.getContext())
                .load(appsList.get(i).getIcon())
                .apply(new RequestOptions().centerCrop())
                .into(vh.img);

        vh.guestSwitch.setChecked(info.isGuest());
        vh.encryptedSwitch.setChecked(info.isEncrypted());
        vh.enabledSwitch.setChecked(info.isEnable());

        if (info.getPackageName().contains(packageName)) {
            vh.enabledSwitch.setVisibility(View.INVISIBLE);
            vh.encryptedSwitch.setVisibility(View.INVISIBLE);
        } else {
            vh.enabledSwitch.setVisibility(View.VISIBLE);
            vh.encryptedSwitch.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public int getItemCount() {

        //This method needs to be overridden so that Androids knows how many items
        //will be making it into the list

        return appsList.size();
    }


    @NonNull
    @Override
    public AppListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //This is what adds the code we've written in here to our target view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View v = inflater.inflate(R.layout.selectable_app_list_item, parent, false);

        return new ViewHolder(v);
    }

}