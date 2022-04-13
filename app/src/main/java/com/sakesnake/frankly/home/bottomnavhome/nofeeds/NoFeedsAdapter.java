package com.sakesnake.frankly.home.bottomnavhome.nofeeds;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.RandomColorGenerator;

import java.util.List;

public class NoFeedsAdapter extends RecyclerView.Adapter<NoFeedsAdapter.NoFeedsViewHolder> {

    private Context mContext;

    private List<NoFeeds> noFeedsList;

    private AdapterPositionCallback adapterPositionCallback;

    public NoFeedsAdapter(Context mContext, List<NoFeeds> noFeedsList, AdapterPositionCallback adapterPositionCallback) {
        this.adapterPositionCallback = adapterPositionCallback;
        this.mContext = mContext;
        this.noFeedsList = noFeedsList;
    }

    public class NoFeedsViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener{
        ImageView contentImageView,uploadContentImageView;
        TextView contentTitleTextView,contentMessageTextView;
        public NoFeedsViewHolder(@NonNull View itemView) {
            super(itemView);
            contentImageView = (ImageView) itemView.findViewById(R.id.content_image_image_view);
            uploadContentImageView = (ImageView) itemView.findViewById(R.id.upload_content_image_view);
            contentTitleTextView = (TextView) itemView.findViewById(R.id.content_title_text_view);
            contentMessageTextView = (TextView) itemView.findViewById(R.id.content_message_text_view);

            // Animating view to scale up and down
            itemView.setOnTouchListener(this);
            itemView.setOnClickListener(view -> {
                if (adapterPositionCallback != null)
                    adapterPositionCallback.adapterPosition(noFeedsList.get(getAdapterPosition()).getContentType());
            });
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int event = motionEvent.getActionMasked();
            switch (event){
                case MotionEvent.ACTION_DOWN:{
                    view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(200).start();
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:{
                    view.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                    break;
                }
            }

            return false;
        }
    }

    @NonNull
    @Override
    public NoFeedsAdapter.NoFeedsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoFeedsViewHolder(LayoutInflater.from(mContext).inflate(R.layout.no_feeds_list_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoFeedsAdapter.NoFeedsViewHolder holder, int position) {
        // Setting Content imageView with background
        Glide.with(mContext).load(noFeedsList.get(position).getContentImage()).into(holder.contentImageView);
        holder.contentImageView.setBackgroundColor(RandomColorGenerator.generateRandomColor(mContext));

        // Setting content name and message
        holder.contentTitleTextView.setText(noFeedsList.get(position).getContentName());
        holder.contentMessageTextView.setText(noFeedsList.get(position).getContentMessage());
    }

    @Override
    public int getItemCount() {
        return noFeedsList.size();
    }
}
