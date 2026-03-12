package com.example.dollcollectionandroid;

import android.content.Context;
import java.io.File;
import java.io.IOException;

/**
 * Utility class that manages the physical storage locations for the 'closet' project.
 * It handles the creation of the hidden '.closetDollUp' vault, manages sub-folders
 * like 'closet' for Doll images, and provides absolute path for the 'closet.db' file.
 */

public class StorageHelper {

    // VARIABLES
    private static final String FOLDER_NAME = ".closetDollUp";
    private static final String SUB_FOLDER_NAME = "closet";
    private static final String DATABASE_NAME = "closet.db";
    private static Context appStaticContext;    // static variable to hold Context

    // this init method initializes CONTEXT for file system operations, due to Android's Context dependency
    public static void init(Context context) {
        appStaticContext = context.getApplicationContext();    // save parameter Context in this Class
    }

    // this creates the hidden vault on the phone's public root
    public static File getHiddenFolder() {
        // warning: call init, then other methods to do file system operations
        if (appStaticContext == null) {
            throw new IllegalStateException("StorageHelper not initialized! Call StorageHelper.init(context) first.");
        }
        // locates path for our folder, to create it in next steps
        // path is: /storage/emulated/0/Android/data/com.example.dollcollectionandroid/files/
        File folder = new File(appStaticContext.getExternalFilesDir(null), FOLDER_NAME);
        // checks and creates the ROOT folder '.closetDollUp', if it does not exist
        if (!folder.exists()) {
            folder.mkdirs();              // creates root folder '.closetDollUp'
            createNoMediaFile(folder);    // cloaks folder with '.nomedia'!!!!
        }
        // checks and creates the SUB folder 'closet'
        File subFolder = new File(folder, SUB_FOLDER_NAME);
        if (!subFolder.exists()) {
            subFolder.mkdirs();
        }
        return folder;
    }

    // this method returns path to 'closet.db' for DatabaseManager; obviously, also runs init(Context)
    public static String getDatabasePath(Context context) {
        // if not initialized yet, do it
        if (appStaticContext == null) {
            init(context);    // runs init(Context)
        }
        return new File(getHiddenFolder(), DATABASE_NAME).getAbsolutePath();
    }

    // this helper method CLOAKS a folder with '.nomedia', so Doll images are not shown in gallery
    private static void createNoMediaFile(File folder) {
        File noMedia = new File(folder, ".nomedia");     // locates path
        try {
            if (!noMedia.exists()) noMedia.createNewFile();    // creates file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}