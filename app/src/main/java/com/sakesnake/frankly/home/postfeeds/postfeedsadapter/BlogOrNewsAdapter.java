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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class BlogOrNewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ParseObject> parseObjects;

    private Context mContext;

    private int layout;

    private PostFeedsCallback postFeedsCallback;

    private final int SHOW_HALF_DATA = 1;

    private final int SHOW_PROGRESS_BAR = 2;

    private boolean showHalfData;

    public BlogOrNewsAdapter(List<ParseObject> parseObjects, Context mContext, int layout, PostFeedsCallback postFeedsCallback,boolean showHalfData) {
        this.parseObjects = parseObjects;
        this.mContext = mContext;
        this.layout = layout;
        this.postFeedsCallback = postFeedsCallback;
        this.showHalfData = showHalfData;
    }


    public class ShowMoreViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener{
        CardView showMoreContent;
        public ShowMoreViewHolder(@NonNull View itemView) {
            super(itemView);
            showMoreContent = (CardView) itemView.findViewById(R.id.show_more_content_big_card_view);

            showMoreContent.setOnTouchListener(this);

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
            int action = motionEvent.getActionMasked();
            switch (action){
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

    public static class ProgressBarViewHolder extends RecyclerView.ViewHolder{
        public ProgressBarViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class NormalItemsViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener{

        ImageView backgroundImageView, profilePicImageView;
        TextView mainContentTextView,contentTitleTextView, usernameTextView,uploadedTimeTextView;
        ConstraintLayout rootConstraintLayout;

        public NormalItemsViewHolder(@NonNull View itemView) {
            super(itemView);
            rootConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.search_blog_or_news_list_constraint_layout);
            backgroundImageView = (ImageView) itemView.findViewById(R.id.search_blog_or_news_list_background_image_view);
            profilePicImageView = (ImageView) itemView.findViewById(R.id.bottom_post_user_profile_pic_image_view);
            mainContentTextView = (TextView) itemView.findViewById(R.id.search_blog_or_news_list_content_text_view);
            contentTitleTextView = (TextView) itemView.findViewById(R.id.search_blog_or_news_list_title_text_view);
            usernameTextView = (TextView) itemView.findViewById(R.id.bottom_profile_user_username_text_view);
            uploadedTimeTextView = (TextView) itemView.findViewById(R.id.bottom_profile_user_post_created_at_text_view);

            // Sending Parse Object to PostFeedCallback Implementation to navigate to PostFullFeedFragment
            rootConstraintLayout.setOnClickListener(view -> {
                if (postFeedsCallback != null)
                    postFeedsCallback.selectedPost(getAdapterPosition(),parseObjects.get(getAdapterPosition()),null);
            });

            // Send Post Owner Object To PostFeedCallback Implementation fragment
            profilePicImageView.setOnClickListener(view -> {
                sendParseUserObject(getAdapterPosition());
            });
            usernameTextView.setOnClickListener(view -> {
                sendParseUserObject(getAdapterPosition());
            });

            // Setting animation of ScaleX and ScaleY
            rootConstraintLayout.setOnTouchListener(this);
        }

        private void sendParseUserObject(int position){
            if (postFeedsCallback != null)
                postFeedsCallback.selectedPost(getAdapterPosition(),null,parseObjects.get(position).getParseUser(ParseConstants.POST_OWNER));
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int action = motionEvent.getActionMasked();
            switch (action){
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
        if (viewType == SHOW_HALF_DATA)
            return new ShowMoreViewHolder(LayoutInflater.from(mContext).inflate(R.layout.show_more_content_big,parent,false));
        else if (viewType == SHOW_PROGRESS_BAR)
            return new ProgressBarViewHolder(LayoutInflater.from(mContext).inflate(R.layout.bottom_progress_bar,parent,false));

        return new NormalItemsViewHolder(LayoutInflater.from(mContext).inflate(layout,parent,false));
    }

    @Override
    public int getItemViewType(int position) {
        if (position == parseObjects.size())
            return SHOW_HALF_DATA;
        else if (parseObjects.get(position) == null)
            return SHOW_PROGRESS_BAR;

        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ShowMoreViewHolder  ||  holder instanceof ProgressBarViewHolder)
            return;

        NormalItemsViewHolder normalItemsViewHolder = (NormalItemsViewHolder) holder;

        // Filling views with data
        ParseUser user = parseObjects.get(position).getParseUser(ParseConstants.POST_OWNER);
        ParseObject post = parseObjects.get(position);
        JSONArray jsonArray = post.getJSONArray(ParseConstants.MULTI_IMAGE);

        // Setting background of the image view if JSON array is null random color will set in background image
        if (jsonArray != null && jsonArray.length() > 0) {
            try {
                Glide.with(mContext).load(jsonArray.getJSONObject(0).getString("url"))
                        .into(normalItemsViewHolder.backgroundImageView);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            normalItemsViewHolder.backgroundImageView.setBackgroundColor(RandomColorGenerator.generateRandomColor(mContext));
        }

        // Setting post user details
        if (user != null) {
            normalItemsViewHolder.usernameTextView.setText(user.getUsername());
            final ParseFile profilePic = user.getParseFile(ParseConstants.PROFILE_PIC);
            if (profilePic != null)
                Glide.with(mContext).load(profilePic.getUrl())
                        .placeholder(ContextCompat.getDrawable(mContext, R.drawable.profile_photo_default_icon))
                        .into(normalItemsViewHolder.profilePicImageView);

        }

        // Setting content
        normalItemsViewHolder.contentTitleTextView.setText(DocNormalizer.htmlToNormal(post.getString(ParseConstants.SMALL_TEXT_ONE)));
        normalItemsViewHolder.mainContentTextView.setText(post.getString(ParseConstants.CONTENT_PREVIEW));
        normalItemsViewHolder.uploadedTimeTextView.setText(DateConversion.getPostTime(post.getCreatedAt().getTime()));
    }

    @Override
    public int getItemCount() {
        if (showHalfData && parseObjects.size() == 15)
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
