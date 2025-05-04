package com.example.vehiclespotapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

        resetButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String newPassword = newPasswordEditText.getText().toString().trim();

            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String regEmail = prefs.getString("email", null);

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(newPassword)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            if (regEmail != null && regEmail.equals(email)) {
                if (newPassword.length() < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                prefs.edit().putString("password", newPassword).apply();
                Toast.makeText(this, "Password reset successful!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Email not found!", Toast.LENGTH_SHORT).show();
            }
        });
    }
} 