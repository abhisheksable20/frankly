package com.sakesnake.frankly.home.postfeeds.postfeedsadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.home.postfeeds.DateConversion;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.adapterscallback.ImagesHomeAdapterCallback;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

public class ImagesOrMemesHomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ParseObject> parseObjects;

    private Context mContext;

    private ImagesHomeAdapterCallback adapterCallback;

    private boolean showMoreView;

    private final int SHOW_MORE_DATA_VIEW = 1;

    public ImagesOrMemesHomeAdapter(List<ParseObject> parseObjects,
                                    Context mContext,
                                    ImagesHomeAdapterCallback adapterCallback,
                                    boolean showMoreView) {

        this.parseObjects = parseObjects;
        this.mContext = mContext;
        this.adapterCallback = adapterCallback;
        this.showMoreView = showMoreView;
    }

    public static class ShowMoreViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener{
        CardView showMoreCardView;
        public ShowMoreViewHolder(@NonNull View itemView) {
            super(itemView);
            showMoreCardView = (CardView) itemView.findViewById(R.id.show_more_content_small_card_view);

            showMoreCardView.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int event = motionEvent.getActionMasked();
            switch (event){
                case MotionEvent.ACTION_DOWN:{
                    view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(200).start();
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:{
                    view.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                    break;
                }
            }
            return false;
        }
    }


    public class ImagesOrMemesViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener{
        ImageView mainContentImageView,profilePicImageView;
        TextView usernameTextView,postingTimeTextView;
        ConstraintLayout rootLConstraintLayout;
        public ImagesOrMemesViewHolder(@NonNull View itemView) {
            super(itemView);
            mainContentImageView = (ImageView) itemView.findViewById(R.id.home_images_and_memes_image_view);
            profilePicImageView = (ImageView) itemView.findViewById(R.id.bottom_post_user_profile_pic_image_view);
            usernameTextView = (TextView) itemView.findViewById(R.id.bottom_profile_user_username_text_view);
            postingTimeTextView = (TextView) itemView.findViewById(R.id.bottom_profile_user_post_created_at_text_view);
            rootLConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.home_image_or_meme_root_constraint_layout);

            // Sending Parse Object of PostFeedCallback Implementor
            rootLConstraintLayout.setOnClickListener(view -> {
                if (adapterCallback != null) {
                    final int contentType = parseObjects.get(getAdapterPosition()).getInt(ParseConstants.CONTENT_TYPE);
                    adapterCallback.imageSelected(contentType, null);
                }
            });

            // Sending ParseUser of PostFeedCallback Implementor
            usernameTextView.setOnClickListener(view -> {
                sendPostOwner(getAdapterPosition());
            });
            profilePicImageView.setOnClickListener(view -> {
                sendPostOwner(getAdapterPosition());
            });


            rootLConstraintLayout.setOnTouchListener(this);
        }

        private void sendPostOwner(final int position){
            if (adapterCallback != null)
                adapterCallback.imageSelected(position, parseObjects.get(position).getParseUser(ParseConstants.POST_OWNER));
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int event = motionEvent.getActionMasked();
            switch (event){
                case MotionEvent.ACTION_DOWN:{
                    view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(200).start();
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:{
                    view.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                    break;
                }
            }
            return false;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SHOW_MORE_DATA_VIEW)
            return new ShowMoreViewHolder(LayoutInflater.from(mContext).inflate(R.layout.show_more_content_small,parent,false));

        return new ImagesOrMemesViewHolder(LayoutInflater.from(mContext).inflate(R.layout.home_images_and_memes_list_view,parent,false));
    }

    @Override
    public int getItemViewType(int position) {
        if (position == parseObjects.size())
            return SHOW_MORE_DATA_VIEW;

        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ShowMoreViewHolder)
            return;

        ImagesOrMemesViewHolder viewHolder = (ImagesOrMemesViewHolder) holder;

        // Getting post owner
        ParseUser postOwner = parseObjects.get(position).getParseUser(ParseConstants.POST_OWNER);
        ParseObject parseObject = parseObjects.get(position);

        // Setting user info
        if (postOwner != null) {
            viewHolder.usernameTextView.setText(postOwner.getUsername());
            final ParseFile profilePic = postOwner.getParseFile(ParseConstants.PROFILE_PIC);
            if (profilePic != null)
                Glide.with(mContext).load(profilePic.getUrl())
                        .placeholder(ContextCompat.getDrawable(mContext,R.drawable.profile_photo_default_icon))
                        .into(viewHolder.profilePicImageView);
        }

        // Setting content
        final ParseFile imageContent = parseObject.getParseFile(ParseConstants.SINGLE_IMAGE);
        if (imageContent != null)
            Glide.with(mContext).load(imageContent.getUrl()).into(viewHolder.mainContentImageView);

        viewHolder.postingTimeTextView.setText(DateConversion.getPostTime(parseObject.getCreatedAt().getTime()));
    }

    public void updateAdapterData(final List<ParseObject> parseObjects){
        this.parseObjects.clear();
        this.parseObjects.addAll(parseObjects);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (showMoreView  &&  parseObjects.size() == 15)
            return parseObjects.size()+1;

        return parseObjects.size();
    }
}
