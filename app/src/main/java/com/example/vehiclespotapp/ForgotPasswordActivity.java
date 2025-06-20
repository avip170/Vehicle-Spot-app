package com.example.vehiclespotapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.vehiclespotapp.service.EmailSupportService;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText emailEditText, newPasswordEditText;
    private Button resetButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEditText = findViewById(R.id.editTextEmail);
        newPasswordEditText = findViewById(R.id.editTextNewPassword);
        resetButton = findViewById(R.id.buttonResetPassword);

        resetButton.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = emailEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String regEmail = prefs.getString("email", null);

        if (regEmail != null && regEmail.equals(email)) {
            // Update password
            prefs.edit().putString("password", newPassword).apply();
            
            // Send password reset email
            EmailSupportService.sendPasswordResetEmail(this, email);
            
            Toast.makeText(this, "Password reset successful! Check your email.", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Email not found in our records!", Toast.LENGTH_SHORT).show();
        }
    }
} 