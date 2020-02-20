package com.screenlocker.secure.settings.codeSetting.secureSettings;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.screenlocker.secure.room.MyAppDatabase;
import com.secure.launcher.R;
import com.screenlocker.secure.app.MyApplication;
import com.screenlocker.secure.room.SubExtension;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import static com.screenlocker.secure.utils.AppConstants.SUB_AdminPanel;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private final List<SubExtension> subExtensionList;

    private Context context;
    private int guestChecked = 0, encryptionChecked = 0;
    private MenuChecklistener checklistener;
    private boolean isFirstTime = true;

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


            guestSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    subExtensionList.get(getAdapterPosition()).setGuest(isChecked);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            checklistener.updateMenu();
                            MyAppDatabase.getInstance(context).getDao().setGuest(isChecked, subExtensionList.get(getAdapterPosition()).getUniqueExtension());
                        }
                    }).start();
                }
            });

            encryptedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    subExtensionList.get(getAdapterPosition()).setEncrypted(isChecked);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            checklistener.updateMenu();
                            MyAppDatabase.getInstance(context).getDao().setEncrypted(isChecked, subExtensionList.get(getAdapterPosition()).getUniqueExtension());
                        }
                    }).start();
                }
            });

        }

    }

    public Adapter(List<SubExtension> subExtensions, Context context) {

        this.subExtensionList = subExtensions;
        this.context = context;
        if (context instanceof MenuChecklistener) {
            checklistener = (MenuChecklistener) context;
        }
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

        vh.img.setColorFilter(ContextCompat.getColor(vh.img.getContext(), R.color.icon_tint), android.graphics.PorterDuff.Mode.MULTIPLY);


        vh.guestSwitch.setChecked(info.isGuest());
        vh.encryptedSwitch.setChecked(info.isEncrypted());
        vh.enabledSwitch.setVisibility(View.GONE);
        if (info.getUniqueExtension().equals(AppConstants.SECURE_SETTINGS_UNIQUE + SUB_AdminPanel)){
            vh.encryptedSwitch.setVisibility(View.INVISIBLE);
        }else vh.encryptedSwitch.setVisibility(View.VISIBLE);

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

    public interface MenuChecklistener {
        void updateMenu();
    }
}