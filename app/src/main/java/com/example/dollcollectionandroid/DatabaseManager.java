package com.example.dollcollectionandroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.dollcollectionandroid.model.Doll;

//import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This Class manages all processes related to our SQL DB 'closet.db'.
 * It extends SQLiteOpenHelper to be able to execute all essential commands:
 * CRUD (Create, Read, Update, Delete). It saves Doll data in 'closet.db', then
 * applies changes and/or deletes it according to user's command.
 */

public class DatabaseManager extends SQLiteOpenHelper {

    // VARIABLES
    // Version increased to 4 after adding new column "display_order"!!!!
    private static final int DATABASE_VERSION = 4;
    private Context myContext;

    // CONSTRUCTOR
    public DatabaseManager(Context context) {
        // calls StorageHelper to get the path to hidden '.closetDollUp' folder
        super(context, StorageHelper.getDatabasePath(context), null, DATABASE_VERSION);
        this.myContext = context; // Kept intact as requested!
    }

    @Override
    // this startup method initializes DatabaseManager
    public void onCreate(SQLiteDatabase db) {
        // STRICT ORDER: ID, Path, Name, Brand, Model, Year, Desc, Gender, Date, Time
        // ALL COLUMNS ARE NOT NULL
        String createTable = "CREATE TABLE items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "image_path TEXT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "brand TEXT NOT NULL, " +
                "model TEXT NOT NULL, " +
                "year INTEGER NOT NULL, " +
                "description TEXT NOT NULL, " +
                "gender TEXT NOT NULL, " +
                "birth_date TEXT NOT NULL, " +
                "birth_time TEXT NOT NULL, " +
                "display_order INTEGER NOT NULL)";    // latest addition
        db.execSQL(createTable);
    }

    @Override
    // this method handles database structure changes by updating it
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS items");
        onCreate(db);
    }

    // this method inserts Doll data (name and image path) for the first time, and returns unique ID
    public int addDoll(String name, String path) {
        // creates and opens SQL DB, for writing in it
        SQLiteDatabase db = this.getWritableDatabase();

        // instantiates and packs variables into ContentValues
        ContentValues values = new ContentValues();
        // the actual two values name and image_path
        values.put("image_path", path);
        values.put("name", name);
        // the other values that are initially entered as default values
        values.put("brand", "Unknown");
        values.put("model", "Generic");
        values.put("year", 0);
        values.put("description", "");
        values.put("gender", "");
        values.put("birth_date", "");
        values.put("birth_time", "");
        values.put("display_order", 0);    // latest addition

        // calls for, stores, and returns ID (autoincrement)
        long id = db.insert("items", null, values);
        return (int) id;
    }

    // this method completes Doll data save by updating Image Path with correct and final file name (based on autoincrement ID)
    public void completeDollInitialSave(int id, String fileName) {
        // creates and opens SQL DB, for writing in it
        SQLiteDatabase db = this.getWritableDatabase();

        // instantiates ContentValues and packs variables "image_path" and "display_order" into it
        ContentValues values = new ContentValues();
        values.put("image_path", fileName);
        values.put("display_order", id);    // latest addition

        // executes SQL UPDATE command on "items" table
        db.update("items", values, "id = ?", new String[]{String.valueOf(id)});
    }

    // this method interacts with a Dolls by ID from SQL DB, runs in DollDetailActivity
    public Doll getDollById(int id) {
        // creates and opens SQL DB, for reading from it
        SQLiteDatabase db = this.getReadableDatabase();

        // reads specific Doll by given ID from "items" table and saves it in Curser object
        Cursor cursor = db.query("items", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);

        // creates and returns Doll object, using cursorToDoll(Cursor) method
        if (cursor != null && cursor.moveToFirst()) {
            Doll doll = cursorToDoll(cursor);
            cursor.close();
            return doll;
        }
        return null;
    }

    // this method creates List of Dolls by loading them from SQL DB, runs in CollectionActivity (and SettingsActivity)
    public List<Doll> getAllDolls() {
        // creates empty List
        List<Doll> dolls = new ArrayList<>();

        // prepares SQL query to read everything, according to user's custom display order
        String sql = "SELECT * FROM items ORDER BY display_order ASC";

        // creates and opens SQL DB, for reading from it
        SQLiteDatabase db = this.getReadableDatabase();

        // runs query on our DB and saves in Curser object
        Cursor cursor = db.rawQuery(sql, null);

        // moves Curser one by one, adds all Dolls to List, and closes Curser
        if (cursor.moveToFirst()) {
            do {
                dolls.add(cursorToDoll(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dolls;
    }

    // this helper method maps cursor row data to newly instantiated Doll object's variables
    private Doll cursorToDoll(Cursor cursor) {
        return new Doll(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("image_path")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getString(cursor.getColumnIndexOrThrow("brand")),
                cursor.getString(cursor.getColumnIndexOrThrow("model")),
                cursor.getInt(cursor.getColumnIndexOrThrow("year")),
                cursor.getString(cursor.getColumnIndexOrThrow("description")),
                cursor.getString(cursor.getColumnIndexOrThrow("gender")),
                cursor.getString(cursor.getColumnIndexOrThrow("birth_date")),
                cursor.getString(cursor.getColumnIndexOrThrow("birth_time"))
        );
    }

    // this method updates Doll variables, it is called in DollDetailActivity
    public void updateFullDollDetails(int id, String path, String name, String brand, String model, int year, String desc,
                                      String gender, String bDate, String bTime) {
        // creates and opens SQL DB, for writing in it
        SQLiteDatabase db = this.getWritableDatabase();

        // instantiates and packs variables into ContentValues
        ContentValues values = new ContentValues();
        values.put("image_path", path);
        values.put("name", name);
        values.put("brand", brand);
        values.put("model", model);
        values.put("year", year);
        values.put("description", desc);
        values.put("gender", gender);
        values.put("birth_date", bDate);
        values.put("birth_time", bTime);

        // executes SQL UPDATE command on "items" table
        db.update("items", values, "id = ?", new String[]{String.valueOf(id)});
    }

    // this method deletes a Doll by its ID from our "items" table, it is called in DollDetailActivity
    public void deleteDollById(int id) {
        // creates and opens SQL DB
        SQLiteDatabase db = this.getWritableDatabase();

        // executes SQL DELETE command on "items" table
        db.delete("items", "id = ?", new String[]{String.valueOf(id)});
    }

    // this method saves user's custom Doll order in our SQL DB, it is called in CollectionActivity
    public void updateAllDollOrders(List<Doll> orderedDolls) {
        // creates and opens SQL DB
        SQLiteDatabase db = this.getWritableDatabase();

        // uses Transaction for performance, user can update a lot of Doll position
        db.beginTransaction();
        try {
            // instantiates and packs variables into ContentValues
            ContentValues values = new ContentValues();

            // loops Doll List, and sets List's index to "display_order", index range is [1:n]
            for (int i = 0; i < orderedDolls.size(); i++) {
                values.put("display_order", i + 1);    // makes range [1:n], to match autoincrement ID in SQL DB
                db.update("items", values, "id = ?", new String[]{String.valueOf(orderedDolls.get(i).getId())});
            }
            db.setTransactionSuccessful();
        } finally {
            // executes massive Transaction save operations
            db.endTransaction();
        }
    }
}