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
 * This Class is an Activity.
 * It starts a new window that handles user inputs for Doll name and image.
 * It uses only these two inputs to save Doll data into SQL DB; additionally;
 * it captures selected image's URI (Uniform Resource Identifier) and copies image
 * inside a secure hidden local storage. Both name and image inputs are mandatory!
 */

public class AddDollActivity extends AppCompatActivity {

    // VARIABLES
    private EditText nameInput;           // for name input
    private ImageView dollImageView;      // for viewing selected Image
    private Uri selectedImageUri;         // for saving Image by URI (Uniform Resource Identifier)
    private DatabaseManager dbManager;    // for SQL DB
    private Button saveButton;            // for final data save

    // this startup method initializes AddDollActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // starts standard lifecycle
        super.onCreate(savedInstanceState);

        // connects XML file with this activity
        setContentView(R.layout.activity_add_doll);

        // finds IDs of XML elements (ImageView, TextView, Button)
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

        // SAVE BUTTON: listens to clicks and runs saveDollToCloset() method
        saveButton.setOnClickListener(v -> saveDollToCloset());
    }

    // this method checks image selection, extracts URI and displays image with Glide
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1000 && data != null) {
            selectedImageUri = data.getData();
            // USES GLIDE FOR PREVIEW SO IT ROTATES CORRECTLY
            Glide.with(this)
                    .load(selectedImageUri)
                    .fitCenter()
                    .into(dollImageView);
        }
    }

    // this method saves image path and name to SQL DB, and copies image to local hidden folder
    private void saveDollToCloset() {
        // rejects saving process if there is no image URI or no name input
        if (selectedImageUri == null || nameInput.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please add a name and photo!", Toast.LENGTH_SHORT).show();
            return;
        }

        // saves name from EditText to String variable
        String name = nameInput.getText().toString();

        // saves name and image path ("pending" for now) to SQL DB, addDoll() sets other entries to default values
        int newId = dbManager.addDoll(name, "pending");

        // uses unique ID to create new image file name for image copy
        String fileName = "doll_" + newId + ".jpg";

        try {
            // creates 'closet' subfolder path in hidden root '.closetDollUp' folder if it doesn't exist
            File closetFolder = new File(StorageHelper.getHiddenFolder(), "closet");

            // creates file path inside 'closet' folder
            File closetFile = new File(closetFolder, fileName);

            // copies image from gallery to private 'closet' folder
            InputStream is = getContentResolver().openInputStream(selectedImageUri);    // sucks data out of original image
            FileOutputStream fos = new FileOutputStream(closetFile);                    // pours data inside of copy image
            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            is.close();
            fos.close();

            // updates SQL DB with real path now (instead of "pending")
            dbManager.completeDollInitialSave(newId, fileName);

            // shows text at the bottom o a window
            Toast.makeText(this, "Saved to Closet!", Toast.LENGTH_SHORT).show();

            // CLOSES add doll window AUTOMATICALLY
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }
}