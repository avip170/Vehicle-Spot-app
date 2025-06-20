package com.example.vehiclespotapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView registerLink, forgotPasswordLink;
    private SignInButton googleSignInButton;
    private GoogleSignInClient mGoogleSignInClient;
    private CheckBox rememberMeCheckBox;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
        if (isLoggedIn) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_login);

        // Initialize views
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);
        registerLink = findViewById(R.id.textRegister);
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink);
        googleSignInButton = findViewById(R.id.buttonGoogleSignIn);
        rememberMeCheckBox = findViewById(R.id.checkboxRememberMe);

        // Load saved credentials if Remember Me was checked
        SharedPreferences loginPrefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        boolean rememberMe = loginPrefs.getBoolean("remember_me", false);
        rememberMeCheckBox.setChecked(rememberMe);
        if (rememberMe) {
            emailEditText.setText(loginPrefs.getString("email", ""));
            passwordEditText.setText(loginPrefs.getString("password", ""));
        }

        // Configure Google Sign-In
        configureGoogleSignIn();

        // Set up click listeners
        loginButton.setOnClickListener(v -> {
            attemptLogin();
            // Save or clear credentials based on Remember Me
            SharedPreferences.Editor loginEditor = loginPrefs.edit();
            if (rememberMeCheckBox.isChecked()) {
                loginEditor.putBoolean("remember_me", true);
                loginEditor.putString("email", emailEditText.getText().toString().trim());
                loginEditor.putString("password", passwordEditText.getText().toString());
            } else {
                loginEditor.clear();
            }
            loginEditor.apply();
        });
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
        forgotPasswordLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
        googleSignInButton.setOnClickListener(v -> signIn());
    }

    private void configureGoogleSignIn() {
        try {
            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestProfile()
                    .build();

            // Build a GoogleSignInClient with the options specified by gso.
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            
            // Check if user is already signed in
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (account != null) {
                // User is already signed in
                onGoogleSignInSuccess(account);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error configuring Google Sign-In: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void signIn() {
        try {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } catch (Exception e) {
            Toast.makeText(this, "Error starting Google Sign-In: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            onGoogleSignInSuccess(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            String errorMessage = "Google Sign-In failed: ";
            switch (e.getStatusCode()) {
                case 12501:
                    errorMessage += "Sign-in was cancelled by user";
                    break;
                case 12500:
                    errorMessage += "Sign-in failed. Please try again.";
                    break;
                case 7:
                    errorMessage += "Network error. Please check your internet connection.";
                    break;
                case 8:
                    errorMessage += "Internal error. Please try again.";
                    break;
                case 10:
                    errorMessage += "Developer error. Please contact support.";
                    break;
                case 12:
                    errorMessage += "Sign-in is currently in progress.";
                    break;
                default:
                    errorMessage += "Error code: " + e.getStatusCode();
                    break;
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Unexpected error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void onGoogleSignInSuccess(GoogleSignInAccount account) {
        try {
            // Save user information to SharedPreferences
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            prefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putString("email", account.getEmail())
                    .putString("first_name", account.getDisplayName() != null ? account.getDisplayName() : "")
                    .putString("last_name", "")
                    .putString("phone", "")
                    .putBoolean("is_verified", true)
                    .putBoolean("google_sign_in", true)
                    .putString("google_account_id", account.getId())
                    .putString("user_photo_url", account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "")
                    .putString("user_id", account.getId())
                    .putLong("sign_in_timestamp", System.currentTimeMillis())
                    .apply();

            String welcomeMessage = "Welcome, " + (account.getDisplayName() != null ? account.getDisplayName() : account.getEmail()) + "!";
            Toast.makeText(this, welcomeMessage, Toast.LENGTH_SHORT).show();
            
            // Send welcome email (optional)
            sendWelcomeEmail(account.getEmail(), account.getDisplayName());
            
            // Navigate to MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void sendWelcomeEmail(String email, String displayName) {
        try {
            // Create email intent
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@vehiclespotapp.com"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Welcome to Vehicle Spot App");
            emailIntent.putExtra(Intent.EXTRA_TEXT, 
                "Dear " + (displayName != null ? displayName : "User") + ",\n\n" +
                "Welcome to Vehicle Spot App! We're excited to have you on board.\n\n" +
                "Your account has been successfully created with email: " + email + "\n\n" +
                "Features available:\n" +
                "• Save parking locations\n" +
                "• Find your parked vehicle\n" +
                "• Real-time location tracking\n" +
                "• Parking history\n" +
                "• Biometric authentication\n\n" +
                "If you have any questions or need support, please don't hesitate to contact us.\n\n" +
                "Best regards,\nVehicle Spot App Team");
            
            // Check if there's an app to handle email
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(emailIntent, "Send welcome email"));
            }
        } catch (Exception e) {
            // Email sending failed, but don't block the sign-in process
            Log.e("LoginActivity", "Failed to send welcome email: " + e.getMessage());
        }
    }

    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String regEmail = prefs.getString("email", null);
        String regPassword = prefs.getString("password", null);
        if (regEmail == null || regPassword == null) {
            Toast.makeText(this, "No user registered. Please register first.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
            return;
        }
        if (email.equals(regEmail) && password.equals(regPassword)) {
            // Login success
            prefs.edit().putBoolean("is_logged_in", true).apply();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void showForgotPasswordDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        // Set up the input fields
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);

        final android.widget.EditText emailInput = new android.widget.EditText(this);
        emailInput.setHint("Registered Email");
        layout.addView(emailInput);

        final android.widget.EditText newPasswordInput = new android.widget.EditText(this);
        newPasswordInput.setHint("New Password");
        newPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPasswordInput);

        builder.setView(layout);

        builder.setPositiveButton("Reset", (dialog, which) -> {
            String email = emailInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();

            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String regEmail = prefs.getString("email", null);

            if (regEmail != null && regEmail.equals(email)) {
                if (newPassword.length() < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                prefs.edit().putString("password", newPassword).apply();
                Toast.makeText(this, "Password reset successful!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Email not found!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public static void signOutGoogle(android.content.Context context) {
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestProfile()
                    .build();
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
            mGoogleSignInClient.signOut();
        } catch (Exception e) {
            // Handle sign out error silently
        }
    }
} 