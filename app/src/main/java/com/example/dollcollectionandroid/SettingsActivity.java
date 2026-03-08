package com.example.dollcollectionandroid;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private TextView tvStatus;
    private Button btnConnect;
    private Button btnSyncNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings); // You will need to create this XML layout

        tvStatus = findViewById(R.id.tvCloudStatus);
        btnConnect = findViewById(R.id.btnConnectGoogle);
        btnSyncNow = findViewById(R.id.btnSyncNow);

        tvStatus.setText("Status: Not Connected");

        btnConnect.setOnClickListener(v -> {
            // We will put the Google Sign-In trigger here next!
            tvStatus.setText("Status: Connecting...");
        });

        btnSyncNow.setOnClickListener(v -> {
            // We will call DriveServiceHelper here next!
        });
    }
}