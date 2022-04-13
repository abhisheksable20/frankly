package com.sakesnake.frankly.home.bottomnavsearch.searchprofiles;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class SearchedProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;

    private boolean showDeleteOption;

    private List<ParseUser> parseUsers;

    private SearchedProfileCallback searchedProfileCallback;

    private int SHOW_PROGRESS_BAR = 101;

    private int SHOW_NORMAL_VIEW = 100;

    public SearchedProfileAdapter(Context mContext,
                                  boolean showDeleteOption,
                                  List<ParseUser> parseUsers,
                                  SearchedProfileCallback searchedProfileCallback) {
        this.mContext = mContext;
        this.showDeleteOption = showDeleteOption;
        this.parseUsers = parseUsers;
        this.searchedProfileCallback = searchedProfileCallback;
    }

    public class SearchedProfileViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener{
        ImageView profilePicImageView,deleteSavedUser;
        TextView usernameTextView,originalNameTextView;
        ConstraintLayout rootConstraintLayout;
        public SearchedProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicImageView = (ImageView) itemView.findViewById(R.id.searched_user_profile_pic_image_view);
            usernameTextView = (TextView) itemView.findViewById(R.id.searched_user_username_text_view);
            originalNameTextView = (TextView) itemView.findViewById(R.id.searched_user_original_name_text_view);
            deleteSavedUser = (ImageView) itemView.findViewById(R.id.delete_saved_search_user);
            rootConstraintLayout = (ConstraintLayout) itemView.findViewById(R.id.searched_users_constraint_layout);

            rootConstraintLayout.setOnTouchListener(this);

            setClickListener();
        }

        private void setClickListener(){
            // Just navigate to profile fragment
            rootConstraintLayout.setOnClickListener(view -> {
                if (searchedProfileCallback != null)
                    searchedProfileCallback.profileSelected(parseUsers.get(getAdapterPosition()));
            });

            // Delete user from list
            deleteSavedUser.setOnClickListener(view -> deleteUserFromList(getAdapterPosition()));
        }

        // Deleting parse user from list
        private void deleteUserFromList(final int position){
            parseUsers.get(position).unpinInBackground();
            parseUsers.remove(position);
            notifyItemRemoved(position);
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


    public class ProgressBarViewHolder extends RecyclerView.ViewHolder{
        public ProgressBarViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SHOW_NORMAL_VIEW)
            return new SearchedProfileViewHolder(LayoutInflater.from(mContext).inflate(R.layout.searched_users_list_view,parent,false));
        else
            return new ProgressBarViewHolder(LayoutInflater.from(mContext).inflate(R.layout.bottom_progress_bar,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof ProgressBarViewHolder)
            return;

        SearchedProfileViewHolder searchedProfileViewHolder = (SearchedProfileViewHolder) holder;

        ParseUser user = parseUsers.get(position);

        if (showDeleteOption)
            searchedProfileViewHolder.deleteSavedUser.setVisibility(View.VISIBLE);
        else
            searchedProfileViewHolder.deleteSavedUser.setVisibility(View.GONE);

        // Setting views
        searchedProfileViewHolder.usernameTextView.setText(user.getUsername());
        searchedProfileViewHolder.originalNameTextView.setText(user.getString(ParseConstants.PROFILE_ORIGINAL_NAME));

        // Getting profile pic parse file
        ParseFile profilePic = user.getParseFile(ParseConstants.PROFILE_PIC);
        if (profilePic != null)
            Glide.with(mContext).load(profilePic.getUrl()).into(searchedProfileViewHolder.profilePicImageView);
    }

    @Override
    public int getItemCount() {
        return parseUsers.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (parseUsers.get(position) == null)
            return SHOW_PROGRESS_BAR;
        else
            return SHOW_NORMAL_VIEW;
    }

    public void updateNetworkData(@NonNull List<ParseUser> newParseUsers,boolean deletePermission){
        if (parseUsers == null)
            return;

        parseUsers.clear();
        parseUsers.addAll(newParseUsers);
        this.showDeleteOption = deletePermission;
        notifyDataSetChanged();
    }

    public void clearData(){
        final int listSize = parseUsers.size();
        parseUsers.clear();
        notifyItemRangeRemoved(0,listSize);
    }

    // Show the progress bar at bottom of recycler view
    public void showProgressBar(){
        parseUsers.add(null);
        notifyItemInserted(parseUsers.size() - 1);
    }

    // Hide the progress bar if no data is found
    public void hideProgressBar(){
        if (parseUsers.get(parseUsers.size() - 1) == null) {
            parseUsers.remove(parseUsers.size() - 1);
            notifyItemRemoved(parseUsers.size() - 1);
        }
    }

    public void appendData(final List<ParseUser> parseUsersList){
        hideProgressBar();
        int lastPosition = parseUsers.size() - 1;
        this.parseUsers.addAll(parseUsersList);
        notifyItemRangeInserted(lastPosition,parseUsersList.size());
    }

    // Getting the parse users lists
    public List<ParseUser> getParseUsers(){
        return parseUsers;
    }
}
