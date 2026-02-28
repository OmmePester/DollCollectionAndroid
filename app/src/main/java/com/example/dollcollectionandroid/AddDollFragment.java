package com.example.dollcollectionandroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AddDollFragment extends Fragment {

    private EditText nameInput, hintInput;
    private ImageView dollImageView;
    private Uri selectedImageUri;
    private DatabaseManager dbManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_doll, container, false);

        dbManager = new DatabaseManager(getActivity());
        nameInput = view.findViewById(R.id.nameInput);
        hintInput = view.findViewById(R.id.hintInput);
        dollImageView = view.findViewById(R.id.dollImageView);
        Button saveButton = view.findViewById(R.id.saveButton);

        // Click the gray box to open phone gallery [cite: 2026-02-22]
        dollImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1000);
        });

        // Click the purple button to save [cite: 2026-02-22]
        saveButton.setOnClickListener(v -> saveDollToCloset());

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == 1000 && data != null) {
            selectedImageUri = data.getData();
            dollImageView.setImageURI(selectedImageUri);
        }
    }

    private void saveDollToCloset() {
        if (selectedImageUri == null || nameInput.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), "Please add a name and photo!", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = nameInput.getText().toString();
        String hint = hintInput.getText().toString();

        // 1. Save entry to SQL [cite: 2026-02-22]
        int newId = dbManager.addDoll(name, "pending");

        // 2. Create the unique filename for your 'closet' [cite: 2026-02-22]
        String fileName = "doll_" + newId + ".jpg";

        try {
            // 3. Copy image from gallery to private closet folder [cite: 2026-02-22]
            InputStream is = getActivity().getContentResolver().openInputStream(selectedImageUri);
            File closetFile = new File(getActivity().getFilesDir(), fileName);
            FileOutputStream fos = new FileOutputStream(closetFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            is.close();
            fos.close();

            // 4. Update SQL with real path and hint [cite: 2026-02-22]
            dbManager.updateImagePath(newId, fileName);
            dbManager.updateFullDollDetails(newId, name, hint, "", "", "", 0);

            Toast.makeText(getActivity(), "Saved to Closet!", Toast.LENGTH_SHORT).show();

            // 5. Go back to the list [cite: 2026-02-22]
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CollectionFragment()).commit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}