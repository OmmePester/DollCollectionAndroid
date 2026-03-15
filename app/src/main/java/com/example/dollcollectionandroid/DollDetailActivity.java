package com.example.dollcollectionandroid;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.dollcollectionandroid.model.Doll;
import java.io.File;
import java.util.Calendar;

/**
 * This Class shows Doll detail screen for selected Doll from our collection.
 * It handles loading and displaying existing Doll data, formatting user input, saving edits to SQL DB,
 * and executing a secure, fool-proof deletion process to prevent accidental data loss.
 */

public class DollDetailActivity extends AppCompatActivity {
    // VARIABLES
    private ImageView detailImage;
    private EditText detailName, brandField, modelField, yearField, descriptionArea;
    private EditText genderField, birthDateField, birthTimeField;
    private Calendar birthCalendar = Calendar.getInstance();
    private DatabaseManager dbManager;
    private int dollId;
    private Doll currentDoll;

    // this startup method initializes DollDetailActivity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // starts standard lifecycle
        super.onCreate(savedInstanceState);
        // connects XML file with this activity
        setContentView(R.layout.activity_doll_detail);
        // instantiates DatabaseManager for SQL DB operations, passes this activity Context
        dbManager = new DatabaseManager(this);
        // gets ID of selected Doll, using Intent created in DollAdapter
        dollId = getIntent().getIntExtra("DOLL_ID", -1);
        // calls necessary methods of this class
        initViews();          // sets up UI of this activity
        setupFormatters();    // applies text field restrictions
        loadDollData();       // loads Doll's data
        // SAVE BUTTON: listens to clicks and calls handleSaveDescription() method
        findViewById(R.id.btnSave).setOnClickListener(v -> handleSaveDescription());
        // CLEAR FIELDS BUTTON: listens to clicks and calls handleClearAllFields() method
        findViewById(R.id.btnClearFields).setOnClickListener(v -> handleClearAllFields());
        // DELETE BUTTON: listens to clicks and calls handleDeleteDoll() method
        findViewById(R.id.btnDelete).setOnClickListener(v -> handleDeleteDoll());
    }

    // this method initializes Views/Fields and implements Field clearing
    private void initViews() {
        // binds variables with XML elements (ImageView, EditText) by ID
        detailImage = findViewById(R.id.detailImage);
        detailName = findViewById(R.id.detailName);
        brandField = findViewById(R.id.brandField);
        modelField = findViewById(R.id.modelField);
        yearField = findViewById(R.id.yearField);
        descriptionArea = findViewById(R.id.descriptionArea);
        genderField = findViewById(R.id.genderField);
        birthDateField = findViewById(R.id.birthDateField);
        birthTimeField = findViewById(R.id.birthTimeField);
        // listens to clicks and runs corresponding spinner methods
        birthDateField.setOnClickListener(v -> showDateSpinner());    // 3 scrolls of date spinner
        birthTimeField.setOnClickListener(v -> showTimeSpinner());    // 2 scrolls time spinner
        // listens and clears the field entirely when it is clicked
        View.OnFocusChangeListener clearOnFocus = (v, hasFocus) -> {
            if (hasFocus && v instanceof EditText) {
                ((EditText) v).setText("");    // if hasFocus is true, set EditText to ""
            }
        };
        // applies previously created clearing behavior (behavior parametrization)
        detailName.setOnFocusChangeListener(clearOnFocus);
        brandField.setOnFocusChangeListener(clearOnFocus);
        modelField.setOnFocusChangeListener(clearOnFocus);
        yearField.setOnFocusChangeListener(clearOnFocus);
        // do NOT apply this to descriptionArea, we can lose long notes by accident
        genderField.setOnFocusChangeListener(clearOnFocus);
        // do NOT apply this to birthDateField/birthTimeField as well
    }

    // this method handles FORMATTING, change according to user demand
    private void setupFormatters() {
//        // creates formatter for letters and spaces only [a-zA-Z\s]
//        InputFilter letterFilter = (source, start, end, dest, dstart, dend) -> {
//            for (int i = start; i < end; i++) {
//                // If it's not a letter and not a space, return empty string
//                if (!Character.isLetter(source.charAt(i)) && !Character.isSpaceChar(source.charAt(i))) {
//                    return "";
//                }
//            }
//            return null;    // this actually accepts entered input
//        };
//        // applies letter filter to Name, Brand, and Model, comment if not needed!!!!
////        detailName.setFilters(new InputFilter[]{letterFilter});
////        brandField.setFilters(new InputFilter[]{letterFilter});
////        modelField.setFilters(new InputFilter[]{letterFilter});
        // creates formatter for numbers (max 4 digits for YEAR)
        yearField.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(4),    // max 4 char for Year Field
                (source, start, end, dest, dstart, dend) -> {
                    for (int i = start; i < end; i++) {
                        if (!Character.isDigit(source.charAt(i))) return "";
                    }
                    return null;    // this actually accepts entered input
                }
        });
    }

    // this method shows three spinners of DatePickerDialog
    private void showDateSpinner() {
        // gets current values from the calendar related variables
        int year = birthCalendar.get(Calendar.YEAR);
        int month = birthCalendar.get(Calendar.MONTH);
        int day = birthCalendar.get(Calendar.DAY_OF_MONTH);
        // forces location to UK just for now
        java.util.Locale.setDefault(java.util.Locale.UK);
        // instantiates DatePickerDialog, passes Context with other necessary parameters
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    birthCalendar.set(selectedYear, selectedMonth, selectedDay);
                    // formats Strings, %02d ensures "07/07" instead of "7/7"
                    String dateStr = String.format("%02d/%02d/%d", selectedDay, (selectedMonth + 1), selectedYear);
                    birthDateField.setText(dateStr);
                },
                year, month, day
        );
        datePickerDialog.setTitle("Select Date");    // sets title for dialog
        datePickerDialog.show();                     // runs this dialog object
    }

    // this method shows two spinner (24-hour format) of TimePickerDialog
    private void showTimeSpinner() {
        // instantiates TimePickerDialog, passes Context with other necessary parameters
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                (view, hourOfDay, minute) -> {
                    birthCalendar.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
                    birthCalendar.set(java.util.Calendar.MINUTE, minute);
                    // formats Strings, %02d ensures "07/07" instead of "7/7"
                    String timeStr = String.format("%02d:%02d", hourOfDay, minute);
                    birthTimeField.setText(timeStr);
                },
                birthCalendar.get(java.util.Calendar.HOUR_OF_DAY),
                birthCalendar.get(java.util.Calendar.MINUTE),
                true    // THIS PART FORCES the 24-HOUR FORMAT!!!!
        );
        timePickerDialog.setTitle("Select Time");    // sets title for dialog
        timePickerDialog.show();                     // runs this dialog object
    }

    // this method loads Doll using its ID, then gets all data from already ready Doll
    private void loadDollData() {
        // asks for Doll from DatabaseManager using its ID
        currentDoll = dbManager.getDollById(dollId);
        if (currentDoll != null) {
            // fills all fields from the current Doll object
            detailName.setText(currentDoll.getName());
            brandField.setText(currentDoll.getBrand());
            modelField.setText(currentDoll.getModel());
            yearField.setText(String.valueOf(currentDoll.getYear()));    // Convert int to String for the UI
            descriptionArea.setText(currentDoll.getDescription());
            genderField.setText(currentDoll.getGender());
            birthDateField.setText(currentDoll.getBirthDate());
            birthTimeField.setText(currentDoll.getBirthTime());
            // locates path to current Doll image from hidden folder
            File imgFile = new File(StorageHelper.getHiddenFolder(), "closet/" + currentDoll.getImagePath());
            if (imgFile.exists()) {
                Glide.with(this)
                        .load(imgFile)
                        .fitCenter()           // shows the whole doll, centered, without cutting/cropping
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(detailImage);    // fixing weird rotation with Glide Class
            } else {
                // if file is missing, clear the view
                Glide.with(this).clear(detailImage);
            }
        }
    }

    // this method saves field changes of DollDetailActivity window into SQL DB
    private void handleSaveDescription() {
        // collects data from UI
        String newName = detailName.getText().toString();
        String newBrand = brandField.getText().toString();
        String newModel = modelField.getText().toString();
        String newDesc = descriptionArea.getText().toString();
        int newYear = 0;
        try {
            // converts year input from String to int
            newYear = Integer.parseInt(yearField.getText().toString());
        } catch (NumberFormatException ignored) {
        }
        String newGender = genderField.getText().toString();
        String newDate = birthDateField.getText().toString();
        String newTime = birthTimeField.getText().toString();
        // uses setters to set variables of current Doll
        currentDoll.setName(newName);
        currentDoll.setBrand(newBrand);
        currentDoll.setModel(newModel);
        currentDoll.setYear(newYear);
        currentDoll.setDescription(newDesc);
        currentDoll.setGender(newGender);
        currentDoll.setBirthDate(newDate);
        currentDoll.setBirthTime(newTime);
        // uses DataBaseManager to save changes into closet.db
        dbManager.updateFullDollDetails(
                currentDoll.getId(),           // redundant but still
                currentDoll.getImagePath(),    // redundant but still
                newName,
                newBrand,
                newModel,
                newYear,
                newDesc,
                newGender,
                newDate,
                newTime
        );
        // displays small message at the bottom
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    // this method CLEARS ALL fields of DollDetailActivity window
    private void handleClearAllFields() {
        // sets text String to "", effectively clearing Fields
        detailName.setText("");
        brandField.setText("");
        modelField.setText("");
        yearField.setText("");
        descriptionArea.setText("");
        genderField.setText("");
        birthDateField.setText("");    // also these
        birthTimeField.setText("");    // also these
    }

    // Doll Deleting Method (Step 1), initial selection, confirming, redirecting to security check
    private void handleDeleteDoll() {
        // instantiates dialog Builder object and sets necessary fields
        new AlertDialog.Builder(this)
                .setTitle("Confirm Murder")
                .setMessage("Are you sure you want to KILL " + currentDoll.getName() + "????\nThis action is EXTREMELY UNETHICAL!!!!")
                .setPositiveButton("OK", (dialog, which) -> triggerSecurityChallenge()) // redirecting to security check
                .setNegativeButton("Cancel", (dialog, which) -> System.out.println("Murder Avoided"))
                .show();
    }

    // Doll Deleting Method (Step 2), security code and final confirmation
    private void triggerSecurityChallenge() {
        // generates random 6-digit code String that need to be repeated
        String securityCode = String.valueOf(new java.util.Random().nextInt(900000) + 100000);

        // inflates XML layout instead of coding it in Java
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_security, null);
        final EditText input = dialogView.findViewById(R.id.securityInput);

        // creates htmlMessage, uses <br> for lines and <b> for bold
        String htmlMessage = "To finalize homicide, enter this code:<br><br><b>" + securityCode + "</b>";

        // prepares popup window and popup htmlMessage
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Final Security Protocol")
                .setMessage(Html.fromHtml(htmlMessage, Html.FROM_HTML_MODE_LEGACY))
                .setView(dialogView)    // sets  XML layout as a view
                .setPositiveButton("Confirm", (d, which) -> {
                    // verifies entered input
                    if (input.getText().toString().equals(securityCode)) {
                        // ACTUALLY CALLS METHOD to terminateDoll()
                        terminateDoll();
                    } else {
                        Toast.makeText(this, "Wrong code. Execution failed.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        // runs popup window, shows htmlMessage
        dialog.show();

        // centers AlertDialog message
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setGravity(Gravity.CENTER);
        }
    }

    // Doll Deleting Method (Step 3), actually deleting from everywhere
    private void terminateDoll() {
        // deletes Doll and all its data in SQL by unique ID
        dbManager.deleteDollById(currentDoll.getId());

        // lastly CLOSE detail window AUTOMATICALLY
        finish();
    }
}