package com.example.vehiclespotapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LanguageActivity extends AppCompatActivity {
    private Spinner languageSpinner;
    private String[] languages = {"en", "hi"};
    private String[] languageNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        languageNames = new String[]{
                getString(R.string.language_english),
                getString(R.string.language_hindi)
        };

        languageSpinner = findViewById(R.id.languageSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                languageNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        // Set current language as selected
        String currentLanguage = LocaleHelper.getLanguage(this);
        int position = currentLanguage.equals("hi") ? 1 : 0;
        languageSpinner.setSelection(position);

        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = languages[position];
                if (!LocaleHelper.getLanguage(LanguageActivity.this).equals(selectedLanguage)) {
                    LocaleHelper.setLocale(LanguageActivity.this, selectedLanguage);
                    recreate();
                    Toast.makeText(LanguageActivity.this, R.string.language_changed, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, LanguageActivity.class);
        context.startActivity(intent);
    }
} 