package com.sakesnake.frankly.home.postfeeds.postfeedsadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.docnormalizer.DocNormalizer;
import com.sakesnake.frankly.home.postfeeds.DateConversion;
import com.sakesnake.frankly.home.postfeeds.allPostFeeds.CustomParseObject;
import com.sakesnake.frankly.home.postfeeds.allPostFeeds.ImagesFeedFragment;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.adapterscallback.PostFeedWithPositionCallback;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.adapterscallback.PostFeedsCallback;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

// This adapter will only used in ImagesFeedFragment
// This is not stable adapter because it contains so many view which may produce lags
public class ImagesOrMemesFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CustomParseObject> customParseObjects;

    private Context mContext;

    private PostFeedsCallback postFeedsCallback;

    private static final int SHOW_PROGRESS_BAR = 1;

    private PostFeedWithPositionCallback postFeedWithPositionCallback;

    public ImagesOrMemesFeedAdapter(List<CustomParseObject> customParseObjects, Context mContext,
                                    PostFeedsCallback postFeedsCallback,
                                    PostFeedWithPositionCallback postFeedWithPositionCallback) {
        this.customParseObjects = customParseObjects;
        this.mContext = mContext;
        this.postFeedsCallback = postFeedsCallback;
        this.postFeedWithPositionCallback = postFeedWithPositionCallback;
    }

    public class ImagesOrMemesFeedViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView,postingTimeTextView,tLikesTextView,tCommentsTextView,
                descriptionTextView,tConnectedUsersTextView,locationTextView;
        ImageView mainContentImageView,profilePicImageView,likeImageView,commentsImageView,
                shareImageView,locationImageView,moreOptionsImageView;
        Button moreDetailsBtn;
        public ImagesOrMemesFeedViewHolder(@NonNull View itemView) {
            super(itemView);
            // init views
            usernameTextView = (TextView) itemView.findViewById(R.id.feed_user_username_text_view);
            postingTimeTextView = (TextView) itemView.findViewById(R.id.feed_user_posting_time_text_view);
            tLikesTextView = (TextView) itemView.findViewById(R.id.feed_total_likes_text_view);
            tCommentsTextView = (TextView) itemView.findViewById(R.id.feed_total_comments_text_view);
            descriptionTextView = (TextView) itemView.findViewById(R.id.feed_user_post_description_text_view);
            locationTextView = (TextView) itemView.findViewById(R.id.feed_post_location_text_view);
            tConnectedUsersTextView = (TextView) itemView.findViewById(R.id.feed_total_connected_users_text_view);
            mainContentImageView = (ImageView) itemView.findViewById(R.id.feed_user_main_post_image_view);
            profilePicImageView = (ImageView) itemView.findViewById(R.id.feed_user_profile_photo_image_view);
            likeImageView = (ImageView) itemView.findViewById(R.id.feed_like_user_post_image_view);
            commentsImageView = (ImageView) itemView.findViewById(R.id.feed_comment_user_post_image_view);
            shareImageView = (ImageView) itemView.findViewById(R.id.feed_share_user_post_image_view);
            locationImageView = (ImageView) itemView.findViewById(R.id.feed_user_post_location_image_view);
            moreOptionsImageView = (ImageView) itemView.findViewById(R.id.feed_refresh_feed_image_view);
            moreDetailsBtn = (Button) itemView.findViewById(R.id.feed_user_post_more_details_button);

            setClickListener();

        }

        // Setting views click listener
        private void setClickListener(){
            // Liking or disliking the image view
            likeImageView.setOnClickListener(view -> likeOrDislikePost(customParseObjects.get(getAdapterPosition()),getAdapterPosition()));
            tLikesTextView.setOnClickListener(view -> likeOrDislikePost(customParseObjects.get(getAdapterPosition()),getAdapterPosition()));

            // show post more options
            moreOptionsImageView.setOnClickListener(view -> showPostMoreDetails(getAdapterPosition()));
            moreDetailsBtn.setOnClickListener(view -> showPostMoreDetails(getAdapterPosition()));
            descriptionTextView.setOnClickListener(view -> showPostMoreDetails(getAdapterPosition()));
            tConnectedUsersTextView.setOnClickListener(view -> showPostMoreDetails(getAdapterPosition()));

            // navigate to post comment
            commentsImageView.setOnClickListener(view -> navigateToPostComment(customParseObjects.get(getAdapterPosition()),getAdapterPosition()));
            tCommentsTextView.setOnClickListener(view -> navigateToPostComment(customParseObjects.get(getAdapterPosition()),getAdapterPosition()));

            // navigate to post likes fragment
            tLikesTextView.setOnClickListener(view -> navigateToPostLikesFrag(customParseObjects.get(getAdapterPosition()),getAdapterPosition()));

            // Share content wit other application
            shareImageView.setOnClickListener(view -> shareContent(customParseObjects.get(getAdapterPosition()),getAdapterPosition()));

            usernameTextView.setOnClickListener(view -> goToUserProfileFrag(getAdapterPosition()));
            profilePicImageView.setOnClickListener(view -> goToUserProfileFrag(getAdapterPosition()));
        }

        // Opening bottom model sheet
        private void showPostMoreDetails(final int position){
            if (postFeedWithPositionCallback != null)
                postFeedWithPositionCallback.feedCallbackWithPosition(customParseObjects.get(position),position,ImagesFeedFragment.WORK_WITH_MORE);
        }

        // Navigate user profile fragment or profile fragment
        private void goToUserProfileFrag(final int position){
            if (postFeedsCallback != null)
                postFeedsCallback.selectedPost(getAdapterPosition(),null,customParseObjects.get(position).getParseObject().getParseUser(ParseConstants.POST_OWNER));
        }

        private void likeOrDislikePost(final CustomParseObject customParseObject, int position){
            if (postFeedWithPositionCallback != null)
                postFeedWithPositionCallback.feedCallbackWithPosition(customParseObject,position, ImagesFeedFragment.WORK_WITH_LIKE);
        }

        // navigate to post likes fragment
        private void navigateToPostLikesFrag(final CustomParseObject customParseObject,final int position){
            if (postFeedWithPositionCallback != null)
                postFeedWithPositionCallback.feedCallbackWithPosition(customParseObject,position,ImagesFeedFragment.WORK_WITH_TOTAL_LIKES);
        }

        // navigate to post comment fragment
        private void navigateToPostComment(final CustomParseObject customParseObject, int position){
            if (postFeedWithPositionCallback != null)
                postFeedWithPositionCallback.feedCallbackWithPosition(customParseObject,position,ImagesFeedFragment.WORK_WITH_COMMENT);
        }

        // Share content
        private void shareContent(final CustomParseObject customParseObject, final int position){
            if (postFeedWithPositionCallback != null)
                postFeedWithPositionCallback.feedCallbackWithPosition(customParseObject,position,ImagesFeedFragment.WORK_WITH_SHARE);
        }

    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SHOW_PROGRESS_BAR)
            return new ProgressViewHolder(LayoutInflater.from(mContext).inflate(R.layout.bottom_progress_bar,parent,false));

        return new ImagesOrMemesFeedViewHolder(LayoutInflater.from(mContext).inflate(R.layout.images_or_memes_feed_list_view,parent,false));
    }

    @Override
    public int getItemViewType(int position) {
        if (customParseObjects.get(position) == null)
            return SHOW_PROGRESS_BAR;

        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ProgressViewHolder)
            return;

        ImagesOrMemesFeedViewHolder imagesOrMemesFeedViewHolder = (ImagesOrMemesFeedViewHolder) holder;

        // Setting views with data
        // Getting Post Owner
        CustomParseObject customParseObject = customParseObjects.get(position);
        ParseUser user = customParseObjects.get(position).getParseObject().getParseUser(ParseConstants.POST_OWNER);
        ParseObject parseObject = customParseObjects.get(position).getParseObject();

        // Setting user details
        if (user != null){
            final ParseFile profilePic = user.getParseFile(ParseConstants.PROFILE_PIC);
            if (profilePic != null)
                Glide.with(mContext).load(profilePic.getUrl())
                        .placeholder(ContextCompat.getDrawable(mContext,R.drawable.profile_photo_default_icon))
                        .into(imagesOrMemesFeedViewHolder.profilePicImageView);

            imagesOrMemesFeedViewHolder.usernameTextView.setText(user.getUsername());
        }

        // Highlighting like image view if liked
        // Image is liked the current user
        if (customParseObject.getIsLikedByMe())
            imagesOrMemesFeedViewHolder.likeImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.upload_logo_color));
        // Image is not liked by current user
        else
            imagesOrMemesFeedViewHolder.likeImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.white));

        // Setting post content
        final ParseFile imageFile = parseObject.getParseFile(ParseConstants.SINGLE_IMAGE);
        if (imageFile != null)
            Glide.with(mContext)
                    .load(imageFile.getUrl())
                    .into(imagesOrMemesFeedViewHolder.mainContentImageView);

        // Setting post details
        String description = parseObject.getString(ParseConstants.DESCRIPTION);
        if (description != null  &&  description.length() > 0){
            imagesOrMemesFeedViewHolder.descriptionTextView.setVisibility(View.VISIBLE);
            imagesOrMemesFeedViewHolder.descriptionTextView.setText(TruncateDescription.getTruncatedDescription(description,
                    ContextCompat.getColor(mContext,R.color.card_view_color_on_top_of_background)));
        }else{
            imagesOrMemesFeedViewHolder.descriptionTextView.setVisibility(View.GONE);
        }

        String location = parseObject.getString(ParseConstants.LOCATION);
        if (location != null && location.length() > 0){
            imagesOrMemesFeedViewHolder.locationImageView.setVisibility(View.VISIBLE);
            imagesOrMemesFeedViewHolder.locationTextView.setText(DocNormalizer.htmlToNormal(location));
        }else{
            imagesOrMemesFeedViewHolder.locationImageView.setVisibility(View.GONE);
        }

        imagesOrMemesFeedViewHolder.tConnectedUsersTextView.setText(mContext.getString(R.string.number_to_string,parseObject.getInt(ParseConstants.TOTAL_CONNECTED_USERS)));
        imagesOrMemesFeedViewHolder.tLikesTextView.setText(mContext.getString(R.string.number_to_string,parseObject.getInt(ParseConstants.TOTAL_POST_LIKES)));
        imagesOrMemesFeedViewHolder.tCommentsTextView.setText(mContext.getString(R.string.number_to_string_comment,parseObject.getInt(ParseConstants.TOTAL_POST_COMMENTS)));
        imagesOrMemesFeedViewHolder.postingTimeTextView.setText(DateConversion.getPostTime(parseObject.getCreatedAt().getTime()));
    }

    @Override
    public int getItemCount() {
        return customParseObjects.size();
    }

    // like image is tapped by user
    public void likeImageTapped(final CustomParseObject customParseObject, final int position){
        customParseObjects.set(position,customParseObject);
        notifyItemChanged(position);
    }


    // Show progress bar on bottom of recycler view
    public void showProgressBar(){
        customParseObjects.add(null);
        notifyItemInserted(customParseObjects.size() - 1);
    }

    // Hide progress bar
    public void hideProgressBar(){
        if (customParseObjects.get(customParseObjects.size() - 1) == null){
            customParseObjects.remove(customParseObjects.size() - 1);
            notifyItemRemoved(customParseObjects.size() - 1);
        }
    }

    // Post liked by currentUser
    public void postLiked(final int postPosition){
        customParseObjects.get(postPosition).setPostLikedByMe(true);
        notifyItemChanged(postPosition);
    }

    // Post has been disliked by the current user
    public void postDisliked(final int postPosition){
        customParseObjects.get(postPosition).setPostLikedByMe(false);
        notifyItemChanged(postPosition);
    }

    // Append the data with current data
    public void appendData(final List<CustomParseObject> customParseObjectList){
        hideProgressBar();
        final int lastPosition = customParseObjects.size() - 1;
        customParseObjects.addAll(customParseObjectList);
        notifyItemRangeInserted(lastPosition,customParseObjectList.size());
    }

    // Change the whole data
    public void updateWholeData(final List<CustomParseObject> customParseObjects){
        this.customParseObjects.clear();
        this.customParseObjects.addAll(customParseObjects);
        notifyDataSetChanged();
    }

    // Getting adapter data
    public List<CustomParseObject> getCustomParseObjects(){
        return this.customParseObjects;
    }

}
