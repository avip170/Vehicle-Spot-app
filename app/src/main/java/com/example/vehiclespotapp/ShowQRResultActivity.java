package com.example.vehiclespotapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ShowQRResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_qr_result);

        TextView txtResult = findViewById(R.id.txt_qr_result);
        String qrMessage = getIntent().getStringExtra("qr_message");
        txtResult.setText(qrMessage != null ? qrMessage : "No data found in QR");
    }
} 