package com.example.dollcollectionandroid;

import android.content.Context;
import android.os.Environment;
import java.io.File;
import java.io.IOException;

public class StorageHelper {

    private static final String FOLDER_NAME = ".closetDollUp";
    private static final String SUB_FOLDER_NAME = "closet";
    private static final String DATABASE_NAME = "closet.db";

    // This is the secret: A static variable to hold the context [cite: 2026-03-06]
    private static Context appStaticContext;

    // Call this ONCE in DatabaseManager
    public static void init(Context context) {
        appStaticContext = context.getApplicationContext();
    }

    // This creates the hidden vault on the phone's public root
    public static File getHiddenFolder() {

        if (appStaticContext == null) {
            throw new IllegalStateException("StorageHelper not initialized! Call StorageHelper.init(context) first.");
        }

        // Use the App-Specific External directory to bypass Android 11+ security crashes
        // path is: /storage/emulated/0/Android/data/com.example.dollcollectionandroid/files/
        File folder = new File(appStaticContext.getExternalFilesDir(null), FOLDER_NAME);

        // Check and Create the ROOT folder '.closetDollUp'
        if (!folder.exists()) {
            folder.mkdirs();
            createNoMediaFile(folder);    // Cloak the root with nomedia!!!!
        }

        // Check and Create the SUB-FOLDER 'closet'
        File subFolder = new File(folder, SUB_FOLDER_NAME);
        if (!subFolder.exists()) {
            subFolder.mkdirs();
        }

        return folder;
    }

    //
    public static String getDatabasePath(Context context) {
        // If we haven't initialized yet, do it now
        if (appStaticContext == null) {
            init(context);
        }
        return new File(getHiddenFolder(), DATABASE_NAME).getAbsolutePath();
    }

    // this "Cloaks" hidden folder with '.nomedia' so Doll images are not shown in gallery
    private static void createNoMediaFile(File folder) {
        File noMedia = new File(folder, ".nomedia");
        try {
            if (!noMedia.exists()) noMedia.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}