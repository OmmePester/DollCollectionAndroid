package com.example.dollcollectionandroid;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * This Class is a translator between Java and Drive API.
 * This Class handles the heavy lifting of uploading, searching for the
 * latest backup, and downloading files that are in the cloud.
 */

public class DriveServiceHelper {

    // VARIABLES
    private final Executor mExecutor = Executors.newSingleThreadExecutor();    // thread for background activity
    private final Drive mDriveService;

    // CONSTRUCTOR
    public DriveServiceHelper(Drive driveService) {
        this.mDriveService = driveService;
    }

    // this method UPLOADS files to 'appDataFolder' in Drive, which is a hidden area
    public Task<String> uploadFile(java.io.File localFile, String mimeType) {
        return Tasks.call(mExecutor, () -> {
            // 1. Creates the Metadata (file name and location on Drive)
            File metadata = new File()
                    .setParents(Collections.singletonList("appDataFolder"))
                    .setName(localFile.getName());
            // 2. Prepares the real content of the file
            FileContent content = new FileContent(mimeType, localFile);
            // 3. Executes the actual upload to Drive
            File googleFile = mDriveService.files().create(metadata, content).execute();
            // checks if Drive actually received our file
            if (googleFile == null) {
                throw new java.io.IOException("Null result when requesting file creation.");
            }
            // returns the unique Drive ID of a file
            return googleFile.getId();
        });
    }

    // this method FINDS LATEST BACKUP, and returns its unique Drive ID
    public Task<String> findLatestBackup() {
        return Tasks.call(mExecutor, () -> {
//            com.google.api.services.drive.model.FileList result = mDriveService.files().list()
            FileList result = mDriveService.files().list()        // starts search query
                    .setSpaces("appDataFolder")                   // tells to look only in hidden 'appDataFolder'
                    .setFields("files(id, name, createdTime)")    // asks for the most necessary info
                    .execute();                                   // runs the query
            // returns null if our search finds nothing
            if (result.getFiles().isEmpty()) return null;
            // returns unique ID of the first found backup file (usually the latest)
            return result.getFiles().get(0).getId();
        });
    }

    // this method DOWNLOADS files from Drive to local hidden folder '.closetDollUp'
    public Task<Void> downloadFile(String fileId, java.io.File targetFile) {
        return Tasks.call(mExecutor, () -> {
            // connects OutputStream to the targeted file, which as a result creates our file
            try (java.io.OutputStream outputStream = new java.io.FileOutputStream(targetFile)) {
                // DOWNLOADS file from Drive by uniqueID and pours it into OutputStream
                mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
                return null;
            }
        });
    }
}
