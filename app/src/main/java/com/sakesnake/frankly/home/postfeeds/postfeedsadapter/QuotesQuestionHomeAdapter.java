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

public class QuotesQuestionHomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ParseObject> parseObjects;

    private Context mContext;

    private PostFeedsCallback postFeedsCallback;

    private int layout;

    private boolean showMoreView;

    private final int SHOW_MORE_VIEW = 1;

    public QuotesQuestionHomeAdapter(List<ParseObject> parseObjects, Context mContext, int layout,PostFeedsCallback postFeedsCallback,boolean showMoreView) {
        this.parseObjects = parseObjects;
        this.mContext = mContext;
        this.postFeedsCallback = postFeedsCallback;
        this.layout = layout;
        this.showMoreView = showMoreView;
    }


    public class ShowMoreViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener{
        CardView showMoreCardView;
        public ShowMoreViewHolder(@NonNull View itemView) {
            super(itemView);
            showMoreCardView = (CardView) itemView.findViewById(R.id.show_more_content_small_card_view);

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

    public class QuotesHomeViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener{
        ImageView contentBackgroundImageView, profilePicImageView;
        TextView mainContentTextView, usernameTextView, postingTimeTextView;
        ConstraintLayout rootConstraintLayout;

        public QuotesHomeViewHolder(@NonNull View itemView) {
            super(itemView);
            contentBackgroundImageView = (ImageView) itemView.findViewById(R.id.search_question_list_background_text_view);
            profilePicImageView = (ImageView) itemView.findViewById(R.id.bottom_post_user_profile_pic_image_view);
            mainContentTextView = (TextView) itemView.findViewById(R.id.search_question_list_content_text_view);
            usernameTextView = (TextView) itemView.findViewById(R.id.bottom_profile_user_username_text_view);
            postingTimeTextView = (TextView) itemView.findViewById(R.id.bottom_profile_user_post_created_at_text_view);
            rootConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.search_question_list_constraint_layout);


            // Send Parse Object to PostFeedCallback implementor
            rootConstraintLayout.setOnClickListener(view -> {
                if (postFeedsCallback != null)
                    postFeedsCallback.selectedPost(getAdapterPosition(),parseObjects.get(getAdapterPosition()),null);
            });

            // Sending Parse User to PostFeedCallback implementor
            usernameTextView.setOnClickListener(view -> {
                sendPostOwner(getAdapterPosition());
            });
            profilePicImageView.setOnClickListener(view -> {
                sendPostOwner(getAdapterPosition());
            });


            // Animating view to scale up or down in on touch
            rootConstraintLayout.setOnTouchListener(this);
        }

        private void sendPostOwner(int position){
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
            return new ShowMoreViewHolder(LayoutInflater.from(mContext).inflate(R.layout.show_more_content_small,parent,false));

        return new QuotesHomeViewHolder(LayoutInflater.from(mContext).inflate(layout, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        if (position == parseObjects.size())
            return SHOW_MORE_VIEW;

        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ShowMoreViewHolder)
            return;

        QuotesHomeViewHolder viewHolder = (QuotesHomeViewHolder) holder;

        // Filling up view with data
        // Getting post owner
        ParseUser postOwner = parseObjects.get(position).getParseUser(ParseConstants.POST_OWNER);
        ParseObject parseObject = parseObjects.get(position);

        // Setting post owner info
        if (postOwner != null) {
            viewHolder.usernameTextView.setText(postOwner.getUsername());
            final ParseFile profilePic = postOwner.getParseFile(ParseConstants.PROFILE_PIC);
            if (profilePic != null)
                Glide.with(mContext).load(profilePic.getUrl())
                        .placeholder(ContextCompat.getDrawable(mContext,R.drawable.profile_photo_default_icon))
                        .into(viewHolder.profilePicImageView);
        }

        //Setting Content
        if (layout == R.layout.home_questions_list_view){
            viewHolder.mainContentTextView.setText(mContext.getString(R.string.question_main_content,
                    DocNormalizer.htmlToNormal(parseObject.getString(ParseConstants.SMALL_TEXT_ONE))));
        }
        else{
            viewHolder.mainContentTextView.setText(DocNormalizer.htmlToNormal(parseObject.getString(ParseConstants.SMALL_TEXT_ONE)));
        }
        viewHolder.postingTimeTextView.setText(DateConversion.getPostTime(parseObject.getCreatedAt().getTime()));
        viewHolder.contentBackgroundImageView.setBackgroundColor(RandomColorGenerator.generateRandomColor(mContext));

        final ParseFile backgroundContent = parseObject.getParseFile(ParseConstants.SINGLE_IMAGE);
        if (backgroundContent != null)
            Glide.with(mContext).load(backgroundContent.getUrl()).into(viewHolder.contentBackgroundImageView);
    }

    @Override
    public int getItemCount() {
        if (showMoreView &&  parseObjects.size() == 15)
            return parseObjects.size()+1;

        return parseObjects.size();
    }
}
