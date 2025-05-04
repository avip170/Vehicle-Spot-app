package com.example.vehiclespotapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "app_settings";
    private static final String[] MAP_TYPES = {"Normal", "Satellite", "Terrain", "Hybrid"};
    private static final String[] LANGUAGES = {
        "English",
        "Hindi",
        "Gujarati",
        "Spanish",
        "French",
        "German",
        "Italian",
        "Portuguese",
        "Russian",
        "Chinese",
        "Japanese",
        "Korean",
        "Arabic",
        "Bengali",
        "Tamil",
        "Telugu",
        "Malayalam",
        "Kannada",
        "Marathi",
        "Punjabi"
    };

    // Add this mapping for language codes
    private static final String[] LANGUAGE_CODES = {
        "en", // English
        "hi", // Hindi
        "gu", // Gujarati
        "es", // Spanish
        "fr", // French
        "de", // German
        "it", // Italian
        "pt", // Portuguese
        "ru", // Russian
        "zh", // Chinese
        "ja", // Japanese
        "ko", // Korean
        "ar", // Arabic
        "bn", // Bengali
        "ta", // Tamil
        "te", // Telugu
        "ml", // Malayalam
        "kn", // Kannada
        "mr", // Marathi
        "pa"  // Punjabi
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize views
        initializeViews();
        loadSavedSettings();
    }

    private void initializeViews() {
        try {
            // App Version
            TextView versionText = findViewById(R.id.textAppVersion);
            String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            versionText.setText("App Version: " + version);

            // Account Settings
            Button changePasswordButton = findViewById(R.id.buttonChangePassword);
            Button deleteAccountButton = findViewById(R.id.buttonDeleteAccount);

            // Notification Settings
            SwitchMaterial switchNotifications = findViewById(R.id.switchNotifications);
            SwitchMaterial switchSound = findViewById(R.id.switchSound);
            SwitchMaterial switchVibration = findViewById(R.id.switchVibration);

            // Map Settings
            AutoCompleteTextView dropdownMapType = findViewById(R.id.dropdownMapType);
            SwitchMaterial switchTraffic = findViewById(R.id.switchTraffic);

            // Theme Settings
            SwitchMaterial switchDarkMode = findViewById(R.id.switchDarkMode);

            // Language Settings
            AutoCompleteTextView dropdownLanguage = findViewById(R.id.dropdownLanguage);

            // Logout Button
            Button logoutButton = findViewById(R.id.buttonLogout);

            // Set up click listeners and change listeners
            setupListeners(changePasswordButton, deleteAccountButton, switchNotifications, 
                    switchSound, switchVibration, dropdownMapType, switchTraffic, 
                    switchDarkMode, dropdownLanguage, logoutButton);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing settings: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupListeners(Button changePasswordButton, Button deleteAccountButton,
                              SwitchMaterial switchNotifications, SwitchMaterial switchSound,
                              SwitchMaterial switchVibration, AutoCompleteTextView dropdownMapType,
                              SwitchMaterial switchTraffic, SwitchMaterial switchDarkMode,
                              AutoCompleteTextView dropdownLanguage, Button logoutButton) {
        try {
            // Account Settings
            changePasswordButton.setOnClickListener(v -> {
                showChangePasswordDialog();
            });

            deleteAccountButton.setOnClickListener(v -> {
                showDeleteAccountDialog();
            });

            // Notification Settings
            switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveSetting("notifications_enabled", isChecked);
            });

            switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveSetting("notification_sound", isChecked);
            });

            switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveSetting("notification_vibration", isChecked);
            });

            // Map Settings
            dropdownMapType.setAdapter(new ArrayAdapter<>(this, 
                    android.R.layout.simple_dropdown_item_1line, MAP_TYPES));
            dropdownMapType.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < MAP_TYPES.length) {
                    saveSetting("map_type", position);
                }
            });

            switchTraffic.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveSetting("show_traffic", isChecked);
            });

            // Theme Settings
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                saveSetting("dark_mode", isChecked);
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            });

            // Language Settings
            dropdownLanguage.setAdapter(new ArrayAdapter<>(this, 
                    android.R.layout.select_dialog_item, LANGUAGES));
            dropdownLanguage.setDropDownHeight(600); // Set a fixed height for the dropdown
            dropdownLanguage.setOnClickListener(v -> dropdownLanguage.showDropDown());
            dropdownLanguage.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < LANGUAGES.length) {
                    try {
                        // Save the language preference
                    saveSetting("language", position);
                        
                    // Change app locale
                        String selectedLanguage = LANGUAGE_CODES[position];
                        LocaleHelper.setLocale(this, selectedLanguage);
                        
                        // Restart the app safely
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            // Logout
            logoutButton.setOnClickListener(v -> {
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                prefs.edit().putBoolean("is_logged_in", false).apply();
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up listeners: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadSavedSettings() {
        try {
            // Load notification settings
            ((SwitchMaterial) findViewById(R.id.switchNotifications))
                    .setChecked(sharedPreferences.getBoolean("notifications_enabled", true));
            ((SwitchMaterial) findViewById(R.id.switchSound))
                    .setChecked(sharedPreferences.getBoolean("notification_sound", true));
            ((SwitchMaterial) findViewById(R.id.switchVibration))
                    .setChecked(sharedPreferences.getBoolean("notification_vibration", true));

            // Load map settings
            int mapType = sharedPreferences.getInt("map_type", 0);
            if (mapType >= 0 && mapType < MAP_TYPES.length) {
                ((AutoCompleteTextView) findViewById(R.id.dropdownMapType))
                        .setText(MAP_TYPES[mapType]);
            }
            ((SwitchMaterial) findViewById(R.id.switchTraffic))
                    .setChecked(sharedPreferences.getBoolean("show_traffic", false));

            // Load theme settings
            boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
            ((SwitchMaterial) findViewById(R.id.switchDarkMode))
                    .setChecked(isDarkMode);
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }

            // Load language settings
            int language = sharedPreferences.getInt("language", 0);
            if (language >= 0 && language < LANGUAGES.length) {
                ((AutoCompleteTextView) findViewById(R.id.dropdownLanguage))
                    .setText(LANGUAGES[language], false);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading settings: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveSetting(String key, boolean value) {
        try {
            sharedPreferences.edit().putBoolean(key, value).apply();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving setting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveSetting(String key, int value) {
        try {
            sharedPreferences.edit().putInt(key, value).apply();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving setting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showChangePasswordDialog() {
        try {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Change Password")
                    .setView(dialogView)
                    .setPositiveButton("Change", null)
                    .setNegativeButton("Cancel", null)
                    .create();

            dialog.setOnShowListener(dialogInterface -> {
                Button positiveButton = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(view -> {
                    com.google.android.material.textfield.TextInputEditText currentPassword = dialogView.findViewById(R.id.currentPassword);
                    com.google.android.material.textfield.TextInputEditText newPassword = dialogView.findViewById(R.id.newPassword);
                    com.google.android.material.textfield.TextInputEditText confirmPassword = dialogView.findViewById(R.id.confirmPassword);

                    String currentPass = currentPassword.getText().toString().trim();
                    String newPass = newPassword.getText().toString().trim();
                    String confirmPass = confirmPassword.getText().toString().trim();

                    // Validate inputs
                    if (currentPass.isEmpty()) {
                        currentPassword.setError("Current password is required");
                        return;
                    }

                    if (newPass.isEmpty()) {
                        newPassword.setError("New password is required");
                        return;
                    }

                    if (confirmPass.isEmpty()) {
                        confirmPassword.setError("Please confirm your new password");
                        return;
                    }

                    if (!newPass.equals(confirmPass)) {
                        confirmPassword.setError("Passwords do not match");
                        return;
                    }

                    if (newPass.length() < 6) {
                        newPassword.setError("Password must be at least 6 characters");
                        return;
                    }

                    // Get stored password
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    String storedPassword = prefs.getString("password", "");

                    // Verify current password
                    if (!currentPass.equals(storedPassword)) {
                        currentPassword.setError("Incorrect current password");
                        return;
                    }

                    // Save new password
                    prefs.edit().putString("password", newPass).apply();
                    Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            });

            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing change password dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showDeleteAccountDialog() {
        try {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This will permanently remove all your data and cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        deleteAccount();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing delete account dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void deleteAccount() {
        try {
            // Clear all user preferences
            SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            SharedPreferences.Editor userEditor = userPrefs.edit();
            userEditor.clear();
            userEditor.apply();

            // Clear all app settings
            SharedPreferences appPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor appEditor = appPrefs.edit();
            appEditor.clear();
            appEditor.apply();

            // Clear parking history
            SharedPreferences historyPrefs = getSharedPreferences("parking_history", MODE_PRIVATE);
            SharedPreferences.Editor historyEditor = historyPrefs.edit();
            historyEditor.clear();
            historyEditor.apply();

            // Show success message
            Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();

            // Return to login screen
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error deleting account: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
} 