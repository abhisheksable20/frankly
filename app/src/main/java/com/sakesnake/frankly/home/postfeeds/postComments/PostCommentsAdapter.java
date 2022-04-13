package com.sakesnake.frankly.home.postfeeds.postComments;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.docnormalizer.DocNormalizer;
import com.sakesnake.frankly.home.postfeeds.DateConversion;
import com.sakesnake.frankly.home.postfeeds.postfeedsadapter.adapterscallback.PostFeedsCallback;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

public class PostCommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;

    private List<ParseObject> postCommentsLists;

    private PostFeedsCallback postFeedsCallback;

    private final int SHOW_PROGRESS_BAR = 1;

    public PostCommentsAdapter(Context mContext, List<ParseObject> postCommentsLists,PostFeedsCallback postFeedsCallback) {
        this.mContext = mContext;
        this.postCommentsLists = postCommentsLists;
        this.postFeedsCallback = postFeedsCallback;
    }

    // post comments views view holder
    public class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicImageView;
        TextView usernameTextView, postingTimeTextView, postCommentTextView;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicImageView = (ImageView) itemView.findViewById(R.id.profile_pic_comments_image_view);
            usernameTextView = (TextView) itemView.findViewById(R.id.profile_username_comments_text_view);
            postingTimeTextView = (TextView) itemView.findViewById(R.id.post_posting_time_comments_text_view);
            postCommentTextView = (TextView) itemView.findViewById(R.id.post_comment_comments_text_view);

            setClickListener();
        }

        // Setting on click listener
        private void setClickListener(){
            profilePicImageView.setOnClickListener(view -> navToUserFragment(getAdapterPosition(),postCommentsLists.get(getAdapterPosition())));
            usernameTextView.setOnClickListener(view -> navToUserFragment(getAdapterPosition(),postCommentsLists.get(getAdapterPosition())));
        }

        private void navToUserFragment(int position, ParseObject parseObject){
            final ParseUser parseUser = parseObject.getParseUser(ParseConstants.COMMENT_OWNER);
            if (parseUser != null && postFeedsCallback != null)
                postFeedsCallback.selectedPost(position,null,parseUser);
        }
    }

    // Bottom loading progress bar view holder
    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SHOW_PROGRESS_BAR)
            return new LoadingViewHolder(LayoutInflater.from(mContext).inflate(R.layout.bottom_progress_bar,parent,false));

        return new CommentViewHolder(LayoutInflater.from(mContext).inflate(R.layout.post_comments_list_view,parent,false));
    }

    @Override
    public int getItemViewType(int position) {
        if (postCommentsLists.get(position) == null)
            return SHOW_PROGRESS_BAR;

        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LoadingViewHolder)
            return;

        CommentViewHolder commentViewHolder = (CommentViewHolder) holder;
        ParseObject commentObject = postCommentsLists.get(position);
        ParseUser commentOwner = commentObject.getParseUser(ParseConstants.COMMENT_OWNER);

        if (commentOwner != null){
            ParseFile profilePicFile = commentOwner.getParseFile(ParseConstants.PROFILE_PIC);
            if (profilePicFile != null)
                Glide.with(mContext).load(profilePicFile.getUrl()).into(commentViewHolder.profilePicImageView);

            commentViewHolder.usernameTextView.setText(commentOwner.getUsername());
        }

        commentViewHolder.postCommentTextView.setText(DocNormalizer.htmlToNormal(commentObject.getString(ParseConstants.POST_COMMENT)));
        commentViewHolder.postingTimeTextView.setText(DateConversion.getPostTime(commentObject.getCreatedAt().getTime()));
    }

    @Override
    public int getItemCount() {
        return postCommentsLists.size();
    }

    // Getting current post comment list data
    public List<ParseObject> getPostCommentsLists(){
        return this.postCommentsLists;
    }

    public void showProgressBar(){
        postCommentsLists.add(null);
        notifyItemInserted(postCommentsLists.size() - 1);
    }

    public void hideProgressBar(){
        if (postCommentsLists.get(postCommentsLists.size() - 1) == null){
            postCommentsLists.remove(postCommentsLists.size() - 1);
            notifyItemRemoved(postCommentsLists.size() - 1);
        }
    }

    public void changeTheData(final List<ParseObject> newCommentsList){
        postCommentsLists.clear();
        postCommentsLists.addAll(newCommentsList);
        notifyDataSetChanged();
    }

    public void appendData(final List<ParseObject> latestData){
        hideProgressBar();
        final int lastPosition = postCommentsLists.size() - 1;
        postCommentsLists.addAll(latestData);
        notifyItemRangeInserted(lastPosition,latestData.size());
    }

}
