package com.example.dollcollectionandroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.dollcollectionandroid.model.Doll;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager extends SQLiteOpenHelper {

    // This tells Java where the file is (Android handles the path internally)
    private static final String DATABASE_NAME = "closet.db";
    private static final int DATABASE_VERSION = 1;

    private Context myContext;

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.myContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // now we have this, instead of manually creating db
        String createTable = "CREATE TABLE items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "image_path TEXT NOT NULL, " +
                "hint TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "brand TEXT NOT NULL, " +
                "model TEXT NOT NULL, " +
                "year INTEGER NOT NULL)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS items");
        onCreate(db);
    }

    // This method adds doll with its path into sql db, return int ID
    public int addDoll(String name, String path) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("image_path", path);

        values.put("brand", "Unknown");
        values.put("model", "Generic");
        values.put("year", 0);
        values.put("hint", "");
        values.put("description", "");

        long id = db.insert("items", null, values);
        return (int) id; // returns the ID (autoincrement)
    }

    // This method updates the path once the file is renamed to "doll_ID.jpg"
    public void updateImagePath(int id, String fileName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("image_path", fileName);

        db.update("items", values, "id = ?", new String[]{String.valueOf(id)});
    }

    // Deleting Dolls by ID
    public void deleteDollById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("items", "id = ?", new String[]{String.valueOf(id)});
    }

    public List<Doll> getAllDolls() {
        List<Doll> dolls = new ArrayList<>();
        String sql = "SELECT * FROM items";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor rs = db.rawQuery(sql, null);

        if (rs.moveToFirst()) {
            do {
                int id = rs.getInt(rs.getColumnIndexOrThrow("id"));
                String imagePath = rs.getString(rs.getColumnIndexOrThrow("image_path"));
                String name = rs.getString(rs.getColumnIndexOrThrow("name"));
                String hint = rs.getString(rs.getColumnIndexOrThrow("hint"));
                String description = rs.getString(rs.getColumnIndexOrThrow("description"));
                String brand = rs.getString(rs.getColumnIndexOrThrow("brand"));
                String model = rs.getString(rs.getColumnIndexOrThrow("model"));
                int year = rs.getInt(rs.getColumnIndexOrThrow("year"));

                // FIXED ORDER: Matches (id, imagePath, name, hint, description, brand, model, year)
                dolls.add(new Doll(id, imagePath, name, hint, description, brand, model, year));
            } while (rs.moveToNext());
        }
        rs.close();
        return dolls;
    }

    public void updateFullDollDetails(int id, String name, String hint, String description, String brand, String model, int year) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("hint", hint);
        values.put("description", description);
        values.put("brand", brand);
        values.put("model", model);
        values.put("year", year); // It is YEAR not price

        db.update("items", values, "id = ?", new String[]{String.valueOf(id)});
    }

    // This method completely DELETES ALL DATA IN SQL AND FOLDER
    public void fullWipeOut() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("items", null, null); // deletes all data in rows
        db.delete("sqlite_sequence", "name='items'", null); // resets counter

        // Use myContext which we saved in the constructor
        java.io.File closetFolder = myContext.getFilesDir();
        java.io.File[] files = closetFolder.listFiles();

        if (files != null) {
            for (java.io.File file : files) {
                if (file.getName().startsWith("doll_")) {
                    file.delete();
                }
            }
        }
    }

    // Must be public so DollDetailActivity can see it! [cite: 2026-02-22]
    public Doll getDollById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Get all dolls from the 'closet' database [cite: 2026-02-22]
        Cursor cursor = db.query("items", null, "id=?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // FIXED ORDER: Matches constructor (id, imagePath, name, hint, description, brand, model, year)
            Doll doll = new Doll(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("image_path")),
                    cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("hint")),
                    cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    cursor.getString(cursor.getColumnIndexOrThrow("brand")),
                    cursor.getString(cursor.getColumnIndexOrThrow("model")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("year"))
            );
            cursor.close();
            return doll;
        }
        return null;
    }
}