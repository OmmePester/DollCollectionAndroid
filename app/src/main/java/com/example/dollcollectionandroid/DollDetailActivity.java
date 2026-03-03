package com.example.dollcollectionandroid;

import android.os.Bundle;
import android.text.InputFilter;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
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

    // this method handles FORMATTING, change according to user demand
    private void setupFormatters() {
        // Formatter for letters and spaces only (Matches [a-zA-Z\s]*)
        InputFilter letterFilter = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                // If it's not a letter and not a space, return empty string to block it [cite: 2026-03-01]
                if (!Character.isLetter(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
                    return "";
                }
            }
            return null; // Accept the input
        };

        // Apply letter filter to Name, Brand, and Model, comment if not needed!!!!
//        detailName.setFilters(new InputFilter[]{letterFilter});
        brandField.setFilters(new InputFilter[]{letterFilter});
        modelField.setFilters(new InputFilter[]{letterFilter});

        // Formatter for numbers (max 4 digits for YEAR)
        yearField.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(4), // Max 4 characters [cite: 2026-03-01]
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
                Glide.with(this)
                        .load(imgFile)
                        .fitCenter()           // show the whole doll without cutting/cropping
                        .into(detailImage);    // fixing weird rotation with Glide Class
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

    // Doll Deleting Method (Step 2) - Centered Edition [cite: 2026-03-01]
    private void triggerSecurityChallenge() {
        // Generate the random 6-digit code
        String securityCode = String.valueOf(new java.util.Random().nextInt(900000) + 100000);

        // 1. Setup the Input Field [cite: 2026-02-22]
        final EditText input = new EditText(this);
        input.setGravity(android.view.Gravity.CENTER); // Centers the typing cursor [cite: 2026-03-01]
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); // Opens number pad automatically [cite: 2026-03-01]

        // 2. Setup the Container with proper margins so the line isn't "ugly" [cite: 2026-03-01]
        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(80, 20, 80, 40); // Left, Top, Right, Bottom margins [cite: 2026-03-01]
        input.setLayoutParams(params);
        container.addView(input);

        // 3. Prepare the Styled Message (Bold + Spacing) [cite: 2026-03-01]
        // We use <br> for lines and <b> for bold because we are using Html.fromHtml next [cite: 2026-03-01]
        String htmlMessage = "To finalize homicide, enter this code:<br><br><b>" + securityCode + "</b>";

        // 4. Build and Show the Dialog [cite: 2026-03-01]
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Final Security Protocol")
                // This line converts the <b> tags into actual bold text on screen [cite: 2026-03-01]
                .setMessage(android.text.Html.fromHtml(htmlMessage, android.text.Html.FROM_HTML_MODE_LEGACY))
                .setView(container)
                .setPositiveButton("Confirm", (d, which) -> {
                    if (input.getText().toString().equals(securityCode)) {
                        terminateDoll(); // Move to Step 3 [cite: 2026-02-22]
                    } else {
                        Toast.makeText(this, "Wrong code. Execution failed.", Toast.LENGTH_SHORT).show();
                    }
                }).create();

        dialog.show();

        // 5. THE SECRET SAUCE: Force the Message TextView to center [cite: 2026-03-01]
        // This finds the internal ID that Android uses for the message body and centers it [cite: 2026-03-01]
        android.widget.TextView messageView = (android.widget.TextView) dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setGravity(android.view.Gravity.CENTER);
        }
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