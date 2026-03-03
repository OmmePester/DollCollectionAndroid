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


public class CollectionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DollAdapter adapter;
    private DatabaseManager dbManager;
    private List<Doll> allDolls;

    // [MY ADDITION: Variables to track current filter state, like your JavaFX MenuButton text]
    private EditText searchField;
    private Button filterButton; // Renamed from local to class variable to update text
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

    // this is a cascading suggestions like setupFilterMenu()]
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

    // this shows the specific unique values for the chosen category after showFilterDialogue()
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
        String newVal = searchField.getText().toString(); // Your variable name style [cite: 2026-03-01]
        List<Doll> filtered = new ArrayList<>();

        for (Doll doll : allDolls) {
            // --- YOUR SEARCH LOGIC --- [cite: 2026-03-01]
            boolean matchesSearch = true;
            if (newVal != null && newVal.length() >= 1) {
                matchesSearch = doll.getName().toLowerCase().startsWith(newVal.toLowerCase());
            }

            // --- MY FILTER LOGIC (Combined Brand and Model) --- [cite: 2026-03-01]
            boolean matchesBrand = currentBrandFilter.equals("All") ||
                    (doll.getBrand() != null && doll.getBrand().equals(currentBrandFilter));

            boolean matchesModel = currentModelFilter.equals("All") ||
                    (doll.getModel() != null && doll.getModel().equals(currentModelFilter));

            //
            if (matchesSearch && matchesBrand && matchesModel) {
                filtered.add(doll);
            }
        }

        // Update list and refresh view to see the changes [cite: 2026-02-22]
        adapter.updateList(filtered);
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