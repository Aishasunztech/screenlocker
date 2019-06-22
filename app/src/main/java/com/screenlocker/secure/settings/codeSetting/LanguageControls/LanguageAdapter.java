package com.screenlocker.secure.settings.codeSetting.LanguageControls;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.screenlocker.secure.R;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.CommonUtils;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.ArrayList;

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

        }
    }

    public interface RecreateActivityListener {
        void recreatActivity();
    }
}
