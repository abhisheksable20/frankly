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
import com.sakesnake.frankly.RandomColorGenerator;
import com.sakesnake.frankly.docnormalizer.DocNormalizer;
import com.sakesnake.frankly.home.postfeeds.DateConversion;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.adapterscallback.PostFeedsCallback;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

public class StoryOrPoemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ParseObject> parseObjects;

    private Context mContext;

    private int layout;

    private PostFeedsCallback postFeedsCallback;

    private boolean showMoreView;

    private final int SHOW_MORE_VIEW = 1;

    private static final int SHOW_PROGRESS_BAR = 2;

    public StoryOrPoemsAdapter(List<ParseObject> parseObjects, int layout, Context mContext,
                               @NonNull PostFeedsCallback postFeedsCallback,
                               boolean showMoreView) {
        this.parseObjects = parseObjects;
        this.mContext = mContext;
        this.layout = layout;
        this.postFeedsCallback = postFeedsCallback;
        this.showMoreView = showMoreView;
    }


    public class ShowMoreViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener{
        CardView showMoreCardView;
        public ShowMoreViewHolder(@NonNull View itemView) {
            super(itemView);
            showMoreCardView = (CardView) itemView.findViewById(R.id.show_more_content_big_card_view);

            showMoreCardView.setOnTouchListener(this);

            itemView.setOnClickListener(view -> showMoreData(parseObjects.get(getAdapterPosition())));
        }

        // Show more data
        private void showMoreData(final ParseObject parseObject){
            if (parseObject == null || postFeedsCallback == null)
                return;

            final int contentType = parseObject.getInt(ParseConstants.CONTENT_TYPE);
            postFeedsCallback.selectedPost(contentType,null,null);
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

    public static class ProgressBarViewHolder extends RecyclerView.ViewHolder {
        public ProgressBarViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class StoryOrPoemViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener{
        TextView contentTitleAndAuthorTextView, mainContentTextView,usernameTextView,postingTimeTextView;
        ImageView contentBackgroundImageView,profilePicImageView;
        ConstraintLayout rootLayout;
        public StoryOrPoemViewHolder(@NonNull View itemView) {
            super(itemView);

            // init views
            contentBackgroundImageView = (ImageView) itemView.findViewById(R.id.search_story_poem_list_background_image_view);
            profilePicImageView = (ImageView) itemView.findViewById(R.id.bottom_post_user_profile_pic_image_view);
            contentTitleAndAuthorTextView = (TextView) itemView.findViewById(R.id.search_story_or_poem_list_title_and_author_text_view);
            mainContentTextView = (TextView) itemView.findViewById(R.id.search_story_or_poem_content_text_view);
            usernameTextView = (TextView) itemView.findViewById(R.id.bottom_profile_user_username_text_view);
            postingTimeTextView = (TextView) itemView.findViewById(R.id.bottom_profile_user_post_created_at_text_view);
            rootLayout = (ConstraintLayout) itemView.findViewById(R.id.search_story_or_poem_list_constraint_layout);

            // Sending Parse Object to PostFeedCallback implementor
            rootLayout.setOnClickListener(view -> {
                if (postFeedsCallback != null)
                    postFeedsCallback.selectedPost(getAdapterPosition(),parseObjects.get(getAdapterPosition()),null);
            });

            // Sending Post owner info to PostFeedCallback implementor
            profilePicImageView.setOnClickListener(view -> {
                setPostOwnerInfo(getAdapterPosition());
            });
            usernameTextView.setOnClickListener(view -> {
                setPostOwnerInfo(getAdapterPosition());
            });

            // Animating constraint layout to scale down or up in on touch
            rootLayout.setOnTouchListener(this);
        }

        private void setPostOwnerInfo(int position){
            if (postFeedsCallback != null)
                postFeedsCallback.selectedPost(getAdapterPosition(),null,parseObjects.get(position).getParseUser(ParseConstants.POST_OWNER));
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
        if (viewType == SHOW_MORE_VIEW)
            return new ShowMoreViewHolder(LayoutInflater.from(mContext).inflate(R.layout.show_more_content_big,parent,false));
        else if (viewType == SHOW_PROGRESS_BAR)
            return new ProgressBarViewHolder(LayoutInflater.from(mContext).inflate(R.layout.bottom_progress_bar,parent,false));

        return new StoryOrPoemViewHolder(LayoutInflater.from(mContext).inflate(layout,parent,false));
    }

    @Override
    public int getItemViewType(int position) {
        if (position == parseObjects.size())
            return SHOW_MORE_VIEW;
        else if (parseObjects.get(position) == null)
            return SHOW_PROGRESS_BAR;

        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ShowMoreViewHolder  ||  holder instanceof ProgressBarViewHolder)
            return;

        StoryOrPoemViewHolder viewHolder = (StoryOrPoemViewHolder) holder;

        // Getting the post owner
        ParseUser postOwner = parseObjects.get(position).getParseUser(ParseConstants.POST_OWNER);
        ParseObject parseObject = parseObjects.get(position);

        if (postOwner != null){
            viewHolder.usernameTextView.setText(postOwner.getUsername());

            final ParseFile profilePic = postOwner.getParseFile(ParseConstants.PROFILE_PIC);
            if (profilePic != null)
                Glide.with(mContext).load(profilePic.getUrl())
                        .placeholder(ContextCompat.getDrawable(mContext,R.drawable.profile_photo_default_icon))
                        .into(viewHolder.profilePicImageView);
        }

        // Setting storyOrPoem Content views
        final String titleAndAuthor = DocNormalizer.htmlToNormal(parseObject.getString(ParseConstants.SMALL_TEXT_ONE)) +
                " \u2022 "+
                DocNormalizer.htmlToNormal(parseObject.getString(ParseConstants.SMALL_TEXT_TWO));
        viewHolder.contentTitleAndAuthorTextView.setText(titleAndAuthor);

        viewHolder.mainContentTextView.setText(parseObject.getString(ParseConstants.CONTENT_PREVIEW));
        viewHolder.postingTimeTextView.setText(DateConversion.getPostTime(parseObject.getCreatedAt().getTime()));


        // Setting content background images with data
        final ParseFile backgroundImage = parseObject.getParseFile(ParseConstants.SINGLE_IMAGE);
        if (backgroundImage != null)
            Glide.with(mContext).load(backgroundImage.getUrl()).into(viewHolder.contentBackgroundImageView);
        else
            viewHolder.contentBackgroundImageView.setBackgroundColor(RandomColorGenerator.generateRandomColor(mContext));
    }

    @Override
    public int getItemCount() {
        if (showMoreView && parseObjects.size() == 15)
            return parseObjects.size()+1;

        return parseObjects.size();
    }

    // Show the progress bar at bottom of recycler view
    public void showProgressBar(){
        parseObjects.add(null);
        notifyItemInserted(parseObjects.size() - 1);
    }

    // Hide the progress bar if no data is found
    public void hideProgressBar(){
        if (parseObjects.get(parseObjects.size() - 1) == null) {
            parseObjects.remove(parseObjects.size() - 1);
            notifyItemRemoved(parseObjects.size() - 1);
        }
    }

    // Append this data with the current data in parse object list
    public void appendData(@NonNull final List<ParseObject> parseObjectsList){
        hideProgressBar();
        final int lastPosition = parseObjects.size() - 1;
        parseObjects.addAll(parseObjectsList);
        notifyItemRangeInserted(lastPosition,parseObjectsList.size());
    }

    // Refreshing the full adapter
    public void fullDataChanged(@NonNull final List<ParseObject> newParseList){
        parseObjects.clear();
        parseObjects.addAll(newParseList);
        notifyDataSetChanged();
    }

    public List<ParseObject> getAdapterList(){
        return parseObjects;
    }
}
