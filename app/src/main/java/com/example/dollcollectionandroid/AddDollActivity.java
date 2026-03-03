package com.example.dollcollectionandroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;    // added to handle image rotation bug
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

//
public class AddDollActivity extends AppCompatActivity {

    private EditText nameInput, hintInput;
    private ImageView dollImageView;
    private Uri selectedImageUri;
    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        setContentView(R.layout.activity_add_doll);

        // Enable Back Arrow to return to Collection [cite: 2026-03-01]
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add to Closet");
        }

        dbManager = new DatabaseManager(this);
        nameInput = findViewById(R.id.nameInput);
        hintInput = findViewById(R.id.hintInput);
        dollImageView = findViewById(R.id.dollImageView);
        Button saveButton = findViewById(R.id.saveButton);

        // Click the gray box to open phone gallery [cite: 2026-02-22]
        dollImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1000);
        });

        // Click the purple button to save [cite: 2026-02-22]
        saveButton.setOnClickListener(v -> saveDollToCloset());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1000 && data != null) {
            selectedImageUri = data.getData();
            // USE GLIDE FOR PREVIEW SO IT ROTATES CORRECTLY
            Glide.with(this)
                    .load(selectedImageUri)
                    .fitCenter()
                    .into(dollImageView);
        }
    }

    private void saveDollToCloset() {
        if (selectedImageUri == null || nameInput.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please add a name and photo!", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = nameInput.getText().toString();
        String hint = hintInput.getText().toString();

        // 1. Save entry to SQL [cite: 2026-02-22]
        // This method sets "Unknown" and "Generic" as defaults in the DB! [cite: 2026-03-01]
        int newId = dbManager.addDoll(name, "pending");

        // 2. Create the unique filename for your 'closet' [cite: 2026-02-22]
        String fileName = "doll_" + newId + ".jpg";

        try {
            // 3. Copy image from gallery to private closet folder [cite: 2026-02-22]
            InputStream is = getContentResolver().openInputStream(selectedImageUri);
            File closetFile = new File(getFilesDir(), fileName);
            FileOutputStream fos = new FileOutputStream(closetFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            is.close();
            fos.close();

            // 4. Update SQL with real path and hint [cite: 2026-02-22]
            dbManager.completeDollInitialSave(newId, fileName, hint);

            Toast.makeText(this, "Saved to Closet!", Toast.LENGTH_SHORT).show();

            // 5. Go back to the list [cite: 2026-02-22, 2026-03-01]
            // We use finish() instead of Fragment Transactions now.
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    // Handles the top bar back arrow [cite: 2026-03-01]
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}