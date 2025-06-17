package com.example.vehiclespotapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class OTPVerificationActivity extends AppCompatActivity {

    private EditText otpEditText;
    private Button verifyButton;
    private TextView resendOTP;
    private String generatedOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Initialize views
        otpEditText = findViewById(R.id.editTextOTP);
        verifyButton = findViewById(R.id.buttonVerifyOTP);
        resendOTP = findViewById(R.id.textResendOTP);

        // Get phone number from intent
        // In onCreate() method, after initializing views
        String phoneNumber = getIntent().getStringExtra("phone_number");
        TextView textPhoneNumber = findViewById(R.id.textPhoneNumber);
        textPhoneNumber.setText(getString(R.string.enter_otp_message) + " " + phoneNumber);

        // Generate and send OTP
        generateAndSendOTP();

        verifyButton.setOnClickListener(v -> verifyOTP());
        resendOTP.setOnClickListener(v -> generateAndSendOTP());
    }

    private void generateAndSendOTP() {
        // Generate a 6-digit OTP
        generatedOTP = String.format("%06d", new Random().nextInt(999999));
        
        // TODO: Implement actual SMS sending logic here
        // For now, just show the OTP in a toast for testing
        Toast.makeText(this, "OTP: " + generatedOTP, Toast.LENGTH_LONG).show();
    }

    private void verifyOTP() {
        String enteredOTP = otpEditText.getText().toString().trim();
        
        if (TextUtils.isEmpty(enteredOTP)) {
            Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        if (enteredOTP.equals(generatedOTP)) {
            // OTP verified successfully
            Toast.makeText(this, "OTP verified successfully!", Toast.LENGTH_SHORT).show();
            
            // Mark user as verified in SharedPreferences
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            prefs.edit().putBoolean("is_verified", true).apply();

            // Proceed to login screen
            Intent intent = new Intent(OTPVerificationActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
        }
    }
}