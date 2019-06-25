package com.screenlocker.secure.settings.codeSetting.LanguageControls;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_lang = itemView.findViewById(R.id.langName);
            itemView.setOnClickListener(this);
        }

        public void setData(LanguageModel languageModel) {
            tv_lang.setText(languageModel.getLanguage_name());
        }

        @Override
        public void onClick(View v) {

            LanguageModel languageModel = langList.get(getAdapterPosition());
            CommonUtils.setAppLocale(languageModel.getLanguage_key(), context);
            PrefUtils.saveStringPref(context, AppConstants.LANGUAGE_PREF, languageModel.getLanguage_key());
            listener.recreatActivity();
            Toast.makeText(context, context.getResources().getString(R.string.language_is_changed), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(BROADCAST_APPS_ACTION);
            intent.putExtra(KEY_DATABASE_CHANGE, "apps");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        }
    }

    public interface RecreateActivityListener {
        void recreatActivity();
    }
}
