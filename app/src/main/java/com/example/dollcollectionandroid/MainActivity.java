package com.example.dollcollectionandroid;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

// [GEMINI ADDITION: MainActivity now controls the Sidebar and Screen Swapping]
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    // ADDED: The manager variable so the switch works [cite: 2026-02-22]
    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Setup the Toolbar (Replaces your JavaFX top bar)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // INITIALIZE DATABASE [cite: 2026-02-22]
        dbManager = new DatabaseManager(this);

        // ============================================================
        // THE SAFETY SWITCH
        // Uncomment to wipe, Comment to save. [cite: 2026-02-22]
        // ============================================================
        // dbManager.fullWipeOut();

        // 2. Setup the Sidebar (Drawer)
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 3. The "Hamburger" icon (three lines) to open the menu
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 4. DEFAULT SCREEN: Show the collection list first when app opens
        if (savedInstanceState == null) {
            // [GEMINI NOTE: We will create CollectionFragment next]
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CollectionFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_collection);
        }
    }

    // [GEMINI ADDITION: This handles the Sidebar clicks]
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_collection) {
            // Logic to show your ListView/RecyclerView
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CollectionFragment()).commit();
            System.out.println("Switching to Collection View");
        } else if (id == R.id.nav_add_doll) {
            // Logic to show your "Add Doll" form (onPickFileClick / onSaveClick)
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AddDollFragment()).commit();
            System.out.println("Switching to Add Doll View");
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // [GEMINI ADDITION: Fixed return type to boolean to match AppCompatActivity]
    @Override
    public boolean onSupportNavigateUp() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else {
            return super.onSupportNavigateUp();
        }
    }
}