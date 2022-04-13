package com.sakesnake.frankly.home.bottomnavupload.multiimageselector;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.R;

import java.util.List;

public class MultiImagesAdapter extends RecyclerView.Adapter<MultiImagesAdapter.MultiImagesViewHolder> {
    private Context mContext;
    private final List<Uri> mobileImages;
    private final List<Uri> preSelectedList;
    private PreImageSelectionCallback callback;

    public MultiImagesAdapter(Context mContext,
                              List<Uri> mobileImages,
                              List<Uri> preSelectedList,
                              PreImageSelectionCallback callback) {
        this.mContext = mContext;
        this.mobileImages = mobileImages;
        this.callback = callback;
        this.preSelectedList = preSelectedList;
    }

    public class MultiImagesViewHolder extends RecyclerView.ViewHolder {
        ImageView mobileImagesList, selectedMobileImages;

        public MultiImagesViewHolder(@NonNull View itemView) {
            super(itemView);
            mobileImagesList = (ImageView) itemView.findViewById(R.id.multi_images_items_image_view);
            selectedMobileImages = (ImageView) itemView.findViewById(R.id.multi_images_items_selected_images_image_view);

            mobileImagesList.setOnClickListener(view -> {
                if (preSelectedList.contains(mobileImages.get(getAdapterPosition()))) {
                    preSelectedList.remove(mobileImages.get(getAdapterPosition()));
                    selectedMobileImages.setVisibility(View.GONE);
                } else {
                    if (preSelectedList.size() >= 5) {
                        Toast.makeText(mContext, "You can select only five images", Toast.LENGTH_SHORT).show();
                    } else {
                        preSelectedList.add(mobileImages.get(getAdapterPosition()));
                        selectedMobileImages.setVisibility(View.VISIBLE);
                    }
                }
                if (callback != null) {
                    callback.selectedImages(preSelectedList);
                }
            });
        }
    }

    @NonNull
    @Override
    public MultiImagesAdapter.MultiImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MultiImagesViewHolder(LayoutInflater.from(mContext).inflate(R.layout.multi_images_list_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MultiImagesAdapter.MultiImagesViewHolder holder, int position) {
        Glide.with(mContext).load(mobileImages.get(position)).centerCrop().into(holder.mobileImagesList);
        if (preSelectedList.contains(mobileImages.get(position)))
            holder.selectedMobileImages.setVisibility(View.VISIBLE);
        else
            holder.selectedMobileImages.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return mobileImages.size();
    }
}
