package com.example.dollcollectionandroid;

import android.content.Context;
import android.content.Intent;
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
import java.util.Collections;
import java.util.List;

/**
 * This Class handles displaying Doll List to UI's RecyclerView.
 * It uses Glide to correctly show Doll images with corresponding data.
 * It attaches listeners to ImageViews to open DollDetailActivity later.
 * onCreateViewHolder() is similar to a viewed box itself in RecyclerView.
 * onBindViewHolder() is similar to an inside of viewed box in RecyclerView.
 */

public class DollAdapter extends RecyclerView.Adapter<DollAdapter.DollViewHolder> {

    private Context context;        // provided from CollectionActivity
    private List<Doll> dollList;    // provided from CollectionActivity's DatabaseManager

    // CONSTRUCTOR
    public DollAdapter(Context context, List<Doll> dollList) {
        this.context = context;
        this.dollList = dollList;
    }

    @NonNull
    @Override
    // this automatic method takes 'item_doll' XML layout, applies it to View of Doll, and creates/returns DollViewHolder
    public DollViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doll, parent, false);
        return new DollViewHolder(view);
    }

    @Override
    // this automatic method takes specific Doll from List and projects its data on existing UI (RecyclerView/DollViewHolder)
    public void onBindViewHolder(@NonNull DollViewHolder holder, int position) {
        // selects specific Doll
        Doll doll = dollList.get(position);

        // gets Doll name
        String displayName = doll.getName();

        // holds Doll name and ordinal number
        holder.nameLabel.setText(displayName);
        holder.numberLabel.setText((position + 1) + ".");

        // locates image path, "closet" folder that is inside hidden folder
        File imgFile = new File(StorageHelper.getHiddenFolder(), "closet/" + doll.getImagePath());

        // uses Glide to run and display Doll image
        if (imgFile.exists()) {
            // calculates limits: Width is 1.5x larger, Height is limited to 2x Width
            int targetWidthPx = (int) (75 * context.getResources().getDisplayMetrics().density);
            int maxHeightPx = (int) (targetWidthPx * 1.333);
            // uses GLIDE Class to fix weird rotations and clearing cache, because otherwise it shows ghost files!!!!
            Glide.with(context)
                    .load(imgFile)
                    .override(targetWidthPx, maxHeightPx)         // shrinks image and reduces memory usage
                    .fitCenter()
                    .skipMemoryCache(true)                        // skips short term memory RAM cache
                    .diskCacheStrategy(DiskCacheStrategy.NONE)    // skips long term memory
                    .into(holder.dollImage);                      // puts image inside ImageView
        } else {
            // clears recycled images
            Glide.with(context).clear(holder.dollImage);
            holder.dollImage.setImageResource(android.R.color.darker_gray);
        }

        // listens to clicks and OPENS DollDetailActivity window!!!!
        holder.itemView.setOnClickListener(v -> {
            // instantiates an Intent because DollDetailActivity is a full window, not a fragment
            Intent intent = new Intent(v.getContext(), DollDetailActivity.class);

            // passes Doll's ID "DOLL_ID" using Intent, which we will use in DollDetailActivity to load Doll's data
            intent.putExtra("DOLL_ID", doll.getId());

            // starts activity, DollDetailActivity
            v.getContext().startActivity(intent);
        });
    }

    @Override
    // this getter method returns total number of Dolls in List, for RecyclerView to know
    public int getItemCount() {
        return dollList.size();
    }

    // this helper method replaces old List with new (current/updated) List
    public void updateList(List<Doll> newList) {
        // makes current (old) List into new List
        this.dollList = newList;

        // updates list of Dolls and refreshes it to view addition
        notifyDataSetChanged();
    }

    // this method visually swaps two Dolls in the list while the user is dragging them
    public void moveItem(int fromPosition, int toPosition) {
        // strictly swaps the items in our Java ArrayList memory
        Collections.swap(dollList, fromPosition, toPosition);

        // notifies the RecyclerView to play the smooth sliding animation on the screen
        notifyItemMoved(fromPosition, toPosition);
    }

    // this getter method returns the current state of Doll List, it is called in CollectionActivity
    public List<Doll> getDollList() {
        return dollList;
    }




    /**
     * This INNER Class is playing the role of a container. It finds and holds
     * the references to specific ImageView and corresponding TextView inside
     * a single list row, which prevents heavy findViewById lookups.
     */
    public static class DollViewHolder extends RecyclerView.ViewHolder {
        // VARIABLES
        TextView nameLabel, numberLabel;
        ImageView dollImage;

        // CONSTRUCTOR
        public DollViewHolder(@NonNull View itemView) {
            super(itemView);
            nameLabel = itemView.findViewById(R.id.nameLabel);
            numberLabel = itemView.findViewById(R.id.numberLabel);
            dollImage = itemView.findViewById(R.id.dollImage);
        }
    }
}