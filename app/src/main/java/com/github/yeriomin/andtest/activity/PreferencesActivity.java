package com.github.yeriomin.andtest.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.github.yeriomin.andtest.R;

public class PreferencesActivity extends PreferenceActivity{

    public static final String SHOW_ANSWERS = "showAnswers";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}