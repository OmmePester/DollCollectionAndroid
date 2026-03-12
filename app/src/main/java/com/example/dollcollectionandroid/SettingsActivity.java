package com.example.dollcollectionandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dollcollectionandroid.model.Doll;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * The user interface controller for application configurations and data management.
 * This activity handles the "Cloud Backup" and "Restore" logic by coordinating
 * with ZipHelper for file packaging and Google Drive API for remote storage.
 * It acts as the bridge between the user's interaction and the background storage tasks.
 */

public class SettingsActivity extends AppCompatActivity {

    // VARIABLES
    private TextView tvStatus;
    private Button btnConnect;       // the SIGN-IN button
    private Button btnSyncNow;       // the UPLOAD button
    private Button btnRestoreNow;    // the DOWNLOAD button
    // variables for google drive API
    private GoogleSignInClient mGoogleSignInClient;
    private DriveServiceHelper mDriveServiceHelper;
    // listener for logging in, again for API
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    handleSignInResult(result.getData());
                } else {
                    tvStatus.setText("Status: Login Cancelled");
                }
            }
    );    // it is asynchronous, which keeps the rest of app responsive.

    // this method is architect.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    // standard lifecycle startup
        setContentView(R.layout.activity_settings);    // connects XML file with this activity
        // find IDs of XML elements (TextView, Button)
        tvStatus = findViewById(R.id.tvCloudStatus);
        btnConnect = findViewById(R.id.btnConnectGoogle);
        btnSyncNow = findViewById(R.id.btnSyncNow);
        btnRestoreNow = findViewById(R.id.btnRestoreNow);
        // disable upload/download buttons, until user logs in.
        btnSyncNow.setEnabled(false);
        btnRestoreNow.setEnabled(false);
        // configures google sign-in request
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()    // shows email based accounts, if exist
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))    // drive folder for this task
                .build();
        // configures google sign-in interface
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // checks if already signed in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            initializeDriveHelper(account);    // STARTS DRIVER CONNECTIONS!!!!
        }
        // if not logged in, status: not connected
        if (account == null) {
            tvStatus.setText("Status: Not Connected");
        }
        // when connect button is clicked, status: connecting
        btnConnect.setOnClickListener(v -> {
            tvStatus.setText("Status: Connecting...");
            signInLauncher.launch(mGoogleSignInClient.getSignInIntent());    // open login popup window
        });
        // UPLOAD Button: if touched -> security verification check
        btnSyncNow.setOnClickListener(v -> {
            triggerSecurityChallenge(true);    // true means, do upload
        });
        // DOWNLOAD Button: if touched -> security verification check
        btnRestoreNow.setOnClickListener(v -> {
            triggerSecurityChallenge(false);    // false means, do download
        });
    }

    // this helper method is gateway, that processes response of login screen
    private void handleSignInResult(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data)    // unpack Intent from login screen, and return Task
                .addOnSuccessListener(this::initializeDriveHelper)
                .addOnFailureListener(e -> tvStatus.setText("Status: Error " + e.getMessage()));
    }

    // this method is bridge, which starts driver connection!!!!
    private void initializeDriveHelper(GoogleSignInAccount account) {
        tvStatus.setText("Status: Connected (" + account.getEmail() + ")");
        btnConnect.setEnabled(false);    // prevents from logging again
        // creates credentials for accessing drive
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                this, Collections.singleton(DriveScopes.DRIVE_APPDATA));
        // attaches credentials to gmail account
        credential.setSelectedAccount(account.getAccount());
        // building the DRIVE API!!!!
        Drive googleDriveService = new Drive.Builder(
                new NetHttpTransport(),          // engine that sends data over web
                new GsonFactory(),               // translator that translates google code into java objects
                credential)
                .setApplicationName("DollUp")    // use same name as in google set up
                .build();
        // wraps DRIVER API into easy to use helper Class, DriveServiceHelper
        mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
        // enable (unlock) Buttons, immediately
        btnSyncNow.setEnabled(true);
        btnRestoreNow.setEnabled(true);
        // checks and saves local file condition
        boolean isLocalEmpty = true;
        try {
            DatabaseManager dbManager = new DatabaseManager(this);    // db management
            List<Doll> localDolls = dbManager.getAllDolls();                  // list of Dolls
            if (localDolls != null && !localDolls.isEmpty()) {
                isLocalEmpty = false;    // local files exist, make this false
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // check if local files are absent
        if (isLocalEmpty) {
            // also checks if there is a backup available for install
            mDriveServiceHelper.findLatestBackup().addOnSuccessListener(fileId -> {
                if (fileId != null) {
                    tvStatus.setText("Status: Backup found and local data is absent. Restoring recommended.");
                    triggerSecurityChallenge(false);    // show popup for download (restore from drive)
                }
            });
        }
    }

    // this method handles file upload (SYNCING)
    private void performSync() {
        tvStatus.setText("Status: Zipping collection...");
        // disables buttons until uploading ends, prevents from multiple button click
        btnSyncNow.setEnabled(false);
        btnRestoreNow.setEnabled(false);
        // locates correct path to ".closetDollUp"
        File rootHiddenFolder = StorageHelper.getHiddenFolder();
        // prepares temporary file "doll_backup.zip" in cache of app
        File tempZip = new File(getCacheDir(), "doll_backup.zip");
        // starts background thread to avoid UI freezing from heavy lifting (zip/unzip)
        new Thread(() -> {
            try {
                // ZipHelper packs .closetDollUp/closet folder and closet.db
                ZipHelper.zipFolder(rootHiddenFolder, tempZip);
                runOnUiThread(() -> tvStatus.setText("Status: Uploading to Cloud..."));    // to setText from inside of thread
                // calls DRIVE and UPLOADS zipped file to private app folder
                mDriveServiceHelper.uploadFile(tempZip, "application/zip")
                        .addOnSuccessListener(fileId -> {        // after successful upload do these:
                            if (tempZip.exists()) tempZip.delete();    // cleanup temp file
                            tvStatus.setText("Status: Sync Complete!");
                            btnSyncNow.setEnabled(true);               // enable buttons again
                            btnRestoreNow.setEnabled(true);            // enable buttons again
                            // small popup at the bottom of the screen to verify successful upload!
                            Toast.makeText(this, "Success! Collection backed up.", Toast.LENGTH_LONG).show();
                        })
                        .addOnFailureListener(e -> {
                            tvStatus.setText("Status: Upload Failed");
                            btnSyncNow.setEnabled(true);               // enable buttons again
                            btnRestoreNow.setEnabled(true);            // enable buttons again
                        });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvStatus.setText("Status: Error during zip");
                    btnSyncNow.setEnabled(true);                       // enable buttons again
                    btnRestoreNow.setEnabled(true);                    // enable buttons again
                });
            }
        }).start();
    }

    // this method handles file download (RESTORING)
    private void performRestore() {
        tvStatus.setText("Status: Checking Cloud...");
        // disables buttons until downloading ends, prevents from multiple button click
        btnSyncNow.setEnabled(false);
        btnRestoreNow.setEnabled(false);
        // checks if there is a backup available for downloading
        mDriveServiceHelper.findLatestBackup().addOnSuccessListener(fileId -> {
            if (fileId == null) {
                tvStatus.setText("Status: No backup found");
                btnSyncNow.setEnabled(true);
                btnRestoreNow.setEnabled(true);
                return;
            }
            // prepares temporary file "restore_temp.zip" in cache of app
            File tempZip = new File(getCacheDir(), "restore_temp.zip");
            tvStatus.setText("Status: Downloading...");
            // downloads zipped backup from DRIVE into our LOCAL folder
            mDriveServiceHelper.downloadFile(fileId, tempZip).addOnSuccessListener(v -> {
                tvStatus.setText("Status: Restoring Files...");
                // starts background thread to avoid UI freezing from heavy lifting (zip/unzip)
                new Thread(() -> {
                    try {
                        // ZipHelper unpacks .closetDollUp/closet folder and closet.db
                        ZipHelper.unzip(tempZip, StorageHelper.getHiddenFolder());
                        runOnUiThread(() -> {
                            if (tempZip.exists()) tempZip.delete();
                            tvStatus.setText("Status: Restore Complete!");
                            btnSyncNow.setEnabled(true);
                            btnRestoreNow.setEnabled(true);
                            // shows success popup, then exits activity on button press
                            new AlertDialog.Builder(this)
                                    .setTitle("Restore Success")
                                    .setMessage("Your collection has been restored. Please restart the app to see your dolls.")
                                    .setPositiveButton("OK", (dialog, which) -> finish())
                                    .setCancelable(false)
                                    .show();
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            tvStatus.setText("Status: Restore Failed");
                            btnSyncNow.setEnabled(true);
                            btnRestoreNow.setEnabled(true);
                        });
                    }
                }).start();
            }).addOnFailureListener(e -> {
                tvStatus.setText("Status: Download Failed");
                btnSyncNow.setEnabled(true);
                btnRestoreNow.setEnabled(true);
            });
        });
    }

    // this helper method is for silly SECURITY PROTOCOL, it prevents upload/download on accidental button touch
    private void triggerSecurityChallenge(boolean isUpload) {
        // generates random 6-digit code String that need to be repeated
        String securityCode = String.valueOf(new java.util.Random().nextInt(900000) + 100000);
        // arranges input of security code, it is FINAL!!!! because of lambda input being final rule!!!!
        final EditText input = new EditText(this);
        // limits input to only 6 chars, as it is intended
        input.setFilters(new android.text.InputFilter[] {
                new android.text.InputFilter.LengthFilter(6)
        });
        input.setGravity(android.view.Gravity.CENTER);    // typed input is centered
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);    // keyboard is number pad :)
        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );    // customization stuff
        params.setMargins(80, 20, 80, 40);
        input.setLayoutParams(params);
        container.addView(input);
        // creates htmlMessage based on method parameter: boolean isUpload
        String actionText = isUpload ? "OVERWRITE CLOUD BACKUP" : "OVERWRITE PHONE DATA";
        String htmlMessage = "To authorize <b>" + actionText + "</b>, enter this code:<br><br><b>" + securityCode + "</b>";
        // prepares popup window, popup htmlMessage, and sets logic of method calling
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Cloud Security Protocol")
                .setMessage(android.text.Html.fromHtml(htmlMessage, android.text.Html.FROM_HTML_MODE_LEGACY))
                .setView(container)
                .setPositiveButton("Confirm", (d, which) -> {
                    // verifies entered input
                    if (input.getText().toString().equals(securityCode)) {
                        // ACTUALLY CALLS METHODS: performSync()/UPLOAD, performRestore()/DOWNLOAD!!!!
                        if (isUpload) performSync(); else performRestore();
                    } else {
                        Toast.makeText(this, "Wrong code. Authorization denied.", Toast.LENGTH_SHORT).show();
                    }
                }).create();
        // runs popup window, shows htmlMessage, causes methods to be called
        dialog.show();
        TextView messageView = dialog.findViewById(android.R.id.message);
        if (messageView != null) {
            messageView.setGravity(android.view.Gravity.CENTER);
        }
    }
}