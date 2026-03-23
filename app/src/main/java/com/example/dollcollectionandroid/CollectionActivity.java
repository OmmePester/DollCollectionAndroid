package com.example.dollcollectionandroid;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dollcollectionandroid.model.Doll;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;    // for Streams
import java.util.Comparator;           // for Streams

/**
 * This Activity is a window where most activity happens.
 * It coordinates the extraction of data from DatabaseManager and passes
 * it to the DollAdapter, which uses that data for RecyclerView.
 * It implements efficient Search, Sort, and Filter mechanics on Doll List.
 * It listens to click and redirects to other activities (AddDollActivity/DollDetailActivity).
 */

public class CollectionActivity extends AppCompatActivity {

    // VARIABLES
    private DatabaseManager dbManager;    // DatabaseManager used only once to load List of Dolls
    private List<Doll> allDolls;          // the actual List of Doll objects
    private DollAdapter adapter;          // DollAdapter that handles Doll display, without constantly bothering DatabaseManager
    private RecyclerView recyclerView;    // UI elements that contains scrolling List of Dolls
    private EditText searchField;
    private Button sortButton, filterButton;
    private String currentSortMode = "None";      // sets initial sort type to "None"
    private String currentBrandFilter = "All";    // sets initial BrandFilter type to "All"
    private String currentModelFilter = "All";    // sets initial ModelFilter type to "All"
    private FloatingActionButton addButtonPlus;

    @Override
    // this startup method initializes CollectionActivity
    protected void onCreate(Bundle savedInstanceState) {
        // starts standard lifecycle
        super.onCreate(savedInstanceState);

        // connects XML file with this activity
        setContentView(R.layout.activity_collection);

        // instantiates DatabaseManager using Context and calls all Dolls to save into the List
        dbManager = new DatabaseManager(this);
        allDolls = dbManager.getAllDolls();

        // instantiates and loads DollAdapter with the List of all Dolls
        adapter = new DollAdapter(this, new ArrayList<>(allDolls));

        // finds IDs of XML elements (RecyclerView, TextField, Button)
        recyclerView = findViewById(R.id.dollRecyclerView);
        searchField = findViewById(R.id.searchField);
        sortButton = findViewById(R.id.sortButton);
        filterButton = findViewById(R.id.filterButton); // Link to class variable
        addButtonPlus = findViewById(R.id.addDollButtonPlus);

        // sets sizes of RecyclerView to be fixed (good memory optimization)
        recyclerView.setHasFixedSize(true);

        // connects Doll data to UI element, RecyclerView, by using DollAdapter
        recyclerView.setAdapter(adapter);

        // instantiates a drag-and-drop helper using important ItemTouchHelper Class
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(
                        ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
                    @Override
                    // checks if dragging a row is currently allowed
                    public int getMovementFlags(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder) {
                        // locks the Doll List, if user is sorting by anything other than "None"
                        if (!currentSortMode.equals("None")) {
                            return 0;
                        }
                        return super.getMovementFlags(recyclerView, viewHolder);
                    }

                    @Override
                    // drags a row up/down the screen, by repeatedly running as user's finger slide on screen
                    public boolean onMove(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, @androidx.annotation.NonNull RecyclerView.ViewHolder target) {
                        // VARIABLES: to hold the initial and final positions
                        int fromPosition = viewHolder.getAdapterPosition();
                        int toPosition = target.getAdapterPosition();

                        // calls helper swap method in DollAdapter to visually move row
                        adapter.moveItem(fromPosition, toPosition);
                        return true;
                    }

                    @Override
                    // this method override is required by OS, but we do not implement swipe-to-delete gestures, so block is empty
                    public void onSwiped(@androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    }

                    @Override
                    // finalizes the dragging, exactly when user lifts their finger and drops row into its final place
                    public void clearView(@androidx.annotation.NonNull RecyclerView recyclerView, @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder) {
                        // starts standard lifecycle
                        super.clearView(recyclerView, viewHolder);

                        // grabs new Doll List from DollAdapter and saves it to SQL DB
                        if (adapter != null) {
                            dbManager.updateAllDollOrders(adapter.getDollList());

                            // notifies adapter and instantly shows the new visible order number
                            //adapter.notifyDataSetChanged();
                        }
                    }
                });

        // attaches ItemTouchHelper helper we just built to our RecyclerView
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // searches Dolls in the List based on the entered input, via attaching complex Listener to search box
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            // this override is mandatory by OS
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            // this override is mandatory by OS
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // calls applyFilters() method to ensure Search + RecyclerView work together
                applyFilters();
            }
            @Override
            // this override is mandatory by OS
            public void afterTextChanged(Editable s) {}
        });

        // SORT Button: listens to clicks and runs showSortDialog() method
        sortButton.setOnClickListener(v -> showSortDialog());

        // FILTER Button: listens to clicks and runs showFilterDialog() method
        filterButton.setOnClickListener(v -> showFilterDialog());

        // ADD Button: listens to clicks and opens AddDollActivity window
        addButtonPlus.setOnClickListener(v -> {
            Intent intent = new Intent(CollectionActivity.this, AddDollActivity.class);
            startActivity(intent);
        });
    }

    // this helper method shows cascading dialog suggestions for SORTing
    private void showSortDialog() {
        // hardcodes SORTing options to show in dialog
        String[] options = {
                "None",
                "Name (A-Z)", "Name (Z-A)",
                "Date (Oldest)", "Date (Newest)",
                "Year (Oldest)", "Year (Newest)"
        };

        // builds SORTing pop up dialog window
        new AlertDialog.Builder(this)
                .setTitle("Sort By:")
                .setItems(options, (dialog, which) -> {
                    currentSortMode = options[which];
                    sortButton.setText("Sort: " + currentSortMode);
                    applyFilters();    // runs applyFilter(), which has both SORT and filter mechanics!!!!
                }).show();
    }

    // this helper method returns Comparator based on our sorting selection
    private Comparator<Doll> getOptimalComparator() {
        // checks for "None" selection and returns 0, which means they are equal
        if (currentSortMode.equals("None")) return (d1, d2) -> 0;

        // instantiates Comparator<Doll>
        Comparator<Doll> comp;

        // handles all cases of selection with corresponding Comparator
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
                // uses SimpleDateFormat object, which parses date String into Date object
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", java.util.Locale.UK);
                comp = Comparator.comparing(doll -> {
                    try { return sdf.parse(doll.getBirthDate()); }
                    catch (Exception e) { return new java.util.Date(0); }
                });
                if (currentSortMode.contains("Newest")) comp = comp.reversed();
                break;
            default:
                return (d1, d2) -> 0;
        }

        // uses thenComparing() method to apply alphabetical sort if there is a tie in primary sort
        return comp.thenComparing(Doll::getName, String.CASE_INSENSITIVE_ORDER);
    }

    // this helper method shows cascading dialog suggestions for FILTERing
    private void showFilterDialog() {
        // hardcodes FILTERing options to show in dialog
        String[] initialOptions = {"Brand", "Model", "Clear All Filters"};

        // builds FILTERing pop up first dialog window
        new AlertDialog.Builder(this)
                .setTitle("Filter By:")
                .setItems(initialOptions, (dialog, which) -> {
                    if (which == 0) {           // clicks "Brand"
                        showSubFilterDialog("Brand");
                    } else if (which == 1) {    // clicks "Model"
                        showSubFilterDialog("Model");
                    } else if (which == 2) {    // clicks "Clear"
                        filterButton.setText("Filter");
                        currentBrandFilter = "All";
                        currentModelFilter = "All";
                        applyFilters();         // runs applyFilter(), which has both sort and FILTER mechanics!!!!
                    }
                }).show();
    }

    // this helper method shows distinct values of chosen category from showFilterDialogue()
    private void showSubFilterDialog(String type) {
        // instantiates HashSet, which prevents duplicates
        Set<String> uniqueValues = new HashSet<>();

        // iterates through Doll List, and stores unique/distinct Brands/Models
        for (Doll d : allDolls) {
            if (type.equals("Brand") && d.getBrand() != null && !d.getBrand().isEmpty()) {
                uniqueValues.add(d.getBrand());
            } else if (type.equals("Model") && d.getModel() != null && !d.getModel().isEmpty()) {
                uniqueValues.add(d.getModel());
            }
        }

        // converts uniqueValues HashSet to List, then to primitive Array, because Builder need Array
        List<String> options = new ArrayList<>(uniqueValues);
        String[] optionsArray = options.toArray(new String[0]);

        // builds FILTERing pop up second dialog window
        new AlertDialog.Builder(this)
                .setTitle("Select " + type)
                .setItems(optionsArray, (dialog, which) -> {
                    // grabs filtering selections and updates Button text
                    String selected = optionsArray[which];
                    filterButton.setText(type + ": " + selected);

                    // selects specific Brand/Model and resets the other
                    if (type.equals("Brand")) {
                        currentBrandFilter = selected;
                        currentModelFilter = "All";    // resets Model, if we filter by Brand
                    } else {
                        currentModelFilter = selected;
                        currentBrandFilter = "All";    // resets Brand, if we filter by Model
                    }
                    applyFilters();    // runs applyFilter(), which has both SORT and FILTER mechanics!!!!
                }).show();
    }

    // this helper method handles search and/or filters on the Doll list
    private void applyFilters() {
        //grabs typed search text and stores as fixed lowercase String
        final String searchVal = searchField.getText().toString().toLowerCase();

        // streams Doll List and applies search/filters in one efficient Stream pipeline
        List<Doll> filtered = allDolls.stream()
                .filter(doll -> searchVal.isEmpty() || doll.getName().toLowerCase().startsWith(searchVal))
                .filter(doll -> currentBrandFilter.equals("All") || (doll.getBrand() != null && doll.getBrand().equals(currentBrandFilter)))
                .filter(doll -> currentModelFilter.equals("All") || (doll.getModel() != null && doll.getModel().equals(currentModelFilter)))
                .sorted(getOptimalComparator())
                .collect(Collectors.toList());

        // updates list and refresh view to see the changes [cite: 2026-02-22]
        adapter.updateList(filtered);
    }




    @Override
    // this method refreshed Doll List and RecyclerView after any changes, to List or to SQL DB
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            // updates Doll List with new changed data from SQL DB
            allDolls = dbManager.getAllDolls();

            // calls applyFilter() method, because it has updateList() method in it
            applyFilters();
        }
    }
}