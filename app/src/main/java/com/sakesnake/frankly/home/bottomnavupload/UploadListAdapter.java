package com.sakesnake.frankly.home.bottomnavupload;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.sakesnake.frankly.R;

import java.util.ArrayList;

public class UploadListAdapter extends RecyclerView.Adapter<UploadListAdapter.UploadListViewHolder>
{
    private Context mContext;
    private ArrayList<UploadList> uploadListArrayList;
    private SelectWhatToUpload selectWhatToUpload;

    public UploadListAdapter(Context mContext, ArrayList<UploadList> uploadListArrayList,SelectWhatToUpload selectWhatToUpload)
    {
        this.mContext = mContext;
        this.uploadListArrayList = uploadListArrayList;
        this.selectWhatToUpload = selectWhatToUpload;
    }

    public interface SelectWhatToUpload
    {
        void selectWhatToUploadMethod(int position);
    }

    public static class UploadListViewHolder extends RecyclerView.ViewHolder
    {
        TextView uploadFragmentDisplayListTextView;
        ConstraintLayout uploadFragmentDisplayListConstraintLayout;
        ImageView uploadFragmentBackgroundImageView;

        public UploadListViewHolder(@NonNull View itemView)
        {
            super(itemView);
            uploadFragmentDisplayListTextView = (TextView) itemView.findViewById(R.id.upload_fragment_display_list_text_view);
            uploadFragmentDisplayListConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.upload_fragment_display_list_constraint_layout);
            uploadFragmentBackgroundImageView = (ImageView) itemView.findViewById(R.id.upload_fragment_display_background_image_view);
        }
    }

    @NonNull
    @Override
    public UploadListAdapter.UploadListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflter = LayoutInflater.from(mContext);
        View rootView = inflter.inflate(R.layout.bottom_upload_item_list_recycler_view,parent,false);
        return new UploadListViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull UploadListAdapter.UploadListViewHolder holder, int position)
    {
        holder.uploadFragmentBackgroundImageView.setBackgroundColor(uploadListArrayList.get(position).getUploadCardBackground());
        holder.uploadFragmentDisplayListTextView.setText(uploadListArrayList.get(position).getUploadName());

        holder.uploadFragmentDisplayListConstraintLayout.setOnClickListener(view -> {
            if (selectWhatToUpload != null)
                selectWhatToUpload.selectWhatToUploadMethod(uploadListArrayList.get(position).getContentType());
        });
    }

    @Override
    public int getItemCount()
    {
        return uploadListArrayList.size();
    }
}
