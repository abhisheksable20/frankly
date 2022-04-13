package com.sakesnake.frankly.home.profileSupporters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sakesnake.frankly.R;
import com.sakesnake.frankly.parsedatabase.ParseConstants;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

// Recycler view adapter for profile supporters
public class ProfileSupportersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ParseUser> parseUserList;

    private Context mContext;

    private boolean isDeletePermissionAvail;

    private final int PROFILE_SUPPORTER_VIEW = 101;

    private final int LOADING_VIEW = 102;

    private SupporterCallback supporterCallback;

    public ProfileSupportersAdapter(List<ParseUser> parseUserList, Context mContext, boolean grantDelPermission,SupporterCallback supporterCallback) {
        this.parseUserList = parseUserList;
        this.mContext = mContext;
        this.isDeletePermissionAvail = grantDelPermission;
        this.supporterCallback = supporterCallback;
    }

    public class ProfileSupporterViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicImageView;
        TextView usernameTextView, originalNameTextView;
        CardView deleteUserCardView;
        ConstraintLayout rootLayout;
        public ProfileSupporterViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicImageView = (ImageView) itemView.findViewById(R.id.supporter_profile_pic_image_view);
            usernameTextView = (TextView) itemView.findViewById(R.id.supporter_username_text_view);
            originalNameTextView = (TextView) itemView.findViewById(R.id.supporter_original_name_text_view);
            deleteUserCardView = (CardView) itemView.findViewById(R.id.delete_supporter_card_view);
            rootLayout = (ConstraintLayout) itemView.findViewById(R.id.supporter_root_constraint_layout);

            setClickListener();
        }

        private void setClickListener(){
            // Navigate to user or currentUser fragment
            rootLayout.setOnClickListener(view -> navigateToProfileFragment(getAdapterPosition()));

            // Remove the user from watchlist
            deleteUserCardView.setOnClickListener(view -> {
                if (isDeletePermissionAvail)
                    removeUserFromWatchlist(getAdapterPosition());
            });
        }

        // Navigating to user or currentUser fragment
        private void navigateToProfileFragment(final int position){
            if (supporterCallback != null)
                supporterCallback.callbackWithDeleteOption(parseUserList.get(position),false);
        }


        // Removing the user from watchlist
        private void removeUserFromWatchlist(final int position){
            if (supporterCallback != null) {
                supporterCallback.callbackWithDeleteOption(parseUserList.get(position), true);
                parseUserList.remove(position);
                notifyItemRemoved(position);
            }
        }
    }

    public class LoadingUserViewHolder extends RecyclerView.ViewHolder {
        public LoadingUserViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == LOADING_VIEW)
            return new LoadingUserViewHolder(LayoutInflater.from(mContext).inflate(R.layout.bottom_progress_bar, parent,false));
        else
            return new ProfileSupporterViewHolder(LayoutInflater.from(mContext).inflate(R.layout.supporters_list_item, parent,false));
    }


    @Override
    public int getItemViewType(int position) {
        if (parseUserList.get(position) == null)
            return LOADING_VIEW;
        else
            return PROFILE_SUPPORTER_VIEW;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LoadingUserViewHolder)
            return;

        ProfileSupporterViewHolder viewHolder = (ProfileSupporterViewHolder) holder;

        if (isDeletePermissionAvail)
            viewHolder.deleteUserCardView.setVisibility(View.VISIBLE);
        else
            viewHolder.deleteUserCardView.setVisibility(View.GONE);

        ParseUser user = parseUserList.get(position);
        ParseFile profilePic = user.getParseFile(ParseConstants.PROFILE_PIC);
        if (profilePic != null)
            Glide.with(mContext).load(profilePic.getUrl()).into(viewHolder.profilePicImageView);

        viewHolder.usernameTextView.setText(user.getUsername());
        viewHolder.originalNameTextView.setText(user.getString(ParseConstants.PROFILE_ORIGINAL_NAME));
    }

    @Override
    public int getItemCount() {
        return parseUserList.size();
    }

    // Showing bottom progress bar
    public void showProgressBar(){
        parseUserList.add(null);
        notifyItemInserted(parseUserList.size() - 1);
    }

    // Hiding bottom progress bar
    public void hideProgressBar(){
        if (parseUserList.get(parseUserList.size() - 1) == null){
            parseUserList.remove(parseUserList.size() - 1);
            notifyItemRemoved(parseUserList.size() - 1);
        }
    }

    // Appending the list with current list
    public void appendTheData(final List<ParseUser> latestParseUserList){
        hideProgressBar();
        int lastPosition = parseUserList.size() - 1;
        parseUserList.addAll(latestParseUserList);
        notifyItemRangeInserted(lastPosition,latestParseUserList.size());
    }

    // Get adapter current list data
    public ArrayList<ParseUser> getCurrentList(){
        return new ArrayList<>(parseUserList);
    }
}
