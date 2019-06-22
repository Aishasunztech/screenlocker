package com.screenlocker.secure.settings.codeSetting.LanguageControls;

public class LanguageModel {
    private String language_key;
    private String language_name;

    public LanguageModel(String language_key, String language_name) {
        this.language_key = language_key;
        this.language_name = language_name;
    }

    public String getLanguage_key() {
        return language_key;
    }

    public void setLanguage_key(String language_key) {
        this.language_key = language_key;
    }

    public String getLanguage_name() {
        return language_name;
    }

    public void setLanguage_name(String language_name) {
        this.language_name = language_name;
    }
}
