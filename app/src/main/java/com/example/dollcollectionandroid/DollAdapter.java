package com.example.dollcollectionandroid;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dollcollectionandroid.model.Doll;
import java.io.File;
import java.util.List;

public class DollAdapter extends RecyclerView.Adapter<DollAdapter.DollViewHolder> {

    private List<Doll> dollList;

    public DollAdapter(List<Doll> dollList) {
        this.dollList = dollList;
    }

    @NonNull
    @Override
    public DollViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doll, parent, false);
        return new DollViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DollViewHolder holder, int position) {
        Doll doll = dollList.get(position);
        holder.nameLabel.setText(doll.getName() + " (" + (doll.getHint() != null ? doll.getHint() : "") + ")");
        holder.numberLabel.setText((position + 1) + ".");

        // Load image from the 'closet' folder [cite: 2026-02-22]
        File imgFile = new File(holder.itemView.getContext().getFilesDir(), doll.getImagePath());
        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            holder.dollImage.setImageBitmap(myBitmap);
        }

        // Handle clicking a doll to open the separate Activity window [cite: 2026-02-22]
        holder.itemView.setOnClickListener(v -> {
            // We use an Intent because DollDetailActivity is a full window, not a fragment [cite: 2026-02-22]
            android.content.Intent intent = new android.content.Intent(v.getContext(), DollDetailActivity.class);

            // Pass the ID using "DOLL_ID" to match what you wrote in the Activity [cite: 2026-02-22]
            intent.putExtra("DOLL_ID", doll.getId());

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return dollList.size();
    }

    public void updateList(List<Doll> newList) {
        this.dollList = newList;
        // method to update list of Dolls and refresh it to view the addition
        notifyDataSetChanged();
    }

    public static class DollViewHolder extends RecyclerView.ViewHolder {
        TextView nameLabel, numberLabel;
        ImageView dollImage;

        public DollViewHolder(@NonNull View itemView) {
            super(itemView);
            nameLabel = itemView.findViewById(R.id.nameLabel);
            numberLabel = itemView.findViewById(R.id.numberLabel);
            dollImage = itemView.findViewById(R.id.dollImage);
        }
    }
}