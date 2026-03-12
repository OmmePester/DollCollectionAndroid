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

/**
 * This is the main activity. It starts the app.
 * It initializes DatabaseManager for all future data manipulations.
 * It handles all critical permissions for accessing hidden folders.
 * It provides user with navigation menu that has three important Buttons.
 */

public class MainActivity extends AppCompatActivity {

    // VARIABLES
    private DatabaseManager dbManager;

    // this startup method runs when user clicks the app icon
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // this is similar to constructor, but with necessary Android functionalities
        super.onCreate(savedInstanceState);

        // connects with the XML code of this activity
        setContentView(R.layout.activity_main);

        // REQUESTS PERMISSIONS!!!
        checkStoragePermissions();

        // INITIALIZE DATABASE MANAGER!!!!
        dbManager = new DatabaseManager(this);    // here we pass our infamous CONTEXT

        // ============================================================
        // THE SAFETY SWITCH
        // Uncomment to wipe, Comment to save.
        // ============================================================
//         dbManager.fullWipeOut();

        // BUTTON VIEW COLLECTION: redirects to CollectionActivity window
        // finds Button ID in corresponding XML and sets listener for clicks
        findViewById(R.id.btnViewCollection).setOnClickListener(v -> {
            // creates Intent, which is used to move to other activity
            Intent intent = new Intent(MainActivity.this, CollectionActivity.class);
            // moves to other activity
            startActivity(intent);
        });

        // BUTTON SETTINGS: redirects to SettingsActivity window
        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // BUTTON EXIT: closes our app completely
        findViewById(R.id.btnExit).setOnClickListener(v -> {
            finishAffinity();    // kills this activity
        });
    }

    // this helper method requests permissions
    private void checkStoragePermissions() {
        // checks version of Android device
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // for Android 13+: if access not granted -> grant access
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 101);
            }
        } else {
            // for Android 12 and below: if access not granted -> grant access
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            }
        }
    }
}