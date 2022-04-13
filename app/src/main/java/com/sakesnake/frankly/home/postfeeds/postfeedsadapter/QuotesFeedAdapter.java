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
import com.sakesnake.frankly.RandomColorGenerator;
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

// This adapter may gets lagged due to many views -- Optimize it later.
public class QuotesFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<CustomParseObject> customParseObjects;

    private final Context mContext;

    private PostFeedsCallback postFeedsCallback;

    private PostFeedWithPositionCallback postFeedWithPositionCallback;

    private static final int SHOW_PROGRESS_VIEW = 1;

    public QuotesFeedAdapter(List<CustomParseObject> customParseObjects, Context mContext,
                             PostFeedsCallback postFeedsCallback, PostFeedWithPositionCallback postFeedWithPositionCallback) {
        this.customParseObjects = customParseObjects;
        this.mContext = mContext;
        this.postFeedsCallback = postFeedsCallback;
        this.postFeedWithPositionCallback = postFeedWithPositionCallback;
    }

    // Normal view holder
    public class QuotesFeedViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicImageView, contentBackgroundImageView, locationImageView,
                connectedUsersImageView,moreOptionImageView,likeImageView,commentImageView,shareImageView;
        TextView usernameTextView,postingTimeTextView,mainContentTextView,
                tLikesTextView,tCommentTextView,descriptionTextView,locationTextView,tConnectedUsersTextView;
        Button moreDetailsBtn;
        public QuotesFeedViewHolder(@NonNull View itemView) {
            super(itemView);

            // init views
            profilePicImageView = (ImageView) itemView.findViewById(R.id.feed_user_profile_photo_image_view);
            contentBackgroundImageView = (ImageView) itemView.findViewById(R.id.background_color_image_view);
            locationImageView = (ImageView) itemView.findViewById(R.id.feed_user_post_location_image_view);
            connectedUsersImageView = (ImageView) itemView.findViewById(R.id.feed_connected_users_image_view);
            moreOptionImageView =(ImageView) itemView.findViewById(R.id.feed_refresh_feed_image_view);
            likeImageView = (ImageView) itemView.findViewById(R.id.feed_like_user_post_image_view);
            commentImageView = (ImageView) itemView.findViewById(R.id.feed_comment_user_post_image_view);
            shareImageView = (ImageView) itemView.findViewById(R.id.feed_share_user_post_image_view);
            usernameTextView = (TextView) itemView.findViewById(R.id.feed_user_username_text_view);
            postingTimeTextView = (TextView) itemView.findViewById(R.id.feed_user_posting_time_text_view);
            mainContentTextView = (TextView) itemView.findViewById(R.id.feed_user_quote_text_view);
            tLikesTextView = (TextView) itemView.findViewById(R.id.feed_total_likes_text_view);
            tCommentTextView = (TextView) itemView.findViewById(R.id.feed_total_comments_text_view);
            descriptionTextView = (TextView) itemView.findViewById(R.id.feed_user_post_description_text_view);
            locationTextView = (TextView) itemView.findViewById(R.id.feed_post_location_text_view);
            tConnectedUsersTextView = (TextView) itemView.findViewById(R.id.feed_total_connected_users_text_view);
            moreDetailsBtn = (Button) itemView.findViewById(R.id.feed_user_post_more_details_button);

            setClickListeners();
        }


        // Setting views click listener
        private void setClickListeners(){

            // Liking or disliking the post
            likeImageView.setOnClickListener(view -> likeOrDislikePost(customParseObjects.get(getAdapterPosition()),getAdapterPosition()));
            tLikesTextView.setOnClickListener(view -> likeOrDislikePost(customParseObjects.get(getAdapterPosition()),getAdapterPosition()));

            // show post more options
            moreOptionImageView.setOnClickListener(view -> showMoreOptions(getAdapterPosition()));
            tConnectedUsersTextView.setOnClickListener(view -> showMoreOptions(getAdapterPosition()));
            moreDetailsBtn.setOnClickListener(view -> showMoreOptions(getAdapterPosition()));
            descriptionTextView.setOnClickListener(view -> showMoreOptions(getAdapterPosition()));

            // navigate to post comment fragment
            commentImageView.setOnClickListener(view -> navigateToPostCommentFrag(customParseObjects.get(getAdapterPosition()),getAdapterPosition()));
            tCommentTextView.setOnClickListener(view -> navigateToPostCommentFrag(customParseObjects.get(getAdapterPosition()),getAdapterPosition()));

            // navigate to post likes fragment
            tLikesTextView.setOnClickListener(view -> navigateToPostLikeFrag(customParseObjects.get(getAdapterPosition()),getAdapterPosition()));

            usernameTextView.setOnClickListener(view -> navToUserFragment(getAdapterPosition()));
            profilePicImageView.setOnClickListener(view -> navToUserFragment(getAdapterPosition()));
        }

        // Show post more options
        private void showMoreOptions(final int position){
            if (postFeedWithPositionCallback != null)
                postFeedWithPositionCallback.feedCallbackWithPosition(customParseObjects.get(position),position,ImagesFeedFragment.WORK_WITH_MORE);
        }

        private void likeOrDislikePost(final CustomParseObject customParseObject,final int position){
            if (postFeedWithPositionCallback != null)
                postFeedWithPositionCallback.feedCallbackWithPosition(customParseObject,position, ImagesFeedFragment.WORK_WITH_LIKE);
        }

        // navigate to post likes fragment
        private void navigateToPostLikeFrag(final CustomParseObject customParseObject,final int position){
            if (postFeedWithPositionCallback != null)
                postFeedWithPositionCallback.feedCallbackWithPosition(customParseObject,position,ImagesFeedFragment.WORK_WITH_TOTAL_LIKES);
        }


        // navigate to posts comment fragment
        private void navigateToPostCommentFrag(final CustomParseObject customParseObject,final int position){
            if (postFeedWithPositionCallback != null)
                postFeedWithPositionCallback.feedCallbackWithPosition(customParseObject,position,ImagesFeedFragment.WORK_WITH_COMMENT);
        }


        // Navigate to user profile fragment or user profile fragment
        private void navToUserFragment(final int position){
            if (postFeedsCallback != null)
                postFeedsCallback.selectedPost(getAdapterPosition(),null,customParseObjects.get(position).getParseObject().getParseUser(ParseConstants.POST_OWNER));
        }
    }

    // Progress bar view holder
    public static class ProgressBarViewHolder extends RecyclerView.ViewHolder{
        public ProgressBarViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SHOW_PROGRESS_VIEW)
            return new ProgressBarViewHolder(LayoutInflater.from(mContext).inflate(R.layout.bottom_progress_bar,parent,false));

        return new QuotesFeedViewHolder(LayoutInflater.from(mContext).inflate(R.layout.quotes_feed_list_view,parent,false));
    }

    @Override
    public int getItemViewType(int position) {
        if (customParseObjects.get(position) == null)
            return SHOW_PROGRESS_VIEW;

        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ProgressBarViewHolder)
            return;

        QuotesFeedViewHolder quotesFeedViewHolder = (QuotesFeedViewHolder) holder;
        // Getting Post Owner
        ParseUser postOwner = customParseObjects.get(position).getParseObject().getParseUser(ParseConstants.POST_OWNER);
        ParseObject parseObject = customParseObjects.get(position).getParseObject();
        CustomParseObject customParseObject = customParseObjects.get(position);

        if (customParseObject.getIsLikedByMe())
            quotesFeedViewHolder.likeImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.upload_logo_color));
        else
            quotesFeedViewHolder.likeImageView.setColorFilter(ContextCompat.getColor(mContext,R.color.white));

        // setting user details
        if(postOwner != null){
            final ParseFile profilePic = postOwner.getParseFile(ParseConstants.PROFILE_PIC);
            if (profilePic != null)
                Glide.with(mContext).load(profilePic.getUrl())
                        .placeholder(ContextCompat.getDrawable(mContext,R.drawable.profile_photo_default_icon))
                        .into(quotesFeedViewHolder.profilePicImageView);

            quotesFeedViewHolder.usernameTextView.setText(postOwner.getUsername());
        }

        // Setting post content
        final ParseFile backgroundImage = parseObject.getParseFile(ParseConstants.SINGLE_IMAGE);
        if (backgroundImage != null)
            Glide.with(mContext).load(backgroundImage.getUrl()).into(quotesFeedViewHolder.contentBackgroundImageView);
        else
            quotesFeedViewHolder.contentBackgroundImageView.setBackgroundColor(RandomColorGenerator.generateRandomColor(mContext));


        String description = parseObject.getString(ParseConstants.DESCRIPTION);
        if ((description != null)  &&  description.length() > 3){
            quotesFeedViewHolder.descriptionTextView.setVisibility(View.VISIBLE);
            quotesFeedViewHolder.descriptionTextView.setText(TruncateDescription.getTruncatedDescription(description,
                    ContextCompat.getColor(mContext,R.color.card_view_color_on_top_of_background)));
        }
        else
            quotesFeedViewHolder.descriptionTextView.setVisibility(View.GONE);

        String location = parseObject.getString(ParseConstants.LOCATION);
        if (location != null  && location.length() > 0){
            quotesFeedViewHolder.locationImageView.setVisibility(View.VISIBLE);
            quotesFeedViewHolder.locationTextView.setText(location);
        }else{
            quotesFeedViewHolder.locationImageView.setVisibility(View.GONE);
        }

        quotesFeedViewHolder.mainContentTextView.setText(DocNormalizer.htmlToNormal(parseObject.getString(ParseConstants.SMALL_TEXT_ONE)));
        quotesFeedViewHolder.tConnectedUsersTextView.setText(mContext.getString(R.string.number_to_string,parseObject.getInt(ParseConstants.TOTAL_CONNECTED_USERS)));
        quotesFeedViewHolder.tLikesTextView.setText(mContext.getString(R.string.number_to_string,parseObject.getInt(ParseConstants.TOTAL_POST_LIKES)));
        quotesFeedViewHolder.tCommentTextView.setText(mContext.getString(R.string.number_to_string_comment,parseObject.getInt(ParseConstants.TOTAL_POST_COMMENTS)));
        quotesFeedViewHolder.postingTimeTextView.setText(DateConversion.getPostTime(parseObject.getCreatedAt().getTime()));

    }

    @Override
    public int getItemCount() {
        return customParseObjects.size();
    }

    // Show progress bar on bottom of recycler view
    public void showProgressBar(){
        customParseObjects.add(null);
        notifyItemInserted(customParseObjects.size() - 1);
    }

    // like quotes is tapped by user
    public void likeImageTapped(final CustomParseObject customParseObject, final int position){
        customParseObjects.set(position,customParseObject);
        notifyItemChanged(position);
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
