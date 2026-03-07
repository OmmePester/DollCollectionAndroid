package com.example.dollcollectionandroid;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
// for getting permission to external hidden files
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {

    // ADDED: The manager variable so the switch works [cite: 2026-02-22]
    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        setContentView(R.layout.activity_main);

        // PERMISSION!!!
        // request permission
        checkStoragePermissions();

        // INITIALIZE DATABASE [cite: 2026-02-22]
        dbManager = new DatabaseManager(this);

        // ============================================================
        // THE SAFETY SWITCH
        // Uncomment to wipe, Comment to save.
        // ============================================================
//         dbManager.fullWipeOut();

        // 1. VIEW COLLECTION: Redirects to the CollectionActivity window [cite: 2026-03-01]
        findViewById(R.id.btnViewCollection).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CollectionActivity.class);
            startActivity(intent);
            System.out.println("Navigating to Collection Activity");
        });

        // 2. SETTINGS: Currently a placeholder per your design [cite: 2026-03-01]
        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            // Logic for settings will go here later
            System.out.println("Settings clicked - Placeholder only");
        });

        // 3. EXIT: Closes the app completely [cite: 2026-03-01]
        findViewById(R.id.btnExit).setOnClickListener(v -> {
            System.out.println("Exiting App...");
            finishAffinity(); // Kills the activity stack and exits [cite: 2026-03-01]
        });
    }

    // this is helper method for requesting permission
    private void checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ logic
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 101);
            }
        } else {
            // Android 12 and below logic
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            }
        }
    }
}