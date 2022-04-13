package com.sakesnake.frankly.home.bottomnavupload.writingupload;

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

public class BlogsAndNewsAdapter extends RecyclerView.Adapter<BlogsAndNewsAdapter.BlogsAndNewsViewHolder> {
    private Context mContext;
    private List<Uri> attachedImagesUri;
    private RemoveAttachedImage removeAttachedImage;

    public interface RemoveAttachedImage {
        void removeAttachedImage(List<Uri> removedImageUri);
    }

    public BlogsAndNewsAdapter(Context mContext, List<Uri> attachedImagesUri, RemoveAttachedImage removeAttachedImage) {
        this.mContext = mContext;
        this.attachedImagesUri = attachedImagesUri;
        this.removeAttachedImage = removeAttachedImage;
    }

    public class BlogsAndNewsViewHolder extends RecyclerView.ViewHolder {
        ImageView attachedImages, deleteAttachedImages;

        public BlogsAndNewsViewHolder(@NonNull View itemView) {
            super(itemView);
            attachedImages = (ImageView) itemView.findViewById(R.id.attach_images_upload_image_view);
            deleteAttachedImages = (ImageView) itemView.findViewById(R.id.delete_attach_images_upload_image_view);

            deleteAttachedImages.setOnClickListener(view -> {
                attachedImagesUri.remove(getAdapterPosition());
                notifyItemRemoved(getAdapterPosition());
                if (removeAttachedImage != null)
                    removeAttachedImage.removeAttachedImage(attachedImagesUri);
            });
        }
    }

    @NonNull
    @Override
    public BlogsAndNewsAdapter.BlogsAndNewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BlogsAndNewsViewHolder(LayoutInflater.from(mContext).inflate(R.layout.attach_images_upload_item_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BlogsAndNewsAdapter.BlogsAndNewsViewHolder holder, int position) {
        Glide.with(mContext).load(attachedImagesUri.get(position)).centerCrop().into(holder.attachedImages);
    }

    @Override
    public int getItemCount() {
        return attachedImagesUri.size();
    }
}
