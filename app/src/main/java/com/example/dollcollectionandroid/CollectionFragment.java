package com.example.dollcollectionandroid;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dollcollectionandroid.model.Doll;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollectionFragment extends Fragment {

    private RecyclerView recyclerView;
    private DollAdapter adapter;
    private DatabaseManager dbManager;
    private List<Doll> allDolls;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection, container, false);

        // 1. Initialize Database and List
        dbManager = new DatabaseManager(getActivity());
        recyclerView = view.findViewById(R.id.dollRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        Button filterButton = view.findViewById(R.id.filterButton);
        FloatingActionButton addFab = view.findViewById(R.id.addDollFab);

        // 2. Get all dolls from the 'closet' database [cite: 2026-02-22]
        allDolls = dbManager.getAllDolls();

        // 3. Connect the data to the UI using the Adapter
        adapter = new DollAdapter(new ArrayList<>(allDolls));
        recyclerView.setAdapter(adapter);

        // THE FILTER SUGGESTION LOGIC [cite: 2026-02-22]
        filterButton.setOnClickListener(v -> showFilterDialog());

        // PLUS BUTTON: Swaps to Add Doll Screen [cite: 2026-02-22]
        addFab.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new AddDollFragment()).commit();
        });

        return view;
    }

    private void showFilterDialog() {
        // We collect unique brands to suggest them in the pop-up [cite: 2026-02-22]
        Set<String> suggestions = new HashSet<>();
        suggestions.add("Show All");

        for (Doll d : allDolls) {
            if (d.getBrand() != null && !d.getBrand().isEmpty()) {
                suggestions.add(d.getBrand());
            }
        }

        String[] options = suggestions.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Filter by Brand");
        builder.setItems(options, (dialog, which) -> {
            String selected = options[which];
            if (selected.equals("Show All")) {
                // it is important for list to refresh itself [cite: 2026-02-22]
                adapter.updateList(allDolls);
            } else {
                applyFilter(selected);
            }
        });
        builder.show();
    }

    private void applyFilter(String brandName) {
        List<Doll> filtered = new ArrayList<>();
        for (Doll doll : allDolls) {
            // Logic to match the suggested brand [cite: 2026-02-22]
            if (doll.getBrand() != null && doll.getBrand().equalsIgnoreCase(brandName)) {
                filtered.add(doll);
            }
        }
        // method to update list of Dolls and refresh it to view the addition [cite: 2026-02-22]
        adapter.updateList(filtered);
    }

    // Refresh the list whenever we switch back to this screen
    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            allDolls = dbManager.getAllDolls();
            adapter.updateList(allDolls);
        }
    }
}