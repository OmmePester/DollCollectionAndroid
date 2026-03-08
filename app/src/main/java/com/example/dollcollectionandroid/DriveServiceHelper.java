package com.example.dollcollectionandroid;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * The "Brain" for Google Drive Sync.
 * This class handles the heavy lifting of sending files to the cloud.
 */
public class DriveServiceHelper {

    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    // CONSTRUCTOR
    public DriveServiceHelper(Drive driveService) {
        this.mDriveService = driveService;
    }

    /**
     * Uploads a file to the user's Google Drive "appDataFolder".
     * This is a hidden area where users can't accidentally delete your doll data.
     */
    public Task<String> uploadFile(java.io.File localFile, String mimeType) {
        return Tasks.call(mExecutor, () -> {
            // 1. Create the Metadata (The file name and location on Drive)
            File metadata = new File()
                    .setParents(Collections.singletonList("appDataFolder"))
                    .setName(localFile.getName());

            // 2. Prepare the actual content of the file
            FileContent content = new FileContent(mimeType, localFile);

            // 3. Execute the upload to Google
            File googleFile = mDriveService.files().create(metadata, content).execute();

            if (googleFile == null) {
                throw new java.io.IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }
}
