package com.screenlocker.secure.settings.codeSetting.LanguageControls;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.screenlocker.secure.R;
import com.screenlocker.secure.appSelection.AppSelectionActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.ArrayList;

import static com.screenlocker.secure.utils.AppConstants.BROADCAST_APPS_ACTION;
import static com.screenlocker.secure.utils.AppConstants.KEY_DATABASE_CHANGE;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.MyViewHolder> {
    private ArrayList<LanguageModel> langList;
    private Context context;
    private RecreateActivityListener listener;

    public LanguageAdapter(ArrayList<LanguageModel> langList, Context context) {
        this.langList = langList;
        this.context = context;
        listener = (RecreateActivityListener) context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_languages, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setData(langList.get(position));
    }

    @Override
    public int getItemCount() {
        return langList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tv_lang;
        ImageView img_flag;
        CheckBox checkBox;
        View view;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_lang = itemView.findViewById(R.id.langName);
            img_flag = itemView.findViewById(R.id.img_flag);
            checkBox = itemView.findViewById(R.id.language_check);
            view = itemView.findViewById(R.id.language_divider);
            itemView.setOnClickListener(this);
        }

        public void setData(LanguageModel languageModel) {

            LanguageModel model = langList.get(getAdapterPosition());
            tv_lang.setText(languageModel.getLanguage_name());

            if(model.getLanguage_key().equals("en"))
            {
                img_flag.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_flag_of_the_united_states));
            }
            else if(model.getLanguage_key().equals("fr"))
            {
                img_flag.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_flag_of_france));
            }else if(model.getLanguage_key().equals("vi"))
            {
                img_flag.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_flag_of_vietnam));
            }

            String key = PrefUtils.getStringPref(context,AppConstants.LANGUAGE_PREF);
           if(model.getLanguage_key().equals(key))
            {

                checkBox.setChecked(true);
            }
           else if (key ==null || key.equals("")){
               if (getAdapterPosition() == 0){
                   checkBox.setChecked(true);
               }else {
                   checkBox.setChecked(false);
               }
           }
           checkBox.setClickable(false);
           if(getAdapterPosition() == langList.size()-1){
               view.setVisibility(View.GONE);
           }else{
               view.setVisibility(View.VISIBLE);
           }

        }

        @Override
        public void onClick(View v) {
            showLanguageDialog();

        }

        private void showLanguageDialog() {
            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle(context.getResources().getString(R.string.change_language_dialog_title));
            alertDialog.setMessage(context.getResources().getString(R.string.change_language_dialog_message));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getResources().getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    changeLanguage();
                    dialog.dismiss();
                }
            });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getResources().getString(R.string.cancel_capital), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.setCancelable(false);

            alertDialog.show();
        }

        private void changeLanguage() {
            LanguageModel languageModel = langList.get(getAdapterPosition());
            CommonUtils.setAppLocale(languageModel.getLanguage_key(), context);
            PrefUtils.saveStringPref(context, AppConstants.LANGUAGE_PREF, languageModel.getLanguage_key());
            listener.recreatActivity();

            Intent intent = new Intent(BROADCAST_APPS_ACTION);
            intent.putExtra(KEY_DATABASE_CHANGE, "apps");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    public interface RecreateActivityListener {
        void recreatActivity();
    }
}
