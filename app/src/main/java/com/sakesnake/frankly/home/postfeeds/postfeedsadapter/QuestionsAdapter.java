package com.sakesnake.frankly.home.postfeeds.postfeedsadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

// This is adapter will be used only in WritingFeedFragment
public class QuestionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ParseObject> parseObjects;

    private Context mContext;

    private PostFeedsCallback postFeedsCallback;

    private static final int SHOW_PROGRESS_BAR = 1;

    private int layout;

    public QuestionsAdapter(Context mContext, List<ParseObject> parseObjects, int layout,@NonNull PostFeedsCallback postFeedsCallback) {
        this.parseObjects = parseObjects;
        this.mContext = mContext;
        this.postFeedsCallback = postFeedsCallback;
        this.layout = layout;
    }

    public static class ProgressBarViewHolder extends RecyclerView.ViewHolder {
        public ProgressBarViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


    public class QuestionAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener{
        ImageView contentBackgroundImageView, profilePicImageView;
        TextView mainContentTextView,usernameTextView,uploadTimingTextView;
        ConstraintLayout rootLayout;
        public QuestionAdapterViewHolder(@NonNull View itemView){
            super(itemView);
            // init views
            contentBackgroundImageView = (ImageView) itemView.findViewById(R.id.search_question_list_background_text_view);
            profilePicImageView = (ImageView) itemView.findViewById(R.id.bottom_post_user_profile_pic_image_view);
            mainContentTextView = (TextView) itemView.findViewById(R.id.search_question_list_content_text_view);
            usernameTextView = (TextView) itemView.findViewById(R.id.bottom_profile_user_username_text_view);
            uploadTimingTextView = (TextView) itemView.findViewById(R.id.bottom_profile_user_post_created_at_text_view);
            rootLayout = (ConstraintLayout) itemView.findViewById(R.id.search_question_list_constraint_layout);


            // Sending Parse Object to PostFeedCallback implementor
            rootLayout.setOnClickListener(view -> {
                if (postFeedsCallback != null)
                    postFeedsCallback.selectedPost(getAdapterPosition(),parseObjects.get(getAdapterPosition()),null);
            });

            // Sending post owner info to PostFeedCallback implementor
            profilePicImageView.setOnClickListener(view -> {
                setPostOwnerInfo(getAdapterPosition());
            });
            usernameTextView.setOnClickListener(view -> {
                setPostOwnerInfo(getAdapterPosition());
            });

            //Animating view to scale up or down in on touch
            rootLayout.setOnTouchListener(this);
        }

        private void setPostOwnerInfo(final int position){
            if (postFeedsCallback != null)
                postFeedsCallback.selectedPost(getAdapterPosition(),null,parseObjects.get(position).getParseUser(ParseConstants.POST_OWNER));
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            int event = motionEvent.getActionMasked();
            switch (event){
                case MotionEvent.ACTION_DOWN:{
                    view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(0).start();
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:{
                    view.animate().scaleX(1f).scaleY(1f).setDuration(0).start();
                    break;
                }
            }
            return false;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SHOW_PROGRESS_BAR)
            return new ProgressBarViewHolder(LayoutInflater.from(mContext).inflate(R.layout.bottom_progress_bar,parent,false));

        return new QuestionAdapterViewHolder(LayoutInflater.from(mContext).inflate(layout,parent,false));
    }

    @Override
    public int getItemViewType(int position) {
        if (parseObjects.get(position) == null)
            return SHOW_PROGRESS_BAR;

        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ProgressBarViewHolder)
            return;

        QuestionAdapterViewHolder questionAdapterViewHolder = (QuestionAdapterViewHolder) holder;

        // Getting post owner
        final ParseUser postOwner = parseObjects.get(position).getParseUser(ParseConstants.POST_OWNER);
        final ParseObject parseObject = parseObjects.get(position);

        // Setting user info
        if (postOwner != null){
            questionAdapterViewHolder.usernameTextView.setText(postOwner.getUsername());
            final ParseFile profilePic = postOwner.getParseFile(ParseConstants.PROFILE_PIC);
            if (profilePic != null)
                Glide.with(mContext).load(profilePic.getUrl())
                        .placeholder(ContextCompat.getDrawable(mContext,R.drawable.profile_photo_default_icon))
                        .into(questionAdapterViewHolder.profilePicImageView);
        }

        //Setting Content view
        questionAdapterViewHolder.contentBackgroundImageView.setBackgroundColor(RandomColorGenerator.generateRandomColor(mContext));
        questionAdapterViewHolder.mainContentTextView.setText(mContext.getString(R.string.question_main_content,
                DocNormalizer.htmlToNormal(parseObject.getString(ParseConstants.SMALL_TEXT_ONE))));
        questionAdapterViewHolder.uploadTimingTextView.setText(DateConversion.getPostTime(parseObject.getCreatedAt().getTime()));
    }

    @Override
    public int getItemCount() {
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
