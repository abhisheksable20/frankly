package com.sakesnake.frankly.home.bottomnavprofile.myuploads;

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

public class MyUploadsAdapter extends RecyclerView.Adapter<MyUploadsAdapter.MyUploadsViewHolder>
{
    private Context mContext;

    private ArrayList<MyUploads> myUploadsArrayList;

    private UploadsAdapterCallback uploadsAdapterCallback;

    public MyUploadsAdapter(Context mContext, ArrayList<MyUploads> myUploadsArrayList, UploadsAdapterCallback uploadsAdapterCallback)
    {
        this.mContext = mContext;

        this.myUploadsArrayList = myUploadsArrayList;

        this.uploadsAdapterCallback = uploadsAdapterCallback;
    }

    public class MyUploadsViewHolder extends RecyclerView.ViewHolder
    {
        ConstraintLayout myUploadsConstraintLayout;
        TextView myUploadsTextView;
        ImageView backgroundImageView;

        public MyUploadsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            myUploadsConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.my_uploads_list_recycler_view_constraint_layout);
            myUploadsTextView = (TextView) itemView.findViewById(R.id.my_uploads_list_recycler_view_text_view);
            backgroundImageView = (ImageView) itemView.findViewById(R.id.my_uploads_list_recycler_view_background_image_view);

            myUploadsConstraintLayout.setOnClickListener(view -> {
                if (uploadsAdapterCallback != null)
                    uploadsAdapterCallback.getContentType(myUploadsArrayList.get(getAdapterPosition()).getContentType());
            });
        }
    }

    @NonNull
    @Override
    public MyUploadsAdapter.MyUploadsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View myUploadView = inflater.inflate(R.layout.my_uploads_profile_fragment_list_recycler_view,parent,false);
        return new MyUploadsViewHolder(myUploadView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyUploadsAdapter.MyUploadsViewHolder holder, int position)
    {
        holder.backgroundImageView.setBackgroundColor(myUploadsArrayList.get(position).getUploadConstraintBackground());
        holder.myUploadsTextView.setText(myUploadsArrayList.get(position).getMyUploadName());
    }

    @Override
    public int getItemCount() {
        return myUploadsArrayList.size();
    }
}
