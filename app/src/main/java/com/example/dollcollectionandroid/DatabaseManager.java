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

public class DatabaseManager extends SQLiteOpenHelper {

    // This tells Java where the file is (Android handles the path internally)
    private static final String DATABASE_NAME = "closet.db";
    // Version increased to 2 to add new columns (Date, Time, City)
    private static final int DATABASE_VERSION = 2;
    private Context myContext;

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.myContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // STRICT ORDER: ID, Path, Name, Brand, Model, Desc, Year, Hint, Date, Time, City, Lat, Long
        // ALL COLUMNS ARE NOT NULL
        String createTable = "CREATE TABLE items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "image_path TEXT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "brand TEXT NOT NULL, " +
                "model TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "year INTEGER NOT NULL, " +
                "hint TEXT NOT NULL, " +
                "birth_date TEXT NOT NULL, " +
                "birth_time TEXT NOT NULL, " +
                "birth_city TEXT NOT NULL, " +
                "latitude REAL NOT NULL, " +
                "longitude REAL NOT NULL)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS items");
        onCreate(db);
    }

    // This method inserts doll for the first time, to get int ID
    public int addDoll(String name, String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // the actual two values name and image_path
        values.put("image_path", path);
        values.put("name", name);
        // the other values that are initially entered as default values
        values.put("brand", "Unknown");
        values.put("model", "Generic");
        values.put("description", "");
        values.put("year", 0);
        values.put("hint", "");
        values.put("birth_date", "");
        values.put("birth_time", "");
        values.put("birth_city", "");
        values.put("latitude", 0.0);
        values.put("longitude", 0.0);

        long id = db.insert("items", null, values);
        return (int) id; // returns the ID (autoincrement)
    }

    // This saves the real path and hint without touching the Brand/Model defaults.
    public void completeDollInitialSave(int id, String fileName, String hint) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("image_path", fileName);
        values.put("hint", hint);

        db.update("items", values, "id = ?", new String[]{String.valueOf(id)});
    }

    // this method interacts with a Dolls by ID from SQL DB, runs in DollDetailActivity
    public Doll getDollById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Get all dolls from the 'closet' database [cite: 2026-02-22]
        Cursor cursor = db.query("items", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Doll doll = cursorToDoll(cursor);
            cursor.close();
            return doll;
        }
        return null;
    }

    // this method creates List of Dolls by loading them from SQL DB, runs in CollectionActivity
    public List<Doll> getAllDolls() {
        List<Doll> dolls = new ArrayList<>();
        String sql = "SELECT * FROM items";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor.moveToFirst()) {
            do {
                dolls.add(cursorToDoll(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return dolls;
    }

    // this is a HELPER method to MAP cursor to Doll objects
    private Doll cursorToDoll(Cursor cursor) {
        return new Doll(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("image_path")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getString(cursor.getColumnIndexOrThrow("brand")),
                cursor.getString(cursor.getColumnIndexOrThrow("model")),
                cursor.getString(cursor.getColumnIndexOrThrow("description")),
                cursor.getInt(cursor.getColumnIndexOrThrow("year")),
                cursor.getString(cursor.getColumnIndexOrThrow("hint")),
                cursor.getString(cursor.getColumnIndexOrThrow("birth_date")),
                cursor.getString(cursor.getColumnIndexOrThrow("birth_time")),
                cursor.getString(cursor.getColumnIndexOrThrow("birth_city")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"))
        );
    }

    // this method updates Doll variables, which runs in DollDetailActivity
    public void updateFullDollDetails(int id, String path, String name, String brand, String model, String desc, int year, String hint,
                                      String bDate, String bTime, String bCity, double lat, double lon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("image_path", path);
        values.put("name", name);
        values.put("brand", brand);
        values.put("model", model);
        values.put("description", desc);
        values.put("year", year);
        values.put("hint", hint);
        values.put("birth_date", bDate);
        values.put("birth_time", bTime);
        values.put("birth_city", bCity);
        values.put("latitude", lat);
        values.put("longitude", lon);

        db.update("items", values, "id = ?", new String[]{String.valueOf(id)});
    }

    // This method deleting Doll by ID
    public void deleteDollById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("items", "id = ?", new String[]{String.valueOf(id)});
    }




    // This method completely DELETES ALL DATA IN SQL AND FOLDER
    public void fullWipeOut() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("items", null, null); // deletes all data in rows
        db.delete("sqlite_sequence", "name='items'", null); // resets counter

        // 1. Clear the 'closet' subfolder
        java.io.File closetFolder = new java.io.File(myContext.getFilesDir(), "closet");
        if (closetFolder.exists()) {
            java.io.File[] files = closetFolder.listFiles();
            if (files != null) {
                for (java.io.File file : files) {
                    // only delete files that start with "doll_"
                    if (file.getName().startsWith("doll_")) {
                        file.delete();
                    }
                }
            }
        }

        // 2. Clear the root 'files' folder (one folder up) to remove "ghost" files
        java.io.File rootFolder = myContext.getFilesDir();
        java.io.File[] rootFiles = rootFolder.listFiles();
        if (rootFiles != null) {
            for (java.io.File file : rootFiles) {
                // only delete files that start with "doll_"
                if (file.getName().startsWith("doll_")) {
                    file.delete();
                }
            }
        }
    }
}