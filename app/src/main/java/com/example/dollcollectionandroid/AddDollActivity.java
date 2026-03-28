package com.example.dollcollectionandroid;

import android.annotation.SuppressLint;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.yalantis.ucrop.UCrop;    // added to handle image cropping with 3:4 ratio
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

    @Override
    // this method checks image selection, extracts URI, manipulates image with uCrop, and displays image with Glide
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // starts standard lifecycle
        super.onActivityResult(requestCode, resultCode, data);

        // picks an image from gallery and uses its URI on uCrop for cropping/zooming/rotating
        if (resultCode == RESULT_OK && requestCode == 1000 && data != null) {
            // locates URI of initial image version
            Uri sourceUri = data.getData();

            // creates temporary file to hold cropped version of an image
            Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "temp_crop.jpg"));

            // starts uCrop with a locked 3:4 aspect ratio
            UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(3, 4)
                    .start(this);
        }

        // finalizes uCrop processes and passes it to Glide for preview
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP && data != null) {
            // grabs  URI of final cropped image version
            selectedImageUri = UCrop.getOutput(data);

            // USES GLIDE FOR PREVIEW SO IT ROTATES CORRECTLY, and skip cache to prevent showing old photo
            Glide.with(this)
                    .load(selectedImageUri)
                    .fitCenter()
                    .skipMemoryCache(true)                        // prevents loading cache ghost image
                    .diskCacheStrategy(DiskCacheStrategy.NONE)    // prevents loading cache ghost image
                    .into(dollImageView);
        }

        // catches errors during cropping process
        if (resultCode == UCrop.RESULT_ERROR && data != null) {
            Throwable cropError = UCrop.getError(data);
            if (cropError != null) {
                cropError.printStackTrace();
                Toast.makeText(this, "Crop error: " + cropError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // this method saves image path and name to SQL DB, and copies image to local hidden folder
    @SuppressLint("ResourceType")
    private void saveDollToCloset() {
        // rejects saving process if there is NO NAME input (now image is added by default)
        if (nameInput.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please name your dear Doll!", Toast.LENGTH_SHORT).show();
            return;
        }

        // saves name from EditText to String variable
        String name = nameInput.getText().toString().trim();

        // saves name and image path ("pending" for now) to SQL DB, addDoll() sets other entries to default values
        int newId = dbManager.addDoll(name, "pending");

        // uses unique ID to create new image file name for image copy
        String fileName = "doll_" + newId + ".jpg";

        try {
            // creates 'closet' subfolder path in hidden root '.closetDollUp' folder if it doesn't exist
            File closetFolder = new File(StorageHelper.getHiddenFolder(), "closet");

            // creates file path inside 'closet' folder
            File closetFile = new File(closetFolder, fileName);

            // instantiates InputStream to SUCK DATA FROM the original file
            InputStream is;
            if (selectedImageUri != null) {
                // sucks data from gallery picked image file
                is = getContentResolver().openInputStream(selectedImageUri);
            } else {
                // suck data from default "dummy_doll_default" image
                is = getResources().openRawResource(R.drawable.dummy_doll_default);
            }

            // instantiates OutputStream to POUR DATA INSIDE a newly created copy file
            FileOutputStream fos = new FileOutputStream(closetFile);

            // copies byte-by-byte specified image to private 'closet' folder
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