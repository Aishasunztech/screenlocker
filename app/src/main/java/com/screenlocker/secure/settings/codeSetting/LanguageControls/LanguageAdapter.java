package com.screenlocker.secure.settings.codeSetting.LanguageControls;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.secure.launcher.R;

import java.util.ArrayList;

public class LanguageAdapter extends ArrayAdapter<LanguageModel> {
    private final Context context;
    private final String[] values;
    private  String selectedText;
    private ArrayList<LanguageModel> langList;

    public LanguageAdapter(Context context, String[] values, String selectedText, ArrayList<LanguageModel> langList) {
        super(context,R.layout.item_languages,langList);
        this.context = context;
        this.values = values;
        this.langList = langList;
        this.selectedText = selectedText;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LanguageModel model = langList.get(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_languages, parent, false);
        }
        TextView tv_lang = convertView.findViewById(R.id.langName);
        ImageView img_flag = convertView.findViewById(R.id.img_flag);
        RadioButton checkBox = convertView.findViewById(R.id.language_check);

        tv_lang.setText(model.getLanguage_name());
        img_flag.setImageResource(model.getFlagId());
        if (model.getLanguage_key().equals(selectedText)) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }
        checkBox.setOnClickListener(v -> {
            selectedText = model.getLanguage_key();
            notifyDataSetChanged();
        });

        return convertView;
    }

    public String getSelectedText() {
        return selectedText;
    }
}



