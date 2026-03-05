package com.example.dollcollectionandroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // [PRO SOLUTION: Added Glide for rotation fix] [cite: 2026-03-03]
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.dollcollectionandroid.model.Doll;
import java.io.File;
import java.util.List;

public class DollAdapter extends RecyclerView.Adapter<DollAdapter.DollViewHolder> {

    private List<Doll> dollList;
    private Context context; // Added to match the 'this' argument from CollectionActivity [cite: 2026-03-01]

    // Updated Constructor to accept two arguments: Context and the List [cite: 2026-03-01]
    public DollAdapter(Context context, List<Doll> dollList) {
        this.context = context;
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

        String displayName = doll.getName();

        holder.nameLabel.setText(displayName);
        holder.numberLabel.setText((position + 1) + ".");

        // adding the path to "closet" folder here
        File closetFolder = new File(context.getFilesDir(), "closet");
        File imgFile = new File(closetFolder, doll.getImagePath());

        // using GLIDE CLASS to fix weird rotations and clearing cache, because otherwise it shows ghost files!!!!
        if (imgFile.exists()) {
            // calculating limits: Width is 1.5x larger, Height is limited to 2x Width
            int targetWidthPx = (int) (75 * context.getResources().getDisplayMetrics().density);
            int maxHeightPx = (int) (targetWidthPx * 1.333);
            //
            Glide.with(context)
                    .load(imgFile)
                    .override(targetWidthPx, maxHeightPx)    //
                    .fitCenter()
                    .skipMemoryCache(true)                        // skip short term memory RAM cache
                    .diskCacheStrategy(DiskCacheStrategy.NONE)    // skip long term memory
                    .into(holder.dollImage);
        } else {
            // Clear recycled images [cite: 2026-03-03]
            Glide.with(context).clear(holder.dollImage);
            holder.dollImage.setImageResource(android.R.color.darker_gray);
        }

        // Handle clicking a doll to open the separate Activity window [cite: 2026-02-22]
        holder.itemView.setOnClickListener(v -> {
            // We use an Intent because DollDetailActivity is a full window, not a fragment
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