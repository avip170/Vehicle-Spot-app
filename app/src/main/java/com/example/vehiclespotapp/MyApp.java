package com.example.vehiclespotapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MyApp extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        try {
            // Get the saved language from SharedPreferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(base);
            String language = preferences.getString("Locale.Helper.Selected.Language", "en");
            
            // Apply the language
            super.attachBaseContext(LocaleHelper.setLocale(base, language));
        } catch (Exception e) {
            super.attachBaseContext(base);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            // Ensure the app starts with the correct language
            String language = LocaleHelper.getLanguage(this);
            LocaleHelper.setLocale(this, language);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 