package com.example.dollcollectionandroid;

import android.os.Bundle;
import android.text.InputFilter;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.dollcollectionandroid.model.Doll;
import java.io.File;

public class DollDetailActivity extends AppCompatActivity {
    // Use TextField (EditText) for everything you want the user to be able to edit
    private EditText detailName, hintField, descriptionArea, brandField, modelField, yearField;
    private EditText birthDateField, birthTimeField;
    private AutoCompleteTextView birthPlaceField;
    private java.util.Calendar birthCalendar = java.util.Calendar.getInstance();
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
        detailImage = findViewById(R.id.detailImage);

        detailName = findViewById(R.id.detailName);
        brandField = findViewById(R.id.brandField);
        modelField = findViewById(R.id.modelField);
        descriptionArea = findViewById(R.id.descriptionArea);
        yearField = findViewById(R.id.yearField);
        hintField = findViewById(R.id.hintField);

        // the date and time and city complex inputs are received here
        birthDateField = findViewById(R.id.birthDateField);
        birthTimeField = findViewById(R.id.birthTimeField);
        birthPlaceField = findViewById(R.id.birthPlaceField);
        birthDateField.setOnClickListener(v -> showDateSpinner());    // 3 scrolls of date spinner
        birthTimeField.setOnClickListener(v -> showTimeSpinner());    // 2 scrolls time spinner
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

    // this method shows three spinner of DatePicker Class
    private void showDateSpinner() {
        // Get current values from the calendar
        int year = birthCalendar.get(java.util.Calendar.YEAR);
        int month = birthCalendar.get(java.util.Calendar.MONTH);
        int day = birthCalendar.get(java.util.Calendar.DAY_OF_MONTH);

        // Force the Locale to UK just for this dialog to get Day-Month-Year wheel order
        java.util.Locale.setDefault(java.util.Locale.UK);

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    birthCalendar.set(selectedYear, selectedMonth, selectedDay);

                    // %02d ensures "07/09" instead of "7/9"
                    String dateStr = String.format("%02d/%02d/%d", selectedDay, (selectedMonth + 1), selectedYear);
                    birthDateField.setText(dateStr);
                },
                year, month, day
        );

        // This removes the "Header" date that sometimes messes up the look
        datePickerDialog.setTitle("Select Birth Date");
        datePickerDialog.show();
    }

    // this method shows two spinner (24-hour format) of TimePicker Class
    private void showTimeSpinner() {
        new android.app.TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                (view, hourOfDay, minute) -> {
                    birthCalendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
                    birthCalendar.set(java.util.Calendar.MINUTE, minute);

                    String timeStr = String.format("%02d:%02d", hourOfDay, minute);
                    birthTimeField.setText(timeStr);
                },
                birthCalendar.get(java.util.Calendar.HOUR_OF_DAY),
                birthCalendar.get(java.util.Calendar.MINUTE),
                true    // THIS PART FORCES the 24-HOUR FORMAT (2 SCROLLS)
        ).show();
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
            yearField.setText(String.valueOf(currentDoll.getYear()));    // Convert int to String for the UI

            // Load the new Natal Chart fields into the UI
            birthDateField.setText(currentDoll.getBirthDate());
            birthTimeField.setText(currentDoll.getBirthTime());
            birthPlaceField.setText(currentDoll.getBirthCity());
            // Latitude and Longitude are hidden/internal for now

            // Again we use path of folder closet to load Doll image in detail window
            File closetFolder = new File(getFilesDir(), "closet");
            File imgFile = new File(closetFolder, currentDoll.getImagePath());
            if (imgFile.exists()) {
                Glide.with(this)
                        .load(imgFile)
                        .fitCenter()           // show the whole doll without cutting/cropping
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(detailImage);    // fixing weird rotation with Glide Class
            } else {
                // If file is missing, clear the view so you don't see a previous doll
                Glide.with(this).clear(detailImage);
            }
        }
    }

    // Method that saves fields of detail window
    private void handleSaveDescription() {
        // 1. Collect data from UI
        String newName = detailName.getText().toString();
        String newBrand = brandField.getText().toString();
        String newModel = modelField.getText().toString();
        String newDesc = descriptionArea.getText().toString();
        // Convert year text to number safely
        int newYear = 0;
        try {
            newYear = Integer.parseInt(yearField.getText().toString());
        } catch (NumberFormatException e) {
            System.out.println("Year was not a number, defaulting to 0");
        }
        String newHint = hintField.getText().toString();
        String newDate = birthDateField.getText().toString();
        String newTime = birthTimeField.getText().toString();
        String newCity = birthPlaceField.getText().toString();
        // keep old coordinates for now, until we add API!!!!
        double currentLat = currentDoll.getLatitude();     // keep old coordinates for now, until we add API!!!!
        double currentLon = currentDoll.getLongitude();    // keep old coordinates for now, until we add API!!!!

        // 2. Use setters to set variables of Java Doll Object in memory
        currentDoll.setName(newName);
        currentDoll.setBrand(newBrand);
        currentDoll.setModel(newModel);
        currentDoll.setDescription(newDesc);
        currentDoll.setYear(newYear);
        currentDoll.setHint(newHint);
        currentDoll.setBirthDate(newDate);
        currentDoll.setBirthTime(newTime);
        currentDoll.setBirthCity(newCity);

        // 3. Use DataBaseManager to save every change with SQL to closet.db
        dbManager.updateFullDollDetails(
                currentDoll.getId(),           // redundant but still
                currentDoll.getImagePath(),    // redundant but still
                newName,
                newBrand,
                newModel,
                newDesc,
                newYear,
                newHint,
                newDate,
                newTime,
                newCity,
                currentLat,
                currentLon
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
        // firstly point to folder 'closet', then get image path
        File closetFolder = new File(getFilesDir(), "closet");
        File fileToDelete = new File(closetFolder, currentDoll.getImagePath());

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