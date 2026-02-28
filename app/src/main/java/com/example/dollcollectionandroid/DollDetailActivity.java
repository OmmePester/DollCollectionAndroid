package com.example.dollcollectionandroid;

import android.os.Bundle;
import android.text.InputFilter;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dollcollectionandroid.model.Doll;
import java.io.File;
import android.graphics.BitmapFactory;

public class DollDetailActivity extends AppCompatActivity {
    // Use TextField (EditText) for everything you want the user to be able to edit
    private EditText detailName, hintField, descriptionArea, brandField, modelField, yearField;
    private ImageView detailImage;
    private DatabaseManager dbManager;
    private int dollId;
    private Doll currentDoll; // To match your JavaFX 'currentDoll' logic

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doll_detail);

        dbManager = new DatabaseManager(this);
        // Get the ID passed from the list
        dollId = getIntent().getIntExtra("DOLL_ID", -1);

        initViews();
        setupFormatters(); //
        loadDollData();

        // Method that saves fields of detail window
        findViewById(R.id.btnSave).setOnClickListener(v -> handleSaveDescription());

        // Method that clears fields of detail window
        findViewById(R.id.btnClearFields).setOnClickListener(v -> handleClearAllFields());

        // Doll Deleting Method (Step 1)
        findViewById(R.id.btnDelete).setOnClickListener(v -> handleDeleteDoll());
    }

    private void initViews() {
        //
        detailName = findViewById(R.id.detailName);
        hintField = findViewById(R.id.hintField);
        descriptionArea = findViewById(R.id.descriptionArea);
        brandField = findViewById(R.id.brandField);
        modelField = findViewById(R.id.modelField);
        yearField = findViewById(R.id.yearField);
        detailImage = findViewById(R.id.detailImage);
    }

    // This replaces your getLetterFormatter and getNumberFormatter
    private void setupFormatters() {
        // Formatter for letters and spaces only (Matches [a-zA-Z\s]*)
        InputFilter letterFilter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (!Character.isLetter(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                    return "";
                }
            }
            return null;
        };

        // Apply letter filter to Name, Brand, and Model
        detailName.setFilters(new InputFilter[]{letterFilter});
        brandField.setFilters(new InputFilter[]{letterFilter});
        modelField.setFilters(new InputFilter[]{letterFilter});

        // Formatter for numbers (max 4 digits) (Matches [0-9]* and length <= 4)
        yearField.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(4),
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isDigit(source.charAt(i))) return "";
                    }
                    return null;
                }
        });
    }

    private void loadDollData() {
        // Correct way: Ask the dbManager to get the doll by its ID
        currentDoll = dbManager.getDollById(dollId);

        if (currentDoll != null) {
            // Filling all fields from the currentDoll object
            detailName.setText(currentDoll.getName());
            hintField.setText(currentDoll.getHint());
            descriptionArea.setText(currentDoll.getDescription());
            brandField.setText(currentDoll.getBrand());
            modelField.setText(currentDoll.getModel());
            yearField.setText(String.valueOf(currentDoll.getYear())); // Convert int to String for the UI

            // Again we use path of folder closet to load Doll image in detail window
            File imgFile = new File(getFilesDir(), currentDoll.getImagePath());
            if (imgFile.exists()) {
                detailImage.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
            }
        }
    }

    // Method that saves fields of detail window
    private void handleSaveDescription() {
        // 1. Collect data from UI
        String newName = detailName.getText().toString();
        String newHint = hintField.getText().toString();
        String newDesc = descriptionArea.getText().toString();
        String newBrand = brandField.getText().toString();
        String newModel = modelField.getText().toString();

        // Convert year text to number safely
        int newYear = 0;
        try {
            newYear = Integer.parseInt(yearField.getText().toString());
        } catch (NumberFormatException e) {
            System.out.println("Year was not a number, defaulting to 0");
        }

        // 2. Update the Java Object in memory
        currentDoll.setName(newName);
        currentDoll.setHint(newHint);
        currentDoll.setDescription(newDesc);
        currentDoll.setBrand(newBrand);
        currentDoll.setModel(newModel);
        currentDoll.setYear(newYear);

        // 3. One single call to save everything to the closet.db
        dbManager.updateFullDollDetails(
                currentDoll.getId(),
                newName,
                newHint,
                newDesc,
                newBrand,
                newModel,
                newYear
        );

        System.out.println("Success: Entire profile updated in SQL.");
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    // Method that clears fields of detail window
    private void handleClearAllFields() {
        // Decide on what to clear (for now all except name)
        detailName.setText("");
        hintField.setText("");
        descriptionArea.setText("");
        brandField.setText("");
        modelField.setText("");
        yearField.setText("");

        System.out.println("Visual area cleared. Object remains unchanged until Save is clicked.");
    }

    // Doll Deleting Method (Step 1), initial selection, confirming, redirecting to security check
    private void handleDeleteDoll() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Murder")
                .setMessage("Are you sure you want to KILL " + currentDoll.getName() + "????\nThis action is EXTREMELY UNETHICAL!!!!")
                .setPositiveButton("OK", (dialog, which) -> triggerSecurityChallenge()) // redirecting to security check
                .setNegativeButton("Cancel", (dialog, which) -> System.out.println("Deletion cancelled at Step 1."))
                .show();
    }

    // Doll Deleting Method (Step 2), passing the 6-digit security check and verifying deletion
    private void triggerSecurityChallenge() {
        // Generate code
        String securityCode = String.valueOf(new java.util.Random().nextInt(900000) + 100000);
        final EditText input = new EditText(this);

        new AlertDialog.Builder(this)
                .setTitle("Final Security Protocol")
                .setMessage("To finalize homicide, enter this code: " + securityCode)
                .setView(input)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    if (input.getText().toString().equals(securityCode)) {
                        terminateDoll();
                    } else {
                        System.out.println("Wrong security code. Termination failed.");
                    }
                }).show();
    }

    // Doll Deleting Method (Step 3), actually deleting from everywhere
    private void terminateDoll() {
        // firstly delete IMAGE FILE from folder 'closet'
        File fileToDelete = new File(getFilesDir(), currentDoll.getImagePath());

        if (fileToDelete.exists()) {
            if (fileToDelete.delete()) {
                System.out.println("Physical image deleted: " + currentDoll.getImagePath());
            }
        }

        // then delete it in SQL with unique ID
        dbManager.deleteDollById(currentDoll.getId());

        // lastly CLOSE detail window
        finish();
    }
}