package com.sakesnake.frankly.home.bottomnavupload.imageupload;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.R;

import java.util.List;

public class MobileImagesAdapter extends RecyclerView.Adapter<MobileImagesAdapter.MobileImagesViewHolder>{

    private Context mContext;

    private List<Uri> mobileImagesList;

    private TappedImageCallback callback;

    private List<Uri> preSelectedImage;

    public class MobileImagesViewHolder extends RecyclerView.ViewHolder {
        ImageView mobileImages, imageSelectedTick;
        public MobileImagesViewHolder(@NonNull View itemView) {
            super(itemView);
            mobileImages = (ImageView) itemView.findViewById(R.id.mobile_images_list_image_view);
            imageSelectedTick = (ImageView) itemView.findViewById(R.id.image_selected_tick_image_view);
            mobileImages.setOnClickListener(view -> {
                if (preSelectedImage.contains(mobileImagesList.get(getAdapterPosition()))){
                    preSelectedImage.remove(mobileImagesList.get(getAdapterPosition()));
                    imageSelectedTick.setVisibility(View.GONE);
                }else{
                    if (preSelectedImage.size() >= 5){
                        Toast.makeText(mContext, "You can select only five images.", Toast.LENGTH_SHORT).show();
                    }else{
                        preSelectedImage.add(mobileImagesList.get(getAdapterPosition()));
                        imageSelectedTick.setVisibility(View.VISIBLE);
                    }
                }
                if (callback != null){
                    callback.tappedImage(getAdapterPosition(),preSelectedImage);
                }
            });
        }
    }

    public MobileImagesAdapter(Context mContext, List<Uri> mobileImagesList, List<Uri> preSelectedImage, TappedImageCallback callback) {
        this.mContext = mContext;
        this.mobileImagesList = mobileImagesList;
        this.callback = callback;
        this.preSelectedImage = preSelectedImage;
    }

    @NonNull
    @Override
    public MobileImagesAdapter.MobileImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MobileImagesViewHolder(LayoutInflater.from(mContext).inflate(R.layout.mobile_images_item_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MobileImagesAdapter.MobileImagesViewHolder holder, int position) {
        Glide.with(mContext)
                .load(mobileImagesList.get(position))
                .placeholder(ContextCompat.getDrawable(mContext, R.color.card_view_color_on_top_of_background))
                .centerCrop()
                .into(holder.mobileImages);

        if (preSelectedImage.contains(mobileImagesList.get(position))){
            holder.imageSelectedTick.setVisibility(View.VISIBLE);
        }else{
            holder.imageSelectedTick.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mobileImagesList.size();
    }

}
