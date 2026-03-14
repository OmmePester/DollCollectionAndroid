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

/**
 *
 */

public class AddDollActivity extends AppCompatActivity {

    // VARIABLES
    private EditText nameInput;           // for name input
    private ImageView dollImageView;      // for viewing selected Image
    private Uri selectedImageUri;         // for saving Image by URI (Uniform Resource Identifier)
    private DatabaseManager dbManager;    // for SQL DB
    private Button saveButton;            // for final data save

    // this startup method initializes AddDollActivity,
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // starts standard lifecycle
        super.onCreate(savedInstanceState);
        // connects XML file with this activity
        setContentView(R.layout.activity_add_doll);
        // finds IDs of XML elements (TextView, Button)
        nameInput = findViewById(R.id.nameInput);
        dollImageView = findViewById(R.id.dollImageView);
        saveButton = findViewById(R.id.saveButton);
        // instantiates DatabaseManager for SQL DB operations, passes this activity Context
        dbManager = new DatabaseManager(this);
        // listens to click, and opens phone gallery for image selection
        dollImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1000);
        });

        // Click the purple button to save
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
        // rejects saving process if there is no image URI or no name input
        if (selectedImageUri == null || nameInput.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please add a name and photo!", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = nameInput.getText().toString();

        // 1. Saving entry to SQL
        // This method sets default values with adding Doll to db
        // We pass "pending" as a placeholder for the path until the file is created.
        int newId = dbManager.addDoll(name, "pending");

        // 2. Creating unique filename for your 'closet' folder
        String fileName = "doll_" + newId + ".jpg";

        try {
            // creating 'closet' subfolder in hidden root 'closetDollUp' folder if it doesn't exist
            File closetFolder = new File(StorageHelper.getHiddenFolder(), "closet");
//            if (!closetFolder.exists()) {
//                closetFolder.mkdirs();
//            }

            // preparing physical file inside 'closet' folder
            File closetFile = new File(closetFolder, fileName);

            // 3. Copying image from gallery to private 'closet' folder
            InputStream is = getContentResolver().openInputStream(selectedImageUri);
            FileOutputStream fos = new FileOutputStream(closetFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            is.close();
            fos.close();

            // 4. Update SQL with real path now
            dbManager.completeDollInitialSave(newId, fileName);

            Toast.makeText(this, "Saved to Closet!", Toast.LENGTH_SHORT).show();

            // 5. Going back to the list
            // We use finish() to close window by adding doll
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }
}