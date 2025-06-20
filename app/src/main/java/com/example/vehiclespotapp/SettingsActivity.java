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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.vehiclespotapp.service.EmailSupportService;

import java.util.concurrent.Executor;

public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "app_settings";
    
    // Biometric variables
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private Executor executor;
    private Button buttonBiometric;
    private boolean isBiometricEnabled = false;
    
    // Add this mapping for language codes
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

        // Apply saved dark mode preference
        boolean darkMode = sharedPreferences.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        // Initialize views
        initializeViews();
        loadSavedSettings();
        setupBiometricPrompt();
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

            // Language Settings
            AutoCompleteTextView dropdownLanguage = findViewById(R.id.dropdownLanguage);

            // Logout Button
            Button logoutButton = findViewById(R.id.buttonLogout);

            // Email Support Buttons
            Button contactSupportButton = findViewById(R.id.buttonContactSupport);
            Button sendFeedbackButton = findViewById(R.id.buttonSendFeedback);

            // Biometric Button
            buttonBiometric = findViewById(R.id.buttonBiometric);
            isBiometricEnabled = sharedPreferences.getBoolean("biometric_enabled", false);
            updateBiometricButtonState();

            // Set up click listeners and change listeners
            setupListeners(changePasswordButton, deleteAccountButton, switchNotifications, 
                    switchSound, switchVibration, 
                    dropdownLanguage, logoutButton, contactSupportButton, sendFeedbackButton);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing settings: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateBiometricButtonState() {
        if (buttonBiometric != null) {
            BiometricManager biometricManager = BiometricManager.from(this);
            int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
            
            if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                buttonBiometric.setEnabled(true);
                buttonBiometric.setAlpha(1f);
                buttonBiometric.setText(isBiometricEnabled ? "Disable Fingerprint" : "Enable Fingerprint");
                buttonBiometric.setOnClickListener(v -> {
                    if (isBiometricEnabled) {
                        disableBiometric();
                    } else {
                        showBiometricPrompt();
                    }
                });
            } else {
                buttonBiometric.setEnabled(false);
                buttonBiometric.setAlpha(0.5f);
                buttonBiometric.setText("Fingerprint Not Available");
            }
        }
    }

    private void setupBiometricPrompt() {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        isBiometricEnabled = !isBiometricEnabled;
                        sharedPreferences.edit().putBoolean("biometric_enabled", isBiometricEnabled).apply();
                        updateBiometricButtonState();
                        Toast.makeText(SettingsActivity.this, 
                            isBiometricEnabled ? "Fingerprint enabled" : "Fingerprint disabled", 
                            Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(SettingsActivity.this, 
                            "Authentication error: " + errString, 
                            Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(SettingsActivity.this, 
                            "Authentication failed", 
                            Toast.LENGTH_SHORT).show();
                    }
                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Authentication")
                .setSubtitle(isBiometricEnabled ? "Authenticate to disable fingerprint" : "Authenticate to enable fingerprint")
                .setNegativeButtonText("Cancel")
                .build();
    }

    private void showBiometricPrompt() {
        if (biometricPrompt != null && promptInfo != null) {
            biometricPrompt.authenticate(promptInfo);
        } else {
            Toast.makeText(this, "Biometric prompt not initialized", Toast.LENGTH_SHORT).show();
        }
    }

    private void disableBiometric() {
        isBiometricEnabled = false;
        sharedPreferences.edit().putBoolean("biometric_enabled", false).apply();
        updateBiometricButtonState();
        Toast.makeText(this, "Fingerprint disabled", Toast.LENGTH_SHORT).show();
    }

    private void setupListeners(Button changePasswordButton, Button deleteAccountButton,
                              SwitchMaterial switchNotifications, SwitchMaterial switchSound,
                              SwitchMaterial switchVibration, AutoCompleteTextView dropdownLanguage, Button logoutButton,
                              Button contactSupportButton, Button sendFeedbackButton) {
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

            // Language Settings
            dropdownLanguage.setAdapter(new ArrayAdapter<>(this, 
                    android.R.layout.select_dialog_item, LANGUAGES));
            dropdownLanguage.setOnClickListener(v -> dropdownLanguage.showDropDown());
            dropdownLanguage.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < LANGUAGES.length) {
                    try {
                    saveSetting("language", position);
                        String selectedLanguage = LANGUAGE_CODES[position];
                        LocaleHelper.setLocale(this, selectedLanguage);
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
                boolean isGoogleSignIn = prefs.getBoolean("google_sign_in", false);
                
                if (isGoogleSignIn) {
                    // For Google users, show a confirmation dialog
                    new AlertDialog.Builder(this)
                            .setTitle("Sign Out")
                            .setMessage("Are you sure you want to sign out of your Google account?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // Sign out from Google
                                LoginActivity.signOutGoogle(this);
                                // Clear all user data
                                prefs.edit().clear().apply();
                                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .setNegativeButton("No", null)
                            .show();
                } else {
                    // For regular users, just log out
                    prefs.edit().putBoolean("is_logged_in", false).apply();
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });

            // Email Support Buttons
            contactSupportButton.setOnClickListener(v -> {
                EmailSupportService.openSupportEmail(this);
            });

            sendFeedbackButton.setOnClickListener(v -> {
                showFeedbackDialog();
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

            SwitchMaterial switchDarkMode = findViewById(R.id.switchDarkMode);
            switchDarkMode.setChecked(sharedPreferences.getBoolean("dark_mode", false));
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply();
                AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            });

            // Load language settings
            int language = sharedPreferences.getInt("language", 0);
            if (language >= 0 && language < LANGUAGES.length) {
                ((AutoCompleteTextView) findViewById(R.id.dropdownLanguage))
                    .setText(LANGUAGES[language], false);
            }

            // Removed: Biometric settings
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
            // Check if user is signed in with Google
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            boolean isGoogleSignIn = prefs.getBoolean("google_sign_in", false);
            
            if (isGoogleSignIn) {
                // Show message for Google users
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Change Password")
                        .setMessage("You are signed in with Google. To change your password, please go to your Google Account settings.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }
            
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
            // Check if user is signed in with Google
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            boolean isGoogleSignIn = prefs.getBoolean("google_sign_in", false);
            
            String message = isGoogleSignIn ? 
                "Are you sure you want to delete your account? This will permanently remove all your data and cannot be undone. You will also be signed out of your Google account." :
                "Are you sure you want to delete your account? This will permanently remove all your data and cannot be undone.";
            
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage(message)
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
            // Send account deletion confirmation email
            EmailSupportService.sendAccountDeletionEmail(this);
            
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

    private void showFeedbackDialog() {
        try {
            View dialogView = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
            android.widget.EditText feedbackInput = new android.widget.EditText(this);
            feedbackInput.setHint("Enter your feedback here...");
            feedbackInput.setMinLines(4);
            feedbackInput.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Send Feedback")
                    .setView(feedbackInput)
                    .setPositiveButton("Send", (dialogInterface, i) -> {
                        String feedback = feedbackInput.getText().toString().trim();
                        if (!feedback.isEmpty()) {
                            EmailSupportService.sendFeedbackEmail(this, feedback);
                            Toast.makeText(this, "Feedback sent successfully!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create();

            dialog.show();
        } catch (Exception e) {
            Toast.makeText(this, "Error showing feedback dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}