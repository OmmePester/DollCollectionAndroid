package com.example.dollcollectionandroid;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Data Packaging Engine for the DollUp Backup System.
 * It utilizes recursive folder traversal and buffered I/O streams.
 * This handles compressing database file (closet.db), image folder (closet),
 * and .nomedia file, which are inside hidden folder (.closetDollUp), into a single zipped file.
 */

public class ZipHelper {

    // this method zips our source folder into zip file
    public static void zipFolder(File sourceFolder, File outputFile) throws IOException {
        // FileStream (starts file stream) -> BufferStream (for speed) -> ZipStream (turns BYTES to ZIP)
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
            addFolderToZip(sourceFolder, sourceFolder.getName(), zos);    // runs recursive helper method
        }
    }

    // this recursive helper method adds folder and all its files/sub-folders into
    private static void addFolderToZip(File folder, String parentPath, ZipOutputStream zos) throws IOException {
        File[] files = folder.listFiles();
        if (files == null) return;    // if folder is empty, stop immediately to avoid crashes
        // traverses through files
        for (File file : files) {
            // creates path of a file to zip
            String zipEntryPath = parentPath + "/" + file.getName();
            // checks if a file is a folder
            if (file.isDirectory()) {
                // adds a sub-folder ('closet', image folder) recursively
                addFolderToZip(file, zipEntryPath, zos);
                continue;
            }
            // adds a file (like Doll images, closet.db, or .nomedia)
            try (FileInputStream fis = new FileInputStream(file);
                 // wraps File stream into Buffer stream for speed
                 BufferedInputStream bis = new BufferedInputStream(fis)) {
                // creates a header for our zip file
                ZipEntry entry = new ZipEntry(zipEntryPath);
                // signals about coming entry
                zos.putNextEntry(entry);
                byte[] buffer = new byte[1024];    // 1KB temporary data bucket
                int read;
                while ((read = bis.read(buffer)) != -1) {
                    zos.write(buffer, 0, read);
                }
                zos.closeEntry();
            }
        }
    }

    // this method extracts zipped file back where it belongs
    public static void unzip(File zipFile, File targetDirectory) throws IOException {
        // FileStream (starts file stream) -> BufferStream (for speed) -> ZipStream (turns ZIP to BYTES)
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry entry;    // holds currently extracted file data
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();    // gets internal path of unzipped file
                // Skip the top-level folder name to put contents directly in .closetDollUp
                if (name.contains("/")) {
                    // extracts just the name of a file, very important to avoid creation of unnecessary folders!
                    name = name.substring(name.indexOf("/") + 1);
                }
                // combines and locates correct path of file
                File file = new File(targetDirectory, name);
                // checks if it is a folder to create that folder later
                if (entry.isDirectory()) {
                    file.mkdirs();    // creates the folder
                } else {
                    file.getParentFile().mkdirs();
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                        byte[] buffer = new byte[1024];    // 1KB temporary data bucket
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            bos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }
}