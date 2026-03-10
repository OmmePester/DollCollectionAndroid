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
 * Handles compressing the database and image folder into a single file.
 */
public class ZipHelper {

    /**
     * Zips a single source folder into an output zip file.
     */
    public static void zipFolder(File sourceFolder, File outputFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
            addFolderToZip(sourceFolder, sourceFolder.getName(), zos);
        }
    }

    private static void addFolderToZip(File folder, String parentPath, ZipOutputStream zos) throws IOException {
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            String zipEntryPath = parentPath + "/" + file.getName();

            if (file.isDirectory()) {
                // Recursively add sub-folders (like the 'closet' image folder)
                addFolderToZip(file, zipEntryPath, zos);
                continue;
            }

            // Add individual files (like closet.db or .nomedia)
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {

                ZipEntry entry = new ZipEntry(zipEntryPath);
                zos.putNextEntry(entry);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = bis.read(buffer)) != -1) {
                    zos.write(buffer, 0, read);
                }
                zos.closeEntry();
            }
        }
    }

    /**
     * Extracts a zip file back into the target directory.
     */
    public static void unzip(File zipFile, File targetDirectory) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                // Skip the top-level folder name to put contents directly in .closetDollUp
                if (name.contains("/")) {
                    name = name.substring(name.indexOf("/") + 1);
                }

                File file = new File(targetDirectory, name);
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    file.getParentFile().mkdirs();
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
                        byte[] buffer = new byte[1024];
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