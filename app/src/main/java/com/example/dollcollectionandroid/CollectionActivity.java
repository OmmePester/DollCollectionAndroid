package com.example.dollcollectionandroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dollcollectionandroid.model.Doll;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;    // for Streams
import java.util.Comparator;           // for Streams

/**
 *
 */

public class CollectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;    //
    private DollAdapter adapter;          // DollAdapter that handles Doll display, without bothering DatabaseManager
    private DatabaseManager dbManager;    // DatabaseManager used only onc to load List of Dolls
    private List<Doll> allDolls;          //

    // [MY ADDITION: Variables to track current filter state, like your JavaFX MenuButton text]
    private EditText searchField;
    private Button filterButton; // Renamed from local to class variable to update text
    private Button sortButton;
    private String currentSortMode = "None";    // sets initial sort type to none, and saves selected type here
    private String currentBrandFilter = "All";
    private String currentModelFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);

        // Enable the back arrow in the top bar to return to your Main Menu [cite: 2026-03-01]
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Closet");
        }

        // 1. Initialize Database and List
        dbManager = new DatabaseManager(this);
        recyclerView = findViewById(R.id.dollRecyclerView);

        // [LAG FIX: Set fixed size to true if your item heights never change] [cite: 2026-03-03]
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        filterButton = findViewById(R.id.filterButton); // Link to class variable
        sortButton = findViewById(R.id.sortButton);
        sortButton.setOnClickListener(v -> showSortDialog());    // Setup sort listener
        searchField = findViewById(R.id.searchField);
        FloatingActionButton addFab = findViewById(R.id.addDollFab);

        // 2. Get all dolls from the 'closet' database [cite: 2026-02-22]
        allDolls = dbManager.getAllDolls();

        // 3. Connect the data to the UI using the Adapter
        adapter = new DollAdapter(this, new ArrayList<>(allDolls));
        recyclerView.setAdapter(adapter);

        // [MY ADDITION: The SEARCH LOGIC (Matches your JavaFX textProperty listener)] [cite: 2026-03-01]
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // [MY ADDITION: We call applyFilters to ensure Search + Menu work together] [cite: 2026-03-01]
                applyFilters();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // THE FILTER SUGGESTION LOGIC [cite: 2026-02-22]
        filterButton.setOnClickListener(v -> showFilterDialog());

        // PLUS BUTTON: Redirects to Add Doll Activity window [cite: 2026-03-01]
        addFab.setOnClickListener(v -> {
            Intent intent = new Intent(CollectionActivity.this, AddDollActivity.class);
            startActivity(intent);
        });
    }

    // this method shows cascading dialog suggestions for filtering
    private void showFilterDialog() {
        String[] initialOptions = {"Brand", "Model", "Clear All Filters"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter By:");
        builder.setItems(initialOptions, (dialog, which) -> {
            if (which == 0) { // Clicked BRAND
                showSubFilterDialog("Brand");
            } else if (which == 1) { // Clicked MODEL
                showSubFilterDialog("Model");
            } else if (which == 2) { // Clicked CLEAR
                filterButton.setText("Filter");
                currentBrandFilter = "All";
                currentModelFilter = "All";
                applyFilters();
            }
        });
        builder.show();
    }

    // this method shows distinct values of chosen category from showFilterDialogue()
    private void showSubFilterDialog(String type) {
        Set<String> uniqueValues = new HashSet<>();
        for (Doll d : allDolls) {
            if (type.equals("Brand") && d.getBrand() != null && !d.getBrand().isEmpty()) {
                uniqueValues.add(d.getBrand());
            } else if (type.equals("Model") && d.getModel() != null && !d.getModel().isEmpty()) {
                uniqueValues.add(d.getModel());
            }
        }

        List<String> options = new ArrayList<>(uniqueValues);
        String[] optionsArray = options.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select " + type);
        builder.setItems(optionsArray, (dialog, which) -> {
            String selected = optionsArray[which];
            filterButton.setText(type + ": " + selected);

            if (type.equals("Brand")) {
                currentBrandFilter = selected;
                currentModelFilter = "All"; // Reset model if filtering by brand [cite: 2026-03-01]
            } else {
                currentModelFilter = selected;
                currentBrandFilter = "All"; // Reset brand if filtering by model [cite: 2026-03-01]
            }
            applyFilters();
        });
        builder.show();
    }

    // this is where search and/or filters actually do their work on the Doll list
    private void applyFilters() {
        final String searchVal = searchField.getText().toString().toLowerCase();

        // One-stop shop: Streams handle filtering and sorting in one efficient pipeline [cite: 2026-03-06]
        List<Doll> filtered = allDolls.stream()
                .filter(doll -> searchVal.isEmpty() || doll.getName().toLowerCase().startsWith(searchVal))
                .filter(doll -> currentBrandFilter.equals("All") || (doll.getBrand() != null && doll.getBrand().equals(currentBrandFilter)))
                .filter(doll -> currentModelFilter.equals("All") || (doll.getModel() != null && doll.getModel().equals(currentModelFilter)))
                .sorted(getOptimalComparator())
                .collect(Collectors.toList());

        // Update list and refresh view to see the changes [cite: 2026-02-22]
        adapter.updateList(filtered);
    }

    // this method shows cascading dialog suggestions for sorting
    private void showSortDialog() {
        String[] options = {
                "None",
                "Name (A-Z)", "Name (Z-A)",
                "Date (Oldest)", "Date (Newest)",
                "Year (Oldest)", "Year (Newest)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Sort By:")
                .setItems(options, (dialog, which) -> {
                    currentSortMode = options[which];
                    sortButton.setText("Sort: " + currentSortMode);
                    applyFilters();    // Re-apply everything with new sort
                }).show();
    }

    //this is helper method that gives Comparator based on our selection
    private Comparator<Doll> getOptimalComparator() {
        if (currentSortMode.equals("None")) return (d1, d2) -> 0;

        Comparator<Doll> comp;
        switch (currentSortMode) {
            case "Name (A-Z)":
                return Comparator.comparing(Doll::getName, String.CASE_INSENSITIVE_ORDER);
            case "Name (Z-A)":
                return Comparator.comparing(Doll::getName, String.CASE_INSENSITIVE_ORDER).reversed();
            case "Year (Oldest)":
                comp = Comparator.comparingInt(Doll::getYear);
                break;
            case "Year (Newest)":
                comp = Comparator.comparingInt(Doll::getYear).reversed();
                break;
            case "Date (Oldest)":
            case "Date (Newest)":
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.UK);
                comp = Comparator.comparing(doll -> {
                    try { return sdf.parse(doll.getBirthDate()); }
                    catch (Exception e) { return new java.util.Date(0); }
                });
                if (currentSortMode.contains("Newest")) comp = comp.reversed();
                break;
            default:
                return (d1, d2) -> 0;
        }
        // Always fallback to Alphabetical if primary sort (Date/Year) is a tie [cite: 2026-03-04]
        return comp.thenComparing(Doll::getName, String.CASE_INSENSITIVE_ORDER);
    }

    // This makes the top bar back arrow work [cite: 2026-03-01]
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Closes collection and returns to your Main Menu [cite: 2026-03-01]
        return true;
    }

    // Refresh the list whenever we switch back to this screen
    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            allDolls = dbManager.getAllDolls();
            // [FIX: Calling applyFilters instead of updateList(allDolls) so filters persist] [cite: 2026-03-01]
            applyFilters();
        }
    }
}