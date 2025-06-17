import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences;


import com.example.vehiclespotapp.R;
import com.example.vehiclespotapp.OTPVerificationActivity;
import com.example.vehiclespotapp.LoginActivity;
// Make sure your package declaration at the top of this file is correct,
// e.g., package com.yourpackage;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.SharedPreferences;

// This includes the earlier correct import: import com.example.vehiclespotapp.R;
// And other imports up to line 20.
import android.content.SharedPreferences; // This was line 20 in the original file (itself a duplicate of an earlier import)

// The following lines (originally 22-27) have been removed:
// // Replace this incorrect import:
// // import com.yourpackage.R;
// // With the correct import using your actual applicationId:
// import com.your.actual.applicationid.R; // <-- MODIFIED LINE: Use your actual applicationId here

public class RegisterActivity extends AppCompatActivity { // This was originally line 28
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // This R should now resolve correctly

        EditText firstName = findViewById(R.id.editTextFirstName);
        EditText lastName = findViewById(R.id.editTextLastName);
        EditText email = findViewById(R.id.editTextEmail);
        EditText phone = findViewById(R.id.editTextPhone);
        EditText password = findViewById(R.id.editTextPassword);
        EditText confirmPassword = findViewById(R.id.editTextConfirmPassword);
        Button registerButton = findViewById(R.id.buttonRegister);
        TextView loginText = findViewById(R.id.textLogin);

        registerButton.setOnClickListener(v -> {
            String firstNameStr = firstName.getText().toString().trim();
            String lastNameStr = lastName.getText().toString().trim();
            String emailStr = email.getText().toString().trim();
            String phoneStr = phone.getText().toString().trim();
            String passwordStr = password.getText().toString().trim();
            String confirmPasswordStr = confirmPassword.getText().toString().trim();

            if (firstNameStr.isEmpty() || lastNameStr.isEmpty() || emailStr.isEmpty() ||
                phoneStr.isEmpty() || passwordStr.isEmpty() || confirmPasswordStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!passwordStr.equals(confirmPasswordStr)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            if (passwordStr.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save credentials to SharedPreferences (for example)
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            prefs.edit()
                .putString("first_name", firstNameStr)
                .putString("last_name", lastNameStr)
                .putString("email", emailStr)
                .putString("password", passwordStr)
                .putString("phone", phoneStr)
                .putBoolean("is_verified", false)
                .apply();

            // Proceed to OTP verification or next step
            Intent intent = new Intent(this, com.example.vehiclespotapp.OTPVerificationActivity.class);
            intent.putExtra("phone_number", phoneStr);
            startActivity(intent);
            finish();
        });

        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
