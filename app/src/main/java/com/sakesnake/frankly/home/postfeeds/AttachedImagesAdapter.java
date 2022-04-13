package com.sakesnake.frankly.home.postfeeds;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.R;

import java.util.List;

public class AttachedImagesAdapter extends RecyclerView.Adapter<AttachedImagesAdapter.AttachedImagesViewHolder> {

    private Context mContext;

    private List<Uri> attachedImages;

    private List<String> serverAttachedImages;

    public AttachedImagesAdapter(Context mContext, List<Uri> attachedImages, List<String> serverAttachedImages) {
        this.mContext = mContext;
        if (attachedImages != null) {
            this.attachedImages = attachedImages;
            this.attachedImages.remove(0);
        }
        else {
            this.serverAttachedImages = serverAttachedImages;
            this.serverAttachedImages.remove(0);
        }
    }

    public static class AttachedImagesViewHolder extends RecyclerView.ViewHolder{
        ImageView attachedImage;

        public AttachedImagesViewHolder(@NonNull View itemView) {
            super(itemView);
            attachedImage = (ImageView) itemView.findViewById(R.id.attached_image);
        }
    }

    @NonNull
    @Override
    public AttachedImagesAdapter.AttachedImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AttachedImagesViewHolder(LayoutInflater.from(mContext).inflate(R.layout.attached_images_list_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull AttachedImagesAdapter.AttachedImagesViewHolder holder, int position) {
        if (attachedImages != null)
            Glide.with(mContext).load(attachedImages.get(position)).into(holder.attachedImage);
        else
            Glide.with(mContext).load(serverAttachedImages.get(position)).into(holder.attachedImage);
    }

    @Override
    public int getItemCount() {
        if (attachedImages != null)
            return attachedImages.size();
        else
            return serverAttachedImages.size();
    }

}
