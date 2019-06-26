package com.screenlocker.secure.settings.codeSetting.LanguageControls;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.screenlocker.secure.R;
import com.screenlocker.secure.base.BaseActivity;
import com.screenlocker.secure.settings.SettingsActivity;
import com.screenlocker.secure.utils.AppConstants;
import com.screenlocker.secure.utils.PrefUtils;

import java.util.ArrayList;

public class ChangeLanguageActivity extends BaseActivity implements LanguageAdapter.RecreateActivityListener {

    private RecyclerView rc;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_language);
        rc = findViewById(R.id.languages_list);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.language));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rc.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<LanguageModel> models = new ArrayList<>();
        String key = PrefUtils.getStringPref(this, AppConstants.LANGUAGE_PREF);
        if (key != null && !key.equals("")) {
            String[] languages = getResources().getStringArray(R.array.languages);

            for (String language : languages) {
                    String language_key = language.split(":")[0];
                    String language_name = language.split(":")[1];
                    LanguageModel languageModel2 = new LanguageModel(language_key, language_name);
                    models.add(languageModel2);
            }
        } else {
            String[] languages = getResources().getStringArray(R.array.languages);
            for (String language : languages) {
                String language_key = language.split(":")[0];
                String language_name = language.split(":")[1];
                LanguageModel languageModel2 = new LanguageModel(language_key, language_name);
                models.add(languageModel2);
            }
        }


        rc.setAdapter(new LanguageAdapter(models, this));


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void recreatActivity() {

        recreate();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
